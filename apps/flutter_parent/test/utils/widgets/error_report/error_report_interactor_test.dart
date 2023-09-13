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
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/error_report_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../canvas_model_utils.dart';
import '../../platform_config.dart';
import '../../test_app.dart';
import '../../test_helpers/mock_helpers.dart';
import '../../test_helpers/mock_helpers.mocks.dart';

void main() {
  final user = User((b) => b
    ..id = '123'
    ..name = 'UserName'
    ..primaryEmail = 'PrimaryEmail');

  final api = MockErrorReportApi();
  final enrollmentsApi = MockEnrollmentsApi();

  setUp(() async {
    reset(api);
    reset(enrollmentsApi);

    await setupPlatformChannels();
  });

  // Setup test dependencies
  setupTestLocator((locator) {
    locator.registerLazySingleton<ErrorReportApi>(() => api);
    locator.registerLazySingleton<EnrollmentsApi>(() => enrollmentsApi);
  });

  test('Submit error report calls to the api', () async {
    final subject = 'subject';
    final description = 'description';
    final email = 'email';
    final severity = ErrorReportSeverity.COMMENT;
    final stacktrace = 'error stack';

    await ErrorReportInteractor().submitErrorReport(subject, description, email, severity, stacktrace);

    verify(api.submitErrorReport(
      subject: subject,
      description: description,
      email: email,
      stacktrace: stacktrace,
      userRoles: '',
      severity: anyNamed('severity'),
      domain: anyNamed('domain'),
      name: anyNamed('name'),
      becomeUser: anyNamed('becomeUser'),
    )).called(1);
  });

  test('Uses user data from api prefs', () async {
    final login = Login((b) => b..user = user.toBuilder());
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));

    await ErrorReportInteractor().submitErrorReport('', '', '', ErrorReportSeverity.COMMENT, '');

    verify(api.submitErrorReport(
      email: user.primaryEmail,
      name: user.name,
      becomeUser: '${ErrorReportApi.DEFAULT_DOMAIN}?become_user_id=${user.id}',
      subject: anyNamed('subject'),
      description: anyNamed('description'),
      stacktrace: anyNamed('stacktrace'),
      severity: anyNamed('severity'),
      domain: anyNamed('domain'),
      userRoles: anyNamed('userRoles'),
    )).called(1);
  });

  test('Uses empty string for no user data in api prefs', () async {
    final login = Login((b) => b..user = User().toBuilder());
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));

    await ErrorReportInteractor().submitErrorReport('', '', null, ErrorReportSeverity.COMMENT, '');

    verify(api.submitErrorReport(
      email: '',
      name: '',
      becomeUser: '',
      subject: anyNamed('subject'),
      description: anyNamed('description'),
      stacktrace: anyNamed('stacktrace'),
      severity: anyNamed('severity'),
      domain: anyNamed('domain'),
      userRoles: anyNamed('userRoles'),
    )).called(1);
  });

  test('Uses domain from api prefs', () async {
    String domain = 'domain';
    Login user = Login((b) => b
      ..accessToken = ''
      ..refreshToken = ''
      ..domain = domain
      ..user = CanvasModelTestUtils.mockUser().toBuilder());
    ApiPrefs.switchLogins(user);

    await ErrorReportInteractor().submitErrorReport('', '', '', ErrorReportSeverity.COMMENT, '');

    verify(api.submitErrorReport(
      domain: domain,
      subject: anyNamed('subject'),
      description: anyNamed('description'),
      email: anyNamed('email'),
      stacktrace: anyNamed('stacktrace'),
      severity: anyNamed('severity'),
      name: anyNamed('name'),
      becomeUser: anyNamed('becomeUser'),
      userRoles: anyNamed('userRoles'),
    )).called(1);
  });

  test('Uses domain from ErrorReportApi if api prefs has none', () async {
    await ErrorReportInteractor().submitErrorReport('', '', '', ErrorReportSeverity.COMMENT, '');

    verify(api.submitErrorReport(
      domain: anyNamed('domain'),
      subject: anyNamed('subject'),
      description: anyNamed('description'),
      email: anyNamed('email'),
      stacktrace: anyNamed('stacktrace'),
      severity: anyNamed('severity'),
      name: anyNamed('name'),
      becomeUser: anyNamed('becomeUser'),
      userRoles: anyNamed('userRoles'),
    )).called(1);
  });

  test('Retrieves user roles and removes duplicates', () async {
    String domain = 'domain';
    Login user = Login((b) => b
      ..accessToken = ''
      ..refreshToken = ''
      ..domain = domain
      ..user = CanvasModelTestUtils.mockUser().toBuilder());
    ApiPrefs.switchLogins(user);

    final enrollmentBuilder = (String type) => Enrollment((b) => b
      ..enrollmentState = 'active'
      ..type = type);
    when(enrollmentsApi.getSelfEnrollments(forceRefresh: true)).thenAnswer((_) async => List.from([
          enrollmentBuilder('ObserverEnrollment'),
          enrollmentBuilder('ObserverEnrollment'),
          enrollmentBuilder('StudentEnrollment'),
        ]));
    await ErrorReportInteractor().submitErrorReport('', '', '', ErrorReportSeverity.COMMENT, '');

    verify(api.submitErrorReport(
      userRoles: 'ObserverEnrollment,StudentEnrollment',
      subject: anyNamed('subject'),
      description: anyNamed('description'),
      email: anyNamed('email'),
      stacktrace: anyNamed('stacktrace'),
      severity: anyNamed('severity'),
      domain: anyNamed('domain'),
      name: anyNamed('name'),
      becomeUser: anyNamed('becomeUser'),
    )).called(1);
  });

  test('Sends empty user roles with null api prefs user', () async {
    await ErrorReportInteractor().submitErrorReport('', '', '', ErrorReportSeverity.COMMENT, '');

    verifyNever(enrollmentsApi.getSelfEnrollments(forceRefresh: anyNamed('forceRefresh')));

    verify(api.submitErrorReport(
      userRoles: '',
      subject: anyNamed('subject'),
      description: anyNamed('description'),
      email: anyNamed('email'),
      stacktrace: anyNamed('stacktrace'),
      severity: anyNamed('severity'),
      domain: anyNamed('domain'),
      name: anyNamed('name'),
      becomeUser: anyNamed('becomeUser'),
    )).called(1);
  });

  test('ErrorReportSeverity serializes', () async {
    final _verifySeverityString = (String severity) {
      verify(api.submitErrorReport(
        severity: severity,
        subject: anyNamed('subject'),
        description: anyNamed('description'),
        email: anyNamed('email'),
        stacktrace: anyNamed('stacktrace'),
        domain: anyNamed('domain'),
        name: anyNamed('name'),
        becomeUser: anyNamed('becomeUser'),
        userRoles: anyNamed('userRoles'),
      )).called(1);
    };

    await ErrorReportInteractor().submitErrorReport('', '', '', ErrorReportSeverity.COMMENT, '');
    _verifySeverityString('just_a_comment');

    await ErrorReportInteractor().submitErrorReport('', '', '', ErrorReportSeverity.NOT_URGENT, '');
    _verifySeverityString('not_urgent');

    await ErrorReportInteractor().submitErrorReport('', '', '', ErrorReportSeverity.WORKAROUND_POSSIBLE, '');
    _verifySeverityString('workaround_possible');

    await ErrorReportInteractor().submitErrorReport('', '', '', ErrorReportSeverity.BLOCKING, '');
    _verifySeverityString('blocks_what_i_need_to_do');

    await ErrorReportInteractor().submitErrorReport('', '', '', ErrorReportSeverity.CRITICAL, '');
    _verifySeverityString('extreme_critical_emergency');

    expect(() async => await ErrorReportInteractor().submitErrorReport('', '', '', null, ''), throwsArgumentError);
  });
}
