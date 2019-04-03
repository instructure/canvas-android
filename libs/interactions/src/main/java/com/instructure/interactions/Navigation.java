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

package com.instructure.interactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

public interface Navigation {

    @Nullable Fragment getTopFragment();
    @Nullable Fragment getPeekingFragment();
    @Nullable Fragment getCurrentFragment();

    void popCurrentFragment();
    void updateCalendarStartDay();
    void addBookmark();

    <F extends Fragment & FragmentInteractions> void attachNavigationDrawer(@NonNull F fragment, @NonNull Toolbar toolbar);
}
