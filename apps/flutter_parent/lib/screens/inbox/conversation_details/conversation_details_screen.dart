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
import 'package:flutter/rendering.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/models/basic_user.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_slidable/flutter_slidable.dart';
import 'package:intl/intl.dart';
import 'package:transparent_image/transparent_image.dart';

import 'conversation_details_interactor.dart';

class ConversationDetailsScreen extends StatefulWidget {
  final String conversationId;
  final String conversationSubject;
  final String courseName;

  const ConversationDetailsScreen({
    Key key,
    this.conversationId,
    this.conversationSubject,
    this.courseName,
  }) : super(key: key);

  @override
  _ConversationDetailsScreenState createState() => _ConversationDetailsScreenState();
}

class _ConversationDetailsScreenState extends State<ConversationDetailsScreen> {
  ConversationDetailsInteractor _interactor = locator<ConversationDetailsInteractor>();
  Future<Conversation> _conversationFuture;

  bool _hasBeenUpdated = false;

  @override
  void initState() {
    _conversationFuture = _interactor.getConversation(widget.conversationId);
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        // Return true from this screen if the conversation has been updated
        Navigator.pop(context, _hasBeenUpdated);
        return false;
      },
      child: DefaultParentTheme(
        builder: (context) => FutureBuilder(
          future: _conversationFuture,
          builder: (BuildContext context, AsyncSnapshot<Conversation> snapshot) => Scaffold(
            backgroundColor: ParentTheme.of(context).nearSurfaceColor,
            appBar: _appBar(context),
            body: _body(context, snapshot),
            floatingActionButton: _fab(context, snapshot),
          ),
        ),
      ),
    );
  }

  Widget _appBar(BuildContext context) {
    return AppBar(
      elevation: 0,
      title: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Text(widget.conversationSubject == null || widget.conversationSubject.isEmpty
              ? L10n(context).noSubject
              : widget.conversationSubject),
          if (widget.courseName != null && widget.courseName.isNotEmpty)
            Text(widget.courseName, style: Theme.of(context).textTheme.caption),
        ],
      ),
      bottom: PreferredSize(
        child: Container(color: Theme.of(context).dividerColor, height: 0.5),
        preferredSize: Size.fromHeight(0.5),
      ),
    );
  }

  Widget _fab(BuildContext context, AsyncSnapshot<Conversation> snapshot) {
    if (!snapshot.hasData) return Container();
    Conversation conversation = snapshot.data;
    return FloatingActionButton(
      child: Icon(CanvasIconsSolid.reply, size: 20),
      tooltip: L10n(context).reply,
      onPressed: () {
        showModalBottomSheet(
            context: context,
            builder: (BuildContext bc) {
              return Container(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: <Widget>[
                    ListTile(
                        leading: Icon(CanvasIconsSolid.reply, size: 20, color: Theme.of(context).iconTheme.color),
                        title: Text(L10n(context).reply),
                        onTap: () {
                          Navigator.of(context).pop();
                          _reply(context, conversation, null, false);
                        }),
                    ListTile(
                        leading: Icon(CanvasIconsSolid.reply_all_2, size: 20, color: Theme.of(context).iconTheme.color),
                        title: Text(L10n(context).replyAll),
                        onTap: () {
                          Navigator.of(context).pop();
                          _reply(context, conversation, null, true);
                        }),
                  ],
                ),
              );
            });
      },
    );
  }

  Widget _body(BuildContext context, AsyncSnapshot<Conversation> snapshot) {
    if (snapshot.hasError) return _errorState(context);
    if (snapshot.hasData) return _successState(context, snapshot.data);
    return LoadingIndicator();
  }

  Widget _errorState(BuildContext context) {
    return ErrorPandaWidget(L10n(context).errorLoadingConversation, () {
      setState(() {
        _conversationFuture = _interactor.getConversation(widget.conversationId);
      });
    });
  }

  Widget _successState(BuildContext context, Conversation conversation) {
    return RefreshIndicator(
      onRefresh: () {
        setState(() {
          _conversationFuture = _interactor.getConversation(widget.conversationId);
        });
        return _conversationFuture.catchError((_) {});
      },
      child: ListView.separated(
        itemCount: conversation.messages?.length ?? 0,
        separatorBuilder: (context, index) => SizedBox(height: 12),
        itemBuilder: (context, index) {
          var message = conversation.messages[index];
          return _message(context, conversation, message);
        },
      ),
    );
  }

  Widget _message(BuildContext context, Conversation conversation, Message message) {
    var author = conversation.participants.firstWhere(
      (it) => it.id == message.authorId,
      orElse: () => BasicUser((b) => b..name = L10n(context).unknownUser),
    );
    var date = DateFormat(L10n(context).dateTimeFormat).format(message.createdAt.toLocal());
    return Semantics(
      customSemanticsActions: {
        CustomSemanticsAction(label: L10n(context).reply): () => _reply(context, conversation, message, false),
        CustomSemanticsAction(label: L10n(context).replyAll): () => _reply(context, conversation, message, true),
      },
      child: Slidable(
        key: Key('message-${message.id}'),
        actionPane: SlidableDrawerActionPane(),
        secondaryActions: <Widget>[
          IconSlideAction(
            caption: L10n(context).replyAll,
            color: ParentTheme.of(context).isDarkMode ? ParentColors.tiara : ParentColors.oxford,
            foregroundColor: Theme.of(context).accentIconTheme.color,
            iconWidget: Padding(
              padding: const EdgeInsets.only(bottom: 16),
              child: Icon(
                CanvasIconsSolid.reply_all_2,
                color: Theme.of(context).accentIconTheme.color,
              ),
            ),
            onTap: () => _reply(context, conversation, message, true),
          ),
          IconSlideAction(
            caption: L10n(context).reply,
            color: Theme.of(context).accentColor,
            foregroundColor: Theme.of(context).accentIconTheme.color,
            iconWidget: Padding(
              padding: const EdgeInsets.only(bottom: 16),
              child: Icon(
                CanvasIconsSolid.reply,
                color: Theme.of(context).accentIconTheme.color,
              ),
            ),
            onTap: () => _reply(context, conversation, message, false),
          ),
        ],
        child: Container(
          padding: EdgeInsets.all(16),
          color: Theme.of(context).scaffoldBackgroundColor,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Row(
                children: <Widget>[
                  Avatar(author.avatarUrl, name: author.name),
                  SizedBox(width: 12),
                  Column(
                    mainAxisSize: MainAxisSize.min,
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      _authorText(context, conversation, message, author),
                      SizedBox(height: 2),
                      Text(date, key: Key('message-date'), style: Theme.of(context).textTheme.subtitle),
                    ],
                  ),
                ],
              ),
              SizedBox(height: 16),
              Text(message.body),
              if (message.attachments?.isNotEmpty == true) _attachmentsWidget(context, message.attachments.toList())
            ],
          ),
        ),
      ),
    );
  }

  Widget _authorText(BuildContext context, Conversation conversation, Message message, BasicUser author) {
    List<InlineSpan> spans = [];
    var currentUserId = _interactor.getCurrentUserId();

    if (message.authorId == currentUserId) {
      spans.add(
        TextSpan(
          text: L10n(context).authorNameMe,
          style: TextStyle(color: ParentTheme.of(context).onSurfaceColor),
        ),
      );

      if (message.participatingUserIds.length == 2) {
        var otherUser = conversation.participants.firstWhere(
          (it) => it.id != message.authorId,
          orElse: () => BasicUser((b) => b..name = L10n(context).unknownUser),
        );
        var userName = UserName.fromBasicUser(otherUser).span.toPlainText();
        spans.add(UserName.stylize(L10n(context).toUser(userName), [otherUser.pronouns]));
      } else if (message.participatingUserIds.length > 2) {
        spans.add(
          TextSpan(
            text: L10n(context).toNOthers(message.participatingUserIds.length - 1),
          ),
        );
      }
    } else {
      // This is an 'incoming' message
      spans.add(
        UserName.fromBasicUser(
          author,
          style: Theme.of(context).textTheme.caption.copyWith(color: ParentTheme.of(context).onSurfaceColor),
        ).span,
      );

      if (message.participatingUserIds.length == 2) {
        spans.add(TextSpan(text: L10n(context).toMe));
      } else if (message.participatingUserIds.length > 2) {
        spans.add(TextSpan(text: L10n(context).toMeAndNOthers(message.participatingUserIds.length - 2)));
      }
    }

    return Text.rich(
      TextSpan(children: spans),
      key: Key('author-info'),
      style: Theme.of(context).textTheme.caption,
    );
  }

  Widget _attachmentsWidget(BuildContext context, List<Attachment> attachments) {
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
                    _interactor.viewAttachment(context, attachment);
                  },
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Future<void> _reply(BuildContext context, Conversation conversation, Message message, bool replyAll) async {
    var newConversation = await _interactor.addReply(context, conversation, message, replyAll);
    if (newConversation != null) {
      _hasBeenUpdated = true;
      setState(() {
        _conversationFuture = Future.value(newConversation);
      });
    }
  }
}
