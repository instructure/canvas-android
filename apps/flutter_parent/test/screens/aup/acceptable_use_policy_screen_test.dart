// Copyright (C) 2023 - present Instructure, Inc.
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
import 'package:flutter_parent/models/terms_of_service.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/aup/acceptable_use_policy_interactor.dart';
import 'package:flutter_parent/screens/aup/acceptable_use_policy_screen.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  testWidgets('Submit button disabled when switch is not checked',
      (tester) async {
    var interactor = MockAcceptableUsePolicyInteractor();
    var nav = MockQuickNav();
    setupTestLocator((locator) {
      locator.registerFactory<AcceptableUsePolicyInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => nav);
    });

    await tester.pumpWidget(TestApp(AcceptableUsePolicyScreen()));
    await tester.pump();
    var textButton = tester.widget<TextButton>(find.ancestor(
        of: find.text(AppLocalizations().acceptableUsePolicyConfirm),
        matching: find.byWidgetPredicate((widget) => widget is TextButton)));
    expect(textButton.enabled, isFalse);
  });

  testWidgets('Submit button enabled when switch is checked', (tester) async {
    var interactor = MockAcceptableUsePolicyInteractor();
    var nav = MockQuickNav();
    setupTestLocator((locator) {
      locator.registerFactory<AcceptableUsePolicyInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => nav);
    });

    await tester.pumpWidget(TestApp(AcceptableUsePolicyScreen()));
    await tester.pump();

    await tester.tap(find.byWidgetPredicate((widget) => widget is Switch));
    await tester.pumpAndSettle();

    var textButton = tester.widget<TextButton>(find.ancestor(
        of: find.text(AppLocalizations().acceptableUsePolicyConfirm),
        matching: find.byWidgetPredicate((widget) => widget is TextButton)));
    expect(textButton.enabled, isTrue);
  });

  testWidgets('Submit button navigates to splash screen', (tester) async {
    var interactor = MockAcceptableUsePolicyInteractor();
    var nav = MockQuickNav();
    setupTestLocator((locator) {
      locator.registerFactory<AcceptableUsePolicyInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => nav);
    });

    when(interactor.acceptTermsOfUse()).thenAnswer((_) async => User());

    await tester.pumpWidget(TestApp(AcceptableUsePolicyScreen()));
    await tester.pump();

    await tester.tap(find.byWidgetPredicate((widget) => widget is Switch));
    await tester.pumpAndSettle();

    await tester.tap(find.ancestor(
        of: find.text(AppLocalizations().acceptableUsePolicyConfirm),
        matching: find.byWidgetPredicate((widget) => widget is TextButton)));

    await tester.pump();

    verify(nav.pushRouteAndClearStack(any, '/'));
  });
}