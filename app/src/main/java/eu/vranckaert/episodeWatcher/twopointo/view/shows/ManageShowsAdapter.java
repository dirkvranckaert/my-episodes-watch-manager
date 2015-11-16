package eu.vranckaert.episodeWatcher.twopointo.view.shows;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.episodeWatcher.domain.Show;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Date: 16/11/15
 * Time: 08:35
 *
 * @author Dirk Vranckaert
 */
public class ManageShowsAdapter extends Adapter<ManageShowsListItemView> {
    private final LayoutInflater mLayoutInflater;
    private final List<Show> mShows = new ArrayList<>();

    public ManageShowsAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    protected void addShow(Show show) {
        mShows.add(show);
        sortShows();
        int index = mShows.indexOf(show);
        notifyItemInserted(index);
    }

    protected void setShows(List<Show> shows) {
        mShows.clear();
        mShows.addAll(shows);
        sortShows();
        notifyDataSetChanged();
    }

    private void sortShows() {
        Collections.sort(mShows, new Comparator<Show>() {
            @Override
            public int compare(Show lhs, Show rhs) {
                return lhs.getShowName().compareTo(rhs.getShowName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mShows.size();
    }

    @Override
    public ManageShowsListItemView onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ManageShowsListItemView(mLayoutInflater, parent);
    }

    @Override
    public void onBindViewHolder(ManageShowsListItemView holder, int position) {
        holder.setShow(mShows.get(position));
    }
}
