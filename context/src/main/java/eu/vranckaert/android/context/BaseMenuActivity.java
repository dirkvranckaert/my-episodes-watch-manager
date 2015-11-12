package eu.vranckaert.android.context;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.MenuRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.internal.view.menu.ActionMenuItem;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import eu.vranckaert.android.general.KeyboardHelper;

import java.util.List;

/**
 * Date: 11/06/15
 * Time: 23:56
 *
 * @author Dirk Vranckaert
 */
public abstract class BaseMenuActivity extends BaseActivity implements OnNavigationItemSelectedListener,
        OnClickListener {
    private static final String SELECTED_MENU_ITEM_ID = "selectedMenuItemId";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;
    private BaseAppcompatFragment mContentFragment;
    private int mSelectedMenuItemId = -1;

    private final Handler mDrawerActionHandler = new Handler();

    @Override
    protected void doCreate(Bundle savedInstanceState) {
        Log.d("dirk", "doCreate");

        if (savedInstanceState != null) {
            mSelectedMenuItemId = savedInstanceState.getInt(SELECTED_MENU_ITEM_ID);
        }
    }

    @Override
    protected final View doCreateView() {
        Log.d("dirk", "doCreateView");
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.activity_menu, null);

        // Setup the ToolBar
        // This needs to be done here, if we wait for the onCreate of the BaseMenuActivity to finish, and then
        // do the toolbar setup in the onCreate of the BaseActivity we might be to late and no titles for example can be
        // set and the drawer layout as well will be overwritten (as this is configured on the toolbar we are
        // initializing here).
        initToolbar(view);

        // Setup the ActionBarDrawerToggle
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, getToolbar(), R.string.general_drawer_open,
                R.string.general_drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        mDrawerToggle.setToolbarNavigationClickListener(this);

        // Setup the NavigationView
        mNavigationView = (NavigationView) view.findViewById(R.id.navigation_view);
        initNavigationView();
        MenuItem defaultMenuItem = mNavigationView.getMenu().findItem(getDefaultMenuItem());
        if (defaultMenuItem != null) {
            defaultMenuItem.setChecked(true);
        }
        mNavigationView.setNavigationItemSelectedListener(this);

        if (mSelectedMenuItemId == -1) {
            BaseAppcompatFragment fragment = getMenuHandler().navigate(this, getDefaultMenuItem());
            if (fragment != null) {
                putFragment(fragment, false);
            }
        } else {
            // Let's see what happens now :-)
        }

        return view;
    }

    private void initNavigationView() {
        int menuResId = getMenuResId();
        mNavigationView.inflateMenu(menuResId);
        int headerResId = getHeaderResId();
        if (headerResId != -1) {
            mNavigationView.inflateHeaderView(headerResId);
        }
    }

    public int getHeaderResId() {
        return -1;
    }

    @MenuRes
    protected abstract int getMenuResId();

    public abstract AbstractMenuHandler getMenuHandler();

    @Override
    protected void onViewCreated() {
        Log.d("dirk", "onViewCreated");
        if (mBackNavigation) {
            setBackNavigation();
        } else {
            resetNavigation();
        }
    }

    @IdRes
    protected abstract int getDefaultMenuItem();

    protected void setHomeAsUpEnabled() {
        throw new RuntimeException("Should not set the home button behaviour from a BaseMenuActivity");
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Log.d("dirk", "onOptionsItemSelected");
        if (item.getItemId() == android.R.id.home) {
            return mDrawerToggle.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        Log.d("dirk", "onNavigationItemSelected");

        // allow some time after closing the drawer before performing real navigation
        // so the user can see what is happening
        mDrawerLayout.closeDrawer(GravityCompat.START);
        boolean startedNewFragment = handleNavigationItemSelected(menuItem);

        // update highlighted item in the navigation menu
        menuItem.setChecked(startedNewFragment);

        return true;
    }

    private boolean handleNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == mSelectedMenuItemId) {
            return true;
        }
        int selectedMenuItemId = menuItem.getItemId();
        boolean fragmentStarted = handleNavigationItemSelected(selectedMenuItemId);
        if (fragmentStarted) {
            mSelectedMenuItemId = selectedMenuItemId;
        }
        return fragmentStarted;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("dirk", "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_MENU_ITEM_ID, mSelectedMenuItemId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("dirk", "onConfigurationChanged");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d("dirk", "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            mContentFragment = (BaseFragment) fragments.get(fragments.size() - 1);
        }
    }

    private boolean handleNavigationItemSelected(int menuId) {
        BaseAppcompatFragment fragment = getMenuHandler().navigate(this, menuId);
        if (fragment != null) {
            putFragment(fragment, false);
            return true;
        }
        return false;
    }

    private int getFragmentCount() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        int count = 0;
        for (Fragment fragment : fragments) {
            if (fragment != null) {
                count++;
            }
        }
        return count;
    }

    private BaseFragment getFragment() {
        Fragment fragment = null;

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (int i = 0; i < fragments.size(); i++) {
            fragment = fragments.get(fragments.size() - (i + 1));
            if (fragment != null && fragment.isAdded()) {
                break;
            }
        }

        return (BaseFragment) fragment;
    }

    @Override
    public void onBackPressed() {
        Log.d("dirk", "onBackPressed");

        if (mContentFragment.isStartedFromOutside()) {
            finish();
            return;
        }

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Fragment previousFragment = mContentFragment.get();
            KeyboardHelper.hideKeyboard(getWindow().getDecorView(), this);

            int numberOfFragments = getFragmentCount();
            if (numberOfFragments > 2) {
                setBackNavigation();
            } else {
                resetNavigation();
            }
            boolean popFromStack = getFragmentCount() > 1;
            if (popFromStack) {
                if (previousFragment instanceof BaseFragment) {
                    BaseFragment baseFragment = (BaseFragment) previousFragment;
                    baseFragment.onFinish();
                }
            }

            super.onBackPressed();

            if (popFromStack) {
                mContentFragment = getFragment();
                if (previousFragment instanceof BaseFragment && mContentFragment instanceof BaseFragment) {
                    BaseFragment baseFragment = (BaseFragment) previousFragment;
                    baseFragment.onFinish();
                    int requestCode = baseFragment.getRequestCode();
                    if (requestCode != -1) {
                        Bundle data = baseFragment.getData();
                        int resultCode = baseFragment.getResultCode();
                        ((BaseFragment) mContentFragment).onFragmentResult(requestCode, resultCode, data);
                    }

                    setTitle(((BaseFragment)mContentFragment).getTitle());
                    getSupportActionBar().setSubtitle(((BaseFragment)mContentFragment).getSubtitle());
                }
            }
        }
    }

    public void onClick(View v) {
        if (v.getId() == -1 && mContentFragment != null && mContentFragment instanceof BaseFragment && !((BaseFragment)mContentFragment).isTopLevelFragment()) {
            Log.d("dirk", "Back Button Clicked");
            onBackPressed();
        }
    }

    public final void putFragment(BaseAppcompatFragment fragment, boolean addToBackstack) {
        Log.d("dirk", "putFragment");

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction ft = fragmentManager.beginTransaction();

        if (fragment == null && mContentFragment != null) {
            ft.remove(mContentFragment.get()).commitAllowingStateLoss();
        } else if (fragment != null) {
            if (addToBackstack) {
                ft.addToBackStack(fragment.getName());
                fragment.setTopLevelFragment(false);
            } else {
                int numberOfFragments = fragmentManager.getFragments() == null ? 0 : fragmentManager.getFragments().size();
                Log.d("dirk", "Putting fragment without backstack, number of fragments found = " + numberOfFragments);
                for (int i = 0; i < numberOfFragments - 2; i++) {
                    Log.d("dirk", "Removing fragment from backstack (N°" + i + ")");
                    fragmentManager.popBackStackImmediate();
                }
                fragment.setTopLevelFragment(true);
            }
            ft.replace(R.id.content, fragment.get()).commitAllowingStateLoss();
        }

        boolean isFirstContentFragment = mContentFragment == null;
        mContentFragment = fragment;
        if (isFirstContentFragment) {
            onContentSet(fragment.get());
        }
    }

    protected void onContentSet(Fragment fragment) {

    }

    protected Fragment getContentFragment() {
        return mContentFragment.get();
    }

    boolean mBackNavigation;
    public void setBackNavigation() {
        mBackNavigation = true;
        if (mDrawerToggle != null) {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerToggle.setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    public void resetNavigation() {
        mBackNavigation = false;
        if (mDrawerToggle != null) {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    public NavigationView getNavigationView() {
        return mNavigationView;
    }

    public void navigateToMenuItem(int menuItemId) {
        ActionMenuItem menuItem = new ActionMenuItem(null, -1, menuItemId, -1, -1, "");
        boolean startedNewFragment = handleNavigationItemSelected(menuItem);
        mNavigationView.getMenu().findItem(menuItemId).setChecked(startedNewFragment);
    }
}
