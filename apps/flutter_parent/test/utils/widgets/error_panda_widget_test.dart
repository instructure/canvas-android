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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_test/flutter_test.dart';

import '../accessibility_utils.dart';
import '../test_app.dart';

void main() {
  var errorString = AppLocalizations().errorLoadingMessages;
  Function? callback = null;

  testWidgetsWithAccessibilityChecks('Shows warning icon', (WidgetTester tester) async {
    await tester.pumpWidget(TestApp(
      ErrorPandaWidget(errorString, callback),
    ));
    await tester.pumpAndSettle();

    expect(find.byType(Icon), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows error message with load target', (tester) async {
    await tester.pumpWidget(TestApp(
      ErrorPandaWidget(errorString, callback),
    ));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().errorLoadingMessages), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows a retry button', (tester) async {
    await tester.pumpWidget(TestApp(
      ErrorPandaWidget(errorString, callback),
    ));
    await tester.pumpAndSettle();

    expect(find.byType(TextButton), findsOneWidget);
    expect(find.text(AppLocalizations().retry), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Retry click calls callback', (tester) async {
    var called = false;

    await tester.pumpWidget(TestApp(
      ErrorPandaWidget(errorString, () {
        called = true;
      }),
    ));
    await tester.pumpAndSettle();

    // Click retry button
    await tester.tap(find.byType(TextButton));
    await tester.pumpAndSettle();

    // Verify the callback was called
    expect(called, true);
  });

  testWidgetsWithAccessibilityChecks('Shows a header', (tester) async {
    await tester.pumpWidget(TestApp(
      ErrorPandaWidget(errorString, callback, header: Text('header here')),
    ));
    await tester.pumpAndSettle();

    expect(find.byType(TextButton), findsOneWidget);
    expect(find.text(AppLocalizations().retry), findsOneWidget);
    expect(find.text('header here'), findsOneWidget);
  });
}
