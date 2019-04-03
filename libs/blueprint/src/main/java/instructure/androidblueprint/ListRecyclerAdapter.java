/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package instructure.androidblueprint;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.pandarecycler.util.UpdatableSortedList;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * This is a stripped down base for our ListRecyclerAdapters. Most of the coupled functionality
 * was torn out of this and added to BaseListPresenter
 */
public abstract class ListRecyclerAdapter<MODEL, HOLDER extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<HOLDER>{

    public abstract void bindHolder(MODEL model, HOLDER holder, int position);
    public abstract HOLDER createViewHolder(View v, int viewType);
    public abstract int itemLayoutResId(int viewType);

    private WeakReference<Context> mContext;
    private ListPresenter mPresenter;

    public ListRecyclerAdapter(final Context context, final ListPresenter presenter) {
        mContext = new WeakReference<>(context);
        mPresenter = presenter;
        mPresenter.setListChangeCallback(new ListChangeCallback() {
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
        });
        notifyDataSetChanged();
    }

    @Override
    public HOLDER onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResId(viewType), parent, false);
        return createViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(HOLDER baseHolder, int position) {
        if (position < getList().size()) {
            bindHolder(getList().get(position), baseHolder, baseHolder.getAdapterPosition());
        }
    }

    public int size() {
        if(mPresenter == null) return 0;
        return mPresenter.getData().size();
    }

    @Override
    public int getItemCount() {
        return size();
    }

    public void clear() {
        getList().clear();
        notifyDataSetChanged();
    }

    // region MODEL Helpers

    /**
     * The loading footer from pagination will be position == size(). So it'll be index out of bounds.
     * Perform a check before calling getItemAtPosition or make sure isPaginated() returns false
     *
     * @param position The position of the item requested
     * @return A model item
     */
    @SuppressWarnings("unused")
    public MODEL getItemAtPosition(int position) {
        return getList().get(position);
    }

    public int indexOf(MODEL item) {
        return getList().indexOf(item);
    }

    public void add(MODEL item) {
        getList().addOrUpdate(item);
    }

    public void addAll(List<MODEL> items) {
        getList().beginBatchedUpdates();
        for (MODEL item : items) {
            add(item);
        }
        getList().endBatchedUpdates();
    }

    @SuppressWarnings("unused")
    public void removeItemAt(int position) {
        getList().removeItemAt(position);
    }

    public void remove(MODEL item) {
        getList().remove(item);
    }

    // endregion

    @SuppressWarnings("unchecked")
    public @NonNull UpdatableSortedList<MODEL> getList() {
        return mPresenter.getData();
    }

    protected @Nullable Context getContext() {
        if(mContext != null) {
            return mContext.get();
        }
        return null;
    }

    protected ListPresenter getPresenter() {
        return mPresenter;
    }
}
