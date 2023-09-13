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

import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/network/api/file_api.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/veneers/path_provider_veneer.dart';

class AttachmentFetcherInteractor {
  Future<File> fetchAttachmentFile(Attachment attachment, CancelToken cancelToken) async {
    var savePath = await getAttachmentSavePath(attachment);

    // Check if file already exists
    var file = File(savePath);
    if (await file.exists() && await file.length() == attachment.size) return file;

    return locator<FileApi>().downloadFile(attachment.url!, savePath, cancelToken: cancelToken);
  }

  Future<String> getAttachmentSavePath(Attachment attachment) async {
    var fileName = attachment.filename;
    if (fileName == null || fileName.isEmpty) {
      var index = attachment.url!.lastIndexOf('/');
      if (index >= 0 && index < attachment.url!.length - 1) {
        fileName = attachment.url!.substring(attachment.url!.lastIndexOf('/') + 1);
      } else {
        fileName = 'file';
      }
    }
    fileName = 'attachment-${attachment.id}-$fileName';
    var cacheDir = await locator<PathProviderVeneer>().getTemporaryDirectory();
    return '${cacheDir.path}/$fileName';
  }

  CancelToken generateCancelToken() => CancelToken();
}
