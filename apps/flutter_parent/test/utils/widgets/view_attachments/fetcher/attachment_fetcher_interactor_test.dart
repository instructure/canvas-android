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

import 'package:built_value/json_object.dart';
import 'package:dio/dio.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/network/api/file_api.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/fetcher/attachment_fetcher_interactor.dart';
import 'package:flutter_parent/utils/veneers/path_provider_veneer.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../../test_app.dart';
import '../../../test_helpers/mock_helpers.mocks.dart';

void main() {
  setUp(() {
    // Move to a temp dir so we don't write test files to the project dir
    Directory.current = Directory.systemTemp;
    Directory('cache').createSync();
  });

  tearDown(() {
    // Delete cache dir and contents
    Directory('cache').deleteSync(recursive: true);
  });

  test('getAttachmentSavePath returns correct value for valid file name', () async {
    await _setupLocator();
    var attachment = _makeAttachment();

    var expected = 'cache/attachment-123-fake-file.txt';
    var actual = await AttachmentFetcherInteractor().getAttachmentSavePath(attachment);

    expect(actual, expected);
  });

  test('getAttachmentSavePath returns correct value for invalid file name but valid url', () async {
    _setupLocator();
    var attachment = _makeAttachment().rebuild((a) => a..filename = '');

    var expected = 'cache/attachment-123-fake-file.txt';
    var actual = await AttachmentFetcherInteractor().getAttachmentSavePath(attachment);

    expect(actual, expected);
  });

  test('getAttachmentSavePath returns correct value for invalid file name and url', () async {
    _setupLocator();
    var attachment = _makeAttachment().rebuild((a) => a
      ..filename = ''
      ..url = 'https://fake.url.com/');

    var expected = 'cache/attachment-123-file';
    var actual = await AttachmentFetcherInteractor().getAttachmentSavePath(attachment);

    expect(actual, expected);
  });

  test('fetchAttachmentFile calls fileApi with correct parameters', () async {
    var attachment = _makeAttachment();
    var fileApi = MockFileApi();

    _setupLocator((locator) {
      locator.registerLazySingleton<FileApi>(() => fileApi);
    });

    CancelToken cancelToken = CancelToken();

    await AttachmentFetcherInteractor().fetchAttachmentFile(attachment, cancelToken);

    verify(
      fileApi.downloadFile(
        attachment.url!,
        'cache/attachment-123-fake-file.txt',
        cancelToken: cancelToken,
      ),
    ).called(1);
  });

  test('fetchAttachmentFile does not call FileApi if cached file exists', () async {
    var savePath = 'cache/attachment-123-fake-file.txt';

    // Crate 'cached' file
    var cachedFile = await File(savePath).create(recursive: true);
    await cachedFile.writeAsString('This is a test');

    var attachment = _makeAttachment().rebuild((b) => b..size = 14);
    var fileApi = MockFileApi();

    _setupLocator((locator) {
      locator.registerLazySingleton<FileApi>(() => fileApi);
    });

    var file = await AttachmentFetcherInteractor().fetchAttachmentFile(attachment, CancelToken());

    // File should have the expected path
    expect(file.path, savePath);

    // Should have returned cached file without performing download
    verifyNever(fileApi.downloadFile(any, any, cancelToken: anyNamed('cancelToken')));
  });

  test('fetchAttachmentFile calls FileApi if cached file exists but size does not match', () async {
    var savePath = 'cache/attachment-123-fake-file.txt';

    // Crate 'cached' file
    var cachedFile = await File(savePath).create(recursive: true);
    await cachedFile.writeAsString('This is a test but the file size does not match');

    var attachment = _makeAttachment().rebuild((b) => b..size = 14);
    var fileApi = MockFileApi();

    _setupLocator((locator) {
      locator.registerLazySingleton<FileApi>(() => fileApi);
    });

    CancelToken cancelToken = CancelToken();

    await AttachmentFetcherInteractor().fetchAttachmentFile(attachment, cancelToken);

    verify(
      fileApi.downloadFile(
        attachment.url,
        'cache/attachment-123-fake-file.txt',
        cancelToken: cancelToken,
      ),
    ).called(1);
  });
}

_setupLocator([config(GetIt locator)? = null]) async {
  var pathProvider = MockPathProviderVeneer();
  await setupTestLocator((locator) {
    locator.registerLazySingleton<PathProviderVeneer>(() => pathProvider);
    if (config != null) config(locator);
  });
  when(pathProvider.getTemporaryDirectory()).thenAnswer((_) => Future.value(Directory('cache')));
}

Attachment _makeAttachment() {
  return Attachment((a) => a
    ..jsonId = JsonObject('123')
    ..displayName = 'Display Name'
    ..filename = 'fake-file.txt'
    ..size = 14 // File size for text file with the contents 'This is a test'
    ..url = 'https://fake.url.com/fake-file.txt');
}