package eu.vranckaert.episodeWatcher.twopointo.view.episode;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.vranckaert.android.viewholder.AbstractViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Episode;
import eu.vranckaert.episodeWatcher.enums.EpisodeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 04/11/15
 * Time: 07:58
 *
 * @author Dirk Vranckaert
 */
public class EpisodesTabsAdapter extends PagerAdapter {
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;

    private List<Episode> mEpisodesToWatch = new ArrayList<>();
    private boolean mLoadingEpisodesToWatch;
    private List<Episode> mEpisodesToAcquire = new ArrayList<>();
    private boolean mLoadingEpisodesToAcquire;

    private EpisodesListView mEpisodesToWatchView;
    private EpisodesListView mEpisodesToAcquireView;

    public EpisodesTabsAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setLoadingEpisodesToWatch(boolean loading) {
        mLoadingEpisodesToWatch = loading;
        if (mEpisodesToWatchView != null) {
            mEpisodesToWatchView.setLoading(mLoadingEpisodesToWatch);
        }
    }

    public void setEpisodesToWatch(List<Episode> episodes) {
        mEpisodesToWatch.clear();
        mEpisodesToWatch.addAll(episodes);
        setLoadingEpisodesToWatch(false);
        if (mEpisodesToWatchView != null) {
            mEpisodesToWatchView.setEpisodes(mEpisodesToWatch);
        }
        notifyDataSetChanged();
    }

    public void setLoadingEpisodesToAcquire(boolean loading) {
        mLoadingEpisodesToAcquire = loading;
        if (mEpisodesToAcquireView != null) {
            mEpisodesToAcquireView.setLoading(mLoadingEpisodesToAcquire);
        }
    }

    public void setEpisodesToAcquire(List<Episode> episodes) {
        mEpisodesToAcquire.clear();
        mEpisodesToAcquire.addAll(episodes);
        setLoadingEpisodesToAcquire(false);
        if (mEpisodesToAcquireView != null) {
            mEpisodesToAcquireView.setEpisodes(mEpisodesToAcquire);
        }
        notifyDataSetChanged();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return mContext.getString(R.string.watchhome, mEpisodesToWatch.size());
        } else {
            return mContext.getString(R.string.acquirehome, mEpisodesToAcquire.size());
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        EpisodesListView view;
        if (position == 0) {
            if (mEpisodesToWatchView == null) {
                mEpisodesToWatchView = new EpisodesListView(mLayoutInflater, container, EpisodeType.EPISODES_TO_WATCH);
                mEpisodesToWatchView.setLoading(mLoadingEpisodesToWatch);
                mEpisodesToWatchView.setEpisodes(mEpisodesToWatch);
            }
            view = mEpisodesToWatchView;
        } else {
            if (mEpisodesToAcquireView == null) {
                mEpisodesToAcquireView = new EpisodesListView(mLayoutInflater, container, EpisodeType.EPISODES_TO_ACQUIRE);
                mEpisodesToWatchView.setLoading(mLoadingEpisodesToAcquire);
                mEpisodesToAcquireView.setEpisodes(mEpisodesToAcquire);
            }
            view = mEpisodesToAcquireView;
        }

        if (view != null) {
            container.addView(view.getView(), position);
        }
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(((AbstractViewHolder) object).getView());
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.getTag() != null && view.getTag().equals(object);
    }

    public void onPageChanged(int position) {
        if (position == 0) {
            mEpisodesToAcquireView.cancelContextualActionbar();
        } else {
            mEpisodesToWatchView.cancelContextualActionbar();
        }
    }
}
