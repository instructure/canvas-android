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

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;

import com.instructure.canvasapi2.models.CanvasComparable;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class SyncExpandableRecyclerAdapter<GROUP, MODEL extends CanvasComparable, HOLDER extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<HOLDER>{

    public abstract HOLDER createViewHolder(View v, int viewType);
    public abstract int itemLayoutResId(int viewType);
    public abstract void onBindHeaderHolder(RecyclerView.ViewHolder holder, GROUP group, boolean isExpanded);
    public abstract void onBindChildHolder(RecyclerView.ViewHolder holder, GROUP group, MODEL item);

    public void onBindEmptyHolder(RecyclerView.ViewHolder holder, GROUP group) {}

    private WeakReference<Context> mContext;
    private SyncExpandablePresenter mPresenter;

    public SyncExpandableRecyclerAdapter(final Context context, final SyncExpandablePresenter presenter) {
        mContext = new WeakReference<>(context);
        mPresenter = presenter;
        setExpandedByDefault(expandByDefault());
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
        getList().setDisplayEmptyCell(showEmptyCells());

        // Workaround for a11y bug that causes TalkBack to skip items after expand/collapse
        AccessibilityManager a11yManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        boolean isTalkBackEnabled = !a11yManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN).isEmpty();
        if (isTalkBackEnabled) getList().setDisallowCollapse(true);

        notifyDataSetChanged();
    }

    @Override
    public HOLDER onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResId(viewType), parent, false);
        return createViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(HOLDER baseHolder, int position) {
        GROUP group = getList().getGroup(position);
        GroupSortedList<GROUP, MODEL> list = getList();

        if (list.isVisualEmptyItemPosition(position)) {
            onBindEmptyHolder(baseHolder, group);
        } else if (list.isVisualGroupPosition(position)) {
            onBindHeaderHolder(baseHolder, group, list.isGroupExpanded(group));
        } else {
            onBindChildHolder(baseHolder, group, list.getItem(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getList().isVisualEmptyItemPosition(position)) {
            return Types.TYPE_EMPTY_CELL;
        }
        return getList().getItemViewType(position);
    }

    public int size() {
        if(mPresenter == null) return 0;
        return mPresenter.getData().size();
    }

    @Override
    public int getItemCount() {
        return size();
    }

    // region GROUP, MODEL Helpers

    @SuppressWarnings("unchecked")
    public @NonNull GroupSortedList<GROUP, MODEL> getList() {
        return mPresenter.getData();
    }

    public void addOrUpdateAllItems(GROUP group, List<MODEL> items) {
        getList().addOrUpdateAllItems(group, items);
    }

    public void addOrUpdateAllItems(GROUP group, MODEL[] items) {
        getList().addOrUpdateAllItems(group, items);
    }

    public void addOrUpdateItem(GROUP group, MODEL item) {
        getList().addOrUpdateItem(group, item);
    }

    /**
     * Uses as last resort, if you have the group you will save a lot of looping by using [addOrUpdateItem(GROUP, MODEL)]
     * @param item A model item
     */
    public void addOrUpdateItem(MODEL item) {
        ArrayList<GROUP> groups = getGroups();
        for (GROUP group : groups) {
            for(MODEL model: getItems(group)) {
                if(model.getId() == item.getId()) {
                    addOrUpdateItem(group, item);
                    return;
                }
            }
        }
    }

    public boolean removeItem(MODEL item) {
        return getList().removeItem(item);
    }

    public boolean removeItem(MODEL item, boolean removeGroupIfEmpty) {
        return getList().removeItem(item, removeGroupIfEmpty);
    }

    public MODEL getItem(GROUP group, int storedPosition) {
        return getList().getItem(group, storedPosition);
    }

    public MODEL getItem(int visualPosition){
        return getList().getItem(visualPosition);
    }

    /**
     * Uses as last resort, if you have the group you will save a lot of looping
     * @param itemId A model itemId
     */
    @Nullable
    public MODEL getItem(long itemId) {
        ArrayList<GROUP> groups = getGroups();
        for (GROUP group : groups) {
            for(MODEL model: getItems(group)) {
                if(model.getId() == itemId) {
                    return model;
                }
            }
        }
        return null;
    }

    public long getChildItemId(int position){
        return getList().getItemId(getList().getItem(position));
    }

    @Override
    public long getItemId(int position) {
        throw new UnsupportedOperationException("Method getItemId() is unimplemented in BaseExpandableRecyclerAdapter. Use getChildItemId instead.");
    }

    public ArrayList<MODEL> getItems(GROUP group) {
        return getList().getItems(group);
    }

    public int storedIndexOfItem(GROUP group, MODEL item) {
        return getList().storedIndexOfItem(group, item);
    }

    public void addOrUpdateAllGroups(GROUP[] groups) {
        getList().addOrUpdateAllGroups(groups);
    }

    public void addOrUpdateGroup(GROUP group) {
        getList().addOrUpdateGroup(group);
    }

    public GROUP getGroup(long groupId) {
        return getList().getGroup(groupId);
    }

    public GROUP getGroup(int position) {
        return getList().getGroup(position);
    }

    public ArrayList<GROUP> getGroups() {
        return getList().getGroups();
    }

    public int getGroupCount() { return getList().getGroupCount(); }

    public int getGroupItemCount(GROUP group) {
        return getList().getGroupItemCount(group);
    }

    public void expandCollapseGroup(GROUP group) {
        getList().expandCollapseGroup(group);
    }

    public void collapseAll() {
        getList().collapseAll();
    }

    public void expandAll() {
        getList().expandAll();
    }

    public void expandGroup(GROUP group) {
        getList().expandGroup(group);
    }

    public void expandGroup(GROUP group, boolean isNotifyGroupChange) {
        getList().expandGroup(group, isNotifyGroupChange);
    }

    public void collapseGroup(GROUP group) {
        getList().collapseGroup(group);
    }

    public void collapseGroup(GROUP group, boolean isNotifyGroupChange) {
        getList().collapseGroup(group, isNotifyGroupChange);
    }

    public boolean isGroupExpanded(GROUP group) {
        return getList().isGroupExpanded(group);
    }

    public int getGroupVisualPosition(int position) {
        return getList().getGroupVisualPosition(position);
    }

    public boolean isPositionGroupHeader(int position) {
        return getList().isVisualGroupPosition(position);
    }

    public void clear() {
        getList().clearAll();
        notifyDataSetChanged();
    }

    //endregion

    protected @Nullable
    Context getContext() {
        if(mContext != null) {
            return mContext.get();
        }
        return null;
    }

    public void setExpandedByDefault(boolean isExpandedByDefault) {
        getList().setExpandedByDefault(isExpandedByDefault);
    }

    public boolean expandByDefault() {
        return true;
    }

    /*
     * Override to allow empty cells (views) to be shown within groups
     */
    public boolean showEmptyCells() {
        return false;
    }
}
