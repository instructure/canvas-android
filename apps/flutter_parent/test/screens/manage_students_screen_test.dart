import 'dart:math';

/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter/material.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_screen.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_interactor.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_screen.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';

import '../utils/accessibility_utils.dart';
import '../utils/network_image_response.dart';
import '../utils/test_app.dart';

void main() {
  mockNetworkImageResponse();

  _setupLocator() {
    final locator = GetIt.instance;
    locator.reset();

    locator.registerFactory<ManageStudentsInteractor>(() => MockInteractor());
  }

  testWidgetsWithAccessibilityChecks('Displays list of observed students', (tester) async {
    _setupLocator();

    var observedStudents = [
      _mockUser('Billy'),
      _mockUser('Sally'),
      _mockUser('Trevor'),
    ];

    await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudents)));
    await tester.pumpAndSettle();

    expect(find.byType(ListTile), findsNWidgets(3));
  });

  testWidgetsWithAccessibilityChecks('Displays username only when pronouns is null', (tester) async {
    _setupLocator();

    var observedStudents = [
      _mockUser('Billy', pronouns: null),
      _mockUser('Sally', pronouns: null),
      _mockUser('Trevor', pronouns: null),
    ];

    await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudents)));
    await tester.pumpAndSettle();

    expect(find.text('Billy'), findsOneWidget);
    expect(find.text('Sally'), findsOneWidget);
    expect(find.text('Trevor'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays username and pronouns when pronouns are not null', (tester) async {
    _setupLocator();

    var observedStudents = [
      _mockUser('Billy', pronouns: 'he/him'),
      _mockUser('Sally', pronouns: 'she/her'),
      _mockUser('Trevor', pronouns: 'he/him'),
    ];

    await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudents)));
    await tester.pumpAndSettle();

    expect(find.text('Billy'), findsOneWidget);
    expect(find.text('Sally'), findsOneWidget);
    expect(find.text('Trevor'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking student goes to the Threshold Screen', (tester) async {
    _setupLocator();

  });

  testWidgetsWithAccessibilityChecks('Clicking FAB opens add student dialog', (tester) async {
    _setupLocator();

  });
}

class MockInteractor extends ManageStudentsInteractor {}

User _mockUser(String name, {String pronouns, String primaryEmail}) => User((b) => b
  ..id = Random(name.hashCode).nextInt(100000)
  ..sortableName = name
  ..name = name
  ..primaryEmail = primaryEmail ?? null
  ..pronouns = pronouns ?? null
  ..build());
