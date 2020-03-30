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

import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/user_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/screens/dashboard/inbox_notifier.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class DashboardInteractor {
  Future<List<User>> getStudents({bool forceRefresh = false}) async =>
      locator<EnrollmentsApi>().getObserveeEnrollments(forceRefresh: forceRefresh).then<List<User>>((enrollments) {
        List<User> users = filterStudents(enrollments);
        sortUsers(users);
        return users;
      });

  Future<User> getSelf({app}) async => locator<UserApi>().getSelf().then((user) {
        ApiPrefs.setUser(user, app: app);
        return user;
      });

  List<User> filterStudents(List<Enrollment> enrollments) =>
      enrollments.map((enrollment) => enrollment.observedUser).where((student) => student != null).toSet().toList();

  void sortUsers(List<User> users) => users.sort((user1, user2) => user1.sortableName.compareTo(user2.sortableName));

  InboxCountNotifier getInboxCountNotifier() => locator<InboxCountNotifier>();

  AlertCountNotifier getAlertCountNotifier() => locator<AlertCountNotifier>();
}
