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
import 'package:flutter_student_embed/utils/design/student_colors.dart';
import 'package:flutter_student_embed/utils/design/student_theme.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../testutils/accessibility_utils.dart';
import '../../testutils/dummy_widget.dart';
import '../../testutils/test_app.dart';

void main() {
  testWidgetsWithAccessibilityChecks(
    'CanvasContextTheme uses context color for primary and accent colors',
    (tester) async {
      String contextCode = 'course_123';
      Color contextColor = Colors.purple;
      StudentColors.contextColors[contextCode] = contextColor;

      await tester.pumpWidget(
        TestApp(
          CanvasContextTheme(
            contextCode: contextCode,
            builder: (context) => DummyWidget(),
          ),
        ),
      );
      await tester.pumpAndSettle();

      BuildContext context = tester.state(find.byType(DummyWidget)).context;
      var theme = Theme.of(context);

      expect(theme.primaryColor.value, contextColor.value);
      expect(theme.accentColor.value, contextColor.value);
    },
  );
}
