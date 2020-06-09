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
import 'package:flutter_student_embed/utils/common_widgets/appbar_dynamic_style.dart';
import 'package:flutter_student_embed/utils/design/student_colors.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../testutils/accessibility_utils.dart';
import '../../testutils/test_app.dart';

void main() {
  tearDown(() {
    StudentColors.reset();
  });

  testWidgetsWithAccessibilityChecks('Returns unmodified AppBar for portrait non-tablet', (tester) async {
    AppBar appBar = AppBar();
    Size screenSize = Size(300, 500); // Portrait
    var result = await _pumpTestWidget(tester, appBar: appBar, size: screenSize);

    expect(result, appBar);
    expect(result.preferredSize.height, 56);
  });

  testWidgetsWithAccessibilityChecks('Returns correct height for tablet', (tester) async {
    var result = await _pumpTestWidget(tester); // Default size (800x600), i.e. tablet

    expect(result.preferredSize.height, 64);
  });

  testWidgetsWithAccessibilityChecks('Returns correct height for landscape', (tester) async {
    Size screenSize = Size(500, 300); // Landscape
    var result = await _pumpTestWidget(tester, size: screenSize);

    expect(result.preferredSize.height, 48);
  });

  testWidgetsWithAccessibilityChecks('Returns correct font size for landscape', (tester) async {
    Size screenSize = Size(500, 300); // Landscape
    String title = "AppBar Test Title";
    AppBar appBar = AppBar(title: Text(title));
    await _pumpTestWidget(tester, appBar: appBar, size: screenSize);

    var fontSize = tester.getSize(find.text(title)).height;
    expect(fontSize, 14);
  });

  testWidgetsWithAccessibilityChecks('Uses correct text color for landscape', (tester) async {
    Color expectedColor = Colors.orange;
    StudentColors.primaryTextColor = expectedColor;
    Size screenSize = Size(500, 300); // Landscape
    String title = "AppBar Colored Title";
    AppBar appBar = AppBar(title: Text(title));

    await _pumpTestWidget(tester, appBar: appBar, size: screenSize);

    // Apply ambient styling by finding the title element and building its widget
    StatelessElement element = find.text(title).evaluate().first;
    TextStyle titleStyle = (element.build() as RichText).text.style;

    expect(titleStyle.color, expectedColor);
  });
}

Future<PreferredSizeWidget> _pumpTestWidget(WidgetTester tester, {AppBar appBar, Size size}) async {
  AppBar appbar = appBar ?? AppBar();
  GlobalKey<ScaffoldState> scaffoldKey = GlobalKey();
  await tester.pumpWidget(
    TestApp(
      MediaQuery(
        data: MediaQueryData.fromWindow(WidgetsBinding.instance.window).copyWith(size: size),
        child: Builder(
          builder: (context) => Scaffold(
            key: scaffoldKey,
            appBar: dynamicStyleAppBar(context: context, appBar: appbar),
          ),
        ),
      ),
    ),
  );
  await tester.pumpAndSettle();

  Scaffold scaffold = tester.state(find.byKey(scaffoldKey)).widget;
  return scaffold.appBar;
}
