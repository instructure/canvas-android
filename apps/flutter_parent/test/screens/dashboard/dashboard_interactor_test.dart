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

import 'dart:math';
import 'package:flutter_parent/api/enrollments_api.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';

void main() {
  test('interactor should sort users in descending order', () {
    // Create lists
    var startingList = [_mockStudent('Zed'), _mockStudent('Alex'), _mockStudent('Billy')];
    var expectedSortedList = [_mockStudent('Alex'), _mockStudent('Billy'), _mockStudent('Zed')];

    // Run the logic
    var interactor = DashboardInteractor();
    interactor.sortUsers(startingList);

    expect(startingList, expectedSortedList);
  });

  test('interactor should filter out enrollments with no observee', () {
    // Create the lists
    var startingList = [_mockEnrollment(null), _mockEnrollment(_mockStudent('Alex').toBuilder()), _mockEnrollment(null)];
    var expectedSortedList = [_mockStudent('Alex')];

    // Run the logic
    var interactor = DashboardInteractor();
    var result = interactor.filterObservees(startingList);

    expect(result, expectedSortedList);

  });
}

User _mockStudent(String name) => User((b) => b
  ..id = Random(name.hashCode).nextInt(100000)
  ..sortableName = name
  ..build());

Enrollment _mockEnrollment(UserBuilder observedUser) => Enrollment((b) => b
  ..enrollmentState = ''
  ..observedUser = observedUser
  ..build());
