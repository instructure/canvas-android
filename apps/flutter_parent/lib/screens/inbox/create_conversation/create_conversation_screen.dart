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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/recipient.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_handler.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import 'package:transparent_image/transparent_image.dart';

import 'create_conversation_interactor.dart';

class CreateConversationScreen extends StatefulWidget {
  CreateConversationScreen(this._course) : _subjectTemplate = _course.name;

  final Course _course;
  final String _subjectTemplate;

  static final sendKey = Key("sendButton");
  static final attachmentKey = Key("attachmentButton");
  static final messageKey = Key("messageText");
  static final subjectKey = Key("subjectText");
  static final recipientsKey = Key("participantsContainer");
  static final recipientsAddKey = Key("participantsAddButton");

  @override
  _CreateConversationScreenState createState() => _CreateConversationScreenState(_subjectTemplate);
}

class _CreateConversationScreenState extends State<CreateConversationScreen> {
  _CreateConversationScreenState(this._subjectText);

  String _subjectText = "";
  String _bodyText = "";

  TextEditingController _subjectController;
  TextEditingController _bodyController = TextEditingController();

  List<Recipient> _allRecipients = [];
  List<Recipient> _selectedRecipients = [];
  List<AttachmentHandler> _attachments = [];

  bool _loading = false;
  bool _error = false;
  bool _sending = false;
  bool _recipientsExpanded = false;

  CreateConversationInteractor _interactor = locator<CreateConversationInteractor>();

  final _scaffoldKey = GlobalKey<ScaffoldState>();

  bool _canSend() =>
      _selectedRecipients.isNotEmpty &&
      _subjectText.isNotEmpty &&
      _bodyText.isNotEmpty &&
      !_sending &&
      _attachments.every((it) => it.stage == AttachmentUploadStage.FINISHED);

  @override
  void initState() {
    _setupTextControllers();
    _loadRecipients();
    super.initState();
  }

  _setupTextControllers() {
    _subjectController = TextEditingController(text: _subjectText);
    _subjectController.addListener(() {
      setState(() => _subjectText = _subjectController.text);
    });
    _bodyController.addListener(() {
      setState(() => _bodyText = _bodyController.text);
    });
  }

  _loadRecipients() {
    setState(() {
      _loading = true;
      _error = false;
    });
    _interactor.getAllRecipients(widget._course).then((data) {
      _allRecipients = data;
      String courseId = widget._course.id.toString();
      _selectedRecipients =
          _allRecipients.where((it) => it.commonCourses[courseId]?.contains("TeacherEnrollment")).toList();
      setState(() {
        _loading = false;
      });
    }).catchError((error) {
      print(error);
      setState(() {
        _loading = false;
        _error = true;
      });
    });
  }

  _send() async {
    setState(() => _sending = true);
    var recipientIds = _selectedRecipients.map((it) => it.id).toList();
    var attachmentIds = _attachments.map((it) => it.attachment.id).toList();
    try {
      await _interactor.createConversation(widget._course, recipientIds, _subjectText, _bodyText, attachmentIds);
      Navigator.of(context).pop(true); // 'true' indicates upload was successful
    } catch (e) {
      setState(() => _sending = false);
      _scaffoldKey.currentState.showSnackBar(
        SnackBar(content: Text(L10n(context).errorSendingMessage)),
      );
    }
  }

  Future<bool> _onWillPop() {
    if (_sending) return Future.value(false);
    if (_bodyText.isEmpty && _attachments.isEmpty) return Future.value(true);
    return showDialog(
          context: context,
          builder: (context) => new AlertDialog(
            title: new Text(L10n(context).unsavedChangesDialogTitle),
            content: new Text(L10n(context).unsavedChangesDialogBody),
            actions: <Widget>[
              new FlatButton(
                onPressed: () => Navigator.of(context).pop(false),
                child: new Text(L10n(context).no),
              ),
              new FlatButton(
                onPressed: () => Navigator.of(context).pop(true),
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
        child: Scaffold(
          key: _scaffoldKey,
          appBar: _appBar(context),
          body: _content(context),
        ),
      ),
    );
  }

  Widget _appBar(BuildContext context) {
    return AppBar(
      bottom: ParentTheme.of(context).appBarDivider(shadowInLightMode: false),
      title: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Text(L10n(context).newMessageTitle),
          Text(widget._course.courseCode, style: Theme.of(context).textTheme.caption),
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
        if (!_sending && !_error && !_loading)
          IconButton(
            tooltip: L10n(context).addAttachment,
            key: CreateConversationScreen.attachmentKey,
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
        if (!_sending && !_error && !_loading)
          IconButton(
            tooltip: L10n(context).sendMessage,
            key: CreateConversationScreen.sendKey,
            icon: Icon(Icons.send),
            color: Theme.of(context).accentColor,
            disabledColor: Theme.of(context).iconTheme.color.withOpacity(0.25),
            onPressed: _canSend() ? _send : null,
          )
      ],
    );
  }

  Widget _content(BuildContext context) {
    if (_error) return _errorState(context);
    if (_loading) return LoadingIndicator();
    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Divider(thickness: 1, height: 1),
          _recipientsWidget(context),
          Divider(thickness: 1, height: 1),
          _subjectWidget(context),
          Divider(thickness: 1, height: 1),
          _messageWidget(context),
          _attachmentsWidget(context),
        ],
      ),
    );
  }

  Widget _attachmentsWidget(BuildContext context) {
    return Container(
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
    );
  }

  Widget _errorState(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        return SingleChildScrollView(
          physics: AlwaysScrollableScrollPhysics(),
          child: Container(
            height: constraints.maxHeight,
            child: Padding(
              padding: const EdgeInsets.all(8.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  Icon(CanvasIcons.warning, size: 40, color: ParentColors.failure),
                  Padding(
                    padding: const EdgeInsets.fromLTRB(48, 28, 48, 32),
                    child: Text(
                      L10n(context).errorLoadingRecipients,
                      textAlign: TextAlign.center,
                      style: Theme.of(context).textTheme.caption.copyWith(fontSize: 16),
                    ),
                  ),
                  FlatButton(
                    onPressed: _loadRecipients,
                    child: Text(
                      L10n(context).retry,
                      style: Theme.of(context).textTheme.caption.copyWith(fontSize: 16),
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: new BorderRadius.circular(24.0),
                      side: BorderSide(color: ParentColors.tiara),
                    ),
                  )
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _recipientsWidget(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Expanded(
          child: Semantics(
            label: L10n(context).recipients,
            child: InkWell(
              onTap: () => setState(() => _recipientsExpanded = !_recipientsExpanded),
              child: Padding(
                padding: const EdgeInsets.fromLTRB(16, 4, 4, 4),
                child: Wrap(
                  crossAxisAlignment: WrapCrossAlignment.center,
                  key: CreateConversationScreen.recipientsKey,
                  spacing: 8,
                  runSpacing: 0,
                  children: _recipientChips(context),
                ),
              ),
            ),
          ),
        ),
        IconButton(
          tooltip: L10n(context).selectRecipients,
          key: CreateConversationScreen.recipientsAddKey,
          icon: Icon(
            CanvasIconsSolid.plus,
            size: 18,
            color: Theme.of(context).hintColor,
          ),
          onPressed: _sending ? null : () => _showRecipientPicker(context),
        )
      ],
    );
  }

  List<Widget> _recipientChips(BuildContext context) {
    if (_selectedRecipients.isEmpty)
      return [
        Chip(
          label: Text(L10n(context).noRecipientsSelected),
          backgroundColor: ParentTheme.of(context).nearSurfaceColor,
          avatar: Icon(
            CanvasIcons.warning,
            color: Colors.redAccent,
            size: 16,
          ),
        )
      ];
    if (_recipientsExpanded) {
      return _selectedRecipients
          .map((user) => Chip(
                label: Padding(
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  child: UserName(
                    user.name,
                    user.pronouns,
                    style: TextStyle(fontWeight: FontWeight.w500),
                  ),
                ),
                avatar: Avatar(user.avatarUrl, name: user.name),
                backgroundColor: ParentTheme.of(context).nearSurfaceColor,
              ))
          .toList();
    } else {
      return [
        Chip(
          label: Padding(
            padding: const EdgeInsets.symmetric(vertical: 8),
            child: ConstrainedBox(
              constraints: BoxConstraints(maxWidth: 180),
              child: UserName(
                _selectedRecipients[0].name,
                _selectedRecipients[0].pronouns,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(fontWeight: FontWeight.w500),
              ),
            ),
          ),
          avatar: Avatar(_selectedRecipients[0].avatarUrl, name: _selectedRecipients[0].name),
          backgroundColor: ParentTheme.of(context).nearSurfaceColor,
        ),
        if (_selectedRecipients.length > 1)
          Text(
            L10n(context).plusRecipientCount(_selectedRecipients.length - 1),
            style: TextStyle(fontWeight: FontWeight.w500),
          )
      ];
    }
  }

  Widget _subjectWidget(BuildContext context) {
    return Semantics(
      label: L10n(context).messageSubjectInputHint,
      child: TextField(
        key: CreateConversationScreen.subjectKey,
        controller: _subjectController,
        enabled: !_sending,
        style: Theme.of(context).textTheme.body2,
        textCapitalization: TextCapitalization.sentences,
        decoration: InputDecoration(
          hintText: L10n(context).messageSubjectInputHint,
          contentPadding: EdgeInsets.all(16),
          border: InputBorder.none,
        ),
      ),
    );
  }

  Widget _messageWidget(BuildContext context) {
    return Semantics(
      label: L10n(context).messageBodyInputHint,
      child: TextField(
        key: CreateConversationScreen.messageKey,
        controller: _bodyController,
        enabled: !_sending,
        textCapitalization: TextCapitalization.sentences,
        minLines: 4,
        maxLines: 10,
        style: Theme.of(context).textTheme.body1,
        decoration: InputDecoration(
          hintText: L10n(context).messageBodyInputHint,
          contentPadding: EdgeInsets.all(16),
          border: InputBorder.none,
        ),
      ),
    );
  }

  _showRecipientPicker(BuildContext context) async {
    await showModalBottomSheet(
      context: context,
      builder: (context) {
        return StatefulBuilder(builder: (context, setState) {
          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Padding(
                padding: const EdgeInsets.fromLTRB(16, 24, 16, 8),
                child: Text(
                  L10n(context).recipients,
                  style: Theme.of(context).textTheme.title,
                ),
              ),
              Expanded(
                child: ListTileTheme(
                  child: ListView.builder(
                    itemCount: _allRecipients.length,
                    itemBuilder: (context, index) {
                      final user = _allRecipients[index];
                      final selected = _selectedRecipients.contains(user);
                      return ListTile(
                        selected: selected,
                        title: UserName.fromRecipient(user),
                        subtitle: Text(_enrollmentType(context, user)),
                        leading: Avatar(
                          user.avatarUrl,
                          overlay: selected
                              ? Container(
                                  color: Theme.of(context).accentColor.withOpacity(0.8),
                                  child: Icon(Icons.check, color: Colors.white),
                                )
                              : null,
                        ),
                        onTap: () {
                          if (_selectedRecipients.contains(user)) {
                            _selectedRecipients.remove(user);
                          } else {
                            _selectedRecipients.add(user);
                          }
                          setState(() {});
                        },
                      );
                    },
                  ),
                ),
              ),
              Row(
                mainAxisSize: MainAxisSize.max,
                mainAxisAlignment: MainAxisAlignment.end,
                children: <Widget>[
                  FlatButton(
                    textColor: Theme.of(context).accentColor,
                    child: Text(L10n(context).done),
                    onPressed: () {
                      Navigator.of(context).pop();
                    },
                  ),
                ],
              ),
            ],
          );
        });
      },
    );
    setState(() {}); // Update page with selected recipients
  }

  String _enrollmentType(BuildContext context, Recipient user) {
    var type = user.commonCourses[widget._course.id.toString()].first;
    switch (type) {
      case "TeacherEnrollment":
        return L10n(context).enrollmentTypeTeacher;
      case "TaEnrollment":
        return L10n(context).enrollmentTypeTA;
      case "StudentEnrollment":
        return L10n(context).enrollmentTypeStudent;
      case "ObserverEnrollment":
        return L10n(context).enrollmentTypeObserver;
      default:
        return '';
    }
  }
}

class AttachmentWidget extends StatelessWidget {
  AttachmentWidget({Key key, this.onDelete}) : super(key: key);

  final Function(AttachmentHandler) onDelete;

  @override
  Widget build(BuildContext context) {
    AttachmentHandler handler = Provider.of<AttachmentHandler>(context);
    Widget child;
    switch (handler.stage) {
      case AttachmentUploadStage.CREATED:
      case AttachmentUploadStage.UPLOADING:
        child = _uploadingWidget(context, handler);
        break;
      case AttachmentUploadStage.FAILED:
        child = _failedWidget(context, handler);
        break;
      case AttachmentUploadStage.FINISHED:
        child = _finishedWidget(context, handler);
        break;
    }
    return Container(
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
          child,
        ],
      ),
    );
  }

  _uploadingWidget(BuildContext context, AttachmentHandler handler) {
    return Tooltip(
      message: handler.displayName,
      child: Stack(
        alignment: Alignment.center,
        children: [
          SizedBox(
            width: 48,
            height: 48,
            child: CircularProgressIndicator(
              value: handler.progress,
              backgroundColor: ParentTheme.of(context).nearSurfaceColor,
            ),
          ),
          Text(
            handler.progress == null ? "" : NumberFormat.percentPattern().format(handler.progress),
            style: TextStyle(fontSize: 14, fontWeight: FontWeight.w500),
          )
        ],
      ),
    );
  }

  _failedWidget(BuildContext context, AttachmentHandler handler) {
    return PopupMenuButton<String>(
      tooltip: handler.displayName,
      onSelected: (option) => _handleMenuOption(option, handler),
      itemBuilder: (context) => [
        PopupMenuItem<String>(
          value: 'retry',
          child: Text(L10n(context).retry),
        ),
        PopupMenuItem<String>(
          value: 'delete',
          child: Text(L10n(context).delete),
        ),
      ],
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          SizedBox(height: 6),
          Icon(
            CanvasIcons.warning,
            size: 27,
            color: Colors.red,
          ),
          SizedBox(height: 15),
          Text(
            L10n(context).attachmentFailed,
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 12),
          ),
        ],
      ),
    );
  }

  _finishedWidget(BuildContext context, AttachmentHandler handler) {
    return PopupMenuButton<String>(
      tooltip: handler.displayName,
      onSelected: (option) => _handleMenuOption(option, handler),
      itemBuilder: (context) => [
        PopupMenuItem<String>(
          value: 'delete',
          child: Text(L10n(context).delete),
        ),
      ],
      child: Stack(
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
                  handler.attachment.displayName,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 12, fontWeight: FontWeight.w500),
                ),
              )
            ],
          ),
          if (handler.attachment?.thumbnailUrl != null)
            ClipRRect(
              borderRadius: new BorderRadius.circular(4),
              child: FadeInImage.memoryNetwork(
                fadeInDuration: const Duration(milliseconds: 300),
                fit: BoxFit.cover,
                image: handler.attachment.thumbnailUrl ?? '',
                placeholder: kTransparentImage,
              ),
            ),
        ],
      ),
    );
  }

  void _handleMenuOption(String option, AttachmentHandler handler) {
    switch (option) {
      case "delete":
        if (onDelete != null) onDelete(handler);
        break;
      case "retry":
        handler.performUpload();
        break;
    }
  }
}
