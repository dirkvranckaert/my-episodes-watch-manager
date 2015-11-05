package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import eu.vranckaert.android.viewholder.AbstractViewHolder;
import eu.vranckaert.episodeWatcher.R;

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
}
