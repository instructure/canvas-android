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

import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class PlannerApi {
  Future<List<PlannerItem>?> getUserPlannerItems(
    String userId,
    DateTime startDay,
    DateTime endDay, {
    Set<String> contexts = const {},
    bool forceRefresh = false,
  }) async {
    var dio = canvasDio(forceRefresh: forceRefresh, pageSize: PageSize.canvasMax);
    var queryParams = {
      'start_date': startDay.toUtc().toIso8601String(),
      'end_date': endDay.toUtc().toIso8601String(),
      'context_codes[]': contexts.toList()..sort(), // Sort for cache consistency
    };
    return fetchList(dio.get('users/$userId/planner/items', queryParameters: queryParams), depaginateWith: dio);
  }
}
