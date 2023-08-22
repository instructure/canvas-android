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

import 'package:android_intent_plus/android_intent.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/view_attachment_interactor.dart';
import 'package:flutter_parent/utils/permission_handler.dart';
import 'package:flutter_parent/utils/veneers/android_intent_veneer.dart';
import 'package:flutter_parent/utils/veneers/flutter_downloader_veneer.dart';
import 'package:flutter_parent/utils/veneers/path_provider_veneer.dart';
import 'package:mockito/mockito.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:test/test.dart';

import '../../test_app.dart';
import '../../test_helpers/mock_helpers.mocks.dart';

void main() {
  test('openExternally calls AndroidIntent with correct parameters', () async {
    var intentVeneer = MockAndroidIntentVeneer();
    await setupTestLocator((locator) {
      locator.registerLazySingleton<AndroidIntentVeneer>(() => intentVeneer);
    });

    Attachment attachment = Attachment((a) => a
      ..url = 'fake_url'
      ..contentType = 'fake/type');

    await ViewAttachmentInteractor().openExternally(attachment);

    AndroidIntent intent = verify(intentVeneer.launch(captureAny)).captured[0];
    expect(intent.action, 'action_view');
    expect(intent.data, attachment.url);
    expect(intent.type, attachment.inferContentType());
  });

  test('checkStoragePermission returns true when permission is already granted', () async {
    var permissionHandler = MockPermissionHandler();
    await setupTestLocator((locator) {
      locator.registerLazySingleton<PermissionHandler>(() => permissionHandler);
    });

    when(permissionHandler.checkPermissionStatus(Permission.storage))
        .thenAnswer((_) => Future.value(PermissionStatus.granted));

    var isGranted = await ViewAttachmentInteractor().checkStoragePermission();
    // requestPermissions should not have been called
    verifyNever(permissionHandler.requestPermission(any));

    // Should return true
    expect(isGranted, isTrue);
  });

  test('checkStoragePermission returns false when permission is rejected', () async {
    var permissionHandler = MockPermissionHandler();
    await setupTestLocator((locator) {
      locator.registerLazySingleton<PermissionHandler>(() => permissionHandler);
    });

    when(permissionHandler.checkPermissionStatus(Permission.storage))
        .thenAnswer((_) => Future.value(PermissionStatus.denied));

    when(permissionHandler.requestPermission(Permission.storage))
        .thenAnswer((_) => Future.value(PermissionStatus.denied));

    var isGranted = await ViewAttachmentInteractor().checkStoragePermission();

    // requestPermissions should have been called once
    verify(permissionHandler.requestPermission(Permission.storage)).called(1);

    // Should return true
    expect(isGranted, isFalse);
  });

  test('checkStoragePermission returns true when permission is request and granted', () async {
    var permissionHandler = MockPermissionHandler();
    await setupTestLocator((locator) {
      locator.registerLazySingleton<PermissionHandler>(() => permissionHandler);
    });

    when(permissionHandler.checkPermissionStatus(Permission.storage))
        .thenAnswer((_) => Future.value(PermissionStatus.denied));

    when(permissionHandler.requestPermission(Permission.storage))
        .thenAnswer((_) => Future.value(PermissionStatus.granted));

    var isGranted = await ViewAttachmentInteractor().checkStoragePermission();

    // requestPermissions should have been called once
    verify(permissionHandler.requestPermission(Permission.storage)).called(1);

    // Should return true
    expect(isGranted, isTrue);
  });

  test('downloadFile does nothing when permission is not granted', () async {
    var permissionHandler = MockPermissionHandler();
    var pathProvider = MockPathProviderVeneer();
    var downloader = MockFlutterDownloaderVeneer();
    await setupTestLocator((locator) {
      locator.registerLazySingleton<PermissionHandler>(() => permissionHandler);
      locator.registerLazySingleton<PathProviderVeneer>(() => pathProvider);
      locator.registerLazySingleton<FlutterDownloaderVeneer>(() => downloader);
    });

    when(permissionHandler.checkPermissionStatus(Permission.storage))
        .thenAnswer((_) => Future.value(PermissionStatus.denied));
    when(permissionHandler.requestPermission(Permission.storage))
        .thenAnswer((_) => Future.value(PermissionStatus.denied));

    ViewAttachmentInteractor().downloadFile(Attachment());

    verifyNever(pathProvider.getExternalStorageDirectories(type: anyNamed('type')));
    verifyNever(
      downloader.enqueue(
        url: anyNamed('url'),
        savedDir: anyNamed('savedDir'),
        showNotification: anyNamed('showNotification'),
        openFileFromNotification: anyNamed('openFileFromNotification'),
      ),
    );
  });

  test('downloadFile calls PathProvider and FlutterDownloader with correct parameters', () async {
    var permissionHandler = MockPermissionHandler();
    var pathProvider = MockPathProviderVeneer();
    var downloader = MockFlutterDownloaderVeneer();
    await setupTestLocator((locator) {
      locator.registerLazySingleton<PermissionHandler>(() => permissionHandler);
      locator.registerLazySingleton<PathProviderVeneer>(() => pathProvider);
      locator.registerLazySingleton<FlutterDownloaderVeneer>(() => downloader);
    });

    Attachment attachment = Attachment((a) => a..url = 'fake_url');

    when(permissionHandler.checkPermissionStatus(Permission.storage))
        .thenAnswer((_) => Future.value(PermissionStatus.granted));

    when(pathProvider.getExternalStorageDirectories(type: StorageDirectory.downloads))
        .thenAnswer((_) => Future.value([Directory('downloads')]));

    await ViewAttachmentInteractor().downloadFile(attachment);

    verify(pathProvider.getExternalStorageDirectories(type: StorageDirectory.downloads)).called(1);

    verify(
      downloader.enqueue(
        url: attachment.url,
        savedDir: 'downloads',
        showNotification: true,
        openFileFromNotification: true,
      ),
    );
  });
}
