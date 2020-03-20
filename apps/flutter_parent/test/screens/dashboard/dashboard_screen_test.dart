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

import 'package:built_value/json_object.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/help_link.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/unread_count.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/alert_api.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/network/api/planner_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:flutter_parent/screens/alerts/alerts_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_screen.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/screens/courses/courses_screen.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_screen.dart';
import 'package:flutter_parent/screens/dashboard/inbox_notifier.dart';
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:flutter_parent/screens/dashboard/student_expansion_widget.dart';
import 'package:flutter_parent/screens/help/help_screen.dart';
import 'package:flutter_parent/screens/help/help_screen_interactor.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_interactor.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_screen.dart';
import 'package:flutter_parent/screens/settings/settings_interactor.dart';
import 'package:flutter_parent/screens/settings/settings_screen.dart';
import 'package:flutter_parent/utils/common_widgets/badges.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/logger.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/network_image_response.dart';
import '../../utils/test_app.dart';
import '../../utils/test_utils.dart';

void main() {
  mockNetworkImageResponse();
  _setupLocator({MockInteractor interactor, AlertsApi alertsApi, InboxApi inboxApi}) {
    final _locator = GetIt.instance;
    _locator.reset();
    _locator.registerFactory<AlertsInteractor>(() => MockAlertsInteractor());
    _locator.registerFactory<CoursesInteractor>(() => MockCoursesInteractor());
    _locator.registerFactory<DashboardInteractor>(() => interactor ?? MockInteractor());
    _locator.registerFactory<HelpScreenInteractor>(() => MockHelpScreenInteractor());
    _locator.registerFactory<ManageStudentsInteractor>(() => MockManageStudentsInteractor());
    _locator.registerFactory<SettingsInteractor>(() => SettingsInteractor());
    _locator.registerLazySingleton<AlertsApi>(() => alertsApi ?? AlertsApiMock());
    _locator.registerLazySingleton<AlertCountNotifier>(() => AlertCountNotifier());
    _locator.registerLazySingleton<InboxApi>(() => inboxApi ?? MockInboxApi());
    _locator.registerLazySingleton<InboxCountNotifier>(() => InboxCountNotifier());
    _locator.registerLazySingleton<PlannerApi>(() => MockPlannerApi());
    _locator.registerLazySingleton<QuickNav>(() => QuickNav());
    _locator.registerLazySingleton<SelectedStudentNotifier>(() => SelectedStudentNotifier());
    _locator.registerLazySingleton<Logger>(() => Logger());
  }

  setUpAll(() => setupPlatformChannels());

  Widget _testableMaterialWidget([Widget widget]) => TestApp(
        Scaffold(body: widget ?? DashboardScreen()),
        highContrast: true,
      );

  group('Render', () {
    testWidgetsWithAccessibilityChecks('Displays name with pronouns when pronouns are not null', (tester) async {
      _setupLocator(interactor: MockInteractor(includePronouns: true));

      // Get the first user
      var interactor = GetIt.instance.get<DashboardInteractor>();
      User first;
      interactor.getStudents().then((students) {
        first = students.first;
      });

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      expect(find.text('${first.shortName} (${first.pronouns})'), findsOneWidget);
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

      // Will find two, one in the navbar header and one in the student switcher
      expect(find.text('${first.shortName}'), findsNWidgets(2));
    });

    // TODO: Finish when we have specs
//  testWidgetsWithAccessibilityChecks('Displays error when retrieving students results in a failure',
//      (tester) async {
//
//  });

    testWidgetsWithAccessibilityChecks('Nav drawer displays observer name (w/pronouns), and email address',
        (tester) async {
      _setupLocator(interactor: MockInteractor(includePronouns: true));

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

    testWidgetsWithAccessibilityChecks('Inbox count of zero hides badge', (tester) async {
      final inboxApi = MockInboxApi();
      var interactor = MockInteractor();
      when(inboxApi.getUnreadCount()).thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject('0'))));
      _setupLocator(interactor: interactor, inboxApi: inboxApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      DashboardScreen.scaffoldKey.currentState.openDrawer();
      await tester.pumpAndSettle();

      // Assert there's no text in the inbox-count
      expect(find.descendant(of: find.byKey(Key('inbox-count')), matching: find.byType(Text)), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('Displays Inbox count', (tester) async {
      final inboxApi = MockInboxApi();
      var interactor = MockInteractor();
      when(inboxApi.getUnreadCount())
          .thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject('12321'))));
      _setupLocator(interactor: interactor, inboxApi: inboxApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      DashboardScreen.scaffoldKey.currentState.openDrawer();
      await tester.pumpAndSettle();

      expect(find.text('12321'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Displays Inbox count on app bar', (tester) async {
      final inboxCount = '12';

      final inboxApi = MockInboxApi();
      var interactor = MockInteractor();
      when(inboxApi.getUnreadCount())
          .thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject(inboxCount))));
      _setupLocator(interactor: interactor, inboxApi: inboxApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      expect(find.descendant(of: find.byType(AppBar), matching: find.byKey(NumberBadge.backgroundKey)), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Does not display Inbox count on app bar when 0', (tester) async {
      final inboxCount = '0';

      final inboxApi = MockInboxApi();
      var interactor = MockInteractor();
      when(inboxApi.getUnreadCount())
          .thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject(inboxCount))));
      _setupLocator(interactor: interactor, inboxApi: inboxApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      expect(find.descendant(of: find.byType(AppBar), matching: find.byKey(NumberBadge.backgroundKey)), findsNothing);
    });
  });

  group('Interactions', () {
    testWidgetsWithAccessibilityChecks('tapping courses in the bottom nav shows courses screen', (tester) async {
      _setupLocator();

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Courses is the default content screen, so we'll navigate away from there, then try navigating
      // back

      // Navigate to Alerts
      await tester.tap(find.text(AppLocalizations().alertsLabel));
      await tester.pumpAndSettle();

      // Navigate to Courses
      await tester.tap(find.text(AppLocalizations().coursesLabel));
      await tester.pumpAndSettle();

      expect(find.byType(CoursesScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('tapping calendar sets correct current page index', (tester) async {
      _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      await ApiPrefs.addLogin(login);
      await ApiPrefs.switchLogins(login);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Navigate to Calendar
      await tester.tap(find.text(AppLocalizations().calendarLabel));

      // Wait for day activity dot animation delay to settle
      await tester.pumpAndSettle(Duration(seconds: 1));

      expect(find.byType(CalendarScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('tapping alerts sets correct current page index', (tester) async {
      _setupLocator();

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Navigate to Alerts
      await tester.tap(find.text(AppLocalizations().alertsLabel));
      await tester.pumpAndSettle();

      expect(find.byType(AlertsScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('tapping Inbox from nav drawer opens inbox page', (tester) async {
      _setupLocator();

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      DashboardScreen.scaffoldKey.currentState.openDrawer();
      await tester.pumpAndSettle();

      // Click on Inbox
      await tester.tap(find.text(AppLocalizations().inbox));
    });

    testWidgetsWithAccessibilityChecks('tapping Manage Students from nav drawer opens manage students page',
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

    testWidgetsWithAccessibilityChecks('tapping Settings in nav drawer opens settings screen', (tester) async {
      _setupLocator();

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      DashboardScreen.scaffoldKey.currentState.openDrawer();
      await tester.pumpAndSettle();

      // Click on Settings
      var settingsFinder = find.text(AppLocalizations().settings);
      await ensureVisibleByScrolling(settingsFinder, tester, scrollFrom: ScreenVerticalLocation.MID_BOTTOM);
      await tester.pumpAndSettle();
      await tester.tap(settingsFinder);
      await tester.pumpAndSettle();

      // Test that settings screen was loaded
      expect(find.byType(SettingsScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('tapping Help from nav drawer shows help', (tester) async {
      _setupLocator(interactor: MockInteractor());

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      DashboardScreen.scaffoldKey.currentState.openDrawer();
      await tester.pumpAndSettle();

      // Click on Help
      var helpFinder = find.text(AppLocalizations().help);
      await ensureVisibleByScrolling(helpFinder, tester, scrollFrom: ScreenVerticalLocation.MID_BOTTOM);
      await tester.pumpAndSettle();
      await tester.tap(helpFinder);
      await tester.pump();
      await tester.pump();

      expect(find.byType(HelpScreen), findsOneWidget);
    });

    testWidgets(
      'tapping Sign Out from nav drawer displays confirmation dialog',
      (tester) async {
        final reminderDb = _MockReminderDb();
        final notificationUtil = _MockNotificationUtil();

        _setupLocator();
        final _locator = GetIt.instance;
        _locator.registerLazySingleton<ReminderDb>(() => reminderDb);
        _locator.registerLazySingleton<NotificationUtil>(() => notificationUtil);

        when(reminderDb.getAllForUser(any, any)).thenAnswer((_) async => []);

        var login = Login((b) => b
          ..domain = 'domain'
          ..accessToken = 'token'
          ..user = CanvasModelTestUtils.mockUser().toBuilder());

        await ApiPrefs.addLogin(login);
        await ApiPrefs.switchLogins(login);

        expect(ApiPrefs.isLoggedIn(), true);

        await tester.pumpWidget(_testableMaterialWidget());
        await tester.pumpAndSettle();

        // Open the nav drawer
        DashboardScreen.scaffoldKey.currentState.openDrawer();
        await tester.pumpAndSettle();

        // Click on Sign Out
        var logoutFinder = find.text(AppLocalizations().logOut);
        await ensureVisibleByScrolling(logoutFinder, tester, scrollFrom: ScreenVerticalLocation.MID_BOTTOM);
        await tester.pumpAndSettle();
        await tester.tap(logoutFinder);
        await tester.pumpAndSettle();

        // Should show logout confirmation text
        expect(find.text(AppLocalizations().logoutConfirmation), findsOneWidget);

        // Tap the cancel button
        await tester.tap(find.text(DefaultMaterialLocalizations().cancelButtonLabel));
        await tester.pumpAndSettle();

        // Dialog should be gone and we should still be logged in on the dashboard screen
        expect(find.text(AppLocalizations().logoutConfirmation), findsNothing);
        expect(find.byType(DashboardScreen), findsOneWidget);
        expect(ApiPrefs.isLoggedIn(), true);
      },
    );

    // Not using the accessibility tester due to an issue where the
    // Login Landing screen fails a contrast ratio test after logging out
    // (the tests for that screen all pass accessibility checks, however)
    testWidgets('tapping Sign Out from nav drawer signs user out and returns to the Login Landing screen',
        (tester) async {
      final reminderDb = _MockReminderDb();
      final calendarFilterDb = _MockCalendarFilterDb();
      final notificationUtil = _MockNotificationUtil();

      _setupLocator();
      final _locator = GetIt.instance;
      _locator.registerLazySingleton<ReminderDb>(() => reminderDb);
      _locator.registerLazySingleton<CalendarFilterDb>(() => calendarFilterDb);
      _locator.registerLazySingleton<NotificationUtil>(() => notificationUtil);

      when(reminderDb.getAllForUser(any, any)).thenAnswer((_) async => []);

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      await ApiPrefs.addLogin(login);
      await ApiPrefs.switchLogins(login);

      expect(ApiPrefs.isLoggedIn(), true);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      DashboardScreen.scaffoldKey.currentState.openDrawer();
      await tester.pumpAndSettle();

      // Click on Sign Out
      var logoutFinder = find.text(AppLocalizations().logOut);
      await ensureVisibleByScrolling(logoutFinder, tester, scrollFrom: ScreenVerticalLocation.MID_BOTTOM);
      await tester.pumpAndSettle();
      await tester.tap(logoutFinder);
      await tester.pumpAndSettle();

      // Tap the OK button in the confirmation dialog
      await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
      await tester.pumpAndSettle();

      // Test if we ended up on the Login Landing page and if we are logged out
      expect(find.byType(LoginLandingScreen), findsOneWidget);
      expect(ApiPrefs.isLoggedIn(), false);
    });

    testWidgetsWithAccessibilityChecks('tapping selected user opens and closes student selector', (tester) async {
      // Animation values
      int retracted = 0;
      int expanded = 1;

      _setupLocator(interactor: MockInteractor(includePronouns: false));

      // Get the first user
      var interactor = GetIt.instance.get<DashboardInteractor>();
      User first;
      interactor.getStudents().then((students) {
        first = students.first;
      });

      // Load the screen
      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Widget tree is built, we should have access to what we need to check the state of the expandable widget animation now
      var slideAnimation =
          ((StudentExpansionWidget.expansionWidgetKey.currentWidget as SizeTransition).listenable as CurvedAnimation)
              .parent;

      // Make sure the expansion widget exists and is initially retracted
      expect(find.byType(StudentExpansionWidget), findsOneWidget);
      expect(slideAnimation.value, retracted);

      // Tap the user header, expanding it
      // There will be two instances, one in the header and one in the student switcher
      // we want to tap the first one (the one in the header)
      await tester.tap(find.text(first.shortName).at(0));
      await tester.pumpAndSettle(); // Wait for user switcher to slide out
      expect(slideAnimation.value, expanded);

      // Tap the user header, retracting it
      // There will be two instances, one in the header and one in the student switcher
      // we want to tap the first one (the one in the header)
      await tester.tap(find.text(first.shortName).at(0));
      await tester.pumpAndSettle(); // Wait for user switcher to slide back
      expect(slideAnimation.value, retracted);
    });

    testWidgetsWithAccessibilityChecks('tapping student from student selector closes selector', (tester) async {
      // Animation values
      int retracted = 0;
      int expanded = 1;

      _setupLocator(interactor: MockInteractor(includePronouns: false));

      // Get the first user
      var interactor = GetIt.instance.get<DashboardInteractor>();
      User first;
      User second;
      interactor.getStudents().then((students) {
        first = students.first;
        second = students[1];
      });

      // Load the screen
      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Widget tree is built, we should have access to what we need to check the state of the expandable widget animation now
      var slideAnimation =
          ((StudentExpansionWidget.expansionWidgetKey.currentWidget as SizeTransition).listenable as CurvedAnimation)
              .parent;

      // Make sure the expansion widget exists and is initially retracted
      expect(find.byType(StudentExpansionWidget), findsOneWidget);
      expect(slideAnimation.value, retracted);

      // Tap the user header, expanding it
      // There will be two instances, one in the header and one in the student switcher
      // we want to tap the first one (the one in the header)
      await tester.tap(find.text(first.shortName).at(0));
      await tester.pumpAndSettle(); // Wait for user switcher to slide out
      expect(slideAnimation.value, expanded);

      // Tap on a user
      await tester.tap(find.text(second.shortName));
      await tester.pumpAndSettle(); // Wait for user switcher to slide back

      expect(slideAnimation.value, retracted);
    });
  });

  group('Loading', () {
    testWidgetsWithAccessibilityChecks('Initiates call to update inbox count', (tester) async {
      final inboxApi = MockInboxApi();
      var interactor = MockInteractor();
      _setupLocator(interactor: interactor, inboxApi: inboxApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      DashboardScreen.scaffoldKey.currentState.openDrawer();
      await tester.pumpAndSettle();

      verify(inboxApi.getUnreadCount()).called(1);
    });

    testWidgetsWithAccessibilityChecks('Updates Inbox count', (tester) async {
      final inboxApi = MockInboxApi();
      var interactor = MockInteractor();
      when(inboxApi.getUnreadCount())
          .thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject('12321'))));
      _setupLocator(interactor: interactor, inboxApi: inboxApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      DashboardScreen.scaffoldKey.currentState.openDrawer();
      await tester.pumpAndSettle();

      when(inboxApi.getUnreadCount())
          .thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject('78987'))));

      interactor.getInboxCountNotifier().update();
      await tester.pumpAndSettle();

      expect(find.text('78987'), findsOneWidget);
    });
  });

  group('alert badge', () {
    testWidgetsWithAccessibilityChecks('Initiates call to update alerts count', (tester) async {
      final alertsApi = MockAlertsApi();
      var interactor = MockInteractor();
      _setupLocator(interactor: interactor, alertsApi: alertsApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      DashboardScreen.scaffoldKey.currentState.openDrawer();
      await tester.pumpAndSettle();

      verify(alertsApi.getUnreadCount(any)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Inbox count of zero hides badge', (tester) async {
      final alertsApi = MockAlertsApi();
      var interactor = MockInteractor();
      when(alertsApi.getUnreadCount(any)).thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject(0))));
      _setupLocator(interactor: interactor, alertsApi: alertsApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      DashboardScreen.scaffoldKey.currentState.openDrawer();
      await tester.pumpAndSettle();

      // Assert there's no text in the alerts-count
      expect(find.descendant(of: find.byKey(Key('alerts-count')), matching: find.byType(Text)), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('Displays Inbox count', (tester) async {
      final alertsApi = MockAlertsApi();
      var interactor = MockInteractor();
      when(alertsApi.getUnreadCount(any))
          .thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject(88))));
      _setupLocator(interactor: interactor, alertsApi: alertsApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      DashboardScreen.scaffoldKey.currentState.openDrawer();
      await tester.pumpAndSettle();

      expect(find.text('88'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Updates Inbox count', (tester) async {
      final alertsApi = MockAlertsApi();
      var interactor = MockInteractor();
      when(alertsApi.getUnreadCount(any))
          .thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject(88))));
      _setupLocator(interactor: interactor, alertsApi: alertsApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      DashboardScreen.scaffoldKey.currentState.openDrawer();
      await tester.pumpAndSettle();

      when(alertsApi.getUnreadCount(any))
          .thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject(77))));

      interactor.getAlertCountNotifier().update('doesn\'t matter');
      await tester.pumpAndSettle();

      expect(find.text('77'), findsOneWidget);
    });
  });
}

class MockHelpScreenInteractor extends HelpScreenInteractor {
  @override
  Future<List<HelpLink>> getObserverCustomHelpLinks({bool forceRefresh = false}) => Future.value(<HelpLink>[]);
}

class MockAlertsInteractor extends AlertsInteractor {}

class AlertsApiMock extends Mock implements AlertsApi {}

class MockInteractor extends DashboardInteractor {
  bool includePronouns;
  bool generateStudents;
  bool generateSelf;

  MockInteractor({this.includePronouns = false, this.generateStudents = true, this.generateSelf = true});

  @override
  Future<List<User>> getStudents({bool forceRefresh = false}) async => generateStudents
      ? [
          CanvasModelTestUtils.mockUser(
              name: 'Billy Name', shortName: 'Billy', pronouns: includePronouns ? 'he/him' : null),
          CanvasModelTestUtils.mockUser(
              name: 'Sally Name', shortName: 'Sally', pronouns: includePronouns ? 'she/her' : null),
          CanvasModelTestUtils.mockUser(
              name: 'Trevor Name', shortName: 'Trevor', pronouns: includePronouns ? 'he/him' : null),
        ]
      : [];

  @override
  Future<User> getSelf({app}) async => generateSelf
      ? CanvasModelTestUtils.mockUser(
          name: 'Marlene Name',
          shortName: 'Marlene',
          pronouns: includePronouns ? 'she/her' : null,
          primaryEmail: 'marlene@instructure.com')
      : null;
}

class MockInboxApi extends Mock implements InboxApi {}

class MockAlertsApi extends Mock implements AlertsApi {}

class MockCoursesInteractor extends CoursesInteractor {
  @override
  Future<List<Course>> getCourses({bool isRefresh = false}) async {
    var courses = List<Course>();
    return courses;
  }
}

class MockManageStudentsInteractor extends ManageStudentsInteractor {
  @override
  Future<List<User>> getStudents({bool forceRefresh = false}) => Future.value([]);
}

class _MockReminderDb extends Mock implements ReminderDb {}

class _MockCalendarFilterDb extends Mock implements CalendarFilterDb {}

class _MockNotificationUtil extends Mock implements NotificationUtil {}

class MockPlannerApi extends Mock implements PlannerApi {}
