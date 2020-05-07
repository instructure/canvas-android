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

import 'calendar_day.dart';
import 'calendar_day_of_week_headers.dart';

class CalendarWeek extends StatelessWidget {
  static double weekHeight = DayOfWeekHeaders.headerHeight + CalendarDay.dayHeight;

  final DateTime firstDay;
  final DateTime selectedDay;
  final DaySelectedCallback onDaySelected;
  final bool displayDayOfWeekHeader;
  final List<DateTime> days;

  CalendarWeek({
    Key key,
    @required this.firstDay,
    @required this.selectedDay,
    @required this.onDaySelected,
    @required this.displayDayOfWeekHeader,
  })  : days = generateDays(firstDay),
        super(key: key);

  static List<DateTime> generateDays(DateTime firstDay) {
    return List.generate(7, (index) => DateTime(firstDay.year, firstDay.month, firstDay.day + index));
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        if (displayDayOfWeekHeader) DayOfWeekHeaders(),
        Container(
          height: CalendarDay.dayHeight,
          child: Row(
            mainAxisSize: MainAxisSize.max,
            children: [
              for (DateTime day in days)
                Expanded(
                  child: CalendarDay(
                    date: day,
                    selectedDay: selectedDay,
                    onDaySelected: onDaySelected,
                  ),
                )
            ],
          ),
        ),
      ],
    );
  }
}
