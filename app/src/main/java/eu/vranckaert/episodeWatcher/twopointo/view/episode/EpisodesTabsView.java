package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
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
public class EpisodesTabsView extends AbstractViewHolder implements OnPageChangeListener {
    private final TabLayout mTabs;
    private final ViewPager mViewpager;
    private final EpisodesTabsAdapter mAdapter;

    public EpisodesTabsView(LayoutInflater inflater, ViewGroup container, EpisodesListListener listener) {
        super(inflater, container, R.layout.new_episodes_tab);

        mTabs = findViewById(R.id.tabs);
        mViewpager = findViewById(R.id.viewpager);
        mTabs.post(new Runnable() {
            @Override
            public void run() {
                mTabs.setupWithViewPager(mViewpager);
            }
        });
        mAdapter = new EpisodesTabsAdapter(getContext(), listener);
        mViewpager.setAdapter(mAdapter);

        mViewpager.addOnPageChangeListener(this);
    }

    public void startLoadingAll() {
        mAdapter.setLoadingEpisodesToWatch(true);
        mAdapter.setLoadingEpisodesToWatch(true);
    }

    public void setEpisodesToWatch(List<Episode> episodes) {
        mAdapter.setEpisodesToWatch(episodes);
    }

    public void setEpisodesToAcquire(List<Episode> episodes) {
        mAdapter.setEpisodesToAcquire(episodes);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
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
}
