/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_parent/models/school_domain.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_interactor.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_screen.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';

import '../utils/test_app.dart';

void main() {
  _setupLocator(MockInteractor interactor) {
    final _locator = GetIt.instance;
    _locator.reset();
    _locator.registerFactory<DomainSearchInteractor>(() => interactor);
  }

  testWidgets("default state", (tester) async {
    _setupLocator(MockInteractor());
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    expect(find.text("Find School or District"), findsOneWidget);
    expect(find.text("NEXT"), findsOneWidget);
    expect(
      tester.widget<FlatButton>(find.ancestor(of: find.text("NEXT"), matching: find.byType(FlatButton))).enabled,
      false,
    );
    expect(find.text("Enter school name or district..."), findsOneWidget);
    expect(find.text("How do I find my school or district?"), findsOneWidget);
  });

  testWidgets("Displays search results", (WidgetTester tester) async {
    int count = 5;
    MockInteractor interactor = MockInteractor()
      ..schoolSearchResponse = (_) async {
        return List.generate(
          count,
          (idx) => SchoolDomain((sd) => sd
            ..domain = "test$idx.domains.com"
            ..name = "Test domain $idx"),
        );
      };
    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();
    await tester.enterText(find.byType(TextField), "test");
    await tester.pumpAndSettle();

    for (int i = 0; i < count; i++) {
      expect(find.text("Test domain $i"), findsOneWidget);
    }
  });

  testWidgets("Enables search button if query is not empty", (WidgetTester tester) async {
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    var nextButton = tester.widget<FlatButton>(find.ancestor(of: find.text("NEXT"), matching: find.byType(FlatButton)));
    expect(nextButton.enabled, false);

    await tester.enterText(find.byType(TextField), "aa");
    await tester.pumpAndSettle();

    nextButton = tester.widget<FlatButton>(find.ancestor(of: find.text("NEXT"), matching: find.byType(FlatButton)));
    expect(nextButton.enabled, true);
  });

  testWidgets("Large result sets do not hide help button", (WidgetTester tester) async {
    MockInteractor interactor = MockInteractor()
      ..schoolSearchResponse = (_) async {
        return List.generate(
          100,
          (idx) => SchoolDomain((sd) => sd
            ..domain = "test$idx.domains.com"
            ..name = "Test domain $idx"),
        );
      };
    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();
    await tester.enterText(find.byType(TextField), "test");
    await tester.pumpAndSettle();

    expect(find.text("How do I find my school or district?"), findsOneWidget);
  });

  testWidgets("Displays results for 2-character query", (WidgetTester tester) async {
    var interactor = MockInteractor()
      ..schoolSearchResponse = (_) async => [
            SchoolDomain((sd) => sd
              ..domain = ''
              ..name = "Domain Result"),
          ];
    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    // Two characters should perform a search
    await tester.enterText(find.byType(TextField), "aa");
    await tester.pumpAndSettle();
    expect(find.text("Domain Result"), findsOneWidget);

    // One character should not search and should remove results
    await tester.enterText(find.byType(TextField), "a");
    await tester.pumpAndSettle();
    expect(find.text("Domain Result"), findsNothing);
  });

  testWidgets("Displays error", (WidgetTester tester) async {
    MockInteractor interactor = MockInteractor()..schoolSearchResponse = (_) => Future.error("Uh oh!");
    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();
    await tester.enterText(find.byType(TextField), "test");
    await tester.pumpAndSettle();

    expect(find.text("Unable to find schools matching 'test'"), findsOneWidget);
  });

  testWidgets("Clear button shows for non-empty query", (WidgetTester tester) async {
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    // Should not show by default
    expect(find.byKey(Key("clear-query")), findsNothing);

    // Add single character query
    await tester.enterText(find.byType(TextField), "a");
    await tester.pumpAndSettle();

    // Button should now show
    expect(find.byKey(Key("clear-query")), findsOneWidget);

    // Add single character query
    await tester.enterText(find.byType(TextField), "");
    await tester.pumpAndSettle();

    // Button should no longer show
    expect(find.byKey(Key("clear-query")), findsNothing);
  });

  testWidgets("Clear button clears text", (WidgetTester tester) async {
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.byType(TextField));
    await tester.enterText(find.byType(TextField), "testing123");
    await tester.pumpAndSettle();
    expect(tester.widget<TextField>(find.byType(TextField)).controller.text, "testing123");
    expect(find.byKey(Key("clear-query")), findsOneWidget);

    await tester.tap(find.byKey(Key("clear-query")));
    await tester.pump();
    expect(tester.widget<TextField>(find.byType(TextField)).controller.text, "");
  });

  testWidgets("Does not show stale search results", (WidgetTester tester) async {
    int queryCount = 0;

    MockInteractor interactor = MockInteractor()
      ..schoolSearchResponse = (_) async {
        if (queryCount == 0) {
          queryCount++;
          return Future.delayed(
              Duration(milliseconds: 1000),
              () => [
                    SchoolDomain((sd) => sd
                      ..domain = "1"
                      ..name = "Query One")
                  ]);
        } else if (queryCount == 1) {
          queryCount++;
          return Future.delayed(
              Duration(milliseconds: 100),
              () => [
                    SchoolDomain((sd) => sd
                      ..domain = "2"
                      ..name = "Query Two")
                  ]);
        } else {
          return Future.delayed(
              Duration(milliseconds: 500),
              () => [
                    SchoolDomain((sd) => sd
                      ..domain = "3"
                      ..name = "Query Three")
                  ]);
        }
      };

    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField), "one");
    await tester.enterText(find.byType(TextField), "two");
    await tester.enterText(find.byType(TextField), "three");
    await tester.pumpAndSettle(Duration(milliseconds: 1000)); // Allow plenty of time for first async call to finish
    expect(find.text("Query One"), findsNothing);
    expect(find.text("Query Two"), findsNothing);
    expect(find.text("Query Three"), findsOneWidget);
  });

  // TODO: Enable once WebLoginPage is implemented
  /*testWidgets("Navigates to Login page from search result", (WidgetTester tester) async {
    var interactor = MockInteractor()
      ..schoolSearchResponse = (_) async => [
            SchoolDomain((sd) => sd
              ..name = "mobileqa.instructure.com"
              ..domain = "Result")
          ];
    _setupLocator(interactor);
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField), "test");
    await tester.pumpAndSettle(Duration(seconds: 5));
    expect(find.text("Result"), findsOneWidget);

    await tester.tap(find.text("Result"));
    await tester.pumpAndSettle();
    expect(find.byType(WebLoginPage), findsOneWidget);
  });*/

  // TODO: Enable once WebLoginPage is implemented
  /*testWidgets("Navigates to Login page from 'Next' button", (WidgetTester tester) async {
    await tester.pumpWidget(TestApp(DomainSearchScreen()));
    await tester.pumpAndSettle();

    await tester.enterText(find.byType(TextField), "mobileqa");
    await tester.pumpAndSettle();

    await tester.tap(find.text("NEXT"));
    await tester.pumpAndSettle();

    expect(find.byType(WebLoginPage), findsOneWidget);
  });*/
}

class MockInteractor extends DomainSearchInteractor {
  Future<List<SchoolDomain>> Function(String query) schoolSearchResponse = (_) => Future.error("Mock not implemented");
  Future<List<SchoolDomain>> performSearch(String query) => schoolSearchResponse(query);
}
