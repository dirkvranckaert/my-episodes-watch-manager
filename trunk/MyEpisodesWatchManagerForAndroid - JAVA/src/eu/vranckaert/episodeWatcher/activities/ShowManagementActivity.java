package eu.vranckaert.episodeWatcher.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.enums.CustomTracker;
import eu.vranckaert.episodeWatcher.enums.ShowAction;
import eu.vranckaert.episodeWatcher.enums.ShowType;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.exception.UnsupportedHttpPostEncodingException;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.service.ShowService;
import eu.vranckaert.episodeWatcher.utils.CustomAnalyticsTracker;
import roboguice.activity.GuiceListActivity;

import java.util.ArrayList;
import java.util.List;

public class ShowManagementActivity extends GuiceListActivity {
    private static final String LOG_TAG = ShowManagementActivity.class.getSimpleName();
    private ShowType showType;
    private User user;
    private ShowService service;
    private ShowAdapter showAdapter;
    private List<Show> shows = new ArrayList<Show>(0);

    private int selectedShow = -1;
    private ShowAction showAction = null;
    private int confirmationMessageResId = -1;
    private Integer exceptionMessageResId = null;

    private static final int DIALOG_LOADING = 0;
    private static final int DIALOG_EXCEPTION = 1;

    private static final int CONTEXT_MENU_DELETE = 0;
    private static final int CONTEXT_MENU_UNIGNORE = 1;
    private static final int CONTEXT_MENU_IGNORE = 2;
    private static final int CONFIRMATION_DIALOG = 3;

    CustomAnalyticsTracker tracker = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	init(savedInstanceState);
        startAnalyticsTracking();

        reloadShows();
    }

    private void init(Bundle savedInstanceState) {
        setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light : android.R.style.Theme);
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.show_management);

        Bundle data = this.getIntent().getExtras();
        showType = (ShowType) data.get(ShowType.class.getSimpleName());

        if(showType.equals(ShowType.FAVOURITE_SHOWS)) {
            Log.d(LOG_TAG, "Opening the favourite shows");
        } else if(showType.equals(ShowType.IGNORED_SHOWS)) {
            Log.d(LOG_TAG, "Opening the ignored shows");
        }

        user = new User(
            Preferences.getPreference(this, User.USERNAME),
            Preferences.getPreference(this, User.PASSWORD)
        );

        initializeShowList();

        service = new ShowService();
    }

    private void startAnalyticsTracking() {
        tracker = CustomAnalyticsTracker.getInstance(this);
        if(showType.equals(ShowType.FAVOURITE_SHOWS)) {
            tracker.trackPageView(CustomTracker.PageView.SHOW_MANAGEMENT_FAVOS);
        } else if(showType.equals(ShowType.IGNORED_SHOWS)) {
            tracker.trackPageView(CustomTracker.PageView.SHOW_MANAGEMENT_IGNORED);
        }
    }

    @Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;

		switch (id) {
			case DIALOG_LOADING: {
				ProgressDialog progressDialog = new ProgressDialog(this);
				progressDialog.setMessage(this.getString(R.string.progressLoadingTitle));
                progressDialog.setCancelable(false);
				dialog = progressDialog;
				break;
            }
            case DIALOG_EXCEPTION: {
				if (exceptionMessageResId == null) {
					exceptionMessageResId = R.string.defaultExceptionMessage;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.exceptionDialogTitle)
					   .setMessage(exceptionMessageResId)
					   .setCancelable(false)
					   .setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
                                exceptionMessageResId = null;
                                removeDialog(DIALOG_EXCEPTION);
				           }
				       });
				dialog = builder.create();
                break;
            }
            case CONFIRMATION_DIALOG: {
                if(selectedShow > -1) {
                    final Show show = shows.get(selectedShow);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(show.getShowName())
                           .setMessage(confirmationMessageResId)
                           .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    selectedShow = -1;
                                    confirmationMessageResId = -1;
                                    showAction = null;
                                    removeDialog(CONFIRMATION_DIALOG);
                                }
                           })
                           .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    removeDialog(CONFIRMATION_DIALOG);
                                    markShow(show, showAction);

                                    selectedShow = -1;
                                    confirmationMessageResId = -1;
                                    showAction = null;
                                }
                            });
				    dialog = builder.create();
                }
                break;
            }
        }
        return dialog;
    }

    private void reloadShows() {
    	AsyncTask<Object, Object, Object> asyncTask = new AsyncTask<Object, Object, Object>()  {
            @Override
            protected void onPreExecute() {
                showDialog(DIALOG_LOADING);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                getShows(user, showType);
                return 100L;
            }

            @Override
            protected void onPostExecute(Object o) {
                if(exceptionMessageResId != null && !exceptionMessageResId.equals("")) {
                    removeDialog(DIALOG_LOADING);
                    showDialog(DIALOG_EXCEPTION);
                } else {
                    updateShowList();
                    removeDialog(DIALOG_LOADING);
                }
            }
        };
        asyncTask.execute();
    }

    private void getShows(User user, ShowType showType) {
        try {
            shows = service.getFavoriteOrIgnoredShows(user, showType);
        } catch (UnsupportedHttpPostEncodingException e) {
            String message = "Network issues";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.networkIssues;
        } catch (InternetConnectivityException e) {
            String message = "Could not connect to host";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.internetConnectionFailureReload;
        } catch (LoginFailedException e) {
            String message = "Login failure";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.networkIssues;
        }
    }

    private void updateShowList() {
        showAdapter.clear();
        for(Show show : shows) {
            showAdapter.add(show);
        }
        showAdapter.notifyDataSetChanged();
    }

    private void initializeShowList() {
        showAdapter = new ShowAdapter(this, R.layout.show_management_add_row, shows);
        setListAdapter(showAdapter);
        registerForContextMenu(getListView());
    }

    private class ShowAdapter extends ArrayAdapter<Show> {
        private List<Show> shows;

        public ShowAdapter(Context context, int textViewResourceId, List<Show> el) {
            super(context, textViewResourceId, el);
            this.shows = el;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row==null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.show_management_add_row, parent, false);
            }

            TextView topText = (TextView) row.findViewById(R.id.showNameSearchResult);

            Show show = shows.get(position);
            topText.setText(show.getShowName());

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openContextMenu(view);
                }
            });

            return row;
        }
    }

    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        switch(showType) {
            case FAVOURITE_SHOWS:
                menu.add(Menu.NONE, CONTEXT_MENU_IGNORE, Menu.NONE, R.string.favoIgnoredIgnoreShow);
                break;
            case IGNORED_SHOWS:
                menu.add(Menu.NONE, CONTEXT_MENU_UNIGNORE, Menu.NONE, R.string.favoIgnoredUnignoreShow);
                break;
        }

        menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, R.string.favoIgnoredDeleteShow);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        selectedShow = info.position;
        switch(item.getItemId()) {
            case CONTEXT_MENU_IGNORE:
                showDialog(CONFIRMATION_DIALOG, info.position, ShowAction.IGNORE, R.string.favoIgnoredConfirmationIgnoreMessage);
                break;
            case CONTEXT_MENU_UNIGNORE:
                showDialog(CONFIRMATION_DIALOG, info.position, ShowAction.UNIGNORE, R.string.favoIgnoredConfirmationUnignoreMessage);
                break;
            case CONTEXT_MENU_DELETE:
                showDialog(CONFIRMATION_DIALOG, info.position, ShowAction.DELETE, R.string.favoIgnoredConfirmationDeleteMessage);
                break;
            default:
                return false;
        }
        return true;
    }

    private void showDialog(int dialogId, int listPosition, ShowAction action, int messageId) {
        this.selectedShow = listPosition;
        this.showAction = action;
        this.confirmationMessageResId = messageId;

        showDialog(dialogId);
    }

    private void markShow(final Show show, final ShowAction action) {
    	AsyncTask<Object, Object, Object> asyncTask = new AsyncTask<Object, Object, Object>() {
            @Override
            protected void onPreExecute() {
                showDialog(DIALOG_LOADING);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                switch(action) {
                    case IGNORE:
                        tracker.trackEvent(CustomTracker.Event.SHOW_INGORE);
                        break;
                    case UNIGNORE:
                        tracker.trackEvent(CustomTracker.Event.SHOW_UNIGNORE);
                        break;
                    case DELETE:
                        tracker.trackEvent(CustomTracker.Event.SHOW_DELETE);
                        break;
                }

                markShow(user, showType, action, show);
                return 100L;
            }

            @Override
            protected void onPostExecute(Object o) {
                if(exceptionMessageResId != null && !exceptionMessageResId.equals("")) {
                    removeDialog(DIALOG_LOADING);
                    showDialog(DIALOG_EXCEPTION);
                } else {
                    updateShowList();
                    removeDialog(DIALOG_LOADING);
                }
            }
        };
        asyncTask.execute();
    }

    private void markShow(User user, ShowType showType, ShowAction showAction, Show show) {
        try {
            shows = service.markShow(user, show, showAction, showType);
        } catch (UnsupportedHttpPostEncodingException e) {
            String message = "Network issues";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.networkIssues;
        } catch (InternetConnectivityException e) {
            String message = "Could not connect to host";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.internetConnectionFailureReload;
        } catch (LoginFailedException e) {
            String message = "Login failure";
			Log.e(LOG_TAG, message, e);
			exceptionMessageResId = R.string.networkIssues;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stop();
    }
}
