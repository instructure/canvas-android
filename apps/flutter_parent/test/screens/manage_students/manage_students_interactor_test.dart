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

import 'dart:math';

import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/user_api.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../dashboard/dashboard_interactor_test.dart';

void main() {
  final api = MockUserApi();
  final AccountsApi accountsApi = MockAccountsApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<UserApi>(() => api);
    locator.registerLazySingleton<AccountsApi>(() => accountsApi);
  });

  setUp(() {
    reset(api);
    reset(accountsApi);
  });

  test('getStudents returns a list of students', () async {
    var startingList = [_mockStudent('Zed'), _mockStudent('Alex')];
    var expectedList = [_mockStudent('Alex'), _mockStudent('Zed')];

    when(api.getObservees(forceRefresh: anyNamed('forceRefresh'))).thenAnswer((_) async => startingList);

    expect(await ManageStudentsInteractor().getStudents(), expectedList);
  });

  test('Sort users in descending order', () {
    // Create lists
    var startingList = [_mockStudent('Zed'), _mockStudent('Alex'), _mockStudent('Billy')];
    var expectedSortedList = [_mockStudent('Alex'), _mockStudent('Billy'), _mockStudent('Zed')];

    // Run the logic
    var interactor = ManageStudentsInteractor();
    interactor.sortUsers(startingList);

    expect(startingList, expectedSortedList);
  });

  test('shouldAllowPairing calls AccountsApi', () async {
    when(accountsApi.getPairingAllowed()).thenAnswer((_) async => true);
    var result = await ManageStudentsInteractor().shouldAllowPairing();

    verify(accountsApi.getPairingAllowed());
    expect(result, isTrue);
  });
}

User _mockStudent(String name) => User((b) => b
  ..id = Random(name.hashCode).nextInt(100000).toString()
  ..sortableName = name
  ..build());
