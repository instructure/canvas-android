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
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:test/test.dart';

class ConversationListPage {
  static Future<void> verifyConversationDisplayed(FlutterDriver driver, Conversation conversation, int index) async {
    var actualSubject = await driver.getText(find.byValueKey('conversation_subject_$index'));
    expect(actualSubject, conversation.subject, reason: "Conversation subject");
    var actualContext = await driver.getText(find.byValueKey('conversation_context_$index'));
    expect(actualContext, conversation.contextName, reason: "Conversation context");
    var message = await driver.getText(find.byValueKey('conversation_message_$index'));
    expect(message, conversation.lastMessage ?? conversation.lastAuthoredMessage, reason: "Conversation message");
  }

  static Future<void> verifyConversationDataDisplayed(FlutterDriver driver, int index,
      {List<String> partialSubjects: null,
      List<String> partialMessages: null,
      List<String> partialContexts: null}) async {
    if (partialSubjects != null) {
      var fullText = await driver.getText(find.byValueKey('conversation_subject_$index'));
      for (String partialSubject in partialSubjects) {
        expect(fullText.toLowerCase().contains(partialSubject.toLowerCase()), true,
            reason: "Message subject \"$partialSubject\" in \"$fullText\"");
      }
    }
    if (partialContexts != null) {
      var fullText = await driver.getText(find.byValueKey('conversation_context_$index'));
      for (String partialContext in partialContexts) {
        expect(fullText.toLowerCase().contains(partialContext.toLowerCase()), true,
            reason: "Message context \"$partialContext\" in \"$fullText\"");
      }
    }
    if (partialMessages != null) {
      var fullText = await driver.getText(find.byValueKey('conversation_message_$index'));
      for (String partialMessage in partialMessages) {
        expect(fullText.toLowerCase().contains(partialMessage.toLowerCase()), true,
            reason: "Message body \"$partialMessage\" in \"$fullText\"");
      }
    }
  }

  /// Gets you to the CreateConversationScreen
  static Future<void> initiateCreateEmail(FlutterDriver driver, Course forCourse) async {
    await driver.tap(find.byType('FloatingActionButton'));
    await driver.tap(find.byValueKey('course_list_course_${forCourse.id}'));
    await Future.delayed(const Duration(seconds: 1)); // Allow time for population
  }

  static Future<void> refresh(FlutterDriver driver) async {
    driver.scroll(find.byType("RefreshIndicator"), 0, 200, const Duration(milliseconds: 200));
  }
}
