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
import 'package:flutter_parent/screens/pairing/pairing_code_dialog.dart';
import 'package:flutter_parent/screens/pairing/pairing_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';
import 'pairing_util_test.dart';

void main() {
  AppLocalizations l10n = AppLocalizations();

  MockPairingInteractor interactor = MockPairingInteractor();

  setupTestLocator((locator) {
    locator.registerLazySingleton<PairingInteractor>(() => interactor);
  });

  setUp(() {
    reset(interactor);
  });

  testWidgetsWithAccessibilityChecks('Displays UI elements', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    showDialog(context: context, builder: (_) => PairingCodeDialog(null));
    await tester.pumpAndSettle();

    // Title
    expect(find.text(l10n.addStudent), findsOneWidget);

    // Message
    expect(find.text(l10n.pairingCodeEntryExplanation), findsOneWidget);

    // Input
    expect(find.byType(TextFormField), findsOneWidget);

    // Buttons
    var button = find.byType(TextButton);
    expect(find.descendant(of: button, matching: find.text(l10n.cancel.toUpperCase())), findsOneWidget);
    expect(find.descendant(of: button, matching: find.text(l10n.ok.toUpperCase())), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays error on fail', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    showDialog(context: context, builder: (_) => PairingCodeDialog(null));
    await tester.pumpAndSettle();

    when(interactor.pairWithStudent(any)).thenAnswer((_) async => false);
    await tester.tap(find.text(l10n.ok));
    await tester.pumpAndSettle();

    expect(find.text(l10n.errorPairingFailed), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('screen returns true when paired successfully', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    var dialogFuture = showDialog(context: context, builder: (_) => PairingCodeDialog(null));
    await tester.pumpAndSettle();

    when(interactor.pairWithStudent(any)).thenAnswer((_) async => true);
    await tester.tap(find.text(l10n.ok));
    await tester.pumpAndSettle();

    expect(await dialogFuture, isTrue);
    expect(find.byType(PairingCodeDialog), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('screen returns false when no pairing performed', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    var dialogFuture = showDialog(context: context, builder: (_) => PairingCodeDialog(null));
    await tester.pumpAndSettle();

    await tester.tap(find.text(l10n.cancel.toUpperCase()));
    await tester.pumpAndSettle();

    expect(await dialogFuture, isFalse);
    expect(find.byType(PairingCodeDialog), findsNothing);
  });
}
