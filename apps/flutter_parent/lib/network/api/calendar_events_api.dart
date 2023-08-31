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

class CalendarEventsApi {
  Future<List<ScheduleItem>?> getAllCalendarEvents({
    bool allEvents = false,
    String type = ScheduleItem.apiTypeCalendar,
    String? startDate = null,
    String? endDate = null,
    List<String> contexts = const [],
    bool forceRefresh = false,
  }) async {
    var dio = canvasDio(forceRefresh: forceRefresh, pageSize: PageSize.canvasMax);
    var params = {
      'all_events': allEvents,
      'type': type,
      'start_date': startDate,
      'end_date': endDate,
      'context_codes[]': contexts,
    };
    return fetchList(dio.get('calendar_events', queryParameters: params), depaginateWith: dio);
  }

  Future<ScheduleItem?> getEvent(String? eventId, bool forceRefresh) async {
    var dio = canvasDio(forceRefresh: forceRefresh);
    return fetch(dio.get('calendar_events/$eventId'));
  }

  Future<List<ScheduleItem>?> getUserCalendarItems(
    String userId,
    DateTime startDay,
    DateTime endDay,
    String type, {
    Set<String> contexts = const {},
    bool forceRefresh = false,
  }) async {
    var dio = canvasDio(forceRefresh: forceRefresh, pageSize: PageSize.canvasMax);

    return fetchList(
        dio.get('users/$userId/calendar_events',
            queryParameters: getQueryParams(startDay, endDay, type, contexts: contexts)),
        depaginateWith: dio);
  }

  Map<String, Object> getQueryParams(DateTime startDay, DateTime endDay, String type,
      {Set<String> contexts = const {}, bool includeSubmissions = false}) {
    return {
      'start_date': startDay.toUtc().toIso8601String(),
      'end_date': endDay.toUtc().toIso8601String(),
      'type': type,
      'context_codes[]': contexts.toList()..sort(), // Sort for cache consistency
      'include[]': ['submission'],
    };
  }
}
