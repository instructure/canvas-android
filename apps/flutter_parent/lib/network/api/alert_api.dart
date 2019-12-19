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
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class AlertsApi {
  /// Alerts were depaginated in the original parent app, then sorted by date. Depaginating here to follow suite.
  Future<List<Alert>> getAlertsDepaginated(String studentId) async {
    var dio = canvasDio();
    return fetchList(dio.get('users/self/observer_alerts/$studentId'), depaginateWith: dio);
  }

  Future<Alert> updateAlertWorkflow(String alertId, String workflowState) async {
    return fetch(canvasDio().put('users/self/observer_alerts/$alertId/$workflowState'));
  }
}
