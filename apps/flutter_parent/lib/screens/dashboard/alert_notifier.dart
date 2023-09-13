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

import 'package:flutter/material.dart';
import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/network/api/alert_api.dart';
import 'package:flutter_parent/utils/alert_helper.dart';
import 'package:flutter_parent/utils/core_extensions/list_extensions.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class AlertCountNotifier extends ValueNotifier<int> {
  AlertCountNotifier() : super(0);

  update(String studentId) async {
    try {
      final unreadAlerts = await locator<AlertsApi>().getAlertsDepaginated(studentId, true).then((List<Alert>? list) async {
        return await locator<AlertsHelper>().filterAlerts(list?.where((element) => element.workflowState == AlertWorkflowState.unread).toList());
      });
      if (unreadAlerts != null) value = unreadAlerts.length;
    } catch (e) {
      print(e);
    }
  }
}
