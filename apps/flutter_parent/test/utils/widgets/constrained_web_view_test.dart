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
import 'package:flutter_parent/utils/common_widgets/constrained_web_view.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../accessibility_utils.dart';
import '../test_app.dart';

void main() {
  group('completely empty', () {
    testWidgetsWithAccessibilityChecks('Shows an empty container with null content and null label', (tester) async {
      await tester.pumpWidget(TestApp(ConstrainedWebView(content: null)));
      await tester.pump();

      expect(find.descendant(of: find.byType(ConstrainedWebView), matching: find.byType(Container)), findsOneWidget);
      expect(find.byType(WebView), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('Shows an empty container with empty content and empty label', (tester) async {
      await tester.pumpWidget(TestApp(ConstrainedWebView(content: '', emptyDescription: '')));
      await tester.pump();

      expect(find.descendant(of: find.byType(ConstrainedWebView), matching: find.byType(Container)), findsOneWidget);
      expect(find.byType(WebView), findsNothing);
    });
  });

  group('empty description', () {
    testWidgetsWithAccessibilityChecks('Shows with null content', (tester) async {
      final empty = 'empty';

      await tester.pumpWidget(TestApp(ConstrainedWebView(content: null, emptyDescription: empty)));
      await tester.pump();

      expect(find.descendant(of: find.byType(ConstrainedWebView), matching: find.text(empty)), findsOneWidget);
      expect(find.byType(WebView), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('Shows with empty content', (tester) async {
      final empty = 'empty';

      await tester.pumpWidget(TestApp(ConstrainedWebView(content: '', emptyDescription: empty)));
      await tester.pump();

      expect(find.descendant(of: find.byType(ConstrainedWebView), matching: find.text(empty)), findsOneWidget);
      expect(find.byType(WebView), findsNothing);
    });
  });

  testWidgetsWithAccessibilityChecks('adds padding to empty text', (tester) async {
    final empty = 'empty';
    final horizontal = 16.0;

    await tester.pumpWidget(TestApp(ConstrainedWebView(
      content: null,
      emptyDescription: empty,
      horizontalPadding: horizontal,
    )));
    await tester.pump();

    final padding = find.descendant(of: find.byType(ConstrainedWebView), matching: find.byType(Padding));
    expect(padding, findsOneWidget);
    expect((tester.widget(padding) as Padding).padding, EdgeInsets.symmetric(horizontal: horizontal));
  });
}
