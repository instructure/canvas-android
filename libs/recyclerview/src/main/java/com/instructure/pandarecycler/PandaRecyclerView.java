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
import android.util.AttributeSet;
import android.view.View;

import com.instructure.pandarecycler.interfaces.EmptyViewInterface;

public class PandaRecyclerView extends RecyclerView {
    private EmptyViewInterface mEmptyView;
    private boolean mIsEmpty = false;
    private boolean mIsSelectionEnabled = false;
    private BaseRecyclerAdapter mBaseRecyclerAdapter;
    private boolean mIsRefresh = false; // When swipe to refresh, keeps the empty view from showing


    // region Callback & Listeners
    final private BaseRecyclerAdapter.AdapterToRecyclerViewCallback mAdapterToRecyclerViewCallback = new BaseRecyclerAdapter.AdapterToRecyclerViewCallback() {
        @Override
        public void setIsEmpty(boolean flag) {
            mIsEmpty = flag;
            mIsRefresh = false;
            checkIfEmpty();
        }

        @Override
        public void setDisplayNoConnection(boolean isNoConnection) {
            mEmptyView.setDisplayNoConnection(isNoConnection);
            setIsEmpty(isNoConnection);
        }

        @Override
        public void refresh() {
            mIsRefresh = true;
            reset();
        }
    };

    final private PaginatedScrollListener mPaginatedScrollListener = new PaginatedScrollListener(new PaginatedScrollListener.PaginatedScrollCallback() {
        @Override
        public void loadData() {
            mBaseRecyclerAdapter.loadData();
        }
    });

    /**
     * The Observer is located in the Recyclerview, because checkIfEmpty sets the visibility
     */
    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mIsRefresh = false;
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    // endregion

    public PandaRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public PandaRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PandaRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        mBaseRecyclerAdapter = (BaseRecyclerAdapter) adapter;
        if (adapter != null) {
            reset();
            adapter.registerAdapterDataObserver(observer);
            mBaseRecyclerAdapter.setAdapterToRecyclerViewCallback(mAdapterToRecyclerViewCallback);

            if (mBaseRecyclerAdapter.isPaginated()) {
                addOnScrollListener(mPaginatedScrollListener);
            }
        }

        checkIfEmpty();
    }


    private void reset() {
        if (mBaseRecyclerAdapter.isPaginated()) {
            mPaginatedScrollListener.resetScroll();
        }
    }

    public void setEmptyView(EmptyViewInterface emptyView) {
        this.mEmptyView = emptyView;
        checkIfEmpty();
    }

    private void init(Context context) {
        addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (mIsSelectionEnabled) {
                            ((BaseRecyclerAdapter) getAdapter()).setSelectedPosition(position);
                        }
                    }
                })
        );
    }

    private void checkIfEmpty() {
        if (mEmptyView != null && getAdapter() != null) {
            if (mBaseRecyclerAdapter.size() == 0 && !mIsRefresh) {
                this.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                if (mIsEmpty) {
                    mEmptyView.setListEmpty();
                } else {
                    mEmptyView.setLoading();
                }
            } else {
                this.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
            }
        }
    }

    // region Getter & Setters


    public boolean isSelectionEnabled() {
        return mIsSelectionEnabled;
    }

    public void setSelectionEnabled(boolean isEnabled) {
        this.mIsSelectionEnabled = isEnabled;
    }
    // endregion

}
