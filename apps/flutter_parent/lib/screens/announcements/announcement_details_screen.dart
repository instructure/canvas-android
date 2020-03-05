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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/screens/announcements/announcement_view_state.dart';
import 'package:flutter_parent/utils/common_widgets/attachment_indicator_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/canvas_web_view.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/service_locator.dart';

import 'announcement_details_interactor.dart';

enum AnnouncementType { INSTITUTION, COURSE }

class AnnouncementDetailScreen extends StatefulWidget {
  final String announcementId;
  final String courseId;
  final AnnouncementType announcementType;

  AnnouncementDetailScreen(this.announcementId, this.announcementType, this.courseId, BuildContext context, {Key key})
      : super(key: key);

  @override
  State createState() => _AnnouncementDetailScreenState();
}

class _AnnouncementDetailScreenState extends State<AnnouncementDetailScreen> {
  Future<AnnouncementViewState> _announcementFuture;

  Future<AnnouncementViewState> _loadAnnouncement(BuildContext context) => _interactor.getAnnouncement(
      widget.announcementId, widget.announcementType, widget.courseId, L10n(context).institutionAnnouncementTitle);

  get _interactor => locator<AnnouncementDetailsInteractor>();

  @override
  Widget build(BuildContext context) {
    if (_announcementFuture == null) {
      _announcementFuture = _loadAnnouncement(context);
    }
    return FutureBuilder(
      future: _announcementFuture,
      builder: (context, AsyncSnapshot<AnnouncementViewState> snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Container(color: Theme.of(context).scaffoldBackgroundColor, child: LoadingIndicator());
        }

        if (snapshot.hasError || snapshot.data == null) {
          return _error();
        } else {
          return _announcementScaffold(snapshot.data);
        }
      },
    );
  }

  Widget _announcementScaffold(AnnouncementViewState announcementViewState) {
    return Scaffold(
      appBar: AppBar(
        title: Text(announcementViewState.toolbarTitle),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: _announcementBody(announcementViewState),
      ),
    );
  }

  Widget _announcementBody(AnnouncementViewState announcementViewState) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.start,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          announcementViewState.announcementTitle,
          style: Theme.of(context).textTheme.display1,
        ),
        SizedBox(height: 4),
        Text(
          announcementViewState.postedAt.l10nFormat(L10n(context).dateAtTime),
          style: Theme.of(context).textTheme.caption,
        ),
        SizedBox(height: 20),
        Divider(),
        Expanded(
          child: CanvasWebView(
            content: announcementViewState.announcementMessage,
          ),
        ),
        _attachmentsWidget(context, announcementViewState.attachment),
      ],
    );
  }

  Widget _error() {
    return Container(
        color: Theme.of(context).scaffoldBackgroundColor,
        child: ErrorPandaWidget(L10n(context).errorLoadingAnnouncement, () {
          setState(() {
            _announcementFuture = _loadAnnouncement(context);
          });
        }));
  }

  Widget _attachmentsWidget(BuildContext context, Attachment attachment) {
    if (attachment == null) return Container();
    return Container(
        height: 108,
        padding: EdgeInsets.only(top: 12),
        child: AttachmentIndicatorWidget(
            attachment: attachment,
            onAttachmentClicked: (attachment) => {_interactor.viewAttachment(context, attachment)}));
  }
}
