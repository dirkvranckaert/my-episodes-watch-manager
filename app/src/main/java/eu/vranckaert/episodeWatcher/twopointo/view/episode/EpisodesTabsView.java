package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.ListView;
import eu.vranckaert.android.viewholder.AbstractViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.twopointo.view.episode.EpisodesListAdapter.EpisodesListListener;

import java.util.List;

/**
 * Date: 04/11/15
 * Time: 07:53
 *
 * @author Dirk Vranckaert
 */
public class EpisodesTabsView extends AbstractViewHolder implements OnPageChangeListener, OnRefreshListener {
    private final EpisodesTabListener mListener;
    private final TabLayout mTabs;
    private final SwipeRefreshLayout mRefresh;
    private final ViewPager mViewpager;
    private final EpisodesTabsAdapter mAdapter;

    private int mCurrentPage = 0;
    private boolean[] mLoadingTabs;

    public EpisodesTabsView(LayoutInflater inflater, ViewGroup container, EpisodesTabListener episodesTabListener,
                            EpisodesListListener episodesListListener) {
        super(inflater, container, R.layout.new_episodes_tab);
        mListener = episodesTabListener;

        mTabs = findViewById(R.id.tabs);
        mRefresh = findViewById(R.id.refresh);
        mViewpager = findViewById(R.id.viewpager);
        mTabs.post(new Runnable() {
            @Override
            public void run() {
                mTabs.setupWithViewPager(mViewpager);
            }
        });
        mAdapter = new EpisodesTabsAdapter(getContext(), episodesListListener);
        mViewpager.setAdapter(mAdapter);

        mRefresh.setOnRefreshListener(this);
        mRefresh.setColorSchemeResources(R.color.accent_color, R.color.primary_color);

        mLoadingTabs = new boolean[2];

        fixSwipeRefreshLayoutWithTabsAndLists();
    }

    private void fixSwipeRefreshLayoutWithTabsAndLists() {
        // http://stackoverflow.com/questions/25978462/swiperefreshlayout-viewpager-limit-horizontal-scroll-only
        // Fixes issue where you start scrolling in the viewpager, while scrolling go a little down and the refresh
        // takes over and resets the view pager to it's original place.
        mViewpager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        mRefresh.setEnabled(false);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        mRefresh.setEnabled(true);
                        break;
                }
                return false;
            }
        });
        mViewpager.addOnPageChangeListener(this);

        mRefresh.getViewTreeObserver().addOnScrollChangedListener(new OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                RecyclerView list = mAdapter.getListFor(mCurrentPage);
                if (list != null) {
                    if (list.computeVerticalScrollOffset() == 0) {
                        mRefresh.setEnabled(true);
                    } else {
                        mRefresh.setEnabled(false);
                    }
                }
            }
        });
    }

    public void setLoadingEpisodesToWatch(boolean loading) {
        mLoadingTabs[0] = loading;
        syncLoadingState();
    }

    public void setLoadingEpisodesToAcquire(boolean loading) {
        mLoadingTabs[1] = loading;
        syncLoadingState();
    }

    private void syncLoadingState() {
        boolean isOneTabLoading = false;

        int size = mLoadingTabs.length;
        for (int i = 0; i < size; i++) {
            boolean loading = mLoadingTabs[i];
            if (loading) {
                isOneTabLoading = true;
                break;
            }
        }

        mRefresh.setRefreshing(isOneTabLoading);
    }

    public void setEpisodesToWatch(List<Episode> episodes) {
        setLoadingEpisodesToWatch(false);
        mAdapter.setEpisodesToWatch(episodes);
    }

    public void setEpisodesToAcquire(List<Episode> episodes) {
        setLoadingEpisodesToAcquire(false);
        mAdapter.setEpisodesToAcquire(episodes);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPage = position;
        mAdapter.onPageChanged(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void onEpisodesMarkedAcquired(List<Episode> episodes) {
        mAdapter.onEpisodesMarkedAcquired(episodes);
    }

    public void onEpisodesMarkedWatched(List<Episode> episodes) {
        mAdapter.onEpisodesMarkedWatched(episodes);
    }

    public void onEpisodesNotMarkedAcquired(List<Episode> episodes) {
        mAdapter.onEpisodesNotMarkedAcquired(episodes);
    }

    public void onEpisodesNotMarkedWatched(List<Episode> episodes) {
        mAdapter.onEpisodesNotMarkedWatched(episodes);
    }

    public void episodeCountHasUpdated() {
        final int tabPosition = mTabs.getSelectedTabPosition();
        mTabs.setupWithViewPager(mViewpager);
        mViewpager.setAdapter(mAdapter);
        mTabs.post(new Runnable() {
            @Override
            public void run() {
                mTabs.getTabAt(tabPosition).select();
            }
        });
    }

    @Override
    public void onRefresh() {
        int currentTab = mViewpager.getCurrentItem();
        if (currentTab == 0) {
            mListener.startRefreshingEpisodesToWatch();
        } else if (currentTab == 1) {
            mListener.startRefreshingEpisodesToAcquire();
        }
    }

    public interface EpisodesTabListener {
        void startRefreshingEpisodesToWatch();

        void startRefreshingEpisodesToAcquire();
    }
}
