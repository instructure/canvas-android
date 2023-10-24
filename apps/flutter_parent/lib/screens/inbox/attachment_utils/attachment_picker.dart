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

import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_picker_interactor.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_svg/svg.dart';

import 'attachment_handler.dart';

class AttachmentPicker extends StatefulWidget {
  @override
  _AttachmentPickerState createState() => _AttachmentPickerState();

  static Future<AttachmentHandler?> asBottomSheet(BuildContext context) {
    return showModalBottomSheet(context: context, builder: (context) => AttachmentPicker());
  }
}

class _AttachmentPickerState extends State<AttachmentPicker> {
  bool _importing = false;

  @override
  Widget build(BuildContext context) {
    return _importing ? _importingWidget(context) : _pickerWidget(context);
  }

  Widget _pickerWidget(BuildContext context) {
    final interactor = locator<AttachmentPickerInteractor>();
    final iconColor = ParentTheme.of(context)?.onSurfaceColor;
    return ListView(
      padding: EdgeInsets.symmetric(vertical: 8),
      shrinkWrap: true,
      children: <Widget>[
        _item(
          icon: SvgPicture.asset('assets/svg/camera.svg', color: iconColor),
          title: L10n(context).useCamera,
          onTap: () => _performImport(() => interactor.getImageFromCamera()),
        ),
        _item(
          icon: Icon(CanvasIcons.paperclip, size: 20, color: iconColor),
          title: L10n(context).uploadFile,
          onTap: () => _performImport(() => interactor.getFileFromDevice()),
        ),
        _item(
          icon: Icon(CanvasIcons.image, size: 20, color: iconColor),
          title: L10n(context).chooseFromGallery,
          onTap: () => _performImport(() => interactor.getImageFromGallery()),
        ),
      ],
    );
  }

  Widget _item({required Widget icon, required String title, required GestureTapCallback onTap}) {
    return ListTile(
      leading: Container(width: 20, alignment: Alignment.center, child: icon),
      title: Text(title),
      onTap: onTap,
    );
  }

  Widget _importingWidget(BuildContext context) {
    return Container(
      height: 184, // Match list height: 16 (padding top + bottom) + 56 (item height) * 3 items
      width: double.infinity,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Text(L10n(context).attachmentPreparing),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 16),
            child: SizedBox(
              width: 120,
              height: 2,
              child: LinearProgressIndicator(value: null),
            ),
          ),
        ],
      ),
    );
  }

  _performImport(Future<File?> Function() import) async {
    setState(() => _importing = true);
    var file = await import();
    if (file != null) {
      Navigator.of(context).pop(AttachmentHandler(file));
    } else {
      setState(() => _importing = false);
    }
  }
}
