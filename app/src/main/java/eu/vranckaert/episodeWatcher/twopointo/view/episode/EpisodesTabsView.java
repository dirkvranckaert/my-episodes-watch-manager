package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.android.viewholder.AbstractViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Episode;

import java.util.List;

/**
 * Date: 04/11/15
 * Time: 07:53
 *
 * @author Dirk Vranckaert
 */
public class EpisodesTabsView extends AbstractViewHolder {
    private final TabLayout mTabs;
    private final ViewPager mViewpager;
    private final EpisodesTabsAdapter mAdapter;

    public EpisodesTabsView(LayoutInflater inflater, ViewGroup container) {
        super(inflater, container, R.layout.new_episodes_tab);

        mTabs = findViewById(R.id.tabs);
        mViewpager = findViewById(R.id.viewpager);
        mTabs.post(new Runnable() {
            @Override
            public void run() {
                mTabs.setupWithViewPager(mViewpager);
            }
        });
        mAdapter = new EpisodesTabsAdapter(getContext());
        mViewpager.setAdapter(mAdapter);
    }

    public void startLoadingAll() {
        mAdapter.setLoadingEpisodesToWatch(true);
        mAdapter.setLoadingEpisodesToWatch(true);
    }

    public void setEpisodesToWatch(List<Episode> episodes) {
        mAdapter.setEpisodesToWatch(episodes);
        mViewpager.setAdapter(mAdapter);
    }

    public void setEpisodesToAcquire(List<Episode> episodes) {
        mAdapter.setEpisodesToAcquire(episodes);
        mViewpager.setAdapter(mAdapter);
    }
}
