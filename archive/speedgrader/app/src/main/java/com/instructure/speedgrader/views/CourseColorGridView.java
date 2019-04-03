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
 *
 */

package com.instructure.speedgrader.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.speedgrader.adapters.CourseColorAdapter;

public class CourseColorGridView extends GridView implements APIStatusDelegate{


    OnItemClickListener onItemClickListener;

    public CourseColorGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public CourseColorGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CourseColorGridView(Context context) {
        super(context);
        init();
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void init(){
        super.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long courseID) {
                CanvasContextColor.setNewColor(CourseColorGridView.this, getContext(), CanvasContext.getGenericContext(CanvasContext.Type.COURSE, courseID, ""), CourseColorAdapter.courseColors[position]);

                //Anytime something is pressed, invalidate the list.
                ListAdapter listAdapter = getAdapter();
                if(listAdapter != null && listAdapter instanceof BaseAdapter){
                    ((BaseAdapter)listAdapter).notifyDataSetChanged();
                }

                //Pass the onItemClickListener the passed in one.
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(adapterView, view, position, courseID);
                }

            }
        });
    }


    @Override
    public void onCallbackStarted() {

    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {

    }

    @Override
    public void onNoNetwork() {

    }
}
