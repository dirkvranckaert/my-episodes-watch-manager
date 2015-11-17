package eu.vranckaert.episodeWatcher.twopointo.view.shows;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.vranckaert.android.viewholder.AbstractRecyclerViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;
import eu.vranckaert.episodeWatcher.twopointo.view.shows.ManageShowsView.ManageShowsListener;

/**
 * Date: 16/11/15
 * Time: 08:37
 *
 * @author Dirk Vranckaert
 */
public class ManageShowsListItemView extends AbstractRecyclerViewHolder implements OnClickListener {
    private final ManageShowsListener mListener;
    private final TextView mShowName;
    private Show mShow;

    public ManageShowsListItemView(LayoutInflater inflater, ViewGroup parent, ManageShowsListener listener) {
        super(inflater, parent, R.layout.new_manage_shows_list_item);
        mListener = listener;

        mShowName = findViewById(R.id.show);

        getView().setOnClickListener(this);
    }

    public void setShow(Show show) {
        mShow = show;
        mShowName.setText(show.getShowName());
    }

    @Override
    public void onClick(View v) {
        mListener.onShowClick(mShow);
    }
}
