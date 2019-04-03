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

package com.instructure.pandarecycler.util;

import androidx.recyclerview.widget.SortedList;

import java.util.List;

public class UpdatableSortedList<MODEL> extends SortedList<MODEL> {
    private ItemCallback<MODEL> mItemCallback;

    public static int NOT_IN_LIST = -2;
    public static int ITEM_UPDATED = -3;


    public interface ItemCallback<MODEL> {
        long getId(MODEL item);
    }

    public UpdatableSortedList(Class<MODEL> klass, Callback<MODEL> callback, ItemCallback<MODEL> itemCallback) {
        super(klass, callback);
        mItemCallback = itemCallback;
    }

    public UpdatableSortedList(Class<MODEL> klass, Callback<MODEL> callback, ItemCallback<MODEL> itemCallback, int defaultSize) {
        super(klass, callback, defaultSize);
        mItemCallback = itemCallback;
    }

    private long getItemId(MODEL item) {
        return mItemCallback.getId(item);
    }

    public int addOrUpdate(MODEL item) {
        int position = indexOfItemById(getItemId(item));
        if (position != NOT_IN_LIST) {
            this.updateItemAt(position, item);
            return ITEM_UPDATED;
        } else {
            position = this.add(item);
        }
        return position;
    }

    public int indexOfItemById(long id) {
        for(int i = 0; i < this.size(); i++){
            MODEL item1 = this.get(i);
            if(getItemId(item1) == id){
                return i;
            }
        }
        return NOT_IN_LIST;
    }
    
    public void addOrUpdate(List<MODEL> items) {
        beginBatchedUpdates();
        for(MODEL item : items) {
            addOrUpdate(item);
        }
        endBatchedUpdates();
    }
}
