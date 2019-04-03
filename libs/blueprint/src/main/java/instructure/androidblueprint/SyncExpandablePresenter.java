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

import com.instructure.canvasapi2.models.CanvasComparable;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;


public abstract class SyncExpandablePresenter<GROUP, MODEL extends CanvasComparable, VIEW extends SyncExpandableManager>
        implements Presenter<VIEW> {

    public abstract void loadData(boolean forceNetwork);
    public abstract void refresh(boolean forceNetwork);

    private ListChangeCallback mListChangeCallback;

    private GroupSortedList<GROUP, MODEL> mData;
    private VIEW mView;

    public SyncExpandablePresenter(Class<GROUP> groupClass, Class<MODEL> modelClass) {
        mData = new GroupSortedList<>(groupClass, modelClass, mVisualArrayCallback, mComparatorCallback, mItemComparatorCallback);
    }

    //region Comparators

    GroupSortedList.VisualArrayCallback mVisualArrayCallback = new GroupSortedList.VisualArrayCallback() {
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
    };

    @SuppressWarnings("unchecked")
    GroupSortedList.GroupComparatorCallback<GROUP> mComparatorCallback = new GroupSortedList.GroupComparatorCallback<GROUP>() {
        @Override
        public int compare(GROUP o1, GROUP o2) {
            return SyncExpandablePresenter.this.compare(o1, o2);
        }

        @Override
        public boolean areContentsTheSame(GROUP group1, GROUP group2) {
            return SyncExpandablePresenter.this.areContentsTheSame(group1, group2);
        }

        @Override
        public boolean areItemsTheSame(GROUP group1, GROUP group2) {
            return SyncExpandablePresenter.this.areItemsTheSame(group1, group2);
        }

        @Override
        public long getUniqueGroupId(GROUP group) {
            return SyncExpandablePresenter.this.getUniqueGroupId(group);
        }

        @Override
        public int getGroupType(GROUP group) {
            return SyncExpandablePresenter.this.getGroupType();
        }
    };

    @SuppressWarnings("unchecked")
    GroupSortedList.ItemComparatorCallback<GROUP, MODEL> mItemComparatorCallback = new GroupSortedList.ItemComparatorCallback<GROUP, MODEL>() {
        @Override
        public int compare(GROUP group, MODEL o1, MODEL o2) {
            return SyncExpandablePresenter.this.compare(group, o1, o2);
        }

        @Override
        public boolean areContentsTheSame(MODEL item1, MODEL item2) {
            return SyncExpandablePresenter.this.areContentsTheSame(item1, item2);
        }

        @Override
        public boolean areItemsTheSame(MODEL item1, MODEL item2) {
            return SyncExpandablePresenter.this.areItemsTheSame(item1, item2);
        }

        @Override
        public long getUniqueItemId(MODEL item) {
            return SyncExpandablePresenter.this.getUniqueItemId(item);
        }

        @Override
        public int getChildType(GROUP group, MODEL item) {
            return SyncExpandablePresenter.this.getChildType();
        }
    };

    //endregion

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

    public @NonNull GroupSortedList<GROUP, MODEL> getData() {
        return mData;
    }

    public @Nullable VIEW getViewCallback() {
        return mView;
    }

    public boolean isEmpty() {
        return (mData == null || mData.size() == 0);
    }

    public void clearData() {
        if(mData != null) {
            mData.clearAll();
            if(mView != null) {
                mView.clearAdapter();
            }
        }
    }

    protected void onRefreshStarted() {
        if(getViewCallback() != null) {
            getViewCallback().onRefreshStarted();
        }
    }

    public int compare(GROUP group, MODEL item1, MODEL item2) {
        return -1;
    }

    public int compare(GROUP group1, GROUP group2) {
        return -1;
    }

    public boolean areContentsTheSame(MODEL item1, MODEL item2) {
        return false;
    }

    public boolean areContentsTheSame(GROUP group1, GROUP group2) {
        return false;
    }

    public boolean areItemsTheSame(MODEL item1, MODEL item2) {
        return false;
    }

    public boolean areItemsTheSame(GROUP group1, GROUP group2) {
        return false;
    }

    public int getChildType() {
        return Types.TYPE_ITEM;
    }

    public int getGroupType() {
        return Types.TYPE_HEADER;
    }

    public long getUniqueItemId(MODEL item) {
        return item.hashCode();
    }

    public long getUniqueGroupId(@NonNull GROUP group) {
        return group.hashCode();
    }

    protected void setListChangeCallback(ListChangeCallback callback) {
        this.mListChangeCallback = callback;
    }
}
