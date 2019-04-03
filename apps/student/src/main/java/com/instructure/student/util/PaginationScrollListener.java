/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.util;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class PaginationScrollListener implements OnScrollListener {

    private int previousTotal = 0;
    private boolean loading = true;

    private static int itemsLeftBeforeNextLoad = 15;
    private GetMoreRequest moreRequestCallback;

    public interface GetMoreRequest {
        public void onMoreRequested();
    }

    public PaginationScrollListener(GetMoreRequest callback) {
        this.moreRequestCallback = callback;
    }

    public PaginationScrollListener(GetMoreRequest callback, int itemsLeftBeforeNextLoad) {
        this.moreRequestCallback = callback;
        this.itemsLeftBeforeNextLoad = itemsLeftBeforeNextLoad;
    }

    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (loading) {
			if (totalItemCount > previousTotal) {
				loading = false;
				previousTotal = totalItemCount;
			}
		}
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + itemsLeftBeforeNextLoad)) {
			// Load the next page.

            //Check if the activity is null. Maybe the fragment was detached.
            if(moreRequestCallback != null){
                moreRequestCallback.onMoreRequested();
            }
			loading = true;
		}
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}
