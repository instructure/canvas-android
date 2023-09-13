import 'dart:convert';

import 'package:built_collection/built_collection.dart';
import 'package:flutter_driver/driver_extension.dart';
import 'package:flutter_parent/main.dart' as app;
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';

import 'apis/assignment_seed_api.dart';
import 'apis/submission_seed_api.dart';
import 'app_seed_utils.dart';

void main() async {
  enableFlutterDriverExtension(handler: AppSeedUtils.seedContextListener);

  // Initialize our ApiPrefs
  await ApiPrefs.init();

  // Seed our data
  var data = await AppSeedUtils.seed(nStudents: 1, nCourses: 1);
  var course = data.courses[0];
  var student = data.students[0];
  // past-due
  var assignment1 = (await AssignmentSeedApi.createAssignment(course.id, dueAt: DateTime.now().subtract(Duration(days: 1)).toUtc()))!;
  // Unsubmitted
  var assignment2 = (await AssignmentSeedApi.createAssignment(course.id, dueAt: DateTime.now().add(Duration(days: 1)).toUtc()))!;
  // Submitted
  var assignment3 = (await AssignmentSeedApi.createAssignment(course.id, dueAt: DateTime.now().add(Duration(days: 1)).toUtc()))!;
  await Future.delayed(const Duration(seconds: 2)); // Allow some time for assignment-creation delayed jobs to complete
  var submission3 = (await SubmissionSeedApi.createSubmission(course.id, assignment3, student.id))!;

  // Graded
  var assignment4 = (await AssignmentSeedApi.createAssignment(course.id, dueAt: DateTime.now().add(Duration(days: 3)).toUtc()))!;
  await Future.delayed(const Duration(seconds: 2)); // Allow some time for assignment-creation delayed jobs to complete
  var submission4 = (await SubmissionSeedApi.createSubmission(course.id, assignment4, student.id))!;
  var grade4 = (await SubmissionSeedApi.gradeSubmission(course.id, assignment4, student.id, "19"))!;

  // Sign in the parent
  await AppSeedUtils.signIn(data.parents.first);

  // Let the test driver know that seeding has completed
  AppSeedUtils.markSeedingComplete(MapBuilder({
    "parent": json.encode(serialize(data.parents.first)),
    "student": json.encode(serialize(data.students[0])),
    "course": json.encode(serialize(data.courses[0])),
    "assignment1": json.encode(serialize(assignment1)),
    "assignment2": json.encode(serialize(assignment2)),
    "assignment3": json.encode(serialize(assignment3)),
    "assignment4": json.encode(serialize(assignment4)),
  }));

  // Call app.main(), which should bring up the dashboard.
  app.main();
}
