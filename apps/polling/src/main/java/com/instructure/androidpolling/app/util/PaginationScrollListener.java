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

package com.instructure.androidpolling.app.util;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.instructure.androidpolling.app.activities.BaseActivity;

public class PaginationScrollListener implements OnScrollListener {

	private int visibleThreshold = 3;
	private int previousTotal = 0;
	private boolean loading = true;
    BaseActivity activity;

	public PaginationScrollListener(BaseActivity a) {
		activity = a;
	}
	public PaginationScrollListener(int visibleThreshold, BaseActivity a) {
		this.visibleThreshold = visibleThreshold;
		activity = a;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (loading) {
			if (totalItemCount > previousTotal) {
				loading = false;
				previousTotal = totalItemCount;
			}
		}
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
			// Load the next page.

            //Check if the activity is null. Maybe the fragment was detached.
            if(activity != null){
                activity.loadData();
            }
			loading = true;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {}
}