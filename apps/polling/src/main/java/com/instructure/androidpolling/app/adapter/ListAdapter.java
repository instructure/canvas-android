/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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

package com.instructure.androidpolling.app.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.instructure.androidpolling.app.delegate.ListDelegate;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Generic type I is the type of the model object used to create row views in adapter
 * It must implement Comparable so the adapter can sort the rows
 */
public class ListAdapter<I extends Comparable<I>> extends BaseAdapter implements CanvasAdapter {

    private Set<I> items;
    private ListDelegate<I> delegate;

    // these are to help us not have to call toArray all the time when
    // we are getting items
    private I[] cachedItems;
    private boolean invalid;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////

    private ListAdapter(ListDelegate<I> delegate, boolean areItemsSorted, boolean isReversedSorted) {
        this.delegate = delegate;

        if (areItemsSorted) {
            if(isReversedSorted){
                items = new TreeSet<I>(Collections.reverseOrder());
            }
            else{
            items = new TreeSet<I>();
            }
        } else {
            // this will keep the ordering of the set like an array
            items = new LinkedHashSet<I>();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Helpers
    ///////////////////////////////////////////////////////////////////////////
    public I getFirstItem(){
        return (I)getItem(0);
    }

    public void addItem(I item) {
        invalid = true;
        boolean added = items.add(item);
        // replace it with the new one
        if (!added) {
            items.remove(item);
            items.add(item);
        }
        notifyDataSetChanged();
    }

    public void addAll(I[] items) {
        for (I item : items) {
            addItem(item);
        }
    }

    public void removeItem(I item) {
        invalid = true;
        items.remove(item);
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        cachedItems = null;
        invalid = true;
        items.clear();
        notifyDataSetChanged();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Helpers
    ///////////////////////////////////////////////////////////////////////////

    private I itemAtPosition(int position) {
        if (invalid || cachedItems == null) {
            cachedItems = (I[]) items.toArray(new Comparable[items.size()]);
            invalid = false;
        }
        if (position < items.size()) {
            return cachedItems[position];
        }
        return null;
    }
    ///////////////////////////////////////////////////////////////////////////
    //Public Static Getters
    //////////////////////////////////////////////////////////////////////////

    public static<I> ListAdapter getListAdapter(ListDelegate<I> delegate, boolean isSorted, boolean isReversedSorted){
        if(isSorted){
            if(isReversedSorted){
                return getReverseSortedListAdapter(delegate);
            }
            else{
                return getSortedListAdapter(delegate);
            }

        }
        else{
            return getUnsortedListAdapter(delegate);
        }

    }

    public static<I> ListAdapter getListAdapter(ListDelegate<I> delegate, boolean isSorted){
        return getListAdapter(delegate, isSorted, false);
    }

    public static<I> ListAdapter getUnsortedListAdapter(ListDelegate<I> delegate){

        return new ListAdapter(delegate, false, false);
    }

    public static<I> ListAdapter getSortedListAdapter(ListDelegate<I> delegate){

        return new ListAdapter(delegate, true, false);
    }

    public static<I> ListAdapter getReverseSortedListAdapter(ListDelegate<I> delegate){
        return new ListAdapter(delegate, true, true);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public int getViewTypeCount() {
        return delegate.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return delegate.getItemViewType(position, itemAtPosition(position));
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return itemAtPosition(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return delegate.getRowViewForItem(itemAtPosition(position), convertView, position);
    }
}
