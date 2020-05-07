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

import 'package:flutter_parent/models/schedule_item.dart';
import 'package:test/test.dart';

void main() {
  final contextId = '1234';
  final contextType = 'course';
  final scheduleItem = ScheduleItem((b) => b
    ..id = ''
    ..type = ScheduleItem.apiTypeCalendar
    ..isAllDay = false
    ..isHidden = false
    ..effectiveContextCode = '${contextType}_$contextId');

  group('context helpers', () {
    test('returns a valid id for getContextId', () {
      final result = scheduleItem.getContextId();

      expect(result, contextId);
    });

    test('returns a valid context type for getContextType', () {
      final result = scheduleItem.getContextType();

      expect(result, contextType);
    });

    test('returns a valid id for getContextId when effectiveContextCode is null', () {
      final newContextId = 'hodor';
      final item = scheduleItem.rebuild((b) => b
        ..contextCode = '${contextType}_$newContextId'
        ..effectiveContextCode = null);

      final result = item.getContextId();

      expect(result, newContextId);
    });

    test('returns a valid context type for getContextType when effectiveContextCode is null', () {
      final newContextType = 'group';
      final item = scheduleItem.rebuild((b) => b
        ..contextCode = '${newContextType}_$contextId'
        ..effectiveContextCode = null);

      final result = item.getContextType();

      expect(result, newContextType);
    });
  });

  group('Item type helpers', () {
    test('type of ScheduleItem.apiTypeCalendar results in event type', () {});

    test('assignment.isQuiz results in quiz type', () {});

    test('assignment.isDiscussion results in discussion type', () {});

    test('else, results in an assignment type', () {});

    test('type of ScheduleItem.apiTypeCalendar results in event type string', () {});

    test('assignment.isQuiz results in quiz type string', () {});

    test('assignment.isDiscussion results in discussion type string', () {});

    test('else, results in an assignment type string', () {});
  });

  group('Planner Item Conversion', () {
    test('PlannerSubmission returns null for null assignment', () {});

    test('PlannerSubmission returns null for null submission', () {});

    test('returns valid PlannerSubmission for valid submission', () {});

    test('returns valid PlannerItem for valid scheduleItem', () {});

    test('returns valid PlannerItem for valid scheduleItem with null assignment', () {});
  });
}
