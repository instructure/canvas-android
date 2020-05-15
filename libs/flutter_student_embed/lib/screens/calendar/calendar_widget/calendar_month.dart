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
import 'package:flutter_student_embed/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_student_embed/utils/core_extensions/list_extensions.dart';

import 'calendar_day.dart';
import 'calendar_day_of_week_headers.dart';
import 'calendar_week.dart';

class CalendarMonth extends StatefulWidget {
  final int year;
  final int month;
  final DateTime selectedDay;
  final DaySelectedCallback onDaySelected;
  final MonthExpansionNotifier monthExpansionListener;

  CalendarMonth({
    Key key,
    @required this.year,
    @required this.month,
    @required this.selectedDay,
    @required this.onDaySelected,
    @required this.monthExpansionListener,
  }) : super(key: key);

  /// The maximum possible height of this widget
  static double maxHeight = DayOfWeekHeaders.headerHeight + (6 * CalendarDay.dayHeight);

  static List<DateTime> generateWeekStarts(int year, int month) {
    DateTime firstDayOfMonth = DateTime(year, month);
    DateTime firstDayOfWeek = firstDayOfMonth.withFirstDayOfWeek();

    List<DateTime> weekStarts = [firstDayOfWeek];

    while (true) {
      var last = weekStarts.last;
      var weekStart = DateTime(last.year, last.month, last.day + 7);
      if (weekStart.month == month) {
        weekStarts.add(weekStart);
      } else {
        break;
      }
    }
    return weekStarts;
  }

  @override
  _CalendarMonthState createState() => _CalendarMonthState();
}

class _CalendarMonthState extends State<CalendarMonth> {
  List<DateTime> weekStarts;

  @override
  void initState() {
    weekStarts = CalendarMonth.generateWeekStarts(widget.year, widget.month);
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final weekWidgets = weekStarts.mapIndexed<Widget>((index, weekStart) {
      final weekWidget = CalendarWeek(
        firstDay: weekStart,
        selectedDay: widget.selectedDay,
        onDaySelected: widget.onDaySelected,
        displayDayOfWeekHeader: false,
      );

      return ValueListenableBuilder<double>(
        child: weekWidget,
        valueListenable: widget.monthExpansionListener,
        builder: (BuildContext context, double value, Widget child) {
          final top = DayOfWeekHeaders.headerHeight + (value * index * CalendarDay.dayHeight);
          return Positioned(
            top: top,
            left: 0,
            right: 0,
            child: Opacity(
              opacity: weekWidget.days.any((it) => it.isSameDayAs(widget.selectedDay))
                  ? 1.0
                  : Curves.easeInCubic.transform(value),
              child: child,
            ),
          );
        },
      );
    });

    return Stack(
      children: [
        DayOfWeekHeaders(),
        ...weekWidgets,
      ],
    );
  }
}

class MonthExpansionNotifier extends ValueNotifier<double> {
  MonthExpansionNotifier(double value) : super(value);

  void notify() => notifyListeners();
}
