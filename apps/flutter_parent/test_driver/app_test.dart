// Imports the Flutter Driver API.
import 'dart:convert';

import 'package:flutter_driver/flutter_driver.dart';
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:test/test.dart';

void main() {
  group('Login Landing Screen', () {
    // Set up our finders
    final findSchoolFinder = find.text('Find School');
    final themeViewerFinder = find.text('Theme Viewer');
    final domainSearchScreenFinder = find.byType('DomainSearchScreen');
    final findSchoolTextFieldFinder = find.byValueKey("FindSchoolTextField");
    final themeViewerScreenFinder = find.byType('ThemeViewerScreen');

    FlutterDriver driver;

    // Connect to the Flutter driver before running any tests.
    setUpAll(() async {
      driver = await FlutterDriver.connect();
    });

    // Close the connection to the driver after the tests have completed.
    tearDownAll(() async {
      if (driver != null) {
        driver.close();
      }
    });

//    test('shows find school button', () async {
//      await driver.waitFor(findSchoolFinder);
//    });
//
//    test('shows theme viewer button', () async {
//      await driver.waitFor(themeViewerFinder);
//    });
//
//    test('opens domain search screen', () async {
//      await driver.tap(findSchoolFinder);
//      await driver.waitFor(domainSearchScreenFinder);
//      await driver.tap(find.byTooltip('Back'));
//    });
//
//    test('opens theme viewer screen', () async {
//      await driver.tap(themeViewerFinder);
//      await driver.waitFor(themeViewerScreenFinder);
//      // I have not found a way to exit the theme viewer via the driver,
//      // nor a way to press the system back-button.  So make sure that this
//      // is the last test in the group!
//    });

    test('Seed user', () async {
      var user = await _seedUser(driver);
      print("New User Name: ${user.name}");
      print("New User: $user}");

      await driver.waitFor(findSchoolFinder);
      await driver.tap(findSchoolFinder);
      await driver.waitFor(domainSearchScreenFinder);
      await driver.tap(findSchoolTextFieldFinder);
      await driver.enterText(user.domain);
      await driver.tap(find.text("Next"));


    });
  });


}

Future<SeededUser> _seedUser(FlutterDriver driver) async {
  var newUserJson = await driver.requestData("SeedUser");
  var userJsonMap = json.decode(newUserJson);
  var user = deserialize<SeededUser>(userJsonMap);
  return user;
}
