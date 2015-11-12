package eu.vranckaert.android.context;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import eu.vranckaert.android.general.KeyboardHelper;

import java.util.Locale;

/**
 * Date: 12/11/15
 * Time: 11:41
 *
 * @author Dirk Vranckaert
 */
public final class BaseFragmentDelegate {
    private static String LOG_TAG = BaseFragmentDelegate.class.getSimpleName();
    
    protected static int RESULT_CANCELLED = Activity.RESULT_CANCELED;
    protected static int RESULT_OK = Activity.RESULT_OK;
    protected static int RESULT_DELETED = -2;
    
    private Fragment mFragment;

    private boolean mIsTopLevelFragment = true;
    private CharSequence mTitle = "";
    private CharSequence mSubtitle = "";
    private boolean mStartedFromOutside = false;

    private BaseMenuActivity mActivity;
    protected View mView;
    private Bundle mExtras;
    private Bundle mData;
    private int mRequestCode = -1;
    private int mResultCode = RESULT_CANCELLED;

    protected static BaseFragmentDelegate getDelegate(Fragment fragment) {
        if (fragment == null) {
            throw new IllegalArgumentException("Fragment for the BaseFragmentDelegate cannot be null!");
        }
        
        BaseFragmentDelegate delegate = new BaseFragmentDelegate();
        delegate.mFragment = fragment;
        return delegate;
    }

    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        mFragment.setHasOptionsMenu(true);

        if (!isTopLevelFragment()) {
            ((BaseMenuActivity) mFragment.getActivity()).setBackNavigation();
        } else {
            ((BaseMenuActivity) mFragment.getActivity()).resetNavigation();
        }
    }

    protected String getName() {
        return getClass().getSimpleName();
    }

    protected void setTitle(CharSequence title) {
        mFragment.getActivity().setTitle(title);
        setSubtitle(null);
        mTitle = title;
    }

    protected void setTitle(@StringRes int titleResId) {
        mFragment.getActivity().setTitle(titleResId);
        setSubtitle(null);
        mTitle = mFragment.getString(titleResId);
    }

    protected CharSequence getTitle() {
        return mTitle;
    }

    protected ActionBar getActionBar() {
        if (mFragment.getActivity() != null && mFragment.getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) mFragment.getActivity()).getSupportActionBar();
            if (actionBar != null) {
                return actionBar;
            }
        }
        return null;
    }

    protected void setSubtitle(@StringRes int subTitleResId) {
        setSubtitle(mFragment.getString(subTitleResId));
    }

    protected void setSubtitle(String subTitle) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(subTitle);
            mSubtitle = subTitle;
        }
    }

    protected CharSequence getSubtitle() {
        return mSubtitle;
    }

    protected void detachViewFromParent(View view) {
        if (view != null) {
            ViewParent viewParent = view.getParent();
            if (viewParent != null && viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(view);
            }
        }
    }

    protected void setTopLevelFragment(boolean isTopLevelFragment) {
        mIsTopLevelFragment = isTopLevelFragment;
    }

    protected boolean isTopLevelFragment() {
        return mIsTopLevelFragment;
    }

    protected boolean onOptionsItemSelected(MenuItem item) {
        if (!mIsTopLevelFragment && item.getItemId() == android.R.id.home) {
            if (mStartedFromOutside) {
                mFragment.getActivity().finish();
            } else {
                mFragment.getActivity().onBackPressed();
            }
            return true;
        }

        return mFragment.onOptionsItemSelected(item);
    }

    protected void setRequestCode(int requestCode) {
        mRequestCode = requestCode;
    }

    protected int getRequestCode() {
        return mRequestCode;
    }

    protected void setResult(int resultCode) {
        mResultCode = resultCode;
    }

    protected void setResult(int resultCode, Bundle data) {
        mResultCode = resultCode;
        mData = data;
    }

    protected int getResultCode() {
        return mResultCode;
    }

    protected Bundle getData() {
        return mData;
    }

    protected void setExtras(Bundle extras) {
        mExtras = extras;
    }

    protected Bundle getExtras() {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        return mExtras;
    }

    protected void startFragmentForResult(BaseFragment baseFragment, int requestCode, Bundle data) {
        baseFragment.setExtras(data);
        startFragmentForResult(baseFragment, requestCode);
    }

    protected void startFragmentForResult(BaseFragment baseFragment, int requestCode) {
        if (mView != null) {
            KeyboardHelper.hideKeyboard(mView, mFragment.getActivity());
        }
        baseFragment.setRequestCode(requestCode);
        this.startFragment(baseFragment, true);
    }

    protected void startFragment(BaseFragment baseFragment) {
        startFragment(baseFragment, true);
    }

    protected void startFragment(BaseFragment baseFragment, Bundle data) {
        baseFragment.setExtras(data);
        startFragment(baseFragment);
    }

    protected void startFragment(BaseFragment baseFragment, boolean addToBackstack) {
        Log.d(LOG_TAG, "startFragment");
        if (mView != null) {
            KeyboardHelper.hideKeyboard(mView, mFragment.getActivity());
        }
        Log.d(LOG_TAG,
                "Navigating to fragment (addToBackstack=" + addToBackstack + ") " + baseFragment.getName());

        getBaseActivity().putFragment(baseFragment, addToBackstack);
    }

    protected BaseMenuActivity getBaseActivity() {
        if (mActivity == null) {
            mActivity = ((BaseMenuActivity) mFragment.getActivity());
        }
        return mActivity;
    }

    protected void setBaseActivity(BaseMenuActivity activity) {
        mActivity = activity;
    }

    protected void startFragment(BaseFragment baseFragment, boolean addToBackstack, Bundle data) {
        baseFragment.setExtras(data);
        startFragment(baseFragment, addToBackstack);
    }

    protected void onFragmentResult(int requestCode, int resultCode, Bundle data) {

    }

    protected void setStartedFromOutside() {
        mStartedFromOutside = true;
    }

    protected boolean isStartedFromOutside() {
        return mStartedFromOutside;
    }

    protected void finish() {
        Log.d(LOG_TAG, "finish");
        if (mView != null) {
            KeyboardHelper.hideKeyboard(mView, mFragment.getActivity());
        }
        if (mStartedFromOutside) {
            mFragment.getActivity().finish();
        } else {
            mFragment.getActivity().onBackPressed();
        }
    }

    protected void invalidateOptionsMenu() {
        mFragment.getActivity().supportInvalidateOptionsMenu();
    }

    protected void onFinish() {
        Log.d(LOG_TAG, "onFinish");
        if (mView != null && mView instanceof ViewGroup) {
            try {
                ((ViewGroup) mView).removeAllViews();
            } catch (RuntimeException e) {
                Log.e(LOG_TAG, "Could not remove all views", e);
            }
        }
    }

    protected void navigateToMenuItem(int menuItemId) {
        ((BaseMenuActivity) mFragment.getActivity()).navigateToMenuItem(menuItemId);
    }

    protected void applyLanguage(String language) {
        Activity activity = mFragment.getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).applyLanguage(language);
        }
    }
}
