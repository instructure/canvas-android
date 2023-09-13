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
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';

class ConversationCreatePage {
  static Future<void> verifyRecipientListed(FlutterDriver? driver, SeededUser user) async {
    var keyString = 'user_chip_${user.id}';
    await driver?.waitFor(find.descendant(of: find.byValueKey(keyString), matching: find.text(user.shortName)));
  }

  static Future<void> verifySubject(FlutterDriver? driver, String subject) async {
    //var text = await driver.getText(find.byValueKey('subjectText'));
    //expect(text, subject, reason: 'email subject text');

    // Unfortunately, the stronger check above won't work because getText() does not
    // work on a TextField.
    await driver?.waitFor(find.text(subject), timeout: const Duration(seconds: 5));
  }

  static Future<void> populateBody(FlutterDriver? driver, String body) async {
    await driver?.tap(find.byValueKey('messageText'));
    await driver?.enterText(body);
  }

  static Future<void> sendMail(FlutterDriver? driver) async {
    await Future.delayed(const Duration(seconds: 1)); // May need to wait a beat for the button to become enabled
    await driver?.tap(find.byValueKey('sendButton'));
  }
}
