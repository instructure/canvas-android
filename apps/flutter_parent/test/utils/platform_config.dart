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

import 'dart:core';

class PlatformConfig {
  final bool initPackageInfo;
  final bool initPrefs;
  final bool initWebview;

  final Map<String, dynamic> mockPrefs;

  final _testPrefix = 'flutter.';

  const PlatformConfig({
    this.initPackageInfo = true,
    this.initPrefs = true,
    this.initWebview = false,
    this.mockPrefs = const {},
  });

  /// SharedPreferences requires that test configurations use 'flutter.' at the beginning of keys in the map
  Map<String, dynamic> get safeMockPrefs => mockPrefs.map((k, v) => MapEntry(_testKey(k), v));

  String _testKey(String key) {
    return key.startsWith(_testPrefix) ? key : '$_testPrefix$key';
  }
}
