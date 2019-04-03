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
import android.view.View;

import com.instructure.student.binders.MasteryPathAssignmentBinder;
import com.instructure.student.holders.MasteryAssignmentViewHolder;
import com.instructure.student.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.MasteryPathAssignment;
import com.instructure.pandautils.utils.ColorKeeper;


public class MasteryPathOptionsRecyclerAdapter extends BaseListRecyclerAdapter<MasteryPathAssignment, MasteryAssignmentViewHolder> {

    private int mCourseColor;
    private AdapterToFragmentCallback<Assignment> mAdapterToFragmentCallback;


    /* This is the real constructor and should be called to create instances of this adapter */
    public MasteryPathOptionsRecyclerAdapter(Context context, CanvasContext canvasContext, MasteryPathAssignment[] assignments, AdapterToFragmentCallback<Assignment> adapterToFragmentCallback) {
        this(context, canvasContext, assignments, adapterToFragmentCallback, false);
        mCourseColor = ColorKeeper.getOrGenerateColor(canvasContext);
    }

    /* This overloaded constructor is for testing purposes ONLY, and should not be used to create instances of this adapter. */
    protected MasteryPathOptionsRecyclerAdapter(Context context, CanvasContext canvasContext,  MasteryPathAssignment[] assignments, AdapterToFragmentCallback<Assignment> adapterToFragmentCallback, boolean isLoadData) {
        super(context, MasteryPathAssignment.class);
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        addAll(assignments);
        setRefresh(false);
    }

    @Override
    public MasteryAssignmentViewHolder createViewHolder(View v, int viewType) {
        return new MasteryAssignmentViewHolder(v);
    }

    @Override
    public void bindHolder(MasteryPathAssignment assignment, MasteryAssignmentViewHolder holder, int position) {
        MasteryPathAssignmentBinder.bind(getContext(), holder, assignment, mCourseColor, mAdapterToFragmentCallback);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return MasteryAssignmentViewHolder.holderResId();
    }

    @Override
    public void contextReady() {

    }



}
