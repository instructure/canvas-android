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

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/user_colors.dart';
import 'package:flutter_parent/screens/manage_students/student_color_picker_dialog.dart';
import 'package:flutter_parent/screens/manage_students/student_color_picker_interactor.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';
import '../pairing/pairing_util_test.dart';

void main() {
  AppLocalizations l10n = AppLocalizations();
  MockStudentColorPickerInteractor interactor = MockStudentColorPickerInteractor();

  setupTestLocator((locator) {
    locator.registerLazySingleton<StudentColorPickerInteractor>(() => interactor);
  });

  setUp(() {
    reset(interactor);
  });

  testWidgetsWithAccessibilityChecks('Displays colors with semantic labels', (tester) async {
    await tester.pumpWidget(TestApp(StudentColorPickerDialog(initialColor: Colors.white, studentId: '')));
    await tester.pumpAndSettle();

    // The number of color options should match the number of available color sets
    Row options = tester.widget<Row>(find.byKey(Key('color-options')));
    expect(options.children.length, StudentColorSet.all.length);

    // Should have semantic labels
    expect(find.bySemanticsLabel(l10n.colorElectric), findsOneWidget);
    expect(find.bySemanticsLabel(l10n.colorPlum), findsOneWidget);
    expect(find.bySemanticsLabel(l10n.colorBarney), findsOneWidget);
    expect(find.bySemanticsLabel(l10n.colorRaspberry), findsOneWidget);
    expect(find.bySemanticsLabel(l10n.colorFire), findsOneWidget);
    expect(find.bySemanticsLabel(l10n.colorShamrock), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Selects initial color', (tester) async {
    var color = StudentColorSet.jeffGoldplum.light;
    await tester.pumpWidget(TestApp(StudentColorPickerDialog(initialColor: color, studentId: '')));
    await tester.pumpAndSettle();

    // 'Plum' color should be selected
    var predicate = (Widget w) => w is Semantics && w.properties.label == l10n.colorPlum && w.properties.selected!;
    expect(find.byWidgetPredicate(predicate), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays indicator and hides buttons while saving', (tester) async {
    Completer<UserColors> completer = Completer();

    when(interactor.save(any, any)).thenAnswer((_) => completer.future);
    await tester.pumpWidget(TestApp(StudentColorPickerDialog(initialColor: Colors.white, studentId: '')));
    await tester.pumpAndSettle();

    // Select 'shamrock'
    await tester.tap(find.bySemanticsLabel(l10n.colorShamrock));
    await tester.pumpAndSettle();

    // Tap 'Ok'
    await tester.tap(find.bySemanticsLabel(l10n.ok));
    await tester.pump();

    // Should hide buttons and display saving indicator
    expect(find.byType(CircularProgressIndicator), findsOneWidget);
    expect(find.text(l10n.ok), findsNothing);
    expect(find.text(l10n.cancel), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Returns true on successful save', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    BuildContext context = tester.state(find.byType(DummyWidget)).context;

    var resultFuture = showDialog(
      context: context,
      builder:(_) => StudentColorPickerDialog(initialColor: Colors.white, studentId: ''),
    );
    await tester.pumpAndSettle();

    // Select a color
    await tester.tap(find.bySemanticsLabel(l10n.colorShamrock));
    await tester.pumpAndSettle();

    // Tap 'Ok' and wait for the result
    await tester.tap(find.text(l10n.ok));
    await tester.pumpAndSettle();
    var result = await resultFuture;

    // Should have returned true
    expect(result, isTrue);
  });

  testWidgetsWithAccessibilityChecks('Returns false if color did not change on save', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    BuildContext context = tester.state(find.byType(DummyWidget)).context;

    // Set initial color to 'shamrock'
    var resultFuture = showDialog(
      context: context,
      builder:(_) => StudentColorPickerDialog(initialColor: StudentColorSet.shamrock.light, studentId: ''),
    );
    await tester.pumpAndSettle();

    // Select 'shamrock'
    await tester.tap(find.bySemanticsLabel(l10n.colorShamrock));
    await tester.pumpAndSettle();

    // Tap 'Ok' and wait for the result
    await tester.tap(find.bySemanticsLabel(l10n.ok));
    var result = await resultFuture;

    // Should have returned false
    expect(result, isFalse);
  });

  testWidgetsWithAccessibilityChecks('Returns false if canceled', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    var resultFuture = showDialog(
      context: context,
      builder:(_) => StudentColorPickerDialog(initialColor: Colors.white, studentId: ''),
    );
    await tester.pumpAndSettle();

    // Tap 'cancel' and wait for the result
    await tester.tap(find.bySemanticsLabel(l10n.cancel));
    var result = await resultFuture;

    // Should have returned false
    expect(result, isFalse);
  });

  testWidgetsWithAccessibilityChecks('Displays error message if saving failed', (tester) async {
    when(interactor.save(any, any)).thenAnswer((_) async => throw 'Fake error');

    await tester.pumpWidget(TestApp(StudentColorPickerDialog(initialColor: Colors.white, studentId: '')));
    await tester.pumpAndSettle();

    // Select a color
    await tester.tap(find.bySemanticsLabel(l10n.colorShamrock));
    await tester.pumpAndSettle();

    // Tap 'Ok'
    await tester.tap(find.bySemanticsLabel(l10n.ok));
    await tester.pumpAndSettle();

    // Should show error message
    expect(find.text(l10n.errorSavingColor), findsOneWidget);
  });
}