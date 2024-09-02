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
import 'package:flutter_parent/utils/common_widgets/web_view/simple_web_view_screen.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../../accessibility_utils.dart';
import '../../platform_config.dart';
import '../../test_app.dart';

void main() {
  setupTestLocator((locator) => locator.registerFactory<WebContentInteractor>(() => WebContentInteractor()));
  final config = PlatformConfig(initWebview: true);

  testWidgetsWithAccessibilityChecks('shows title in app bar', (tester) async {
    final url = 'https://www.google.com';
    final title = 'title';

    await tester.pumpWidget(TestApp(SimpleWebViewScreen(url, title, true), platformConfig: config));
    await tester.pump();

    expect(find.text(title), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows webview', (tester) async {
    final url = 'https://www.google.com';
    final title = 'title';

    await tester.pumpWidget(TestApp(SimpleWebViewScreen(url, title, true), platformConfig: config));
    await tester.pump();

    expect(find.byType(WebView), findsOneWidget);
  });

  // Can only test the case where the controller isn't set
  testWidgetsWithAccessibilityChecks('handles back press', (tester) async {
    final url = 'https://www.google.com';
    final title = 'title';

    await TestApp.showWidgetFromTap(tester, (context) {
      return Navigator.of(context).push(MaterialPageRoute(builder: (context) => SimpleWebViewScreen(url, title, true)));
    }, config: config);

    expect(find.byType(WebView), findsOneWidget);

    await tester.pageBack();
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsNothing);
  });
}
