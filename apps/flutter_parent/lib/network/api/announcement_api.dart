// Copyright (C) 2020 - present Instructure, Inc.
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

import 'package:flutter_parent/models/account_notification.dart';
import 'package:flutter_parent/models/announcement.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class AnnouncementApi {
  Future<Announcement?> getCourseAnnouncement(String courseId, String announcementId, bool forceRefresh) async {
    var dio = canvasDio(forceRefresh: forceRefresh);
    return fetch(dio.get('courses/$courseId/discussion_topics/$announcementId'));
  }

  Future<AccountNotification?> getAccountNotification(String accountNotificationId, bool forceRefresh) async {
    var dio = canvasDio(forceRefresh: forceRefresh);
    return fetch(dio.get('accounts/self/users/self/account_notifications/$accountNotificationId'));
  }
}
