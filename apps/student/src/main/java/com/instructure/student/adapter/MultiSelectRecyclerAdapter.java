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
import com.instructure.canvasapi2.models.CanvasComparable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class MultiSelectRecyclerAdapter<T extends CanvasComparable, VH extends RecyclerView.ViewHolder> extends BaseListRecyclerAdapter<T, VH> {

    protected boolean mIsMultiSelectMode;

    //List of items to be passed into binder, enabling view to display select items
    protected ArrayList<T> mSelectedItems;

    //Used for communication with the fragment containing the ActionMode
    protected MultiSelectCallback mMultiSelectCallback;

    public interface MultiSelectCallback {
        void startMultiSelectMode();
        void endMultiSelectMode();
        void setMultiSelectCount(int selectedCount);
    }

    public MultiSelectRecyclerAdapter(Context context, Class<T> klazz, List items, MultiSelectCallback multiSelectCallback) {
        super(context, klazz, items);
        mMultiSelectCallback = multiSelectCallback;
        mSelectedItems = new ArrayList<>();
    }

    public ArrayList<T> getSelectedItems(){
        return mSelectedItems;
    }

    public boolean isItemSelected(T item){
        return mSelectedItems.contains(item);
    }

    public void setMultiSelectMode(boolean isMultiSelectMode){
        mIsMultiSelectMode = isMultiSelectMode;
        if(isMultiSelectMode){
            mMultiSelectCallback.startMultiSelectMode();
        } else {
            clearAndUpdateItems();
            mMultiSelectCallback.endMultiSelectMode();
        }
    }

    public boolean isMultiSelectMode(){
        return mIsMultiSelectMode;
    }

    protected void toggleSelection(T item) {
        if (mSelectedItems.contains(item)) {
            mSelectedItems.remove(item);
        } else {
            mSelectedItems.add(item);
        }

        int selectedCount = mSelectedItems.size();

        mMultiSelectCallback.setMultiSelectCount(selectedCount);

        if (mSelectedItems.size() == 0) {
            setMultiSelectMode(false);
        }
    }

    private void clearAndUpdateItems(){
        for(Iterator<T> iterator  = mSelectedItems.iterator(); iterator.hasNext();){
            int pos = indexOf(iterator.next());
            iterator.remove();
            notifyItemChanged(pos);
        }
    }
}
