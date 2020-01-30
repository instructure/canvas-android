// Copyright (C) 2019 - present Instructure, Inc.
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

import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/alert_threshold.dart';
import 'package:flutter_parent/network/api/alert_api.dart';
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';

void main() {
  final api = _MockAlertsApi();

  setupTestLocator((_locator) {
    _locator.registerFactory<AlertsApi>(() => api);
  });

  setUp(() {
    reset(api);
  });

  test('mark alert read calls the api', () {
    final alertId = '123';

    when(api.updateAlertWorkflow(alertId, 'read')).thenAnswer((_) => Future.value(null));

    AlertsInteractor().markAlertRead(alertId);

    verify(api.updateAlertWorkflow(alertId, 'read')).called(1);
  });

  test('get alerts for student returns date sorted list', () async {
    final date = DateTime.now();
    final studentId = '123';
    final data = List.generate(5, (index) {
      // Create a list of alerts with dates in ascending order (reversed)
      return Alert((b) => b
        ..id = index.toString()
        ..actionDate = date.add(Duration(days: index)));
    });

    when(api.getAlertsDepaginated(studentId, false)).thenAnswer((_) => Future.value(data.toList()));

    final actual = await AlertsInteractor().getAlertsForStudent(studentId, false);

    verify(api.getAlertsDepaginated(studentId, false)).called(1);
    expect(actual.alerts, data.reversed.toList()); // Verify that the actual list sorted correctly
  });

  test('get alerts for student returns thresholds', () async {
    final studentId = '123';

    final data = List.generate(5, (index) {
      return AlertThreshold((b) => b..id = '$index');
    });
    when(api.getAlertThresholds(studentId, false)).thenAnswer((_) async => data);

    final actual = await AlertsInteractor().getAlertsForStudent(studentId, false);

    verify(api.getAlertThresholds(studentId, false)).called(1);
    expect(actual.thresholds, data); // Verify that the actual list sorted correctly
  });
}

class _MockAlertsApi extends Mock implements AlertsApi {}
