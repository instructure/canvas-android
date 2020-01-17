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

/*
@GET("{contextType}/{contextId}/discussion_topics/{topicId}?include[]=sections")
        fun getDetailedDiscussion(@Path("contextType") contextType: String, @Path("contextId") contextId: Long, @Path("topicId") topicId: Long): Call<DiscussionTopicHeader>
 */
import 'package:flutter_parent/models/account_notification.dart';
import 'package:flutter_parent/models/announcement.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class AnnouncementApi {
  Future<Announcement> getCourseAnnouncement(String courseId, String announcementId) {
    return fetch(canvasDio().get('courses/$courseId/discussion_topics/$announcementId'));
  }

  Future<AccountNotification> getAccountNotification(String accountNotificationid) {
    return fetch(canvasDio().get('accounts/self/users/self/account_notifications/$accountNotificationid'));
  }
}