//  Copyright (C) 2019 - present Instructure, Inc.
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, version 3 of the License.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter/material.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';

import '../accessibility_utils.dart';
import '../test_app.dart';

void main() {
  final svgPath = 'assets/svg/panda-space-no-assignments.svg';
  final title = 'title';
  final subtitle = 'subtitle';
  final buttonText = 'Click me';

  testWidgetsWithAccessibilityChecks('shows an svg', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(svgPath: svgPath)));
    await tester.pumpAndSettle();

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.byType(Text), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('shows a title', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(title: title)));
    await tester.pumpAndSettle();

    expect(find.byType(SvgPicture), findsNothing);
    expect(find.byType(Text), findsOneWidget);
    expect(find.text(title), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows a subtitle', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(subtitle: subtitle)));
    await tester.pumpAndSettle();

    expect(find.byType(SvgPicture), findsNothing);
    expect(find.byType(Text), findsOneWidget);
    expect(find.text(subtitle), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows a button', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(buttonText: buttonText)));
    await tester.pumpAndSettle();

    expect(find.widgetWithText(TextButton, buttonText), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('tapping button invokes callback', (tester) async {
    var called = false;
    await tester.pumpWidget(
      _testableWidget(EmptyPandaWidget(buttonText: buttonText, onButtonTap: () => called = true)),
    );
    await tester.pumpAndSettle();

    expect(find.widgetWithText(TextButton, buttonText), findsOneWidget);
    await tester.tap(find.text(buttonText));
    expect(called, isTrue);
  });

  // TODO Fix test
  testWidgetsWithAccessibilityChecks('shows an svg and a title', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(svgPath: svgPath, title: title)));
    await tester.pumpAndSettle();

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.byType(SizedBox), findsOneWidget); // The spacing between the svg and the title
    expect(find.byType(Text), findsOneWidget);
    expect(find.text(title), findsOneWidget);
  }, skip: true);

  // TODO Fix test
  testWidgetsWithAccessibilityChecks('shows an svg and a subtitle', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(svgPath: svgPath, subtitle: subtitle)));
    await tester.pumpAndSettle();

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.byType(SizedBox), findsOneWidget); // The spacing between the svg and the subtitle
    expect(find.byType(Text), findsOneWidget);
    expect(find.text(subtitle), findsOneWidget);
  }, skip: true);

  testWidgetsWithAccessibilityChecks('shows a title and a subtitle', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(title: title, subtitle: subtitle)));
    await tester.pumpAndSettle();

    expect(find.byType(SizedBox), findsOneWidget); // The spacing between the title and the subtitle
    expect(find.byType(Text), findsNWidgets(2));
    expect(find.text(title), findsOneWidget);
    expect(find.text(subtitle), findsOneWidget);
  });

  // TODO Fix test
  testWidgetsWithAccessibilityChecks('shows an svg, a title, and a subtitle', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(svgPath: svgPath, title: title, subtitle: subtitle)));
    await tester.pumpAndSettle();

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.byType(SizedBox), findsNWidgets(2)); // Between the svg and title, as well as title and subtitle
    expect(find.byType(Text), findsNWidgets(2));
    expect(find.text(title), findsOneWidget);
    expect(find.text(subtitle), findsOneWidget);
  }, skip: true);

  // TODO Fix test
  testWidgetsWithAccessibilityChecks('shows an svg, title, subtitle, and button', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(
      svgPath: svgPath,
      title: title,
      subtitle: subtitle,
      buttonText: buttonText,
    )));
    await tester.pumpAndSettle();

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.byType(SizedBox), findsNWidgets(2)); // Between the svg and title, as well as title and subtitle
    expect(find.byType(Text), findsNWidgets(3));
    expect(find.text(title), findsOneWidget);
    expect(find.text(subtitle), findsOneWidget);
    expect(find.widgetWithText(TextButton, buttonText), findsOneWidget);
  }, skip: true);

  testWidgetsWithAccessibilityChecks('shows a header', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(header: Text('h'))));
    await tester.pumpAndSettle();

    expect(find.byType(Text), findsOneWidget);
    expect(find.text('h'), findsOneWidget);
  });
}

Widget _testableWidget(EmptyPandaWidget child) {
  return TestApp(Scaffold(body: child));
}
