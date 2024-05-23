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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/utils/common_widgets/masquerade_ui.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/remote_config_utils.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../accessibility_utils.dart';
import '../canvas_model_utils.dart';
import '../platform_config.dart';
import '../test_app.dart';
import '../test_helpers/mock_helpers.dart';
import '../test_helpers/mock_helpers.mocks.dart';

void main() {
  AppLocalizations l10n = AppLocalizations();

  Login normalLogin = Login((b) => b
    ..domain = 'domain'
    ..accessToken = 'token'
    ..user = CanvasModelTestUtils.mockUser().toBuilder());

  Login masqueradeLogin = normalLogin.rebuild((b) => b
    ..masqueradeDomain = 'masqueradeDomain'
    ..masqueradeUser = CanvasModelTestUtils.mockUser(name: 'Masked User').toBuilder());

  String masqueradeText = l10n.actingAsUser(masqueradeLogin.masqueradeUser!.name);

  Key masqueradeContainerKey = Key('masquerade-ui-container');

  setUp(() async {
    final mockRemoteConfig = setupMockRemoteConfig(valueSettings: {'qr_login_enabled_parent': 'true'});
    await setupPlatformChannels(config: PlatformConfig(initRemoteConfig: mockRemoteConfig));
  });

  tearDown(() {
    RemoteConfigUtils.clean();
  });

  testWidgetsWithAccessibilityChecks('Builds initially as disabled', (tester) async {
    await tester.pumpWidget(TestApp(_childWithButton()));
    await tester.pumpAndSettle();

    expect(find.byType(MasqueradeUI), findsOneWidget);
    expect(find.text(masqueradeText), findsNothing);

    MasqueradeUIState state = tester.state(find.byType(MasqueradeUI));
    expect(state.enabled, isFalse);
  });

  testWidgetsWithAccessibilityChecks('Builds initially as enabled', (tester) async {
    await tester.pumpWidget(TestApp(_childWithButton()));
    ApiPrefs.switchLogins(masqueradeLogin);
    await tester.pumpAndSettle();

    expect(find.byType(MasqueradeUI), findsOneWidget);
    expect(find.text(masqueradeText), findsOneWidget);

    MasqueradeUIState state = tester.state(find.byType(MasqueradeUI));
    expect(state.enabled, isTrue);
  });

  testWidgetsWithAccessibilityChecks('Shows expected UI elements', (tester) async {
    await tester.pumpWidget(TestApp(_childWithButton()));
    ApiPrefs.switchLogins(masqueradeLogin);
    await tester.pumpAndSettle();

    // MasqueradeUI widget
    expect(find.byType(MasqueradeUI), findsOneWidget);

    // Container for UI elements
    expect(find.byKey(masqueradeContainerKey), findsOneWidget);

    // Foreground border
    Container container = tester.widget(find.byKey(masqueradeContainerKey));
    Border border = (container.foregroundDecoration as BoxDecoration).border as Border;
    expect(border.left, BorderSide(color: ParentColors.masquerade, width: 3));
    expect(border.top, BorderSide(color: ParentColors.masquerade, width: 3));
    expect(border.right, BorderSide(color: ParentColors.masquerade, width: 3));
    expect(border.bottom, BorderSide(color: ParentColors.masquerade, width: 3));

    // 'You are acting as <user name>' message
    expect(find.text(masqueradeText), findsOneWidget);

    // 'Stop acting as user' close button
    expect(find.byIcon(Icons.close), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Updates to reflect new masquerade status', (tester) async {
    await tester.pumpWidget(TestApp(_childWithButton()));
    ApiPrefs.switchLogins(masqueradeLogin);
    await tester.pumpAndSettle();

    // Should be enabled
    expect(find.text(masqueradeText), findsOneWidget);

    ApiPrefs.switchLogins(normalLogin);
    await tester.tap(find.byType(TextButton));
    await tester.pumpAndSettle();

    // Should now be disabled
    expect(find.text(masqueradeText), findsNothing);

    ApiPrefs.switchLogins(masqueradeLogin);
    await tester.tap(find.byType(TextButton));
    await tester.pumpAndSettle();

    // Should be enabled again
    expect(find.text(masqueradeText), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Stop button displays confirmation dialog', (tester) async {
    await tester.pumpWidget(TestApp(_childWithButton()));
    ApiPrefs.switchLogins(masqueradeLogin);
    await tester.pumpAndSettle();

    await tester.tap(find.byIcon(Icons.close));
    await tester.pumpAndSettle();

    expect(find.byType(AlertDialog), findsOneWidget);
    expect(find.text(l10n.endMasqueradeMessage(masqueradeLogin.masqueradeUser!.name)), findsOneWidget);

    // Close the dialog
    await tester.tap(find.text(l10n.cancel));
    await tester.pumpAndSettle();

    expect(find.byType(AlertDialog), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Stop button logout message if masquerading from QR code', (tester) async {
    Login login = masqueradeLogin.rebuild((b) => b..isMasqueradingFromQRCode = true);

    await tester.pumpWidget(TestApp(_childWithButton()));
    ApiPrefs.switchLogins(login);
    await tester.pumpAndSettle();

    await tester.tap(find.byIcon(Icons.close));
    await tester.pumpAndSettle();

    expect(find.byType(AlertDialog), findsOneWidget);
    expect(find.text(l10n.endMasqueradeLogoutMessage(masqueradeLogin.masqueradeUser!.name)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Accepting confirmation dialog stops masquerading', (tester) async {
    await tester.pumpWidget(TestApp(_childWithButton()));
    ApiPrefs.switchLogins(masqueradeLogin);
    await tester.pumpAndSettle();

    expect(find.text(masqueradeText), findsOneWidget);
    expect(ApiPrefs.isMasquerading(), true);

    await tester.tap(find.byIcon(Icons.close));
    await tester.pumpAndSettle();

    // Tap OK
    await tester.tap(find.text(l10n.ok));
    await tester.pumpAndSettle();

    expect(find.byType(AlertDialog), findsNothing);
    expect(find.text(masqueradeText), findsNothing);
    expect(ApiPrefs.isMasquerading(), false);
  });

  testWidgetsWithAccessibilityChecks('Accepting logout confirmation performs logout', (tester) async {
    final reminderDb = MockReminderDb();
    final calendarFilterDb = MockCalendarFilterDb();
    final notificationUtil = MockNotificationUtil();
    when(reminderDb.getAllForUser(any, any)).thenAnswer((_) async => []);
    setupTestLocator((locator) {
      locator.registerLazySingleton<ReminderDb>(() => reminderDb);
      locator.registerLazySingleton<CalendarFilterDb>(() => calendarFilterDb);
      locator.registerLazySingleton<NotificationUtil>(() => notificationUtil);
      locator.registerLazySingleton<QuickNav>(() => QuickNav());
    });

    Login login = masqueradeLogin.rebuild((b) => b..isMasqueradingFromQRCode = true);
    await tester.pumpWidget(TestApp(_childWithButton()));
    ApiPrefs.switchLogins(login);
    await tester.pumpAndSettle();

    await tester.tap(find.byIcon(Icons.close));
    await tester.pumpAndSettle();

    // Tap OK
    await tester.tap(find.text(l10n.ok));
    await tester.pumpAndSettle();

    expect(find.byType(AlertDialog), findsNothing);
    expect(find.text(masqueradeText), findsNothing);
    expect(find.byType(LoginLandingScreen), findsOneWidget);
    expect(ApiPrefs.isLoggedIn(), false);
  });
}

Widget _childWithButton() {
  return Material(
    child: Builder(
      builder: (context) => TextButton(
        child: Text('Tap to refresh'),
        onPressed: () {
          MasqueradeUI.of(context)?.refresh();
        },
      ),
    ),
  );
}
