package eu.vranckaert.android.recyclerview;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import eu.vranckaert.android.recyclerview.decorator.DividerItemDecoration;

/**
 * Date: 14/10/15
 * Time: 23:01
 *
 * @author Dirk Vranckaert
 */
public class RecyclerViewUtil {
    public static void init(RecyclerView recyclerView) {
        LayoutManager layoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL_LIST));
    }

    public static void initSuperSlim(RecyclerView recyclerView) {
        com.tonicartos.superslim.LayoutManager layoutManager = new com.tonicartos.superslim.LayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL_LIST));
    }
}
