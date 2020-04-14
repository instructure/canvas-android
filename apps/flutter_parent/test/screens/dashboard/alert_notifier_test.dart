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
import 'package:built_value/json_object.dart';
import 'package:flutter_parent/models/unread_count.dart';
import 'package:flutter_parent/network/api/alert_api.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';

void main() {
  final api = MockAlertsApi();

  setUp(() {
    reset(api);
  });

  setupTestLocator((locator) {
    locator.registerLazySingleton<AlertsApi>(() => api);
  });

  test('calls the API with the provided student id', () async {
    final studentId = '123';
    final count = 4;
    final notifier = AlertCountNotifier();

    when(api.getUnreadCount(studentId)).thenAnswer((_) async => UnreadCount((b) => b..count = JsonObject(count)));
    expect(notifier.value, 0);
    await notifier.update(studentId);
    expect(notifier.value, count);

    verify(api.getUnreadCount(studentId)).called(1);
  });

  test('handles null responses', () async {
    final studentId = '123';
    final count = 4;
    final notifier = AlertCountNotifier()..value = count;

    when(api.getUnreadCount(studentId)).thenAnswer((_) async => UnreadCount((b) => b..count = JsonObject('bad')));
    expect(notifier.value, count);
    await notifier.update(studentId);
    expect(notifier.value, count);
  });
}
