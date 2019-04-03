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

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.instructure.student.R;
import com.instructure.student.binders.ExpandableHeaderBinder;
import com.instructure.student.binders.SyllabusBinder;
import com.instructure.student.holders.ExpandableViewHolder;
import com.instructure.student.holders.SyllabusItemViewHolder;
import com.instructure.student.holders.SyllabusViewHolder;
import com.instructure.student.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.CalendarEventAPI;
import com.instructure.canvasapi2.managers.CalendarEventManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.ScheduleItem;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;
import com.instructure.pandautils.utils.ColorKeeper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class SyllabusRecyclerAdapter extends ExpandableRecyclerAdapter<String, ScheduleItem, RecyclerView.ViewHolder> {

    private CanvasContext mCanvasContext;

    // region callback
    private long EVENTS_ID = 2222;
    private long ASSIGNMENTS_ID = 3333;
    private StatusCallback<List<ScheduleItem>> mScheduleCallback;
    private StatusCallback<List<ScheduleItem>> mAssignmentCallback;

    @SuppressLint("UseSparseArrays")
    // schedule and assignment have their own ids. When both callbacks return, the items are added to the adapter.
    private HashMap<Long, List<ScheduleItem>> mCallbackSyncHash = new HashMap<>();

    // endregion

    private AdapterToFragmentCallback<ScheduleItem> mAdapterToFragmentCallback;
    private String mPast;
    private String mNext7Days;
    private String mFuture;
    private String mNoDate;
    private String mSyllabus;

    /* For testing purposes only */
    protected SyllabusRecyclerAdapter(Context context){
        super(context, String.class, ScheduleItem.class);

    }

    public SyllabusRecyclerAdapter(
            Context context,
            CanvasContext canvasContext,
            AdapterToFragmentCallback<ScheduleItem> adapterToFragmentCallback) {

        super(context, String.class, ScheduleItem.class);
        mCanvasContext = canvasContext;
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        mPast = mContext.getString(R.string.past);
        mNext7Days = mContext.getString(R.string.next7Days);
        mFuture = mContext.getString(R.string.future);
        mNoDate = mContext.getString(R.string.noDate);
        mSyllabus = mContext.getString(R.string.syllabus);
        setExpandedByDefault(true);
        loadData();
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else if(viewType == Types.TYPE_SYLLABUS) {
            return new SyllabusItemViewHolder(v);
        } else {
            return new SyllabusViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        } else if(viewType == Types.TYPE_SYLLABUS) {
            return SyllabusItemViewHolder.holderResId();
        } else {
            return SyllabusViewHolder.holderResId();
        }
    }

    @Override
    public void contextReady() {

    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, String s, ScheduleItem scheduleItem) {
        if(scheduleItem != null) {
            final int courseColor = ColorKeeper.getOrGenerateColor(mCanvasContext);
            if (scheduleItem.getItemType() == ScheduleItem.Type.TYPE_SYLLABUS) {
                SyllabusBinder.bindSyllabusItem(getContext(), (SyllabusItemViewHolder) holder, courseColor, scheduleItem, mAdapterToFragmentCallback);
            } else {
                SyllabusBinder.bind(getContext(), (SyllabusViewHolder) holder, courseColor, scheduleItem, mAdapterToFragmentCallback);
            }
        }
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, String s, boolean isExpanded) {
        ExpandableHeaderBinder.bind(getContext(), mCanvasContext, (ExpandableViewHolder) holder, s, s, isExpanded, getViewHolderHeaderClicked());
    }



    // region Data


    @Override
    public void refresh() {
        mCallbackSyncHash.clear();
        super.refresh();
    }

    @Override
    public void loadData() {
        CalendarEventManager.getCalendarEventsExhaustive(true, CalendarEventAPI.CalendarEventType.ASSIGNMENT, null, null, getContextCodes(), mAssignmentCallback, isRefresh());
        CalendarEventManager.getCalendarEventsExhaustive(true, CalendarEventAPI.CalendarEventType.CALENDAR, null, null, getContextCodes(), mScheduleCallback, isRefresh());
    }

    private void populateAdapter(List<ScheduleItem> scheduleItems) {
        if (mCanvasContext.getType() == CanvasContext.Type.COURSE) {
            Course course = (Course)mCanvasContext;
            ScheduleItem syllabus = ScheduleItem.createSyllabus(course.getName(), course.getSyllabusBody());
            addOrUpdateItem(mSyllabus, syllabus);
        }
        Date curDate = new Date();
        // Set a future date 7 days in the future, make it the end of the day to include every assignment within the next 7 days,
        // including assignments that are due at the end of the 7th day
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 7);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        Date weekFutureDate = calendar.getTime();

        for (ScheduleItem scheduleItem : scheduleItems) {

            // If it is hidden we don't want to show it. This can happen when an event has multiple sections
            if((scheduleItem.isHidden())) {
                continue;
            }
            Date dueDate = scheduleItem.getStartDate();

            if(dueDate == null) {
                addOrUpdateItem(mNoDate, scheduleItem);
            } else if(dueDate.before(curDate)) {
                addOrUpdateItem(mPast, scheduleItem);
            } else if(((dueDate.after(curDate) && (dueDate.before(weekFutureDate))) || dueDate.equals(weekFutureDate)) ) {
                addOrUpdateItem(mNext7Days, scheduleItem);
            } else if(dueDate.after(weekFutureDate)) {
                addOrUpdateItem(mFuture, scheduleItem);
            }
        }
    }

    private void syncCallbacks() {
        if (mCallbackSyncHash.keySet().size() < 2) {
            return;
        }

        for (Map.Entry<Long, List<ScheduleItem>> entry : mCallbackSyncHash.entrySet()) {
            populateAdapter(entry.getValue());
        }

        mAdapterToFragmentCallback.onRefreshFinished();
    }

    public void removeCallbacks() {
        if(mScheduleCallback != null) {
            mScheduleCallback.cancel();
        }

        if(mAssignmentCallback != null) {
            mAssignmentCallback.cancel();
        }
    }

    @Override
    public void setupCallbacks() {
        mScheduleCallback = new StatusCallback<List<ScheduleItem>>() {

            @Override
            public void onResponse(@NonNull Response<List<ScheduleItem>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                mCallbackSyncHash.put(EVENTS_ID, response.body());
                syncCallbacks();
            }
        };

        mAssignmentCallback = new StatusCallback<List<ScheduleItem>>() {

            @Override
            public void onResponse(@NonNull Response<List<ScheduleItem>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                mCallbackSyncHash.put(ASSIGNMENTS_ID, response.body());
                syncCallbacks();
            }
        };
    }

    private ArrayList<String> getContextCodes() {
        return new ArrayList<String>() {{ add(mCanvasContext.getContextId()); }};
    }

    // endregion

    // region Expandable Callbacks

    private int getGroupPosition(String groupHeader) {
        if (mSyllabus.equals(groupHeader)) {
            return 0;
        } else if (mNext7Days.equals(groupHeader)) {
            return 1;
        } else if (mFuture.equals(groupHeader)) {
            return 2;
        } else if (mNoDate.equals(groupHeader)) {
            return 3;
        } else {
            return 4;
        }
    }

    @Override
    public GroupSortedList.GroupComparatorCallback<String> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<String>() {
            @Override
            public int compare(String o1, String o2) {
                return getGroupPosition(o1) - getGroupPosition(o2);
            }

            @Override
            public boolean areContentsTheSame(String oldGroup, String newGroup) {
                return oldGroup.equals(newGroup);
            }

            @Override
            public boolean areItemsTheSame(String group1, String group2) {
                return group1.equals(group2);
            }

            @Override
            public long getUniqueGroupId(String group) {
                return group.hashCode();
            }

            @Override
            public int getGroupType(String group) {
                return Types.TYPE_HEADER;
            }
        };
    }

    private boolean isNullableChanged(Object o1, Object o2) {
        return (o1 == null && o2 != null) || (o1 !=null && o2 == null);
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<String, ScheduleItem> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<String, ScheduleItem>() {
            @Override
            public int compare(String group, ScheduleItem o1, ScheduleItem o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(ScheduleItem oldItem, ScheduleItem newItem) {
                boolean isStartDateTheSame = true;
                if (isNullableChanged(oldItem.getStartAt(), newItem.getStartAt())) {
                    return false;
                } else if (oldItem.getStartAt() != null && newItem.getStartAt() != null) {
                    isStartDateTheSame = oldItem.getStartAt().equals(newItem.getStartAt());
                }
                return isStartDateTheSame && oldItem.getTitle().equals(newItem.getTitle());
            }

            @Override
            public boolean areItemsTheSame(ScheduleItem item1, ScheduleItem item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(ScheduleItem item) {
                return item.getId();
            }

            @Override
            public int getChildType(String group, ScheduleItem item) {
                if(item.getItemType() == ScheduleItem.Type.TYPE_SYLLABUS) {
                    return Types.TYPE_SYLLABUS;
                }
                return Types.TYPE_ITEM;
            }
        };
    }
    // endregion
}
