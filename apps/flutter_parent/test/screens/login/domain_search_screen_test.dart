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

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/school_domain.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_interactor.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_screen.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:flutter_parent/screens/web_login/web_login_screen.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';

import '../../utils/test_app.dart';

void main() {
  AppLocalizations l10n = AppLocalizations();
  final analytics = _MockAnalytics();

  _setupLocator(MockInteractor interactor, {WebLoginInteractor webInteractor}) {
    final _locator = GetIt.instance;
    _locator.reset();
    _locator.registerLazySingleton(() => QuickNav());
    _locator.registerLazySingleton<Analytics>(() => analytics);

    _locator.registerFactory<DomainSearchInteractor>(() => interactor);
    _locator.registerFactory<WebLoginInteractor>(() => webInteractor ?? _MockWebLoginInteractor());
  }

  setUp(() {
    reset(analytics);
  });

  testWidgets('default state', (tester) async {
    _setupLocator(MockInteractor());
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    expect(find.text(l10n.findSchool), findsOneWidget);
    expect(find.text(l10n.next), findsOneWidget);
    expect(
      tester
          .widget<MaterialButton>(find.ancestor(of: find.text(l10n.next), matching: find.byType(MaterialButton)))
          .enabled,
      false,
    );
    expect(find.text(l10n.domainSearchInputHint), findsOneWidget);
    expect(find.text(l10n.domainSearchHelpLabel), findsOneWidget);
  });

  testWidgets('Displays search results', (WidgetTester tester) async {
    int count = 5;
    MockInteractor interactor = MockInteractor();
    when(interactor.performSearch(any)).thenAnswer((_) => Future.value(List.generate(
          count,
          (idx) => SchoolDomain((sd) => sd
            ..domain = 'test$idx.domains.com'
            ..name = 'Test domain $idx'),
        )));
    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();
    await tester.enterText(find.byType(TextField), 'test');
    await tester.pumpAndSettle();

    for (int i = 0; i < count; i++) {
      expect(find.text('Test domain $i'), findsOneWidget);
    }
  });

  testWidgets('Enables "next" button if query is not empty', (WidgetTester tester) async {
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    var nextButton =
        tester.widget<MaterialButton>(find.ancestor(of: find.text(l10n.next), matching: find.byType(MaterialButton)));
    expect(nextButton.enabled, false);

    await tester.enterText(find.byType(TextField), 'aa');
    await tester.pumpAndSettle();

    nextButton =
        tester.widget<MaterialButton>(find.ancestor(of: find.text(l10n.next), matching: find.byType(MaterialButton)));
    expect(nextButton.enabled, true);
  });

  testWidgets('Large result sets do not hide help button', (WidgetTester tester) async {
    MockInteractor interactor = MockInteractor();
    when(interactor.performSearch(any)).thenAnswer((_) => Future.value(List.generate(
          100,
          (idx) => SchoolDomain((sd) => sd
            ..domain = 'test$idx.domains.com'
            ..name = 'Test domain $idx'),
        )));
    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();
    await tester.enterText(find.byType(TextField), 'test');
    await tester.pumpAndSettle();

    expect(find.text(l10n.domainSearchHelpLabel), findsOneWidget);
  });

  testWidgets('Displays results for 2-character query', (WidgetTester tester) async {
    var interactor = MockInteractor();
    when(interactor.performSearch(any)).thenAnswer((_) => Future.value([
          SchoolDomain((sd) => sd
            ..domain = ''
            ..name = 'Domain Result'),
        ]));
    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    // Two characters should perform a search
    await tester.enterText(find.byType(TextField), 'aa');
    await tester.pumpAndSettle();
    expect(find.text('Domain Result'), findsOneWidget);

    // One character should not search and should remove results
    await tester.enterText(find.byType(TextField), 'a');
    await tester.pumpAndSettle();
    expect(find.text('Domain Result'), findsNothing);
  });

  testWidgets('Displays error', (WidgetTester tester) async {
    MockInteractor interactor = MockInteractor();
    when(interactor.performSearch(any)).thenAnswer((_) => Future.error(''));
    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();
    await tester.enterText(find.byType(TextField), 'test');
    await tester.pumpAndSettle();

    expect(find.text(l10n.noDomainResults('test')), findsOneWidget);
  });

  testWidgets('Clear button shows for non-empty query', (WidgetTester tester) async {
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    // Should not show by default
    expect(find.byKey(Key('clear-query')), findsNothing);

    // Add single character query
    await tester.enterText(find.byType(TextField), 'a');
    await tester.pumpAndSettle();

    // Button should now show
    expect(find.byKey(Key('clear-query')), findsOneWidget);

    // Add single character query
    await tester.enterText(find.byType(TextField), '');
    await tester.pumpAndSettle();

    // Button should no longer show
    expect(find.byKey(Key('clear-query')), findsNothing);
  });

  testWidgets('Clear button clears text', (WidgetTester tester) async {
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.byType(TextField));
    await tester.enterText(find.byType(TextField), 'testing123');
    await tester.pumpAndSettle();
    expect(tester.widget<TextField>(find.byType(TextField)).controller.text, 'testing123');
    expect(find.byKey(Key('clear-query')), findsOneWidget);

    await tester.tap(find.byKey(Key('clear-query')));
    await tester.pump();
    expect(tester.widget<TextField>(find.byType(TextField)).controller.text, '');
  });

  testWidgets('Does not show stale search results', (WidgetTester tester) async {
    int queryCount = 0;

    MockInteractor interactor = MockInteractor();
    when(interactor.performSearch(any)).thenAnswer((_) => Future.sync(() {
          if (queryCount == 0) {
            queryCount++;
            return Future.delayed(
                Duration(milliseconds: 1000),
                () => [
                      SchoolDomain((sd) => sd
                        ..domain = '1'
                        ..name = 'Query One')
                    ]);
          } else if (queryCount == 1) {
            queryCount++;
            return Future.delayed(
                Duration(milliseconds: 100),
                () => [
                      SchoolDomain((sd) => sd
                        ..domain = '2'
                        ..name = 'Query Two')
                    ]);
          } else {
            return Future.delayed(
                Duration(milliseconds: 500),
                () => [
                      SchoolDomain((sd) => sd
                        ..domain = '3'
                        ..name = 'Query Three')
                    ]);
          }
        }));

    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField), 'one');
    await tester.enterText(find.byType(TextField), 'two');
    await tester.enterText(find.byType(TextField), 'three');
    await tester.pumpAndSettle(Duration(milliseconds: 1000)); // Allow plenty of time for first async call to finish
    expect(find.text('Query One'), findsNothing);
    expect(find.text('Query Two'), findsNothing);
    expect(find.text('Query Three'), findsOneWidget);
  });

  testWidgets('Displays and hides help dialog', (WidgetTester tester) async {
    _setupLocator(MockInteractor());
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    // Tap the help button
    await tester.tap(find.byKey(Key('help-button')));
    await tester.pump();

    // Make sure alert dialog is showing
    expect(find.byType(AlertDialog), findsOneWidget);
    verify(analytics.logEvent(AnalyticsEventConstants.HELP_DOMAIN_SEARCH));

    // Tap the Ok button
    await tester.tap(find.descendant(of: find.byType(AlertDialog), matching: find.text('OK')));
    await tester.pump();

    // Make sure alert dialog is no longer showing
    expect(find.byType(AlertDialog), findsNothing);
  });

  testWidgets('Tapping "Canvas Guides" launches url', (WidgetTester tester) async {
    var interactor = MockInteractor();
    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    // Tap the help button
    await tester.tap(find.byKey(Key('help-button')));
    await tester.pump();

    // Get text selection for 'Canvas Support' span
    var targetText = l10n.canvasGuides;
    var bodyWidget = tester.widget<Text>(find.byKey(DomainSearchScreen.helpDialogBodyKey));
    var bodyText = bodyWidget.textSpan.toPlainText();
    var index = bodyText.indexOf(targetText);
    var selection = TextSelection(baseOffset: index, extentOffset: index + targetText.length);

    // Get clickable area
    RenderParagraph box = DomainSearchScreen.helpDialogBodyKey.currentContext.findRenderObject();
    var bodyOffset = box.localToGlobal(Offset.zero);
    var textOffset = box.getBoxesForSelection(selection)[0].toRect().center;

    await tester.tapAt(bodyOffset + textOffset);

    verify(interactor.openCanvasGuides());
  });

  testWidgets('Tapping "Canvas Support" launches url', (WidgetTester tester) async {
    var interactor = MockInteractor();
    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    // Tap the help button
    await tester.tap(find.byKey(Key('help-button')));
    await tester.pump();

    // Get text selection for 'Canvas Support' span
    var targetText = l10n.canvasSupport;
    var bodyWidget = tester.widget<Text>(find.byKey(DomainSearchScreen.helpDialogBodyKey));
    var bodyText = bodyWidget.textSpan.toPlainText();
    var index = bodyText.indexOf(targetText);
    var selection = TextSelection(baseOffset: index, extentOffset: index + targetText.length);

    // Get clickable area
    RenderParagraph box = DomainSearchScreen.helpDialogBodyKey.currentContext.findRenderObject();
    var bodyOffset = box.localToGlobal(Offset.zero);
    var textOffset = box.getBoxesForSelection(selection)[0].toRect().center;

    await tester.tapAt(bodyOffset + textOffset);
    verify(interactor.openCanvasSupport());
  });

  testWidgets('Navigates to Login page from search result', (WidgetTester tester) async {
    var interactor = MockInteractor();
    when(interactor.performSearch(any)).thenAnswer((_) => Future.value([
          SchoolDomain((sd) => sd
            ..domain = 'mobileqa.instructure.com'
            ..name = 'Result')
        ]));
    final webInteractor = _MockWebLoginInteractor();
    when(webInteractor.mobileVerify(any)).thenAnswer((_) => Future.value(MobileVerifyResult()));
    _setupLocator(interactor, webInteractor: webInteractor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField), 'test');
    await tester.pumpAndSettle(Duration(seconds: 5));
    expect(find.text('Result'), findsOneWidget);

    await tester.tap(find.text('Result'));
    await tester.pumpAndSettle();

    expect(find.byType(WebLoginScreen), findsOneWidget);
  });

  testWidgets('Navigates to Login page from "Next" button', (WidgetTester tester) async {
    var interactor = MockInteractor();
    when(interactor.performSearch(any)).thenAnswer((_) => Future.value([
          SchoolDomain((sd) => sd
            ..domain = 'mobileqa.instructure.com'
            ..name = 'Result')
        ]));
    final webInteractor = _MockWebLoginInteractor();
    when(webInteractor.mobileVerify(any)).thenAnswer((_) => Future.value(MobileVerifyResult()));
    _setupLocator(interactor, webInteractor: webInteractor);

    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField), 'mobileqa');
    await tester.pumpAndSettle();

    await tester.tap(find.text(l10n.next));
    await tester.pumpAndSettle();

    expect(find.byType(WebLoginScreen), findsOneWidget);
  });

  testWidgets('Navigates to Login page from keyboard submit button', (WidgetTester tester) async {
    final interactor = MockInteractor();
    when(interactor.performSearch(any)).thenAnswer((_) => Future.value([
          SchoolDomain((sd) => sd
            ..domain = 'mobileqa.instructure.com'
            ..name = 'Result')
        ]));
    final webInteractor = _MockWebLoginInteractor();
    when(webInteractor.mobileVerify(any)).thenAnswer((_) => Future.value(MobileVerifyResult()));
    _setupLocator(interactor, webInteractor: webInteractor);

    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField), 'mobileqa');
    await tester.pumpAndSettle();

    await tester.testTextInput.receiveAction(TextInputAction.done);
    await tester.pumpAndSettle();

    expect(find.byType(WebLoginScreen), findsOneWidget);
  });
}

class MockInteractor extends Mock implements DomainSearchInteractor {}

class _MockWebLoginInteractor extends Mock implements WebLoginInteractor {}

class _MockAnalytics extends Mock implements Analytics {}
