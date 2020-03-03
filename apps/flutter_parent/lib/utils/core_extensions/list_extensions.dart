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

/// Defines the sort order of null values when compared to non-null values
enum NullSortOrder {
  /// Null values are treated as being greater than the non-null values to which they are compared
  greaterThan,

  /// Null values are treated as being less than the non-null values to which they are compared
  lessThan,

  /// Null values are treated as being equal to the non-null values to which they are compared
  equal,

  /// Null values will not be handled specially. Throws an [ArgumentError] when comparing null values to non-null values.
  none,
}

typedef Comparable Selector<T>(T element);

extension ListUtils<T> on List<T> {
  /// Sorts elements in-place according to the natural sort order of the value returned by the specified [selectors].
  /// Subsequent selectors will only be used when elements returned by preceding selectors have the same sort order.
  List<T> sortBy(
    List<Selector<T>> selectors, {
    bool descending = false,
    NullSortOrder nullSortOrder = NullSortOrder.greaterThan,
  }) {
    if (this == null || this.isEmpty || selectors.isEmpty) return this;
    sort((a, b) {
      int result;
      for (int i = 0; i < selectors.length; i++) {
        var selector = selectors[i];

        var value1 = selector(descending ? b : a);
        var value2 = selector(descending ? a : b);

        if (value1 != null && value2 != null) {
          // Both values are non-null; perform a direct comparison.
          result = value1.compareTo((value2));
        } else if ((value1 == null) != (value2 == null)) {
          // Only one of the values is null
          switch (nullSortOrder) {
            case NullSortOrder.greaterThan:
              result = value1 == null ? 1 : -1;
              break;
            case NullSortOrder.lessThan:
              result = value1 == null ? -1 : 1;
              break;
            case NullSortOrder.equal:
              result = 0;
              break;
            case NullSortOrder.none:
              var validValue = value1 ?? value2;
              throw ArgumentError('Cannot compare null to $validValue. Consider using a different NullSortOrder.');
              break;
          }
        } else {
          // Both values are null; treat them as equal.
          result = 0;
        }

        // Skip subsequent comparisons if we have already found a difference
        if (result != 0) break;
      }
      return result;
    });
    return this;
  }

  /// Returns the number of elements matching the given [predicate].
  int count(bool Function(T) predicate) {
    if (this == null) return 0;
    var count = 0;
    this.forEach((element) {
      if (predicate(element)) ++count;
    });
    return count;
  }

  List<R> mapIndexed<R>(R transform(int index, T t)) {
    if (this == null) return null;
    final List<R> list = [];
    for (int i = 0; i < this.length; i++) {
      list.add(transform(i, this[i]));
    }
    return list;
  }
}
