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
 *
 */

package com.instructure.androidpolling.app.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.activities.BaseActivity;
import com.instructure.androidpolling.app.adapter.ListAdapter;
import com.instructure.androidpolling.app.delegate.ListDelegate;
import com.instructure.androidpolling.app.util.ListViewHelpers;
import com.instructure.androidpolling.app.util.PaginationScrollListener;

/**
 * Generic type I is the type of the model object used to create row views in adapter
 * It must implement Comparable so the adapter can sort the rows
 */
public abstract class PaginatedListFragment<I extends Comparable<I>>
        extends ParentFragment
        implements ListDelegate<I>, PaginatedListInterface {

    // views
    protected SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private LinearLayout footer;
    private LinearLayout emptyView;

    // pagination
    private boolean hasLoadedFirstPage = false;

    // adapter
    private ListAdapter<I> adapter;

    //Layout inflater
    LayoutInflater layoutInflater;

    // Tablet race condition for callbacks vs onactivitycreated
    private boolean hasActivityBeenCreated = false;
    private boolean hasLoadedFirstItem = false;

    private I selectedItem;
    ///////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////

    public ListView getListView() {
        return listView;
    }

    public SwipeRefreshLayout getPullToRefreshListView() {
        return swipeRefreshLayout;
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

    @Override
    public abstract View getRowViewForItem(I item, View convertView, int position);
    @Override
    public abstract String getTitle();

    public abstract boolean onRowClick(I item, int position);

    // Data methods

    @Override
    public abstract boolean areItemsSorted();
    @Override
    public boolean areItemsReverseSorted(){return false;}
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

    // callback method

    @Override
    public abstract void setupCallbacks();

    ///////////////////////////////////////////////////////////////////////////
    // Interface - Optional Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int getRootLayoutCode() {
        return R.layout.swipe_refresh_listview_layout;
    }


    @Override
    public int getEmptyViewLayoutCode() {
        return R.layout.empty_view;
    }

    @Override
    public void configureViews(View rootView) {}

    @Override
    public int getDividerHeight(){ return 1;}

    @Override
    public int getDividerColor(){ return R.color.canvasRowDivider;}

    public boolean onRowLongClick(I item, int position){
        //Returning true means that we DON'T want a context menu
        return true;
    }

    public int getItemViewType(int position, I item) {
        return 1;
    }

    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public void showFirstItem(I item) {
        onRowClick(item, 0);
    }

    @Override
    public boolean isShowFirstItem() { return false; }

    ///////////////////////////////////////////////////////////////////////////
    // Interface - Adapter Methods
    ///////////////////////////////////////////////////////////////////////////
    // View methods
    private void startFirstItem() {
        if(isShowFirstItem() && hasActivityBeenCreated && hasLoadedFirstPage && !hasLoadedFirstItem){
            hasLoadedFirstItem = true;
            showFirstItem(getFirstItem());
        }
    }


    // Adapter Methods

    public void addItem(I item) {
        adapter.addItem(item);
    }

    public void addAll(I[] items) {
        adapter.addAll(items);
    }

    public void removeItem(I item) {
        adapter.removeItem(item);
    }

    public int getItemCount() {
        return adapter.getCount();
    }

    public I getItem(int position) {
        return (I) adapter.getItem(position);
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }


    public void clearAdapter(){
        adapter.clear();
    }


    ///////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(getRootLayoutCode(), container, false);
        emptyView = (LinearLayout) inflater.inflate(getEmptyViewLayoutCode(), null);

        setupViews(rootView);

        // divider options
        listView.setDivider(this.getResources().getDrawable(getDividerColor()));
        listView.setDividerHeight(getDividerHeight());
        listView.setFooterDividersEnabled(false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Let subclass do more with the view setup
        configureViews(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        listView.setAdapter(adapter);

        hasLoadedFirstPage = false;
        setupListeners();
        setupCallbacks();
        loadData();
        startFirstItem();

    }

    public void onActivityCreatedMemorySafe(Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getTitle());
    }

    @Override
    public void finishLoading() {

        hasLoadedFirstPage = true;
        setOnScrollListener();

        ListViewHelpers.hideFooter(footer);
        // empty view only shows if there are no items
        ListViewHelpers.changeEmptyViewToNoItems(emptyView);

        swipeRefreshLayout.setRefreshing(false);
    }

    private void setupViews(View rootView) {
        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getContext(), R.color.polling_aqua),
                ContextCompat.getColor(getContext(), R.color.polling_green),
                ContextCompat.getColor(getContext(), R.color.polling_purple),
                ContextCompat.getColor(getContext(), R.color.canvaspollingtheme_color)
        );
        ListViewHelpers.setupListView(swipeRefreshLayout);
        listView = (ListView)swipeRefreshLayout.findViewById(R.id.listView);
        //add the empty view to the rootview. you can only add one item to a swipeRefreshLayout, so I wrapped that in a frameLayout so we add the
        //empty view to the expandableListView's parent's parent
        ((ViewGroup)listView.getParent().getParent()).addView(emptyView);
        listView.setEmptyView(emptyView);

        ListViewHelpers.changeEmptyViewToLoading(emptyView);

        adapter = ListAdapter.getListAdapter(this, areItemsSorted(), areItemsReverseSorted());
        listView.setAdapter(adapter);
    }

    private void setOnScrollListener() {
        listView.setOnScrollListener(new PaginationScrollListener((BaseActivity)getActivity()));
    }

    protected void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadData();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // position is off by one because the refreshable list view adds a header.
                // parent.getAdapter() returns an adapter that handles this, but we need to change
                // position when we pass it on
                I item = (I) parent.getAdapter().getItem(position);
                setSelectedItem(item);
                position -= listView.getHeaderViewsCount();
                if (position >= 0 && position < getItemCount()) {
                    onRowClick(item, position);
                }

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // position is off by one because the refreshable list view adds a header.
                // parent.getAdapter() returns an adapter that handles this, but we need to change
                // position when we pass it on
                I item = (I) parent.getAdapter().getItem(position);
                position -= listView.getHeaderViewsCount();
                if (position >= 0 && position < getItemCount()) {
                    return onRowLongClick(item, position);
                }

                return false;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // position is off by one because the refreshable list view adds a header.
                // parent.getAdapter() returns an adapter that handles this, but we need to change
                // position when we pass it on
                I item = (I) parent.getAdapter().getItem(position);
                position -= listView.getHeaderViewsCount();
                if (position >= 0 && position < getItemCount()) {
                    return onRowLongClick(item, position);
                }

                return false;
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


    public void reloadData() {
        // reset everything
        resetData();
        hasLoadedFirstPage = false;
        setupCallbacks();
        setNextURLNull();

        if(adapter != null){
            ListViewHelpers.changeEmptyViewToLoading(emptyView);
            adapter.clear();
        }

        listView.setOnScrollListener(null);

        loadData();
    }

    public I getFirstItem(){
        return (I)adapter.getFirstItem();
    }

    public void setSelectedItem(I selectedItem) {
        this.selectedItem = selectedItem;
    }


}
