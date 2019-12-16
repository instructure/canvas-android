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

import 'dart:math';

import 'package:flutter/material.dart';
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
import 'package:flutter_parent/screens/manage_students/manage_students_interactor.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_screen.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/network_image_response.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';

void main() {
  mockNetworkImageResponse();
  _setupLocator([MockInteractor interactor]) {
    final _locator = GetIt.instance;
    _locator.reset();
    _locator.registerFactory<DashboardInteractor>(() => interactor ?? MockInteractor());
    _locator.registerFactory<CoursesInteractor>(() => MockCoursesInteractor());
    _locator.registerFactory<AlertsInteractor>(() => MockAlertsInteractor());
    _locator.registerFactory<ManageStudentsInteractor>(() => MockManageStudentsInteractor());
    _locator.registerLazySingleton<AlertsApi>(() => AlertsApiMock());
    _locator.registerLazySingleton<QuickNav>(() => QuickNav());
  }

  setUpAll(() => setupPlatformChannels());

  Widget _testableMaterialWidget([Widget widget]) => TestApp(
        Scaffold(body: widget ?? DashboardScreen()),
        highContrast: true,
      );

  testWidgetsWithAccessibilityChecks('Displays name with pronouns when pronouns are not null', (tester) async {
    _setupLocator(MockInteractor(includePronouns: true));

    // Get the first user
    var interactor = GetIt.instance.get<DashboardInteractor>();
    User first;
    interactor.getStudents().then((students) {
      first = students.first;
    });

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    expect(find.text('${first.name} (${first.pronouns})'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays name without pronouns when pronouns are null', (tester) async {
    _setupLocator();

    // Get the first user
    var interactor = GetIt.instance.get<DashboardInteractor>();
    User first;
    interactor.getStudents().then((students) {
      first = students.first;
    });

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    expect(find.text('${first.name}'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays No Students when there are no observees', (tester) async {
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

  testWidgetsWithAccessibilityChecks('Nav drawer displays observer name (w/pronouns), and email address',
      (tester) async {
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

  testWidgetsWithAccessibilityChecks('Nav drawer displays observer name without pronouns, and email address',
      (tester) async {
    _setupLocator();

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
    _setupLocator();

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    expect(find.byType(CoursesScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking courses in the bottom nav shows courses screen', (tester) async {
    _setupLocator();

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

  testWidgetsWithAccessibilityChecks('Clicking alerts sets correct current page index', (tester) async {
    _setupLocator();

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
//    // Open the nav drawer
//    DashboardScreen.scaffoldKey.currentState.openDrawer();
//    await tester.pumpAndSettle();
//
//    // Click on Inbox
//    await tester.tap(find.text(AppLocalizations().inbox));
//
//    // TODO: Test that Inbox screen was loaded
//
//  });

  testWidgetsWithAccessibilityChecks('Clicking Manage Students from nav drawer opens manage students page',
      (tester) async {
    _setupLocator();

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle();

    // Open the nav drawer
    DashboardScreen.scaffoldKey.currentState.openDrawer();
    await tester.pumpAndSettle();

    // Click on Manage Students
    await tester.tap(find.text(AppLocalizations().manageStudents));
    await tester.pumpAndSettle(Duration(seconds: 3));

    // Test that Manage Students screen was loaded
    expect(find.byType(ManageStudentsScreen), findsOneWidget);
  });

//  testWidgetsWithAccessibilityChecks('Clicking Help from nav drawer signs user out', (tester) async {
//    _setupLocator(MockInteractor());
//
//    await tester.pumpWidget(_testableMaterialWidget());
//    await tester.pumpAndSettle();
//
//    // Open the nav drawer
//    DashboardScreen.scaffoldKey.currentState.openDrawer();
//    await tester.pumpAndSettle();
//
//    // Click on Help
//    await tester.tap(find.text(AppLocalizations().help));
//
//    // TODO: Test that Help screen was loaded
//  });

  // Not using the accessibility tester due to an issue where the
  // Login Landing screen fails a contrast ratio test after logging out
  // (the tests for that screen all pass accessibility checks, however)
  testWidgets('Clicking Sign Out from nav drawer signs user out and returns to the Login Landing screen',
      (tester) async {
    _setupLocator();

    await setupPlatformChannels(
        config: PlatformConfig(mockPrefs: {
      ApiPrefs.KEY_ACCESS_TOKEN: 'token',
      ApiPrefs.KEY_DOMAIN: 'domain',
    }));

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
//    _setupLocator();
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
}

class MockAlertsInteractor extends AlertsInteractor {}

class AlertsApiMock extends AlertsApi {}

class MockInteractor extends DashboardInteractor {
  bool includePronouns;
  bool generateStudents;
  bool generateSelf;

  MockInteractor({this.includePronouns = false, this.generateStudents = true, this.generateSelf = true});

  @override
  Future<List<User>> getStudents({bool forceRefresh = false}) async => generateStudents
      ? [
          _mockUser('Billy', pronouns: includePronouns ? 'he/him' : null),
          _mockUser('Sally', pronouns: includePronouns ? 'she/her' : null),
          _mockUser('Trevor', pronouns: includePronouns ? 'he/him' : null),
        ]
      : [];

  @override
  Future<User> getSelf({app}) async => generateSelf
      ? _mockUser('Marlene', pronouns: includePronouns ? 'she/her' : null, primaryEmail: 'marlene@instructure.com')
      : null;
}

class MockCoursesInteractor extends CoursesInteractor {
  @override
  Future<List<Course>> getCourses() async {
    var courses = await [];
    return courses;
  }
}

class MockManageStudentsInteractor extends ManageStudentsInteractor {
  @override
  Future<List<User>> getStudents({bool forceRefresh = false}) => Future.value([]);
}

User _mockUser(String name, {String pronouns, String primaryEmail}) => User((b) => b
  ..id = Random(name.hashCode).nextInt(100000).toString()
  ..sortableName = name
  ..name = name
  ..primaryEmail = primaryEmail ?? null
  ..pronouns = pronouns ?? null
  ..build());
