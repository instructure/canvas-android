/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:flutter_parent/utils/common_widgets/web_view/canvas_html.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/canvas_web_view.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../../accessibility_utils.dart';
import '../../test_app.dart';

void main() {
  final interactor = _MockWebViewInteractor();

  setupTestLocator((locator) => locator.registerFactory<WebContentInteractor>(() => interactor));

  setUp(() async {
    reset(interactor);
  });

  test('throws assertion error when content is null', () {
    expect(() => CanvasHtml(null), throwsAssertionError);
  });

  testWidgetsWithAccessibilityChecks('Shows an empty container with empty content and empty label', (tester) async {
    final empty = 'empty';

    await tester.pumpWidget(TestApp(CanvasHtml('', emptyDescription: empty)));
    await tester.pump(); // Let the widget build
    await tester.pump(); // Let the webview future finish

    expect(find.descendant(of: find.byType(CanvasWebView), matching: find.text(empty)), findsOneWidget);
    expect(find.byType(WebView), findsNothing);
  });
}

class _MockWebViewInteractor extends Mock implements WebContentInteractor {}
