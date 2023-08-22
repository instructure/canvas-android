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

import 'package:collection/collection.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/utils/core_extensions/list_extensions.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:tuple/tuple.dart';

class ConversationListInteractor {
  Future<List<Conversation>> getConversations({bool forceRefresh = false}) async {
    var api = locator<InboxApi>();
    try {
      // Get messages from both 'normal' scope 'sent' scopes
      var results = (await Future.wait([
        api.getConversations(forceRefresh: forceRefresh),
        api.getConversations(scope: 'sent', forceRefresh: forceRefresh)
      ])).nonNulls.toList();

      // Remove messages in the 'sent' scope that also exist in the normal scope
      results[1].retainWhere((sent) => !results[0].any((it) => it.id == sent.id));

      // Combine results
      var conversations = results.expand((it) => it).toList();

      // Sort by date (descending)
      conversations.sort((a, b) {
        var dateA = a.lastMessageAt ?? a.lastAuthoredMessageAt ?? DateTime.now();
        var dateB = b.lastMessageAt ?? b.lastAuthoredMessageAt ?? DateTime.now();
        return dateB.compareTo(dateA);
      });
      return Future.value(conversations);
    } catch (e) {
      return Future.error(e);
    }
  }

  Future<List<Course>?> getCoursesForCompose() async {
    return locator<CourseApi>().getObserveeCourses();
  }

  Future<List<Enrollment>?> getStudentEnrollments() async {
    return locator<EnrollmentsApi>().getObserveeEnrollments();
  }

  /// Create a List<Tuple2>, where each tuple is (<User> : <Course>), this tuple is then sorted by user name and also sorted by the students courses
  List<Tuple2<User, Course>> combineEnrollmentsAndCourses(List<Course> courses, List<Enrollment> enrollments) {
    // Create tuple list
    // Remove enrollments where the user is not observing anyone
    enrollments.retainWhere((e) => e.observedUser != null);
    List<Tuple2<User, Course>> thing = enrollments
        .map((e) {
          final course = courses.firstWhereOrNull((c) => c.id == e.courseId);
          if (course == null) return null;
          return Tuple2(e.observedUser!, course);
        })
        .nonNulls
        .toList();

    // Sort users in alphabetical order and sort their courses alphabetically
    thing.sortBySelector(
      [(it) => it?.item1.shortName, (it) => it?.item2.name],
    );

    return thing;
  }
}
