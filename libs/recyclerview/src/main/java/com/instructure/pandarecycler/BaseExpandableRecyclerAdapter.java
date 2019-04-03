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

package com.instructure.pandarecycler;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseExpandableRecyclerAdapter<GROUP, ITEM, VIEWHOLDER extends RecyclerView.ViewHolder> extends PaginatedRecyclerAdapter<VIEWHOLDER> {

    /**
     * Create a viewholder based on the given viewType
     * @param v
     * @param viewType
     * @return
     */
    public abstract VIEWHOLDER createViewHolder(View v, int viewType);

    /**
     * Bind the item in a given group
     * @param holder
     * @param group
     * @param item
     */
    public abstract void onBindChildHolder(RecyclerView.ViewHolder holder, GROUP group, ITEM item);

    /**
     * Bind the Header for a given Group
     * @param holder
     * @param group
     * @param isExpanded
     */
    public abstract void onBindHeaderHolder(RecyclerView.ViewHolder holder, GROUP group, boolean isExpanded);

    /**
     * Empty Views are shown when IsExpandedByDefault is set to true and when there are no items in the group.
     * @param holder
     * @param group
     */
    public void onBindEmptyHolder(RecyclerView.ViewHolder holder, GROUP group) {}

    /**
     * Must return a non-null GroupComparatorCallback
     * @return
     */
    public abstract GroupSortedList.GroupComparatorCallback<GROUP> createGroupCallback();

    /**
     * Must return a non-null ItemComparatorCallback
     * @return
     */
    public abstract GroupSortedList.ItemComparatorCallback<GROUP, ITEM> createItemCallback();

    private GroupSortedList<GROUP, ITEM> mGroupSortedList; // Manages all the objects in the list
    private ViewHolderHeaderClicked<GROUP> mViewHolderHeaderClicked;

    private Class<GROUP> mGroupKlazz;
    private Class<ITEM> mItemKlazz;

    private long mSelectedItemId;
    private boolean mIsDisplayEmptyCell = false;
    private boolean mIsChildrenAboveGroup = false;

    public BaseExpandableRecyclerAdapter(Context context, Class<GROUP> groupKlazz, Class<ITEM> itemKlazz) {
        super(context);
        mGroupKlazz = groupKlazz;
        mItemKlazz = itemKlazz;

        mGroupSortedList = new GroupSortedList<>(mGroupKlazz, mItemKlazz, new GroupSortedList.VisualArrayCallback() {
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
        }, createGroupCallback(), createItemCallback());
        mGroupSortedList.setDisplayEmptyCell(mIsDisplayEmptyCell);
        mGroupSortedList.setChildrenAboveGroup(mIsChildrenAboveGroup);

        mViewHolderHeaderClicked = new ViewHolderHeaderClicked<GROUP>() {
            @Override
            public void viewClicked(View view, GROUP group) {
                mGroupSortedList.expandCollapseGroup(group);
            }
        };

        // Workaround for a11y bug that causes TalkBack to skip items after expand/collapse
        AccessibilityManager a11yManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = a11yManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN);
        if (enabledServices != null && !enabledServices.isEmpty()) mGroupSortedList.setDisallowCollapse(true);
    }

    @Override
    public void clear(){
        mGroupSortedList.clearAll();
        notifyDataSetChanged();
    }



    // region Selection

    /**
     * Sets the selected position. The position is saved based on the ID of the object, so if a group
     *  is expaned or collapsed, the correct position remains selected.
     *
     * @param position
     */
    @Override
    public void setSelectedPosition(int position) {
        if (position == -1 || mGroupSortedList.isVisualGroupPosition(position)) { return; }

        if (mSelectedItemId != -1) {
            int oldPosition = mGroupSortedList.getItemVisualPosition(mSelectedItemId);
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition);
            }
        }
        ITEM item = mGroupSortedList.getItem(position);
        if (item != null) {
            mSelectedItemId = mGroupSortedList.getItemId(item);
            notifyItemChanged(position);
        }
        super.setSelectedPosition(position);
    }

    /**
     * True when an item is selected; false otherwise
     * @param item
     * @return
     */
    public boolean isItemSelected(ITEM item) {
        return mGroupSortedList.getItemId(item) == mSelectedItemId;
    }

    @Override
    public void setSelectedItemId(long selectedItemId) {
        this.mSelectedItemId = selectedItemId;
    }

    // endregion

    @Override
    public void onBindViewHolder(VIEWHOLDER baseHolder, int position) {
        GROUP group = mGroupSortedList.getGroup(position);
        if (mGroupSortedList.isVisualEmptyItemPosition(position)) {
            onBindEmptyHolder(baseHolder, group);
        } else if (mGroupSortedList.isVisualGroupPosition(position)) {
            onBindHeaderHolder(baseHolder, group, mGroupSortedList.isGroupExpanded(group));
        } else if (isLoadingFooterPosition(position)) {
            super.onBindViewHolder(baseHolder, position);
        } else {
            onBindChildHolder(baseHolder, group, mGroupSortedList.getItem(position));
        }
    }

    /**
     * The total size of the adapter. This includes GROUPS and ITEMS together
     * @return
     */
    @Override
    public int size() {
        return mGroupSortedList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mGroupSortedList.isVisualEmptyItemPosition(position)) {
            return Types.TYPE_EMPTY_CELL;
        } else if (isLoadingFooterPosition(position)) {
            return super.getItemViewType(position);
        }
        return mGroupSortedList.getItemViewType(position);
    }

    // region helpers

    /**
     * See {@link GroupSortedList#addOrUpdateAllItems}
     * @param group
     * @param items
     */
    public void addOrUpdateAllItems(GROUP group, List<ITEM> items) {
        mGroupSortedList.addOrUpdateAllItems(group, items);
    }

    /**
     * See {@link GroupSortedList#addOrUpdateAllItems}
     * @param group
     * @param items
     */
    public void addOrUpdateAllItems(GROUP group, ITEM[] items) {
        mGroupSortedList.addOrUpdateAllItems(group, items);
    }

    /**
     * See {@link GroupSortedList#addOrUpdateItem}
     * @param group
     * @param item
     */
    public void addOrUpdateItem(GROUP group, ITEM item) {
        mGroupSortedList.addOrUpdateItem(group, item);
    }

    /**
     * See {@link GroupSortedList#removeItem}
     * @param item
     * @return
     */
    public boolean removeItem(ITEM item) {
        return mGroupSortedList.removeItem(item);
    }


    /**
     * See {@link GroupSortedList#removeItem}
     * @param item
     * @return
     */
    public boolean removeItem(ITEM item, boolean removeGroupIfEmpty) {
        return mGroupSortedList.removeItem(item, removeGroupIfEmpty);
    }

    /**
     * See {@link GroupSortedList#getItem}
     * @param group
     * @param storedPosition
     * @return
     */
    public ITEM getItem(GROUP group, int storedPosition) {
        return mGroupSortedList.getItem(group, storedPosition);
    }

    public ITEM getItem(int visualPosition){
        return mGroupSortedList.getItem(visualPosition);
    }

    public long getChildItemId(int position){
        return mGroupSortedList.getItemId(mGroupSortedList.getItem(position));
    }

    @Override
    public long getItemId(int position) {
        throw new UnsupportedOperationException("Method getItemId() is unimplemented in BaseExpandableRecyclerAdapter. Use getChildItemId instead.");
    }

    public ArrayList<ITEM> getItems(GROUP group) {
        return mGroupSortedList.getItems(group);
    }

    /**
     * See {@link GroupSortedList#storedIndexOfItem}
     * @param group
     * @param item
     * @return
     */
    public int storedIndexOfItem(GROUP group, ITEM item) {
        return mGroupSortedList.storedIndexOfItem(group, item);
    }

    /**
     * See {@link GroupSortedList#addOrUpdateAllGroups(Object[])}
     * @param groups
     */
    public void addOrUpdateAllGroups(GROUP[] groups) {
        mGroupSortedList.addOrUpdateAllGroups(groups);
    }

    /**
     * See {@link GroupSortedList#addOrUpdateGroup}
     * @param group
     */
    public void addOrUpdateGroup(GROUP group) {
        mGroupSortedList.addOrUpdateGroup(group);
    }

    /**
     * See {@link GroupSortedList#getGroup(long)}
     * @param groupId
     * @return
     */
    public GROUP getGroup(long groupId) {
        return mGroupSortedList.getGroup(groupId);
    }

    /**
     * See {@link GroupSortedList#getGroup(int)}
     *
     * @param position
     * @return
     */
    public GROUP getGroup(int position) {
        return mGroupSortedList.getGroup(position);
    }

    /**
     * See {@link GroupSortedList#getGroups()}
     * @return
     */
    public ArrayList<GROUP> getGroups() {
        return mGroupSortedList.getGroups();
    }

    /**
     * See {@link GroupSortedList#getGroupCount()}
     *
     * @return
     */
    public int getGroupCount() { return mGroupSortedList.getGroupCount(); }
    /**
     * See {@link GroupSortedList#getGroupItemCount}
     * @param group
     * @return
     */
    public int getGroupItemCount(GROUP group) {
        return mGroupSortedList.getGroupItemCount(group);
    }

    /**
     * See {@link GroupSortedList#expandCollapseGroup}
     * @param group
     */
    public void expandCollapseGroup(GROUP group) {
        mGroupSortedList.expandCollapseGroup(group);
    }

    /**
     * See {@link GroupSortedList#collapseAll()}
     */
    public void collapseAll() {
        mGroupSortedList.collapseAll();
    }

    /**
     * See {@link GroupSortedList#expandAll()}
     */
    public void expandAll() {
        mGroupSortedList.expandAll();
    }


    /**
     * See {@link GroupSortedList#expandGroup}
     * @param group
     */
    public void expandGroup(GROUP group) {
        mGroupSortedList.expandGroup(group);
    }

    /**
     * See {@link GroupSortedList#expandGroup(Object, boolean)}
     * @param group
     * @param isNotifyGroupChange
     */
    public void expandGroup(GROUP group, boolean isNotifyGroupChange) {
        mGroupSortedList.expandGroup(group, isNotifyGroupChange);
    }

    /**
     * See {@link GroupSortedList#collapseGroup(Object)}
     * @param group
     */
    public void collapseGroup(GROUP group) {
        mGroupSortedList.collapseGroup(group);
    }

    /**
     * See {@link GroupSortedList#collapseGroup(Object, boolean)}
     * @param group
     * @param isNotifyGroupChange
     */
    public void collapseGroup(GROUP group, boolean isNotifyGroupChange) {
        mGroupSortedList.collapseGroup(group, isNotifyGroupChange);
    }

    /**
     * See {@link GroupSortedList#isGroupExpanded(Object)}
     * @param group
     * @return
     */
    public boolean isGroupExpanded(GROUP group) {
        return mGroupSortedList.isGroupExpanded(group);
    }

    /**
     * See {@link GroupSortedList#getGroupVisualPosition(int)}
     * @param position
     * @return
     */
    public int getGroupVisualPosition(int position) {
        return mGroupSortedList.getGroupVisualPosition(position);
    }

    /**
     * See {@link GroupSortedList#isVisualGroupPosition(int)}
     * @param position
     * @return
     */
    public boolean isPositionGroupHeader(int position) {
        return mGroupSortedList.isVisualGroupPosition(position);
    }

    // endregion

    // region Getter & Setters

    public boolean isDisplayEmptyCell() {
        return mIsDisplayEmptyCell;
    }

    public void setDisplayEmptyCell(boolean isDisplayEmptyCell) {
        mGroupSortedList.setDisplayEmptyCell(isDisplayEmptyCell);
        this.mIsDisplayEmptyCell = isDisplayEmptyCell;
    }

    /**
     * See {@link GroupSortedList#setExpandedByDefault(boolean)}
     * @param isExpandedByDefault
     */
    public void setExpandedByDefault(boolean isExpandedByDefault) {
        mGroupSortedList.setExpandedByDefault(isExpandedByDefault);
    }

    /**
     * See {@link GroupSortedList#isExpandedByDefault()}
     * @return
     */
    public boolean isExpandedByDefault() {
        return mGroupSortedList.isExpandedByDefault();
    }

    public boolean isChildrenAboveGroup() {
        return mIsChildrenAboveGroup;
    }

    /**
     * See {@link GroupSortedList#setChildrenAboveGroup(boolean)}
     * @param isChildrenAboveGroup
     */
    public void setChildrenAboveGroup(boolean isChildrenAboveGroup) {
        mGroupSortedList.setChildrenAboveGroup(isChildrenAboveGroup);
        this.mIsChildrenAboveGroup = isChildrenAboveGroup;
    }

    /**
     * A generic way to handle when a header is clicked
     * @return
     */
    public ViewHolderHeaderClicked<GROUP> getViewHolderHeaderClicked() {
        return mViewHolderHeaderClicked;
    }

    public void setViewHolderHeaderClicked(ViewHolderHeaderClicked<GROUP> viewHolderHeaderClicked) {
        this.mViewHolderHeaderClicked = viewHolderHeaderClicked;
    }

    // endregion

    public void cancel() {}

}

