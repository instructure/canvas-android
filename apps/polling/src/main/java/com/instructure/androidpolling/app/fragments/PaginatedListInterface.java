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

package com.instructure.androidpolling.app.fragments;

import android.view.View;

public interface PaginatedListInterface {

    int getRootLayoutCode();
    int getFooterLayoutCode();
    int getEmptyViewLayoutCode();
    void configureViews(View rootView);
    String getTitle();
    int getDividerHeight();
    int getDividerColor();

    void setupCallbacks();
    boolean areItemsSorted();
    boolean areItemsReverseSorted();
    void loadFirstPage();
    void loadNextPage(String nextURL);
    String getNextURL();
    void setNextURLNull();
    void resetData();
    void finishLoading();
}
