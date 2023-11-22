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

import 'package:built_collection/built_collection.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_parent/models/calendar_filter.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/screens/calendar/planner_fetcher.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  MockCalendarEventsApi api = MockCalendarEventsApi();
  MockCalendarFilterDb filterDb = MockCalendarFilterDb();
  MockCoursesInteractor interactor = MockCoursesInteractor();

  final String userDomain = 'user_domain';
  final String userId = 'user_123';
  final String observeeId = 'observee_123';
  final Set<String> contexts = {'course_123'};
  final course = Course((b) => b
    ..id = '123'
    ..name = 'hodor'
    ..enrollments = BuiltList.of([
      Enrollment((enrollment) => enrollment
        ..userId = '123'
        ..courseId = '123'
        ..enrollmentState = 'active')
    ]).toBuilder());

  setupTestLocator((locator) {
    locator.registerLazySingleton<CalendarEventsApi>(() => api);
    locator.registerLazySingleton<CalendarFilterDb>(() => filterDb);
    locator.registerFactory<CoursesInteractor>(() => interactor);
  });

  setUp(() async {
    // Reset APi mock
    reset(api);
    reset(interactor);

    // Reset db mock
    reset(filterDb);
    when(filterDb.getByObserveeId(any, any, any)).thenAnswer((_) async {
      return CalendarFilter((b) => b
        ..userDomain = userDomain
        ..userId = userId
        ..observeeId = observeeId
        ..filters = SetBuilder(contexts));
    });

    when(interactor.getCourses(isRefresh: anyNamed('isRefresh'))).thenAnswer((_) => Future.value(List.of([course])));
  });

  test('fetches month for date', () async {
    final date = DateTime(2000, 1, 15); // Jan 15 2000
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain, observeeId: observeeId, fetchFirst: date);

    fetcher.getSnapshotForDate(date);
    await untilCalled(
      api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeAssignment,
          contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    await untilCalled(
      api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeCalendar,
          contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    verify(
      api.getUserCalendarItems(
        observeeId,
        DateTime(2000), // Start of day Jan 1 2000
        DateTime(2000, 1, 31, 23, 59, 59, 999), // End of day Jan 31 2000
        ScheduleItem.apiTypeAssignment,
        contexts: contexts,
        forceRefresh: false,
      ),
    );

    verify(
      api.getUserCalendarItems(
        observeeId,
        DateTime(2000), // Start of day Jan 1 2000
        DateTime(2000, 1, 31, 23, 59, 59, 999), // End of day Jan 31 2000
        ScheduleItem.apiTypeCalendar,
        contexts: contexts,
        forceRefresh: false,
      ),
    );
  });

  test('fetches specified fetchFirst date', () async {
    final date = DateTime.now();
    final userId = 'user_123';
    final contexts = {'course_123'};
    PlannerFetcher(userId: userId, userDomain: userDomain, observeeId: observeeId, fetchFirst: date);

    await untilCalled(
      api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeAssignment,
          contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    await untilCalled(
      api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeCalendar,
          contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    verify(
      api.getUserCalendarItems(
        observeeId,
        date.withStartOfMonth()!,
        date.withEndOfMonth()!,
        ScheduleItem.apiTypeAssignment,
        contexts: contexts,
        forceRefresh: false,
      ),
    );

    verify(
      api.getUserCalendarItems(
        observeeId,
        date.withStartOfMonth()!,
        date.withEndOfMonth()!,
        ScheduleItem.apiTypeCalendar,
        contexts: contexts,
        forceRefresh: false,
      ),
    );
  });

  test('does not perform fetch if snapshot exists for day', () {
    final date = DateTime.now();
    final fetcher = PlannerFetcher(userId: '', userDomain: userDomain, observeeId: observeeId);

    final expectedSnapshot = AsyncSnapshot<List<PlannerItem>>.nothing();
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = expectedSnapshot;

    final snapshot = fetcher.getSnapshotForDate(date);

    expect(snapshot, expectedSnapshot);

    verifyNever(
      api.getUserCalendarItems(
        any,
        any,
        any,
        any,
        contexts: anyNamed('contexts'),
        forceRefresh: anyNamed('forceRefresh'),
      ),
    );
  });

  test('refreshes single day if day has data', () async {
    final date = DateTime.now();
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain, observeeId: observeeId);

    final existingSnapshot = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, []);
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = existingSnapshot;

    fetcher.refreshItemsForDate(date);

    await untilCalled(
      api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeAssignment,
          contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    await untilCalled(
      api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeCalendar,
          contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    verify(
      api.getUserCalendarItems(
        observeeId,
        date.withStartOfDay()!,
        date.withEndOfDay()!,
        ScheduleItem.apiTypeAssignment,
        contexts: contexts,
        forceRefresh: true,
      ),
    );

    verify(
      api.getUserCalendarItems(
        observeeId,
        date.withStartOfDay()!,
        date.withEndOfDay()!,
        ScheduleItem.apiTypeCalendar,
        contexts: contexts,
        forceRefresh: true,
      ),
    );
  });

  test('refreshes single day if day has failed', () async {
    final date = DateTime.now();
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain, observeeId: observeeId);

    final failedSnapshot = AsyncSnapshot<List<PlannerItem>>.withError(ConnectionState.done, Error());
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = failedSnapshot;
    fetcher.failedMonths[fetcher.monthKeyForDate(date)] = false;

    fetcher.refreshItemsForDate(date);

    await untilCalled(
      api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeAssignment,
          contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    await untilCalled(
      api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeCalendar,
          contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    verify(
      api.getUserCalendarItems(
        observeeId,
        date.withStartOfDay()!,
        date.withEndOfDay()!,
        ScheduleItem.apiTypeAssignment,
        contexts: contexts,
        forceRefresh: true,
      ),
    );

    verify(
      api.getUserCalendarItems(
        observeeId,
        date.withStartOfDay()!,
        date.withEndOfDay()!,
        ScheduleItem.apiTypeCalendar,
        contexts: contexts,
        forceRefresh: true,
      ),
    );
  });

  test('refreshes entire month if month has failed for day', () async {
    final date = DateTime.now();
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain, observeeId: observeeId);

    final failedSnapshot = AsyncSnapshot<List<PlannerItem>>.withError(ConnectionState.done, Error());
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = failedSnapshot;
    fetcher.failedMonths[fetcher.monthKeyForDate(date)] = true;

    fetcher.refreshItemsForDate(date);

    await untilCalled(
      api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeAssignment,
          contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    await untilCalled(
      api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeCalendar,
          contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    verify(
      api.getUserCalendarItems(
        observeeId,
        date.withStartOfMonth()!,
        date.withEndOfMonth()!,
        ScheduleItem.apiTypeCalendar,
        contexts: contexts,
        forceRefresh: true,
      ),
    );

    verify(
      api.getUserCalendarItems(
        observeeId,
        date.withStartOfMonth()!,
        date.withEndOfMonth()!,
        ScheduleItem.apiTypeAssignment,
        contexts: contexts,
        forceRefresh: true,
      ),
    );
  });

  test('sets error snapshot if refresh fails', () async {
    final date = DateTime.now();
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain, observeeId: observeeId);

    when(api.getUserCalendarItems(any, any, any, any,
            contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) async => throw Error);

    final existingSnapshot = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, []);
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = existingSnapshot;

    await fetcher.refreshItemsForDate(date);
    final snapshot = fetcher.getSnapshotForDate(date);

    expect(snapshot.hasError, isTrue);
  });

  test('reset clears data and notifies listeners', () {
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain, observeeId: observeeId);
    fetcher.daySnapshots['ABC'] = null;
    fetcher.failedMonths['JAN'] = true;

    int notifyCount = 0;
    fetcher.addListener(() {
      notifyCount++;
    });

    expect(fetcher.daySnapshots, isNotEmpty);
    expect(fetcher.failedMonths, isNotEmpty);

    fetcher.reset();

    expect(notifyCount, 1);
    expect(fetcher.daySnapshots, isEmpty);
    expect(fetcher.failedMonths, isEmpty);
  });

  test('setObserveeId resets fetcher and notifies listeners', () {
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain, observeeId: observeeId);
    fetcher.daySnapshots['ABC'] = null;
    fetcher.failedMonths['JAN'] = true;

    int notifyCount = 0;
    fetcher.addListener(() {
      notifyCount++;
    });

    expect(fetcher.observeeId, observeeId);
    expect(fetcher.daySnapshots, isNotEmpty);
    expect(fetcher.failedMonths, isNotEmpty);

    final newObserveeId = 'new-observee-id';
    fetcher.setObserveeId(newObserveeId);

    expect(notifyCount, 1);
    expect(fetcher.observeeId, newObserveeId);
    expect(fetcher.daySnapshots, isEmpty);
    expect(fetcher.failedMonths, isEmpty);
  });

  test('setContexts calls insertOrUpdate on database, resets fetcher, and notifies listeners', () async {
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain, observeeId: observeeId);
    fetcher.daySnapshots['ABC'] = null;
    fetcher.failedMonths['JAN'] = true;

    int notifyCount = 0;
    fetcher.addListener(() {
      notifyCount++;
    });

    expect(fetcher.observeeId, observeeId);
    expect(fetcher.daySnapshots, isNotEmpty);
    expect(fetcher.failedMonths, isNotEmpty);

    final newContexts = {'course_123', 'course_456'};
    await fetcher.setContexts(newContexts);

    expect(notifyCount, 1);
    expect(fetcher.daySnapshots, isEmpty);
    expect(fetcher.failedMonths, isEmpty);

    final expectedFilterData = CalendarFilter((b) => b
      ..userDomain = userDomain
      ..userId = userId
      ..observeeId = observeeId
      ..filters = SetBuilder(newContexts));
    verify(filterDb.insertOrUpdate(expectedFilterData));
  });

  test('getContexts fetches courses and sets courseNameMap', () async {
    when(filterDb.getByObserveeId(any, any, any)).thenAnswer((_) => Future.value(null));

    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain, observeeId: observeeId);

    final newContexts = await fetcher.getContexts();

    await untilCalled(interactor.getCourses(isRefresh: anyNamed('isRefresh')));

    verify(interactor.getCourses(isRefresh: anyNamed('isRefresh')));

    expect(newContexts, contexts);
    expect(fetcher.courseNameMap[observeeId], isNotNull);
    expect(fetcher.courseNameMap[observeeId]?[course.id], course.name);
  });

  test('fetchPlannerItems excludes hidden items', () async {
    var observeeId = "observee_123";
    var courseId = "456";
    var courseName = "Course 456";
    var now = DateTime.now();

    var fetcher = PlannerFetcher(userId: "", userDomain: "", observeeId: observeeId);
    fetcher.courseNameMap[observeeId] = {};
    fetcher.courseNameMap[observeeId]?[courseId] = courseName;

    var item = ScheduleItem((b) => b
      ..title = "Item"
      ..contextCode = "course_$courseId"
      ..startAt = now);

    var hiddenItem = item.rebuild((b) => b
      ..title = 'Hidden Item'
      ..isHidden = true);

    when(
      api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeAssignment,
          contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    ).thenAnswer((realInvocation) async => []);

    when(
      api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeCalendar,
          contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    ).thenAnswer((realInvocation) async => [item, hiddenItem]);

    var expected = [item.toPlannerItem(courseName)];
    var actual = await fetcher.fetchPlannerItems(now, now, {}, false);

    expect(actual, expected);
  });
}
