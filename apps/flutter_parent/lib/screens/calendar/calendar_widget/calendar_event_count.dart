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

/// TODO: Modify or replace this class when implementing MBL-13913.
/// A temporary class for displaying fake event counts on the calendar.
class CalendarEventCount {
  final Map<String, int> countMap = {};

  int getCountForDate(DateTime date) => countMap[keyForDate(date)] ?? 0;

  int setCountForDate(DateTime date, int count) => countMap[keyForDate(date)] = count;

  String keyForDate(DateTime date) => '${date.year}-${date.month}-${date.day}';

  void reset() {
    countMap.clear();
  }
}
