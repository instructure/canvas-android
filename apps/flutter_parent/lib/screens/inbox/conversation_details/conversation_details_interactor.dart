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

import 'package:flutter/material.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/dashboard/inbox_notifier.dart';
import 'package:flutter_parent/screens/inbox/reply/conversation_reply_screen.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/view_attachment_screen.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class ConversationDetailsInteractor {
  Future<Conversation?> getConversation(String id) async {
    Conversation? conversation = await locator<InboxApi>().getConversation(id, refresh: true);

    // Fetching a conversation automatically marks it as read, so we'll want to update the inbox count badge
    locator<InboxCountNotifier>().update();

    return conversation;
  }

  Future<Conversation?> addReply(BuildContext context, Conversation? conversation, Message? message, bool replyAll) async {
    Conversation? r = await locator<QuickNav>().push(context, ConversationReplyScreen(conversation, message, replyAll));
    return r;
  }

  String? getCurrentUserId() => ApiPrefs.getUser()?.id;

  Future<void> viewAttachment(BuildContext context, Attachment attachment) async {
    locator<QuickNav>().push(context, ViewAttachmentScreen(attachment));
  }
}
