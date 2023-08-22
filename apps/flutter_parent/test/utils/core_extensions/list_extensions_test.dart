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

import 'package:flutter_parent/utils/core_extensions/list_extensions.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('sortBy', () {
    test('sortBy returns null if list is null', () {
      final List<int>? unsorted = null;
      final List<int?>? actual = unsorted.sortBySelector([(it) => it]);
      expect(actual, isNull);
    });

    test('sortBy correctly sorts in ascending order', () {
      final List<int> unsorted = [3, 4, 2, 5, 1];
      final List<int> expected = [1, 2, 3, 4, 5];
      final List<int?>? actual = unsorted.sortBySelector([(it) => it]);
      expect(actual, expected);
    });

    test('sortBy correctly sorts in descending order', () {
      final List<int> unsorted = [3, 4, 2, 5, 1];
      final List<int> expected = [5, 4, 3, 2, 1];
      final List<int?>? actual = unsorted.sortBySelector([(it) => it], descending: true);
      expect(actual, expected);
    });

    test('sortBy throws ArgumentError for NullSortOrder.none when comparing null and non-null values', () {
      final unsorted = [
        _TestClass(number: 1),
        _TestClass(number: 2),
        _TestClass(number: null),
      ];
      expect(
        () => unsorted.sortBySelector([(it) => it?.number], nullSortOrder: NullSortOrder.none),
        throwsArgumentError,
      );
    });

    test('sortBy does not throw ArgumentError for NullSortOrder.none when comparing all null values', () {
      final unsorted = [
        _TestClass(number: null),
        _TestClass(number: null),
        _TestClass(number: null),
      ];
      expect(
        unsorted.sortBySelector([(it) => it?.number], nullSortOrder: NullSortOrder.none),
        unsorted,
      );
    });

    test('sortBy correctly sorts with NullSortOrder.greaterThan', () {
      final unsorted = [
        _TestClass(number: null, text: '1'),
        _TestClass(number: 2, text: '2'),
        _TestClass(number: null, text: '3'),
      ];
      final expected = [unsorted[1], unsorted[0], unsorted[2]];
      final actual = unsorted.sortBySelector([(it) => it?.number, (it) => it?.text], nullSortOrder: NullSortOrder.greaterThan);
      expect(actual, expected);
    });

    test('sortBy correctly sorts with NullSortOrder.lessThan', () {
      final unsorted = [
        _TestClass(number: null, text: '1'),
        _TestClass(number: 2, text: '2'),
        _TestClass(number: null, text: '3'),
      ];
      final expected = [unsorted[0], unsorted[2], unsorted[1]];
      final actual = unsorted.sortBySelector([(it) => it?.number, (it) => it?.text], nullSortOrder: NullSortOrder.lessThan);
      expect(actual, expected);
    });

    test('sortBy correctly sorts with NullSortOrder.equal', () {
      final unsorted = [
        _TestClass(number: null, text: '1'),
        _TestClass(number: 2, text: '2'),
        _TestClass(number: null, text: '3'),
      ];
      final expected = [unsorted[0], unsorted[1], unsorted[2]];
      final actual = unsorted.sortBySelector([(it) => it?.number, (it) => it?.text], nullSortOrder: NullSortOrder.equal);
      expect(actual, expected);
    });

    test('sortBy performs a stable sort with null comparisons', () {
      final unsorted = [
        _TestClass(number: 1, text: '1'),
        _TestClass(number: null, text: '2'),
        _TestClass(number: 3, text: '3'),
        _TestClass(number: null, text: '4'),
        _TestClass(number: 5, text: '5'),
        _TestClass(number: null, text: '6'),
        _TestClass(number: 7, text: '7'),
      ];
      final expected = [unsorted[0], unsorted[2], unsorted[4], unsorted[6], unsorted[1], unsorted[3], unsorted[5]];
      final actual = unsorted.sortBySelector([(it) => it?.number]);
      expect(actual, expected);
    });

    test('sortBy falls back to subsequent selectors', () {
      DateTime now = DateTime.now();
      final unsorted = [
        _TestClass(date: now, number: 123, text: '4'),
        _TestClass(date: now, number: 123, text: '2'),
        _TestClass(date: now, number: 123, text: '3'),
        _TestClass(date: now, number: 123, text: '1'),
        _TestClass(date: now, number: 123, text: '0'),
        _TestClass(date: now, number: 123, text: '6'),
        _TestClass(date: now, number: 123, text: '5'),
      ];
      final expected = [unsorted[4], unsorted[3], unsorted[1], unsorted[2], unsorted[0], unsorted[6], unsorted[5]];
      final actual = unsorted.sortBySelector([(it) => it?.date, (it) => it?.number, (it) => it?.text]);
      expect(actual, expected);
    });
  });

  group('count', () {
    test('count returns 0 for empty list', () {
      List<_TestClass> list = [];
      expect(list.count((it) => it?.number != null ? false : it!.number! < 5), 0);
    });

    test('count returns 0 if predicate is always false', () {
      List<_TestClass> list = List<_TestClass>.generate(100, (index) => _TestClass(number: index));
      expect(list.count((_) => false), 0);
    });

    test('count returns 0 if list is null', () {
      List<_TestClass>? list = null;
      expect(list.count((_) => false), 0);
    });

    test('count returns correct count based on predicate', () {
      List<_TestClass> list = List<_TestClass>.generate(100, (index) => _TestClass(number: index));
      expect(list.count((it) => it?.number == null ? false : it!.number! < 50), 50);
    });

    test('count returns list size if predicate is always true', () {
      List<_TestClass> list = List<_TestClass>.generate(100, (index) => _TestClass(number: index));
      expect(list.count((_) => true), 100);
    });
  });

  group('mapIndexed', () {
    test('Maps items with index', () {
      List<String> original = ['', 'A', 'AB', 'ABC'];
      List<String> expected = ['0', '1', '2', '3'];

      List<String?>? actual = original.mapIndexed((index, item) {
        expect(item, original[index]);
        return item?.length.toString();
      });

      expect(actual, expected);
    });

    test('Returns null if list is null', () {
      List<String>? original = null;
      List<String?>? actual = original.mapIndexed((index, item) => '');
      expect(actual, isNull);
    });
  });
}

class _TestClass {
  final int? number;
  final String? text;
  final DateTime? date;

  _TestClass({this.number, this.text, this.date});

  @override
  String toString() => 'number: $number, text: $text, date: $date';
}
