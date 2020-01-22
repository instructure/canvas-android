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

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/models/basic_user.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/style_slicer.dart';
import 'package:intl/intl.dart';
import 'package:transparent_image/transparent_image.dart';

class MessageWidget extends StatelessWidget {
  final Conversation conversation;
  final Message message;
  final String currentUserId;
  final Function(Attachment) onAttachmentClicked;

  const MessageWidget({
    Key key,
    @required this.conversation,
    @required this.message,
    @required this.currentUserId,
    this.onAttachmentClicked = null,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    var author = conversation.participants.firstWhere(
      (it) => it.id == message.authorId,
      orElse: () => BasicUser((b) => b..name = L10n(context).unknownUser),
    );
    var date = DateFormat(L10n(context).dateTimeFormat).format(message.createdAt.toLocal());
    return Container(
      padding: EdgeInsets.all(16),
      color: Theme.of(context).scaffoldBackgroundColor,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
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
                    _authorText(context, conversation, message, author),
                    SizedBox(height: 2),
                    Text(date, key: Key('message-date'), style: Theme.of(context).textTheme.subtitle),
                  ],
                ),
              ),
            ],
          ),
          SizedBox(height: 16),
          Text(message.body),
          _attachmentsWidget(context, message)
        ],
      ),
    );
  }

  Widget _authorText(BuildContext context, Conversation conversation, Message message, BasicUser author) {
    String authorInfo;
    List<StyleSlicer> slicers = [];
    Color authorColor = ParentTheme.of(context).onSurfaceColor;

    if (message.authorId == currentUserId) {
      var authorName = toBeginningOfSentenceCase(L10n(context).userNameMe);
      slicers.add(PatternSlice(authorName, style: TextStyle(color: authorColor), maxMatches: 1));
      if (message.participatingUserIds.length == 2) {
        var otherUser = conversation.participants.firstWhere(
          (it) => it.id != message.authorId,
          orElse: () => BasicUser((b) => b..name = L10n(context).unknownUser),
        );
        var recipientName = UserName.fromBasicUser(otherUser).text;
        slicers.add(PronounSlice(otherUser.pronouns));
        authorInfo = L10n(context).authorToRecipient(authorName, recipientName);
      } else if (message.participatingUserIds.length > 2) {
        authorInfo = L10n(context).authorToNOthers(authorName, message.participatingUserIds.length - 1);
      } else {
        authorInfo = authorName;
      }
    } else {
      // This is an 'incoming' message
      String authorName = UserName.fromBasicUser(author).text;
      slicers.add(PatternSlice(authorName, style: TextStyle(color: authorColor), maxMatches: 1));
      slicers.add(PronounSlice(author.pronouns));
      if (message.participatingUserIds.length == 2) {
        authorInfo = L10n(context).authorToRecipient(authorName, L10n(context).userNameMe);
      } else if (message.participatingUserIds.length > 2) {
        authorInfo = L10n(context).authorToRecipientAndNOthers(
          authorName,
          L10n(context).userNameMe,
          message.participatingUserIds.length - 2,
        );
      } else {
        authorInfo == authorName;
      }
    }

    return Text.rich(
      StyleSlicer.apply(authorInfo, slicers, baseStyle: Theme.of(context).textTheme.caption),
      key: Key('author-info'),
    );
  }

  Widget _attachmentsWidget(BuildContext context, Message message) {
    List<Attachment> attachments = message.attachments?.toList() ?? [];
    if (message.mediaComment != null) attachments.add(message.mediaComment.toAttachment());
    if (attachments.isEmpty) return Container();
    return Container(
      height: 108,
      padding: EdgeInsets.only(top: 12),
      child: ListView.separated(
        shrinkWrap: true,
        itemCount: attachments.length,
        scrollDirection: Axis.horizontal,
        separatorBuilder: (context, index) => SizedBox(width: 12),
        itemBuilder: (context, index) => _attachment(context, attachments[index]),
      ),
    );
  }

  Widget _attachment(BuildContext context, Attachment attachment) {
    return Container(
      key: Key('attachment-${attachment.id}'),
      width: 112,
      height: 96,
      child: Stack(
        alignment: Alignment.center,
        children: [
          Container(
            decoration: BoxDecoration(
              border: Border.all(color: ParentColors.tiara, width: 0.5),
              borderRadius: BorderRadius.all(Radius.circular(4)),
            ),
          ),
          Stack(
            fit: StackFit.expand,
            children: [
              Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    CanvasIcons.document,
                    color: Theme.of(context).accentColor,
                  ),
                  Padding(
                    padding: const EdgeInsets.fromLTRB(12, 11, 12, 0),
                    child: Text(
                      attachment.displayName,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      textAlign: TextAlign.center,
                      style: TextStyle(fontSize: 12, fontWeight: FontWeight.w500),
                    ),
                  )
                ],
              ),
              if (attachment.thumbnailUrl != null)
                ClipRRect(
                  borderRadius: new BorderRadius.circular(4),
                  child: FadeInImage.memoryNetwork(
                    fadeInDuration: const Duration(milliseconds: 300),
                    fit: BoxFit.cover,
                    image: attachment.thumbnailUrl,
                    placeholder: kTransparentImage,
                  ),
                ),
              Material(
                color: Colors.transparent,
                child: InkWell(
                  onTap: () {
                    if (onAttachmentClicked != null) onAttachmentClicked(attachment);
                  },
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
