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

import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/crash_screen.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_dialog.dart';
import 'package:flutter_parent/utils/crash_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';

import '../utils/accessibility_utils.dart';
import '../utils/test_app.dart';
import '../utils/test_helpers/mock_helpers.dart';
import '../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  setupTestLocator((locator) {
    locator.registerLazySingleton<FirebaseCrashlytics>(() => MockFirebaseCrashlytics());
  });

  setUp(() {
    CrashUtils.init();
  });

  final l10n = AppLocalizations();

  testWidgetsWithAccessibilityChecks('Displays and closes crash screen when widget crashes', (tester) async {
    await tester.pumpWidget(TestApp(_CrashTestWidget()));
    await tester.pump();

    // Tap test button which should throw an exception
    await tester.tap(find.byKey(_CrashTestWidget.crashKey));
    await tester.pumpAndSettle();
    await tester.takeException();

    // Crash screen should now be showing
    expect(find.byType(CrashScreen), findsOneWidget);

    // Tap back button
    await tester.tap(find.byType(BackButton));
    await tester.pumpAndSettle();

    // Crash screen should be gone and test button should be visible
    expect(find.byType(CrashScreen), findsNothing);
    expect(find.byKey(_CrashTestWidget.crashKey), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Reset app button clears app state', (tester) async {
    await tester.pumpWidget(TestApp(_CrashTestWidget()));
    await tester.pump();

    // Tap 'increment' button twice
    await tester.tap(find.byKey(_CrashTestWidget.incrementKey));
    await tester.pump();
    await tester.tap(find.byKey(_CrashTestWidget.incrementKey));
    await tester.pump();

    // Counter should now read 'Count: 2'
    expect(find.text('Count: 2'), findsOneWidget);

    // Tap test button which should throw an exception
    await tester.tap(find.byKey(_CrashTestWidget.crashKey));
    await tester.pumpAndSettle();
    await tester.takeException();

    // Crash screen should now be showing
    expect(find.byType(CrashScreen), findsOneWidget);

    // Tap reset button
    await tester.tap(find.text(l10n.crashScreenRestart));
    await tester.pumpAndSettle();

    // Crash screen should be gone and counter should have reset
    expect(find.byType(CrashScreen), findsNothing);
    expect(find.text('Count: 0'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays core elements', (tester) async {
    await tester.pumpWidget(TestApp(CrashScreen(_makeError())));
    await tester.pumpAndSettle();

    // Panda image
    expect(find.byType(SvgPicture), findsOneWidget);

    // Title
    expect(find.text(l10n.crashScreenTitle), findsOneWidget);

    // Message
    expect(find.text(l10n.crashScreenMessage), findsOneWidget);

    // 'Contact support' button
    expect(find.widgetWithText(TextButton, l10n.crashScreenContact), findsOneWidget);

    // 'View error details' button
    expect(find.widgetWithText(TextButton, l10n.crashScreenViewDetails), findsOneWidget);

    // 'Restart app' button
    expect(find.widgetWithText(TextButton, l10n.crashScreenRestart), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays report error dialog', (tester) async {
    await tester.pumpWidget(TestApp(CrashScreen(_makeError())));
    await tester.pumpAndSettle();

    // 'Contact support' button
    expect(find.widgetWithText(TextButton, l10n.crashScreenContact), findsOneWidget);
    await tester.tap(find.widgetWithText(TextButton, l10n.crashScreenContact));
    await tester.pumpAndSettle();

    expect(find.byType(ErrorReportDialog), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays error info', (tester) async {
    await tester.pumpWidget(TestApp(CrashScreen(_makeError())));
    await tester.pumpAndSettle();

    // Tap 'View error details' button
    await tester.tap(find.text(l10n.crashScreenViewDetails));
    await tester.pumpAndSettle();

    expectTile(String title, String subtitle) {
      var tile = find.widgetWithText(ListTile, title);
      expect(tile, findsOneWidget);
      expect(find.descendant(of: tile, matching: find.text(subtitle)), findsOneWidget);
    }

    // App version
    expectTile(l10n.crashDetailsAppVersion, '1.0.0 (3)');

    // Device model
    expectTile(l10n.crashDetailsDeviceModel, 'Instructure Canvas Phone');

    // Android OS version
    expectTile(l10n.crashDetailsAndroidVersion, 'FakeOS 9000');

    // By default should show 'full error message' button, but not the message itself
    expect(find.text(l10n.crashDetailsFullMessage), findsOneWidget);
    expect(find.byKey(Key('full-error-message')), findsNothing);

    await tester.tap(find.text(l10n.done));
    await tester.pumpAndSettle();

    expect(find.text(l10n.crashDetailsFullMessage), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Displays full error info', (tester) async {
    await tester.pumpWidget(TestApp(CrashScreen(_makeError())));
    await tester.pumpAndSettle();

    // Tap 'View error details' button
    await tester.tap(find.text(l10n.crashScreenViewDetails));
    await tester.pumpAndSettle();

    // Tap full error message button
    await tester.tap(find.text(l10n.crashDetailsFullMessage));
    await tester.pumpAndSettle();

    // Should show error message container and message
    expect(find.byKey(Key('full-error-message')), findsOneWidget);
    expect(find.text('Fake Error\n\nFake StackTrace'), findsOneWidget);
  });
}

FlutterErrorDetails _makeError() =>
    FlutterErrorDetails(exception: 'Fake Error', stack: StackTrace.fromString('Fake StackTrace'));

class _CrashTestWidget extends StatefulWidget {
  static Key crashKey = Key('crash-button');
  static Key incrementKey = Key('increment-button');
  static Key counterKey = Key('counter');

  @override
  __CrashTestWidgetState createState() => __CrashTestWidgetState();
}

class __CrashTestWidgetState extends State<_CrashTestWidget> {
  int _counter = 0;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        children: <Widget>[
          Text(
            'Count: $_counter',
            key: _CrashTestWidget.counterKey,
          ),
          TextButton(
            key: _CrashTestWidget.incrementKey,
            child: Text('Tap to increment'),
            onPressed: () => setState(() => _counter++),
          ),
          TextButton(
            key: _CrashTestWidget.crashKey,
            child: Text('Tap to crash'),
            onPressed: () {
              QuickNav().push(context, _CrashingWidget());
            },
          ),
        ],
      ),
    );
  }
}

class _CrashingWidget extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    throw 'Error message';
  }
}
