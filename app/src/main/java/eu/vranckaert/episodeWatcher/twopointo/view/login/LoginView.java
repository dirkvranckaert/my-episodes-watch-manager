package eu.vranckaert.episodeWatcher.twopointo.view.login;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import eu.vranckaert.android.viewholder.AbstractViewHolder;
import eu.vranckaert.episodeWatcher.BuildConfig;
import eu.vranckaert.episodeWatcher.R;

/**
 * Date: 03/11/15
 * Time: 22:02
 *
 * @author Dirk Vranckaert
 */
public class LoginView extends AbstractViewHolder implements OnClickListener {
    private final LoginListener mListener;

    private final EditText mUsername;
    private final EditText mPassword;

    public LoginView(Context context, LoginListener listener) {
        super(context, R.layout.new_login);
        mListener = listener;

        mUsername = findViewById(R.id.username);
        mPassword = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(this);
        Button registerButton = findViewById(R.id.register);
        registerButton.setOnClickListener(this);

        if (BuildConfig.DEBUG) {
            mUsername.setText("dirken");
        }
    }

    public void setUsername(String username) {
        mUsername.setText(username);
        mUsername.post(new Runnable() {
            @Override
            public void run() {
                mPassword.requestFocus();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login) {
            String username = mUsername.getText().toString();
            String password = mPassword.getText().toString();
            mListener.login(username, password);
        } else if (v.getId() == R.id.register) {
            mListener.register();
        }
    }

    public void resetPasswordField() {
        mPassword.setText(null);
    }

    public interface LoginListener {
        void login(String username, String password);
        void register();
    }
}
