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

import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_test/flutter_test.dart';

import '../accessibility_utils.dart';
import '../network_image_response.dart';
import '../test_app.dart';

void main() {
  mockNetworkImageResponse();

  test('Question mark when short name is empty or null', () {
    var blank = '';
    String? nullName = null;

    expect(Avatar.getUserInitials(blank), equals('?'));
    expect(Avatar.getUserInitials(nullName), equals('?'));
  });

  test('Two initials when exactly two initials in short name', () {
    var name = 'Canvas Instructure';

    expect(Avatar.getUserInitials(name), equals('CI'));
  });

  test('One initial when more or less than two initials in short name', () {
    var more = 'Canvas by Instructure';
    var less = 'Canvas';

    expect(Avatar.getUserInitials(more), equals('C'));
    expect(Avatar.getUserInitials(less), equals('C'));
  });

  /// This test is currently disabled due to the complexity of mocking CachedNetworkImage's dependencies.
  /*testWidgetsWithAccessibilityChecks('Displays avatar when there is a url', (tester) async {
    var avatarUrl = 'http://www.instructure.com';

    await tester.pumpWidget(TestApp(Avatar(
      avatarUrl,
    )));

    await tester.pumpAndSettle();

    expect(find.byType(CachedNetworkImage), findsOneWidget);
  });*/

  testWidgetsWithAccessibilityChecks('Displays initials when there is no avatar url', (tester) async {
    var avatarUrl = null;
    var name = 'Canvas Instructure';

    await tester.pumpWidget(TestApp(Avatar(
      avatarUrl,
      name: name,
    )));

    await tester.pumpAndSettle();

    expect(find.text('CI'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Does not display initials when showInitials is false', (tester) async {
    var avatarUrl = null;
    var name = 'Canvas Instructure';

    await tester.pumpWidget(TestApp(Avatar(
      avatarUrl,
      name: name,
      showInitials: false,
    )));

    await tester.pumpAndSettle();

    expect(find.text('CI'), findsNothing);
  });
}
