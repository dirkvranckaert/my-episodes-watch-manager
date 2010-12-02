package eu.vranckaert.episodeWatcher.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.preferences.PreferencesKeys;
import eu.vranckaert.episodeWatcher.service.UserService;

public class LoginSubActivity extends Activity {
    private Button loginButton;
    private TextView register;
    private UserService service;
    private int exceptionMessageResId = -1;

    private static final int MY_EPISODES_LOGIN_DIALOG_LOADING = 0;
    private static final int MY_EPISODES_ERROR_DIALOG = 1;
    private static final int MY_EPISODES_VALIDATION_REQUIRED_ALL_FIELDS = 2;
    
    private static final String LOG_TAG = "LoginSubActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(Preferences.getPreferenceInt(this, PreferencesKeys.THEME_KEY) == 0 ? android.R.style.Theme_Light : android.R.style.Theme);        
    	super.onCreate(savedInstanceState);
        init();
        
        if (!checkLoginCredentials()) {
        	GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
        	tracker.trackPageView("loginSubActivity");

	        setContentView(R.layout.login);

	    	register = (TextView) findViewById(R.id.registerForm);
	    	register.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
			    	openRegisterScreen();
				}
			});

	        loginButton = (Button) findViewById(R.id.loginLogin);
	        loginButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
                    String username = ((EditText) findViewById(R.id.loginUsername)).getText().toString();
				    String password = ((EditText) findViewById(R.id.loginPassword)).getText().toString();

                    if( (username != null && username.length()>0) && (password != null && password.length()>0) ) {
                        final User user = new User(
                                username, password
                        );

                        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask() {
                            boolean loginStatus = false;

                            @Override
                            protected void onPreExecute() {
                                showDialog(MY_EPISODES_LOGIN_DIALOG_LOADING);
                            }

                            @Override
                            protected Object doInBackground(Object... objects) {
                                loginStatus = login(user);
                                if(loginStatus) {
                                    storeLoginCredentials(user);
                                }
                                return 100L;
                            }

                            @Override
                            protected void onPostExecute(Object o) {
                                removeDialog(MY_EPISODES_LOGIN_DIALOG_LOADING);
                                if(loginStatus) {
                                    Toast.makeText(LoginSubActivity.this, R.string.loginSuccessfullLogin, Toast.LENGTH_LONG).show();
                                    finalizeLogin();
                                } else {
                                    ((EditText) findViewById(R.id.loginUsername)).setText("");
                                    ((EditText) findViewById(R.id.loginPassword)).setText("");
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
			case MY_EPISODES_LOGIN_DIALOG_LOADING:
				ProgressDialog progressDialog = new ProgressDialog(this);
				progressDialog.setMessage(this.getString(R.string.loginStartLogin));
                progressDialog.setCancelable(false);
				dialog = progressDialog;
				break;
            case MY_EPISODES_ERROR_DIALOG:
                AlertDialog errorDialog = new AlertDialog.Builder(this)
                        .setMessage(exceptionMessageResId)
                        .setCancelable(false)
                        .setNeutralButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                removeDialog(MY_EPISODES_ERROR_DIALOG);
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
            default:
				dialog = super.onCreateDialog(id);
				break;
		}
		return dialog;
	}

    private boolean login(User user) {
        boolean loginStatus = false;
        try {
            loginStatus = service.login(user);
        } catch (InternetConnectivityException e) {
            String message = "Could not connect to host";
            Log.e(LOG_TAG, message, e);
            exceptionMessageResId = R.string.internetConnectionFailureTryAgain;
        } catch (LoginFailedException e) {
            String message = "Login failed";
            Log.e(LOG_TAG, message, e);
            exceptionMessageResId = R.string.loginLoginFailed;
        } catch (Exception e) {
            String message = "Some Exception occured";
            Log.e(LOG_TAG, message, e);
            exceptionMessageResId = R.string.defaultExceptionMessage;
        }
        return loginStatus;
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
    
	private void openRegisterScreen() {
    	Intent registerActivity = new Intent(this.getApplicationContext(), RegisterSubActivity.class);
    	startActivity(registerActivity);
	}
}