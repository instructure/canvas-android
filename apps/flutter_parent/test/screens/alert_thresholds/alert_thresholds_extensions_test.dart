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

import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/alert_threshold.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_extensions.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('List<AlertThreshold> - Get Threshold From Type', () {
    test('non-existing threshold returns null', () {
      List<AlertThreshold> thresholds = [];
      AlertType type = AlertType.courseGradeLow;

      expect(thresholds.getThreshold(type), null);
    });

    test('existing threshold returns threshold', () {
      AlertThreshold threshold = _mockThreshold(AlertType.assignmentMissing);
      List<AlertThreshold> thresholds = [
        _mockThreshold(AlertType.courseGradeLow, value: '23'),
        threshold,
        _mockThreshold(AlertType.courseAnnouncement)
      ];
      AlertType type = AlertType.assignmentMissing;

      expect(thresholds.getThreshold(type), threshold);
    });
  });

  group('AlertType - Get Threshold Min/Max', () {
    test('Course grade low: null/course grade high', () {
      AlertType testingType = AlertType.courseGradeLow;
      String highValue = '42';
      List<String?> expectedResult = [null, highValue];
      AlertThreshold courseGradeLow = _mockThreshold(testingType, value: '24');
      AlertThreshold courseGradeHigh = _mockThreshold(AlertType.courseGradeHigh, value: highValue);
      List<AlertThreshold> thresholds = [courseGradeLow, courseGradeHigh];

      expect(testingType.getMinMax(thresholds), expectedResult);
    });

    test('Course grade high: course grade low/null', () {
      AlertType testingType = AlertType.courseGradeHigh;
      String lowValue = '24';
      List<String?> expectedResult = [lowValue, null];
      AlertThreshold courseGradeLow = _mockThreshold(AlertType.courseGradeLow, value: lowValue);
      AlertThreshold courseGradeHigh = _mockThreshold(testingType, value: '42');
      List<AlertThreshold> thresholds = [courseGradeLow, courseGradeHigh];

      expect(testingType.getMinMax(thresholds), expectedResult);
    });

    test('Assignment grade low: null/assignment grade high', () {
      AlertType testingType = AlertType.assignmentGradeLow;
      String highValue = '42';
      List<String?> expectedResult = [null, highValue];
      AlertThreshold assignmentGradeLow = _mockThreshold(testingType, value: '24');
      AlertThreshold assignmentGradeHigh = _mockThreshold(AlertType.assignmentGradeHigh, value: highValue);
      List<AlertThreshold> thresholds = [assignmentGradeLow, assignmentGradeHigh];

      expect(testingType.getMinMax(thresholds), expectedResult);
    });

    test('Assignment grade high: assignment grade low/null', () {
      AlertType testingType = AlertType.assignmentGradeHigh;
      String lowValue = '24';
      List<String?> expectedResult = [lowValue, null];
      AlertThreshold assignmentGradeLow = _mockThreshold(AlertType.assignmentGradeLow, value: lowValue);
      AlertThreshold assignmentGradeHigh = _mockThreshold(testingType, value: '42');
      List<AlertThreshold> thresholds = [assignmentGradeLow, assignmentGradeHigh];

      expect(testingType.getMinMax(thresholds), expectedResult);
    });
  });
}

AlertThreshold _mockThreshold(AlertType? type, {String? value}) => AlertThreshold((b) => b
  ..alertType = type ?? AlertType.courseGradeLow
  ..threshold = value ?? null
  ..build());
