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
import 'package:flutter_parent/utils/common_widgets/view_attachment/fetcher/attachment_fetcher.dart';

class TextAttachmentViewer extends StatefulWidget {
  final Attachment attachment;

  const TextAttachmentViewer(this.attachment, {super.key});

  @override
  _TextAttachmentViewerState createState() => _TextAttachmentViewerState();
}

class _TextAttachmentViewerState extends State<TextAttachmentViewer> {
  static const minTextSize = 10.0;
  static const maxTextSize = 48.0;

  var _textSize = 14.0;
  var _referenceTextSize;

  @override
  Widget build(BuildContext context) {
    return AttachmentFetcher(
      attachment: widget.attachment,
      builder: (context, file) {
        return LayoutBuilder(
          builder: (context, size) => GestureDetector(
            onScaleStart: (_) => _referenceTextSize = _textSize,
            onScaleUpdate: (scaleDetails) {
              var newSize = _referenceTextSize * scaleDetails.scale;
              if (newSize < minTextSize) newSize = minTextSize;
              if (newSize > maxTextSize) newSize = maxTextSize;
              setState(() {
                _textSize = newSize;
              });
            },
            child: Container(
              width: size.maxWidth,
              height: size.maxHeight,
              child: SingleChildScrollView(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Text(
                    file.readAsStringSync(),
                    style: TextStyle(fontSize: _textSize),
                  ),
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}
