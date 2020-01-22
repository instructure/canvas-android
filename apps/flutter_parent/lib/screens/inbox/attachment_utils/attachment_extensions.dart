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

import 'package:flutter/widgets.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';

extension AttachmentIcons on Attachment {
  IconData getIcon() {
    String type = inferContentType();

    if (type.startsWith('video')) {
      return CanvasIcons.video;
    } else if (type.startsWith('audio')) {
      return CanvasIcons.audio;
    } else if (type.startsWith('image')) {
      return CanvasIcons.image;
    } else if (type == 'application/pdf') {
      return CanvasIcons.pdf;
    } else if (type.contains('powerpoint') || type.contains('presentation')) {
      return CanvasIcons.ms_ppt;
    } else if (type.contains('excel') || type.contains('spreadsheet') || type == 'text/csv') {
      return CanvasIcons.ms_excel;
    } else if (type.contains('word') || type.contains('opendocument.text') || type == 'application/rtf') {
      return CanvasIcons.ms_word;
    } else if (type.contains('zip') ||
        type.contains('archive') ||
        type.contains('compressed') ||
        type.contains('x-tar')) {
      return CanvasIcons.zipped;
    } else {
      return CanvasIcons.document;
    }
  }
}
