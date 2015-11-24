package eu.vranckaert.episodeWatcher.twopointo.view.shows;

import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.android.recyclerview.RecyclerViewUtil;
import eu.vranckaert.android.recyclerview.decorator.SwipeBackgroundItemDecoration;
import eu.vranckaert.android.viewholder.AbstractViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;

import java.util.List;

/**
 * Date: 16/11/15
 * Time: 08:32
 *
 * @author Dirk Vranckaert
 */
public class ManageShowsView extends AbstractViewHolder implements OnRefreshListener {
    private final ManageShowsListener mListener;
    private final SwipeRefreshLayout mRefresh;
    private final RecyclerView mList;
    private final ManageShowsAdapter mAdapter;

    public ManageShowsView(LayoutInflater inflater, ViewGroup parent, ManageShowsListener listener) {
        super(inflater, parent, R.layout.new_manage_shows);
        mListener = listener;

        mRefresh = findViewById(R.id.refresh);
        mRefresh.setOnRefreshListener(this);
        mList = findViewById(R.id.list);
        mList.addItemDecoration(new SwipeBackgroundItemDecoration(getContext(), android.R.color.holo_red_dark,
                R.drawable.ic_delete_white_36dp));
        RecyclerViewUtil.init(mList);
        mAdapter = new ManageShowsAdapter(getContext(), listener);
        mList.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        Log.d("dirk", "swipeDir=" + swipeDir);
                        final int position = viewHolder.getAdapterPosition();
                        if (ItemTouchHelper.LEFT == swipeDir) {
                            mList.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyItemChanged(position);
                                }
                            }, 500);

                            Snackbar snackbar =
                                    Snackbar.make(getView(), R.string.favoIgnoredIgnoredShow, Snackbar.LENGTH_LONG);
                            snackbar.show();
                        } else if (ItemTouchHelper.RIGHT == swipeDir) {
                            final Show removedShow = mAdapter.getItem(position);
                            mListener.onRemoveShow(removedShow);
                        }
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mList);
    }

    public void setShows(List<Show> shows) {
        mAdapter.setShows(shows);
    }

    public void addShow(Show show) {
        mAdapter.addShow(show);
    }

    public SwipeRefreshLayout getRefreshView() {
        return mRefresh;
    }

    @Override
    public void onRefresh() {
        mListener.refresh();
    }

    public void removeShow(Show show) {
        mAdapter.remove(show);
    }

    public interface ManageShowsListener {
        void refresh();

        void onShowClick(Show show);

        void onRemoveShow(Show show);
    }
}
