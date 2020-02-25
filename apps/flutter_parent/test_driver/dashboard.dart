import 'dart:convert';
import 'dart:io';

import 'package:built_collection/built_collection.dart';
import 'package:flutter_driver/driver_extension.dart';
import 'package:flutter_parent/main.dart' as app;
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/api/dataseeding/course_seed_api.dart';
import 'package:flutter_parent/network/api/dataseeding/enrollment_seed_api.dart';
import 'package:flutter_parent/network/api/dataseeding/user_seed_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';

import 'seed_app_logic.dart';


void main() async {
  enableFlutterDriverExtension(handler: SeedAppLogic.seedContextListener);
  
  // Initialize our ApiPrefs
  await ApiPrefs.init();

  // Perform any necessary data seeding
  var student = await UserSeedApi.createUser();
  var parent = await UserSeedApi.createUser();
  var parentLogin = parent.toLogin();
  await ApiPrefs.addLogin(parentLogin);
  await ApiPrefs.switchLogins(parentLogin);
  var course1 = await CourseSeedApi.createCourse();
  var studentEnrollment1 = await EnrollmentSeedApi.createEnrollment(student.id, course1.id, "StudentEnrollment", "");
  var parentEnrollment1 = await EnrollmentSeedApi.createEnrollment(parent.id, course1.id, "ObserverEnrollment", student.id);
  var course2 = await CourseSeedApi.createCourse();
  var studentEnrollment2 = await EnrollmentSeedApi.createEnrollment(student.id, course2.id, "StudentEnrollment", "");
  var parentEnrollment2 = await EnrollmentSeedApi.createEnrollment(parent.id, course2.id, "ObserverEnrollment", student.id);

  SeedAppLogic.markSeedingComplete( MapBuilder({
    "parent" : json.encode(serialize(parent)),
    "student" : json.encode(serialize(student)),
    "course1" : json.encode(serialize(course1)),
    "course2" : json.encode(serialize(course2))
  }));
  
  // Call app.main(), which should being up the dashboard.
  app.main();
}
