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
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:flutter_parent/screens/dashboard/student_horizontal_list_view.dart';
import 'package:flutter_parent/screens/under_construction_screen.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/test_app.dart';

void main() {
  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows all students as well as add student button', (tester) async {
      var student1 = CanvasModelTestUtils.mockUser(shortName: 'Billy');
      var student2 = CanvasModelTestUtils.mockUser(shortName: 'Sally');
      var student3 = CanvasModelTestUtils.mockUser(shortName: 'Trevor');
      var students = [student1, student2, student3];

      // Setup the widget
      await tester.pumpWidget(TestApp(StudentHorizontalListView(students)));
      await tester.pump();

      // Check for the names of the students and the add student button
      expect(find.text(student1.shortName), findsOneWidget);
      expect(find.text(student2.shortName), findsOneWidget);
      expect(find.text(student3.shortName), findsOneWidget);
      expect(find.text(AppLocalizations().addStudent), findsOneWidget);
    });
  });

  group('Interaction', () {
    testWidgetsWithAccessibilityChecks('student tap updates selected student, theme, and calls onTap', (tester) async {
      var student1 = CanvasModelTestUtils.mockUser(shortName: 'Billy');
      var student2 = CanvasModelTestUtils.mockUser(shortName: 'Sally');

      var students = [student1, student2];

      bool called = false;
      Function tapped = () {
        called = !called;
      };

      var notifier = SelectedStudentNotifier();

      setupTestLocator((locator) {
        locator.registerLazySingleton<SelectedStudentNotifier>(() => notifier);
      });

      // Setup the widget
      await tester
          .pumpWidget(SelectedStudentNotifierTestApp(StudentHorizontalListView(students, onTap: tapped), notifier));
      await tester.pump();

      // Check the initial value of the student notifier
      expect(notifier.value, null);

      // Check the initial value of the theme, should be 0
      var state = ParentTheme.of(TestApp.navigatorKey.currentContext);
      expect(state.studentIndex, 0);

      // Check for the second student
      expect(find.text(student2.shortName), findsOneWidget);

      // Tap on the student
      await tester.tap(find.text(student2.shortName));
      await tester.pumpAndSettle(); // Wait for animations to settle
      await tester.pump();

      // Check the selected student notifier
      expect(student2, notifier.value);

      // Check the theme, should be 1
      expect(state.studentIndex, students.indexOf(student2));

      // Check to make sure we called the onTap function passed in
      expect(called, true);
    });

    testWidgetsWithAccessibilityChecks('add student tap shows under construction screen', (tester) async {
      var student1 = CanvasModelTestUtils.mockUser(name: 'Billy');

      setupTestLocator((locator) {
        locator.registerLazySingleton<QuickNav>(() => QuickNav());
      });

      // Setup the widget
      await tester.pumpWidget(TestApp(StudentHorizontalListView([student1])));
      await tester.pump();

      expect(find.text(AppLocalizations().addStudent), findsOneWidget);

      // Tap the 'Add Student' button and wait for any transition animations to finish
      await tester.tap(find.byType(RaisedButton));
      await tester.pumpAndSettle();

      // Check for the under construction screen
      expect(find.byType(UnderConstructionScreen), findsOneWidget);
    });
  });
}

class SelectedStudentNotifierTestApp extends StatelessWidget {
  final Widget child;
  final SelectedStudentNotifier notifier;
  SelectedStudentNotifierTestApp(this.child, this.notifier);

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider<SelectedStudentNotifier>(
      create: (context) => notifier,
      child: Consumer<SelectedStudentNotifier>(builder: (context, model, _) {
        return TestApp(child);
      }),
    );
  }
}
