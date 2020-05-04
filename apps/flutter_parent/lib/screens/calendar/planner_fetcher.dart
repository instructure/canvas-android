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
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class PlannerFetcher extends ChangeNotifier {
  final Map<String, AsyncSnapshot<List<ScheduleItem>>> daySnapshots = {};

  final Map<String, bool> failedMonths = {};

  final String userDomain;

  final String userId;

  String _observeeId;

  String get observeeId => _observeeId;

  PlannerFetcher({DateTime fetchFirst, @required String observeeId, @required this.userDomain, @required this.userId}) {
    this._observeeId = observeeId;
    if (fetchFirst != null) getSnapshotForDate(fetchFirst);
  }

  Future<Set<String>> getContexts() async {
    CalendarFilter calendarFilter = await locator<CalendarFilterDb>().getByObserveeId(
      userDomain,
      userId,
      _observeeId,
    );
    if (calendarFilter == null) {
      // Fetch them courses and fill the filters
      var courses = await locator<CoursesInteractor>().getCourses(isRefresh: true);
      Set<String> courseSet = Set.from(courses.map((course) => course.contextFilterId()).toList());

      CalendarFilter filter = CalendarFilter((b) => b
        ..userDomain = userDomain
        ..userId = userId
        ..observeeId = _observeeId
        ..filters = SetBuilder(courseSet));
      locator<CalendarFilterDb>().insertOrUpdate(filter);

      return courseSet;
    } else {
      return calendarFilter.filters.toSet();
    }
  }

  AsyncSnapshot<List<ScheduleItem>> getSnapshotForDate(DateTime date) {
    final dayKey = dayKeyForDate(date);
    AsyncSnapshot<List<ScheduleItem>> daySnapshot = daySnapshots[dayKey];
    if (daySnapshot != null) return daySnapshot;
    _beginMonthFetch(date);
    return daySnapshots[dayKey];
  }

  Future<void> refreshItemsForDate(DateTime date) async {
    String dayKey = dayKeyForDate(date);
    bool hasError = daySnapshots[dayKey].hasError;
    bool monthFailed = failedMonths[monthKeyForYearMonth(date.year, date.month)] ?? false;
    if (hasError && monthFailed) {
      // Previous fetch failed, retry the whole month
      _beginMonthFetch(date, refresh: true);
      notifyListeners();
    } else {
      // Just retry the single day
      if (hasError) {
        daySnapshots[dayKey] = AsyncSnapshot<List<ScheduleItem>>.nothing().inState(ConnectionState.waiting);
      } else {
        daySnapshots[dayKey] = daySnapshots[dayKey].inState(ConnectionState.waiting);
      }
      notifyListeners();
      try {
        final contexts = await getContexts();
        List<List<ScheduleItem>> tempItems = await Future.wait([
          locator<CalendarEventsApi>().getUserCalendarItems(
            _observeeId,
            date.withStartOfDay(),
            date.withEndOfDay(),
            ScheduleItem.typeAssignment,
            contexts: contexts,
            forceRefresh: true,
          ),
          locator<CalendarEventsApi>().getUserCalendarItems(
            _observeeId,
            date.withStartOfDay(),
            date.withEndOfDay(),
            ScheduleItem.typeCalendar,
            contexts: contexts,
            forceRefresh: true,
          ),
        ]);

        List<ScheduleItem> items = tempItems[0] + tempItems[1];
        daySnapshots[dayKey] = AsyncSnapshot<List<ScheduleItem>>.withData(ConnectionState.done, items);
      } catch (e) {
        daySnapshots[dayKey] = AsyncSnapshot<List<ScheduleItem>>.withError(ConnectionState.done, e);
      } finally {
        notifyListeners();
      }
    }
  }

  _beginMonthFetch(DateTime date, {bool refresh = false}) {
    final lastDayOfMonth = date.withEndOfMonth().day;
    for (int i = 1; i <= lastDayOfMonth; i++) {
      var dayKey = dayKeyForYearMonthDay(date.year, date.month, i);
      daySnapshots[dayKey] = AsyncSnapshot<List<ScheduleItem>>.nothing().inState(ConnectionState.waiting);
    }
    _fetchMonth(date, refresh);
  }

  _fetchMonth(DateTime date, bool refresh) async {
    try {
      final contexts = await getContexts();
      List<List<ScheduleItem>> tempItems = await Future.wait([
        locator<CalendarEventsApi>().getUserCalendarItems(
          _observeeId,
          date.withStartOfMonth(),
          date.withEndOfMonth(),
          ScheduleItem.typeAssignment,
          contexts: contexts,
          forceRefresh: true,
        ),
        locator<CalendarEventsApi>().getUserCalendarItems(
          _observeeId,
          date.withStartOfMonth(),
          date.withEndOfMonth(),
          ScheduleItem.typeCalendar,
          contexts: contexts,
          forceRefresh: true,
        ),
      ]);

      List<ScheduleItem> items = tempItems[0] + tempItems[1];
      _completeMonth(items, date);
    } catch (e) {
      _failMonth(e, date);
    }
  }

  _completeMonth(List<ScheduleItem> items, DateTime date) {
    failedMonths[monthKeyForDate(date)] = false;
    final Map<String, List<ScheduleItem>> dayItems = {};
    final lastDayOfMonth = date.withEndOfMonth().day;
    for (int i = 1; i <= lastDayOfMonth; i++) {
      dayItems[dayKeyForYearMonthDay(date.year, date.month, i)] = [];
    }
    items.forEach((item) {
      String dayKey = dayKeyForDate(item.startAt.toLocal());
      dayItems[dayKey].add(item);
    });

    dayItems.forEach((dayKey, items) {
      daySnapshots[dayKey] = AsyncSnapshot<List<ScheduleItem>>.withData(ConnectionState.done, items);
    });
    notifyListeners();
  }

  _failMonth(Object error, DateTime date) {
    failedMonths[monthKeyForDate(date)] = true;
    final lastDayOfMonth = date.withEndOfMonth().day;
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
    reset();
  }

  void reset() {
    daySnapshots.clear();
    failedMonths.clear();
    notifyListeners();
  }
}
