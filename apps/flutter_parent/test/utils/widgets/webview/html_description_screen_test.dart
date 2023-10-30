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
import 'package:flutter_parent/utils/common_widgets/web_view/canvas_web_view.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/html_description_screen.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../accessibility_utils.dart';
import '../../test_app.dart';
import '../../test_helpers/mock_helpers.dart';
import '../../test_helpers/mock_helpers.mocks.dart';

void main() {
  setupTestLocator((locator) => locator..registerLazySingleton<WebContentInteractor>(() => MockWebContentInteractor()));

  // Can't test html content in webview, so just make sure it renders one on the screen
  testWidgetsWithAccessibilityChecks('renders', (tester) async {
    await tester.pumpWidget(TestApp(HtmlDescriptionScreen('anything', 'Description')));
    await tester.pumpAndSettle();

    expect(find.byType(CanvasWebView), findsOneWidget);
  });
}
