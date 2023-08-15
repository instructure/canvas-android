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
import 'package:flutter_parent/utils/common_widgets/view_attachment/view_attachment_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/audio_video_attachment_viewer.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/image_attachment_viewer.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/text_attachment_viewer.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/unknown_attachment_type_viewer.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class ViewAttachmentScreen extends StatefulWidget {
  const ViewAttachmentScreen(this.attachment, {super.key});

  final Attachment attachment;

  @override
  _ViewAttachmentScreenState createState() => _ViewAttachmentScreenState();
}

class _ViewAttachmentScreenState extends State<ViewAttachmentScreen> {
  GlobalKey<ScaffoldState> scaffoldKey = GlobalKey();

  final _interactor = locator<ViewAttachmentInteractor>();

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        key: scaffoldKey,
        appBar: AppBar(
          elevation: 2,
          title: Text(widget.attachment.displayName ?? widget.attachment.filename ?? ''),
          bottom: ParentTheme.of(context)?.appBarDivider(),
          actions: <Widget>[_overflowMenu()],
        ),
        body: _body(context),
      ),
    );
  }

  Widget _body(BuildContext context) {
    String? contentType = widget.attachment.inferContentType();

    if (contentType == null) return UnknownAttachmentTypeViewer(widget.attachment);

    if (contentType.startsWith('audio') || contentType.startsWith('video')) {
      return AudioVideoAttachmentViewer(widget.attachment);
    } else if (contentType.startsWith('image')) {
      return ImageAttachmentViewer(widget.attachment);
    } else if (contentType == 'text/plain') {
      return TextAttachmentViewer(widget.attachment);
    }

    return UnknownAttachmentTypeViewer(widget.attachment);
  }

  Widget _overflowMenu() {
    return PopupMenuButton(
      onSelected: (option) {
        if (option == 'open_externally') {
          _openExternally();
        } else {
          _interactor.downloadFile(widget.attachment);
        }
      },
      itemBuilder: (context) => [
        PopupMenuItem<String>(
          value: 'download',
          child: ListTile(
            contentPadding: EdgeInsets.zero,
            leading: Icon(Icons.file_download),
            title: Text(L10n(context).download),
          ),
        ),
        PopupMenuItem<String>(
          value: 'open_externally',
          child: ListTile(
            contentPadding: EdgeInsets.zero,
            leading: Icon(Icons.open_in_new),
            title: Text(L10n(context).openFileExternally),
          ),
        ),
      ],
    );
  }

  _openExternally() {
    _interactor.openExternally(widget.attachment).catchError((_) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(L10n(context).noApplicationsToHandleFile),
        ),
      );
    });
  }
}
