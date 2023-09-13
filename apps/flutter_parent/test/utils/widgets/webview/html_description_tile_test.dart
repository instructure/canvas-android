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
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/html_description_screen.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/html_description_tile.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../accessibility_utils.dart';
import '../../test_app.dart';
import '../../test_helpers/mock_helpers.dart';
import '../../test_helpers/mock_helpers.mocks.dart';

void main() {
  final l10n = AppLocalizations();

  group('empty', () {
    testWidgetsWithAccessibilityChecks('shows an empty container with no html and no empty message', (tester) async {
      await tester.pumpWidget(TestApp(HtmlDescriptionTile(html: null)));
      await tester.pump();

      expect(find.byType(Container), findsWidgets);
      expect(find.text(l10n.descriptionTitle), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('shows an empty container with empty html and no empty message', (tester) async {
      await tester.pumpWidget(TestApp(HtmlDescriptionTile(html: '')));
      await tester.pump();

      expect(find.byType(Container), findsWidgets);
      expect(find.text(l10n.descriptionTitle), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('shows an empty message with no html', (tester) async {
      final empty = 'Empty here';

      await tester.pumpWidget(TestApp(HtmlDescriptionTile(html: null, emptyDescription: empty)));
      await tester.pump();

      expect(find.text(l10n.descriptionTitle), findsOneWidget);
      expect(find.text(empty), findsOneWidget);
    });
  });

  group('given html', () {
    testWidgetsWithAccessibilityChecks('shows components with defaults', (tester) async {
      await tester.pumpWidget(TestApp(HtmlDescriptionTile(html: 'whatever')));
      await tester.pump();

      expect(find.text(l10n.descriptionTitle), findsOneWidget);
      expect(find.text(l10n.viewDescription), findsOneWidget);
      expect(find.byIcon(Icons.arrow_forward), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows components with custom values', (tester) async {
      final title = 'This is a title';
      final label = 'Click this to view';

      await tester.pumpWidget(TestApp(HtmlDescriptionTile(
        html: 'whatever',
        descriptionTitle: title,
        buttonLabel: label,
      )));
      await tester.pump();

      expect(find.text(l10n.descriptionTitle), findsNothing);
      expect(find.text(l10n.viewDescription), findsNothing);
      expect(find.text(title), findsOneWidget);
      expect(find.text(label), findsOneWidget);
      expect(find.byIcon(Icons.arrow_forward), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('can click to launch HtmlDescriptionScreen', (tester) async {
      final html = 'whatever';
      final nav = MockQuickNav();
      setupTestLocator((locator) => locator..registerLazySingleton<QuickNav>(() => nav));

      await tester.pumpWidget(TestApp(HtmlDescriptionTile(html: html)));
      await tester.pump();

      await tester.tap(find.text(l10n.viewDescription));
      await tester.pumpAndSettle();

      final widget = verify(nav.push(any, captureAny)).captured[0];
      expect(widget, isInstanceOf<HtmlDescriptionScreen>());
      expect((widget as HtmlDescriptionScreen).html, html);
    });
  });
}
