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
import 'package:flutter_student_embed/utils/common_widgets/dropdown_arrow.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../testutils/accessibility_utils.dart';
import '../../testutils/test_app.dart';

void main() {
  Type reify<T>() => T;

  testWidgetsWithAccessibilityChecks(
    'Uses TweenAnimationBuilder when specificProgress is not specified',
    (tester) async {
      await tester.pumpWidget(TestApp(DropdownArrow()));
      await tester.pumpAndSettle();

      expect(find.byType(reify<TweenAnimationBuilder<double>>()), findsOneWidget);
    },
  );
}
