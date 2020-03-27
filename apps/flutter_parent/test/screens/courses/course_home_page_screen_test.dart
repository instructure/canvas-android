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
import 'package:flutter_parent/models/page.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_home_page_screen.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';

void main() {
  final l10n = AppLocalizations();

  final _courseId = '123';
  final _page = Page((b) => b..id = '1');
  final _interactor = _MockCourseDetailsInteractor();

  setUp(() {
    reset(_interactor);
  });

  setupTestLocator((locator) {
    locator.registerFactory<CourseDetailsInteractor>(() => _interactor);
    locator.registerFactory<WebContentInteractor>(() => WebContentInteractor());
  });

  testWidgetsWithAccessibilityChecks('shows loading', (tester) async {
    await tester.pumpWidget(TestApp(CourseHomePageScreen(courseId: _courseId)));
    await tester.pump(); // Wait for widget to build

    expect(find.byType(LoadingIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows error', (tester) async {
    when(_interactor.loadHomePage(_courseId)).thenAnswer((_) => Future<Page>.error('Failed to load course home page'));

    await tester.pumpWidget(TestApp(CourseHomePageScreen(courseId: _courseId)));
    await tester.pump(); // Wait for widget to build
    await tester.pump(); // Wait for future to finish

    expect(find.byType(ErrorPandaWidget), findsOneWidget);
    await tester.tap(find.text(l10n.retry));
    await tester.pump();

    verify(_interactor.loadHomePage(_courseId, forceRefresh: true)).called(1);
  });

  testWidgetsWithAccessibilityChecks('can refresh', (tester) async {
    when(_interactor.loadHomePage(_courseId, forceRefresh: anyNamed('forceRefresh'))).thenAnswer((_) async => _page);

    await tester.pumpWidget(TestApp(CourseHomePageScreen(courseId: _courseId)));
    await tester.pump(); // Wait for widget to build
    await tester.pump(); // Wait for the future to finish

    final refresh = find.byType(RefreshIndicator);
    expect(refresh, findsOneWidget);
    await tester.drag(refresh, const Offset(0, 200));
    await tester.pumpAndSettle();

    verify(_interactor.loadHomePage(_courseId, forceRefresh: true)).called(1);
  });

  testWidgetsWithAccessibilityChecks('shows page content', (tester) async {
    when(_interactor.loadHomePage(_courseId)).thenAnswer((_) async => _page.rebuild((b) => b..body = 'body'));

    await tester.pumpWidget(TestApp(
      CourseHomePageScreen(courseId: _courseId),
      platformConfig: PlatformConfig(initWebview: true),
    ));
    await tester.pump(); // Wait for widget to build
    await tester.pump(); // Wait for future to finish
    await tester.pump(); // Wait for the webview future to finish

    expect(find.byType(WebView), findsOneWidget);
  });
}

class _MockCourseDetailsInteractor extends Mock implements CourseDetailsInteractor {}
