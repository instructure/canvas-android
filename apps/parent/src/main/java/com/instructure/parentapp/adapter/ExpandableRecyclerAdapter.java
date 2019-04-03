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
package com.instructure.parentapp.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import com.instructure.pandarecycler.BaseExpandableRecyclerAdapter;

public abstract class ExpandableRecyclerAdapter<GROUP, ITEM, VIEWHOLDER extends RecyclerView.ViewHolder> extends BaseExpandableRecyclerAdapter<GROUP, ITEM, VIEWHOLDER> {

    public ExpandableRecyclerAdapter(Context context, Class groupKlazz, Class itemKlazz) {
        super(context, groupKlazz, itemKlazz);
    }

    //TODO:
    //FIXME:
    //IMBROKE

//    @Override
//    public boolean shouldIgnoreCache() {
//        return isRefresh();
//    }
//
//    @Override
//    public void onCallbackStarted() { }
//
//    @Override
//    public void onCallbackFinished(CanvasCallback.SOURCE source) {
//        setLoadedFirstPage(true);
//        shouldShowLoadingFooter();
//        AdapterToRecyclerViewCallback adapterToRecyclerViewCallback = getAdapterToRecyclerViewCallback();
//        if(adapterToRecyclerViewCallback != null){
//            adapterToRecyclerViewCallback.setDisplayNoConnection(false);
//            getAdapterToRecyclerViewCallback().setIsEmpty(isAllPagesLoaded() && size() == 0);
//        }
//    }
//
//    @Override
//    public void onNoNetwork() {
//        AdapterToRecyclerViewCallback adapterToRecyclerViewCallback = getAdapterToRecyclerViewCallback();
//        if(adapterToRecyclerViewCallback != null){
//            int size = size();
//            adapterToRecyclerViewCallback.setDisplayNoConnection(size == 0);
//            adapterToRecyclerViewCallback.setIsEmpty(size == 0);
//        }
//    }
}
