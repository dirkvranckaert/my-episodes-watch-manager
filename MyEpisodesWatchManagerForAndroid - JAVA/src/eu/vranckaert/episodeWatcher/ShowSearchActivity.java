package eu.vranckaert.episodeWatcher;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import eu.vranckaert.episodeWatcher.domain.Show;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.exception.UnsupportedHttpPostEncodingException;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.service.MyEpisodesService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dirk Vranckaert
 *         Date: 18-sep-2010
 *         Time: 17:20:41
 */
public class ShowSearchActivity extends ListActivity {
    private static final String LOG_TAG = "SHOW_SEARCH_AVTIVITY";

    private static final int SEARCH_DIALOG = 0;

    private MyEpisodesService service;
    private User user;
    private ShowAdapter showAdapter;
    private List<Show> shows = new ArrayList<Show>(0);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        init(savedInstanceState);

        ImageButton searchButton = (ImageButton) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence query = ((EditText) findViewById(R.id.searchQuery)).getText();
                if(query.length() > 0) {
                    ShowSearchActivity.this.searchShows();
                } else {
                    //TODO show message: enter a show name to search for!!
                }
            }
        });
    }

    private void init(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showsearch);

        service = new MyEpisodesService();
        user = new User(
            Preferences.getPreference(this, User.USERNAME),
            Preferences.getPreference(this, User.PASSWORD)
        );

        initializeShowList();
    }

    private void initializeShowList() {
        showAdapter = new ShowAdapter(this, R.layout.searchresultsshowsrow, shows);
        setListAdapter(showAdapter);
    }

    private void updateShowList() {
        showAdapter.clear();
        for(Show show : shows) {
            showAdapter.add(show);
        }
        showAdapter.notifyDataSetChanged();
    }

    @Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
			case SEARCH_DIALOG:
				ProgressDialog progressDialog = new ProgressDialog(this);
				progressDialog.setMessage(this.getString(R.string.progressLoadingTitle));
                progressDialog.setCancelable(false);
				dialog = progressDialog;
				break;
        }
        return dialog;
    }

    private void searchShows() {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                showDialog(SEARCH_DIALOG);
            }

            @Override
            protected Object doInBackground(Object... objects) {
                doSearch(); //TODO handle excpetions
                return 100L;
            }

            @Override
            protected void onPostExecute(Object o) {
                updateShowList();
                dismissDialog(SEARCH_DIALOG);
            }
        };
        asyncTask.execute();
    }

    private void doSearch() {
        CharSequence query = ((EditText) findViewById(R.id.searchQuery)).getText();
        try {
            shows = service.searchShows(query.toString(), user);
            Log.d(LOG_TAG, shows.size() + " show(s) found!!!");
        } catch (UnsupportedHttpPostEncodingException e) {
            String message = "Network issues";
			Log.e(LOG_TAG, message, e);
			//exceptionMessageResId = R.string.networkIssues;
        } catch (InternetConnectivityException e) {
            String message = "Could not connect to host";
			Log.e(LOG_TAG, message, e);
			//exceptionMessageResId = R.string.internetConnectionFailureReload;
        } catch (LoginFailedException e) {
            String message = "Login failure";
			Log.e(LOG_TAG, message, e);
			//exceptionMessageResId = R.string.networkIssues;
        }
    }

    private class ShowAdapter extends ArrayAdapter<Show> {
        private List<Show> shows;

        public ShowAdapter(Context context, int textViewResourceId, List<Show> el) {
            super(context, textViewResourceId, el);
            this.shows = el;
        }

        @Override
        public View getView(int posistion, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row==null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.searchresultsshowsrow, parent, false);
            }

            TextView topText = (TextView) row.findViewById(R.id.showNameSearchResult);

            Show show = shows.get(posistion);
            topText.setText(show.getShowName());

            return row;
        }
    }
}
