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

import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/not_a_parent_screen.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_svg/svg.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../utils/accessibility_utils.dart';
import '../utils/platform_config.dart';
import '../utils/test_app.dart';
import '../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  AppLocalizations l10n = AppLocalizations();

  testWidgetsWithAccessibilityChecks('Displays panda, text, and buttons', (tester) async {
    await tester.pumpWidget(TestApp(NotAParentScreen()));
    await tester.pump();

    expect(find.byType(SvgPicture), findsOneWidget); // Panda image
    expect(find.text(l10n.notAParentTitle), findsOneWidget); // Title
    expect(find.text(l10n.notAParentSubtitle), findsOneWidget); // Subtitle
    expect(find.text(l10n.returnToLogin), findsOneWidget); // 'Return to Login' button
    expect(find.text(l10n.studentOrTeacherTitle), findsOneWidget); // 'Student or teacher' button
  });

  testWidgetsWithAccessibilityChecks('Can return to login', (tester) async {
    final mockNav = MockQuickNav();
    setupTestLocator((locator) {
      locator.registerLazySingleton<QuickNav>(() => mockNav);
      locator.registerLazySingleton<ReminderDb>(() => MockReminderDb());
      locator.registerLazySingleton<NotificationUtil>(() => MockNotificationUtil());
      locator.registerLazySingleton<CalendarFilterDb>(() => MockCalendarFilterDb());
      locator.registerLazySingleton<AuthApi>(() => MockAuthApi());
    });

    await tester.pumpWidget(TestApp(
      NotAParentScreen(),
      platformConfig: PlatformConfig(initLoggedInUser: Login((b) => b..user = User((u) => u..id = '123').toBuilder())),
    ));
    await tester.pump();

    expect(find.text(l10n.returnToLogin), findsOneWidget);
    await tester.tap(find.text(l10n.returnToLogin));
    await tester.pump();

    verify(mockNav.pushRouteAndClearStack(any, PandaRouter.login()));
  });

  testWidgetsWithAccessibilityChecks('Expands to show app options', (tester) async {
    await tester.pumpWidget(TestApp(NotAParentScreen()));
    await tester.pump();

    // Tap 'Are you a student or teacher?' button and wait for options to animate open
    await tester.tap(find.text(l10n.studentOrTeacherTitle));
    await tester.pumpAndSettle();

    expect(find.text(l10n.studentOrTeacherTitle), findsOneWidget); // 'Student or teacher' button
    expect(find.text(l10n.studentOrTeacherSubtitle), findsOneWidget); // App options text
    expect(find.bySemanticsLabel(l10n.canvasStudentApp), findsOneWidget); // Student app button
    expect(find.bySemanticsLabel(l10n.canvasTeacherApp), findsOneWidget); // Teacher app button
  });

  testWidgetsWithAccessibilityChecks('Launches intent to open student app in play store', (tester) async {
    var mockLauncher = MockUrlLauncher();
    setupTestLocator((locator) => locator.registerLazySingleton<UrlLauncher>(() => mockLauncher));

    await tester.pumpWidget(TestApp(NotAParentScreen()));
    await tester.pump();

    // Tap 'Are you a student or teacher?' button and wait for options to animate open
    await tester.tap(find.text(l10n.studentOrTeacherTitle));
    await tester.pumpAndSettle();

    // Tap the student app button
    await tester.tap(find.text(l10n.studentApp));
    await tester.pump();

    var actualUrl = verify(mockLauncher.launch(captureAny)).captured[0];
    expect(actualUrl, 'market://details?id=com.instructure.candroid');
  });

  testWidgetsWithAccessibilityChecks('Launches intent to open teacher app in play store', (tester) async {
    var mockLauncher = MockUrlLauncher();
    setupTestLocator((locator) => locator.registerLazySingleton<UrlLauncher>(() => mockLauncher));

    await tester.pumpWidget(TestApp(NotAParentScreen()));
    await tester.pump();

    // Tap 'Are you a student or teacher?' button and wait for options to animate open
    await tester.tap(find.text(l10n.studentOrTeacherTitle));
    await tester.pumpAndSettle();

    // Tap the student app button
    await tester.tap(find.text(l10n.teacherApp));
    await tester.pump();

    var actualUrl = verify(mockLauncher.launch(captureAny)).captured[0];
    expect(actualUrl, 'market://details?id=com.instructure.teacher');
  });
}