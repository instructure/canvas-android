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

import 'package:flutter_driver/flutter_driver.dart';
import 'package:flutter_parent/models/dataseeding/seed_context.dart';
import 'package:flutter_parent/models/serializers.dart';

// Some driver-side abstractions for grabbing SeedContext from the app.
class DriverSeedUtils {
  // Driver side: Grab the current seed context (might be incomplete).
  static Future<SeedContext> _getSeedContext(FlutterDriver? driver) async {
    var jsonContext = await driver?.requestData("GetSeedContext");
    return deserialize<SeedContext>(json.decode(jsonContext ?? ''))!;
  }

  // Driver side: Retrieve the SeedContext once seeding is complete
  static Future<SeedContext?> waitForSeedingToComplete(FlutterDriver? driver) async {
    var seedContext = await _getSeedContext(driver);
    while (seedContext.seedingComplete == false) {
      await Future.delayed(const Duration(seconds: 1));
      seedContext = await (_getSeedContext(driver));
    }

    // Throw in a delay to allow rendering to complete
    await Future.delayed(Duration(seconds: 2));
    return seedContext;
  }
}
