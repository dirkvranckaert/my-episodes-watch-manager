package eu.vranckaert.episodeWatcher;

import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.service.MyEpisodesService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    private Button loginButton;
    private MyEpisodesService myEpisodesService;
    private User user;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        init();
        
        setContentView(R.layout.login);
        
        loginButton = (Button) findViewById(R.id.loginLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {				
				user = new User(
						((EditText) findViewById(R.id.loginUsername)).getText().toString(),
						((EditText) findViewById(R.id.loginPassword)).getText().toString()
				);
				
		    	Toast.makeText(MainActivity.this, R.string.loginStartLogin, Toast.LENGTH_SHORT).show();
		    	int status = 0;
				try {
					status = myEpisodesService.login(user);
					Toast.makeText(MainActivity.this, R.string.loginSuccessfullLogin, Toast.LENGTH_LONG).show();
					Intent watchListIntent = new Intent(v.getContext(), EpisodesWatchListActivity.class);
					watchListIntent.putExtra(User.USERNAME, user.getUsername());
					watchListIntent.putExtra(User.PASSWORD, user.getPassword());
	                startActivityForResult(watchListIntent, 0);
				} catch (LoginFailedException e) {
					Toast.makeText(MainActivity.this, R.string.loginLoginFailed, Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					Toast.makeText(MainActivity.this, R.string.loginLoginFailedUnhandledException, Toast.LENGTH_LONG).show();
				}
			}
		});
    }
    
    private void init() {
    	this.myEpisodesService = new MyEpisodesService();
    }
}