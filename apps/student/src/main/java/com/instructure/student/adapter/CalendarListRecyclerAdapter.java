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
import android.text.TextUtils;
import android.view.View;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.CalendarEventAPI;
import com.instructure.canvasapi2.managers.CalendarEventManager;
import com.instructure.canvasapi2.managers.CourseManager;
import com.instructure.canvasapi2.managers.GroupManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Group;
import com.instructure.canvasapi2.models.ScheduleItem;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.CanvasApiExtensionsKt;
import com.instructure.canvasapi2.utils.DateHelper;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.ModelExtensionsKt;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;
import com.instructure.pandautils.utils.ColorKeeper;
import com.instructure.pandautils.utils.Const;
import com.instructure.student.binders.ExpandableHeaderBinder;
import com.instructure.student.binders.ScheduleItemBinder;
import com.instructure.student.fragment.CalendarListViewFragment;
import com.instructure.student.holders.ExpandableViewHolder;
import com.instructure.student.holders.ScheduleItemViewHolder;
import com.instructure.student.interfaces.AdapterToFragmentCallback;
import com.instructure.student.model.DateWindow;
import com.instructure.student.model.EventData;
import com.instructure.student.util.CanvasCalendarUtils;
import com.instructure.student.util.StudentPrefs;
import com.roomorama.caldroid.CalendarHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import hirondelle.date4j.DateTime;
import retrofit2.Call;
import retrofit2.Response;


public class CalendarListRecyclerAdapter extends ExpandableRecyclerAdapter<Date, ScheduleItem, RecyclerView.ViewHolder> {

    public interface AdapterToCalendarCallback{
        void showChooserDialog(boolean firstShow);
        void hidePandaLoading();
        void showPandaLoading();
        int getCurrentCalendarView();
        HashMap<String, Object> getExtraCalendarData();
        void refreshCalendarFragment();
        void setSelectedDates(Date d1, Date d2);
    }

    private static final int TYPE_ITEM = 1044;

    //region Callbacks
    private StatusCallback<List<ScheduleItem>> mCalendarEventsCallback;
    private StatusCallback<List<ScheduleItem>> mAssignmentEventsCallback;
    private StatusCallback<List<Course>> mAllCoursesCallback;
    private StatusCallback<List<Course>> mAllFavoriteCoursesCallback;
    private StatusCallback<List<Group>> mAllGroupsCallback;
    private AdapterToFragmentCallback<ScheduleItem> mAdapterToFragmentCallback;
    private AdapterToCalendarCallback mAdapterToCalendarCallback;
    //endregion

    //region Models
    private HashMap<String, String> mContextNamesMap = new LinkedHashMap<>(); // id -> name
    private ArrayList<String> mContextCourseCodes = new ArrayList<>(); // id -> course name, used for Calendar list names
    private ArrayList<CanvasContext> mCanvasContextItems = new ArrayList<>();
    private ArrayList<String> mFavoriteContextIds = new ArrayList<>();

    private ArrayList<ScheduleItem> mAssignmentEvents = new ArrayList<>();
    private ArrayList<ScheduleItem> mCalendarEvents = new ArrayList<>();
    private ArrayList<ScheduleItem> mAllEvents = new ArrayList<>();
    private HashMap<Long, Course> favoriteCourseMap = new HashMap<>();
    private DateTime mSelectedDay;
    //endregion

    //region Boolean State
    private boolean mGotCourses = false;
    private boolean mGotFavoriteCourses = false;
    private boolean mGotGroups = false;
    private boolean mGotBothGroupsAndCourses = false;
    private boolean mIsTodayPressed = false;
    private boolean mIsStartDayMonday = false;
    private boolean mIsCalendarViewCreated = false;
    private boolean mHasLoadedCanvasContexts = false;
    private boolean mHasLoadedCalenderEvents = false;
    private boolean mHasLoadedAssignmentEvents = false;
    //endregion

    private boolean mIsNoNetwork; // With multiple callbacks, some could fail while others don't. This manages when to display no connection when offline

    private static final SimpleDateFormat mFormat = DateHelper.getFullDayAndMonthFormat();

    /* For testing purposes only */
    protected CalendarListRecyclerAdapter(Context context){
        super(context, Date.class, ScheduleItem.class);
    }

    public CalendarListRecyclerAdapter(Context context, AdapterToFragmentCallback<ScheduleItem> adapterToFragmentCallback, AdapterToCalendarCallback adapterToCalendarCallback) {
        super(context, Date.class, ScheduleItem.class);

        mAdapterToCalendarCallback = adapterToCalendarCallback;
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        addUserContext();
        setExpandedByDefault(true);
        setupCallbacks();
    }

    //region Comparators
    @Override
    public GroupSortedList.GroupComparatorCallback<Date> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(Date oldGroup, Date newGroup) {
                return oldGroup.equals(newGroup);
            }

            @Override
            public boolean areItemsTheSame(Date group1, Date group2) {
                return getUniqueGroupId(group1) == getUniqueGroupId(group2);
            }

            @Override
            public long getUniqueGroupId(Date group) {
                return group.hashCode();
            }

            @Override
            public int getGroupType(Date group) {
                return Types.TYPE_HEADER;
            }
        };
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<Date, ScheduleItem> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<Date, ScheduleItem>() {
            @Override
            public int compare(Date group, ScheduleItem o1, ScheduleItem o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(ScheduleItem oldItem, ScheduleItem newItem) {
                return compareScheduleItems(oldItem, newItem);
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
            public int getChildType(Date group, ScheduleItem item) {
                return TYPE_ITEM;
            }
        };
    }


    private boolean compareScheduleItems(ScheduleItem oldItem, ScheduleItem newItem){
        boolean isSameDate = oldItem.getComparisonDate().equals(newItem.getComparisonDate());
        boolean isSameString = oldItem.getComparisonString().equals(newItem.getComparisonString());

        if (oldItem.getAssignment() != null && newItem.getAssignment() != null) {
            return oldItem.getAssignment().getDueAt().equals(newItem.getAssignment().getDueAt());
        } else {
            return !isNullableChanged(oldItem.getAssignment(), newItem.getAssignment()) && isSameDate && isSameString;
        }
    }

    private boolean isNullableChanged(Object o1, Object o2) {
        return (o1 == null && o2 != null) || (o1 !=null && o2 == null);
    }
    //endregion

    @Override
    public void contextReady() {

    }

    //region ViewHolder Configuration
    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, Date date, ScheduleItem item) {
        final CanvasContext canvasContext = getCanvasContextForItem(item);
        final int courseColor = ColorKeeper.getOrGenerateColor(canvasContext);
        ScheduleItemBinder.Companion.bind((ScheduleItemViewHolder)holder, item, getContext(), courseColor, mAdapterToFragmentCallback);
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, Date date, boolean isExpanded) {
        ExpandableHeaderBinder.bind(getContext(), null, (ExpandableViewHolder)holder, date, dateFormatSimple(date), isExpanded, getViewHolderHeaderClicked());
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if(viewType == Types.TYPE_HEADER){
            return new ExpandableViewHolder(v);
        } else {
            return new ScheduleItemViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if(viewType == Types.TYPE_HEADER){
            return ExpandableViewHolder.holderResId();
        } else {
            return ScheduleItemViewHolder.holderResId();
        }
    }
    //endregion

    //region Callbacks
    @Override
    public void onNoNetwork() {
        super.onNoNetwork();
        mIsNoNetwork = true;
    }

    @Override
    public void onCallbackFinished(ApiType type) {
        // Workaround for the multiple callbacks, some will succeed while others don't
        setLoadedFirstPage(true);
        shouldShowLoadingFooter();
        AdapterToRecyclerViewCallback adapterToRecyclerViewCallback = getAdapterToRecyclerViewCallback();
        if(adapterToRecyclerViewCallback != null){
            if (!mIsNoNetwork) { // double negative, only happens when there is network
                adapterToRecyclerViewCallback.setDisplayNoConnection(false);
                getAdapterToRecyclerViewCallback().setIsEmpty(isAllPagesLoaded() && size() == 0);
            }
        }
    }

    @Override
    public void refresh() {
        mIsNoNetwork = false;
        setRefresh(true);
        getAdapterToRecyclerViewCallback().setDisplayNoConnection(false);
        refreshCalendar();
    }

    @Override
    public void loadData() {
        if(getContext() == null){
            return;
        }
        if(!mHasLoadedCanvasContexts){
            CourseManager.getAllFavoriteCourses(true, mAllFavoriteCoursesCallback);
            CourseManager.getCourses(true, mAllCoursesCallback);
        }

    }

    // When sliding to the next month, we only want to recreate the events and assignment callbacks
    private void setupAssignmentAndEventCallbacks() {
        mAssignmentEventsCallback = new StatusCallback<List<ScheduleItem>>() {

            @Override
            public void onResponse(@NonNull Response<List<ScheduleItem>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                mAssignmentEvents.addAll(response.body());
                for (ScheduleItem s : mAssignmentEvents) {
                    s.setItemType(ScheduleItem.Type.TYPE_ASSIGNMENT);
                    if(s.getStartAt() != null && !s.isHidden()){
                        mAllEvents.add(s);
                    }
                }
                mHasLoadedAssignmentEvents = true;
                finishCalendarEvents();
                mAdapterToFragmentCallback.onRefreshFinished();
            }

            @Override
            public void onFail(@Nullable Call<List<ScheduleItem>> call, @NonNull Throwable error, @Nullable Response<?> response) {
                // Panda Loading doesn't disappear on error or lose of data connection
                mAdapterToCalendarCallback.hidePandaLoading();
            }
        };

        mCalendarEventsCallback = new StatusCallback<List<ScheduleItem>>() {

            @Override
            public void onResponse(@NonNull Response<List<ScheduleItem>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                mCalendarEvents.addAll(response.body());
                for (ScheduleItem s : mCalendarEvents) {
                    s.setItemType(ScheduleItem.Type.TYPE_CALENDAR);
                    if(s.getStartAt() != null && !s.isHidden()){
                        mAllEvents.add(s);
                    }
                }
                mHasLoadedCalenderEvents = true;
                finishCalendarEvents();
                mAdapterToFragmentCallback.onRefreshFinished();
            }

            @Override
            public void onFail(@Nullable Call<List<ScheduleItem>> call, @NonNull Throwable error, @Nullable Response<?> response) {
                mAdapterToCalendarCallback.hidePandaLoading();
            }
        };
    }

    @Override
    public void setupCallbacks(){
        setupAssignmentAndEventCallbacks();
        mAllCoursesCallback = new StatusCallback<List<Course>>() {

            @Override
            public void onResponse(@NonNull Response<List<Course>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {

                if (response.body() == null) {
                    return;
                }

                if (mGotCourses) {
                    return;
                }

                mGotCourses = true;

                for (Course c : response.body()) {
                    // Make sure we should add the course
                    if(c.getAccessRestrictedByDate() || ModelExtensionsKt.isInvited(c)) {
                        continue;
                    }
                    // We won't be able to get the name if the course hasn't started yet or if the user doesn't have
                    // access to the course. So we don't want to add the course to the list
                    if(!TextUtils.isEmpty(c.getName())) {
                        mContextNamesMap.put(c.getContextId(), c.getName());
                        mContextCourseCodes.add(c.getCourseCode());
                        mCanvasContextItems.add(c);
                    }
                }

                showFirstCalendarEvents();
            }

            @Override
            public void onFail(@Nullable Call<List<Course>> call, @NonNull Throwable error, @Nullable Response<?> response) {
                mAdapterToCalendarCallback.hidePandaLoading();
            }
        };

        mAllFavoriteCoursesCallback = new StatusCallback<List<Course>>() {

            @Override
            public void onResponse(@NonNull Response<List<Course>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                if (response.body() == null) {
                    return;
                }

                if (mGotFavoriteCourses) {
                    return;
                }

                mGotFavoriteCourses = true;

                for (Course c : response.body()) {
                    // Make sure we should add the course
                    if(!c.getAccessRestrictedByDate() && !ModelExtensionsKt.isInvited(c)) {
                        mFavoriteContextIds.add(c.getContextId());
                        favoriteCourseMap.put(c.getId(), c);
                    }
                }

                // We need the favorite courses to check which groups we should display
                GroupManager.getAllGroups(mAllGroupsCallback, true);
            }

            @Override
            public void onFail(@Nullable Call<List<Course>> call, @NonNull Throwable error, @Nullable Response<?> response) {

                mAdapterToCalendarCallback.hidePandaLoading();
            }
        };

        mAllGroupsCallback = new StatusCallback<List<Group>>() {

            @Override
            public void onResponse(@NonNull Response<List<Group>> response, @NonNull LinkHeaders linkHeaders, @NonNull ApiType type) {
                List<Group> groups = response.body();
                if (groups == null) {
                    return;
                }

                if (mGotGroups) {
                    return;
                }
                mGotGroups = true;

                for (Group g : groups) {
                    // This is the same check we use in the dashboard, so if it shows up there they should be able to use the calendar for it.
                    if(g.getCourseId() == 0L || (favoriteCourseMap.get(g.getCourseId()) != null && ModelExtensionsKt.isValidTerm(favoriteCourseMap.get(g.getCourseId())))) {
                        mContextNamesMap.put(g.getContextId(), g.getName());
                        mCanvasContextItems.add(g);
                        mContextCourseCodes.add(g.getName());
                    }
                }

                showFirstCalendarEvents();
            }

            @Override
            public void onFail(@Nullable Call<List<Group>> call, @NonNull Throwable error, @Nullable Response<?> response) {
                mAdapterToCalendarCallback.hidePandaLoading();
            }
        };
    }
    //endregion

    /**
     * Pulls down all calendar events for the provided date window.
     *
     * @param startDate
     * @param endDate
     */
    private void getEventsForTimePeriod(Calendar startDate, Calendar endDate, ArrayList<String> canvasContextIds) {
        final String startDateString = CanvasApiExtensionsKt.toApiString(new Date(startDate.getTimeInMillis()));
        final String endDateString = CanvasApiExtensionsKt.toApiString(new Date(endDate.getTimeInMillis()));
        mAssignmentEventsCallback.cancel();
        mCalendarEventsCallback.cancel();

        clear();
        setupAssignmentAndEventCallbacks();

        if (canvasContextIds.size() < 1) {
            // firstShow is false here
            mAdapterToCalendarCallback.showChooserDialog(false);
        } else {
            CalendarEventManager.getCalendarEventsExhaustive(false, CalendarEventAPI.CalendarEventType.ASSIGNMENT, startDateString, endDateString, canvasContextIds, mAssignmentEventsCallback, isRefresh());
            CalendarEventManager.getCalendarEventsExhaustive(false, CalendarEventAPI.CalendarEventType.CALENDAR, startDateString, endDateString, canvasContextIds, mCalendarEventsCallback, isRefresh());
        }
    }

    /**
     * This method makes sure that all calendar events have been pulled down before adding
     * items to our list and populating the calendar view.
     */
    private void finishCalendarEvents() {
        if (mIsNoNetwork) { // workaround for the multiple callbacks, which mess up the generic solution
            getAdapterToRecyclerViewCallback().setDisplayNoConnection(true);
            getAdapterToRecyclerViewCallback().setIsEmpty(size() == 0);
        }

        if(mHasLoadedCalenderEvents && mHasLoadedAssignmentEvents){
            getExtraData();
            selectDateAfterLoad();
        }
    }

    /**
     * This method makes sure that all courses, groups, and favorites have been pulled
     * down before beginning API calls for calendar events
     */
    private void showFirstCalendarEvents() {
        if (!mGotCourses || !mGotGroups || !mGotFavoriteCourses || !mIsCalendarViewCreated) {
            return;
        }

        if (mGotBothGroupsAndCourses) {
            return;
        }
        //prevents multiple calls to ths method
        mGotBothGroupsAndCourses = true;
        mHasLoadedCanvasContexts = true;

        ArrayList<String> prefs = getFilterPrefs();

        //show Dialog here
        if (prefs.size() == 0) {
            // if no calendar preferences exist, prompt the user to select calendars to view
            // no prefs -> firstShow = true
            mAdapterToCalendarCallback.showChooserDialog(true);
        } else {
            getEventsForTimePeriod(getStartDate(), getEndDate(), prefs);
        }
    }

    /**
     * This method will refresh the list view itself, and not effect the calendar view
     */
    public void refreshListView() {
        clear();

        for (ScheduleItem s : mAllEvents) {
            addItemForViewType(s);
        }

        //This prevents date selection from changing the empty text view before things are loaded
        //(will also be called by cache, so won't interfere with cached behavior)
        if(mHasLoadedCalenderEvents && mHasLoadedAssignmentEvents){
            mAdapterToCalendarCallback.hidePandaLoading();
            setAllPagesLoaded(true);
        }
    }

    /**
     * This method will reset all data, list and calendar, and hit the APIs again.
     */
    public void refreshCalendar() {
        mAdapterToCalendarCallback.showPandaLoading();
        // reset everything
        mIsNoNetwork = false;
        resetData();

        resetBooleans();
        getEventsForTimePeriod(getStartDate(), getEndDate(), new ArrayList<>(getFilterPrefs()));

    }

    @Override
    public boolean isPaginated() {
        return false;
    }

    /**
     * A special refresh method for when a user pressed the "today" button
     */
    public void refreshForTodayPressed() {
        mAdapterToCalendarCallback.showPandaLoading();
        // reset everything
        resetData();

        resetBooleans();
        //reset boolean for month listener
        mIsTodayPressed = false;
        getEventsForTimePeriod(getFirstStartDate(), getFirstEndDate(), new ArrayList<>(getFilterPrefs()));
    }

    /**
     * A small helper method to make adding items to our adapter simpler
     *
     * @param s
     * @return
     */
    private boolean addItemForViewType(ScheduleItem s) {
        Date d = s.getStartDate();
        if (d == null) {
            return false;
        }

        DateTime dateTime = DateTime.forInstant(d.getTime(), TimeZone.getDefault());
        CalendarListViewFragment.CalendarView currentCalendarView = CalendarListViewFragment.CalendarView.Companion.fromInteger(mAdapterToCalendarCallback.getCurrentCalendarView());
        if (currentCalendarView == CalendarListViewFragment.CalendarView.DAY_VIEW  && mSelectedDay != null) {
            if (dateTime.getDay().equals(mSelectedDay.getDay()) && dateTime.getMonth().equals(mSelectedDay.getMonth())) {
                addOrUpdateItem(DateHelper.getCleanDate(d.getTime()), s);
            }
        } else if (currentCalendarView == CalendarListViewFragment.CalendarView.WEEK_VIEW && mSelectedDay != null) {
            Date selectedDate = new Date(mSelectedDay.getMilliseconds(TimeZone.getDefault()));
            DateWindow dateWindow = CanvasCalendarUtils.setSelectedWeekWindow(selectedDate, mIsStartDayMonday);
            if (CanvasCalendarUtils.isWithinWeekWindow(d, dateWindow.getStart(), dateWindow.getEnd())) {
                addOrUpdateItem(DateHelper.getCleanDate(d.getTime()), s);
            }
        } else if (currentCalendarView == CalendarListViewFragment.CalendarView.MONTH_VIEW) {
            addOrUpdateItem(DateHelper.getCleanDate(d.getTime()), s);
        }

        return true;
    }

    /**
     * A small helper method used to create a data list for use in getView for the calendar grid
     * used to determine when dates have events and how many.
     */
    private void getExtraData() {
        HashMap<String, Object> hashMap = mAdapterToCalendarCallback.getExtraCalendarData();
        ArrayList<EventData> eventList = new ArrayList<>();

        //flush mAllEvents
        HashSet<ScheduleItem> hs = new HashSet<>();
        hs.addAll(mAllEvents);
        mAllEvents.clear();
        mAllEvents.addAll(hs);

        // Todo optimize
        for (ScheduleItem s : mAllEvents) {
            Date d = s.getStartDate();
            if (d == null) continue; // TODO: What do we do when the start date of a schedule item is null?
            DateTime dateTime = DateTime.forInstant(d.getTime(), TimeZone.getDefault());
            int dateCount = countDateInEvents(d);
            if (dateCount == 1) {  //ExtraData(date, number of events)
                eventList.add(new EventData(dateTime, EventData.EventCount.MIN));
            } else if (dateCount >= 2 && dateCount <= 5) {
                eventList.add(new EventData(dateTime, EventData.EventCount.MID));
            } else {
                eventList.add(new EventData(dateTime, EventData.EventCount.MAX));
            }
        }

        hashMap.put(Const.EVENT_LIST, eventList);

        mAdapterToCalendarCallback.refreshCalendarFragment();
        refreshListView();
        mAdapterToCalendarCallback.hidePandaLoading();
    }

    /**
     * Small helper method for getExtraData(), determines how many events coincide with each date.
     *
     * @param d
     * @return
     */
    private int countDateInEvents(Date d) {
        int count = 0;
        DateTime dateTime = DateTime.forInstant(d.getTime(), TimeZone.getDefault());
        for (ScheduleItem scheduleItem : mAllEvents) {
            DateTime compareDate = DateTime.forInstant(scheduleItem.getStartDate().getTime(), TimeZone.getDefault());
            if (dateTime.getMonth().equals(compareDate.getMonth()) && dateTime.getDay().equals(compareDate.getDay()) && dateTime.getYear().equals(compareDate.getYear())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Helper method to retrieve a users shared prefs for calendar
     *
     * @return
     */
    public static ArrayList<String> getFilterPrefs() {
        ArrayList<String> prefs = new ArrayList<>();
        prefs.addAll(StudentPrefs.getCalendarFilters());
        return prefs;
    }

    /**
     * Helper method to set a users shared prefs for calendar
     *
     * @return
     */
    public static void setFilterPrefs(List<String> filterPrefs) {
        Set<String> set = new HashSet<>();
        for (String s : filterPrefs) {
            if (s != null) {
                set.add(s);
            }
        }
        StudentPrefs.setCalendarFilters(set);
    }

    private void addUserContext() {
        User mUser = ApiPrefs.getUser();
        mCanvasContextItems.add(0, mUser);
        mContextCourseCodes.add(0, ""); // Blank holder - The user isn't a course.
        mContextNamesMap.put(mUser.getContextId(), mUser.getName());
    }

    /**
     * Helper method to get the course/group/context name
     *
     * @return
     */
    private String getContextName(ScheduleItem item) {
        for (CanvasContext canvasContext : mCanvasContextItems) {
            if (canvasContext.getId() == item.getContextId()) {
                return canvasContext.getName();
            }
        }
        return null;
    }

    /**
     * Helper method to get the course/group/context name
     *
     * @return
     */
    private CanvasContext getCanvasContextForItem(ScheduleItem item) {
        for (CanvasContext canvasContext : mCanvasContextItems) {
            if (canvasContext.getId() == item.getContextId()) {
                return canvasContext;
            }
        }
        return null;
    }

    public boolean isStartDayChanged(){
        return mIsStartDayMonday != StudentPrefs.getWeekStartsOnMonday();
    }

    private void selectDateAfterLoad(){
        if (mSelectedDay != null){
            Date selected = new Date(mSelectedDay.getMilliseconds(TimeZone.getDefault()));
            CalendarListViewFragment.CalendarView currentCalendarView = CalendarListViewFragment.CalendarView.Companion.fromInteger(mAdapterToCalendarCallback.getCurrentCalendarView());
            if (currentCalendarView == CalendarListViewFragment.CalendarView.DAY_VIEW) {
                mAdapterToCalendarCallback.setSelectedDates(selected, selected);
            } else if (currentCalendarView == CalendarListViewFragment.CalendarView.WEEK_VIEW) {
                DateWindow dateWindow = CanvasCalendarUtils.setSelectedWeekWindow(selected, mIsStartDayMonday);
                mAdapterToCalendarCallback.setSelectedDates(dateWindow.getStart(),dateWindow.getEnd());
            }
            refreshListView();
        }
    }

    private Calendar getStartDate() {
        int startDay = getStartDayOfWeek();
        Calendar cal = Calendar.getInstance();
        ArrayList<DateTime> dates = CalendarHelper.getFullWeeks(mSelectedDay.getMonth(), mSelectedDay.getYear(), startDay, false);
        DateTime startDate = dates.get(0);
        cal.set(startDate.getYear(), startDate.getMonth() - 1, startDate.getDay(), startDate.getHour(), startDate.getMinute(), startDate.getSecond());
        return cal;
    }

    private Calendar getEndDate() {
        int startDay = getStartDayOfWeek();
        Calendar cal = Calendar.getInstance();
        ArrayList<DateTime> dates = CalendarHelper.getFullWeeks(mSelectedDay.getMonth(), mSelectedDay.getYear(), startDay, false);
        DateTime endDate = dates.get(dates.size() - 1);
        cal.set(endDate.getYear(), endDate.getMonth() - 1, endDate.getDay(), endDate.getHour(), endDate.getMinute(), endDate.getSecond());
        return cal;
    }

    private Calendar getFirstStartDate(){
        int startDay = getStartDayOfWeek();
        Calendar cal = Calendar.getInstance();
        DateTime today = DateTime.forInstant(cal.getTime().getTime(), TimeZone.getDefault());
        ArrayList<DateTime> dates = CalendarHelper.getFullWeeks(today.getMonth(), today.getYear(), startDay, false);
        DateTime startDate = dates.get(0);
        cal.set(startDate.getYear(), startDate.getMonth() - 1, startDate.getDay(), startDate.getHour(), startDate.getMinute(), startDate.getSecond());
        return cal;
    }

    private Calendar getFirstEndDate(){
        int startDay = getStartDayOfWeek();
        Calendar cal = Calendar.getInstance();
        DateTime today = DateTime.forInstant(cal.getTime().getTime(), TimeZone.getDefault());
        ArrayList<DateTime> dates = CalendarHelper.getFullWeeks(today.getMonth(), today.getYear(), startDay, false);
        DateTime endDate = dates.get(dates.size() - 1);
        cal.set(endDate.getYear(), endDate.getMonth() - 1, endDate.getDay(), endDate.getHour(), endDate.getMinute(), endDate.getSecond());
        return cal;
    }

    private int getStartDayOfWeek(){
        int startDay;
        if(mIsStartDayMonday){
            startDay = 2; //Monday
        } else {
            startDay = 1; //Sunday
        }
        return startDay;
    }

    private void resetAPIBooleans() {
        mGotCourses = false;
        mGotGroups = false;
        mGotBothGroupsAndCourses = false;
        mHasLoadedAssignmentEvents = false;
        mHasLoadedCalenderEvents = false;
    }

    @Override
    public void resetData() {
        mAllEvents.clear();
        mAssignmentEvents.clear();
        mCalendarEvents.clear();
        clear();
    }

    @Override
    public void cancel() {
        mCalendarEventsCallback.cancel();
        mAssignmentEventsCallback.cancel();
        mAllCoursesCallback.cancel();
        mAllFavoriteCoursesCallback.cancel();
        mAllGroupsCallback.cancel();
    }

    public DateTime getSelectedDay(){
        return mSelectedDay;
    }

    public void setSelectedDay(DateTime date){
        mSelectedDay = date;
    }

    public boolean isStartDayMonday(){
        return mIsStartDayMonday;
    }

    public void setStartDayMonday(boolean flag){
        mIsStartDayMonday = flag;
    }

    public boolean isCalendarViewCreated(){
        return mIsCalendarViewCreated;
    }

    public void setCalendarViewCreated(boolean flag){
        mIsCalendarViewCreated = flag;
    }

    public boolean isTodayPressed(){
        return mIsTodayPressed;
    }

    public void setTodayPressed(boolean flag){
        mIsTodayPressed = flag;
    }


    public HashMap<String, String> getContextNames(){
        return mContextNamesMap;
    }

    public ArrayList<String> getContextCourseCodes() {
        return mContextCourseCodes;
    }

    public ArrayList<CanvasContext> getCanvasContextItems(){
        return mCanvasContextItems;
    }

    public void updateSelectedCalendarContexts(List<String> subscribedContexts){
        mHasLoadedAssignmentEvents = false;
        mHasLoadedCalenderEvents = false;
        setFilterPrefs(subscribedContexts);
        refreshCalendar();
    }

    /**
     * DateTime Format:
     *
     * WWWW, MMMM DD
     * Wednesday, February 3
     * @param date
     * @return
     */
    public static String dateFormatSimple(Date date){
        return mFormat.format(date.getTime());
    }
}
