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
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:test/test.dart';

class EventDetailsPage {
  static Future<void> verifyEventDisplayed(FlutterDriver? driver, ScheduleItem event) async {
    var titleText = await driver?.getText(find.byValueKey('event_details_title'));
    expect(titleText, event.title, reason: 'Event title');

    if (event.locationName != null) {
      var locationText = await driver?.getText(find.byValueKey('event_details_location_line1'));
      expect(locationText, event.locationName, reason: 'event location name');
    }

    if (event.locationAddress != null) {
      var locationAddressText = await driver?.getText(find.byValueKey('event_details_location_line2'));
      expect(locationAddressText, event.locationAddress, reason: 'event location address');
    }
  }
}
