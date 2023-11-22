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
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_handler.dart';
import 'package:flutter_parent/utils/service_locator.dart';

import '../attachment_utils/attachment_picker.dart';

class ConversationReplyInteractor {
  Future<Conversation?> createReply(
    Conversation? conversation,
    Message? message,
    String body,
    List<String> attachmentIds,
    bool replyAll,
  ) async {
    Message replyMessage = message ?? conversation!.messages![0];
    List<String> includedMessageIds = [if (message != null || !replyAll) replyMessage.id];
    List<String> recipientIds = [];

    if (!replyAll) {
      if (replyMessage.authorId == getCurrentUserId()) {
        recipientIds = replyMessage.participatingUserIds!.toList();
      } else {
        recipientIds = [replyMessage.authorId];
      }
    } else {
      // We need to make sure the recipients list doesn't contain any off limit users, such as non-observed students.
      final courseId = conversation?.getContextId();
      final userId = ApiPrefs.getUser()?.id;
      final enrollments = await locator<EnrollmentsApi>().getObserveeEnrollments();
      final observeeIds = enrollments
          ?.map((enrollment) => enrollment.observedUser)
          .where((student) => student != null)
          .toSet()
          .map<String>((student) => student!.id);
      final permissions = await locator<CourseApi>().getCoursePermissions(courseId!);
      final recipients = await locator<InboxApi>().getRecipients(courseId);
      recipients?.retainWhere((recipient) {
        // Allow self and any observed students as recipients if the sendMessages permission is granted
        if (permissions?.sendMessages == true && (observeeIds?.contains(recipient.id) == true || recipient.id == userId))
          return true;

        // Always allow instructors (teachers and TAs) as recipients
        var enrollments = recipient.commonCourses![courseId];
        if (enrollments == null) return false;
        return enrollments.contains('TeacherEnrollment') || enrollments.contains('TaEnrollment');
      });

      final filteredRecipientIds = recipients?.map<String>((recipient) => recipient.id);

      recipientIds = replyMessage.participatingUserIds!
          .toList()
          .where((participantId) => filteredRecipientIds?.contains(participantId) == true)
          .toList();
    }

    return locator<InboxApi>().addMessage(conversation?.id, body, recipientIds, attachmentIds, includedMessageIds);
  }

  Future<AttachmentHandler?> addAttachment(BuildContext context) async {
    return AttachmentPicker.asBottomSheet(context);
  }

  String getCurrentUserId() => ApiPrefs.getUser()!.id;
}
