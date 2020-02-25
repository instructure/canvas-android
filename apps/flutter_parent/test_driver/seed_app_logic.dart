
import 'dart:convert';

import 'package:built_collection/built_collection.dart';
import 'package:flutter_driver/driver_extension.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/dataseeding/seed_context.dart';
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:flutter_parent/models/serializers.dart';

class SeedResults {
  List<SeededUser> students;
  List<SeededUser> teachers;
  List<SeededUser> parents;
  List<Course> courses;
}

// Some app-side helper logic to abstract the "GetSeedContext" app call logic and
// mark data seeding as complete.
class SeedAppLogic {

  static SeedContext _seedContext = SeedContext( (b) => b
      ..seedingComplete = false
      ..build()
  );

  static DataHandler seedContextListener = (String message) async {
    if(message == "GetSeedContext") {
      return json.encode(serialize(_seedContext));
    }
  };

  static void markSeedingComplete(MapBuilder<String,String> seedObjects) {
    _seedContext = SeedContext( (b) => b
        ..seedingComplete = true
        ..seedObjects = seedObjects
        ..build()
    );
  }

  static SeedResults seed({int nStudents = 1, int nTeachers = 1, int nParents = 1, int nCourses = 1}) {
    SeedResults result = SeedResults();
    return result; // TODO
  }
}