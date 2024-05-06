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
import 'package:flutter_parent/models/grade_cell_data.dart';
import 'package:flutter_parent/screens/assignments/grade_cell.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:percent_indicator/circular_percent_indicator.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';

void main() {
  AppLocalizations l10n = AppLocalizations();

  Future<void> setupWithData(WidgetTester tester, GradeCellData data) async {
    await tester.pumpWidget(TestApp(GradeCell(data)));
    await tester.pumpAndSettle();
  }

  testWidgetsWithAccessibilityChecks('Show empty state', (tester) async {
    GradeCellData data = GradeCellData();
    await setupWithData(tester, data);

    expect(find.byKey(Key('grade-cell-empty-container')), findsOneWidget);
    expect(find.byKey(Key('grade-cell-graded-container')), findsNothing);
    expect(find.byKey(Key('grade-cell-submitted-container')), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Shows submitted state', (tester) async {
    GradeCellData data = GradeCellData((b) => b..state = GradeCellState.submitted);
    await setupWithData(tester, data);

    expect(find.byKey(Key('grade-cell-empty-container')), findsNothing);
    expect(find.byKey(Key('grade-cell-graded-container')), findsNothing);
    expect(find.byKey(Key('grade-cell-submitted-container')), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows graded state', (tester) async {
    GradeCellData data = GradeCellData((b) => b..state = GradeCellState.graded);
    await setupWithData(tester, data);

    expect(find.byKey(Key('grade-cell-empty-container')), findsNothing);
    expect(find.byKey(Key('grade-cell-graded-container')), findsOneWidget);
    expect(find.byKey(Key('grade-cell-submitted-container')), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Does not show divider and label for empty state', (tester) async {
    GradeCellData data = GradeCellData();
    await setupWithData(tester, data);

    expect(find.byType(Divider), findsNothing);
    expect(find.text(l10n.assignmentGradeLabel), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Shows divider and label for submitted state', (tester) async {
    GradeCellData data = GradeCellData((b) => b..state = GradeCellState.submitted);
    await setupWithData(tester, data);

    expect(find.byType(Divider), findsOneWidget);
    expect(find.text(l10n.assignmentGradeLabel), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows divider and label for graded state', (tester) async {
    GradeCellData data = GradeCellData((b) => b..state = GradeCellState.graded);
    await setupWithData(tester, data);

    expect(find.byType(Divider), findsOneWidget);
    expect(find.text(l10n.assignmentGradeLabel), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows title and message in submitted state', (tester) async {
    GradeCellData data = GradeCellData((b) => b
      ..state = GradeCellState.submitted
      ..submissionText = 'Submission text');
    await setupWithData(tester, data);

    expect(find.text(l10n.submissionStatusSuccessTitle), findsOneWidget);
    expect(find.text(data.submissionText), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays circular grade graph with correct percentage value', (tester) async {
    GradeCellData data = GradeCellData((b) => b
      ..state = GradeCellState.graded
      ..accentColor = Colors.pinkAccent
      ..graphPercent = 0.25);
    await setupWithData(tester, data);

    var finder = find.byType(CircularPercentIndicator);
    expect(finder, findsOneWidget);
    expect(tester.widget<CircularPercentIndicator>(finder).percent, data.graphPercent);
    expect(tester.widget<CircularPercentIndicator>(finder).progressColor, data.accentColor);
  });

  testWidgetsWithAccessibilityChecks('Displays points and label', (tester) async {
    GradeCellData data = GradeCellData((b) => b
      ..state = GradeCellState.graded
      ..score = '99.5'
      ..showPointsLabel = true);
    await setupWithData(tester, data);

    expect(find.byKey(Key('grade-cell-score')).evaluate(), find.text(data.score).evaluate());
    expect(find.byKey(Key('grade-cell-points-label')), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays complete icon', (tester) async {
    GradeCellData data = GradeCellData((b) => b
      ..state = GradeCellState.graded
      ..showCompleteIcon = true);
    await setupWithData(tester, data);

    expect(find.byKey(Key('grade-cell-complete-icon')), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays incomplete icon', (tester) async {
    GradeCellData data = GradeCellData((b) => b
      ..state = GradeCellState.graded
      ..showIncompleteIcon = true);
    await setupWithData(tester, data);

    expect(find.byKey(Key('grade-cell-incomplete-icon')), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays grade text', (tester) async {
    GradeCellData data = GradeCellData((b) => b
      ..state = GradeCellState.graded
      ..grade = 'A-'
      ..gradeContentDescription = 'A. minus');
    await setupWithData(tester, data);

    var finder = find.byKey(Key('grade-cell-grade'));
    expect(tester.widget<Text>(finder).data, data.grade);
    expect(tester.widget<Text>(finder).semanticsLabel, data.gradeContentDescription);
  });

  testWidgetsWithAccessibilityChecks('Displays \'out of\' text', (tester) async {
    GradeCellData data = GradeCellData((b) => b
      ..state = GradeCellState.graded
      ..outOf = 'Out of 100 points');
    await setupWithData(tester, data);

    expect(find.byKey(Key('grade-cell-out-of')).evaluate(), find.text(data.outOf).evaluate());
  });

  testWidgetsWithAccessibilityChecks('Displays late penalty text', (tester) async {
    GradeCellData data = GradeCellData((b) => b
      ..state = GradeCellState.graded
      ..accentColor = Colors.pinkAccent
      ..yourGrade = 'Your grade: 85'
      ..latePenalty = 'Late penalty: -25');
    await setupWithData(tester, data);

    expect(find.byKey(Key('grade-cell-late-penalty')).evaluate(), find.text(data.latePenalty).evaluate());
    expect(find.byKey(Key('grade-cell-your-grade')).evaluate(), find.text(data.yourGrade).evaluate());
    expect(tester.widget<Text>(find.byKey(Key('grade-cell-late-penalty'))).style!.color, ParentColors.failure);
  });

  testWidgetsWithAccessibilityChecks('Displays final grade text', (tester) async {
    GradeCellData data = GradeCellData((b) => b
      ..state = GradeCellState.graded
      ..finalGrade = 'Final Grade: A-');
    await setupWithData(tester, data);

    expect(find.byKey(Key('grade-cell-final-grade')).evaluate(), find.text(data.finalGrade).evaluate());
  });

  testWidgetsWithAccessibilityChecks('Correctly hides elements', (tester) async {
    GradeCellData data = GradeCellData((b) => b..state = GradeCellState.graded);
    await setupWithData(tester, data);

    expect(find.byKey(Key('grade-cell-score')), findsNothing);
    expect(find.byKey(Key('grade-cell-grade')), findsNothing);
    expect(find.byKey(Key('grade-cell-out-of')), findsNothing);
    expect(find.byKey(Key('grade-cell-final-grade')), findsNothing);
    expect(find.byKey(Key('grade-cell-points-label')), findsNothing);
    expect(find.byKey(Key('grade-cell-late-penalty')), findsNothing);
    expect(find.byKey(Key('grade-cell-complete-icon')), findsNothing);
    expect(find.byKey(Key('grade-cell-incomplete-icon')), findsNothing);
  });
}
