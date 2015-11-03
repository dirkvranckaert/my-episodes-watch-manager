package eu.vranckaert.episodeWatcher.twopointo.context;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import eu.vranckaert.episodeWatcher.R;

/**
 * Date: 03/11/15
 * Time: 17:10
 *
 * @author Dirk Vranckaert
 */
public class LoginActivity extends BaseActivity {
    @Override
    protected void doCreate(Bundle savedInstanceState) {
        setTitle(R.string.loginLoginBtn);
        setHomeAsUpEnabled();
    }

    @Override
    protected View doCreateView() {
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}
