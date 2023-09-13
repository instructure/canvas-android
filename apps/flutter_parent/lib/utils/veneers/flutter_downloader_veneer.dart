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
import 'package:flutter_downloader/flutter_downloader.dart';

class FlutterDownloaderVeneer {
  Future<String?> enqueue({
    required String url,
    required String savedDir,
    String? fileName,
    bool showNotification = true,
    bool openFileFromNotification = true,
    bool requiresStorageNotLow = true,
    bool saveInPublicStorage = true}) =>
      FlutterDownloader.enqueue(
          url: url,
          savedDir: savedDir,
          fileName: fileName,
          showNotification: showNotification,
          openFileFromNotification: openFileFromNotification,
          requiresStorageNotLow: requiresStorageNotLow,
          saveInPublicStorage: saveInPublicStorage);

  Future<List<DownloadTask>?> loadTasks() => FlutterDownloader.loadTasks();

  static Future<List<DownloadTask>?> loadTasksWithRawQuery({required String query}) =>
      FlutterDownloader.loadTasksWithRawQuery(query: query);

  static Future<void> cancel({required String taskId}) => FlutterDownloader.cancel(taskId: taskId);

  static Future<void> cancelAll() => FlutterDownloader.cancelAll();

  static Future<void> pause({required String taskId}) => FlutterDownloader.pause(taskId: taskId);

  static Future<String?> resume({
    required String taskId,
    bool requiresStorageNotLow = true,
  }) =>
      FlutterDownloader.resume(taskId: taskId, requiresStorageNotLow: requiresStorageNotLow);

  static Future<String?> retry({
    required String taskId,
    bool requiresStorageNotLow = true,
  }) =>
      FlutterDownloader.retry(taskId: taskId, requiresStorageNotLow: requiresStorageNotLow);

  static Future<void> remove({required String taskId, bool shouldDeleteContent = false}) =>
      FlutterDownloader.remove(taskId: taskId, shouldDeleteContent: shouldDeleteContent);

  static Future<bool> open({required String taskId}) => FlutterDownloader.open(taskId: taskId);

  static registerCallback(DownloadCallback callback) => FlutterDownloader.registerCallback(callback);
}
