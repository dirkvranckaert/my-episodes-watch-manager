package eu.vranckaert.android.general;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Date: 31/03/14
 * Time: 15:51
 *
 * @author Dirk Vranckaert
 */
public class KeyboardHelper {
    public static void hideKeyboard(final MenuItem menuItem, final Context context) {
        if (menuItem == null || context == null) {
            return;
        }

        View view = menuItem.getActionView();
        if (view != null) {
            hideKeyboard(view, context);
        }
    }

    public static void hideKeyboard(final View view, final Context context) {
        if (view == null || context == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(final EditText view, final Context context) {
        if (view == null || context == null) {
            return;
        }

        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
