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
import 'package:flutter_linkify/flutter_linkify.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/models/basic_user.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/utils/common_widgets/attachment_indicator_widget.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/style_slicer.dart';
import 'package:intl/intl.dart';

class MessageWidget extends StatefulWidget {
  final Conversation? conversation;
  final Message? message;
  final String? currentUserId;
  final Function(Attachment)? onAttachmentClicked;

  const MessageWidget({
    this.conversation,
    required this.message,
    required this.currentUserId,
    this.onAttachmentClicked = null,
    super.key,
  });

  @override
  _MessageWidgetState createState() => _MessageWidgetState();
}

class _MessageWidgetState extends State<MessageWidget> {
  bool _participantsExpanded = false;

  @override
  Widget build(BuildContext context) {
    var author = widget.conversation?.participants?.firstWhere(
      (it) => it.id == widget.message?.authorId,
      orElse: () => BasicUser((b) => b..name = L10n(context).unknownUser),
    );
    var date = widget.message?.createdAt.l10nFormat(L10n(context).dateAtTime);
    if (author == null || date == null) return Container();
    return Container(
      padding: EdgeInsets.symmetric(vertical: 16),
      color: Theme.of(context).scaffoldBackgroundColor,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          _header(author, context, date),
          Padding(
            padding: EdgeInsets.symmetric(horizontal: 16),
            child: Linkify(
              text: widget.message?.body ?? '',
              options: LinkifyOptions(humanize: false),
              onOpen: (link) => locator<QuickNav>().routeInternally(context, link.url),
            ),
          ),
          _attachmentsWidget(context, widget.message)
        ],
      ),
    );
  }

  Widget _header(BasicUser author, BuildContext context, String date) {
    return Material(
      key: Key('message-header'),
      child: InkWell(
        onTap:
          widget.message?.participatingUserIds != null && widget.message!.participatingUserIds!.length > 1 // Only allow expansion if there are non-author participants
            ? () => setState(() => _participantsExpanded = !_participantsExpanded)
            : null,
        child: Padding(
          padding: EdgeInsets.symmetric(horizontal: 16),
          child: Column(
            children: <Widget>[
              Row(
                children: <Widget>[
                  Avatar(author.avatarUrl, name: author.name),
                  SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: <Widget>[
                        if (widget.message != null) _authorText(context, widget.conversation, widget.message!, author),
                        SizedBox(height: 2),
                        Text(date, key: Key('message-date'), style: Theme.of(context).textTheme.titleSmall),
                      ],
                    ),
                  ),
                ],
              ),
              if (_participantsExpanded) _participants(author),
              SizedBox(height: 16),
            ],
          ),
        ),
      ),
    );
  }

  Widget _participants(BasicUser author) {
    var participants = widget.message!.participatingUserIds!
        .map((id) => widget.conversation?.participants!.firstWhere((it) => it.id == id))
        .toList()
          ..retainWhere((it) => it?.id != author.id);
    return Padding(
      key: Key('participants'),
      padding: const EdgeInsetsDirectional.only(top: 16, start: 52),
      child: ListView.separated(
        itemCount: participants.length,
        shrinkWrap: true,
        separatorBuilder: (context, index) => SizedBox(height: 8),
        itemBuilder: (context, index) {
          var user = participants[index];
          return Row(
            children: <Widget>[
              Avatar(user?.avatarUrl, name: user?.name ?? '', radius: 16),
              SizedBox(width: 12),
              Expanded(
                  child: Text(user?.name ?? '',
                      key: ValueKey('participant_id_${user?.id}'),
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(fontSize: 14)))
            ],
          );
        },
      ),
    );
  }

  Widget _authorText(BuildContext context, Conversation? conversation, Message message, BasicUser author) {
    String authorInfo = '';
    List<StyleSlicer> slicers = [];
    Color? authorColor = ParentTheme.of(context)?.onSurfaceColor;

    if (message.authorId == widget.currentUserId) {
      var authorName = toBeginningOfSentenceCase(L10n(context).userNameMe) ?? '';
      slicers.add(PatternSlice(authorName, style: TextStyle(color: authorColor), maxMatches: 1));
      if (message.participatingUserIds!.length == 2) {
        var otherUser = conversation?.participants?.firstWhere(
          (it) => it.id != message.authorId,
          orElse: () => BasicUser((b) => b..name = L10n(context).unknownUser),
        );
      var recipientName = UserName.fromBasicUser(otherUser!).text;
        slicers.add(PronounSlice(otherUser.pronouns));
        authorInfo = L10n(context).authorToRecipient(authorName, recipientName);
      } else if (message.participatingUserIds!.length > 2) {
        authorInfo = L10n(context).authorToNOthers(authorName, message.participatingUserIds!.length - 1);
      } else {
        authorInfo = authorName;
      }
    } else {
      // This is an 'incoming' message
      String authorName = UserName.fromBasicUser(author).text;
      slicers.add(PatternSlice(authorName, style: TextStyle(color: authorColor), maxMatches: 1));
      slicers.add(PronounSlice(author.pronouns));
      if (message.participatingUserIds!.length == 2) {
        authorInfo = L10n(context).authorToRecipient(authorName, L10n(context).userNameMe);
      } else if (message.participatingUserIds!.length > 2) {
        authorInfo = L10n(context).authorToRecipientAndNOthers(
          authorName,
          L10n(context).userNameMe,
          message.participatingUserIds!.length - 2,
        );
      } else {
        authorInfo == authorName;
      }
    }

    return Text.rich(
      StyleSlicer.apply(authorInfo, slicers, baseStyle: Theme.of(context).textTheme.bodySmall),
      key: Key('author-info'),
    );
  }

  Widget _attachmentsWidget(BuildContext context, Message? message) {
    List<Attachment> attachments = message?.attachments?.toList() ?? [];
    if (message?.mediaComment != null) attachments.add(message!.mediaComment!.toAttachment());
    if (attachments.isEmpty) return Container();
    return Container(
      height: 108,
      padding: EdgeInsets.only(top: 12),
      child: ListView.separated(
        padding: EdgeInsets.symmetric(horizontal: 16),
        key: Key('message_attachment_list'),
        shrinkWrap: true,
        itemCount: attachments.length,
        scrollDirection: Axis.horizontal,
        separatorBuilder: (context, index) => SizedBox(width: 12),
        itemBuilder: (context, index) =>
            AttachmentIndicatorWidget(attachment: attachments[index], onAttachmentClicked: widget.onAttachmentClicked),
      ),
    );
  }
}
