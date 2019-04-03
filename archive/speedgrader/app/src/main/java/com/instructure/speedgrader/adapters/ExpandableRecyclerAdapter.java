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

package com.instructure.speedgrader.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.instructure.canvasapi.utilities.APICacheStatusDelegate;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.pandarecycler.BaseExpandableRecyclerAdapter;

public abstract class ExpandableRecyclerAdapter<GROUP, ITEM, VIEWHOLDER extends RecyclerView.ViewHolder> extends BaseExpandableRecyclerAdapter<GROUP, ITEM, VIEWHOLDER> implements APIStatusDelegate, APICacheStatusDelegate {

    public ExpandableRecyclerAdapter(Context context, Class groupKlazz, Class itemKlazz) {
        super(context, groupKlazz, itemKlazz);
    }

    @Override
    public boolean shouldIgnoreCache() {
        return isRefresh();
    }

    @Override
    public void onCallbackStarted() { }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        setLoadedFirstPage(true);
        shouldShowLoadingFooter();
        AdapterToRecyclerViewCallback adapterToRecyclerViewCallback = getAdapterToRecyclerViewCallback();
        if(adapterToRecyclerViewCallback != null){
            adapterToRecyclerViewCallback.setDisplayNoConnection(false);
            getAdapterToRecyclerViewCallback().setIsEmpty(isAllPagesLoaded() && size() == 0);
        }
    }

    @Override
    public void onNoNetwork() {
        AdapterToRecyclerViewCallback adapterToRecyclerViewCallback = getAdapterToRecyclerViewCallback();
        if(adapterToRecyclerViewCallback != null){
            int size = size();
            adapterToRecyclerViewCallback.setDisplayNoConnection(size == 0);
            adapterToRecyclerViewCallback.setIsEmpty(size == 0);
        }
    }
}
