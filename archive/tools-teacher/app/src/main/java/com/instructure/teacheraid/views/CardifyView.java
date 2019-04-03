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

package com.instructure.teacheraid.views;

import android.content.Context;
import android.util.AttributeSet;

import com.instructure.pandautils.views.FloatingCardView;
import com.instructure.teacheraid.R;

public class CardifyView extends FloatingCardView {

    @Override
    public int backgroundResId() {
        return R.drawable.background_repeated;
    }

    public CardifyView(Context context) {
        super(context);
    }

    public CardifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardifyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CardifyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
