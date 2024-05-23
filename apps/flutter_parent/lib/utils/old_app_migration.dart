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

import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';

class OldAppMigration {
  static const channelName = 'com.instructure.parentapp/oldAppMigrations';
  static const methodGetLogins = 'getLogins';
  static const methodHasOldReminders = 'hasOldReminders';

  static const channel = const MethodChannel(channelName);

  Future<void> performMigrationIfNecessary() async {
    // Skip if we have already performed the migration
    if (ApiPrefs.getHasMigrated() == true) return;

    // Get the list of logins from the native side
    List<dynamic> data = await channel.invokeMethod(methodGetLogins);
    List<Login> logins = data.map((it) => deserialize<Login>(json.decode(it))).toList().nonNulls.toList();

    if (logins.isNotEmpty) {
      // Save the list of logins to prefs
      ApiPrefs.saveLogins(logins);

      // Set the first login as the current login
      ApiPrefs.switchLogins(logins[0]);
    }

    // Mark as migrated
    ApiPrefs.setHasMigrated(true);
  }

  Future<bool> hasOldReminders() async {
    // Skip if we have already performed the check
    if (ApiPrefs.getHasCheckedOldReminders() == true) return false;

    // Get result from native side
    var hasOldReminders = await channel.invokeMethod(methodHasOldReminders);

    // Mark as checked
    await ApiPrefs.setHasCheckedOldReminders(true);

    return hasOldReminders;
  }
}
