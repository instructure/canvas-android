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

import 'package:dio/dio.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/network/api/file_upload_api.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_handler.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../../utils/test_app.dart';

void main() {
  test('Is constructed in "created" stage with indeterminate progress', () {
    var handler = AttachmentHandler(File(''));
    expect(handler.stage, equals(AttachmentUploadStage.CREATED));
    expect(handler.progress, isNull);
  });

  test('Calls onStageChange when stage changes', () {
    var handler = AttachmentHandler(File(''));

    AttachmentUploadStage lastStage = null;
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
    final api = _MockFileUploadApi();
    setupTestLocator((locator) => locator.registerLazySingleton<FileUploadApi>(() => api));

    final completer = Completer<Attachment>();

    when(api.uploadConversationFile(any, any)).thenAnswer((_) => completer.future);

    var handler = AttachmentHandler(File(''));

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
    final api = _MockFileUploadApi();
    setupTestLocator((locator) => locator.registerLazySingleton<FileUploadApi>(() => api));

    when(api.uploadConversationFile(any, any)).thenAnswer((_) => Future.error("Error!"));

    var handler = AttachmentHandler(File(''));
    await handler.performUpload();
    expect(handler.stage, equals(AttachmentUploadStage.FAILED));
  });

  test('performUpload does nothing if stage is uploading or finished', () {
    final api = _MockFileUploadApi();
    setupTestLocator((locator) => locator.registerLazySingleton<FileUploadApi>(() => api));

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
}

class _MockFileUploadApi extends Mock implements FileUploadApi {}
