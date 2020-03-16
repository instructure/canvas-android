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

import 'package:flutter_parent/screens/quizzes/quiz_details_screen.dart';
import 'package:flutter_parent/screens/under_construction_screen.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';

void main() {
  final String courseId = '123';
  final String quizId = '1234';

  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows under construction', (tester) async {
      await tester.pumpWidget(TestApp(
        QuizDetailsScreen(
          courseId: courseId,
          quizId: quizId,
        ),
        highContrast: true,
      ));

      await tester.pump();

      expect(find.byType(UnderConstructionScreen), findsOneWidget);
    });
  });

  group('Interaction', () {});
}
