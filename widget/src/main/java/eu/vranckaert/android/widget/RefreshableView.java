package eu.vranckaert.android.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

/**
 * Date: 19/05/15
 * Time: 09:25
 *
 * @author Dirk Vranckaert
 */
public class RefreshableView extends SwipeRefreshLayout {
    private boolean mMeasured;
    private boolean mRefreshing;

    public RefreshableView(Context context) {
        this(context, null);
    }

    public RefreshableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (!mMeasured) {
            mMeasured = true;
            if (mRefreshing != isRefreshing()) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        setRefreshing(mRefreshing);
                    }
                });
            }
        }
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        if (mMeasured) {
            if (isRefreshing() != refreshing) {
                super.setRefreshing(refreshing);
            }
        }
        mRefreshing = refreshing;
    }
}
