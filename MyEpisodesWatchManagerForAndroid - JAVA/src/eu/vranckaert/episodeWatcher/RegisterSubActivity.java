package eu.vranckaert.episodeWatcher;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.service.MyEpisodesService;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import eu.vranckaert.episodeWatcher.preferences.Preferences;

public class RegisterSubActivity extends Activity {
    private Button registerButton;
    private MyEpisodesService myEpisodesService;
    private User user;
    
    private static final String LOG_TAG = "RegisterSubActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        init();
        
        if (!checkLoginCredentials()) {
        	GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
        	tracker.trackPageView("registerSubActivity");
        	
	        setContentView(R.layout.register);
	        
	        registerButton = (Button) findViewById(R.id.registerRegister);
	        registerButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
                    String username = ((EditText) findViewById(R.id.registerUsername)).getText().toString();
				    String password = ((EditText) findViewById(R.id.registerPassword)).getText().toString();
				    String email = ((EditText) findViewById(R.id.registerEmail)).getText().toString();
                    if( (username!= null && username.length()>0) && (password != null && password.length()>0) && (email != null && email.length()>0)) {
                        user = new User(
                                username, password
                        );

                        Toast.makeText(RegisterSubActivity.this, R.string.registerStart, Toast.LENGTH_SHORT).show();
                        try {
                            boolean registerStatus = myEpisodesService.register(user, email);
                            if(registerStatus)
                            {
                                Toast.makeText(RegisterSubActivity.this, R.string.registerSuccessfull, Toast.LENGTH_LONG).show();
                                storeLoginCredentials(user);
                                finalizeLogin();
                            } else {
                            	Toast.makeText(RegisterSubActivity.this, R.string.registerFailed, Toast.LENGTH_LONG).show();
                                ((EditText) findViewById(R.id.registerUsername)).setText("");
                                ((EditText) findViewById(R.id.registerPassword)).setText("");
                                ((EditText) findViewById(R.id.registerEmail)).setText("");
                            }
                        } catch (InternetConnectivityException e) {
                            ((EditText) findViewById(R.id.registerUsername)).setText("");
                            ((EditText) findViewById(R.id.registerPassword)).setText("");
                            ((EditText) findViewById(R.id.registerEmail)).setText("");
                            String message = "Could not connect to host";
                            Log.e(LOG_TAG, message, e);
                            Toast.makeText(RegisterSubActivity.this, R.string.internetConnectionFailureTryAgain, Toast.LENGTH_LONG).show();
                        } catch (LoginFailedException e) {
                            String message = "Register failed";
                            Log.e(LOG_TAG, message, e);
                            Toast.makeText(RegisterSubActivity.this, R.string.registerFailed, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            String message = "Some Exception occured";
                            Log.e(LOG_TAG, message, e);
                            Toast.makeText(RegisterSubActivity.this, R.string.defaultExceptionMessage, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(RegisterSubActivity.this, R.string.fillInAllFields, Toast.LENGTH_LONG).show();
                    }
				}
			});
        } else {
        	finalizeLogin();
        }
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
    	this.myEpisodesService = new MyEpisodesService();
    }
}