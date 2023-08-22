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
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class AlertThresholdsInteractor {
  Future<List<AlertThreshold>?> getAlertThresholdsForStudent(String studentId, {bool forceRefresh = true}) async {
    return locator<AlertsApi>().getAlertThresholds(studentId, forceRefresh);
  }

  ///
  /// Switches are created when turned on
  /// Switches disable/delete when [threshold] is null
  ///
  /// Percentages disable/delete when [value] is '-1'
  /// [value] is only used when creating percentages
  ///
  ///
  Future<AlertThreshold?> updateAlertThreshold(AlertType type, String studentId, AlertThreshold? threshold,
      {String? value}) {
    var api = locator<AlertsApi>();
    if (type.isSwitch()) {
      if (threshold == null) {
        // Create threshold, effectively putting it into the 'on' state
        return api.createThreshold(type, studentId);
      } else {
        // Delete switch threshold, effectively putting it into the 'off' state
        return api.deleteAlert(threshold);
      }
    } else {
      // For the percentage thresholds
      if (value != '-1') {
        // Create/update the threshold
        return api.createThreshold(type, studentId, value: value);
      } else
        // Disable the threshold
        return api.deleteAlert(threshold!);
    }
  }

  Future<bool> deleteStudent(String studentId) => locator<EnrollmentsApi>().unpairStudent(studentId);

  Future<bool> canDeleteStudent(String studentId) => locator<EnrollmentsApi>().canUnpairStudent(studentId);
}
