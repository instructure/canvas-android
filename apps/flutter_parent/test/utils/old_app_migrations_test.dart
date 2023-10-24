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
import 'package:flutter_parent/utils/old_app_migration.dart';
import 'package:flutter_test/src/deprecated.dart';
import 'package:test/test.dart';

import 'canvas_model_utils.dart';
import 'test_app.dart';

void main() {
  setUp(() async {
    await setupPlatformChannels();
  });

  group('performMigrationIfNecessary', () {
    test('does nothing if already migrated', () async {
      var channelCalled = false;
      OldAppMigration.channel.setMockMethodCallHandler((MethodCall methodCall) async {
        channelCalled = true;
        return null;
      });

      await ApiPrefs.setHasMigrated(true);
      await OldAppMigration().performMigrationIfNecessary();

      expect(channelCalled, false);
    });

    test('calls channel and sets prefs during migration', () async {
      List<Login> logins = [
        Login((b) => b..user = CanvasModelTestUtils.mockUser(name: 'User 1').toBuilder()),
        Login((b) => b..user = CanvasModelTestUtils.mockUser(name: 'User 2').toBuilder()),
        Login((b) => b..user = CanvasModelTestUtils.mockUser(name: 'User 3').toBuilder()),
      ];

      List<String> serializedLogins = logins.map((it) => json.encode(serialize(it))).toList();

      String? calledMethod;
      OldAppMigration.channel.setMockMethodCallHandler((MethodCall methodCall) async {
        calledMethod = methodCall.method;
        return serializedLogins;
      });

      await ApiPrefs.setHasMigrated(false);
      await OldAppMigration().performMigrationIfNecessary();

      // Should have called correct method
      expect(calledMethod, OldAppMigration.methodGetLogins);

      // Should have set hasMigrated to true
      expect(ApiPrefs.getHasMigrated(), isTrue);

      // Should have saved all logins
      expect(ApiPrefs.getLogins(), logins);

      // Should have current login to first migrated login
      expect(ApiPrefs.getCurrentLogin(), logins[0]);
    });
  });

  group('hasOldReminders', () {
    test('returns false if already checked', () async {
      await ApiPrefs.setHasCheckedOldReminders(true);
      var hasReminders = await OldAppMigration().hasOldReminders();
      expect(hasReminders, false);
    });

    test('calls platform channel and sets checked value', () async {
      bool channelReturnValue = true;
      String? calledMethod;

      OldAppMigration.channel.setMockMethodCallHandler((MethodCall methodCall) async {
        calledMethod = methodCall.method;
        return channelReturnValue;
      });

      await ApiPrefs.setHasCheckedOldReminders(false);
      var hasReminders = await OldAppMigration().hasOldReminders();

      // Should have called correct channel method
      expect(calledMethod, OldAppMigration.methodHasOldReminders);

      // Should have returned value provided by channel
      expect(hasReminders, channelReturnValue);

      // Should have updated checked value in prefs
      expect(ApiPrefs.getHasCheckedOldReminders(), true);
    });
  });
}
