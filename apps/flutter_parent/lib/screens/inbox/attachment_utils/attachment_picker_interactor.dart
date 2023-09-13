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

import 'package:file_picker/file_picker.dart';
import 'package:image_picker/image_picker.dart';

/// Note: Currently excluded from code coverage. That may need to change if this file is updated with testable code.
class AttachmentPickerInteractor {
  final ImagePicker _imagePicker = ImagePicker();

  Future<File?> getImageFromCamera() {
    return _imagePicker
        .pickImage(source: ImageSource.camera)
        .then((value) => File(value!.path));
  }

  Future<File?> getFileFromDevice() {
    final result = FilePicker.platform.pickFiles();

    return result.then((value) => File(value!.files.single.path!)).onError((error, stackTrace) => Future.error(""));
  }

  Future<File?> getImageFromGallery() {
    return _imagePicker
        .pickImage(source: ImageSource.gallery)
        .then((value) => File(value!.path));
  }
}
