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
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:flutter_parent/screens/dashboard/student_horizontal_list_view.dart';
import 'package:flutter_parent/screens/pairing/pairing_util.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:provider/provider.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows all students as well as add student button', (tester) async {
      var student1 = CanvasModelTestUtils.mockUser(shortName: 'Billy');
      var student2 = CanvasModelTestUtils.mockUser(shortName: 'Sally');
      var student3 = CanvasModelTestUtils.mockUser(shortName: 'Trevor');
      var students = [student1, student2, student3];

      AccountsApi accountsApi = MockAccountsApi();
      setupTestLocator((locator) {
        locator.registerLazySingleton<AccountsApi>(() => accountsApi);
      });

      // Setup the widget
      await tester.pumpWidget(TestApp(StudentHorizontalListView(students)));
      await tester.pumpAndSettle();

      // Check for the names of the students and the add student button
      expect(find.text(student1.shortName!), findsOneWidget);
      expect(find.text(student2.shortName!), findsOneWidget);
      expect(find.text(student3.shortName!), findsOneWidget);
      expect(find.text(AppLocalizations().addStudent), findsOneWidget);
    });
  });

  group('Interaction', () {
    testWidgetsWithAccessibilityChecks('student tap updates selected student, theme, and calls onTap', (tester) async {
      var student1 = CanvasModelTestUtils.mockUser(shortName: 'Billy');
      var student2 = CanvasModelTestUtils.mockUser(shortName: 'Sally');

      var students = [student1, student2];

      bool called = false;
      Function tapped = () {
        called = !called;
      };

      var notifier = SelectedStudentNotifier();
      final alertNotifier = _MockAlertCountNotifier();

      setupTestLocator((locator) {
        locator.registerLazySingleton<SelectedStudentNotifier>(() => notifier);
        locator.registerLazySingleton<AlertCountNotifier>(() => alertNotifier);
        locator.registerLazySingleton<AccountsApi>(() => MockAccountsApi());
      });

      // Setup the widget
      await tester
          .pumpWidget(SelectedStudentNotifierTestApp(StudentHorizontalListView(students, onTap: tapped), notifier));
      await tester.pumpAndSettle();

      // Check the initial value of the student notifier
      expect(notifier.value, null);

      // Check the initial value of the theme, should be default student color (electric)
      var state = ParentTheme.of(TestApp.navigatorKey.currentContext!);
      expect(state?.studentColor, StudentColorSet.electric.light);

      // Check for the second student
      expect(find.text(student2.shortName!), findsOneWidget);

      // Tap on the student
      await tester.tap(find.text(student2.shortName!));
      await tester.pumpAndSettle(); // Wait for animations to settle
      await tester.pump();

      // Check the selected student notifier
      expect(student2, notifier.value);

      // Check the theme, should be correct color for student
      var expectedColor = (await state!.getColorsForStudent(student2.id)).light;
      expect(state.studentColor, expectedColor);

      // Check to make sure we called the onTap function passed in
      expect(called, true);
      verify(alertNotifier.update(student2.id)).called(1);
    });

    testWidgetsWithAccessibilityChecks('add student tap calls PairingUtil', (tester) async {
      var student1 = CanvasModelTestUtils.mockUser(name: 'Billy');

      MockPairingUtil pairingUtil = MockPairingUtil();
      MockAccountsApi accountsApi = MockAccountsApi();

      setupTestLocator((locator) {
        locator.registerLazySingleton<QuickNav>(() => QuickNav());
        locator.registerLazySingleton<PairingUtil>(() => pairingUtil);
        locator.registerLazySingleton<Analytics>(() => MockAnalytics());
        locator.registerLazySingleton<AccountsApi>(() => accountsApi);
      });

      // Setup the widget
      await tester.pumpWidget(TestApp(StudentHorizontalListView([student1])));
      await tester.pumpAndSettle();

      expect(find.text(AppLocalizations().addStudent), findsOneWidget);

      // Tap the 'Add Student' button and wait for any transition animations to finish
      await tester.tap(find.byType(ElevatedButton));
      await tester.pump();

      // Check that PairingUtil was called
      verify(pairingUtil.pairNewStudent(any, any)).called(1);
    });
  });

  testWidgetsWithAccessibilityChecks('attempt to pair with a student calls get students', (tester) async {
    var student1 = CanvasModelTestUtils.mockUser(name: 'Billy');
    var callbackCalled = false;

    MockPairingUtil pairingUtil = MockPairingUtil();
    MockAccountsApi accountsApi = MockAccountsApi();
    final analytics = _MockAnalytics();

    when(pairingUtil.pairNewStudent(any, any)).thenAnswer((inv) => inv.positionalArguments[1]());

    setupTestLocator((locator) {
      locator.registerLazySingleton<QuickNav>(() => QuickNav());
      locator.registerLazySingleton<PairingUtil>(() => pairingUtil);
      locator.registerLazySingleton<Analytics>(() => analytics);
      locator.registerLazySingleton<AccountsApi>(() => accountsApi);
    });

    // Setup the widget
    await tester.pumpWidget(TestApp(StudentHorizontalListView(
      [student1],
      onAddStudent: () {
        callbackCalled = true;
      },
    )));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().addStudent), findsOneWidget);

    // Tap the 'Add Student' button and wait for any transition animations to finish
    await tester.tap(find.byType(ElevatedButton));
    await tester.pumpAndSettle();

    // Verify
    expect(callbackCalled, true);
    verify(pairingUtil.pairNewStudent(any, any)).called(1);
  });
}

class SelectedStudentNotifierTestApp extends StatelessWidget {
  final Widget child;
  final SelectedStudentNotifier notifier;
  SelectedStudentNotifierTestApp(this.child, this.notifier);

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider<SelectedStudentNotifier>(
      create: (context) => notifier,
      child: Consumer<SelectedStudentNotifier>(builder: (context, model, _) {
        return TestApp(child);
      }),
    );
  }
}

class _MockAnalytics extends Mock implements Analytics {}

class _MockAlertCountNotifier extends Mock implements AlertCountNotifier {}
