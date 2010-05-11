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
import eu.vranckaert.episodeWatcher.utils.Preferences;

public class LoginSubActivity extends Activity {
    private Button loginButton;
    private MyEpisodesService myEpisodesService;
    private User user;
    
    private static final String LOG_TAG = "LoginSubActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        init();
        
        if (!checkLoginCredentials()) {
        	GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
        	tracker.trackPageView("loginSubActivity");
        	
	        setContentView(R.layout.login);
	        
	        loginButton = (Button) findViewById(R.id.loginLogin);
	        loginButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
                    String username = ((EditText) findViewById(R.id.loginUsername)).getText().toString();
				    String password = ((EditText) findViewById(R.id.loginPassword)).getText().toString();

                    if( (username!= null && username.length()>0) && (password!=null && password.length()>0) ) {
                        user = new User(
                                username, password
                        );

                        Toast.makeText(LoginSubActivity.this, R.string.loginStartLogin, Toast.LENGTH_SHORT).show();
                        try {
                            boolean loginStatus = myEpisodesService.login(user);
                            if(loginStatus)
                            {
                                Toast.makeText(LoginSubActivity.this, R.string.loginSuccessfullLogin, Toast.LENGTH_LONG).show();
                                storeLoginCredentials(user);
                                finalizeLogin();
                            }
                        } catch (InternetConnectivityException e) {
                            String message = "Could not connect to host";
                            Log.e(LOG_TAG, message, e);
                            Toast.makeText(LoginSubActivity.this, R.string.internetConnectionFailureTryAgain, Toast.LENGTH_LONG).show();
                        } catch (LoginFailedException e) {
                            String message = "Login failed";
                            //((EditText) findViewById(R.id.loginUsername)).setText("");
                            ((EditText) findViewById(R.id.loginPassword)).setText("");
                            Log.e(LOG_TAG, message, e);
                            Toast.makeText(LoginSubActivity.this, R.string.loginLoginFailed, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            String message = "Some Exception occured";
                            Log.e(LOG_TAG, message, e);
                            Toast.makeText(LoginSubActivity.this, R.string.defaultExceptionMessage, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LoginSubActivity.this, R.string.fillInAllFields, Toast.LENGTH_LONG).show();
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