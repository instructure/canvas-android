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
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:flutter_parent/screens/courses/details/course_summary_screen.dart';
import 'package:flutter_parent/screens/under_construction_screen.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';

const studentId = '1234';
const courseId = '1234';

void main() {
  testWidgetsWithAccessibilityChecks('shows under construction', (tester) async {
    final model = CourseDetailsModel(studentId, '', courseId);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump();

    expect(find.byType(UnderConstructionScreen), findsOneWidget);
  });
}

Widget _testableWidget(CourseDetailsModel model, {bool highContrastMode = false}) {
  return TestApp(
    Scaffold(
      body: ChangeNotifierProvider<CourseDetailsModel>.value(value: model, child: CourseSummaryScreen()),
    ),
    highContrast: highContrastMode,
  );
}
