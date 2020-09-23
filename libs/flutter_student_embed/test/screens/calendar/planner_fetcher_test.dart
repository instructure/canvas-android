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
import 'package:built_value/json_object.dart';
import 'package:flutter/material.dart';
import 'package:flutter_student_embed/models/calendar_filter.dart';
import 'package:flutter_student_embed/models/plannable.dart';
import 'package:flutter_student_embed/models/planner_item.dart';
import 'package:flutter_student_embed/models/planner_submission.dart';
import 'package:flutter_student_embed/models/serializers.dart';
import 'package:flutter_student_embed/network/api/planner_api.dart';
import 'package:flutter_student_embed/screens/calendar/planner_fetcher.dart';
import 'package:flutter_student_embed/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_student_embed/utils/db/calendar_filter_db.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../testutils/mock_helpers.dart';
import '../../testutils/test_app.dart';

void main() {
  PlannerApi api = MockPlannerApi();
  CalendarFilterDb filterDb = MockCalendarFilterDb();

  final String userDomain = 'user_domain';
  final String userId = 'user_123';
  final Set<String> contexts = {'course_123'};

  setupTestLocator((locator) {
    locator.registerLazySingleton<PlannerApi>(() => api);
    locator.registerLazySingleton<CalendarFilterDb>(() => filterDb);
  });

  setUp(() {
    // Reset APi mock
    reset(api);

    // Reset db mock
    reset(filterDb);
    when(filterDb.getForUser(any, any)).thenAnswer((_) async {
      return CalendarFilter((b) => b
        ..userDomain = userDomain
        ..userId = userId
        ..filters = SetBuilder(contexts));
    });
  });

  test('fetches month for date', () async {
    final date = DateTime(2000, 1, 15); // Jan 15 2000
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain, fetchFirst: date);

    fetcher.getSnapshotForDate(date);
    await untilCalled(
      api.getUserPlannerItems(any, any, any, contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    verify(
      api.getUserPlannerItems(
        userId,
        DateTime(2000), // Start of day Jan 1 2000
        DateTime(2000, 1, 31, 23, 59, 59, 999), // End of day Jan 31 2000
        contexts: contexts.toList(),
        forceRefresh: false,
      ),
    );
  });

  test('fetchItems excludes announcements', () async {
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain);

    var inputItems = [
      _createPlannerItem(plannableType: 'announcement', contextName: ''),
      _createPlannerItem(plannableType: 'assignment', contextName: ''),
      _createPlannerItem(plannableType: 'plannable_note', contextName: ''),
    ];

    when(api.getUserPlannerItems(any, any, any, contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((realInvocation) async => inputItems);

    var expected = inputItems.sublist(1);
    var actual = await fetcher.fetchItems(DateTime.now(), DateTime.now(), [], false);

    expect(actual, expected);
  });

  test('fetches specified fetchFirst date', () async {
    final date = DateTime.now();
    final userId = 'user_123';
    final contexts = {'course_123'};
    PlannerFetcher(userId: userId, userDomain: userDomain, fetchFirst: date);

    await untilCalled(
      api.getUserPlannerItems(any, any, any, contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    verify(
      api.getUserPlannerItems(
        userId,
        date.withStartOfMonth(),
        date.withEndOfMonth(),
        contexts: contexts.toList(),
        forceRefresh: false,
      ),
    );
  });

  test('does not perform fetch if snapshot exists for day', () {
    final date = DateTime.now();
    final fetcher = PlannerFetcher(userId: '', userDomain: userDomain);

    final expectedSnapshot = AsyncSnapshot<List<PlannerItem>>.nothing();
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = expectedSnapshot;

    final snapshot = fetcher.getSnapshotForDate(date);

    expect(snapshot, expectedSnapshot);

    verifyNever(
      api.getUserPlannerItems(
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
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain);

    final existingSnapshot = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, []);
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = existingSnapshot;

    fetcher.refreshItemsForDate(date);

    await untilCalled(
      api.getUserPlannerItems(any, any, any, contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    verify(
      api.getUserPlannerItems(
        userId,
        date.withStartOfDay(),
        date.withEndOfDay(),
        contexts: contexts.toList(),
        forceRefresh: true,
      ),
    );
  });

  test('refreshes single day if day has failed', () async {
    final date = DateTime.now();
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain);

    final failedSnapshot = AsyncSnapshot<List<PlannerItem>>.withError(ConnectionState.done, Error());
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = failedSnapshot;
    fetcher.failedMonths[fetcher.monthKeyForDate(date)] = false;

    fetcher.refreshItemsForDate(date);

    await untilCalled(
      api.getUserPlannerItems(any, any, any, contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    verify(
      api.getUserPlannerItems(
        userId,
        date.withStartOfDay(),
        date.withEndOfDay(),
        contexts: contexts.toList(),
        forceRefresh: true,
      ),
    );
  });

  test('refreshes entire month if month has failed for day', () async {
    final date = DateTime.now();
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain);

    final failedSnapshot = AsyncSnapshot<List<PlannerItem>>.withError(ConnectionState.done, Error());
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = failedSnapshot;
    fetcher.failedMonths[fetcher.monthKeyForDate(date)] = true;

    fetcher.refreshItemsForDate(date);

    await untilCalled(
      api.getUserPlannerItems(any, any, any, contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')),
    );

    verify(
      api.getUserPlannerItems(
        userId,
        date.withStartOfMonth(),
        date.withEndOfMonth(),
        contexts: contexts.toList(),
        forceRefresh: true,
      ),
    );
  });

  test('sets error snapshot if refresh fails', () async {
    final date = DateTime.now();
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain);

    when(api.getUserPlannerItems(any, any, any, contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) async => throw Error);

    final existingSnapshot = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, []);
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = existingSnapshot;

    await fetcher.refreshItemsForDate(date);
    final snapshot = fetcher.getSnapshotForDate(date);

    expect(snapshot.hasError, isTrue);
  });

  test('reset clears data and notifies listeners', () {
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain);
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

  test('setContexts calls insertOrUpdate on database, resets fetcher, and notifies listeners', () async {
    final fetcher = PlannerFetcher(userId: userId, userDomain: userDomain);
    fetcher.daySnapshots['ABC'] = null;
    fetcher.failedMonths['JAN'] = true;

    int notifyCount = 0;
    fetcher.addListener(() {
      notifyCount++;
    });

    expect(fetcher.userId, userId);
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
      ..filters = SetBuilder(newContexts));
    verify(filterDb.insertOrUpdate(expectedFilterData));
  });
}

Plannable _createPlannable({String title, DateTime dueAt, double pointsPossible, String assignmentId}) =>
    Plannable((b) => b
      ..id = ''
      ..title = title ?? ''
      ..pointsPossible = pointsPossible
      ..dueAt = dueAt
      ..toDoDate = dueAt
      ..assignmentId = assignmentId);

PlannerItem _createPlannerItem(
    {String contextName,
      Plannable plannable,
      String plannableType,
      PlannerSubmission submission,
      String htmlUrl}) =>
    PlannerItem((b) => b
      ..courseId = ''
      ..plannable = plannable != null ? plannable.toBuilder() : _createPlannable().toBuilder()
      ..contextType = ''
      ..contextName = contextName
      ..plannableType = plannableType ?? 'assignment'
      ..plannableDate = DateTime.now().toUtc()
      ..htmlUrl = htmlUrl ?? ''
      ..submissionStatusRaw = submission != null ? JsonObject(serialize(submission)) : null);
