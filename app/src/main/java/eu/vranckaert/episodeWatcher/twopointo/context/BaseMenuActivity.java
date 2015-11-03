package eu.vranckaert.episodeWatcher.twopointo.context;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
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
import eu.vranckaert.framework.util.KeyboardHelper;
import eu.vranckaert.trains.be.R;
import eu.vranckaert.trains.be.util.MenuHandler;

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

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;
    private BaseFragment mContentFragment;
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
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Setup the ActionBarDrawerToggle
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.general_drawer_open,
                R.string.general_drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        mDrawerToggle.setToolbarNavigationClickListener(this);

        // Setup the NavigationView
        mNavigationView = (NavigationView) view.findViewById(R.id.navigation_view);
        mNavigationView.getMenu().findItem(getDefaultMenuItem()).setChecked(true);
        mNavigationView.setNavigationItemSelectedListener(this);

        if (mSelectedMenuItemId == -1) {
            BaseFragment fragment = MenuHandler.navigate(getDefaultMenuItem());
            if (fragment != null) {
                putFragment(fragment, false);
            }
        } else {
            // Let's what happens now :-)
        }

        return view;
    }

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
        if (item.getItemId() == android.support.v7.appcompat.R.id.home) {
            return mDrawerToggle.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        Log.d("dirk", "onNavigationItemSelected");

        // update highlighted item in the navigation menu
        menuItem.setChecked(true);

        // allow some time after closing the drawer before performing real navigation
        // so the user can see what is happening
        mDrawerLayout.closeDrawer(GravityCompat.START);
        handleNavigationItemSelected(menuItem);

        return true;
    }

    private void handleNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == mSelectedMenuItemId) {
            return;
        }
        mSelectedMenuItemId = menuItem.getItemId();
        handleNavigationItemSelected(mSelectedMenuItemId);
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

    private void handleNavigationItemSelected(int menuId) {
        BaseFragment fragment = MenuHandler.navigate(menuId);
        if (fragment != null) {
            putFragment(fragment, false);
        }
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
            Fragment previousFragment = mContentFragment;
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

    public final void putFragment(BaseFragment fragment, boolean addToBackstack) {
        Log.d("dirk", "putFragment");

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction ft = fragmentManager.beginTransaction();

        if (fragment == null && mContentFragment != null) {
            ft.remove(mContentFragment).commitAllowingStateLoss();
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
            ft.replace(R.id.content, fragment).commitAllowingStateLoss();
        }

        boolean isFirstContentFragment = mContentFragment == null;
        mContentFragment = fragment;
        if (isFirstContentFragment) {
            onContentSet(fragment);
        }
    }

    protected void onContentSet(BaseFragment fragment) {

    }

    protected BaseFragment getContentFragment() {
        return mContentFragment;
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
        handleNavigationItemSelected(menuItem);
        mNavigationView.getMenu().findItem(menuItemId).setChecked(true);
    }
}
