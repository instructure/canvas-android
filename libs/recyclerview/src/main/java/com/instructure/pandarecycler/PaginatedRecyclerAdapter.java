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

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.pandarecycler.interfaces.PaginatedLoadingFooterRecyclerAdapterInterface;
import com.instructure.pandarecycler.interfaces.PaginatedRecyclerAdapterInterface;

import instructure.com.pandarecycler.R;

public abstract class PaginatedRecyclerAdapter<VIEWHOLDER extends RecyclerView.ViewHolder> extends BaseRecyclerAdapter<VIEWHOLDER> implements PaginatedRecyclerAdapterInterface, PaginatedLoadingFooterRecyclerAdapterInterface<VIEWHOLDER> {
    public static final int LOADING_FOOTER_TYPE = 1222233; // Loading footer appears as a spinner at the bottom

    private boolean mIsLoadedFirstPage = false;
    private boolean mShouldLoadNextPage = false; // If the user scrolls to the end of the first page really fast, mNextUrl is null. When true mNextUrl will be loaded when its set
    private boolean mIsAllPagesLoaded = false;
    private boolean mIsRefresh = false;
    private String mNextUrl;

    public PaginatedRecyclerAdapter(Context context) {
        super(context);
        setupCallbacks();
    }

    @Override
    public void loadData() {
        if (!isLoadedFirstPage()) {
            loadFirstPage();
        } else if (mNextUrl != null) {
            loadNextPage(mNextUrl);
            mNextUrl = null;
        } else {
            mShouldLoadNextPage = true; // The previous page has not loaded yet, when it does, setNextUrl will load the next page
        }
    }

    // region Footer
    @Override
    public void onBindViewHolder(VIEWHOLDER baseHolder, int position) {
        if (isLoadingFooterPosition(position)) {
            onBindLoadingFooterViewHolder(baseHolder, position);
        }
    }

    @Override
    public VIEWHOLDER onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LOADING_FOOTER_TYPE) {
            return onCreateLoadingFooterViewHolder(parent);
        } else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public int getItemCount() {
        return shouldShowLoadingFooter() ? size() + 1 : size(); // + 1 for the loading footer
    }

    @Override
    public VIEWHOLDER onCreateLoadingFooterViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewholder_loading, parent, false);
        return (VIEWHOLDER) new RecyclerView.ViewHolder(view) {};
    }

    @Override
    public void onBindLoadingFooterViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingFooterPosition(position)) {
            return LOADING_FOOTER_TYPE;
        } else {
            return super.getItemViewType(position);
        }
    }

    /**
     * Override for custom logic of showing the loading footer
     * @return
     */
    public boolean shouldShowLoadingFooter() {
        //show the loader after first page is loaded to avoid first page insertion animation
        return (!isAllPagesLoaded() && size() > 0) && isPaginated();
    }

    public boolean isLoadingFooterPosition(int position) {
        //List size is offset by one therefore last position = size
        return size() == position && isPaginated();
    }
    // endregion

    // region Pagination

    @Override
    public void loadFirstPage() {
        if (isPaginated()) {
            throw new UnsupportedOperationException("Method must be overridden since isPaginated() is true");
        }
    }

    @Override
    public void loadNextPage(String nextURL) {
        if (isPaginated()) {
            throw new UnsupportedOperationException("Method must be overridden since isPaginated() is true");
        }
    }

    @Override
    public void setupCallbacks() {

    }

    /**
     *
     * The nextUrl to load
     *
     * Set nextUrl to null when all pages are loaded.
     * @param nextUrl
     */
    public void setNextUrl(String nextUrl) {
        this.mNextUrl = nextUrl;
        mIsLoadedFirstPage = true;
        if (nextUrl != null && mShouldLoadNextPage) {
            loadNextPage(nextUrl);
            mShouldLoadNextPage = false; // The previous page has not loaded yet, when it does, setNextUrl will load the next page
        } else if (nextUrl == null) {
            mIsAllPagesLoaded = true;
            mIsRefresh = false;
            if(!shouldShowLoadingFooter()) {
                notifyItemRemoved(size());
            }
        }
    }

    @Override
    public void refresh(){
        super.refresh();
        resetData();
        mIsRefresh = true;
        loadData();
    }

    @Override
    public void resetData() {
        clear();
        mNextUrl = null;
        resetBooleans();
    }
    // endregion

    protected void resetBooleans(){
        mIsLoadedFirstPage = false;
        mIsAllPagesLoaded = false;
        mShouldLoadNextPage = false;
    }

    // region Getter & Setters
    public boolean isLoadedFirstPage() {
        return mIsLoadedFirstPage;
    }

    public void setLoadedFirstPage(boolean isLoadedFirstPage) {
        this.mIsLoadedFirstPage = isLoadedFirstPage;
    }

    public boolean isAllPagesLoaded() {
        return mIsAllPagesLoaded;
    }

    public void setAllPagesLoaded(boolean isAllPagesLoaded) {
        this.mIsAllPagesLoaded = isAllPagesLoaded;
        this.mIsRefresh = false;
    }

    public boolean isRefresh() {
        return mIsRefresh;
    }

    public void setRefresh(boolean isRefresh) {
        this.mIsRefresh = isRefresh;
    }

    // endregion
}
