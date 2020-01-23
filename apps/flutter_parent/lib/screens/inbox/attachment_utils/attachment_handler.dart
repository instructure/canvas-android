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

import 'package:flutter/foundation.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/network/api/file_api.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:path/path.dart';

enum AttachmentUploadStage { CREATED, UPLOADING, FAILED, FINISHED }

class AttachmentHandler with ChangeNotifier {
  AttachmentHandler(this._file);

  final File _file;
  Function(AttachmentUploadStage) onStageChange;
  double progress = null;
  Attachment attachment;
  AttachmentUploadStage _stage = AttachmentUploadStage.CREATED;

  AttachmentUploadStage get stage => _stage;

  set stage(AttachmentUploadStage stage) {
    _stage = stage;
    if (onStageChange != null) onStageChange(_stage);
  }

  String get displayName => attachment?.displayName ?? attachment?.filename ?? basename(_file?.path ?? '');

  Future<void> performUpload() async {
    // Do nothing if the upload is finished or in progress
    if (stage == AttachmentUploadStage.UPLOADING || stage == AttachmentUploadStage.FINISHED) return;

    // Move to Uploading stage
    stage = AttachmentUploadStage.UPLOADING;
    notifyListeners();

    try {
      // Upload the file and monitor progress
      attachment = await locator<FileApi>().uploadConversationFile(_file, (current, total) {
        progress = total == -1 ? null : current.toDouble() / total;
        notifyListeners();
      });

      // Set progress to null (i.e. indeterminate)
      progress = null;
      notifyListeners();

      // Give the server a short time to generate the thumbnail
      await Future.delayed(Duration(milliseconds: 500), () {
        stage = AttachmentUploadStage.FINISHED;
        notifyListeners();
      });
    } catch (e) {
      stage = AttachmentUploadStage.FAILED;
      notifyListeners();
    }
  }
}
