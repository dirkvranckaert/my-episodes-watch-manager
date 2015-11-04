package eu.vranckaert.episodeWatcher.twopointo.widget;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.View;
import eu.vranckaert.episodeWatcher.twopointo.util.KeyboardHelper;

/**
 * Date: 23/06/15
 * Time: 16:49
 *
 * @author Dirk Vranckaert
 */
public class MyDrawerLayout extends DrawerLayout {
    public MyDrawerLayout(Context context) {
        super(context);
    }

    public MyDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void openDrawer(View drawerView) {
        KeyboardHelper.hideKeyboard(this, getContext());
        super.openDrawer(drawerView);
    }

    @Override
    public void openDrawer(int gravity) {
        KeyboardHelper.hideKeyboard(this, getContext());
        super.openDrawer(gravity);
    }
}
