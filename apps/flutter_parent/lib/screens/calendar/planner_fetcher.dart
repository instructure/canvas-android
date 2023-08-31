// Copyright (C) 2020 - present Instructure, Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 3 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'dart:async';

import 'package:built_collection/built_collection.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/models/calendar_filter.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class PlannerFetcher extends ChangeNotifier {
  final Map<String, AsyncSnapshot<List<PlannerItem>>?> daySnapshots = {};

  final Map<String, bool> failedMonths = {};

  // Maps observee ids to a map of course ids to names
  final Map<String, Map<String, String>> courseNameMap = {};

  final String userDomain;

  final String userId;

  Future<List<Course>?>? courseListFuture;
  bool firstFilterUpdateFlag = false;

  String _observeeId;

  String get observeeId => _observeeId;

  PlannerFetcher({DateTime? fetchFirst, required String observeeId, required this.userDomain, required this.userId}): _observeeId = observeeId {
    if (fetchFirst != null) getSnapshotForDate(fetchFirst);
  }

  Future<Set<String>> getContexts() async {
    CalendarFilter? calendarFilter = await locator<CalendarFilterDb>().getByObserveeId(
      userDomain,
      userId,
      _observeeId,
    );
    if (calendarFilter == null || (courseNameMap[_observeeId] == null || courseNameMap[_observeeId]!.isEmpty)) {
      // We need to fetch the courses for a couple of reasons:
      // First, scheduleItems don't have a context name.
      // Second, calendar is opposite planner, in that 0 contexts means no calendar items. In order to deal with that
      // case we have to load all courses and set the first ten as filters for the first load
      if (courseListFuture == null) {
        courseListFuture = locator<CoursesInteractor>().getCourses(isRefresh: true);
      }

      var courses = await courseListFuture;

      if (courseNameMap[_observeeId] == null) {
        courseNameMap[_observeeId] = {};
      }

      // Add the names to our map so we can fill those in later
      // We have to handle the case where their filter is from before the api migration, so we trim it down.
      courses?.forEach((course) => courseNameMap[_observeeId]![course.id] = course.name);
      var tempCourseList = courses?.map((course) => course.contextFilterId()).toList();
      Set<String> courseSet = Set.from(tempCourseList ?? []);

      if (calendarFilter != null) {
        return calendarFilter.filters.toSet();
      } else if (firstFilterUpdateFlag == false) {
        // This initial DB insert only needs to happen once for fresh users,
        // prevent the multiple month loads from repeating it
        CalendarFilter filter = CalendarFilter((b) => b
          ..userDomain = userDomain
          ..userId = userId
          ..observeeId = _observeeId
          ..filters = SetBuilder(courseSet));
        locator<CalendarFilterDb>().insertOrUpdate(filter);
        firstFilterUpdateFlag = true;
      }

      return courseSet;
    } else {
      return calendarFilter.filters.toSet();
    }
  }

  AsyncSnapshot<List<PlannerItem>> getSnapshotForDate(DateTime date) {
    final dayKey = dayKeyForDate(date);
    AsyncSnapshot<List<PlannerItem>>? daySnapshot = daySnapshots[dayKey];
    if (daySnapshot != null) return daySnapshot;
    _beginMonthFetch(date);
    return daySnapshots[dayKey]!;
  }

  Future<void> refreshItemsForDate(DateTime date) async {
    String dayKey = dayKeyForDate(date);
    bool hasError = daySnapshots[dayKey]?.hasError ?? true;
    bool monthFailed = failedMonths[monthKeyForYearMonth(date.year, date.month)] ?? false;
    if (hasError && monthFailed) {
      // Previous fetch failed, retry the whole month
      _beginMonthFetch(date, refresh: true);
      notifyListeners();
    } else {
      // Just retry the single day
      if (hasError) {
        daySnapshots[dayKey] = AsyncSnapshot<List<PlannerItem>>.nothing().inState(ConnectionState.waiting);
      } else {
        daySnapshots[dayKey] = daySnapshots[dayKey]!.inState(ConnectionState.waiting);
      }
      notifyListeners();
      try {
        final contexts = await getContexts();
        List<PlannerItem> items = await fetchPlannerItems(date.withStartOfDay()!, date.withEndOfDay()!, contexts, true);
        daySnapshots[dayKey] = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, items);
      } catch (e) {
        daySnapshots[dayKey] = AsyncSnapshot<List<PlannerItem>>.withError(ConnectionState.done, e);
      } finally {
        notifyListeners();
      }
    }
  }

  _beginMonthFetch(DateTime date, {bool refresh = false}) {
    final lastDayOfMonth = date.withEndOfMonth()!.day;
    for (int i = 1; i <= lastDayOfMonth; i++) {
      var dayKey = dayKeyForYearMonthDay(date.year, date.month, i);
      daySnapshots[dayKey] = AsyncSnapshot<List<PlannerItem>>.nothing().inState(ConnectionState.waiting);
    }
    _fetchMonth(date, refresh);
  }

  _fetchMonth(DateTime date, bool refresh) async {
    try {
      final contexts = await getContexts();
      List<PlannerItem> items =
          await fetchPlannerItems(date.withStartOfMonth()!, date.withEndOfMonth()!, contexts, refresh);
      _completeMonth(items, date);
    } catch (e) {
      _failMonth(e, date);
    }
  }

  @visibleForTesting
  Future<List<PlannerItem>> fetchPlannerItems(
      DateTime startDate, DateTime endDate, Set<String> contexts, bool refresh) async {
    List<List<ScheduleItem>?> tempItems = await Future.wait([
      locator<CalendarEventsApi>().getUserCalendarItems(
        _observeeId,
        startDate,
        endDate,
        ScheduleItem.apiTypeAssignment,
        contexts: contexts,
        forceRefresh: refresh,
      ),
      locator<CalendarEventsApi>().getUserCalendarItems(
        _observeeId,
        startDate,
        endDate,
        ScheduleItem.apiTypeCalendar,
        contexts: contexts,
        forceRefresh: refresh,
      ),
    ]);
    List<ScheduleItem>? scheduleItems;
    if (tempItems[0] == null ||  tempItems[1] == null) {
      scheduleItems = [];
    }
    else {
      scheduleItems = tempItems[0]! + tempItems[1]!;
    }
    scheduleItems.retainWhere((it) => it.isHidden != true); // Exclude hidden items
    return scheduleItems.map((item) => item.toPlannerItem(courseNameMap[_observeeId]![item.getContextId()])).toList();
  }

  _completeMonth(List<PlannerItem> items, DateTime date) {
    failedMonths[monthKeyForDate(date)] = false;
    final Map<String, List<PlannerItem>> dayItems = {};
    final lastDayOfMonth = date.withEndOfMonth()!.day;
    for (int i = 1; i <= lastDayOfMonth; i++) {
      dayItems[dayKeyForYearMonthDay(date.year, date.month, i)] = [];
    }
    items.forEach((item) {
      if (item.plannableDate != null) {
        String dayKey = dayKeyForDate(item.plannableDate!.toLocal());
        dayItems[dayKey]?.add(item);
      }
    });

    dayItems.forEach((dayKey, items) {
      daySnapshots[dayKey] = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, items);
    });
    notifyListeners();
  }

  _failMonth(Object error, DateTime date) {
    failedMonths[monthKeyForDate(date)] = true;
    final lastDayOfMonth = date.withEndOfMonth()!.day;
    for (int i = 1; i <= lastDayOfMonth; i++) {
      daySnapshots[dayKeyForYearMonthDay(date.year, date.month, i)] =
          AsyncSnapshot.withError(ConnectionState.done, error);
    }
    notifyListeners();
  }

  String dayKeyForDate(DateTime date) => dayKeyForYearMonthDay(date.year, date.month, date.day);

  String monthKeyForDate(DateTime date) => monthKeyForYearMonth(date.year, date.month);

  String dayKeyForYearMonthDay(int year, int month, int day) => '$year-$month-$day';

  String monthKeyForYearMonth(int year, int month) => '$year-$month';

  Future<void> setContexts(Set<String> contexts) async {
    CalendarFilter filter = CalendarFilter((b) => b
      ..userDomain = userDomain
      ..userId = userId
      ..observeeId = _observeeId
      ..filters = SetBuilder(contexts));
    await locator<CalendarFilterDb>().insertOrUpdate(filter);
    reset();
  }

  void setObserveeId(String observeeId) {
    this._observeeId = observeeId;
    // Reset the flag for first time DB inserts
    this.firstFilterUpdateFlag = false;
    this.courseListFuture = null;
    reset();
  }

  void reset() {
    daySnapshots.clear();
    failedMonths.clear();
    notifyListeners();
  }
}
