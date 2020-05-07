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

import 'package:flutter_student_embed/models/login.dart';

class PlatformConfig {
  final bool initDeviceInfo;
  final Login initLoggedInUser;
  final bool initPackageInfo;

  /// A helper class to setup initial configuration of platform channels
  ///
  /// [initDeviceInfo] Initializes the device info plugin with mock data
  /// [initLoggedInUser] Sets a user as the current login in ApiPrefs
  /// [initPackageInfo] Initializes the package info plugin with mock data
  const PlatformConfig({
    this.initDeviceInfo = true,
    this.initLoggedInUser,
    this.initPackageInfo = true,
  });
}
