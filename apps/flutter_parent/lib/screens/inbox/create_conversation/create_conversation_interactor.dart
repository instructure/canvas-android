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

import 'package:flutter/widgets.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/recipient.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_handler.dart';
import 'package:flutter_parent/utils/service_locator.dart';

import '../attachment_utils/attachment_picker.dart';

class CreateConversationInteractor {
  Future<CreateConversationData> loadData(String courseId, String studentId) async {
    final courseFuture = locator<CourseApi>().getCourse(courseId);
    final recipients = await locator<InboxApi>().getRecipients(courseId);

    // The only allowed recipients are teachers and the specific student
    recipients.retainWhere((it) {
      return it.id == studentId || it.commonCourses[courseId]?.contains('TeacherEnrollment') == true;
    });

    return CreateConversationData(await courseFuture, recipients);
  }

  Future<Conversation> createConversation(
    String courseId,
    List<String> recipientIds,
    String subject,
    String body,
    List<String> attachmentIds,
  ) {
    return locator<InboxApi>().createConversation(courseId, recipientIds, subject, body, attachmentIds);
  }

  Future<AttachmentHandler> addAttachment(BuildContext context) async {
    return AttachmentPicker.asBottomSheet(context);
  }
}

class CreateConversationData {
  final Course course;
  final List<Recipient> recipients;

  CreateConversationData(this.course, this.recipients);
}
