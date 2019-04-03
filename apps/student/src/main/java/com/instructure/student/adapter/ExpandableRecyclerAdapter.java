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

package com.instructure.student.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.pandarecycler.BaseExpandableRecyclerAdapter;

public abstract class ExpandableRecyclerAdapter<GROUP, ITEM, VIEWHOLDER extends RecyclerView.ViewHolder> extends BaseExpandableRecyclerAdapter<GROUP, ITEM, VIEWHOLDER> {

    public ExpandableRecyclerAdapter(Context context, Class groupKlazz, Class itemKlazz) {
        super(context, groupKlazz, itemKlazz);
    }

    public void onCallbackFinished(ApiType type) {
        setLoadedFirstPage(true);
        boolean shouldShowFooter = shouldShowLoadingFooter();
        //if we're supposed to show the loading footer but there are is a collapsed group we want to keep loading the data.
        //If we don't we may have a spinning progress bar there but it won't be actively trying to load anything. So a user
        //might think that we're trying to load the next page of data to load more groups, but because the pagination scroll
        //listener is never triggered it will never show up.
        if(shouldShowFooter && existsCollapsedGroup()) {
            loadData();
        }
        AdapterToRecyclerViewCallback adapterToRecyclerViewCallback = getAdapterToRecyclerViewCallback();
        if(adapterToRecyclerViewCallback != null){
            adapterToRecyclerViewCallback.setDisplayNoConnection(false);
            getAdapterToRecyclerViewCallback().setIsEmpty(isAllPagesLoaded() && size() == 0);
        }
    }

    public void onNoNetwork() {
        AdapterToRecyclerViewCallback adapterToRecyclerViewCallback = getAdapterToRecyclerViewCallback();
        if(adapterToRecyclerViewCallback != null){
            int size = size();
            adapterToRecyclerViewCallback.setDisplayNoConnection(size == 0);
            adapterToRecyclerViewCallback.setIsEmpty(size == 0);
        }
    }

    private boolean existsCollapsedGroup() {
        for(GROUP group : getGroups()) {
            if(!isGroupExpanded(group)) {
                return true;
            }
        }
        return false;
    }
}
