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

void main() {
  final svgPath = 'assets/svg/panda-space-no-assignments.svg';
  final title = 'title';
  final subtitle = 'subtitle';

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

  testWidgetsWithAccessibilityChecks('shows an svg and a title', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(svgPath: svgPath, title: title)));
    await tester.pumpAndSettle();

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.byType(SizedBox), findsOneWidget); // The spacing between the svg and the title
    expect(find.byType(Text), findsOneWidget);
    expect(find.text(title), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows an svg and a subtitle', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(svgPath: svgPath, subtitle: subtitle)));
    await tester.pumpAndSettle();

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.byType(SizedBox), findsOneWidget); // The spacing between the svg and the subtitle
    expect(find.byType(Text), findsOneWidget);
    expect(find.text(subtitle), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows a title and a subtitle', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(title: title, subtitle: subtitle)));
    await tester.pumpAndSettle();

    expect(find.byType(SizedBox), findsOneWidget); // The spacing between the title and the subtitle
    expect(find.byType(Text), findsNWidgets(2));
    expect(find.text(title), findsOneWidget);
    expect(find.text(subtitle), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows an svg, a title, and a subtitle', (tester) async {
    await tester.pumpWidget(_testableWidget(EmptyPandaWidget(svgPath: svgPath, title: title, subtitle: subtitle)));
    await tester.pumpAndSettle();

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.byType(SizedBox), findsNWidgets(2)); // Between the svg and title, as well as title and subtitle
    expect(find.byType(Text), findsNWidgets(2));
    expect(find.text(title), findsOneWidget);
    expect(find.text(subtitle), findsOneWidget);
  });
}

Widget _testableWidget(EmptyPandaWidget child) {
  return MaterialApp(
    home: Scaffold(
      body: child,
    ),
  );
}
