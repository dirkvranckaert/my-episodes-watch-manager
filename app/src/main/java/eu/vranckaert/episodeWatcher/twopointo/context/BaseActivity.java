package eu.vranckaert.episodeWatcher.twopointo.context;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import eu.vranckaert.episodeWatcher.R;

/**
 * Date: 11/06/15
 * Time: 23:21
 *
 * @author Dirk Vranckaert
 */
public abstract class BaseActivity extends AppCompatActivity {
    private View mView;
    private Toolbar mToolbar;
    private boolean mHomeAsUpEnabled;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mView == null) {
            mView = doCreateView();
        }
        setContentView(mView);

        // Setup the ToolBar
        mToolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        doCreate(savedInstanceState);

        onViewCreated();
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    protected  abstract void doCreate(Bundle savedInstanceState);

    protected abstract View doCreateView();

    protected void onViewCreated() {

    }

    protected void setHomeAsUpEnabled() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mHomeAsUpEnabled = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home && mHomeAsUpEnabled) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
