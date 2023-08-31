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

import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/alert_threshold.dart';
import 'package:flutter_parent/models/unread_count.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

const String _alertThresholdsEndpoint = 'users/self/observer_alert_thresholds';

class AlertsApi {
  /// Alerts were depaginated in the original parent app, then sorted by date. Depaginating here to follow suite.
  Future<List<Alert>?> getAlertsDepaginated(String studentId, bool forceRefresh) async {
    var dio = canvasDio(forceRefresh: forceRefresh);
    return fetchList(dio.get('users/self/observer_alerts/$studentId'), depaginateWith: dio);
  }

  Future<Alert?> updateAlertWorkflow(String studentId, String alertId, String workflowState) async {
    final config = DioConfig.canvas();
    // Read/dismissed data has changed and makes the cache stale
    config.clearCache(path: 'users/self/observer_alerts/$studentId');
    var dio = config.dio;
    return fetch(dio.put('users/self/observer_alerts/$alertId/$workflowState'));
  }

  // Always force a refresh when retrieving this data
  Future<UnreadCount?> getUnreadCount(String studentId) async {
    var dio = canvasDio(forceRefresh: true);
    return fetch(dio.get('users/self/observer_alerts/unread_count', queryParameters: {'student_id': studentId}));
  }

    Future<List<AlertThreshold>?> getAlertThresholds(String studentId, bool forceRefresh) async {
      var dio = canvasDio(forceRefresh: forceRefresh);
      return fetchList(dio.get(_alertThresholdsEndpoint, queryParameters: {'student_id': studentId}));
    }

    Future<AlertThreshold?> deleteAlert(AlertThreshold threshold) async {
      var dio = canvasDio();
      return fetch(dio.delete('$_alertThresholdsEndpoint/${threshold.id}'));
    }

    Future<AlertThreshold?> createThreshold(AlertType type, String studentId, {String? value}) async {
      var dio = canvasDio();
      return fetch(dio.post(_alertThresholdsEndpoint, data: {
        'observer_alert_threshold': {
          'alert_type': type.toApiString(),
          'user_id': studentId,
          if (value != null) 'threshold': value,
        }
      }));
    }
}
