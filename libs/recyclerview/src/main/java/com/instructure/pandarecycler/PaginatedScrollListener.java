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

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;


public class PaginatedScrollListener extends RecyclerView.OnScrollListener {

    private int mThreshold = 15;
    private int mPreviousTotal = 0;
    private int mVisibleItemCount;
    private int mTotalItemCount;
    private int mFirstVisibleItem;
    private boolean mIsLoading;
    private PaginatedScrollCallback mPaginatedScrollCallback;

    public interface PaginatedScrollCallback {
        void loadData();
    }

    public PaginatedScrollListener(PaginatedScrollCallback paginatedScrollCallback) {
        mPaginatedScrollCallback = paginatedScrollCallback;
    }

    public PaginatedScrollListener(PaginatedScrollCallback paginatedScrollCallback, int threshold) {
        mPaginatedScrollCallback = paginatedScrollCallback;
        mThreshold = threshold;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        mVisibleItemCount = recyclerView.getChildCount();
        mTotalItemCount = recyclerView.getLayoutManager().getItemCount();
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) { // TODO staggered grid won't work here
            mFirstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        }

        if (mIsLoading) {
            if (mTotalItemCount > mPreviousTotal) {
                mIsLoading = false;
                mPreviousTotal = mTotalItemCount;
            }
        }

        if (!mIsLoading && (mTotalItemCount - mVisibleItemCount)
                <= (mFirstVisibleItem + mThreshold)) {
            if (mFirstVisibleItem != RecyclerView.NO_POSITION) {
                mPaginatedScrollCallback.loadData();
                Log.v("scroll", "end called");
                mIsLoading = true;
            }
        }
    }

    public void resetScroll() {
        mPreviousTotal = 0;
        mVisibleItemCount = 0;
        mTotalItemCount = 0;
        mFirstVisibleItem = 0;
    }
}
