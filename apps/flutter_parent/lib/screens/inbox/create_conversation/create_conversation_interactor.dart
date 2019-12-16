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

import 'package:flutter/cupertino.dart';
import 'package:flutter_parent/api/inbox_api.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/recipient.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_handler.dart';
import 'package:flutter_parent/utils/service_locator.dart';

import '../attachment_utils/attachment_picker.dart';

class CreateConversationInteractor {
  Future<List<Recipient>> getAllRecipients(Course course) async {
    return locator<InboxApi>().getRecipients(course);
  }

  Future<Conversation> createConversation(
    Course course,
    List<String> recipientIds,
    String subject,
    String body,
    List<String> attachmentIds,
  ) {
    return locator<InboxApi>().createConversation(course, recipientIds, subject, body, attachmentIds);
  }

  Future<AttachmentHandler> addAttachment(BuildContext context) async {
    return AttachmentPicker.asBottomSheet(context);
  }
}
