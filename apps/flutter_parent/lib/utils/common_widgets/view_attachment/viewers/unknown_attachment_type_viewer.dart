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
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';

class UnknownAttachmentTypeViewer extends StatelessWidget {
  final Attachment attachment;

  const UnknownAttachmentTypeViewer(this.attachment, {super.key});

  @override
  Widget build(BuildContext context) {
    return EmptyPandaWidget(
      svgPath: 'assets/svg/panda-not-supported.svg',
      title: L10n(context).unsupportedFileTitle,
      subtitle: L10n(context).unsupportedFileMessage,
    );
  }
}
