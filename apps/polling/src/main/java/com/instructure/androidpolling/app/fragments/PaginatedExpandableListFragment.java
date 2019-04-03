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
 *
 */

package com.instructure.androidpolling.app.fragments;

import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.activities.BaseActivity;
import com.instructure.androidpolling.app.adapter.ExpandableListAdapter;
import com.instructure.androidpolling.app.delegate.ExpandableListDelegate;
import com.instructure.androidpolling.app.util.ListViewHelpers;
import com.instructure.androidpolling.app.util.PaginationScrollListener;

import java.util.HashSet;

/**
 * Generic type G is the type of the model object used to create group views
 * Generic type I is the type of the model object used to create row views
 * These must implement Comparable so the adapter can sort the rows/groups
 */
public abstract class PaginatedExpandableListFragment<G extends Comparable<? super G>, I extends Comparable<? super I>>
        extends ParentFragment
        implements ExpandableListDelegate<G, I>, PaginatedListInterface {

    // views
    protected SwipeRefreshLayout swipeRefreshLayout;
    private ExpandableListView expandableListView;
    private LinearLayout footer;
    private LinearLayout emptyView;

    // pagination
    private boolean hasLoadedFirstPage = false;

    // adapter
    private ExpandableListAdapter adapter;

    //Layout inflater
    LayoutInflater layoutInflater;

    // Tablet race condition for callbacks vs onactivitycreated
    private boolean hasActivityBeenCreated = false;
    private boolean hasLoadedFirstItem = false;

    private HashSet<G> disabledGroups = new HashSet<G>();

    private I selectedItem;
    ///////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////

    public ExpandableListView getExpandableListView() {
        return expandableListView;
    }

    public LayoutInflater layoutInflater() {
        if(layoutInflater == null){
            layoutInflater = LayoutInflater.from(getActivity());
        }
        return layoutInflater;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Interface - Required Overrides (most are passed on from PaginatedListInterface)
    ///////////////////////////////////////////////////////////////////////////

    // View methods
    private void startFirstItem() {
        if(isShowFirstItem() && hasActivityBeenCreated && hasLoadedFirstPage && !hasLoadedFirstItem && getFirstItem() != null){
            hasLoadedFirstItem = true;
            showFirstItem(getFirstItem());
        }
    }

    @Override
    public abstract View getRowViewForItem(I item, View convertView, int groupPosition, int childPosition, boolean isLastRowInGroup, boolean isLastRow);
    @Override
    public abstract View getGroupViewForItem(G groupItem, View convertView, int groupPosition, boolean isExpanded);
    @Override
    public abstract String getTitle();

    public abstract boolean onRowClick(I item);

    // Data methods

    @Override
    public abstract boolean areItemsSorted();
    @Override
    public boolean areItemsReverseSorted(){
        return false;
    }

    @Override
    public abstract void loadFirstPage();
    @Override
    public abstract void loadNextPage(String nextURL);
    @Override
    public abstract String getNextURL();
    @Override
    public abstract void setNextURLNull();
    @Override
    public abstract void resetData();

    public abstract boolean areGroupsSorted();

    public boolean areGroupsReverseSorted(){
        return false;
    }

    // callback method

    @Override
    public abstract void setupCallbacks();

    ///////////////////////////////////////////////////////////////////////////
    // Interface - Optional Overrides
    ///////////////////////////////////////////////////////////////////////////

    // NOTE: See PaginatedListFragment for more

    @Override
    public int getDividerHeight(){ return 1;}

    @Override
    public int getDividerColor(){ return R.color.canvasRowDivider;}

    @Override
    public int getRootLayoutCode() {
        return R.layout.swipe_refresh_expandable_layout;
    }

    @Override
    public int getEmptyViewLayoutCode() {
        return R.layout.empty_view;
    }

    @Override
    public void configureViews(View rootView) {}

    protected boolean areGroupsCollapsible() {
        return true;
    }

    protected boolean onGroupClick(G groupItem, int groupPosition) {
        return true;
    }

    protected boolean onRowLongClick(I item){
        //Returning true means that we DON'T want a context menu
        return true;
    }

    protected void onGroupExpand(G groupItem, int groupPosition) {

    }

    protected void onGroupCollapse(G groupItem, int groupPosition) {

    }

    public boolean deleteCache(){
        //Override to delete the cache from your callbacks
        return false;
    }

    @Override
    public void showFirstItem(I item) {
        setSelectedItem(item);
        onRowClick(item);
    }

    @Override
    public boolean isShowFirstItem() {
        return false;
    }

    @Override
    public int getChildType(I item) {
        return 0;
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Interface - View methods
    ///////////////////////////////////////////////////////////////////////////

     public void expandAllGroups() {
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            expandableListView.expandGroup(i);
            onGroupExpand((G) adapter.getGroup(i), i);
        }
    }

    public void expandGroup(G groupItem) {
        Integer groupIndex = adapter.getIndexOfGroup(groupItem);
        if (groupIndex >= 0) {
            expandableListView.expandGroup(groupIndex);
            onGroupExpand(groupItem, groupIndex);
        }
    }

    public void collapseGroup(G groupItem) {
        Integer groupIndex = adapter.getIndexOfGroup(groupItem);
        if (groupIndex >= 0) {
            expandableListView.collapseGroup(groupIndex);
            onGroupCollapse(groupItem, groupIndex);
        }
    }

    public void disableExpanding() {
        swipeRefreshLayout.setVisibility(View.GONE);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Interface - Adapter methods
    ///////////////////////////////////////////////////////////////////////////

    public void addItem(G groupItem, I item) {
        adapter.addItem(item, groupItem);
    }

    public void removeItem(I item) {
        adapter.removeItem(item);
    }

    public void addGroup(G groupItem) {
        adapter.addGroup(groupItem);
    }

    public void addDisabledGroup(G groupItem){
        disabledGroups.add(groupItem);
        notifyDataSetChanged();
    }

    public void clearDisabledGroups(){
        disabledGroups.clear();
    }

    public void removeDisabledGroup(G groupItem){
        disabledGroups.remove(groupItem);
    }

    public int getIndexOfGroup(G group) {
        return adapter.getIndexOfGroup(group);
    }

    public G getGroup(int index) {
        if (index < 0 || index >= adapter.getGroupCount()) {
            return null;
        }

        return (G) adapter.getGroup(index);
    }

    public I childForPosition(int groupIndex, int childIndex) {
       return (I) adapter.childForPosition(groupIndex, childIndex);
    }


    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    public int getGroupCount() {
        return adapter.getGroupCount();
    }

    public int getChildrenCount(int groupPosition) {
        return adapter.getChildrenCount(groupPosition);
    }

    public void clearItems() {
        adapter.clearItems();
    }

    public void removeEmptyGroups() {
        adapter.removeEmptyGroups();
    }

    public I getFirstItem(){
        if(adapter.getGroupCount() != 0 && adapter.getChildrenCount(0) != 0){
            return (I)adapter.getFirstItem();
        }
        return null;
    }
    ///////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(getRootLayoutCode(), container, false);
        //footer = (LinearLayout) inflater.inflate(getFooterLayoutCode(), null);
        emptyView = (LinearLayout) inflater.inflate(getEmptyViewLayoutCode(), null);

        setupViews(rootView);

        // divider options
        expandableListView.setDivider(this.getResources().getDrawable(getDividerColor()));
        expandableListView.setDividerHeight(getDividerHeight());

        // let subclass do more with the view setup
        configureViews(rootView);

        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        expandableListView.setAdapter(adapter);
        hasActivityBeenCreated = true;
        hasLoadedFirstPage = false;
        setupListeners();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getTitle());
        setupCallbacks();
        loadData();
    }

    ///////////////////////////////////////////////////////////////////////////
    // View
    ///////////////////////////////////////////////////////////////////////////


    public void onCallbackFinished() {
        finishLoading();
    }

    @Override
    public void finishLoading() {

        hasLoadedFirstPage = true;
        setOnScrollListener();

        ListViewHelpers.hideFooter(footer);
        // empty view only shows if there are no items
        ListViewHelpers.changeEmptyViewToNoItems(emptyView);

        swipeRefreshLayout.setRefreshing(false);
	    startFirstItem();
    }

    private void setupViews(View rootView) {
        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.polling_aqua,
                R.color.polling_green,
                R.color.polling_purple,
                R.color.canvaspollingtheme_color);
        expandableListView = (ExpandableListView)swipeRefreshLayout.findViewById(R.id.expandableListView);
        expandableListView.setEmptyView(emptyView);
        //add the empty view to the rootview. you can only add one item to a swipeRefreshLayout, so I wrapped that in a frameLayout so we add the
        //empty view to the expandableListView's parent's parent
        ((ViewGroup)expandableListView.getParent().getParent()).addView(emptyView);
        ListViewHelpers.changeEmptyViewToLoading(emptyView);

        if (areGroupsCollapsible()) {
            expandableListView.setGroupIndicator(getResources().getDrawable(R.drawable.expandable_selector));
        }

        adapter = ExpandableListAdapter.getExpandableListAdapter(this, areGroupsSorted(), areItemsSorted(),areGroupsReverseSorted(), areItemsReverseSorted());

        expandableListView.setAdapter(adapter);
    }

    private void setOnScrollListener() {
        expandableListView.setOnScrollListener(new PaginationScrollListener((BaseActivity)getActivity()));
    }

    private void setupListeners() {
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                G group = (G)parent.getExpandableListAdapter().getGroup(groupPosition);

                //If the group has been disabled, we don't want to click it.
                if(disabledGroups.contains(group)){
                    return true;
                }

                I item = (I) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
                setSelectedItem(item);
                boolean result = onRowClick(item);

                return result;
            }
        });

        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

                    long flatId = expandableListView.getExpandableListPosition(position);

                    int groupPosition = ExpandableListView.getPackedPositionGroup(flatId);
                    int childPosition = ExpandableListView.getPackedPositionChild(flatId);

                    I item = (I) expandableListView.getExpandableListAdapter().getChild(groupPosition, childPosition);

                    return onRowLongClick(item);
                }

                //Returning true means that we DON'T want a context menu
                return true;
            }
        });

        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {

                    long flatId = expandableListView.getExpandableListPosition(position);

                    int groupPosition = ExpandableListView.getPackedPositionGroup(flatId);
                    int childPosition = ExpandableListView.getPackedPositionChild(flatId);

                    I item = (I) expandableListView.getExpandableListAdapter().getChild(groupPosition, childPosition);

                    return onRowLongClick(item);
                }

                //Returning true means that we DON'T want a context menu
                return true;
            }
        });

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                G groupItem = (G) parent.getExpandableListAdapter().getGroup(groupPosition);

                if (areGroupsCollapsible()) {
                    if (expandableListView.isGroupExpanded(groupPosition)) {
                        expandableListView.collapseGroup(groupPosition);
                    } else {
                        expandableListView.expandGroup(groupPosition);
                    }
                }

                return PaginatedExpandableListFragment.this.onGroupClick(groupItem, groupPosition);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    expandableListView.setOnScrollListener(null);
                    reloadData();
                }
        });
    }


    public void onNoNetwork() {
        ListViewHelpers.showFooterTapToRetry(footer);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Data
    ///////////////////////////////////////////////////////////////////////////

    public void loadData() {
        //If the Activity is null then the fragment is not attached.
        if(getActivity() == null){return;}

        if (!hasLoadedFirstPage) {
            showProgressUI();
            loadFirstPage();
        } else if (getNextURL() != null) {
            showProgressUI();
            loadNextPage(getNextURL());
        }
    }

    private void showProgressUI() {

    }

    public void reloadData(View v) {
        //we only care about taps when the view says 'Tap to Retry'
        if(ListViewHelpers.footerIsTapToRetry(footer)) {
            reloadData();
        }
    }

    public void reloadData() {
        // reset everything
        resetData();
        clearDisabledGroups();
        hasLoadedFirstPage = false;
        setupCallbacks();
        setNextURLNull();

        if(adapter != null){
            ListViewHelpers.changeEmptyViewToLoading(emptyView);
            adapter.clear();
        }

        expandableListView.setOnScrollListener(null);

        loadData();
    }

    public void clear(){
        adapter.clear();
    }

    public void setSelectedItem(I item) {
        selectedItem = item;
    }

}
