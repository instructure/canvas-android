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

import 'package:flutter/widgets.dart';
import 'package:flutter_parent/models/account_notification.dart';
import 'package:flutter_parent/models/announcement.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/network/api/announcement_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_screen.dart';
import 'package:flutter_parent/screens/announcements/announcement_view_state.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/view_attachment_screen.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class AnnouncementDetailsInteractor {
  AnnouncementApi _announcementApi() => locator<AnnouncementApi>();

  CourseApi _courseApi() => locator<CourseApi>();

  Future<AnnouncementViewState?> getAnnouncement(
    String announcementId,
    AnnouncementType type,
    String courseId,
    String institutionToolbarTitle,
    bool forceRefresh,
  ) async {
    if (type == AnnouncementType.COURSE) {
      Announcement? announcement =
          await _announcementApi().getCourseAnnouncement(courseId, announcementId, forceRefresh);

      Course? course = await _courseApi().getCourse(courseId);
      if (announcement == null || course == null) {
        return null;
      }

      return AnnouncementViewState(
        course.name,
        announcement.title,
        announcement.message,
        announcement.postedAt,
        announcement.attachments.isNotEmpty ? announcement.attachments.first.toAttachment() : null,
      );
    } else {
      AccountNotification? accountNotification =
          await _announcementApi().getAccountNotification(announcementId, forceRefresh);

      if (accountNotification == null)
        return null;

      return AnnouncementViewState(
        institutionToolbarTitle,
        accountNotification.subject,
        accountNotification.message,
        DateTime.parse(accountNotification.startAt),
        null, // Account notifications can't have attachments
      );
    }
  }

  void viewAttachment(BuildContext context, Attachment attachment) {
    locator<QuickNav>().push(context, ViewAttachmentScreen(attachment));
  }
}
