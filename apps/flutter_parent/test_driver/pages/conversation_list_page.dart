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

import 'package:flutter_driver/flutter_driver.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:test/test.dart';

class ConversationListPage {
  /// Since subjects/messages/contexts can be pretty complex, allow for portions of those
  /// fields to be verified.
  static Future<void> verifyConversationDataDisplayed(FlutterDriver? driver, int index,
      {List<String>? partialSubjects = null,
      List<String>? partialBodies = null,
      List<String>? partialContexts = null}) async {
    // Validate any specified partial subjects
    if (partialSubjects != null) {
      var finder = find.byValueKey('conversation_subject_$index');
      await driver?.scrollIntoView(finder);
      var fullText = await driver?.getText(finder);
      for (String partialSubject in partialSubjects) {
        expect(fullText?.toLowerCase().contains(partialSubject.toLowerCase()), true,
            reason: "Message subject \"$partialSubject\" in \"$fullText\"");
      }
    }

    // Validate any specified partial contexts
    if (partialContexts != null) {
      var finder = find.byValueKey('conversation_context_$index');
      await driver?.scrollIntoView(finder);
      var fullText = await driver?.getText(finder);
      for (String partialContext in partialContexts) {
        expect(fullText?.toLowerCase().contains(partialContext.toLowerCase()), true,
            reason: "Message context \"$partialContext\" in \"$fullText\"");
      }
    }

    // Validate any specified partial messages bodies
    if (partialBodies != null) {
      var finder = find.byValueKey('conversation_message_$index');
      await driver?.scrollIntoView(finder);
      var fullText = await driver?.getText(finder);
      for (String partialMessage in partialBodies) {
        expect(fullText?.toLowerCase().contains(partialMessage.toLowerCase()), true,
            reason: "Message body \"$partialMessage\" in \"$fullText\"");
      }
    }
  }

  /// Gets you to the CreateConversationScreen
  static Future<void> initiateCreateEmail(FlutterDriver? driver, Course forCourse) async {
    await driver?.tap(find.byType('FloatingActionButton'));
    await driver?.tap(find.byValueKey('course_list_course_${forCourse.id}'));
    await Future.delayed(const Duration(seconds: 1)); // Allow time for population
  }

  static Future<void> selectMessage(FlutterDriver? driver, int index) async {
    var finder = find.byValueKey('conversation_subject_$index');
    await driver?.scrollIntoView(finder);
    await driver?.tap(finder);
  }
}
