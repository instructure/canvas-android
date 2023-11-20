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

import 'package:path_provider/path_provider.dart' as PathProvider;

/// Wrapper around path_provider for ease of mocking in tests
class PathProviderVeneer {
  Future<Directory> getTemporaryDirectory() => PathProvider.getTemporaryDirectory();

  Future<Directory> getApplicationSupportDirectory() => PathProvider.getApplicationSupportDirectory();

  Future<Directory> getLibraryDirectory() => PathProvider.getLibraryDirectory();

  Future<Directory> getApplicationDocumentsDirectory() => PathProvider.getApplicationDocumentsDirectory();

  Future<Directory?> getExternalStorageDirectory() => PathProvider.getExternalStorageDirectory();

  Future<List<Directory>?> getExternalCacheDirectories() => PathProvider.getExternalCacheDirectories();

  Future<List<Directory>?> getExternalStorageDirectories({PathProvider.StorageDirectory? type}) {
    return PathProvider.getExternalStorageDirectories(type: type);
  }
}
