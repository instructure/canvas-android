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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.SortedList;

import com.instructure.pandarecycler.util.UpdatableSortedList;


public abstract class ListPresenter<MODEL, VIEW extends ListManager> implements Presenter<VIEW> {

    private ListChangeCallback mListChangeCallback;

    public abstract void loadData(boolean forceNetwork);
    public abstract void refresh(boolean forceNetwork);
    public abstract long getItemId(@NonNull MODEL item);

    private UpdatableSortedList<MODEL> mData;
    private VIEW mView;

    @SuppressWarnings("unchecked")
    public ListPresenter(Class<MODEL> clazz) {
        mData = new UpdatableSortedList<>(clazz, new SortedList.Callback<MODEL>() {
            @Override
            public int compare(MODEL o1, MODEL o2) {
                return ListPresenter.this.compare(o1, o2);
            }

            @Override
            public void onInserted(int position, int count) {
                if (mListChangeCallback != null) {
                    mListChangeCallback.onInserted(position, count);
                }
            }

            @Override
            public void onRemoved(int position, int count) {
                if (mListChangeCallback != null) {
                    mListChangeCallback.onRemoved(position, count);
                }
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                if (mListChangeCallback != null) {
                    mListChangeCallback.onMoved(fromPosition, toPosition);
                }
            }

            @Override
            public void onChanged(int position, int count) {
                if (mListChangeCallback != null) {
                    mListChangeCallback.onChanged(position, count);
                }
            }

            @Override
            public boolean areContentsTheSame(MODEL item1, MODEL item2) {
                return ListPresenter.this.areContentsTheSame(item1, item2);
            }

            @Override
            public boolean areItemsTheSame(MODEL item1, MODEL item2) {
                return ListPresenter.this.areItemsTheSame(item1, item2);
            }

        }, new UpdatableSortedList.ItemCallback<MODEL>() {
            @Override
            public long getId(MODEL model) {
                return getItemId(model);
            }
        });
    }

    @Override
    public Presenter onViewAttached(@NonNull VIEW view) {
        mView = view;
        return this;
    }

    @Override
    public void onViewDetached() {
        mView = null;
    }

    @Override
    public void onDestroyed() {
        mView = null;
    }

    public @NonNull UpdatableSortedList<MODEL> getData() {
        return mData;
    }

    public void clearData() {
        if(mData != null) {
            mData.clear();
            if(mView != null) {
                mView.clearAdapter();
            }
        }
    }

    public @Nullable VIEW getViewCallback() {
        return mView;
    }

    public boolean isEmpty() {
        return (mData == null || mData.size() == 0);
    }

    protected void onRefreshStarted() {
        if(getViewCallback() != null) {
            getViewCallback().onRefreshStarted();
        }
    }

    @SuppressWarnings("unchecked")
    protected int compare(MODEL item1, MODEL item2) {
        return -1;
    }

    @SuppressWarnings("UnusedParameters")
    protected boolean areContentsTheSame(MODEL item1, MODEL item2) {
        return false;
    }

    protected boolean areItemsTheSame(MODEL item1, MODEL item2) {
        return getItemId(item1) == getItemId(item2);
    }

    @SuppressWarnings("WeakerAccess")
    public void setListChangeCallback(ListChangeCallback callback) {
        this.mListChangeCallback = callback;
    }

}
