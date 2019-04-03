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

package com.instructure.student.view;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

public class SpinnerInteractionListener implements
        AdapterView.OnItemSelectedListener,
        View.OnTouchListener {

    public interface ItemSelection{
        public void itemSelected(AdapterView<?> parent, View view, int position, long id);
    }

    public SpinnerInteractionListener(ItemSelection itemSelection) {
        this.itemSelection = itemSelection;
    }

    private ItemSelection itemSelection;
    boolean userSelect = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        userSelect = true;
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (userSelect) {
            // Your selection handling code here
            if(itemSelection != null) {
                itemSelection.itemSelected(parent, view, pos, id);
            }
            userSelect = false;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
