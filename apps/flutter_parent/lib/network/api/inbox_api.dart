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

import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/recipient.dart';
import 'package:flutter_parent/models/unread_count.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class InboxApi {
  Future<List<Conversation>> getConversations({String scope: null, bool forceRefresh: false}) async {
    final dio = canvasDio(forceRefresh: forceRefresh, pageSize: PageSize.canvasMax);
    final params = {
      'scope': scope,
      'include': ['participant_avatars'],
    };
    return fetchList(dio.get('conversations', queryParameters: params), depaginateWith: dio);
  }

  Future<Conversation> getConversation(int id) => fetch(canvasDio().get('conversations/$id'));

  Future<UnreadCount> getUnreadCount() => fetch(canvasDio().get('conversations/unread_count'));

  Future<Conversation> addMessage(int conversationId, String message) {
    var dio = canvasDio();
    dio.options.queryParameters.addAll({'body': message});
    return fetch(dio.post('conversations/$conversationId/add_message'));
  }

  Future<List<Recipient>> getRecipients(Course course, {bool forceRefresh: false}) {
    var dio = canvasDio(forceRefresh: forceRefresh, pageSize: PageSize.canvasMax);
    var params = {
      'permissions': ['send_messages_all'],
      'messageable_only': true,
      'context': 'course_${course.id}',
    };
    return fetchList(dio.get('search/recipients', queryParameters: params), depaginateWith: dio);
  }

  Future<Conversation> createConversation(
    Course course,
    List<String> recipientIds,
    String subject,
    String body,
    List<String> attachmentIds,
  ) async {
    var dio = canvasDio();
    var params = {
      'group_conversation': 'true',
      'recipients': recipientIds,
      'context_code': 'course_${course.id}',
      'subject': subject,
      'body': body,
      'attachment_ids': attachmentIds,
    };
    List<Conversation> result = await fetchList(dio.post('conversations', queryParameters: params));
    return result[0];
  }
}
