// Copyright (C) 2019 - present Instructure, Inc.
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

import 'package:dio/dio.dart';
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/api/utils/paged_list.dart';
import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/serializers.dart';

class AlertsApi {
  /// Alerts were depaginated in the original parent app, then sorted by date. Depaginating here to follow suite.
  Future<List<Alert>> getAlertsDepaginated(int studentId) async {
    var response = await Dio().get(ApiPrefs.getApiUrl(path: 'users/self/observer_alerts/$studentId'),
        options: Options(headers: ApiPrefs.getHeaderMap()));

    if (response.statusCode == 200 || response.statusCode == 201) {
      final list = PagedList<Alert>(response);
      return (list.nextUrl == null) ? list.data : _getAlertsDepaginated(list);
    } else {
      return Future.error(response.statusMessage);
    }
  }

  Future<List<Alert>> _getAlertsDepaginated(PagedList<Alert> prevList) async {
    var response = await Dio().get(prevList.nextUrl, options: Options(headers: ApiPrefs.getHeaderMap()));

    if (response.statusCode == 200 || response.statusCode == 201) {
      prevList.updateWithResponse(response);
      return (prevList.nextUrl == null) ? prevList.data : _getAlertsDepaginated(prevList);
    } else {
      return Future.error(response.statusMessage);
    }
  }

  Future<Alert> updateAlertWorkflow(int alertId, String workflowState) async {
    var response = await Dio().put(
      ApiPrefs.getApiUrl(path: "users/self/observer_alerts/$alertId/$workflowState"),
      options: Options(headers: ApiPrefs.getHeaderMap()),
    );

    if (response.statusCode == 200 || response.statusCode == 201) {
      return deserialize<Alert>(response.data);
    } else {
      return Future.error(response.statusMessage);
    }
  }
}
