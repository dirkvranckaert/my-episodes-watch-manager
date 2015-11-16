package eu.vranckaert.episodeWatcher.twopointo.view.shows;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.episodeWatcher.domain.Show;
import eu.vranckaert.episodeWatcher.twopointo.view.shows.AddShowView.AddShowsListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 13/11/15
 * Time: 14:19
 *
 * @author Dirk Vranckaert
 */
public class AddShowListAdapter extends Adapter<AddShowsListItemView> {
    private final List<Show> mShows = new ArrayList<>();
    private final LayoutInflater mLayoutInflater;
    private final AddShowsListener mListener;

    public AddShowListAdapter(Context context, AddShowsListener listener) {
        mLayoutInflater = LayoutInflater.from(context);
        mListener = listener;
    }

    public void setShows(List<Show> shows) {
        mShows.clear();
        mShows.addAll(shows);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mShows.size();
    }

    @Override
    public AddShowsListItemView onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AddShowsListItemView(mLayoutInflater, parent, mListener);
    }

    @Override
    public void onBindViewHolder(AddShowsListItemView holder, int position) {
        Show show = mShows.get(position);
        holder.setShow(show);
    }
}
