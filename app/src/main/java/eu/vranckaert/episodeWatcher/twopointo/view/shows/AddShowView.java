package eu.vranckaert.episodeWatcher.twopointo.view.shows;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import eu.vranckaert.android.recyclerview.RecyclerViewUtil;
import eu.vranckaert.android.viewholder.AbstractViewHolder;
import eu.vranckaert.episodeWatcher.R;
import eu.vranckaert.episodeWatcher.domain.Show;

import java.util.List;

/**
 * Date: 13/11/15
 * Time: 12:23
 *
 * @author Dirk Vranckaert
 */
public class AddShowView extends AbstractViewHolder {
    private final AddShowsListener mListener;

    private final EditText mSearch;
    private final TextView mEmpty;
    private final AddShowListAdapter mAdapter;
    private final RecyclerView mList;

    public AddShowView(LayoutInflater inflater, ViewGroup parent, AddShowsListener listener) {
        super(inflater, parent, R.layout.new_add_show);
        mListener = listener;

        mSearch = findViewById(R.id.search);
        mEmpty = findViewById(R.id.empty);
        mList = findViewById(R.id.list);
        mAdapter = new AddShowListAdapter(getContext(), listener);
        mList.setAdapter(mAdapter);
        RecyclerViewUtil.init(mList);

        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String needle = s.toString();
                if (!TextUtils.isEmpty(needle) && needle.length() >= 3) {
                    mListener.searchShows(needle);
                } else {
                    mEmpty.setVisibility(VISIBLE);
                    mList.setVisibility(GONE);
                    mListener.cancelPreviousSearch();
                }
            }
        });
    }

    public void setShows(List<Show> result) {
        boolean notEmpty = result != null && result.size() > 0;
        mEmpty.setVisibility(notEmpty ? GONE : VISIBLE);
        mList.setVisibility(notEmpty ? VISIBLE : GONE);
        if (notEmpty) {
            mAdapter.setShows(result);
        }
    }

    public interface AddShowsListener {
        void searchShows(CharSequence needle);

        void cancelPreviousSearch();

        void addShow(Show show);
    }
}
