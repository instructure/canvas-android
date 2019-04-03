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

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class ActionbarCourseSpinner extends Spinner {

    private boolean mToggleFlag = true;

    public ActionbarCourseSpinner(Context context) {
        super(context);
    }

    public ActionbarCourseSpinner(Context context, int mode) {
        super(context, mode);
    }

    public ActionbarCourseSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ActionbarCourseSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionbarCourseSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    @Override
    public int getSelectedItemPosition() {
        //Toggle flag is used to force the spinner to scroll to the top when something is selected.
        if (!mToggleFlag) {
            return 0;
        }
        return super.getSelectedItemPosition();
    }

    @Override
    public boolean performClick() {
        //Toggle flag is used to force the spinner to scroll to the top when something is selected.
        mToggleFlag = false;
        boolean result = super.performClick();
        mToggleFlag = true;
        return result;
    }

    public void forceSpinnerOpen(final Activity activity) {
        if(activity == null) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                performClick();
            }
        });
    }
}
