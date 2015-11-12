package eu.vranckaert.android.context;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;
import android.view.View;

/**
 * Date: 12/11/15
 * Time: 11:40
 *
 * @author Dirk Vranckaert
 */
public abstract class BasePreferenceFragment extends PreferenceFragmentCompat implements BaseAppcompatFragment {
    private static final String LOG_TAG = BaseFragment.class.getSimpleName();

    private BaseFragmentDelegate mDelegate;

    @Override
    public Fragment get() {
        return this;
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDelegate().onCreate(savedInstanceState);
        doCreate(savedInstanceState);
    }

    protected void doCreate(Bundle savedInstanceState) {
        setTitle("");
    }

    @Override
    public final void onCreatePreferences(Bundle savedInstanceState, String rootkey) {
        doCreatePreferences(savedInstanceState, rootkey);
    }

    public abstract void doCreatePreferences(Bundle savedInstanceState, String rootkey);

    @Override
    public final String getName() {
        return getDelegate().getName();
    }

    public final void setTitle(CharSequence title) {
        getDelegate().setTitle(title);
    }

    public final void setTitle(@StringRes int titleResId) {
        getDelegate().setTitle(titleResId);
    }

    protected final CharSequence getTitle() {
        return getDelegate().getTitle();
    }

    public final ActionBar getActionBar() {
        return getDelegate().getActionBar();
    }

    public final void setSubtitle(@StringRes int subTitleResId) {
        getDelegate().setSubtitle(subTitleResId);
    }

    public final void setSubtitle(String subTitle) {
        getDelegate().setSubtitle(subTitle);
    }

    protected final CharSequence getSubtitle() {
        return getDelegate().getSubtitle();
    }

    private void detachViewFromParent(View view) {
        getDelegate().detachViewFromParent(view);
    }

    @Override
    public final void setTopLevelFragment(boolean isTopLevelFragment) {
        getDelegate().setTopLevelFragment(isTopLevelFragment);
    }

    public final boolean isTopLevelFragment() {
        return getDelegate().isTopLevelFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return getDelegate().onOptionsItemSelected(item);
    }

    protected void setRequestCode(int requestCode) {
        getDelegate().setRequestCode(requestCode);
    }

    protected int getRequestCode() {
        return getDelegate().getRequestCode();
    }

    protected final void setResult(int resultCode) {
        getDelegate().setResult(resultCode);
    }

    protected final void setResult(int resultCode, Bundle data) {
        getDelegate().setResult(resultCode, data);
    }

    protected final int getResultCode() {
        return getDelegate().getResultCode();
    }

    protected final Bundle getData() {
        return getDelegate().getData();
    }

    public final void setExtras(Bundle extras) {
        getDelegate().setExtras(extras);
    }

    protected final Bundle getExtras() {
        return getDelegate().getExtras();
    }

    public void startFragmentForResult(BaseFragment baseFragment, int requestCode, Bundle data) {
        getDelegate().startFragmentForResult(baseFragment, requestCode, data);
    }

    public void startFragmentForResult(BaseFragment baseFragment, int requestCode) {
        getDelegate().startFragmentForResult(baseFragment, requestCode);
    }

    public void startFragment(BaseFragment baseFragment) {
        getDelegate().startFragment(baseFragment, true);
    }

    public void startFragment(BaseFragment baseFragment, Bundle data) {
        getDelegate().startFragment(baseFragment, data);
    }

    public void startFragment(BaseFragment baseFragment, boolean addToBackstack) {
        getDelegate().startFragment(baseFragment, addToBackstack);
    }

    public final BaseMenuActivity getBaseActivity() {
        return getDelegate().getBaseActivity();
    }

    public final void setBaseActivity(BaseMenuActivity activity) {
        getDelegate().setBaseActivity(activity);
    }

    public void startFragment(BaseFragment baseFragment, boolean addToBackstack, Bundle data) {
        getDelegate().startFragment(baseFragment, addToBackstack, data);
    }

    protected void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        getDelegate().onFragmentResult(requestCode, resultCode, data);
    }

    public final void setStartedFromOutside() {
        getDelegate().setStartedFromOutside();
    }

    @Override
    public final boolean isStartedFromOutside() {
        return getDelegate().isStartedFromOutside();
    }

    public void finish() {
        getDelegate().finish();
    }

    public final void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    public final void onFinish() {
        getDelegate().onFinish();
    }

    public void navigateToMenuItem(int menuItemId) {
        getDelegate().navigateToMenuItem(menuItemId);
    }

    public void applyLanguage(String language) {
        getDelegate().applyLanguage(language);
    }

    private BaseFragmentDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = BaseFragmentDelegate.getDelegate(this);
        }

        return mDelegate;
    }
}
