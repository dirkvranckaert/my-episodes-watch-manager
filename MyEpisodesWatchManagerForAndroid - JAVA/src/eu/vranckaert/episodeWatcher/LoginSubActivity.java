package eu.vranckaert.episodeWatcher;

import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.service.MyEpisodesService;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginSubActivity extends Activity {
    private Button loginButton;
    private MyEpisodesService myEpisodesService;
    private User user;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        init();
        
        if (!checkLoginCredentials()) {
	        setContentView(R.layout.login);
	        
	        loginButton = (Button) findViewById(R.id.loginLogin);
	        loginButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {				
					user = new User(
							((EditText) findViewById(R.id.loginUsername)).getText().toString(),
							((EditText) findViewById(R.id.loginPassword)).getText().toString()
					);
					
			    	Toast.makeText(LoginSubActivity.this, R.string.loginStartLogin, Toast.LENGTH_SHORT).show();
			    	int status = 0;
					try {
						status = myEpisodesService.login(user);
						Toast.makeText(LoginSubActivity.this, R.string.loginSuccessfullLogin, Toast.LENGTH_LONG).show();
						
						storeLoginCredentials(user);
						finalizeLogin();
					} catch (LoginFailedException e) {
						Toast.makeText(LoginSubActivity.this, R.string.loginLoginFailed, Toast.LENGTH_LONG).show();
						e.printStackTrace();
					} catch (Exception e) {
						Toast.makeText(LoginSubActivity.this, R.string.loginLoginFailedUnhandledException, Toast.LENGTH_LONG).show();
						e.printStackTrace();
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