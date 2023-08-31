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

import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class CalendarSeedApi {
  static Future<ScheduleItem?> createCalendarEvent(String courseId, String title, DateTime startAt,
      {String description = "",
      DateTime? endAt = null,
      bool allDay = false,
      String locationName = "",
      String locationAddress = ""}) async {
    var queryParams = {
      'calendar_event[context_code]': 'course_$courseId',
      'calendar_event[title]': title,
      'calendar_event[start_at]':
          allDay ? DateTime(startAt.year, startAt.month, startAt.day).toIso8601String() : startAt.toIso8601String(),
      'calendar_event[end_at]': endAt == null ? null : endAt.toIso8601String(),
      'calendar_event[description]': description,
      'calendar_event[all_day]': allDay,
      'calendar_event[location_name]': locationName,
      'calendar_event[location_address]': locationAddress,
    };

    var dio = seedingDio();

    return fetch(dio.post('calendar_events', queryParameters: queryParams));
  }
}
