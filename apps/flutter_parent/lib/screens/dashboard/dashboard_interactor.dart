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

import 'package:flutter_parent/api/enrollments_api.dart';
import 'package:flutter_parent/api/user_api.dart';
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/user.dart';

class DashboardInteractor {
  Future<List<User>> getObservees() async =>
    EnrollmentsApi.getObserveeEnrollments().then<List<User>>((enrollments) {
      List<User> users = filterObservees(enrollments);
      sortUsers(users);
      return users;
  });

  Future<User> getSelf({app}) async =>
    UserApi.getSelf().then((user) {
      ApiPrefs.setUser(user, app: app);
      return user;
  });

  List<User> filterObservees(List<Enrollment> enrollments) =>
      enrollments.map((enrollment) => enrollment.observedUser).where((student) => student != null).toSet().toList();

  void sortUsers(List<User> users) =>
      users.sort((user1, user2) => user1.sortableName.compareTo(user2.sortableName));

}