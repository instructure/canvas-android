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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/alert_threshold.dart';

extension GetTitleFromAlert on AlertType {
  String getTitle(BuildContext context) {
    String title;
    switch (this) {
      case AlertType.courseGradeLow:
        title = L10n(context).courseGradeBelow;
        break;
      case AlertType.courseGradeHigh:
        title = L10n(context).courseGradeAbove;
        break;
      case AlertType.assignmentGradeLow:
        title = L10n(context).assignmentGradeBelow;
        break;
      case AlertType.assignmentGradeHigh:
        title = L10n(context).assignmentGradeAbove;
        break;
      case AlertType.assignmentMissing:
        title = L10n(context).assignmentMissing;
        break;
      case AlertType.courseAnnouncement:
        title = L10n(context).courseAnnouncements;
        break;
      case AlertType.institutionAnnouncement:
        title = L10n(context).globalAnnouncements;
        break;
      default:
        title = L10n(context).unexpectedError;
    }
    return title;
  }
}

extension GetThresholdFromType on List<AlertThreshold?>? {
  AlertThreshold? getThreshold(AlertType type) {
    var index = this?.indexWhere((threshold) => threshold?.alertType == type);
    if (index == null || index == -1)
      return null;
    else
      return this?[index];
  }
}

extension GetThresholdMinMax on AlertType {
  List<String?> getMinMax(List<AlertThreshold?>? thresholds) {
    String? max;
    String? min;

    if (this == AlertType.courseGradeLow) {
      max = thresholds.getThreshold(AlertType.courseGradeHigh)?.threshold;
    } else if (this == AlertType.courseGradeHigh) {
      min = thresholds.getThreshold(AlertType.courseGradeLow)?.threshold;
    } else if (this == AlertType.assignmentGradeLow) {
      max = thresholds.getThreshold(AlertType.assignmentGradeHigh)?.threshold;
    } else if (this == AlertType.assignmentGradeHigh) {
      min = thresholds.getThreshold(AlertType.assignmentGradeLow)?.threshold;
    }

    return [min, max];
  }
}
