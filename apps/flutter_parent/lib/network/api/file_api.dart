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

import 'package:dio/dio.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/models/file_upload_config.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';
import 'package:mime/mime.dart';
import 'package:path/path.dart';

class FileApi {
  /// Uploads the given [file] to the user's 'conversation attachment's directory. The [progressCallback] will be
  /// called intermittently with two values - a 'count' of the currently uploaded bytes and a 'total' byte count. A
  /// 'total' value of -1 is considered to represent indeterminate progress, and means either the file size is unknown
  /// or the upload is at a stage where progress cannot be determined. In either case, user-facing progress indicators
  /// should be aware of this and show 'indeterminate' progress as needed.
  Future<Attachment?> uploadConversationFile(File file, ProgressCallback progressCallback) async {
    progressCallback(0, -1); // Indeterminate
    final name = basename(file.path);
    final size = await file.length();
    final contentType = lookupMimeType(file.path);

    var dio = canvasDio();
    final params = ({
      'name': name,
      'size': size,
      'content_type': contentType,
      'parent_folder_path': 'conversation attachments',
      'on_duplicate': 'rename'
    });

    // Get the upload configuration
    FileUploadConfig? uploadConfig;
    try {
      uploadConfig = await fetch(dio.post('users/self/files', queryParameters: params));
    } catch (e) {
      print(e);
      return Future.error(e);
    }

    // Build the form data for upload
    FormData formData = FormData.fromMap(uploadConfig?.params?.toMap() ?? {});
    formData.files.add(MapEntry('file', await MultipartFile.fromFile(file.path, filename: name)));

    // Perform upload with progress
    return fetch(Dio().post(
      uploadConfig?.url ?? '',
      data: formData,
      onSendProgress: (count, total) {
        if (total > 0 && count >= total) {
          // After 100% it still takes a bit for the request to complete, so we'll push 'indeterminate' progress
          progressCallback(0, -1);
        } else {
          progressCallback(count, total);
        }
      },
    ));
  }

  /// Downloads a file located at [url] to the specified [savePath]
  Future<File> downloadFile(
    String url,
    String savePath, {
    CancelToken? cancelToken,
    ProgressCallback? onProgress,
  }) async {
    var dio = DioConfig.core(forceRefresh: true).dio;
    await dio.download(
          url,
          savePath,
          cancelToken: cancelToken,
          onReceiveProgress: onProgress,
        );
    return File(savePath);
  }

  Future<void> deleteFile(String fileId) async {
    var dio = canvasDio();
    await dio.delete('files/$fileId');
  }
}
