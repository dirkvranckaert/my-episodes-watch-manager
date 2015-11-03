package eu.vranckaert.episodeWatcher.twopointo.context;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import eu.vranckaert.episodeWatcher.twopointo.util.KeyboardHelper;

/**
 * Date: 15/06/15
 * Time: 07:52
 *
 * @author Dirk Vranckaert
 */
public abstract class BaseFragment extends Fragment {
    private static final String LOG_TAG = BaseFragment.class.getSimpleName();
    
    public static final int RESULT_CANCELLED = Activity.RESULT_CANCELED;
    public static final int RESULT_OK = Activity.RESULT_OK;
    public static final int RESULT_DELETED = -2;

    private boolean mIsTopLevelFragment = true;
    private CharSequence mTitle = "";
    private CharSequence mSubtitle = "";
    private boolean mStartedFromOutside = false;

    private BaseMenuActivity mActivity;
    private View mView;
    private Bundle mExtras;
    private Bundle mData;
    private int mRequestCode = -1;
    private int mResultCode = RESULT_CANCELLED;

    public final String getName() {
        return getClass().getSimpleName();
    }

    public final void setTitle(CharSequence title) {
        getActivity().setTitle(title);
        setSubtitle(null);
        mTitle = title;
    }

    public final void setTitle(@StringRes int titleResId) {
        getActivity().setTitle(titleResId);
        setSubtitle(null);
        mTitle = getString(titleResId);
    }

    protected final CharSequence getTitle() {
        return mTitle;
    }

    public final ActionBar getActionBar() {
        if (getActivity() != null && getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                return actionBar;
            }
        }
        return null;
    }

    public final void setSubtitle(@StringRes int subTitleResId) {
        setSubtitle(getString(subTitleResId));
    }

    public final void setSubtitle(String subTitle) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(subTitle);
            mSubtitle = subTitle;
        }
    }

    protected final CharSequence getSubtitle() {
        return mSubtitle;
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (!mIsTopLevelFragment) {
            ((BaseMenuActivity) getActivity()).setBackNavigation();
        } else {
            ((BaseMenuActivity) getActivity()).resetNavigation();
        }

        doCreate(savedInstanceState);
    }

    protected void doCreate(Bundle savedInstanceState) {
        setTitle("");
    }

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");
        if (mView == null) {
            mView = doCreateView(inflater, container, savedInstanceState);
        }
        detachViewFromParent(mView);
        return mView;
    }

    protected View doCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    private void detachViewFromParent(View view) {
        if (view != null) {
            ViewParent viewParent = view.getParent();
            if (viewParent != null && viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(view);
            }
        }
    }

    public final void setTopLevelFragment(boolean isTopLevelFragment) {
        mIsTopLevelFragment = isTopLevelFragment;
    }

    public final boolean isTopLevelFragment() {
        return mIsTopLevelFragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!mIsTopLevelFragment && item.getItemId() == android.R.id.home) {
            if (mStartedFromOutside) {
                getActivity().finish();
            } else {
                getActivity().onBackPressed();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void setRequestCode(int requestCode) {
        mRequestCode = requestCode;
    }

    protected int getRequestCode() {
        return mRequestCode;
    }

    protected final void setResult(int resultCode) {
        mResultCode = resultCode;
    }

    protected final void setResult(int resultCode, Bundle data) {
        mResultCode = resultCode;
        mData = data;
    }

    protected final int getResultCode() {
        return mResultCode;
    }

    protected final Bundle getData() {
        return mData;
    }

    public final void setExtras(Bundle extras) {
        mExtras = extras;
    }

    protected final Bundle getExtras() {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        return mExtras;
    }

    public void startFragmentForResult(BaseFragment baseFragment, int requestCode, Bundle data) {
        baseFragment.setExtras(data);
        startFragmentForResult(baseFragment, requestCode);
    }

    public void startFragmentForResult(BaseFragment baseFragment, int requestCode) {
        if (mView != null) {
            KeyboardHelper.hideKeyboard(mView, getActivity());
        }
        baseFragment.setRequestCode(requestCode);
        this.startFragment(baseFragment, true);
    }

    public void startFragment(BaseFragment baseFragment) {
        startFragment(baseFragment, true);
    }

    public void startFragment(BaseFragment baseFragment, Bundle data) {
        baseFragment.setExtras(data);
        startFragment(baseFragment);
    }

    public void startFragment(BaseFragment baseFragment, boolean addToBackstack) {
        Log.d(LOG_TAG, "startFragment");
        if (mView != null) {
            KeyboardHelper.hideKeyboard(mView, getActivity());
        }
        Log.d(LOG_TAG,
                "Navigating to fragment (addToBackstack=" + addToBackstack + ") " + baseFragment.getName());

        getBaseActivity().putFragment(baseFragment, addToBackstack);
    }

    public final BaseMenuActivity getBaseActivity() {
        if (mActivity == null) {
            mActivity = ((BaseMenuActivity) getActivity());
        }
        return mActivity;
    }

    public final void setBaseActivity(BaseMenuActivity activity) {
        mActivity = activity;
    }

    public void startFragment(BaseFragment baseFragment, boolean addToBackstack, Bundle data) {
        baseFragment.setExtras(data);
        startFragment(baseFragment, addToBackstack);
    }

    protected void onFragmentResult(int requestCode, int resultCode, Bundle data) {

    }

    public final void setStartedFromOutside() {
        mStartedFromOutside = true;
    }

    public final boolean isStartedFromOutside() {
        return mStartedFromOutside;
    }

    public void finish() {
        Log.d(LOG_TAG, "finish");
        if (mView != null) {
            KeyboardHelper.hideKeyboard(mView, getActivity());
        }
        if (mStartedFromOutside) {
            getActivity().finish();
        } else {
            getActivity().onBackPressed();
        }
    }

    public final void invalidateOptionsMenu() {
        getActivity().invalidateOptionsMenu();
    }

    public final void onFinish() {
        Log.d(LOG_TAG, "onFinish");
        if (mView != null && mView instanceof ViewGroup) {
            try {
                ((ViewGroup) mView).removeAllViews();
            } catch (RuntimeException e) {
                Log.e(LOG_TAG, "Could not remove all views", e);
            }
        }
    }

    public void navigateToMenuItem(int menuItemId) {
        ((BaseMenuActivity) getActivity()).navigateToMenuItem(menuItemId);
    }

    protected abstract String getAnalyticsScreenName();

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onDetach() {
        Log.d(LOG_TAG, "onDetach");
        super.onDetach();
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(LOG_TAG, "onAttach");
        super.onAttach(activity);
    }

    @Override
    public void onDestroyView() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroyView();
    }
}
