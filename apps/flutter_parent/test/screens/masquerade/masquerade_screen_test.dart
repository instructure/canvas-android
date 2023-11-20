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

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/masquerade/masquerade_screen.dart';
import 'package:flutter_parent/screens/masquerade/masquerade_screen_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  MockMasqueradeScreenInteractor interactor = MockMasqueradeScreenInteractor();
  String siteAdminDomain = 'https://siteadmin.instructure.com';
  String normalDomain = 'https://example.instructure.com';

  AppLocalizations l10n = AppLocalizations();

  Key domainKey = Key('domain-input');
  Key userIdKey = Key('user-id-input');

  setupTestLocator((locator) {
    locator.registerFactory<MasqueradeScreenInteractor>(() => interactor);
  });

  setUp(() {
    reset(interactor);

    // For getDomain, return normalDomain by default
    when(interactor.getDomain()).thenReturn(normalDomain);

    // For sanitizeDomain, return actual implementation
    when(interactor.sanitizeDomain(any)).thenAnswer((inv) {
      String domain = inv.positionalArguments[0];
      return MasqueradeScreenInteractor().sanitizeDomain(domain);
    });
  });

  // TODO Fix test
  testWidgetsWithAccessibilityChecks(
    'Animates red panda mask',
    (tester) async {
      Duration animationInterval = Duration(milliseconds: 2000);
      double epsilon = 1.0; // Allow variance within one logical pixel

      var screenCenter = WidgetsBinding.instance.renderView.configuration.size.width / 2;
      var offset = MasqueradeScreenState.pandaMaskOffset.dx / 2;

      Offset left = Offset(screenCenter - offset, 137.0);
      Offset center = Offset(screenCenter, 97.0);
      Offset right = Offset(screenCenter + offset, 137.0);

      await tester.pumpWidget(TestApp(MasqueradeScreen()));
      await tester.pump();
      await tester.pump();

      var mask = find.byKey(Key('red-panda-mask'));

      // Mask should start at the left offset
      expect(tester.getCenter(mask), offsetMoreOrLessEquals(left, epsilon: epsilon));

      // After the first animation interval it should be at the center offset
      await tester.pump(animationInterval);
      expect(tester.getCenter(mask), offsetMoreOrLessEquals(center, epsilon: epsilon));

      // At the next interval it should be at the right offset
      await tester.pump(animationInterval);
      expect(tester.getCenter(mask), offsetMoreOrLessEquals(right, epsilon: epsilon));

      // It should then reverse and go back to the center offset
      await tester.pump(animationInterval);
      expect(tester.getCenter(mask), offsetMoreOrLessEquals(center, epsilon: epsilon));

      // And finally it should go back to the starting offset
      await tester.pump(animationInterval);
      expect(tester.getCenter(mask), offsetMoreOrLessEquals(left, epsilon: epsilon));
    },
    a11yExclusions: {A11yExclusion.multipleNodesWithSameLabel}, skip: true);

  testWidgetsWithAccessibilityChecks(
    'Disables domain input and populates with domain if not siteadmin',
    (tester) async {
      await tester.pumpWidget(TestApp(MasqueradeScreen()));
      await tester.pump();

      TextField input = tester.widget(find.byKey(domainKey));
      expect(input.enabled, isFalse);
      expect(input.controller!.text, normalDomain);
    },
    a11yExclusions: {A11yExclusion.multipleNodesWithSameLabel},
  );

  testWidgetsWithAccessibilityChecks(
    'Enables domain input if siteadmin',
    (tester) async {
      when(interactor.getDomain()).thenReturn(siteAdminDomain);
      await tester.pumpWidget(TestApp(MasqueradeScreen()));
      await tester.pump();

      TextField input = tester.widget(find.byKey(domainKey));
      expect(input.enabled, isTrue);
      expect(input.controller!.text, isEmpty);
    },
    a11yExclusions: {A11yExclusion.multipleNodesWithSameLabel},
  );

  testWidgetsWithAccessibilityChecks(
    'Shows error for invalid domain',
    (tester) async {
      when(interactor.getDomain()).thenReturn(siteAdminDomain);
      await tester.pumpWidget(TestApp(MasqueradeScreen()));
      await tester.pump();

      // Tap the 'Act As User' button - domain input should be empty
      await tester.tap(find.byType(ElevatedButton));
      await tester.pump();

      // Error message should show
      TextField input = tester.widget(find.byKey(domainKey));
      expect(input.decoration?.errorText, l10n.domainInputError);
      expect(find.text(l10n.domainInputError), findsOneWidget);

      // Input a valid domain
      await tester.enterText(find.byKey(domainKey), normalDomain);
      await tester.pump();

      // Entering text should have cleared the error
      input = tester.widget(find.byKey(domainKey));
      expect(input.decoration?.errorText, isNull);
      expect(find.text(l10n.domainInputError), findsNothing);

      // Tap the 'Act As User' button again
      await tester.tap(find.byType(ElevatedButton));
      await tester.pump();

      // The error should not be displayed
      input = tester.widget(find.byKey(domainKey));
      expect(input.decoration?.errorText, isNull);
      expect(find.text(l10n.domainInputError), findsNothing);
    },
    a11yExclusions: {A11yExclusion.multipleNodesWithSameLabel},
  );

  testWidgetsWithAccessibilityChecks(
    'Shows error for invalid userId',
    (tester) async {
      when(interactor.getDomain()).thenReturn(siteAdminDomain);
      await tester.pumpWidget(TestApp(MasqueradeScreen()));
      await tester.pump();

      // Tap the 'Act As User' button - user id input should be empty
      await tester.tap(find.byType(ElevatedButton));
      await tester.pump();

      // Error message should show
      TextField input = tester.widget(find.byKey(userIdKey));
      expect(input.decoration?.errorText, l10n.userIdInputError);
      expect(find.text(l10n.userIdInputError), findsOneWidget);

      // Input a valid user id
      await tester.enterText(find.byKey(userIdKey), '123');
      await tester.pump();

      // Entering text should have cleared the error
      input = tester.widget(find.byKey(userIdKey));
      expect(input.decoration?.errorText, isNull);
      expect(find.text(l10n.userIdInputError), findsNothing);

      // Tap the 'Act As User' button again
      await tester.tap(find.byType(ElevatedButton));
      await tester.pump();

      // The error should not be displayed
      input = tester.widget(find.byKey(userIdKey));
      expect(input.decoration?.errorText, isNull);
      expect(find.text(l10n.userIdInputError), findsNothing);
    },
    a11yExclusions: {A11yExclusion.multipleNodesWithSameLabel},
  );

  testWidgetsWithAccessibilityChecks(
    'Displays loading indicator while attempting to start the masquerade',
    (tester) async {
      Completer<bool> completer = Completer();
      when(interactor.startMasquerading(any, any)).thenAnswer((_) => completer.future);

      await tester.pumpWidget(TestApp(MasqueradeScreen()));
      await tester.pump();

      // Enter a user id and press the button
      await tester.enterText(find.byKey(userIdKey), '123');
      await tester.tap(find.byType(ElevatedButton));
      await tester.pump();

      expect(find.byType(ElevatedButton), findsNothing);
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    },
    a11yExclusions: {A11yExclusion.multipleNodesWithSameLabel},
  );

  testWidgetsWithAccessibilityChecks(
    'Displays error message on masquerade fail',
    (tester) async {
      Completer<bool> completer = Completer();
      when(interactor.startMasquerading(any, any)).thenAnswer((_) => completer.future);

      await tester.pumpWidget(TestApp(MasqueradeScreen()));
      await tester.pump();

      // Enter a user id and press the button
      await tester.enterText(find.byKey(userIdKey), '123');
      await tester.tap(find.byType(ElevatedButton));
      await tester.pump();

      // Should show loading state
      expect(find.byType(ElevatedButton), findsNothing);
      expect(find.byType(CircularProgressIndicator), findsOneWidget);

      completer.complete(false);
      await tester.pump();

      // Should show error message and no loading state
      expect(find.text(l10n.actAsUserError), findsOneWidget);
      expect(find.byType(ElevatedButton), findsOneWidget);
      expect(find.byType(CircularProgressIndicator), findsNothing);
    },
    a11yExclusions: {A11yExclusion.multipleNodesWithSameLabel},
  );

  testWidgetsWithAccessibilityChecks(
    'Respawns on masquerade success',
    (tester) async {
      when(interactor.getDomain()).thenReturn(siteAdminDomain);
      when(interactor.startMasquerading(any, any)).thenAnswer((_) async => true);

      await tester.pumpWidget(TestApp(MasqueradeScreen()));
      await tester.pump();

      // Enter data and tap the button
      await tester.enterText(find.byKey(domainKey), normalDomain);
      await tester.enterText(find.byKey(userIdKey), '123');
      await tester.tap(find.byType(ElevatedButton));
      await tester.pumpAndSettle();

      // Respawn should have created a new screen with empty fields
      TextField domainInput = tester.widget(find.byKey(domainKey));
      expect(domainInput.controller?.text, isEmpty);

      TextField userIdInput = tester.widget(find.byKey(userIdKey));
      expect(userIdInput.controller?.text, isEmpty);
    },
    a11yExclusions: {A11yExclusion.multipleNodesWithSameLabel},
  );
}
