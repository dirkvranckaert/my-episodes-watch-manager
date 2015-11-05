package eu.vranckaert.android.recyclerview;

import android.support.annotation.ColorRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
/**
 * Date: 14/10/15
 * Time: 23:01
 *
 * @author Dirk Vranckaert
 */
public class RecyclerViewUtil {
    public static void init(RecyclerView recyclerView, @ColorRes int dividerColor) {
        LayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new ListItemDividerDecoration(recyclerView.getContext(), dividerColor, 1));
    }
}
