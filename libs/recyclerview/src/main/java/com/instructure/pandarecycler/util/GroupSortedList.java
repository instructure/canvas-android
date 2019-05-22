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

package com.instructure.pandarecycler.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.SortedList;


public class GroupSortedList<GROUP, ITEM> {
    private static int GROUP_NOT_FOUND = -2;
    private SortedList<GROUP> mGroupObjects; // Manages the Group objects
    private HashMap<Long, SortedList<ITEM>> mItems = new HashMap<>(); // Maps the items in each group to the id of the group
    private GroupComparatorCallback<GROUP> mGroupComparatorCallback; // Handles changes to the groups, unique group ids, and group types
    private ItemComparatorCallback<GROUP, ITEM> mItemComparatorCallback; // Handles changes to items in each group, unique item ids, and item types
    private VisualArrayCallback mVisualArrayCallback;

    private Class<GROUP> mGroupKlazz;
    private Class<ITEM> mItemKlazz;
    private boolean disallowCollapse = false;
    private boolean mIsExpandedByDefault = false;
    private boolean mIsDisplayEmptyCell;
    private boolean mIsChildrenAboveGroup;

    private HashMap<Long, Boolean> mExpanded = new HashMap<>(); // The expanded state of a group is stored by its ID, so it doesn't matter if it moves positions

    // region Interfaces

    public interface GroupComparatorCallback <GRP> {
        int compare(GRP o1, GRP o2);
        boolean areContentsTheSame(GRP oldGroup, GRP newGroup); // Visual Contents
        boolean areItemsTheSame(GRP group1, GRP group2); // Actual Groups (normally compare the ids)
        long getUniqueGroupId(GRP group); // Must be unique among the groups
        int getGroupType(GRP group);
    }

    public interface ItemComparatorCallback <GRP, ITM> {
        int compare(GRP group, ITM o1, ITM o2);
        boolean areContentsTheSame(ITM oldItem, ITM newItem); // Visual Contents
        boolean areItemsTheSame(ITM item1, ITM item2); // Actual Items (normally compare the ids)
        long getUniqueItemId(ITM item); // Only has to be unique within the group
        int getChildType(GRP group, ITM item);
    }

    public static abstract class VisualArrayCallback {
        abstract public void onInserted(int position, int count);
        abstract public void onRemoved(int position, int count);
        abstract public void onMoved(int fromPosition, int toPosition);
        abstract public void onChanged(int position, int count);
    }

    // endregion

    public GroupSortedList(Class<GROUP> groupKlazz, Class<ITEM> itemKlazz, VisualArrayCallback visualArrayCallback, GroupComparatorCallback<GROUP> groupComparatorCallback, ItemComparatorCallback<GROUP, ITEM> itemComparatorCallback) {
        mGroupKlazz = groupKlazz;
        mItemKlazz = itemKlazz;
        mVisualArrayCallback = visualArrayCallback;
        mGroupObjects = new SortedList<>(mGroupKlazz, mGroupCallback);
        mGroupComparatorCallback = groupComparatorCallback;
        mItemComparatorCallback = itemComparatorCallback;
    }

    // region Adapter methods

    /**
     * Determines whether a visualPosition is a group position
     * @param visualPosition
     * @return
     */
    public boolean isVisualGroupPosition(int visualPosition) {
        int visualGroupPosition = getGroupVisualPosition(visualPosition);
        return visualPosition == visualGroupPosition;
    }

    public boolean isVisualEmptyItemPosition(int visualPosition) {
        // TODO test this
        GROUP group = getGroup(visualPosition);
        return getStoredChildrenCount(group) == 0 && isGroupExpanded(group) && !isVisualGroupPosition(visualPosition) && mIsDisplayEmptyCell;
    }

    /**
     * The visual size of the list
     * @return
     */
    public int size() {
        int totalChildren = 0;
        for (int i = 0; i < getStoredGroupCount(); i++) {
            totalChildren += calculatedChildrenCount(i);
        }
        return totalChildren + getStoredGroupCount();
    }

    /**
     * Returns the number of groups in the list
     * @return
     */
    public int getGroupCount() {
        return mGroupObjects.size();
    }

    /**
     * Returns number of items in a group
     * @param group
     * @return
     */
    public int getGroupItemCount(GROUP group) {
        return getGroupItems(group).size();
    }

    /**
     * Passes a GROUP or ITEM to callbacks to determine type
     * @param position
     * @return
     */
    public int getItemViewType(int position) {
        int groupStoredPosition = getStoredGroupPosition(position);
        int groupIndex = getGroupVisualPositionFromStoredPosition(groupStoredPosition);
        GROUP groupObject = mGroupObjects.get(groupStoredPosition);
        if (position == groupIndex) {
            return mGroupComparatorCallback.getGroupType(groupObject);
        } else {
            ITEM item;
            if (mIsChildrenAboveGroup) {
                int groupItemPosition = Math.abs(groupIndex - calculatedChildrenCount(groupStoredPosition) - position);
                item = mItems.get(getGroupId(groupObject)).get(groupItemPosition);
            } else {
                item = mItems.get(getGroupId(groupObject)).get(position - groupIndex - 1);// -1 for group header
            }
            return mItemComparatorCallback.getChildType(groupObject, item);
        }
    }

    // endregion

    // region expand/collaspe helpers

    /**
     * True if group is expanded; false otherwise
     * @param group
     * @return
     */
    public boolean isGroupExpanded(GROUP group) {
        return isGroupExpanded(getGroupId(group));
    }

    private boolean isGroupExpanded(long groupId) {
        if (!mExpanded.containsKey(groupId)) {
            mExpanded.put(groupId, mIsExpandedByDefault);
        }
        return mExpanded.get(groupId);
    }


    private void setExpanded(long groupId, boolean isExpanded) {
        mExpanded.put(groupId, isExpanded);
    }

    /**
     * Marks the groups matching the provided group IDs as expanded or collapsed.
     * Note that this only updates the underlying map and does notify any callbacks of the change.
     * @param groupIds IDs of the groups to mark as expanded or collapsed.
     * @param isExpanded Whether the groups should be marked as expanded (true) or collapsed (false)
     */
    public void markExpanded(Set<Long> groupIds, boolean isExpanded) {
        if (disallowCollapse) return;
        for (Long groupId : groupIds) {
            mExpanded.put(groupId, isExpanded);
        }
    }

    /**
     * Clears the underlying map that tracks which groups are expanded and collapsed.
     * Note that this only updates the underlying map and does notify any callbacks of the change.
     */
    public void clearExpanded() {
        mExpanded.clear();
    }

    /**
     * Expands given group
     * @param group
     */
    public void expandGroup(GROUP group) {
        expandGroup(group, false);
    }

    /**
     * Expands the group
     * @param group
     * @param isNotifyGroupChange when true calls notify changed on the group's view holder
     */
    public void expandGroup(GROUP group, boolean isNotifyGroupChange) {
        if (!isGroupExpanded(group)) {
            // add 1 to offset from where the header is located
            int groupPosition = storedGroupPosition(getGroupId(group));
            if (groupPosition == GROUP_NOT_FOUND) { return; }

            setExpanded(getGroupId(group), true);
            int visualGroupPosition = getGroupVisualPositionFromStoredPosition(groupPosition);
            if (isNotifyGroupChange) {
                mVisualArrayCallback.onChanged(visualGroupPosition, 1);
            }
            if (calculatedChildrenCount(groupPosition) > 0) {
                if (mIsChildrenAboveGroup) {
                    visualGroupPosition -= calculatedChildrenCount(groupPosition);
                } else {
                    visualGroupPosition += 1;
                }
                mVisualArrayCallback.onInserted(visualGroupPosition, calculatedChildrenCount(groupPosition));
            }
        }
    }

    /**
     * Collapses all groups
     */
    public void collapseAll() {
        for (int i = 0; i < mGroupObjects.size(); i++) {
            collapseGroup(mGroupObjects.get(i), true);
        }
    }

    /**
     * Expands all groups
     */
    public void expandAll() {
        for (int i = 0; i < mGroupObjects.size(); i++) {
            expandGroup(mGroupObjects.get(i), true);
        }
    }

    public void collapseGroup(GROUP group) {
        collapseGroup(group, false);
    }

    /**
     *
     * @param group
     * @param isNotifyGroupChange when true calls notify changed on the group's view holder
     */
    public void collapseGroup(GROUP group, boolean isNotifyGroupChange) {
        if (isGroupExpanded(group) && !disallowCollapse) {
            // add 1 to offset from where the header is located
            int groupPosition = storedGroupPosition(getGroupId(group));
            if (groupPosition == GROUP_NOT_FOUND) { return; }

            int visualGroupPosition = getGroupVisualPositionFromStoredPosition(groupPosition);
            if (isNotifyGroupChange) {
                mVisualArrayCallback.onChanged(visualGroupPosition, 1);
            }
            if (calculatedChildrenCount(groupPosition) > 0) {
                if (mIsChildrenAboveGroup) {
                    visualGroupPosition -= calculatedChildrenCount(groupPosition);
                } else {
                    visualGroupPosition += 1;
                }
                mVisualArrayCallback.onRemoved(visualGroupPosition, calculatedChildrenCount(groupPosition));
            }
            setExpanded(getGroupId(group), false); // make sure setExpanded occurs after calculatedChildrenCount, so the proper amount is notified to be removed
        }
    }

    /**
     * Expands if collapsed, collapses if expanded.
     * @param group
     */
    public void expandCollapseGroup(GROUP group) {
        if (isGroupExpanded(group)) {
            collapseGroup(group);
        } else {
            expandGroup(group);
        }
    }

    /**
     * Expands if collapsed, collapses if expanded.
     * @param group
     * @param isNotifyGroupChange when true calls notify changed on the group's view holder
     */
    public void expandCollapseGroup(GROUP group, boolean isNotifyGroupChange) {
        if (isGroupExpanded(group)) {
            collapseGroup(group, isNotifyGroupChange);
        } else {
            expandGroup(group, isNotifyGroupChange);
        }
    }
    // endregion

    // region Pseudo Array helpers

    private int getStoredGroupCount() {
        return mGroupObjects != null ? mGroupObjects.size() : 0;
    }

    private int getStoredChildrenCount(int storedGroupPosition) {
        SortedList<ITEM> moduleItems = mItems.get(getGroupObjectId(storedGroupPosition));
        return moduleItems != null ? moduleItems.size() : 0;
    }

    private int getStoredChildrenCount(GROUP group) {
        SortedList<ITEM> moduleItems = mItems.get(getGroupId(group));
        return moduleItems != null ? moduleItems.size() : 0;
    }

    /**
     * Provides the index of where the group is located in the visual array
     * @param visualPosition
     * @return
     */
    public int getGroupVisualPosition(int visualPosition) {
        int groupStoredPosition = getStoredGroupPosition(visualPosition);
        return getGroupVisualPositionFromStoredPosition(groupStoredPosition);
    }

    /**
     * Translates the stored position of a group to the Visual Position
     *
     * A way to think about finding the visual position is counting the group header, then the children in each group.
     *  i.e. Given Group0 = {child0, child1}; Group1 = {child0, child1, child2} (GCCGCCC).
     *
     *      The storedPosition of Group1 is 1.
     *
     *      Running through the loop would be as follows (where the condition is i < storedPosition):
     *          groupVisualPosition = 3; i = 0; storedPosition = 1 [0 < 1 is true]
     *          groupVisualPosition = 3; i = 1; storedPosition = 1 [1 < 1 is false]. Visual group position is found.
     *
     *  It is reversed when children are above (mIsChildrenAboveGroup). Given CCGCCCG
     *
     *      The storedPosition of Group1 is 1.
     *
     *      Running through the loop would be as follows (where the condition is i <= storedPosition):
     *          groupVisualPosition = 3; i = 0; storedPosition = 1 [0 <= 1 is true]
     *          groupVisualPosition = 7; i = 1; storedPosition = 1 [1 <= 1 is true]
     *          groupVisualPosition = 7; i = 2; storedPosition = 1 [2 <= 1 is false]. Visual group position is found.
     * @param storedPosition
     * @return
     */
    private int getGroupVisualPositionFromStoredPosition(int storedPosition) {
        int groupVisualPosition = 0;
        if (mIsChildrenAboveGroup) {
            // Notice: This for loop has a '<=' which is different from the for loop in the else condition
            for (int i = 0; i <= storedPosition; i++) {
                if (i == storedPosition) { // Edge case, group is first in array
                    groupVisualPosition += calculatedChildrenCount(i);
                    break;
                }
                groupVisualPosition += 1; // group header
                groupVisualPosition += calculatedChildrenCount(i);
            }
        } else {
            for (int i = 0; i < storedPosition; i++) {
                groupVisualPosition += 1; // group header
                groupVisualPosition += calculatedChildrenCount(i);
            }
        }
        return groupVisualPosition;
    }

    private int getItemStoredPosition(int visualPosition) {
        int groupVisualPosition = getGroupVisualPosition(visualPosition);
        int itemPosition;
        if (mIsChildrenAboveGroup) {
            int groupStoredPosition = getStoredGroupPosition(visualPosition);
            // When the children are above the group, calculate the position relative to the groupVisualPosition
            itemPosition = Math.abs(groupVisualPosition - calculatedChildrenCount(groupStoredPosition) - visualPosition);
        } else {
            itemPosition = visualPosition - groupVisualPosition - 1; // -1 for group header
        }
        return itemPosition;
    }

    /**
     * When a group is expanded it returns the child count. If the Group has no children AND IsDisplayEmptyCell is true, it'll return 1 when the group is expanded
     *  Otherwise when the group is collapsed it'll return 0.
     *
     *  Helps in maintaining the visual array representation for the recycler adapter
     * @param storedGroupPosition
     * @return
     */
    private int calculatedChildrenCount(int storedGroupPosition) {
        if (storedGroupPosition < mGroupObjects.size() && isGroupExpanded(mGroupObjects.get(storedGroupPosition))) {
            int storedChildrenCount = getStoredChildrenCount(storedGroupPosition);
            if (mIsDisplayEmptyCell) {
                return storedChildrenCount == 0 ? 1 : storedChildrenCount; // Return 1 so that the empty item will be displayed. Used for if a group is empty
            } else {
                return storedChildrenCount;
            }
        } else {
            return 0;
        }
    }

    /**
     * Searches for the group.
     *
     * i.e. Given a data set that had 2 groups first group has 2 children, the second group has 3 children (GCCGCCC).
     *      The second group in the list has a visual position (or index) of 3.
     *      As soon as the search position is greater than the visual position, or 3 in this case, the stored position is known.
     *
     *      Running through the loop would be as follows (where the condition is visualPosition < searchPosition):
     *          searchPosition = 3; i = 0; visualPosition = 3 [3 < 3 is false]
     *          searchPosition = 7; i = 1; visualPosition = 3 [3 < 7 is true]. Stored group position is found
     *
     * @param visualPosition
     * @return
     */
    private int getStoredGroupPosition(int visualPosition) {
        int searchPosition = 0;
        for (int i = 0; i < getStoredGroupCount(); i++) {
            searchPosition += 1; // group header
            searchPosition += calculatedChildrenCount(i);
            if (visualPosition < searchPosition) {
                return i;
            }
        }
        return 0; // 0 by default
    }

    // endregion

    // region model helpers

    // NOTE: Items are stored by their group ids NOT the stored group position

    public long getGroupId(GROUP group) {
        return mGroupComparatorCallback.getUniqueGroupId(group);
    }

    public long getItemId(ITEM item) {
        return mItemComparatorCallback.getUniqueItemId(item);
    }

    /**
     * Add or updates the group. If updated onChange is called in the visualArrayCallback, if added onInserted is called.
     * @param group
     * @return
     */
    public int addOrUpdateGroup(GROUP group) {
        int position = storedGroupPosition(getGroupId(group));
        if (position != GROUP_NOT_FOUND) { // TODO this can be better (if same group object, sorted list assumes object has changed)
            mGroupObjects.updateItemAt(position, group);
        } else {
            position = mGroupObjects.add(group);
        }
        return position;
    }

    /**
     * Removes a group
     * @param group
     * @return
     */
    public boolean removeGroup(GROUP group) {
        SortedList<ITEM> items = mItems.get(getGroupId(group));
        removeItems(items);
        return mGroupObjects.remove(group);
    }

    /**
     * Get the group based on the visual position
     * @param visualPosition
     * @return
     */
    public GROUP getGroup(int visualPosition) {
        int groupNumber = getStoredGroupPosition(visualPosition);
        return mGroupObjects.get(groupNumber);
    }

    /**
     * @param groupId the group ID to look for
     * @return the visible position in the adapter, or -1 if not found
     */
    public int getGroupPosition(long groupId) {
        int expandedItems = 0;
        for (int i = 0; i < mGroupObjects.size(); i++) {
            if (getGroupId(mGroupObjects.get(i)) == groupId) {
                return i + expandedItems;
            } else {
                expandedItems += calculatedChildrenCount(i);
            }
        }
        return -1;
    }

    /**
     * Gets the group based on id.
     *
     * Returns {@link #GROUP_NOT_FOUND} if group doesn't exist
     * @param id
     * @return
     */
    @Nullable
    public GROUP getGroup(long id) {
        int storedGroupPosition = storedGroupPosition(id);
        if (storedGroupPosition == GROUP_NOT_FOUND) { return null; }
        return getGroupFromStoredPosition(storedGroupPosition);
    }

    @Nullable
    private GROUP getGroupFromStoredPosition(int storedGroupPosition) {
        if (mGroupObjects.size() > storedGroupPosition) {
            return mGroupObjects.get(storedGroupPosition);
        } else {
            return null;
        }
    }

    /**
     * Gets all groups as a list
     * @return returns all groups as a list of GROUPs
     */
    @Nullable
    public ArrayList<GROUP> getGroups() {
        if(mGroupObjects == null) return new ArrayList<>();

        ArrayList<GROUP> items = new ArrayList<>(mGroupObjects.size());
        for (int i = 0; i < mGroupObjects.size(); i++) {
            items.add(mGroupObjects.get(i));
        }
        return items;
    }

    /**
     * Add or updates the groups
     * @param groups
     */
    public void addOrUpdateAllGroups(List<GROUP> groups) {
        // TODO batched updates
        for (GROUP groupObject : groups) {
            addOrUpdateGroup(groupObject);
        }
    }

    /**
     * Add all the groups
     * @param groups
     */
    public void addOrUpdateAllGroups(GROUP[] groups) {
        addOrUpdateAllGroups(Arrays.asList(groups));
    }

    private long getGroupObjectId(int groupNumber) {
        long groupId = getGroupId(mGroupObjects.get(groupNumber));
        return groupId;
    }

    /**
     * Remove all items
     */
    public void clearAll(){
        mGroupObjects = new SortedList<>(mGroupKlazz, mGroupCallback);
        mItems = new HashMap<>();
    }

    private void removeItems(SortedList<ITEM> items) {
        if (items == null) { return; }
        while (items.size() > 0) {
            items.remove(items.get(items.size() - 1));
        }
    }

    /**
     * Iterates through all the groups to find the group by its id
     * @param id
     * @return
     */
    private int storedGroupPosition(long id) {
        // TODO Optimize this (it is really slow)
        for (int i =0; i < mGroupObjects.size(); i++) {
            GROUP group = mGroupObjects.get(i);
            if (getGroupId(group) == id) {
                return i;
            }
        }
        return GROUP_NOT_FOUND;
    }

    // ITEMs

    /**
     * Remove the item
     * @param item
     * @return
     */
    public boolean removeItem(ITEM item) {
        return removeItem(item, true);
    }

    /**
     * Remove the item
     * @param item
     * @param removeGroupIfEmpty Removes the group if no items remain in the group upon successful deletion.
     * @return
     */
    public boolean removeItem(ITEM item, boolean removeGroupIfEmpty) {
        ItemPosition itemPosition = storedItemPosition(getItemId(item));

        if (itemPosition == null) {
            return false;
        }

        GROUP group = getGroup(itemPosition.groupId);
        SortedList<ITEM> groupItems = getGroupItems(group);
        boolean isRemoved = groupItems.remove(item);

        // If the item was the last in the group, remove the group too.
        if (removeGroupIfEmpty && groupItems.size() == 0) {
            mGroupObjects.remove(group);
        }
        return isRemoved;
    }

    /**
     * Add the item to the group.
     *
     * If only the group id is known, call {@link #getGroup} to get the group object.
     * @param group
     * @param item
     * @return index of item in group
     */
    public int addOrUpdateItem(GROUP group, ITEM item) {
        if (getGroup(getGroupId(group)) != group) { // assume if same object nothing changed
            addOrUpdateGroup(group);
        }
        ItemPosition itemPosition = storedItemPosition(getItemId(item)); // determine if the item exists

        SortedList<ITEM> groupItems = getGroupItems(group);

        // handles empty cell
        if (groupItems.size() == 0 && isGroupExpanded(group) && mIsDisplayEmptyCell) {
            int storedGroupPosition = mGroupObjects.indexOf(group);
            if (storedGroupPosition != -1) {
                if (mIsChildrenAboveGroup) {
                    // The Empty cell will be above the group, so -1
                    mVisualArrayCallback.onRemoved(getGroupVisualPositionFromStoredPosition(storedGroupPosition) - 1, 1); // remove the empty cell assuming an item is added
                } else {
                    // The Empty cell will be below the group, so +1
                    mVisualArrayCallback.onRemoved(getGroupVisualPositionFromStoredPosition(storedGroupPosition) + 1, 1); // remove the empty cell assuming an item is added
                }
            }
        }

        // Add or update the item
        if (itemPosition != null) {
            if (itemPosition.groupId == getGroupId(group)) {
                groupItems.updateItemAt(itemPosition.itemPosition, item);
            } else { // handle the case where the item has changed groups
                SortedList<ITEM> oldGroupItems = getGroupItems(getGroup(itemPosition.groupId));
                oldGroupItems.removeItemAt(itemPosition.itemPosition);
                return groupItems.add(item);
            }
        } else { // if its not there, just add it
            return groupItems.add(item);
        }
        return itemPosition.itemPosition;
    }

    /**
     * Adds or updates all the items
     * @param group
     * @param items
     */
    public void addOrUpdateAllItems(GROUP group, List<ITEM> items) {
        if(items.size() == 0) {
            return;
        }

        if (getGroup(getGroupId(group)) != group) { // assume if same object nothing changed
            addOrUpdateGroup(group);
        }

        SortedList<ITEM> groupItems = getGroupItems(group);
        groupItems.beginBatchedUpdates();

        // handles empty cell
        if (groupItems.size() == 0 && isGroupExpanded(group) && mIsDisplayEmptyCell) {
            int storedGroupPosition = mGroupObjects.indexOf(group);
            if (storedGroupPosition != -1) {
                if (mIsChildrenAboveGroup) {
                    // The Empty cell will be above the group, so -1
                    mVisualArrayCallback.onRemoved(getGroupVisualPositionFromStoredPosition(storedGroupPosition) - 1, 1); // remove the empty cell assuming an item is added
                } else {
                    // The Empty cell will be below the group, so +1
                    mVisualArrayCallback.onRemoved(getGroupVisualPositionFromStoredPosition(storedGroupPosition) + 1, 1); // remove the empty cell assuming an item is added
                }
            }
        }

        for (ITEM item : items) {
            ItemPosition itemPosition = storedItemPosition(getItemId(item));
            // Add or update the item
            if (itemPosition != null) {
                if (itemPosition.groupId == getGroupId(group)) {
                    groupItems.updateItemAt(itemPosition.itemPosition, item);
                } else { // handle the case where the item has changed groups
                    SortedList<ITEM> oldGroupItems = getGroupItems(getGroup(itemPosition.groupId));
                    oldGroupItems.removeItemAt(itemPosition.itemPosition);
                    groupItems.add(item);
                }
            } else { // if its not there, just add it
                groupItems.add(item);
            }
        }

        groupItems.endBatchedUpdates();
    }

    public void addOrUpdateAllItems(GROUP group, ITEM[] items) {
        addOrUpdateAllItems(group, Arrays.asList(items));
    }

    /**
     * returns where the item is in the pseudo array
     * @param itemId
     * @return -1 if not found, visual position otherwise
     */
    public int getItemVisualPosition(long itemId) {
        if (itemId == -1) { return -1; }
        ItemPosition itemPosition = storedItemPosition(itemId); // Locate the item
        if (itemPosition == null) {
            return -1;
        }
        long groupId = itemPosition.groupId;
        int storedGroupPosition = storedGroupPosition(groupId);
        if (storedGroupPosition == GROUP_NOT_FOUND) { return -1; }
        if (mIsChildrenAboveGroup) {
            return getGroupVisualPositionFromStoredPosition(storedGroupPosition) - calculatedChildrenCount(storedGroupPosition) + itemPosition.itemPosition;
        } else {
            return getGroupVisualPositionFromStoredPosition(storedGroupPosition) + itemPosition.itemPosition + 1;

        }
    }

    /**
     * Get an item based on its visualPostion
     * @param visualPosition
     * @return
     */
    @Nullable
    public ITEM getItem(int visualPosition) {
        // TODO test this
        GROUP group = getGroup(visualPosition);
        int itemPosition = getItemStoredPosition(visualPosition);
        SortedList<ITEM> list = getGroupItems(group);
        return isValidItemIndex(itemPosition, list) ? list.get(itemPosition) : null;
    }

    /**
     * Get an item based on its stored position (the position relative to the list of items only)
     *
     * i.e. Group1 = { item0, item1, item2 }; Group2= { item3, item4, item5 };
     *          getItem(Group2, 1) == item4
     *
     * @param group
     * @param itemStoredPosition
     * @return
     */
    @Nullable
    public ITEM getItem(GROUP group, int itemStoredPosition) {
        SortedList<ITEM> list = getGroupItems(group);
        return isValidItemIndex(itemStoredPosition, list) ? list.get(itemStoredPosition) : null;
    }

    /**
     * Returns all items for a specified group as a List<ITEM>
     * @param group A group of items
     * @return returns a list of ITEMs for a GROUP
     */
    @Nullable
    public ArrayList<ITEM> getItems(GROUP group) {
        if(group == null) return new ArrayList<>();

        SortedList<ITEM> list = getGroupItems(group);
        ArrayList<ITEM> items = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            items.add(list.get(i));
        }
        return items;
    }

    /**
     * If the index is -1 or size() (itemPosition == 5, when size() == 5)
     * Then the position is most likely a header, in that case we shouldn't crash, but just return null
     * @param itemStoredPosition
     * @return
     */
    private boolean isValidItemIndex(int itemStoredPosition, SortedList<ITEM> list) {
        return list.size() > 0 && itemStoredPosition >= 0 && itemStoredPosition < list.size();
    }

    /**
     * Get item index in relation to its group
     * @param group
     * @param item
     * @return -1 if not found
     */
    public int storedIndexOfItem(GROUP group, ITEM item) {
        SortedList<ITEM> list = getGroupItems(group);
        return list.size() > 0 ? list.indexOf(item) : -1;
    }

    /**
     * Search for the item in all of the groups
     * @param itemId
     * @return
     */
    @Nullable
    private ItemPosition storedItemPosition(long itemId) {
        // TODO Optimize this (it is really slow)
        for (Map.Entry<Long, SortedList<ITEM>> entry : mItems.entrySet()) {
            SortedList<ITEM> itemList = entry.getValue();
            for (int i =0; i < itemList.size(); i++) {
                ITEM item = itemList.get(i);
                if (getItemId(item) == itemId) {
                    return new ItemPosition(entry.getKey(), i);
                }
            }
        }
        return null;
    }

    /**
     * Get the items in a group
     * @param group
     * @return
     */
    private SortedList<ITEM> getGroupItems(GROUP group) {
        long groupObjectId = getGroupId(group);
        SortedList<ITEM> groupItems = mItems.get(groupObjectId);
        if (groupItems == null) {
            groupItems = new SortedList<>(mItemKlazz, createCallback(groupObjectId));
            mItems.put(groupObjectId, groupItems);
        }
        return groupItems;
    }

    /**
     * Manages the SortedList that contains the groups.
     */
    private SortedList.Callback<GROUP> mGroupCallback = new SortedList.Callback<GROUP>() {
        @Override
        public int compare(GROUP o1, GROUP o2) {
            return mGroupComparatorCallback.compare(o1, o2);
        }

        @Override
        public void onInserted(int storedGroupPosition, int count) {
            mVisualArrayCallback.onInserted(getGroupVisualPositionFromStoredPosition(storedGroupPosition), count);
        }

        @Override
        public void onRemoved(int storedGroupPosition, int count) {
            mVisualArrayCallback.onRemoved(getGroupVisualPositionFromStoredPosition(storedGroupPosition), count);
        }

        @Override
        public void onMoved(int storedGroupFromPosition, int storedGroupToPosition) {

            // Handles when groups change positions
            //  Collapse both groups, move them, then expand again if expanded.
            int groupNumberFromPosition = getStoredGroupPosition(storedGroupFromPosition);
            GROUP fromGroup = getGroup(groupNumberFromPosition);
            boolean isFromGroupExpanded = isGroupExpanded(fromGroup);
            collapseGroup(fromGroup);

            int groupNumberToPosition = getStoredGroupPosition(storedGroupToPosition);
            GROUP toGroup = getGroup(groupNumberToPosition);
            boolean isToGroupExpanded = isGroupExpanded(toGroup);
            collapseGroup(toGroup);

            mVisualArrayCallback.onMoved(getGroupVisualPositionFromStoredPosition(storedGroupFromPosition), getGroupVisualPositionFromStoredPosition(storedGroupToPosition));
            if (isFromGroupExpanded) {
                expandGroup(fromGroup);
            }
            if (isToGroupExpanded) {
                expandGroup(toGroup);
            }
        }

        @Override
        public void onChanged(int storedGroupPosition, int count) {
            mVisualArrayCallback.onChanged(getGroupVisualPositionFromStoredPosition(storedGroupPosition), count);
        }

        @Override
        public boolean areContentsTheSame(GROUP oldItem, GROUP newItem) {
            return mGroupComparatorCallback.areContentsTheSame(oldItem, newItem);
        }

        @Override
        public boolean areItemsTheSame(GROUP item1, GROUP item2) {
            return mGroupComparatorCallback.areItemsTheSame(item1, item2);
        }
    };

    /**
     * Manages the SortedLists that contains the items in each group.
     */
    public ExpandableCallback<ITEM> createCallback(final long groupObjectId) {
        ExpandableCallback<ITEM> expandableCallback = new ExpandableCallback<ITEM>(groupObjectId) {
            /**
             * Translate the storedGroupPosition and item position to the visual index.
             * @param storedGroupPosition
             * @param position
             * @return
             */
            private int getVisualIndex(int storedGroupPosition, int position) {
                int visualIndex = getGroupVisualPositionFromStoredPosition(storedGroupPosition);
                if (mIsChildrenAboveGroup) {
                    visualIndex = visualIndex - calculatedChildrenCount(storedGroupPosition) + position;
                } else {
                    visualIndex += 1 + position;
                }
                return visualIndex;
            }

            @Override
            public int compare(ITEM o1, ITEM o2) {
                return mItemComparatorCallback.compare(getGroup(getGroupObjectId()), o1, o2);
            }

            @Override
            public void onInserted(int position, int count) {
                long groupId = this.getGroupObjectId();
                if (isGroupExpanded(groupId)) {
                    int storedGroupPosition = storedGroupPosition(groupId);
                    if (storedGroupPosition == GROUP_NOT_FOUND) { return; }
                    mVisualArrayCallback.onInserted(getVisualIndex(storedGroupPosition, position), count);
                }
            }

            @Override
            public void onRemoved(int position, int count) {
                long groupId = this.getGroupObjectId();
                if (isGroupExpanded(groupId)) {
                    int storedGroupPosition = storedGroupPosition(groupId);
                    if (storedGroupPosition == GROUP_NOT_FOUND) { return; }
                    mVisualArrayCallback.onRemoved(getVisualIndex(storedGroupPosition, position), count);
                }
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                long groupId = this.getGroupObjectId();
                if (isGroupExpanded(groupId)) {
                    int storedGroupPosition = storedGroupPosition(groupId);
                    if (storedGroupPosition == GROUP_NOT_FOUND) { return; }
                    mVisualArrayCallback.onMoved(getVisualIndex(storedGroupPosition, fromPosition), getVisualIndex(storedGroupPosition, toPosition));
                }
            }

            @Override
            public void onChanged(int position, int count) {
                long groupId = this.getGroupObjectId();
                if (isGroupExpanded(groupId)) {
                    int storedGroupPosition = storedGroupPosition(groupId);
                    if (storedGroupPosition == GROUP_NOT_FOUND) { return; }
                    mVisualArrayCallback.onChanged(getVisualIndex(storedGroupPosition, position), count);
                }
            }

            @Override
            public boolean areContentsTheSame(ITEM oldItem, ITEM newItem) {
                return mItemComparatorCallback.areContentsTheSame(oldItem, newItem);
            }

            @Override
            public boolean areItemsTheSame(ITEM item1, ITEM item2) {
                return mItemComparatorCallback.areItemsTheSame(item1, item2);
            }
        };
        return expandableCallback;
    }

    public static abstract class ExpandableCallback<T> extends SortedList.Callback<T> {
        private long mGroupObjectId;
        public ExpandableCallback(long groupObjectId) {
            super();
            mGroupObjectId = groupObjectId;
        }

        public long getGroupObjectId() {
            return mGroupObjectId;
        }

        public void setGroupObjectId(long groupObjectId) {
            this.mGroupObjectId = groupObjectId;
        }
    }

    private static class ItemPosition{
        public long groupId;
        public int itemPosition;
        public ItemPosition(long groupId, int itemPos) {
            this.groupId = groupId;
            itemPosition = itemPos;
        }
    }

    // endregion

    // region Getter & Setters


    public boolean isChildrenAboveGroup() {
        return mIsChildrenAboveGroup;
    }

    /**
     * The child item appear above the groups when true, below the group if false
     * @param isChildrenAboveGroup
     */
    public void setChildrenAboveGroup(boolean isChildrenAboveGroup) {
        this.mIsChildrenAboveGroup = isChildrenAboveGroup;
    }

    public boolean isDisplayEmptyCell() {
        return mIsDisplayEmptyCell;
    }

    public void setDisplayEmptyCell(boolean isDisplayEmptyCell) {
        this.mIsDisplayEmptyCell = isDisplayEmptyCell;
    }

    /**
     * Expands the groups by default
     * @return
     */
    public boolean isExpandedByDefault() {
        return mIsExpandedByDefault;
    }

    /**
     * Groups will be expanded by default when true
     * @param isExpandedByDefault
     */
    public void setExpandedByDefault(boolean isExpandedByDefault) {
        this.mIsExpandedByDefault = isExpandedByDefault;
    }

    public boolean getDisallowCollapse() {
        return disallowCollapse;
    }

    public void setDisallowCollapse(boolean disallow) {
        disallowCollapse = disallow;
    }

    public GroupComparatorCallback<GROUP> getGroupComparatorCallback() {
        return mGroupComparatorCallback;
    }

    public void setGroupComparatorCallback(GroupComparatorCallback<GROUP> groupComparatorCallback) {
        this.mGroupComparatorCallback = groupComparatorCallback;
    }

    public ItemComparatorCallback<GROUP, ITEM> getItemComparatorCallback() {
        return mItemComparatorCallback;
    }

    public void setItemComparatorCallback(ItemComparatorCallback<GROUP, ITEM> itemComparatorCallback) {
        this.mItemComparatorCallback = itemComparatorCallback;
    }

// endregion
}
