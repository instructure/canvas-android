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

import 'dart:async';
import 'dart:io';

import 'package:built_value/json_object.dart';
import 'package:dio/dio.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/network/api/file_api.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_handler.dart';
import 'package:flutter_parent/utils/veneers/path_provider_veneer.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../../utils/test_app.dart';
import '../../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  setUpAll(() async {
    // Move to a temp dir so we don't write test files to the project dir
    Directory.current = Directory.systemTemp;
    await Directory('cache').create();
  });

  tearDownAll(() async {
    // Delete cache dir and contents
    await Directory('cache').delete(recursive: true);
  });

  test('Is constructed in "created" stage with indeterminate progress', () {
    var handler = AttachmentHandler(File(''));
    expect(handler.stage, equals(AttachmentUploadStage.CREATED));
    expect(handler.progress, isNull);
  });

  test('Calls onStageChange when stage changes', () {
    var handler = AttachmentHandler(File(''));

    AttachmentUploadStage? lastStage = null;
    handler.onStageChange = (stage) => lastStage = stage;

    handler.stage = AttachmentUploadStage.CREATED;
    expect(lastStage, equals(AttachmentUploadStage.CREATED));

    handler.stage = AttachmentUploadStage.UPLOADING;
    expect(lastStage, equals(AttachmentUploadStage.UPLOADING));

    handler.stage = AttachmentUploadStage.FAILED;
    expect(lastStage, equals(AttachmentUploadStage.FAILED));

    handler.stage = AttachmentUploadStage.FINISHED;
    expect(lastStage, equals(AttachmentUploadStage.FINISHED));
  });

  test('Notifies listeners during upload', () async {
    final api = MockFileApi();
    final pathProvider = MockPathProviderVeneer();

    await setupTestLocator((locator) {
      locator.registerLazySingleton<FileApi>(() => api);
      locator.registerLazySingleton<PathProviderVeneer>(() => pathProvider);
    });

    when(pathProvider.getTemporaryDirectory()).thenAnswer((_) async => Directory('other'));
    when(pathProvider.getApplicationSupportDirectory()).thenAnswer((_) async => Directory('other'));
    when(pathProvider.getExternalStorageDirectory()).thenAnswer((_) async => Directory('other'));

    final completer = Completer<Attachment>();

    when(api.uploadConversationFile(any, any)).thenAnswer((_) => completer.future);

    // Create file
    var file = File('cache/test-file.txt');
    file.writeAsStringSync('This is a test!');

    var handler = AttachmentHandler(file);

    // Expect updates for:
    //  1 - Change to 'uploading' stage
    //  3 - Mock API updates progress with null (i.e. indeterminate)
    //  2 - Mock API updates progress with value
    //  4 - Upload completes internally, progress updated to indeterminate
    //  5 - Change to 'finished' stage after short delay to allow thumbnail generation on the server
    final expectedCount = 5;

    var actualCount = 0;
    handler.addListener(() {
      actualCount++;
    });

    var uploadFuture = handler.performUpload();

    expect(handler.stage, equals(AttachmentUploadStage.UPLOADING));

    ProgressCallback callback = verify(api.uploadConversationFile(any, captureAny)).captured.single;

    callback(0, -1);
    expect(handler.progress, isNull);

    callback(5, 10);
    expect(handler.progress, equals(0.5));

    completer.complete(Attachment());

    await uploadFuture;
    expect(handler.stage, equals(AttachmentUploadStage.FINISHED));
    expect(actualCount, equals(expectedCount));
  });

  test('Sets failed state when API fails', () async {
    final api = MockFileApi();
    await setupTestLocator((locator) => locator.registerLazySingleton<FileApi>(() => api));

    when(api.uploadConversationFile(any, any)).thenAnswer((_) => Future.error('Error!'));

    var handler = AttachmentHandler(File(''));
    await handler.performUpload();
    expect(handler.stage, equals(AttachmentUploadStage.FAILED));
  });

  test('performUpload does nothing if stage is uploading or finished', () async {
    final api = MockFileApi();
    await setupTestLocator((locator) => locator.registerLazySingleton<FileApi>(() => api));

    var handler = AttachmentHandler(File(''))
      ..stage = AttachmentUploadStage.UPLOADING
      ..progress = 0.25;

    handler.performUpload();

    // Stage and progress should remain unchanged
    expect(handler.stage, equals(AttachmentUploadStage.UPLOADING));
    expect(handler.progress, equals(0.25));

    handler.stage = AttachmentUploadStage.FINISHED;
    handler.progress = 1.0;

    handler.performUpload();

    expect(handler.stage, equals(AttachmentUploadStage.FINISHED));
    expect(handler.progress, equals(1.0));

    // Ensure the API was not called
    verifyNever(api.uploadConversationFile(any, any));
  });

  test('displayName returns attachment display name', () {
    var attachment = Attachment((b) => b
      ..displayName = 'Attachment display name'
      ..filename = 'file.txt');
    var handler = AttachmentHandler(File('/path/to/file.txt'))..attachment = attachment;
    expect(handler.displayName, attachment.displayName);
  });

  test('displayName falls back to attachment file name when attachment display name is null', () {
    var attachment = Attachment((b) => b
      ..displayName = null
      ..filename = 'file.txt');
    var handler = AttachmentHandler(File('/path/to/file.txt'))..attachment = attachment;
    expect(handler.displayName, attachment.filename);
  });

  test('displayName falls back to file name when attachment is null', () {
    var handler = AttachmentHandler(File('/path/to/file.txt'))..attachment = null;
    expect(handler.displayName, 'file.txt');
  });

  test('displayName falls back to empty string when attachment is null and file is null', () {
    var handler = AttachmentHandler(null)..attachment = null;
    expect(handler.displayName, '');
  });

  test('cleans up file if local', () async {
    final pathProvider = MockPathProviderVeneer();

    await setupTestLocator((locator) {
      locator.registerLazySingleton<PathProviderVeneer>(() => pathProvider);
    });

    when(pathProvider.getTemporaryDirectory()).thenAnswer((_) async => Directory('cache'));
    when(pathProvider.getApplicationSupportDirectory()).thenAnswer((_) async => Directory('cache'));
    when(pathProvider.getExternalStorageDirectory()).thenAnswer((_) async => Directory('cache'));

    // Create file
    var file = File('cache/test-file.txt');
    file.writeAsStringSync('This is a test!');

    expect(file.existsSync(), isTrue);

    var handler = AttachmentHandler(file);
    await handler.cleanUpFile();

    expect(file.existsSync(), isFalse);
  });

  test('does not clean up file if not local', () async {
    final pathProvider = MockPathProviderVeneer();

    await setupTestLocator((locator) {
      locator.registerLazySingleton<PathProviderVeneer>(() => pathProvider);
    });

    when(pathProvider.getTemporaryDirectory()).thenAnswer((_) async => Directory('other'));
    when(pathProvider.getApplicationSupportDirectory()).thenAnswer((_) async => Directory('other'));
    when(pathProvider.getExternalStorageDirectory()).thenAnswer((_) async => Directory('other'));

    // Create file
    var file = File('cache/test-file.txt');
    file.writeAsStringSync('This is a test!');

    expect(file.existsSync(), isTrue);

    var handler = AttachmentHandler(file);
    await handler.cleanUpFile();

    expect(file.existsSync(), isTrue);
  });

  test('cleanUpFile prints error on failure', interceptPrint((log) async {
    final pathProvider = MockPathProviderVeneer();

    await setupTestLocator((locator) {
      locator.registerLazySingleton<PathProviderVeneer>(() => pathProvider);
    });

    when(pathProvider.getTemporaryDirectory()).thenAnswer((_) => Future.error(Error()));

    var handler = AttachmentHandler(null);
    await handler.cleanUpFile();

    expect(log.length, greaterThan(0));
    expect(log.first, 'Unable to clean up attachment source file');
  }));

  test('deleteAttachment calls API if attachment exists', () async {
    final api = MockFileApi();
    await setupTestLocator((locator) => locator.registerLazySingleton<FileApi>(() => api));
    when(api.deleteFile(any)).thenAnswer((_) async {});

    var handler = AttachmentHandler(null)..attachment = Attachment((a) => a..jsonId = JsonObject('attachment_123'));
    await handler.deleteAttachment();

    verify(api.deleteFile('attachment_123'));
  });

  test('deleteAttachment does not call API if attachment is null', () async {
    final api = MockFileApi();
    await setupTestLocator((locator) => locator.registerLazySingleton<FileApi>(() => api));

    var handler = AttachmentHandler(null);
    await handler.deleteAttachment();

    verifyNever(api.deleteFile(any));
  });

  test('deleteAttachment prints error on failure', interceptPrint((log) async {
    final api = MockFileApi();
    await setupTestLocator((locator) => locator.registerLazySingleton<FileApi>(() => api));
    when(api.deleteFile(any)).thenAnswer((_) => Future.error(Error()));

    var handler = AttachmentHandler(null)..attachment = Attachment((a) => a..jsonId = JsonObject('attachment_123'));
    await handler.deleteAttachment();

    expect(log.length, greaterThan(0));
    expect(log.first, 'Unable to delete attachment');
  }));
}

interceptPrint(testBody(List<String> log)) => () {
      final List<String> log = [];
      final spec = ZoneSpecification(print: (self, parent, zone, String msg) => log.add(msg));
      return Zone.current.fork(specification: spec).run(() => testBody(log));
    };
