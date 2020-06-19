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
import 'package:flutter_student_embed/models/enrollment.dart';
import 'package:test/test.dart';

void main() {
  final _enrollment = Enrollment((b) => b..enrollmentState = 'active');

  group('hasActiveGradingPeriod', () {
    test('returns false if multipleGradingPeriodsEnabled is false', () {
      final enrollment = _enrollment.rebuild((b) => b..multipleGradingPeriodsEnabled = false);
      expect(enrollment.hasActiveGradingPeriod(), false);
    });

    test('returns false if currentGradingPeriodId is null', () {
      final enrollment = _enrollment.rebuild((b) => b
        ..multipleGradingPeriodsEnabled = true
        ..currentGradingPeriodId = null);
      expect(enrollment.hasActiveGradingPeriod(), false);
    });

    test('returns false if currentGradingPeriodId is empty', () {
      final enrollment = _enrollment.rebuild((b) => b
        ..multipleGradingPeriodsEnabled = true
        ..currentGradingPeriodId = '');
      expect(enrollment.hasActiveGradingPeriod(), false);
    });

    test('returns false if currentGradingPeriodId is 0', () {
      final enrollment = _enrollment.rebuild((b) => b
        ..multipleGradingPeriodsEnabled = true
        ..currentGradingPeriodId = '0');
      expect(enrollment.hasActiveGradingPeriod(), false);
    });

    test('returns true if multipleGradingPeriodsEnabled and currentGradingPeriodId is set', () {
      final enrollment = _enrollment.rebuild((b) => b
        ..multipleGradingPeriodsEnabled = true
        ..currentGradingPeriodId = '123');
      expect(enrollment.hasActiveGradingPeriod(), true);
    });
  });

  group('isTotalsForAllGradingPeriodsEnabled', () {
    test('returns false if enrollment is not student or observer', () {
      final enrollment = _enrollment.rebuild((b) => b);
      expect(enrollment.isTotalsForAllGradingPeriodsEnabled(), false);
    });

    test('returns false if enrollment has multipleGradingPeriodsEnabled false', () {
      final enrollment = _enrollment.rebuild((b) => b
        ..role = 'student'
        ..multipleGradingPeriodsEnabled = false);
      expect(enrollment.isTotalsForAllGradingPeriodsEnabled(), false);
    });

    test('returns false if enrollment has totalsForAllGradingPeriodsOption false', () {
      final enrollment = _enrollment.rebuild((b) => b
        ..role = 'student'
        ..multipleGradingPeriodsEnabled = true
        ..totalsForAllGradingPeriodsOption = false);
      expect(enrollment.isTotalsForAllGradingPeriodsEnabled(), false);
    });

    test('returns false if student enrollment has multipleGradingPeriodsEnabled and totalsForAllGradingPeriodsOption',
        () {
      final enrollment = _enrollment.rebuild((b) => b
        ..role = 'student'
        ..multipleGradingPeriodsEnabled = true
        ..totalsForAllGradingPeriodsOption = true);
      expect(enrollment.isTotalsForAllGradingPeriodsEnabled(), true);
    });

    test('returns false if observer enrollment has multipleGradingPeriodsEnabled and totalsForAllGradingPeriodsOption',
        () {
      final enrollment = _enrollment.rebuild((b) => b
        ..role = 'observer'
        ..multipleGradingPeriodsEnabled = true
        ..totalsForAllGradingPeriodsOption = true);
      expect(enrollment.isTotalsForAllGradingPeriodsEnabled(), true);
    });
  });

  group('isRole helpers', () {
    _testIsRoles('ta', 'TaEnrollment', 'isTa', (it) => it.isTa());
    _testIsRoles('student', 'StudentEnrollment', 'isStudent', (it) => it.isStudent());
    _testIsRoles('teacher', 'TeacherEnrollment', 'isTeacher', (it) => it.isTeacher());
    _testIsRoles('observer', 'ObserverEnrollment', 'isObserver', (it) => it.isObserver());
    _testIsRoles('designer', 'DesignerEnrollment', 'isDesigner', (it) => it.isDesigner());
  });
}

void _testIsRoles(String roleName1, String roleName2, String methodName, bool Function(Enrollment) method) {
  group(methodName, () {
    _testIsRole(roleName1, methodName, method);
    _testIsRole(roleName2, methodName, method);
  });
}

void _testIsRole(String roleName, String methodName, bool Function(Enrollment) method) {
  test('$methodName returns true when type is "$roleName"', () {
    Enrollment enrollment = Enrollment((b) => b..type = roleName);
    expect(method(enrollment), isTrue);
  });

  test('$methodName returns true when role is "$roleName"', () {
    Enrollment enrollment = Enrollment((b) => b..role = roleName);
    expect(method(enrollment), isTrue);
  });

  test('$methodName returns false when neither type nor role is "$roleName"', () {
    Enrollment enrollment = Enrollment((b) => b
      ..role = 'test_value'
      ..type = 'test_value');
    expect(method(enrollment), isFalse);
  });
}
