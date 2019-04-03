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
package com.instructure.parentapp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.ScheduleItem;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.models.User;
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked;
import com.instructure.pandarecycler.util.Types;
import com.instructure.parentapp.R;
import com.instructure.parentapp.binders.CalendarWeekBinder;
import com.instructure.parentapp.binders.ExpandableHeaderBinder;
import com.instructure.parentapp.holders.CalendarWeekViewHolder;
import com.instructure.parentapp.holders.ExpandableViewHolder;
import com.instructure.parentapp.interfaces.AdapterToFragmentCallback;
import com.instructure.parentapp.models.WeekHeaderItem;
import com.instructure.parentapp.presenters.WeekPresenter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import instructure.androidblueprint.SyncExpandableRecyclerAdapter;

public class CalendarWeekRecyclerAdapter extends SyncExpandableRecyclerAdapter<WeekHeaderItem, ScheduleItem, RecyclerView.ViewHolder> {

    private AdapterToFragmentCallback<ScheduleItem> mAdapterToFragmentCallback;
    private ViewHolderHeaderClicked<WeekHeaderItem> mAdapterToHeaderCallback;
    private Map<Long, Course> mCourses = new HashMap<>();
    private User mStudent;


    public CalendarWeekRecyclerAdapter(
            @NonNull Context context,
            @NonNull WeekPresenter presenter,
            @NonNull List<Course> courses,
            @NonNull User student,
            @NonNull AdapterToFragmentCallback<ScheduleItem> callback,
            @NonNull ViewHolderHeaderClicked<WeekHeaderItem> headerCallback) {
        super(context, presenter);
        mStudent = student;
        mAdapterToFragmentCallback = callback;
        mAdapterToHeaderCallback = headerCallback;
        for(Course course : courses) {
            mCourses.put(course.getId(), course);
        }
    }

    public void setCourses(List<Course> courses) {
        for(Course course : courses) {
            mCourses.put(course.getId(), course);
        }
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, WeekHeaderItem item, boolean isExpanded) {
        ExpandableHeaderBinder.bind(getContext(), (ExpandableViewHolder) holder, item, item.getComparisonDate(), isExpanded, mAdapterToHeaderCallback);
    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, WeekHeaderItem weekHeaderItem, ScheduleItem item) {
        if(getContext() == null) return;

        final int courseColor = ContextCompat.getColor(getContext(), R.color.defaultPrimary);
        CalendarWeekBinder.bind(getContext(), (CalendarWeekViewHolder) holder, courseColor, item, mAdapterToFragmentCallback, mCourses);
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else {
            return new CalendarWeekViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        } else {
            return CalendarWeekViewHolder.holderResId();
        }
    }
}
