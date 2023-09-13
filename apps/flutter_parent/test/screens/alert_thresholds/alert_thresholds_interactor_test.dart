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

import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/alert_threshold.dart';
import 'package:flutter_parent/network/api/alert_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  AlertThreshold _mockThreshold(AlertType type, {String? value}) => AlertThreshold((b) => b
    ..alertType = type
    ..threshold = value
    ..build());

  final api = MockAlertsApi();
  final enrollmentsApi = MockEnrollmentsApi();

  setupTestLocator((locator) {
    locator.registerFactory<AlertsApi>(() => api);
    locator.registerFactory<EnrollmentsApi>(() => enrollmentsApi);
  });

  setUp(() {
    reset(api);
    reset(enrollmentsApi);
  });

  test('Switch created api call', () async {
    when(api.createThreshold(any, any)).thenAnswer((_) => Future.value(null));

    var type = AlertType.assignmentMissing;
    var alertThreshold = null;
    var studentId = '1234';

    await AlertThresholdsInteractor().updateAlertThreshold(type, studentId, alertThreshold);

    verify(api.createThreshold(type, studentId)).called(1);
  });

  test('Switch deleted api call', () async {
    when(api.deleteAlert(any)).thenAnswer((_) => Future.value(null));

    var type = AlertType.assignmentMissing;
    var alertThreshold = _mockThreshold(type);
    var studentId = '1234';

    await AlertThresholdsInteractor().updateAlertThreshold(alertThreshold.alertType, studentId, alertThreshold);

    verify(api.deleteAlert(alertThreshold)).called(1);
  });

  test('Percentage updated api call', () async {
    when(api.createThreshold(any, any)).thenAnswer((_) => Future.value(null));

    var type = AlertType.courseGradeLow;
    var value = '42';
    var alertThreshold = _mockThreshold(type);
    var studentId = '1234';

    await AlertThresholdsInteractor().updateAlertThreshold(type, studentId, alertThreshold, value: value);

    verify(api.createThreshold(type, studentId, value: value)).called(1);
  });

  test('Percentage deleted api call', () async {
    when(api.deleteAlert(any)).thenAnswer((_) => Future.value(null));

    var type = AlertType.courseGradeLow;
    var value = '-1';
    var alertThreshold = _mockThreshold(type);
    var studentId = '1234';

    await AlertThresholdsInteractor()
        .updateAlertThreshold(alertThreshold.alertType, studentId, alertThreshold, value: value);

    verify(api.deleteAlert(alertThreshold)).called(1);
  });

  test('getAlertThresholdsForStudent calls through to the api', () async {
    var studentId = '1234';

    await AlertThresholdsInteractor().getAlertThresholdsForStudent(studentId, forceRefresh: true);

    verify(api.getAlertThresholds(studentId, true)).called(1);
  });

  test('canDeleteStudent calls through to the api', () async {
    var studentId = '1234';

    await AlertThresholdsInteractor().canDeleteStudent(studentId);

    verify(enrollmentsApi.canUnpairStudent(studentId));
  });

  test('deleteStudent calls through to the api', () async {
    var studentId = '1234';

    await AlertThresholdsInteractor().deleteStudent(studentId);

    verify(enrollmentsApi.unpairStudent(studentId));
  });
}
