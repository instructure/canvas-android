// Imports the Flutter Driver API.

import 'package:flutter_driver/flutter_driver.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:test/test.dart';

import 'seed_driver_logic.dart';

void main() {
  group('Dashboard Screen', () {
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

    test('One student, two courses', () async {

      // Wait for seeding to complete
      var seedContext = await SeedDriverLogic.waitForSeedingToComplete(driver);

      print("driver: Seeding complete!");
      var student = seedContext.getNamedObject<SeededUser>("student");
      print("driver: Student = $student");
      var course1 = seedContext.getNamedObject<Course>("course1");
      var course2 = seedContext.getNamedObject<Course>("course2");
      print("driver: Course1 = $course1");

      await Future.delayed(const Duration(seconds: 2));
      await driver.scroll(find.text(course1.name), 0, 150, Duration(milliseconds: 200)); // Refresh

      await driver.waitFor(find.byType("DashboardScreen"));
      await driver.waitFor(find.text(student.shortName));
      await driver.waitFor(find.text(course1.name));
      await driver.waitFor(find.text(course2.name));

    });
  });


}


