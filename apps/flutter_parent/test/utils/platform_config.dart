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

import 'package:firebase_remote_config/firebase_remote_config.dart';
import 'package:flutter_parent/models/login.dart';

class PlatformConfig {
  final bool initDeviceInfo;
  final Login? initLoggedInUser;
  final bool initPackageInfo;
  final bool initPathProvider;
  final FirebaseRemoteConfig? initRemoteConfig;
  final bool initWebview;

  final Map<String, dynamic>? _mockApiPrefs;

  final Map<String, dynamic>? _mockPrefs;

  static const _testPrefix = 'flutter.';

  /// A helper class to setup initial configuration of platform channels
  ///
  /// [initDeviceInfo] Initializes the device info plugin with mock data
  /// [initLoggedInUser] Sets a user as the current login in ApiPrefs
  /// [initPackageInfo] Initializes the package info plugin with mock data
  /// [initRemoteConfig] Sets initial data for [RemoteConfigUtils]
  /// [initWebview] Initializes web views to be used in widgets during tests
  /// [mockApiPrefs] A map of initial ApiPrefs to mock. If null is set, then the EncryptedSharedPreferences platform channel won't be initialized (or reset)
  /// [mockPrefs] A map of initial prefs to mock for ThemePrefs and RemoteConfigUtils. If null is set, then the SharedPreferences platform channel won't be initialized (or reset)
  const PlatformConfig({
    this.initDeviceInfo = true,
    this.initLoggedInUser = null,
    this.initPackageInfo = true,
    this.initPathProvider = true,
    this.initRemoteConfig = null,
    this.initWebview = false,

    Map<String, dynamic>? mockApiPrefs = const {},
    Map<String, dynamic>? mockPrefs = null,
  })  : this._mockApiPrefs = mockApiPrefs,
        this._mockPrefs = mockPrefs;

  /// SharedPreferences requires that test configurations use 'flutter.' at the beginning of keys in the map
  Map<String, Object>? get mockApiPrefs => _safeMap(_mockApiPrefs);

  Map<String, Object>? get mockPrefs => _safeMap(_mockPrefs);

  Map<String, Object>? _safeMap(Map<String, dynamic>? map) => map?.map((k, v) => MapEntry(_testKey(k), v));

  String _testKey(String key) {
    return key.startsWith(_testPrefix) ? key : '$_testPrefix$key';
  }
}
