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
import 'package:flutter/material.dart';
import 'package:flutter_student_embed/models/calendar_filter.dart';
import 'package:flutter_student_embed/models/planner_item.dart';
import 'package:flutter_student_embed/network/api/planner_api.dart';
import 'package:flutter_student_embed/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_student_embed/utils/db/calendar_filter_db.dart';
import 'package:flutter_student_embed/utils/service_locator.dart';

class PlannerFetcher extends ChangeNotifier {
  @visibleForTesting
  static final ValueNotifier<List<DateTime>> updateNotifier = ValueNotifier<List<DateTime>>([]);

  final Map<String, AsyncSnapshot<List<PlannerItem>>> daySnapshots = {};

  final Map<String, bool> failedMonths = {};

  final String userDomain;

  final String userId;

  Function() _updateListener;

  PlannerFetcher({DateTime fetchFirst, @required this.userDomain, @required this.userId}) {
    _updateListener = () {
      updateNotifier.value?.forEach((date) => refreshItemsForDate(date.toLocal(), clearCaches: true));
    };
    updateNotifier.addListener(_updateListener);
    if (fetchFirst != null) getSnapshotForDate(fetchFirst);
  }

  static void notifyDatesChanged(List<DateTime> dates) {
    updateNotifier.value = dates;
  }

  Future<Set<String>> getContexts() async {
    CalendarFilter calendarFilter = await locator<CalendarFilterDb>().getForUser(
      userDomain,
      userId,
    );
    return calendarFilter?.filters?.toSet() ?? {};
  }

  AsyncSnapshot<List<PlannerItem>> getSnapshotForDate(DateTime date) {
    final dayKey = dayKeyForDate(date);
    AsyncSnapshot<List<PlannerItem>> daySnapshot = daySnapshots[dayKey];
    if (daySnapshot != null) return daySnapshot;
    _beginMonthFetch(date);
    return daySnapshots[dayKey];
  }

  Future<void> refreshItemsForDate(DateTime date, {bool clearCaches: false}) async {
    String dayKey = dayKeyForDate(date);
    bool hasError = daySnapshots[dayKey].hasError;
    bool monthFailed = failedMonths[monthKeyForYearMonth(date.year, date.month)] ?? false;
    if (clearCaches) locator<PlannerApi>().clearCachesForUser(userId);
    if (hasError && monthFailed) {
      // Previous fetch failed, retry the whole month
      _beginMonthFetch(date, refresh: true);
      notifyListeners();
    } else {
      // Just retry the single day
      if (hasError) {
        daySnapshots[dayKey] = AsyncSnapshot<List<PlannerItem>>.nothing().inState(ConnectionState.waiting);
      } else {
        daySnapshots[dayKey] = daySnapshots[dayKey].inState(ConnectionState.waiting);
      }
      notifyListeners();
      try {
        final contexts = await getContexts();
        List<PlannerItem> items = await locator<PlannerApi>().getUserPlannerItems(
          userId,
          date.withStartOfDay(),
          date.withEndOfDay(),
          contexts: contexts.toList(),
          forceRefresh: true,
        );
        daySnapshots[dayKey] = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, items);
      } catch (e) {
        daySnapshots[dayKey] = AsyncSnapshot<List<PlannerItem>>.withError(ConnectionState.done, e);
      } finally {
        notifyListeners();
      }
    }
  }

  _beginMonthFetch(DateTime date, {bool refresh = false}) {
    final lastDayOfMonth = date.withEndOfMonth().day;
    for (int i = 1; i <= lastDayOfMonth; i++) {
      var dayKey = dayKeyForYearMonthDay(date.year, date.month, i);
      daySnapshots[dayKey] = AsyncSnapshot<List<PlannerItem>>.nothing().inState(ConnectionState.waiting);
    }
    _fetchMonth(date, refresh);
  }

  _fetchMonth(DateTime date, bool refresh) async {
    try {
      final contexts = await getContexts();
      var items = await locator<PlannerApi>().getUserPlannerItems(
        userId,
        date.withStartOfMonth(),
        date.withEndOfMonth(),
        contexts: contexts.toList(),
        forceRefresh: refresh,
      );
      _completeMonth(items, date);
    } catch (e) {
      _failMonth(e, date);
    }
  }

  _completeMonth(List<PlannerItem> items, DateTime date) {
    failedMonths[monthKeyForDate(date)] = false;
    final Map<String, List<PlannerItem>> dayItems = {};
    final lastDayOfMonth = date.withEndOfMonth().day;
    for (int i = 1; i <= lastDayOfMonth; i++) {
      dayItems[dayKeyForYearMonthDay(date.year, date.month, i)] = [];
    }
    items.forEach((item) {
      String dayKey = dayKeyForDate(item.plannableDate.toLocal());
      dayItems[dayKey].add(item);
    });

    dayItems.forEach((dayKey, items) {
      daySnapshots[dayKey] = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, items);
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
      ..filters = SetBuilder(contexts));
    await locator<CalendarFilterDb>().insertOrUpdate(filter);
    reset();
  }

  void reset() {
    daySnapshots.clear();
    failedMonths.clear();
    notifyListeners();
  }

  @override
  void dispose() {
    updateNotifier.removeListener(_updateListener);
    super.dispose();
  }
}
