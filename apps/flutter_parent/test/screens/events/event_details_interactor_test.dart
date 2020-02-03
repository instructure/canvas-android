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
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/screens/events/event_details_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';

void main() {
  // Setup
  final eventsApi = _MockEventsApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<CalendarEventsApi>(() => eventsApi);
  });

  setUp(() {
    reset(eventsApi);
  });

  // Start tests
  test('loadEvent calls api', () {
    final itemId = 'id';
    final interactor = EventDetailsInteractor();
    interactor.loadEvent(itemId, true);
    verify(eventsApi.getEvent(itemId, true)).called(1);
  });
}

class _MockEventsApi extends Mock implements CalendarEventsApi {}
