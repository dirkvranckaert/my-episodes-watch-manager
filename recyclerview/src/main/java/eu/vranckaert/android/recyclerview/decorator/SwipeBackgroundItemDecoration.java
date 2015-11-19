package eu.vranckaert.android.recyclerview.decorator;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;
import eu.vranckaert.android.general.MetricHelper;

/**
 * Date: 18/11/15
 * Time: 13:29
 *
 * @author Dirk Vranckaert
 */
public class SwipeBackgroundItemDecoration extends ItemDecoration {
    private final Context mContext;

    private static final int POSITION_LEFT = 0;
    private static final int POSITION_RIGHT = 1;

    private final Paint mPaint;
    private final Rect mActionDrawableSourceRect;
    private final Rect mActionDrawableDestinationRect;
    private final Resources res;

    private int mBackgroundColor;
    private Bitmap mActionDrawable;
    private int mDrawableColor;
    private int mPosition;
    private int mLeftMargin;
    private int mRightMargin;
    private int mTopMargin;
    private int mBottomMargin;

    public SwipeBackgroundItemDecoration(Context context, @ColorRes int backgroundColor,
                                         @DrawableRes int actionDrawable) {
        mContext = context;

        mPaint = new Paint();
        mActionDrawableSourceRect = new Rect();
        mActionDrawableDestinationRect = new Rect();
        res = context.getResources();

        setBackgroundColor(backgroundColor);
        setActionDrawable(actionDrawable);

        // Defaults
        setDrawableColor(android.R.color.white);
        setLeftMargin(MetricHelper.dpToPixel(mContext, 10));
        setRightMaring(MetricHelper.dpToPixel(mContext, 10));
        setTopMargin(MetricHelper.dpToPixel(mContext, 6));
        setBottomMargin(MetricHelper.dpToPixel(mContext, 6));
        setPosition(POSITION_LEFT);
    }

    public void setActionDrawable(@DrawableRes int actionDrawable) {
        Bitmap bitmap = BitmapFactory.decodeResource(res, actionDrawable);
        mActionDrawable = bitmap;

        mActionDrawableSourceRect.left = 0;
        mActionDrawableSourceRect.top = 0;
        mActionDrawableSourceRect.right = mActionDrawable.getWidth();
        mActionDrawableSourceRect.bottom = mActionDrawable.getHeight();
    }

    public void setBackgroundColor(@ColorRes int backgroundColor) {
        mBackgroundColor = mContext.getResources().getColor(backgroundColor);
    }

    public void setDrawableColor(@ColorRes int drawableColor) {
        mDrawableColor = mContext.getResources().getColor(drawableColor);
    }

    public void setPosition(@ActionPosition int position) {
        mPosition = position;
    }

    public void setLeftMargin(int px) {
        mLeftMargin = px;
    }

    public void setRightMaring(int px) {
        mRightMargin = px;
    }

    public void setTopMargin(int px) {
        mTopMargin = px;
    }

    public void setBottomMargin(int px) {
        mBottomMargin = px;
    }

    private Paint getPaintForBackground() {
        mPaint.setColor(mBackgroundColor);
        return mPaint;
    }

    private Paint getPaintForImage() {
        mPaint.setColor(mDrawableColor);
        return mPaint;
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, State state) {
        if (parent.getItemAnimator() != null && parent.getItemAnimator().isRunning()) {
            // We do not want our dismiss background to be drawn during animation
            return;
        }

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View item = parent.getChildAt(i);
            // Draw the background
            mActionDrawableDestinationRect.top = item.getTop();
            mActionDrawableDestinationRect.bottom = item.getBottom();
            mActionDrawableDestinationRect.left = item.getLeft();
            mActionDrawableDestinationRect.right = item.getRight();
            canvas.drawRect(mActionDrawableDestinationRect, getPaintForBackground());

            mActionDrawableDestinationRect.top = mActionDrawableDestinationRect.top + mTopMargin;
            mActionDrawableDestinationRect.bottom = mActionDrawableDestinationRect.bottom - mBottomMargin;
            if (mPosition == POSITION_RIGHT) {
                mActionDrawableDestinationRect.right = item.getRight() - mRightMargin;
                mActionDrawableDestinationRect.left =
                        mActionDrawableDestinationRect.right - mActionDrawableSourceRect.width();
            } else {
                mActionDrawableDestinationRect.left = item.getLeft() + mLeftMargin;
                mActionDrawableDestinationRect.right =
                        mActionDrawableSourceRect.width() + mActionDrawableDestinationRect.left;
            }
            canvas.drawBitmap(mActionDrawable, mActionDrawableSourceRect, mActionDrawableDestinationRect,
                    getPaintForImage());
        }
    }

    @IntDef({POSITION_LEFT, POSITION_RIGHT})
    public @interface ActionPosition {
    }
}
