package eu.vranckaert.android.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;
import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LinearSLM;

/**
 * Uses the SuperSlim RecyclerView framework to create sectioned adapters. Make sure to use the
 * {@link RecyclerViewUtil#initSuperSlim(RecyclerView, int)} method to setup your recyclerview to be able to handle this
 * adapter.<br/>
 * <br/>
 * Don't forget to edit your header's layout file to indicate that it's a header using:
 * <b>app:slm_isHeader="true"</b>
 * In order to enable sticky headers/sections you should adapt the header layout file with:<br/>
 * <br/>
 * app:slm_headerDisplay="inline|<b>sticky</b>"
 *
 * @author Dirk Vranckaert
 */
public abstract class SectionedAdapter<VH extends ViewHolder> extends Adapter<VH> {
    private static final int VIEW_TYPE_HEADER_ELEMENT = 0x01;
    private static final int VIEW_TYPE_LIST_ELEMENT = 0x02;

    @Override
    public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER_ELEMENT) {
            return onCreateHeaderViewHolder(parent);
        } else {
            return onCreateElementViewHolder(parent);
        }
    }

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER_ELEMENT) {
            onBindHeaderViewHolder(holder, position);
        } else {
            onBindElementViewHolder(holder, position);
        }

        View itemView = holder.itemView;
        final GridSLM.LayoutParams lp = GridSLM.LayoutParams.from(itemView.getLayoutParams());
        lp.setSlm(isLinear() ? LinearSLM.ID : GridSLM.ID);
        lp.setFirstPosition(getHeaderPosition(position));
        itemView.setLayoutParams(lp);
    }

    @Override
    public final int getItemViewType(int position) {
        return isHeader(position) ? VIEW_TYPE_HEADER_ELEMENT : VIEW_TYPE_LIST_ELEMENT;
    }

    public abstract VH onCreateHeaderViewHolder(ViewGroup parent);

    public abstract VH onCreateElementViewHolder(ViewGroup parent);

    protected abstract void onBindHeaderViewHolder(VH holder, int position);

    protected abstract void onBindElementViewHolder(VH holder, int position);

    protected abstract int getHeaderPosition(int position);

    public abstract boolean isHeader(int position);

    public abstract boolean isLinear();
}
