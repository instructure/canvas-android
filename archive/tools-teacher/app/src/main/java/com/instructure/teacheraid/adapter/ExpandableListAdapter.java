/*
 * Copyright (C) 2016 - present  Instructure, Inc.
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

package com.instructure.teacheraid.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.instructure.teacheraid.delegate.ExpandableListDelegate;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Generic type G is the type of the model object used to create group views
 * Generic type I is the type of the model object used to create row views
 * These must implement Comparable so the adapter can sort the rows/groups
 */
public class ExpandableListAdapter<G extends Comparable<G>, I extends Comparable<I>> extends BaseExpandableListAdapter implements CanvasAdapter {
    private Set<G> groups;
    private HashMap<G, Set<I>> children;
    private ExpandableListDelegate<G, I> delegate;
    private boolean areItemsSorted;
    private boolean areItemsReversedSorted;

    // these are to help us not have to call toArray all the time when
    // we are getting items.
    private G[] cachedGroups;
    private boolean groupsInvalid;
    private HashMap<G, I[]> cachedItemsForGroup;
    private HashMap<G, Boolean> itemsInvalidForGroup;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////

    private ExpandableListAdapter(ExpandableListDelegate<G, I> delegate, boolean areGroupsSorted, boolean areItemsSorted,  boolean areGroupReversedSorted, boolean areItemsReversedSorted) {
        this.delegate = delegate;
        this.areItemsSorted = areItemsSorted;
        this.areItemsReversedSorted = areItemsReversedSorted;

        if (areGroupsSorted) {
            if(areGroupReversedSorted){
                groups = new TreeSet<G>(Collections.reverseOrder());
            }
            else{
               groups = new TreeSet<G>();
            }
        } else {
            // this will keep the ordering of the set like an array
            groups = new LinkedHashSet<G>();
        }

        children = new HashMap<G, Set<I>>();

        cachedItemsForGroup = new HashMap<G, I[]>(groups.size());
        itemsInvalidForGroup = new HashMap<G, Boolean>();
    }
    ///////////////////////////////////////////////////////////////////////////
    //Public Static Getters
    //////////////////////////////////////////////////////////////////////////

    public static<G, I> ExpandableListAdapter getExpandableListAdapter(ExpandableListDelegate<G, I> delegate,boolean areGroupSorted, boolean isItemSorted, boolean areGroupsReversedSorted, boolean isItemReversedSorted){
        if(isItemSorted){
            if(isItemReversedSorted){
                return getItemReverseSortedExpandableListAdapter(delegate, areGroupSorted, areGroupsReversedSorted);
            }
            else{
                return getItemSortedExpandableListAdapter(delegate, areGroupSorted, areGroupsReversedSorted);
            }

 }
        else{
            return getItemUnsortedExpandableListAdapter(delegate, areGroupSorted, areGroupsReversedSorted);
        }

    }

    public static<G, I> ExpandableListAdapter getExpandableListAdapter(ExpandableListDelegate<G, I> delegate, boolean areGroupSorted, boolean areGroupsReversedSorted, boolean isItemSorted){
        return getExpandableListAdapter(delegate, areGroupSorted, isItemSorted, areGroupsReversedSorted, false);
    }

    public static<G, I> ExpandableListAdapter getItemUnsortedExpandableListAdapter(ExpandableListDelegate<G, I> delegate, boolean areGroupSorted, boolean areGroupsReversedSorted){

        return new ExpandableListAdapter(delegate, areGroupSorted, false, areGroupsReversedSorted, false);
    }

    public static<G, I> ExpandableListAdapter getItemSortedExpandableListAdapter(ExpandableListDelegate<G, I> delegate, boolean areGroupSorted, boolean areGroupsReversedSorted){

        return new ExpandableListAdapter(delegate, areGroupSorted, true, areGroupsReversedSorted, false);
    }

    public static<G, I> ExpandableListAdapter getItemReverseSortedExpandableListAdapter(ExpandableListDelegate<G, I> delegate, boolean areGroupSorted, boolean areGroupsReversedSorted){
        return new ExpandableListAdapter(delegate, areGroupSorted, true, areGroupsReversedSorted, true);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Helpers
    ///////////////////////////////////////////////////////////////////////////


    public I getFirstItem(){
        return (I)getChild(0,0);
    }   
    
public void addItem(I item, G groupItem) {
        itemsInvalidForGroup.put(groupItem, true);
        addGroup(groupItem);

        Set items = children.get(groupItem);
        boolean added = items.add(item);
        // replace it with the new one
        if (!added) {
            items.remove(item);
            items.add(item);
        }

        notifyDataSetChanged();
    }

    public void addGroup(G groupItem) {
        boolean didAdd = groups.add(groupItem);
        if (didAdd) {
            groupsInvalid = true;
        }

        if (!children.containsKey(groupItem)) {
            if (areItemsSorted) {
                if(areItemsReversedSorted){
                    children.put(groupItem, new TreeSet<I>(Collections.reverseOrder()));
                }
                else{
                    children.put(groupItem, new TreeSet<I>());
                }
            } else {
                // this will keep the ordering of the set like an array
                children.put(groupItem, new LinkedHashSet<I>());
            }
        }

        notifyDataSetChanged();
    }

    public void removeItem(I item) {
        for (G groupItem : getGroupArray()) {
            boolean didRemove = children.get(groupItem).remove(item);

            if (didRemove) {
                itemsInvalidForGroup.put(groupItem, true);
                if (children.get(groupItem).size() == 0) {
                    removeGroup(groupItem);
                }
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void removeGroup(G groupItem) {
        groupsInvalid = true;
        groups.remove(groupItem);
        children.remove(groupItem);
        itemsInvalidForGroup.remove(groupItem);

        notifyDataSetChanged();
    }

    public void clearItems() {
        for (G groupItem : groups) {
            clearItemsForGroup(groupItem);
        }
    }

    public void clearItemsForGroup(G groupItem) {
        itemsInvalidForGroup.remove(groupItem);
        Set<I> toClear = children.get(groupItem);
        if(toClear != null) {
            toClear.clear();
        }

        notifyDataSetChanged();
    }

    public void removeEmptyGroups() {
        for (G groupItem : getGroupArray()) {
            if (children.get(groupItem).size() == 0) {
                removeGroup(groupItem);
            }
        }
    }

    public int getIndexOfGroup(G groupItem) {
        G[] groupArray = getGroupArray();
        for (int i = 0; i < groupArray.length; i++) {
            if (groupArray[i].compareTo(groupItem) == 0) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void clear() {
        cachedGroups = null;
        groupsInvalid = true;
        cachedItemsForGroup.clear();
        itemsInvalidForGroup.clear();

        groups.clear();
        children.clear();
        notifyDataSetChanged();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Helpers
    ///////////////////////////////////////////////////////////////////////////

    public G[] getGroupArray() {
        if (groupsInvalid || cachedGroups == null) {
            groupsInvalid = false;
            cachedGroups = (G[]) groups.toArray(new Comparable[groups.size()]);
        }
        return cachedGroups;
    }

    public G groupForPosition(int position) {
        if (position < groups.size()) {
            return getGroupArray()[position];
        }
        return null;
    }

    public I[] childrenForGroupIndex(int groupIndex){
        G group = (G)getGroup(groupIndex);
        return childrenForGroupItem(group);
    }

    public I[] childrenForGroupItem(G groupItem){
        if (itemsInvalidForGroup.get(groupItem) || !cachedItemsForGroup.containsKey(groupItem)) {
            itemsInvalidForGroup.put(groupItem, false);
            cachedItemsForGroup.put(groupItem, children.get(groupItem).toArray((I[]) new Comparable[children.get(groupItem).size()]));
        }
        return cachedItemsForGroup.get(groupItem);
    }


    public I childForPosition(int groupPosition, int position) {
        G groupItem = groupForPosition(groupPosition);
        if (position < children.get(groupItem).size()) {
           return childrenForGroupItem(groupItem)[position];
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        G groupItem = groupForPosition(groupPosition);
        return children.get(groupItem).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupForPosition(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childForPosition(groupPosition, childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        I childItem = childForPosition(groupPosition, childPosition);
        return delegate.getChildType(childItem);
    }

    @Override
    public int getChildTypeCount() {
        return delegate.getChildCount();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(getGroupCount() == 0) {
            return convertView;
        }

        G groupItem = groupForPosition(groupPosition);
        return delegate.getGroupViewForItem(groupItem, convertView, groupPosition, isExpanded);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(children.size() == 0) {
            return convertView;
        }

        G groupItem = groupForPosition(groupPosition);
        I item = childForPosition(groupPosition, childPosition);
        boolean isLastRow = children.size() == groupPosition + 1 && children.get(groupItem).size() == childPosition + 1;

        return delegate.getRowViewForItem(item, convertView, groupPosition, childPosition, isLastChild, isLastRow);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
