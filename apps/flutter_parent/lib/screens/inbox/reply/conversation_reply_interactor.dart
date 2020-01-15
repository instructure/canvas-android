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
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_handler.dart';
import 'package:flutter_parent/utils/service_locator.dart';

import '../attachment_utils/attachment_picker.dart';

class ConversationReplyInteractor {
  Future<Conversation> createReply(
    Conversation conversation,
    Message message,
    String body,
    List<String> attachmentIds,
    bool replyAll,
  ) {
    Message replyMessage = message ?? conversation.messages[0];
    List<String> includedMessageIds = [if (message != null || !replyAll) replyMessage.id];
    List<String> recipientIds = [];

    if (!replyAll) {
      if (replyMessage.authorId == getCurrentUserId()) {
        recipientIds = replyMessage.participatingUserIds.toList();
      } else {
        recipientIds = [replyMessage.authorId];
      }
    } else if (message != null) {
      recipientIds = replyMessage.participatingUserIds.toList();
    }

    return locator<InboxApi>().addMessage(conversation.id, body, recipientIds, attachmentIds, includedMessageIds);
  }

  Future<AttachmentHandler> addAttachment(BuildContext context) async {
    return AttachmentPicker.asBottomSheet(context);
  }

  String getCurrentUserId() => ApiPrefs.getUser().id;
}
