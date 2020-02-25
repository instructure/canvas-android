

import 'dart:convert';

import 'package:flutter_driver/flutter_driver.dart';
import 'package:flutter_parent/models/dataseeding/seed_context.dart';
import 'package:flutter_parent/models/serializers.dart';

// Some driver-side abstractions for grabbing SeedContext from the app.
class SeedDriverLogic {
  // Driver side: Grab the current seed context (might be incomplete).
  static Future<SeedContext> _getSeedContext(FlutterDriver driver) async {
    var jsonContext = await driver.requestData("GetSeedContext");
    return deserialize<SeedContext>(json.decode(jsonContext));
  }

  // Driver side: Retrieve the SeedContext once seeding is complete
  static Future<SeedContext> waitForSeedingToComplete(FlutterDriver driver) async {
    var seedContext = await _getSeedContext(driver);
    while(!seedContext.seedingComplete) {
      await Future.delayed(const Duration(seconds: 1));
      seedContext = await(_getSeedContext(driver));
    }
    return seedContext;
  }
}