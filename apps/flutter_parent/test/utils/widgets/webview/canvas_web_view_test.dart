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
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/canvas_web_view.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../../accessibility_utils.dart';
import '../../platform_config.dart';
import '../../test_app.dart';
import '../../test_helpers/mock_helpers.mocks.dart';

void main() {
  final interactor = _MockWebViewInteractor();

  setupTestLocator((locator) => locator.registerFactory<WebContentInteractor>(() => interactor));

  setUp(() async {
    reset(interactor);
  });

  group('completely empty', () {
    testWidgetsWithAccessibilityChecks('Shows an empty container with null content and null label', (tester) async {
      await tester.pumpWidget(TestApp(CanvasWebView(content: null)));
      await tester.pump();

      expect(find.descendant(of: find.byType(CanvasWebView), matching: find.byType(Container)), findsWidgets);
      expect(find.byType(WebView), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('Shows an empty container with empty content and empty label', (tester) async {
      await tester.pumpWidget(TestApp(CanvasWebView(content: '', emptyDescription: '')));
      await tester.pump();

      expect(find.descendant(of: find.byType(CanvasWebView), matching: find.byType(Container)), findsWidgets);
      expect(find.byType(WebView), findsNothing);
    });
  });

  group('empty description', () {
    testWidgetsWithAccessibilityChecks('Shows with null content', (tester) async {
      final empty = 'empty';

      await tester.pumpWidget(TestApp(CanvasWebView(content: null, emptyDescription: empty)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the webview future finish

      expect(find.descendant(of: find.byType(CanvasWebView), matching: find.text(empty)), findsOneWidget);
      expect(find.byType(WebView), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('Shows with empty content', (tester) async {
      final empty = 'empty';

      await tester.pumpWidget(TestApp(CanvasWebView(content: '', emptyDescription: empty)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the webview future finish

      expect(find.descendant(of: find.byType(CanvasWebView), matching: find.text(empty)), findsOneWidget);
      expect(find.byType(WebView), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('adds padding to empty text', (tester) async {
      final empty = 'empty';
      final horizontal = 16.0;

      await tester.pumpWidget(TestApp(CanvasWebView(
        content: null,
        emptyDescription: empty,
        horizontalPadding: horizontal,
      )));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the webview future finish

      final padding = find.descendant(of: find.byType(CanvasWebView), matching: find.byType(Padding));
      expect(padding, findsOneWidget);
      expect((tester.widget(padding) as Padding).padding, EdgeInsets.symmetric(horizontal: horizontal));
    });
  });

  group('auth content', () {
    final config = PlatformConfig(initWebview: true);

    testWidgetsWithAccessibilityChecks('shows loading while waiting to authenticate content', (tester) async {
      final content = 'html_content';
      when(interactor.authContent(content, AppLocalizations().launchExternalTool)).thenAnswer((_) async => content);

      await tester.pumpWidget(TestApp(
        CanvasWebView(content: content, authContentIfNecessary: true),
        platformConfig: config,
      ));
      await tester.pump(); // Let the webview build

      expect(find.byType(WebView), findsNothing);
      expect(find.byType(LoadingIndicator), findsOneWidget);
      verify(interactor.authContent(any, any)).called(1);
    });

    testWidgetsWithAccessibilityChecks('does not call to authenticate content', (tester) async {
      final content = 'html_content';
      await tester.pumpWidget(TestApp(
        CanvasWebView(content: content, authContentIfNecessary: false),
        platformConfig: config,
      ));
      await tester.pump(); // Let the webview build
      await tester.pump(); // Let the future finish

      expect(find.byType(WebView), findsOneWidget);
      verifyNever(interactor.authContent(any, any));
    });

    testWidgetsWithAccessibilityChecks('calls to authenticate content', (tester) async {
      final content = 'html_content';
      when(interactor.authContent(content, AppLocalizations().launchExternalTool)).thenAnswer((_) async => content);

      await tester.pumpWidget(TestApp(CanvasWebView(content: content), platformConfig: config));
      await tester.pump(); // Let the webview build
      await tester.pump(); // Let the future finish

      expect(find.byType(WebView), findsOneWidget);
      verify(interactor.authContent(content, AppLocalizations().launchExternalTool)).called(1);
    });
  });
}

class _MockWebViewInteractor extends Mock implements MockWebContentInteractor {
  @override
  JavascriptChannel ltiToolPressedChannel(handler) {
    return WebContentInteractor().ltiToolPressedChannel(handler!);
  }
}
