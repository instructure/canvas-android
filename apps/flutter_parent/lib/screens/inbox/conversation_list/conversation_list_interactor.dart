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

import 'package:flutter_parent/api/course_api.dart';
import 'package:flutter_parent/api/inbox_api.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class ConversationListInteractor {
  Future<List<Conversation>> getConversations({bool forceRefresh: false}) async {
    var api = locator<InboxApi>();
    try {
      // Get messages from both 'normal' scope 'sent' scopes
      var results = await Future.wait([
        api.getConversations(forceRefresh: forceRefresh),
        api.getConversations(scope: 'sent', forceRefresh: forceRefresh)
      ]);

      // Remove messages in the 'sent' scope that also exist in the normal scope
      results[1].retainWhere((sent) => !results[0].any((it) => it.id == sent.id));

      // Combine results
      var conversations = results.expand((it) => it).toList();

      // Sort by date (descending)
      conversations.sort((a, b) {
        var dateA = a.lastMessageAt ?? a.lastAuthoredMessageAt;
        var dateB = b.lastMessageAt ?? b.lastAuthoredMessageAt;
        return dateB.compareTo(dateA);
      });
      return Future.value(conversations);
    } catch (e) {
      return Future.error(e);
    }
  }

  Future<List<Course>> getCoursesForCompose() async {
    return locator<CourseApi>().getObserveeCourses();
  }
}
