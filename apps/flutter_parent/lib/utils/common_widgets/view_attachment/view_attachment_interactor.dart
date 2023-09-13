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

import 'package:android_intent_plus/android_intent.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/utils/permission_handler.dart';
import 'package:flutter_parent/utils/veneers/android_intent_veneer.dart';
import 'package:flutter_parent/utils/veneers/flutter_downloader_veneer.dart';
import 'package:flutter_parent/utils/veneers/path_provider_veneer.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../service_locator.dart';

class ViewAttachmentInteractor {
  Future<void> openExternally(Attachment attachment) async {
    AndroidIntent intent = AndroidIntent(
      action: 'action_view',
      data: attachment.url,
      type: attachment.inferContentType(),
    );
    await locator<AndroidIntentVeneer>().launch(intent);
  }

  Future<void> downloadFile(Attachment attachment) async {
    var dirs = await locator<PathProviderVeneer>().getExternalStorageDirectories(type: StorageDirectory.downloads);
    locator<FlutterDownloaderVeneer>().enqueue(
      url: attachment.url!,
      savedDir: dirs![0].path,
      showNotification: true,
      openFileFromNotification: true,
    );
  }
}