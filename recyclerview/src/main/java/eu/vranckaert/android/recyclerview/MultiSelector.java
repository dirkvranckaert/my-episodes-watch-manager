package eu.vranckaert.android.recyclerview;

import android.app.Activity;
import android.support.annotation.MenuRes;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Date: 16/10/15
 * Time: 06:46
 *
 * @author Dirk Vranckaert
 */
public class MultiSelector implements Callback {
    private final MultiSelectorListener mListener;
    private final Activity mActivity;
    private final Adapter mAdapter;

    private SparseBooleanArray mSelectedPositions = new SparseBooleanArray();
    private boolean mSelectionMode = false;
    private ActionMode mActionMode;

    public MultiSelector(Activity activity, Adapter adapter) {
        this(activity, adapter, null);
    }

    public MultiSelector(Activity activity, Adapter adapter, MultiSelectorListener listener) {
        mActivity = activity;
        mAdapter = adapter;
        mListener = listener;
    }

    private void setItemChecked(int position, boolean isChecked) {
        mSelectionMode = true;

        mSelectedPositions.put(position, isChecked);
        mAdapter.notifyItemChanged(position);

        if (mActionMode == null) {
            mActionMode = mActivity.startActionMode(this);
        }
        if (mActionMode != null) {
            mActionMode.invalidate();
            if (mListener == null || !mListener.onSelectionUpdate(this, mActionMode)) {
                int selectionCount = getSelectedItemCount();
                String title =
                        mActivity.getString(selectionCount == 0 ? R.string.vr_vh_action_mode_title_selection_zero :
                                selectionCount == 1 ? R.string.vr_vh_action_mode_title_selection_one :
                                        R.string.vr_vh_action_mode_title_selection_multiple, "" + selectionCount);
                mActionMode.setTitle(title);
            }
        }
    }

    public final void setItemChecked(ViewHolder viewHolder, boolean isChecked) {
        int position = viewHolder.getAdapterPosition();

        setItemChecked(position, isChecked);
    }

    public final boolean isSelectionMode() {
        return mSelectionMode;
    }

    public final int getSelectedItemCount() {
        int count = 0;
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            int key = mSelectedPositions.keyAt(i);
            if (mSelectedPositions.get(key)) {
                count++;
            }
        }
        return count;
    }

    public final List<Integer> getSelectedPositions() {
        List<Integer> selectedPositions = new ArrayList<>();
        for (int i = 0; i < mSelectedPositions.size(); i++) {
            int key = mSelectedPositions.keyAt(i);
            if (mSelectedPositions.get(key)) {
                selectedPositions.add(key);
            }
        }
        return selectedPositions;
    }

    public final List<Integer> getSelectedPositionsInReversedOrder() {
        List<Integer> selectedPositions = getSelectedPositions();
        Collections.sort(selectedPositions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return lhs.compareTo(rhs) * -1;
            }
        });
        return selectedPositions;
    }

    public final boolean tapSelection(ViewHolder viewHolder) {
        if (mSelectionMode) {
            int position = viewHolder.getAdapterPosition();
            boolean isChecked = mSelectedPositions.get(position);
            setItemChecked(viewHolder, !isChecked);
        }
        return mSelectionMode;
    }

    public final boolean isItemChecked(int position) {
        if (position < 0) {
            return false;
        }
        return mSelectedPositions.get(position);
    }

    @Override
    public final boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (mListener != null) {
            mode.getMenuInflater().inflate(mListener.getMenuRes(), menu);
        }
        mode.getMenuInflater().inflate(R.menu.vr_vh_select_all, menu);
        return true;
    }

    @Override
    public final boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        menu.findItem(R.id.vr_vh_select_all).setVisible(!areAllItemsChecked());
        if (mListener != null) {
            mListener.invalidateActionModeMenu(this, menu);
        }
        return true;
    }

    private boolean areAllItemsChecked() {
        int count = mAdapter.getItemCount();
        int selectedCount = getSelectedItemCount();
        return count == selectedCount;
    }

    @Override
    public final boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.vr_vh_select_all) {
            int itemCount = mAdapter.getItemCount();
            for (int i=0; i<itemCount; i++) {
                setItemChecked(i, true);
            }
        } else if (mListener != null && mListener.onActionItemClicked(this, item)) {
            mSelectionMode = false;
            mode.finish();
            mActionMode = null;
            return true;
        }
        return false;
    }

    @Override
    public final void onDestroyActionMode(ActionMode mode) {
        clearSelection();
    }

    public final void cancelActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
            clearSelection();
        }
    }

    private void clearSelection() {
        mActionMode = null;
        mSelectionMode = false;
        mSelectedPositions.clear();
        mAdapter.notifyDataSetChanged();
    }

    public interface MultiSelectorListener {
        boolean onSelectionUpdate(MultiSelector multiSelector, ActionMode actionMode);

        boolean onActionItemClicked(MultiSelector multiSelector, MenuItem item);

        @MenuRes
        int getMenuRes();

        void invalidateActionModeMenu(MultiSelector multiSelector, Menu menu);
    }
}
