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

package com.instructure.student.model;

import android.graphics.Bitmap;

public class NavMenuItem {

    public int localId;
    public String text;
    public int iconResId;
    public Bitmap icon;

    public NavMenuItem(int localId, String text, Bitmap icon) {
        this.localId = localId;
        this.text = text;
        this.iconResId = 0;
        this.icon = icon;
    }

    public NavMenuItem(int localId, String text, int iconResId) {
        this.localId = localId;
        this.text = text;
        this.iconResId = iconResId;
        this.icon = null;
    }
}
