package eu.vranckaert.episodeWatcher.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.enums.CustomTracker;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.exception.UnsupportedHttpPostEncodingException;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.service.UserService;
import eu.vranckaert.episodeWatcher.utils.CustomAnalyticsTracker;
import roboguice.activity.GuiceActivity;

public class RegisterActivity extends GuiceActivity {
    private Button registerButton;
    private UserService service;
    private User user;
    private boolean registerStatus;
    private String email;
    
    private static final int MY_EPISODES_REGISTER_DIALOG_LOADING = 0;
    private static final int MY_EPISODES_ERROR_DIALOG = 1;
    private static final int MY_EPISODES_VALIDATION_REQUIRED_ALL_FIELDS = 2;

    private static final String LOG_TAG = RegisterActivity.class.getSimpleName();
    CustomAnalyticsTracker tracker;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light_NoTitleBar : android.R.style.Theme_NoTitleBar);
    	super.onCreate(savedInstanceState);
        
        init();
        
    	tracker = CustomAnalyticsTracker.getInstance(this);
        
        if (!checkLoginCredentials()) {
        	tracker.trackPageView(CustomTracker.PageView.REGISTER_USER);
        	setContentView(R.layout.register);
	        
	        registerButton = (Button) findViewById(R.id.registerRegister);
	        registerButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
                    String username = ((EditText) findViewById(R.id.registerUsername)).getText().toString();
				    String password = ((EditText) findViewById(R.id.registerPassword)).getText().toString();
				    email = ((EditText) findViewById(R.id.registerEmail)).getText().toString();
                    if( (username!= null && username.length()>0) && (password != null && password.length()>0) && (email != null && email.length()>0)) {
                        user = new User(
                                username, password
                        );
                        registerStatus = false;

                        AsyncTask<Object, Object, Object> asyncTask = new AsyncTask<Object, Object, Object>()  {
                            @Override
                            protected void onPreExecute() {
                                showDialog(MY_EPISODES_REGISTER_DIALOG_LOADING);
                            }

                            @Override
                            protected Object doInBackground(Object... objects) {
									try {
										registerStatus = register(user);
							        } catch (InternetConnectivityException e) {
							            String message = "Could not connect to host";
							            Log.e(LOG_TAG, message, e);
							        } catch (LoginFailedException e) {
							            String message = "Login failed";
							            Log.e(LOG_TAG, message, e);
							        } catch (Exception e) {
							            String message = "Some Exception occured";
							            Log.e(LOG_TAG, message, e);
							        }
                                if(registerStatus) {
                                    storeLoginCredentials(user);
                                }
                                return 100L;
                            }

							@Override
                            protected void onPostExecute(Object o) {
                                removeDialog(MY_EPISODES_REGISTER_DIALOG_LOADING);
                                if(registerStatus) {
                                    Toast.makeText(RegisterActivity.this, R.string.registerSuccessfull, Toast.LENGTH_LONG).show();
                                    finalizeLogin();
                                } else {
                                    ((EditText) findViewById(R.id.registerUsername)).setText("");
                                    ((EditText) findViewById(R.id.registerPassword)).setText("");
                                    ((EditText) findViewById(R.id.registerEmail)).setText("");
                                    showDialog(MY_EPISODES_ERROR_DIALOG);
                                }
                            }
                        };
                        asyncTask.execute();
                    } else {
                    	showDialog(MY_EPISODES_VALIDATION_REQUIRED_ALL_FIELDS);
                    }
				}
			});
        } else {
        	finalizeLogin();
        }
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
			case MY_EPISODES_REGISTER_DIALOG_LOADING:
				ProgressDialog progressDialog = new ProgressDialog(this);
				progressDialog.setMessage(this.getString(R.string.registerStart));
                progressDialog.setCancelable(false);
				dialog = progressDialog;
				break;
            case MY_EPISODES_ERROR_DIALOG:
                AlertDialog errorDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.registerFailed)
                        .setCancelable(false)
                        .setNeutralButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).create();
                dialog = errorDialog;
                break;
            case MY_EPISODES_VALIDATION_REQUIRED_ALL_FIELDS:
                AlertDialog validationRequiredAllFieldsDialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.fillInAllFields)
                        .setCancelable(false)
                        .setNeutralButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).create();
                dialog = validationRequiredAllFieldsDialog;
                break;
		}
		return dialog;
    }
    
    private boolean checkLoginCredentials() {
		String username = Preferences.getPreference(this, User.USERNAME);
		String password = Preferences.getPreference(this, User.PASSWORD);
		
		if (username == null || password == null) {
			return false;
		} else {
			return true;
		}
	}
    
    private boolean register(User user) throws LoginFailedException, UnsupportedHttpPostEncodingException, InternetConnectivityException {
    	return service.register(user, email);
	}
    
    private void storeLoginCredentials(User user) {
    	Preferences.setPreference(this, User.USERNAME, user.getUsername());
    	Preferences.setPreference(this, User.PASSWORD, user.getPassword());
    }

	private void finalizeLogin() {
    	setResult(RESULT_OK);
		finish();
    }
    
    private void init() {
    	this.service = new UserService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stop();
    }
}