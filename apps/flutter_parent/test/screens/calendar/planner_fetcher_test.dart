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

import 'package:flutter/cupertino.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/network/api/planner_api.dart';
import 'package:flutter_parent/screens/calendar/planner_fetcher.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';

void main() {
  PlannerApi api = _MockPlannerApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<PlannerApi>(() => api);
  });

  setUp(() {
    reset(api);
  });

  test('fetches month for date', () {
    final date = DateTime(2000, 1, 15); // Jan 15 2000
    final userId = 'user_123';
    final contexts = ['course_123'];
    final fetcher = PlannerFetcher(userId: userId, contexts: contexts, fetchFirst: date);

    fetcher.getSnapshotForDate(date);
    verify(
      api.getUserPlannerItems(
        userId,
        DateTime(2000), // Start of day Jan 1 2000
        DateTime(2000, 1, 31, 23, 59, 59, 999), // End of day Jan 31 2000
        contexts: contexts,
        forceRefresh: false,
      ),
    );
  });

  test('fetches specified fetchFirst date', () {
    final date = DateTime.now();
    final userId = 'user_123';
    final contexts = ['course_123'];
    PlannerFetcher(userId: userId, contexts: contexts, fetchFirst: date);
    verify(
      api.getUserPlannerItems(
        userId,
        date.withStartOfMonth(),
        date.withEndOfMonth(),
        contexts: contexts,
        forceRefresh: false,
      ),
    );
  });

  test('does not perform fetch if snapshot exists for day', () {
    final date = DateTime.now();
    final fetcher = PlannerFetcher(userId: '');

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

  test('refreshes single day if day has data', () {
    final date = DateTime.now();
    final userId = 'user_123';
    final contexts = ['course_123'];
    final fetcher = PlannerFetcher(userId: userId, contexts: contexts);

    final existingSnapshot = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, []);
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = existingSnapshot;

    fetcher.refreshItemsForDate(date);

    verify(
      api.getUserPlannerItems(
        userId,
        date.withStartOfDay(),
        date.withEndOfDay(),
        contexts: contexts,
        forceRefresh: true,
      ),
    );
  });

  test('refreshes single day if day has failed', () {
    final date = DateTime.now();
    final userId = 'user_123';
    final contexts = ['course_123'];
    final fetcher = PlannerFetcher(userId: userId, contexts: contexts);

    final failedSnapshot = AsyncSnapshot<List<PlannerItem>>.withError(ConnectionState.done, Error());
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = failedSnapshot;
    fetcher.failedMonths[fetcher.monthKeyForDate(date)] = false;

    fetcher.refreshItemsForDate(date);

    verify(
      api.getUserPlannerItems(
        userId,
        date.withStartOfDay(),
        date.withEndOfDay(),
        contexts: contexts,
        forceRefresh: true,
      ),
    );
  });

  test('refreshes entire month if month has failed for day', () {
    final date = DateTime.now();
    final userId = 'user_123';
    final contexts = ['course_123'];
    final fetcher = PlannerFetcher(userId: userId, contexts: contexts);

    final failedSnapshot = AsyncSnapshot<List<PlannerItem>>.withError(ConnectionState.done, Error());
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = failedSnapshot;
    fetcher.failedMonths[fetcher.monthKeyForDate(date)] = true;

    fetcher.refreshItemsForDate(date);

    verify(
      api.getUserPlannerItems(
        userId,
        date.withStartOfMonth(),
        date.withEndOfMonth(),
        contexts: contexts,
        forceRefresh: true,
      ),
    );
  });

  test('sets error snapshot if refresh fails', () async {
    final date = DateTime.now();
    final userId = 'user_123';
    final contexts = ['course_123'];
    final fetcher = PlannerFetcher(userId: userId, contexts: contexts);

    when(api.getUserPlannerItems(any, any, any, contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) async => throw Error);

    final existingSnapshot = AsyncSnapshot<List<PlannerItem>>.withData(ConnectionState.done, []);
    fetcher.daySnapshots[fetcher.dayKeyForDate(date)] = existingSnapshot;

    await fetcher.refreshItemsForDate(date);
    final snapshot = fetcher.getSnapshotForDate(date);

    expect(snapshot.hasError, isTrue);
  });
}

class _MockPlannerApi extends Mock implements PlannerApi {}
