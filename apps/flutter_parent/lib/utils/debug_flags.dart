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

import 'package:flutter/foundation.dart';

/// A collection of boolean flags that will control when debug features are used
///
/// To edit this file and have git ignore your local changes, run this command:
/// git update-index --skip-worktree apps/flutter_parent/lib/utils/debug_flags.dart
///
/// To start tracking local changes again, run this command:
/// git update-index --no-skip-worktree apps/flutter_parent/lib/utils/debug_flags.dart
class DebugFlags {
  static bool isDebug = kDebugMode; // Defaults to kDebugMode, can set to true to have debug features in release builds
  static bool isDebugApi = false;
}
