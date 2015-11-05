package eu.vranckaert.android.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Date: 08/01/15
 * Time: 16:46
 *
 * @author Dirk Vranckaert
 */
public class ListItemDividerDecoration extends ItemDecoration {
    private final Context mContext;
    private final int mDividerHeight;
    private final Paint mPaint;

    public ListItemDividerDecoration(Context context, @ColorRes int color, int dp) {
        mContext = context;

        mDividerHeight = dpToPx(dp);

        mPaint = new Paint();
        mPaint.setColor(context.getResources().getColor(color));
        mPaint.setStrokeWidth(mDividerHeight);

    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, State state) {
        final int left = parent.getLeft();
        final int right = parent.getRight();

        int childCount = parent.getChildCount();
        for (int i=0; i<childCount; i++) {
            View item = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) item.getLayoutParams();

            final int bottom = item.getBottom() + params.bottomMargin;
            final int y = bottom + mDividerHeight/2;

            canvas.drawLine(left, y, right, y, mPaint);
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        outRect.bottom = mDividerHeight;
    }
}
