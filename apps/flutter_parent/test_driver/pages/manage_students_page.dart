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

class ManageStudentsPage {
  static Future<void> addStudent(FlutterDriver? driver, String pairingCode) async {
    await driver?.tap(find.byType("FloatingActionButton"));
    await driver?.tap(find.text("Pairing Code")); // Choose between pairing code and qr-code
    await driver?.tap(find.byType("TextFormField"));
    await driver?.enterText(pairingCode);
    await driver?.tap(find.text("OK"));
  }

  static Future<void> verifyStudentDisplayed(FlutterDriver? driver, SeededUser user) async {
    await driver?.waitFor(
        find.descendant(of: find.byValueKey('studentTextHero${user.id}'), matching: find.text(user.shortName)));
  }
}
