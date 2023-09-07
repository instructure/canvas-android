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
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_handler.dart';
import 'package:flutter_parent/screens/inbox/conversation_details/message_widget.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_screen.dart';
import 'package:flutter_parent/utils/common_widgets/arrow_aware_focus_scope.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/view_attachment_screen.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:provider/provider.dart';

import 'conversation_reply_interactor.dart';

class ConversationReplyScreen extends StatefulWidget {
  ConversationReplyScreen(this.conversation, this.message, this.replyAll);

  final Conversation? conversation;
  final Message? message;
  final bool replyAll;

  static final sendKey = Key('sendButton');
  static final attachmentKey = Key('attachmentButton');
  static final messageKey = Key('messageText');

  @override
  _ConversationReplyScreenState createState() => _ConversationReplyScreenState();
}

class _ConversationReplyScreenState extends State<ConversationReplyScreen> {
  _ConversationReplyScreenState();

  String _bodyText = '';
  List<AttachmentHandler> _attachments = [];
  bool _sending = false;
  TextEditingController _bodyController = TextEditingController();
  ConversationReplyInteractor _interactor = locator<ConversationReplyInteractor>();
  FocusScopeNode _focusScopeNode = FocusScopeNode();

  final _scaffoldKey = GlobalKey<ScaffoldState>();

  bool _canSend() =>
      _bodyText.isNotEmpty && !_sending && _attachments.every((it) => it.stage == AttachmentUploadStage.FINISHED);

  @override
  void initState() {
    _bodyController.addListener(() {
      setState(() => _bodyText = _bodyController.text);
    });
    super.initState();
  }

  @override
  void dispose() {
    _focusScopeNode.dispose();
    super.dispose();
  }

  _send() async {
    setState(() => _sending = true);
    var attachmentIds = _attachments.map((it) => it.attachment?.id).toList().nonNulls.toList();
    try {
      var result = await _interactor.createReply(
        widget.conversation,
        widget.message,
        _bodyText,
        attachmentIds,
        widget.replyAll,
      );
      var newMessage = result?.messages?.first;
      Conversation? updatedConversation = null;
      if (newMessage != null) updatedConversation = widget.conversation?.rebuild((c) => c..messages.insert(0, newMessage));
      Navigator.of(context).pop(updatedConversation); // Return updated conversation
    } catch (e) {
      setState(() => _sending = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(L10n(context).errorSendingMessage)),
      );
    }
  }

  Future<bool> _onWillPop() async {
    if (_sending) return Future.value(false);
    if (_bodyText.isEmpty && _attachments.isEmpty) return Future.value(true);
    return await showDialog(
          context: context,
          builder: (context) => new AlertDialog(
            title: new Text(L10n(context).unsavedChangesDialogTitle),
            content: new Text(L10n(context).unsavedChangesDialogBody),
            actions: <Widget>[
              new TextButton(
                onPressed: () => Navigator.of(context).pop(false),
                child: new Text(L10n(context).no),
              ),
              new TextButton(
                onPressed: () {
                  _attachments.forEach((it) => it.deleteAttachment());
                  Navigator.of(context).pop(true);
                },
                child: new Text(L10n(context).yes),
              ),
            ],
          ),
        ) ??
        false;
  }

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => WillPopScope(
        onWillPop: _onWillPop,
        child: ArrowAwareFocusScope(
          node: _focusScopeNode,
          child: Scaffold(
            key: _scaffoldKey,
            appBar: _appBar(context),
            body: _content(context),
          ),
        ),
      ),
    );
  }

  AppBar _appBar(BuildContext context) {
    return AppBar(
      bottom: ParentTheme.of(context)?.appBarDivider(shadowInLightMode: false),
      title: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Text(widget.replyAll ? L10n(context).replyAll : L10n(context).reply, style: Theme.of(context).textTheme.bodySmall),
          Text(widget.conversation?.subject ?? '', style: Theme.of(context).textTheme.bodySmall),
        ],
      ),
      actions: [
        if (_sending)
          Center(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Container(
                width: 24,
                height: 24,
                child: CircularProgressIndicator(
                  strokeWidth: 3,
                ),
              ),
            ),
          ),
        if (!_sending)
          IconButton(
            tooltip: L10n(context).addAttachment,
            key: ConversationReplyScreen.attachmentKey,
            icon: Icon(CanvasIcons.paperclip),
            onPressed: _sending
                ? null
                : () {
                    _interactor.addAttachment(context).then((attachment) {
                      if (attachment != null) {
                        attachment.onStageChange = (_) {
                          setState(() {});
                        };
                        attachment.performUpload();
                        setState(() => _attachments.add(attachment));
                      }
                    });
                  },
          ),
        if (!_sending)
          IconButton(
            tooltip: L10n(context).sendMessage,
            key: ConversationReplyScreen.sendKey,
            icon: Icon(Icons.send),
            color: Theme.of(context).colorScheme.secondary,
            disabledColor: Theme.of(context).iconTheme.color?.withOpacity(0.25),
            onPressed: _canSend() ? _send : null,
          )
      ],
    );
  }

  Widget _content(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          MessageWidget(
            conversation: widget.conversation,
            message: widget.message ?? widget.conversation?.messages?[0],
            currentUserId: _interactor.getCurrentUserId(),
            onAttachmentClicked: (attachment) {
              locator<QuickNav>().push(context, ViewAttachmentScreen(attachment));
            },
          ),
          Divider(height: 1),
          _editorWidget(context),
          _attachmentsWidget(context),
        ],
      ),
    );
  }

  Widget _attachmentsWidget(BuildContext context) {
    if (_attachments.isEmpty) return Container();
    return IgnorePointer(
      ignoring: _sending,
      child: Container(
        height: 104,
        padding: EdgeInsets.symmetric(vertical: 4),
        child: ListView.separated(
          shrinkWrap: true,
          itemCount: _attachments.length,
          padding: const EdgeInsets.symmetric(horizontal: 16),
          scrollDirection: Axis.horizontal,
          separatorBuilder: (context, index) => SizedBox(width: 12),
          itemBuilder: (context, index) {
            var handler = _attachments[index];
            return ChangeNotifierProvider<AttachmentHandler>.value(
              value: handler,
              child: AttachmentWidget(
                onDelete: (handler) {
                  setState(() {
                    _attachments.remove(handler);
                  });
                },
              ),
            );
          },
        ),
      ),
    );
  }

  Widget _editorWidget(BuildContext context) {
    return Semantics(
      label: L10n(context).messageBodyInputHint,
      child: TextField(
        autofocus: true,
        key: ConversationReplyScreen.messageKey,
        controller: _bodyController,
        enabled: !_sending,
        textCapitalization: TextCapitalization.sentences,
        minLines: 4,
        maxLines: null,
        style: Theme.of(context).textTheme.bodyMedium,
        decoration: InputDecoration(
          hintText: L10n(context).messageBodyInputHint,
          contentPadding: EdgeInsets.all(16),
          border: InputBorder.none,
        ),
      ),
    );
  }
}
