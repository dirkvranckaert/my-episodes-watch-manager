package eu.vranckaert.android.recyclerview;

import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;

/**
 * Date: 06/11/15
 * Time: 16:10
 *
 * @author Dirk Vranckaert
 */
public abstract class SectionedListAdapter extends Adapter<ViewHolder> {
    private static final int VIEW_TYPE_HEADER_ELEMENT = 0x01;
    private static final int VIEW_TYPE_LIST_ELEMENT = 0x02;

    @Override
    public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public final void onBindViewHolder(ViewHolder holder, int position) {

    }
}
