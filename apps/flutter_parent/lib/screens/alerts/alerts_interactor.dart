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
import 'package:flutter_parent/network/api/alert_api.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class AlertsInteractor {
  Future<List<Alert>> getAlertsForStudent(String studentId) async {
    final data = await _alertsApi().getAlertsDepaginated(studentId);
    return data
      ..sort((a, b) {
        if (a.actionDate == null && b.actionDate == null) return 0;
        if (a.actionDate == null && b.actionDate != null) return -1;
        if (a.actionDate != null && b.actionDate == null) return 1;
        return b.actionDate.compareTo(a.actionDate);
      });
  }

  Future<Alert> markAlertRead(String alertId) {
    return _alertsApi().updateAlertWorkflow(alertId, AlertWorkflowState.read.name);
  }

  AlertsApi _alertsApi() => locator<AlertsApi>();
}
