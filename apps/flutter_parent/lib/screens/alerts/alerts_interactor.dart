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
import 'package:flutter_parent/network/api/alert_api.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/utils/alert_helper.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class AlertsInteractor {
  Future<AlertsList?> getAlertsForStudent(String studentId, bool forceRefresh) async {
    final alertsFuture = _alertsApi().getAlertsDepaginated(studentId, forceRefresh)?.then((List<Alert>? list) async {
      return locator<AlertsHelper>().filterAlerts(list);
    }).then((list) => list
      ?..sort((a, b) {
        if (a.actionDate == null && b.actionDate == null) return 0;
        if (a.actionDate == null && b.actionDate != null) return -1;
        if (a.actionDate != null && b.actionDate == null) return 1;
        return b.actionDate!.compareTo(a.actionDate!);
      }));

    final thresholdsFuture = _alertsApi().getAlertThresholds(studentId, forceRefresh);

    // If forcing a refresh, also update the alert count
    if (forceRefresh) locator<AlertCountNotifier>().update(studentId);
    return AlertsList(await alertsFuture, await thresholdsFuture);
  }

  Future<Alert?> markAlertRead(String studentId, String alertId) {
    return _alertsApi().updateAlertWorkflow(studentId, alertId, AlertWorkflowState.read.name);
  }

  Future markAlertDismissed(String studentId, String alertId) {
    return _alertsApi().updateAlertWorkflow(studentId, alertId, AlertWorkflowState.dismissed.name);
  }

  AlertsApi _alertsApi() => locator<AlertsApi>();
}

class AlertsList {
  final List<Alert>? alerts;
  final List<AlertThreshold>? thresholds;

  AlertsList(this.alerts, this.thresholds);
}
