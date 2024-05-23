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
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:test/test.dart';

void main() {
  IconData getIcon(String? contentType) => Attachment((a) => a..contentType = contentType).getIcon();

  test('returns video icon for video attachments', () {
    expect(getIcon('video/mp4'), CanvasIcons.video);
  });

  test('returns image icon for image attachments', () {
    expect(getIcon('image/png'), CanvasIcons.image);
  });

  test('returns pdf icon for PDF attachments', () {
    expect(getIcon('application/pdf'), CanvasIcons.pdf);
  });

  test('returns powerpoint icon for powerpoint attachments', () {
    expect(getIcon('application/vnd.ms-powerpoint'), CanvasIcons.ms_ppt);
    expect(getIcon('application/vnd.openxmlformats-officedocument.presentationml.presentation'), CanvasIcons.ms_ppt);
  });

  test('returns spreadsheet icon for spreadsheet attachments', () {
    expect(getIcon('text/csv'), CanvasIcons.ms_excel);
    expect(getIcon('application/vnd.ms-excel'), CanvasIcons.ms_excel);
    expect(getIcon('application/vnd.oasis.opendocument.spreadsheet'), CanvasIcons.ms_excel);
    expect(getIcon('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'), CanvasIcons.ms_excel);
  });

  test('returns word icon for word processor attachments', () {
    expect(getIcon('application/rtf'), CanvasIcons.ms_word);
    expect(getIcon('application/msword'), CanvasIcons.ms_word);
    expect(getIcon('application/x-abiword'), CanvasIcons.ms_word);
    expect(getIcon('application/vnd.oasis.opendocument.text'), CanvasIcons.ms_word);
    expect(getIcon('application/vnd.openxmlformats-officedocument.wordprocessingml.document'), CanvasIcons.ms_word);
  });

  test('returns zip icon for archive attachments', () {
    expect(getIcon('application/zip'), CanvasIcons.zipped);
    expect(getIcon('application/gzip'), CanvasIcons.zipped);
    expect(getIcon('application/x-tar'), CanvasIcons.zipped);
    expect(getIcon('application/x-bzip'), CanvasIcons.zipped);
    expect(getIcon('application/java-archive'), CanvasIcons.zipped);
    expect(getIcon('application/x-7z-compressed'), CanvasIcons.zipped);
    expect(getIcon('application/application/x-bzip2'), CanvasIcons.zipped);
    expect(getIcon('application/vnd.android.package-archive'), CanvasIcons.zipped);
    expect(getIcon('application/application/x-rar-compressed'), CanvasIcons.zipped);
  });

  test('returns document icon for other attachment types', () {
    expect(getIcon(''), CanvasIcons.document);
    expect(getIcon(null), CanvasIcons.document);
    expect(getIcon('null'), CanvasIcons.document);
    expect(getIcon('application/octet-stream'), CanvasIcons.document);
  });
}
