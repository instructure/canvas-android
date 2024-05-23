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

import 'package:built_value/json_object.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/help_link.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/unread_count.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/network/api/alert_api.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/network/api/planner_api.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:flutter_parent/screens/alerts/alerts_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_today_click_notifier.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_widget.dart';
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
import 'package:flutter_parent/screens/masquerade/masquerade_screen.dart';
import 'package:flutter_parent/screens/masquerade/masquerade_screen_interactor.dart';
import 'package:flutter_parent/screens/pairing/pairing_util.dart';
import 'package:flutter_parent/screens/settings/settings_interactor.dart';
import 'package:flutter_parent/screens/settings/settings_screen.dart';
import 'package:flutter_parent/utils/alert_helper.dart';
import 'package:flutter_parent/utils/common_widgets/badges.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/remote_config_utils.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/network_image_response.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';
import '../../utils/test_utils.dart';
import '../courses/course_summary_screen_test.dart';

/**
 * NOTE: This test file is from the before times, please don't use as reference.
 */
void main() {
  mockNetworkImageResponse();
  final analyticsMock = MockAnalytics();
  final alertsHelper = AlertsHelper();

  _setupLocator({MockInteractor? interactor, AlertsApi? alertsApi, InboxApi? inboxApi}) async {
    await setupTestLocator((locator) {
      locator.registerFactory<AlertsInteractor>(() => MockAlertsInteractor());
      locator.registerFactory<CoursesInteractor>(() => MockCoursesInteractor());
      locator.registerFactory<DashboardInteractor>(() => interactor ?? MockInteractor());
      locator.registerFactory<HelpScreenInteractor>(() => MockHelpScreenInteractor());
      locator.registerFactory<ManageStudentsInteractor>(() => MockManageStudentsInteractor());
      locator.registerFactory<MasqueradeScreenInteractor>(() => MasqueradeScreenInteractor());
      locator.registerFactory<SettingsInteractor>(() => SettingsInteractor());
      locator.registerLazySingleton<AlertsApi>(() => alertsApi ?? MockAlertsApi());
      locator.registerLazySingleton<AlertCountNotifier>(() => AlertCountNotifier());
      locator.registerLazySingleton<Analytics>(() => analyticsMock);
      locator.registerLazySingleton<CalendarTodayClickNotifier>(() => CalendarTodayClickNotifier());
      locator.registerLazySingleton<InboxApi>(() => inboxApi ?? MockInboxApi());
      locator.registerLazySingleton<InboxCountNotifier>(() => InboxCountNotifier());
      locator.registerLazySingleton<PlannerApi>(() => MockPlannerApi());
      locator.registerLazySingleton<QuickNav>(() => QuickNav());
      locator.registerLazySingleton<SelectedStudentNotifier>(() => SelectedStudentNotifier());
      locator.registerLazySingleton<StudentAddedNotifier>(() => StudentAddedNotifier());
      locator.registerLazySingleton<AccountsApi>(() => MockAccountsApi());
      locator.registerLazySingleton<AlertsHelper>(() => alertsHelper);
    });
  }

  setUp(() async {
    reset(analyticsMock);
    final mockRemoteConfig = setupMockRemoteConfig(valueSettings: {'qr_login_enabled_parent': 'true'});
    await setupPlatformChannels(config: PlatformConfig(initRemoteConfig: mockRemoteConfig));
  });

  tearDown(() {
    RemoteConfigUtils.clean();
  });

  Widget _testableMaterialWidget({
    Login? initLogin,
    Map<String, Object>? deepLinkParams,
    DashboardContentScreens? startingPage,
  }) =>
      TestApp(
        Scaffold(
          body: DashboardScreen(
            deepLinkParams: deepLinkParams,
            startingPage: startingPage,
          ),
        ),
        platformConfig: PlatformConfig(initLoggedInUser: initLogin),
      );

  group('Render', () {
    testWidgetsWithAccessibilityChecks('Displays name with pronouns when pronouns are not null', (tester) async {
      await _setupLocator(interactor: MockInteractor(includePronouns: true));

      // Get the first user
      var interactor = GetIt.instance.get<DashboardInteractor>();
      late User? first;
      interactor.getStudents().then((students) {
        first = students?.first;
      });

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      expect(find.text('${first?.shortName} (${first?.pronouns})'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Displays name without pronouns when pronouns are null', (tester) async {
      await _setupLocator();

      // Get the first user
      var interactor = GetIt.instance.get<DashboardInteractor>();
      late User? first;
      interactor.getStudents().then((students) {
        first = students?.first;
      });

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Will find two, one in the navbar header and one in the student switcher
      expect(find.text('${first?.shortName}'), findsNWidgets(2));
    });

    testWidgetsWithAccessibilityChecks(
      'Displays empty state when there are no students',
      (tester) async {
        var interactor = MockInteractor(generateStudents: false);
        await _setupLocator(interactor: interactor);

        await tester.pumpWidget(_testableMaterialWidget());
        await tester.pumpAndSettle();

        expect(find.byType(EmptyPandaWidget), findsOneWidget);
        expect(find.text(l10n.emptyStudentList), findsOneWidget);
        expect(find.descendant(of: find.byType(AppBar), matching: find.text(l10n.noStudents)), findsOneWidget);
        expect(
            find.descendant(of: find.byType(EmptyPandaWidget), matching: find.text(l10n.noStudents)), findsOneWidget);
      },
      a11yExclusions: {A11yExclusion.multipleNodesWithSameLabel},
    );

    testWidgetsWithAccessibilityChecks('Does not display Act As User button if user cannot masquerade', (tester) async {
      await _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..canMasquerade = false
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      await tester.pumpWidget(_testableMaterialWidget(initLogin: login));
      await tester.pumpAndSettle();

      // Open the drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      expect(find.text(l10n.actAsUser), findsNothing);
      expect(find.text(l10n.stopActAsUser), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('Displays Act As User button if user can masquerade', (tester) async {
      await _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..canMasquerade = true
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      await tester.pumpWidget(_testableMaterialWidget(initLogin: login));
      await tester.pumpAndSettle();

      // Open the drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      expect(find.text(l10n.actAsUser), findsOneWidget);
      expect(find.text(l10n.stopActAsUser), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('Displays Stop Acting As User button if user is masquerading', (tester) async {
      await _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..canMasquerade = true
        ..user = CanvasModelTestUtils.mockUser().toBuilder()
        ..masqueradeDomain = 'masqueradeDomain'
        ..masqueradeUser = CanvasModelTestUtils.mockUser().toBuilder());

      await tester.pumpWidget(_testableMaterialWidget(initLogin: login));
      await tester.pumpAndSettle();

      // Open the drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      expect(find.text(l10n.actAsUser), findsNothing);
      expect(find.text(l10n.stopActAsUser), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Nav drawer displays observer name (w/pronouns), and email address',
        (tester) async {
      await _setupLocator(interactor: MockInteractor(includePronouns: true));

      // Get the first user
      var interactor = GetIt.instance.get<DashboardInteractor>();
      late User observer;
      interactor.getSelf().then((self) {
        observer = self ?? User();
      });

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      expect(find.text('${observer.name} (${observer.pronouns})'), findsOneWidget);
      expect(find.text('${observer.primaryEmail}'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Nav drawer displays observer name without pronouns, and email address',
        (tester) async {
      await _setupLocator();

      // Get the first user
      var interactor = GetIt.instance.get<DashboardInteractor>();
      late User observer;
      interactor.getSelf().then((self) {
        observer = self ?? User();
      });

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      expect(find.text('${observer.name}'), findsOneWidget);
      expect(find.text('${observer.primaryEmail}'), findsOneWidget);
    });

    // TODO: Finish when we have specs
//  testWidgetsWithAccessibilityChecks(
//      'Displays error when retrieving self (observee) results in a failure', (tester) async {
//  });

    testWidgetsWithAccessibilityChecks('Courses is the default content screen', (tester) async {
      await _setupLocator();

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      expect(find.byType(CoursesScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Inbox count of zero hides badge', (tester) async {
      final inboxApi = MockInboxApi();
      var interactor = MockInteractor();
      when(inboxApi.getUnreadCount()).thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject('0'))));
      await _setupLocator(interactor: interactor, inboxApi: inboxApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      // Assert there's no text in the inbox-count
      expect(find.descendant(of: find.byKey(Key('inbox-count')), matching: find.byType(Text)), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('Displays Inbox count', (tester) async {
      final inboxApi = MockInboxApi();
      var interactor = MockInteractor();
      when(inboxApi.getUnreadCount())
          .thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject('12321'))));
      await _setupLocator(interactor: interactor, inboxApi: inboxApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      expect(find.text('12321'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Displays Inbox count on app bar', (tester) async {
      final inboxCount = '12';

      final inboxApi = MockInboxApi();
      var interactor = MockInteractor();
      when(inboxApi.getUnreadCount())
          .thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject(inboxCount))));
      await _setupLocator(interactor: interactor, inboxApi: inboxApi);

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
      await _setupLocator(interactor: interactor, inboxApi: inboxApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      expect(find.descendant(of: find.byType(AppBar), matching: find.byKey(NumberBadge.backgroundKey)), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('displays course when passed in as starting page', (tester) async {
      await _setupLocator();

      await tester.pumpWidget(_testableMaterialWidget(startingPage: DashboardContentScreens.Courses));
      await tester.pumpAndSettle();

      expect(find.byType(CoursesScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('displays calendar when passed in as starting page', (tester) async {
      await _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      await tester.pumpWidget(_testableMaterialWidget(
        initLogin: login,
        startingPage: DashboardContentScreens.Calendar,
      ));

      // Wait for day activity dot animation delay to settle
      await tester.pumpAndSettle(Duration(seconds: 1));

      expect(find.byType(CalendarScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('displays alerts when passed in as starting page', (tester) async {
      await _setupLocator();

      await tester.pumpWidget(_testableMaterialWidget(startingPage: DashboardContentScreens.Alerts));
      await tester.pumpAndSettle();

      // Check for the alerts screen
      expect(find.byType(AlertsScreen), findsOneWidget);
    });
  });

  group('Interactions', () {
    testWidgetsWithAccessibilityChecks('tapping courses in the bottom nav shows courses screen', (tester) async {
      await _setupLocator();

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
      await _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      await tester.pumpWidget(_testableMaterialWidget(initLogin: login));
      await tester.pumpAndSettle();

      // Navigate to Calendar
      await tester.tap(find.text(AppLocalizations().calendarLabel));

      // Wait for day activity dot animation delay to settle
      await tester.pumpAndSettle(Duration(seconds: 1));

      expect(find.byType(CalendarScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('tapping alerts sets correct current page index', (tester) async {
      await _setupLocator();

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Navigate to Alerts
      await tester.tap(find.text(AppLocalizations().alertsLabel));
      await tester.pumpAndSettle();

      expect(find.byType(AlertsScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('tapping Inbox from nav drawer opens inbox page', (tester) async {
      await _setupLocator();

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      // Click on Inbox
      await tester.tap(find.text(AppLocalizations().inbox));
    });

    testWidgetsWithAccessibilityChecks('tapping Manage Students from nav drawer opens manage students page',
        (tester) async {
      await _setupLocator();

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      // Click on Manage Students
      await tester.tap(find.text(AppLocalizations().manageStudents));
      await tester.pumpAndSettle(Duration(seconds: 3));

      // Test that Manage Students screen was loaded
      expect(find.byType(ManageStudentsScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('tapping Settings in nav drawer opens settings screen', (tester) async {
      await _setupLocator();

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      // Click on Settings
      var settingsFinder = find.text(AppLocalizations().settings);
      await ensureVisibleByScrolling(settingsFinder, tester, scrollFrom: ScreenVerticalLocation.MID_BOTTOM);
      await tester.pumpAndSettle();
      await tester.tap(settingsFinder);
      await tester.pumpAndSettle();

      // Test that settings screen was loaded
      expect(find.byType(SettingsScreen), findsOneWidget);

      await tester.pageBack();
      await tester.pumpAndSettle();

      // Drawer should be closed
      expect(find.byType(Drawer), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('tapping Help from nav drawer shows help', (tester) async {
      await _setupLocator(interactor: MockInteractor());

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
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
        final reminderDb = MockReminderDb();
        final notificationUtil = MockNotificationUtil();

        await _setupLocator();
        final _locator = GetIt.instance;
        _locator.registerLazySingleton<ReminderDb>(() => reminderDb);
        _locator.registerLazySingleton<NotificationUtil>(() => notificationUtil);

        when(reminderDb.getAllForUser(any, any)).thenAnswer((_) async => []);

        var login = Login((b) => b
          ..domain = 'domain'
          ..accessToken = 'token'
          ..user = CanvasModelTestUtils.mockUser().toBuilder());

        await tester.pumpWidget(_testableMaterialWidget(initLogin: login));
        await tester.pumpAndSettle();

        expect(ApiPrefs.isLoggedIn(), true);

        // Open the nav drawer
        dashboardState(tester).scaffoldKey.currentState?.openDrawer();
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
        verifyNever(analyticsMock.logEvent(AnalyticsEventConstants.LOGOUT));
      },
    );

    // Not using the accessibility tester due to an issue where the
    // Login Landing screen fails a contrast ratio test after logging out
    // (the tests for that screen all pass accessibility checks, however)
    testWidgets('tapping Sign Out from nav drawer signs user out and returns to the Login Landing screen',
        (tester) async {
      final reminderDb = MockReminderDb();
      final calendarFilterDb = MockCalendarFilterDb();
      final notificationUtil = MockNotificationUtil();
      final authApi = MockAuthApi();

      await _setupLocator();
      final _locator = GetIt.instance;
      _locator.registerLazySingleton<ReminderDb>(() => reminderDb);
      _locator.registerLazySingleton<CalendarFilterDb>(() => calendarFilterDb);
      _locator.registerLazySingleton<NotificationUtil>(() => notificationUtil);
      _locator.registerLazySingleton<AuthApi>(() => authApi);

      when(reminderDb.getAllForUser(any, any)).thenAnswer((_) async => []);

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      await tester.pumpWidget(_testableMaterialWidget(initLogin: login));
      await tester.pumpAndSettle();

      expect(ApiPrefs.isLoggedIn(), true);

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
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
      verify(analyticsMock.logEvent(AnalyticsEventConstants.LOGOUT)).called(1);
    });

    // Not using the accessibility tester due to an issue where the
    // Login Landing screen fails a contrast ratio test after logging out
    // (the tests for that screen all pass accessibility checks, however)
    testWidgets('tapping Switch Users from nav drawer signs user out and returns to the Login Landing screen',
        (tester) async {
      final reminderDb = MockReminderDb();
      final calendarFilterDb = MockCalendarFilterDb();
      final notificationUtil = MockNotificationUtil();

      await _setupLocator();
      final _locator = GetIt.instance;
      _locator.registerLazySingleton<ReminderDb>(() => reminderDb);
      _locator.registerLazySingleton<CalendarFilterDb>(() => calendarFilterDb);
      _locator.registerLazySingleton<NotificationUtil>(() => notificationUtil);

      when(reminderDb.getAllForUser(any, any)).thenAnswer((_) async => []);

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      await tester.pumpWidget(_testableMaterialWidget(initLogin: login));
      await tester.pumpAndSettle();

      expect(ApiPrefs.isLoggedIn(), true);

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      // Click on Sign Out
      var logoutFinder = find.text(AppLocalizations().switchUsers);
      await ensureVisibleByScrolling(logoutFinder, tester, scrollFrom: ScreenVerticalLocation.MID_BOTTOM);
      await tester.pumpAndSettle();
      await tester.tap(logoutFinder);
      await tester.pumpAndSettle();

      // Test if we ended up on the Login Landing page and if we are logged out
      expect(find.byType(LoginLandingScreen), findsOneWidget);
      expect(ApiPrefs.isLoggedIn(), false);
      verify(analyticsMock.logEvent(AnalyticsEventConstants.SWITCH_USERS)).called(1);
    });

    testWidgetsWithAccessibilityChecks('tapping selected user opens and closes student selector', (tester) async {
      // Animation values
      int retracted = 0;
      int expanded = 1;

      await _setupLocator(interactor: MockInteractor(includePronouns: false));

      // Get the first user
      var interactor = GetIt.instance.get<DashboardInteractor>();
      late User? first;
      interactor.getStudents().then((students) {
        first = students?.first;
      });

      // Load the screen
      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Widget tree is built, we should have access to what we need to check the state of the expandable widget animation now
      var slideAnimation = ((studentExpansionState(tester).animation));

      // Make sure the expansion widget exists and is initially retracted
      expect(find.byType(StudentExpansionWidget), findsOneWidget);
      expect(slideAnimation.value, retracted);

      expect(first, isNotNull);
      // Tap the user header, expanding it
      // There will be two instances, one in the header and one in the student switcher
      // we want to tap the first one (the one in the header)
      await tester.tap(find.text(first!.shortName!).at(0));
      await tester.pumpAndSettle(); // Wait for user switcher to slide out
      expect(slideAnimation.value, expanded);

      // Tap the user header, retracting it
      // There will be two instances, one in the header and one in the student switcher
      // we want to tap the first one (the one in the header)
      await tester.tap(find.text(first!.shortName!).at(0));
      await tester.pumpAndSettle(); // Wait for user switcher to slide back
      expect(slideAnimation.value, retracted);
    });

    testWidgetsWithAccessibilityChecks('tapping student from student selector closes selector', (tester) async {
      // Animation values
      int retracted = 0;
      int expanded = 1;

      await _setupLocator(interactor: MockInteractor(includePronouns: false));

      // Get the first user
      var interactor = GetIt.instance.get<DashboardInteractor>();
      late User? first;
      late User? second;
      interactor.getStudents().then((students) {
        first = students?.first;
        second = students?[1];
      });

      // Load the screen
      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Widget tree is built, we should have access to what we need to check the state of the expandable widget animation now
      var slideAnimation = ((studentExpansionState(tester).animation));

      // Make sure the expansion widget exists and is initially retracted
      expect(find.byType(StudentExpansionWidget), findsOneWidget);
      expect(slideAnimation.value, retracted);

      expect(first, isNotNull);
      expect(second, isNotNull);

      // Tap the user header, expanding it
      // There will be two instances, one in the header and one in the student switcher
      // we want to tap the first one (the one in the header)
      await tester.tap(find.text(first!.shortName!).at(0));
      await tester.pumpAndSettle(); // Wait for user switcher to slide out
      expect(slideAnimation.value, expanded);

      // Tap on a user
      await tester.tap(find.text(second!.shortName!));
      await tester.pumpAndSettle(); // Wait for user switcher to slide back

      expect(slideAnimation.value, retracted);
    });

    testWidgetsWithAccessibilityChecks('deep link params is cleared after first screen is shown', (tester) async {
      await _setupLocator();

      Map<String, Object> params = {'test': 'Instructure Pandas'};

      await tester.pumpWidget(_testableMaterialWidget(deepLinkParams: params));
      await tester.pump();

      // Make sure our params made it into DashboardScreen
      expect(dashboardState(tester).currentDeepLinkParams, params);

      // Finish loading the screen
      await tester.pumpAndSettle();

      // Check that the deep link params are null
      expect(dashboardState(tester).currentDeepLinkParams, null);
    });

    testWidgetsWithAccessibilityChecks('Tapping Act As User button opens MasqueradeScreen', (tester) async {
      await _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..canMasquerade = true
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      await tester.pumpWidget(_testableMaterialWidget(initLogin: login));
      await tester.pumpAndSettle();

      // Open the drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      // Tap the 'Act As User' button
      await tester.tap(find.text(l10n.actAsUser));
      await tester.pump();
      await tester.pump();
      await tester.pumpAndSettle();

      expect(find.byType(MasqueradeScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Tapping Stop Acting As User button shows confirmation dialog', (tester) async {
      await _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..canMasquerade = true
        ..user = CanvasModelTestUtils.mockUser().toBuilder()
        ..masqueradeDomain = 'masqueradeDomain'
        ..masqueradeUser = CanvasModelTestUtils.mockUser().toBuilder());

      await tester.pumpWidget(_testableMaterialWidget(initLogin: login));
      await tester.pumpAndSettle();

      // Open the drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      // Tap the 'Stop Acting As User' button
      await tester.tap(find.text(l10n.stopActAsUser));
      await tester.pump();
      await tester.pump();
      await tester.pumpAndSettle();

      expect(find.byType(AlertDialog), findsOneWidget);
      expect(find.text(l10n.endMasqueradeMessage(login.user.name)), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Displays and dismisses Old Reminders dialog', (tester) async {
      var interactor = MockInteractor();
      interactor.showOldReminderMessage = true;
      await _setupLocator(interactor: interactor);

      // Load the screen
      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Should display dialog
      var dialog = find.byType(AlertDialog);
      expect(dialog, findsOneWidget);

      // Should display correct title
      var title = find.descendant(of: dialog, matching: find.text(AppLocalizations().oldReminderMessageTitle));
      expect(title, findsOneWidget);

      // Should display correct message
      var message = find.descendant(of: dialog, matching: find.text(AppLocalizations().oldReminderMessage));
      expect(message, findsOneWidget);

      // Tap ok to dismiss
      var ok = find.descendant(of: dialog, matching: find.text(AppLocalizations().ok));
      await tester.tap(ok);
      await tester.pumpAndSettle();

      // Dialog should no longer show
      expect(find.byType(AlertDialog), findsNothing);

      // Should have logged analytics event
      verify(analyticsMock.logEvent(AnalyticsEventConstants.VIEWED_OLD_REMINDER_MESSAGE));
    });

    testWidgetsWithAccessibilityChecks('Does not display Old Reminders dialog if no reminders', (tester) async {
      var interactor = MockInteractor();
      interactor.showOldReminderMessage = false;
      await _setupLocator(interactor: interactor);

      // Load the screen
      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Should not display dialog, title, or message
      expect(find.byType(AlertDialog), findsNothing);
      expect(find.text(AppLocalizations().oldReminderMessage), findsNothing);
      expect(find.text(AppLocalizations().oldReminderMessageTitle), findsNothing);
    });
  });

  group('Loading', () {
    testWidgetsWithAccessibilityChecks('Initiates call to update inbox count', (tester) async {
      final inboxApi = MockInboxApi();
      var interactor = MockInteractor();
      await _setupLocator(interactor: interactor, inboxApi: inboxApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      verify(inboxApi.getUnreadCount()).called(1);
    });

    testWidgetsWithAccessibilityChecks('Updates Inbox count', (tester) async {
      final inboxApi = MockInboxApi();
      var interactor = MockInteractor();
      when(inboxApi.getUnreadCount())
          .thenAnswer((_) => Future.value(UnreadCount((b) => b..count = JsonObject('12321'))));
      await _setupLocator(interactor: interactor, inboxApi: inboxApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
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
      await _setupLocator(interactor: interactor, alertsApi: alertsApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      verify(alertsApi.getAlertsDepaginated(any, any)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Alerts count of zero hides badge', (tester) async {
      final alertsApi = MockAlertsApi();
      var interactor = MockInteractor();
      when(alertsApi.getAlertsDepaginated(any, any)).thenAnswer((_) => Future.value([]));
      await _setupLocator(interactor: interactor, alertsApi: alertsApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      // Assert there's no text in the alerts-count
      expect(find.descendant(of: find.byKey(Key('alerts-count')), matching: find.byType(Text)), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('Displays Alert count', (tester) async {
      final alertsApi = MockAlertsApi();
      var interactor = MockInteractor();

      final date = DateTime.now();
      final data = List.generate(5, (index) {
        // Create a list of alerts with dates in ascending order (reversed)
        return Alert((b) => b
          ..id = index.toString()
          ..workflowState = AlertWorkflowState.unread
          ..actionDate = date.add(Duration(days: index))
          ..lockedForUser = false);
      });

      when(alertsApi.getAlertsDepaginated(any, any)).thenAnswer((_) => Future.value(data.toList()));

      await _setupLocator(interactor: interactor, alertsApi: alertsApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      expect(find.text('5'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Updates Alert count', (tester) async {
      final alertsApi = MockAlertsApi();
      var interactor = MockInteractor();

      final date = DateTime.now();
      final data = List.generate(5, (index) {
        // Create a list of alerts with dates in ascending order (reversed)
        return Alert((b) => b
          ..id = index.toString()
          ..workflowState = AlertWorkflowState.unread
          ..actionDate = date.add(Duration(days: index))
          ..lockedForUser = false);
      });

      when(alertsApi.getAlertsDepaginated(any, any)).thenAnswer((_) => Future.value(data.toList()));

      await _setupLocator(interactor: interactor, alertsApi: alertsApi);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      // Open the nav drawer
      dashboardState(tester).scaffoldKey.currentState?.openDrawer();
      await tester.pumpAndSettle();

      when(alertsApi.getAlertsDepaginated(any, any)).thenAnswer((_) => Future.value(data.sublist(0, 4).toList()));

      interactor.getAlertCountNotifier().update('doesn\'t matter');
      await tester.pumpAndSettle();

      expect(find.text('4'), findsOneWidget);
    });
  });

  // Need the Dashboard screen as well as the Calendar screen to test the today button.
  // To avoid polluting the Calendar screen test file with Dashboard dependencies, these
  // tests were put here instead of the Calendar screen
  group('calendar today button', () {
    testWidgetsWithAccessibilityChecks('today button not shown by default', (tester) async {
      await _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      // Start on the Calendar screen
      await tester.pumpWidget(_testableMaterialWidget(
        initLogin: login,
        startingPage: DashboardContentScreens.Calendar,
      ));

      // Wait for day activity dot animation delay to settle
      await tester.pumpAndSettle(Duration(seconds: 1));

      // Check we're on the Calendar screen
      expect(find.byType(CalendarScreen), findsOneWidget);

      // Check to make sure the today button is hidden
      expect(find.bySemanticsLabel(AppLocalizations().gotoTodayButtonLabel), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('today button shown when date other than today selected', (tester) async {
      await _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      // Start on the Calendar screen
      await tester.pumpWidget(_testableMaterialWidget(
        initLogin: login,
        startingPage: DashboardContentScreens.Calendar,
      ));

      // Wait for day activity dot animation delay to settle
      await tester.pumpAndSettle(Duration(seconds: 1));

      // Check we're on the Calendar screen
      expect(find.byType(CalendarScreen), findsOneWidget);

      // Make sure we aren't showing the today button
      expect(find.bySemanticsLabel(AppLocalizations().gotoTodayButtonLabel), findsNothing);

      // Tap on another date
      await goToDate(tester, DateTime.now().add(Duration(days: 2)));

      // Check to see if we are showing the today button
      expect(find.bySemanticsLabel(AppLocalizations().gotoTodayButtonLabel), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('today button tap goes to now', (tester) async {
      await _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      // Start on the Calendar screen
      await tester.pumpWidget(_testableMaterialWidget(
        initLogin: login,
        startingPage: DashboardContentScreens.Calendar,
      ));

      Finder todayButton = find.bySemanticsLabel(AppLocalizations().gotoTodayButtonLabel);

      // Wait for day activity dot animation delay to settle
      await tester.pumpAndSettle(Duration(seconds: 2));

      // Check we're on the Calendar screen
      expect(find.byType(CalendarScreen), findsOneWidget);

      // Make sure we aren't showing the today button
      expect(todayButton, findsNothing);

      // Tap on another date
      await goToDate(tester, DateTime.now().add(Duration(days: 2)));

      // Check to see if we are showing the today button
      expect(todayButton, findsOneWidget);

      // Tap on today button
      await tester.tap(todayButton);

      // Make sure we are on today's date
      CalendarWidgetState state = tester.state(find.byType(CalendarWidget));
      expect(state.selectedDay.year, DateTime.now().year);
      expect(state.selectedDay.month, DateTime.now().month);
      expect(state.selectedDay.day, DateTime.now().day);
    });

    testWidgetsWithAccessibilityChecks('tapping today button hides button', (tester) async {
      await _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      // Start on the Calendar screen
      await tester.pumpWidget(_testableMaterialWidget(
        initLogin: login,
        startingPage: DashboardContentScreens.Calendar,
      ));

      Finder todayButton = find.bySemanticsLabel(AppLocalizations().gotoTodayButtonLabel);

      // Wait for day activity dot animation delay to settle
      await tester.pumpAndSettle(Duration(seconds: 1));

      // Check we're on the Calendar screen
      expect(find.byType(CalendarScreen), findsOneWidget);

      // Check the today button is hidden
      expect(todayButton, findsNothing);

      // Tap on another date
      await goToDate(tester, DateTime.now().add(Duration(days: 2)));

      // Check to see if we are showing the today button
      expect(todayButton, findsOneWidget);

      // Tap on today button
      await tester.tap(todayButton);

      // Make sure we are on today's date
      CalendarWidgetState state = tester.state(find.byType(CalendarWidget));
      expect(state.selectedDay.year, DateTime.now().year);
      expect(state.selectedDay.month, DateTime.now().month);
      expect(state.selectedDay.day, DateTime.now().day);

      // Wait for the button to be hidden
      await tester.pumpAndSettle();

      // Make sure we aren't showing the today button
      expect(todayButton, findsNothing);
    });

    testWidgetsWithAccessibilityChecks('today button hides when not on calendar screen', (tester) async {
      await _setupLocator();

      var login = Login((b) => b
        ..domain = 'domain'
        ..accessToken = 'token'
        ..user = CanvasModelTestUtils.mockUser().toBuilder());

      // Start on the Calendar screen
      await tester.pumpWidget(_testableMaterialWidget(
        initLogin: login,
        startingPage: DashboardContentScreens.Calendar,
      ));

      Finder todayButton = find.bySemanticsLabel(AppLocalizations().gotoTodayButtonLabel);

      // Wait for day activity dot animation delay to settle
      await tester.pumpAndSettle(Duration(seconds: 2));

      expect(find.byType(CalendarScreen), findsOneWidget);

      // Check to make sure we aren't showing the today button
      expect(todayButton, findsNothing);

      // Tap on another date
      await goToDate(tester, DateTime.now().add(Duration(days: 2)));

      // Check to see if we are showing the today button
      expect(todayButton, findsOneWidget);

      // Navigate to Courses
      await tester.tap(find.text(AppLocalizations().coursesLabel));
      await tester.pumpAndSettle();

      // Check to make sure we're on the Courses screen
      expect(find.byType(CoursesScreen), findsOneWidget);

      // Check to make sure the today button is hidden
      expect(todayButton, findsNothing);
    });
  });
}

Future<CalendarWidgetState> goToDate(WidgetTester tester, DateTime date) async {
  CalendarWidgetState state = tester.state(find.byType(CalendarWidget));
  state.selectDay(
    date,
    dayPagerBehavior: CalendarPageChangeBehavior.jump,
    weekPagerBehavior: CalendarPageChangeBehavior.jump,
    monthPagerBehavior: CalendarPageChangeBehavior.jump,
  );
  await tester.pumpAndSettle(Duration(seconds: 1));
  return state;
}

DashboardState dashboardState(WidgetTester tester) => tester.state(find.byType(DashboardScreen));

StudentExpansionWidgetState studentExpansionState(WidgetTester tester) =>
    tester.state(find.byType(StudentExpansionWidget));

class MockHelpScreenInteractor extends HelpScreenInteractor {
  @override
  Future<List<HelpLink>> getObserverCustomHelpLinks({bool forceRefresh = false}) => Future.value(<HelpLink>[]);
}

class MockInteractor extends DashboardInteractor {
  bool includePronouns;
  bool generateStudents;
  bool generateSelf;
  bool showOldReminderMessage = false;

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
  Future<User?> getSelf({app}) async => generateSelf
      ? CanvasModelTestUtils.mockUser(
          name: 'Marlene Name',
          shortName: 'Marlene',
          pronouns: includePronouns ? 'she/her' : null,
          primaryEmail: 'marlene@instructure.com')
      : null;

  @override
  Future<bool> shouldShowOldReminderMessage() async => showOldReminderMessage;

  @override
  Future<PermissionStatus> requestNotificationPermission() async => PermissionStatus.granted;
}

class MockCoursesInteractor extends CoursesInteractor {
  @override
  Future<List<Course>> getCourses({bool isRefresh = false, String? studentId = null}) async {
    var courses = <Course>[];
    return courses;
  }
}

class MockManageStudentsInteractor extends ManageStudentsInteractor {
  @override
  Future<List<User>> getStudents({bool forceRefresh = false}) => Future.value([]);
}
