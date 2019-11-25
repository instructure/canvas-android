/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_parent/api/alert_api.dart';
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:flutter_parent/screens/alerts/alerts_screen.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/screens/courses/courses_screen.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';

void main() {
  _setupLocator(MockInteractor interactor) {
    final _locator = GetIt.instance;
    _locator.reset();
    _locator.registerFactory<DashboardInteractor>(() => interactor);
    _locator.registerFactory<CoursesInteractor>(() => MockCoursesInteractor());
    _locator.registerFactory<AlertsInteractor>(() => MockAlertsInteractor());
    _locator.registerLazySingleton<AlertsApi>(() => AlertsApiMock());
  }

  Widget _testableMaterialWidget([Widget widget]) =>
      TestApp(Scaffold(body: widget ?? DashboardScreen()));

  testWidgetsWithAccessibilityChecks('Displays name with pronouns when pronouns are not null',
      (tester) async {
    _setupLocator(MockInteractor(includePronouns: true));

    // Get the first user
    var interactor = GetIt.instance.get<DashboardInteractor>();
    User first;
    interactor.getObservees().then((students) {
      first = students.first;
    });

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    expect(find.text('${first.name} (${first.pronouns})'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays name without pronouns when pronouns are null',
      (tester) async {
    _setupLocator(MockInteractor());

    // Get the first user
    var interactor = GetIt.instance.get<DashboardInteractor>();
    User first;
    interactor.getObservees().then((students) {
      first = students.first;
    });

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    expect(find.text('${first.name}'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays No Students when there are no observees',
      (tester) async {
    _setupLocator(MockInteractor(generateStudents: false));

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().noStudents), findsOneWidget);
  });

  // TODO: Finish when we have specs
//  testWidgetsWithAccessibilityChecks('Displays error when retrieving students results in a failure',
//      (tester) async {
//
//  });

  testWidgetsWithAccessibilityChecks(
      'Nav drawer displays observer name (w/pronouns), and email address', (tester) async {
    _setupLocator(MockInteractor(includePronouns: true));

    // Get the first user
    var interactor = GetIt.instance.get<DashboardInteractor>();
    User observer;
    interactor.getSelf().then((self) {
      observer = self;
    });

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    // Open the drawer
    DashboardScreen.scaffoldKey.currentState.openDrawer();
    await tester.pumpAndSettle();

    expect(find.text('${observer.name} (${observer.pronouns})'), findsOneWidget);
    expect(find.text('${observer.primaryEmail}'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks(
      'Nav drawer displays observer name without pronouns, and email address', (tester) async {
    _setupLocator(MockInteractor());

    // Get the first user
    var interactor = GetIt.instance.get<DashboardInteractor>();
    User observer;
    interactor.getSelf().then((self) {
      observer = self;
    });

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    // Open the drawer
    DashboardScreen.scaffoldKey.currentState.openDrawer();
    await tester.pumpAndSettle();

    expect(find.text('${observer.name}'), findsOneWidget);
    expect(find.text('${observer.primaryEmail}'), findsOneWidget);
  });

  // TODO: Finish when we have specs
//  testWidgetsWithAccessibilityChecks(
//      'Displays error when retrieving self (observee) results in a failure', (tester) async {
//  });

  testWidgetsWithAccessibilityChecks('Courses is the default content screen', (tester) async {
    _setupLocator(MockInteractor());

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    expect(find.byType(CoursesScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking courses in the bottom nav shows courses screen',
      (tester) async {
    _setupLocator(MockInteractor());

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    // Courses is the default content screen, so we'll navigate away from there, then try navigating
    // back

    // Navigate to Calendar
    await tester.tap(find.text(AppLocalizations().calendarLabel));
    await tester.pumpAndSettle();

    // Navigate to Courses
    await tester.tap(find.text(AppLocalizations().coursesLabel));
    await tester.pumpAndSettle();

    expect(find.byType(CoursesScreen), findsOneWidget);
  });

  // TODO: Uncomment when Calendar gets put in
//  testWidgetsWithAccessibilityChecks(
//      'Clicking calendar sets correct current page index', (tester) async {
//
//    _setupLocator(MockInteractor());
//
//    await tester.pumpWidget(_testableMaterialWidget());
//    await tester.pumpAndSettle();
//
//    // Navigate to Calendar
//    await tester.tap(find.text(AppLocalizations().calendarLabel));
//    await tester.pumpAndSettle();
//
//    expect(find.byType(CalendarScreen), findsOneWidget);
//  });

  testWidgetsWithAccessibilityChecks('Clicking alerts sets correct current page index',
      (tester) async {
    _setupLocator(MockInteractor());

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    // Navigate to Alerts
    await tester.tap(find.text(AppLocalizations().alertsLabel));
    await tester.pumpAndSettle();

    expect(find.byType(AlertsScreen), findsOneWidget);
  });

//  testWidgetsWithAccessibilityChecks('Clicking Inbox from nav drawer opens inbox page', (tester) async {
//    _setupLocator(MockInteractor());
//
//    await tester.pumpWidget(_testableMaterialWidget());
//    await tester.pumpAndSettle();
//
//    // Open the nave drawer
//    DashboardScreen.scaffoldKey.currentState.openDrawer();
//    await tester.pumpAndSettle();
//
//    // Click on Inbox
//    await tester.tap(find.text(AppLocalizations().inbox));
//
//    // TODO: Test that Inbox screen was loaded
//
//  });

//  testWidgetsWithAccessibilityChecks('Clicking Manage Students from nav drawer opens manage students page', (tester) async {
//    _setupLocator(MockInteractor());
//
//    await tester.pumpWidget(_testableMaterialWidget());
//    await tester.pumpAndSettle();
//
//    // Open the nave drawer
//    DashboardScreen.scaffoldKey.currentState.openDrawer();
//    await tester.pumpAndSettle();
//
//    // Click on Manage Students
//    await tester.tap(find.text(AppLocalizations().manageStudents));
//
//    // TODO: Test that Manage Students screen was loaded
//  });

//  testWidgetsWithAccessibilityChecks('Clicking Help from nav drawer signs user out', (tester) async {
//    _setupLocator(MockInteractor());
//
//    await tester.pumpWidget(_testableMaterialWidget());
//    await tester.pumpAndSettle();
//
//    // Open the nave drawer
//    DashboardScreen.scaffoldKey.currentState.openDrawer();
//    await tester.pumpAndSettle();
//
//    // Click on Help
//    await tester.tap(find.text(AppLocalizations().help));
//
//    // TODO: Test that Manage Students screen was loaded
//  });

  // Not using the accessibility tester due to an issue where the
  // Login Landing screen fails a contrast ratio test after logging out
  // (the tests for that screen all pass accessibility checks, however)
  testWidgets(
      'Clicking Sign Out from nav drawer signs user out and returns to the Login Landing screen',
      (tester) async {
    _setupLocator(MockInteractor());

    // Setup prefs and test that we are logged in
    SharedPreferences.setMockInitialValues({
      'flutter.${ApiPrefs.KEY_ACCESS_TOKEN}': 'token',
      'flutter.${ApiPrefs.KEY_DOMAIN}': 'domain',
    });

    await ApiPrefs.init();
    expect(ApiPrefs.isLoggedIn(), true);

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    // Open the nave drawer
    DashboardScreen.scaffoldKey.currentState.openDrawer();
    await tester.pumpAndSettle();

    // Click on Sign Out
    await tester.tap(find.text(AppLocalizations().signOut));
    await tester.pumpAndSettle();

    // Test if we ended up on the Login Landing page and if we are logged out
    expect(find.byType(LoginLandingScreen), findsOneWidget);
    expect(ApiPrefs.isLoggedIn(), false);
  });

//  testWidgetsWithAccessibilityChecks('Updating the inbox notifier value updates in the nav drawer', (tester) async {
//    _setupLocator(MockInteractor());
//
//    await tester.pumpWidget(_testableMaterialWidget());
//    await tester.pumpAndSettle();
//
//    // Open the nave drawer
//    DashboardScreen.scaffoldKey.currentState.openDrawer();
//    await tester.pumpAndSettle();
//
//    // Change inbox notifier value
//    // TODO: Implement when we get the Inbox api up and going
//  });

  setUpAll(() {
    // Setup for package_info
    const MethodChannel channel = MethodChannel('plugins.flutter.io/package_info');
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      switch (methodCall.method) {
        case 'getAll':
          return <String, dynamic>{
            'appName': 'android_parent',
            'buildNumber': '10',
            'packageName': 'com.instructure.parentapp',
            'version': '2.0.0',
          };
        default:
          assert(false);
          return null;
      }
    });
  });
}

class MockAlertsInteractor extends AlertsInteractor {}

class AlertsApiMock extends AlertsApi {}

class MockInteractor extends DashboardInteractor {
  bool includePronouns;
  bool generateStudents;
  bool generateSelf;

  MockInteractor(
      {this.includePronouns = false, this.generateStudents = true, this.generateSelf = true});

  @override
  Future<List<User>> getObservees() async => generateStudents
      ? [
          _mockUser('Billy', pronouns: includePronouns ? 'he/him' : null),
          _mockUser('Sally', pronouns: includePronouns ? 'she/her' : null),
          _mockUser('Trevor', pronouns: includePronouns ? 'he/him' : null),
        ]
      : [];

  @override
  Future<User> getSelf({app}) async => generateSelf
      ? _mockUser('Marlene',
          pronouns: includePronouns ? 'she/her' : null, primaryEmail: 'marlene@instructure.com')
      : null;
}

class MockCoursesInteractor extends CoursesInteractor {
  @override
  Future<List<Course>> getCourses() async {
    var courses = await [];
    return courses;
  }
}

User _mockUser(String name, {String pronouns, String primaryEmail}) => User((b) => b
  ..id = Random(name.hashCode).nextInt(100000)
  ..sortableName = name
  ..name = name
  ..primaryEmail = primaryEmail ?? null
  ..pronouns = pronouns ?? null
  ..build());
