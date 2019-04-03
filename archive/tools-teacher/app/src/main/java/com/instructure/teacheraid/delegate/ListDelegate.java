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
 */

package com.instructure.teacheraid.delegate;

import android.view.View;

/**
 * Generic type I is the type of the model object used to create row views in adapter
 * It must implement Comparable so the adapter can sort the rows
 */
public interface ListDelegate<I> {
    public View getRowViewForItem(I item, View convertView, int position);
    public int getViewTypeCount();
    public int getItemViewType(int position, I item);
    public void showFirstItem(I item);
    public boolean isShowFirstItem();
}
