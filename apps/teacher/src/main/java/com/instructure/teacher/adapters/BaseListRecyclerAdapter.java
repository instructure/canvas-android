/*
 * Copyright (C) 2017 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.teacher.adapters;

import android.content.Context;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.canvasapi2.models.CanvasComparable;
import com.instructure.pandarecycler.util.UpdatableSortedList;

import java.util.Arrays;
import java.util.List;

/**
 * This is a stripped down base for our ListRecyclerAdapters. Most of the coupled functionality
 * was torn out of this and added to BaseListPresenter
 */
public abstract class BaseListRecyclerAdapter<MODEL extends CanvasComparable, T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T>{

    private static final int DEFAULT_LIST_SIZE = 50; // List will increase in size automatically

    public abstract void bindHolder(MODEL model, T holder, int position);
    public abstract T createViewHolder(View v, int viewType);
    public abstract int itemLayoutResId(int viewType);

    private UpdatableSortedList<MODEL> mList;
    private SortedList.Callback<MODEL> mCallback;
    private ItemComparableCallback<MODEL> mItemCallback;
    private Context mContext;


    public static abstract class ItemComparableCallback<MDL extends CanvasComparable> { // Provides optional overrides
        public int compare(MDL o1, MDL o2) {
            return o1.compareTo(o2);
        }

        public boolean areContentsTheSame(MDL oldItem, MDL newItem) {
            return false;
        }

        public boolean areItemsTheSame(MDL item1, MDL item2) {
            return item1.getId() == item2.getId();
        }

        public long getUniqueItemId(MDL mdl) {
            return mdl.getId();
        }
    }

    public BaseListRecyclerAdapter(Context context, Class<MODEL> klazz) {
        mContext = context;
        mItemCallback = new ItemComparableCallback<MODEL>() {};
        mCallback = new SortedList.Callback<MODEL>() {
            @Override
            public int compare(MODEL o1, MODEL o2) {
                return mItemCallback.compare(o1, o2);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(MODEL oldItem, MODEL newItem) {
                if (mItemCallback != null) {
                    return mItemCallback.areContentsTheSame(oldItem, newItem);
                }
                return false;
            }

            @Override
            public boolean areItemsTheSame(MODEL item1, MODEL item2) {
                if (mItemCallback != null) {
                    return mItemCallback.areItemsTheSame(item1, item2);
                }
                return item1.getId() == item2.getId();
            }
        };
        mList = new UpdatableSortedList<MODEL>(klazz, mCallback, new UpdatableSortedList.ItemCallback<MODEL>() {
            @Override
            public long getId(MODEL model) {
                return mItemCallback.getUniqueItemId(model);
            }
        }, DEFAULT_LIST_SIZE);
    }

    public BaseListRecyclerAdapter(Context context, Class<MODEL> klazz, List items) {
        this(context, klazz);
        addAll(items);
    }

    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        this.mContext = parent.getContext();
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResId(viewType), parent, false);
        return createViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(T baseHolder, int position) {
        if (position < mList.size()) {
            bindHolder(mList.get(position), baseHolder, position);
        }
    }

    public int size() {
        return mList.size();
    }

    @Override
    public int getItemCount() {
        return size();
    }

    public void clear() {
        mList.clear();
    }

    // region MODEL Helpers

    /**
     * The loading footer from pagination will be position == size(). So it'll be index out of bounds.
     * Perform a check before calling getItemAtPosition or make sure isPaginated() returns false
     *
     * @param position
     * @return
     */
    public MODEL getItemAtPosition(int position) {
        return mList.get(position);
    }

    public int indexOf(MODEL item) {
        return mList.indexOf(item);
    }

    public void add(MODEL item) {
        mList.addOrUpdate(item);
    }

    public void addAll(List<MODEL> items) {
        mList.beginBatchedUpdates();
        for (MODEL item : items) {
            add(item);
        }
        mList.endBatchedUpdates();
    }

    public void addAll(MODEL[] items) {
        addAll(Arrays.asList(items));
    }

    public void removeItemAt(int position) {
        mList.removeItemAt(position);
    }

    public void remove(MODEL item) {
        mList.remove(item);
    }

    // endregion

    // region Getter & Setters
    public ItemComparableCallback<MODEL> getItemCallback() {
        return mItemCallback;
    }

    public void setItemCallback(ItemComparableCallback<MODEL> itemCallback) {
        this.mItemCallback = itemCallback;
    }

    /**
     * Must be nulled on fragment/activity destruction to avoid mem leaks using destroyContext
     * @return
     */
    public Context getContext() {
        return mContext;
    }

    public void destroyContext() {
        mContext = null;
    }

    public UpdatableSortedList<MODEL> getSaveData() {
        return mList;
    }
    // endregion
}
