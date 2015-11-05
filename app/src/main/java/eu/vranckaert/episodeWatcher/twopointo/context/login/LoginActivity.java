package eu.vranckaert.episodeWatcher.twopointo.context.login;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.User;
import eu.vranckaert.episodeWatcher.exception.InternetConnectivityException;
import eu.vranckaert.episodeWatcher.exception.LoginFailedException;
import eu.vranckaert.episodeWatcher.preferences.Preferences;
import eu.vranckaert.episodeWatcher.service.UserService;
import eu.vranckaert.android.threading.CustomTask;
import eu.vranckaert.android.threading.ErrorMapping;
import eu.vranckaert.episodeWatcher.twopointo.context.NavigationManager;
import eu.vranckaert.android.context.BaseActivity;
import eu.vranckaert.episodeWatcher.twopointo.view.login.LoginView;
import eu.vranckaert.episodeWatcher.twopointo.view.login.LoginView.LoginListener;

/**
 * Date: 03/11/15
 * Time: 17:10
 *
 * @author Dirk Vranckaert
 */
public class LoginActivity extends BaseActivity implements LoginListener {
    private LoginTask mLoginTask;
    private LoginView mView;

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        setTitle(R.string.loginLoginBtn);
        setHomeAsUpEnabled();
    }

    @Override
    protected View doCreateView() {
        mView = new LoginView(this, this);
        return mView.getView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (mLoginTask != null) {
            mLoginTask.cancel();
            mLoginTask = null;
        }

        super.onDestroy();
    }

    @Override
    public void login(String username, String password) {
        if (mLoginTask != null) {
            mLoginTask.cancel();
        }
        mLoginTask = new LoginTask(this, new User(username, password));
        mLoginTask.execute();
    }

    @Override
    public void register() {
        NavigationManager.openUrl(this, "http://www.myepisodes.com/register.php");
    }

    public final class LoginTask extends CustomTask<Boolean> {
        private final LoginActivity mActivity;
        private final User mUser;

        public LoginTask(LoginActivity logonActivity, User user) {
            super(logonActivity);
            mActivity = logonActivity;
            mUser = user;
        }

        @Override
        public ErrorMapping getErrorMapping(Exception e) {
            if (e instanceof InternetConnectivityException) {
                return new ErrorMapping.Builder()
                        .setMessage(getString(R.string.internetConnectionFailureTryAgain))
                        .build();
            } else if (e instanceof LoginFailedException) {
                return new ErrorMapping.Builder()
                        .setMessage(getString(R.string.loginLoginFailed))
                        .build();
            }

            return super.getErrorMapping(e);
        }

        @Override
        public void onError(Exception exception) {
            if (exception instanceof LoginFailedException) {
                mActivity.mView.resetPasswordField();
            }
        }

        @Override
        public Boolean doInBackground() throws Exception {
            return new UserService().login(mUser);
        }

        @Override
        public void onTaskCompleted(Boolean result) {
            if (result) {
                Preferences.setPreference(mActivity, User.USERNAME, mUser.getUsername());
                Preferences.setPreference(mActivity, User.PASSWORD, mUser.getPassword());
                NavigationManager.restartApplication(mActivity);
            }
        }
    }
}
