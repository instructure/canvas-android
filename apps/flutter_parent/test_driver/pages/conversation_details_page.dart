import 'package:flutter_driver/flutter_driver.dart';
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:test/test.dart';

class ConversationDetailsPage {
  static Future<void> verifyRecipientListed(FlutterDriver? driver, int index, SeededUser user) async {
    var messageFinder = find.byValueKey('conversation_message_index_$index');
    var participantFinder = find.descendant(of: messageFinder, matching: find.byValueKey('participant_id_${user.id}'));
    var fullText = await driver?.getText(participantFinder);
    expect(fullText?.contains(user.shortName), true,
        reason: 'email detail user: searching for \"${user.shortName}\" in \"$fullText\"');
  }

  static Future<void> verifySubject(FlutterDriver? driver, List<String> partialSubjects) async {
    var fullText = await driver?.getText(find.byValueKey('subjectText'));
    for (String partialSubject in partialSubjects) {
      expect(fullText?.toLowerCase().contains(partialSubject.toLowerCase()), true,
          reason: 'email detail header subject: searching for \"$partialSubject\" in \"$fullText\"');
    }
  }

  static Future<void> verifyCourse(FlutterDriver? driver, String courseName) async {
    var fullText = await driver?.getText(find.byValueKey('courseText'));
    expect(fullText?.contains(courseName), true,
        reason: 'email detail header course: searching for \"$courseName\" in \"$fullText\"');
  }

  static Future<void> tapParticipants(FlutterDriver? driver) async {
    await driver?.tap(find.byValueKey('author-info'));
  }

  static Future<void> initiateEmailReplyAll(FlutterDriver? driver) async {
    await driver?.tap(find.byType('FloatingActionButton'));
    await driver?.tap(find.text('Reply All'));
  }
}
