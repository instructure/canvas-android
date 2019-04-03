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


public abstract class BaseRecyclerAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    public abstract T createViewHolder(View v, int viewType);
    public abstract int itemLayoutResId(int viewType);
    public abstract void loadData();
    public abstract int size();
    public abstract void clear();
    public abstract void setSelectedItemId(long itemId);
    public void contextReady(){}

    protected Context mContext;
    protected int mSelectedPosition = -1;

    public interface AdapterToRecyclerViewCallback {
        void setIsEmpty(boolean flag);
        void setDisplayNoConnection(boolean isNoConnection);
        void refresh();
    }

    private AdapterToRecyclerViewCallback mAdapterToRecyclerViewCallback;

    public BaseRecyclerAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        this.mContext = parent.getContext();
        contextReady();
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResId(viewType), parent, false);
        return createViewHolder(v, viewType);
    }

    public boolean isPaginated() {
        return false;
    }

    public void refresh() {
        mAdapterToRecyclerViewCallback.refresh();
    }

    public void cancel() {}

    // region selectedPosition
    public void setSelectedPosition(int position){
        mSelectedPosition = position;
    }

    /**
     * Depends on PandaRecyclerView's onTouchListener
     * @return
     */
    public int getSelectedPosition(){
        return mSelectedPosition;
    }

    // endregion

    // region Getter & Setter

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public void setAdapterToRecyclerViewCallback(AdapterToRecyclerViewCallback adapterToRecyclerViewCallback){
        this.mAdapterToRecyclerViewCallback = adapterToRecyclerViewCallback;
    }

    public AdapterToRecyclerViewCallback getAdapterToRecyclerViewCallback(){
        return mAdapterToRecyclerViewCallback;
    }

    // endregion

}
