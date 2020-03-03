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
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_event_count.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:intl/intl.dart';

typedef DaySelectedCallback(DateTime day);

class CalendarDay extends StatelessWidget {
  static const double dayHeight = 50;

  final DateTime date;
  final DateTime selectedDay;
  final DaySelectedCallback onDaySelected;
  final CalendarEventCount eventCount;

  const CalendarDay({
    Key key,
    @required this.date,
    @required this.selectedDay,
    @required this.onDaySelected,
    @required this.eventCount,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    final isToday = date.isSameDayAs(DateTime.now());
    final isSelected = date.isSameDayAs(selectedDay);

    TextStyle textStyle = theme.textTheme.headline;
    if (date.isWeekend() || date.month != selectedDay.month) textStyle = textStyle.copyWith(color: ParentColors.ash);
    BoxDecoration decoration = null;

    if (isToday) {
      textStyle = Theme.of(context).accentTextTheme.headline;
      decoration = BoxDecoration(color: theme.accentColor, shape: BoxShape.circle);
    } else if (isSelected) {
      textStyle = textStyle.copyWith(color: Theme.of(context).accentColor);
      decoration = BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: theme.accentColor,
          width: 2,
        ),
      );
    }

    return InkResponse(
      enableFeedback: true,
      highlightColor: theme.accentColor.withOpacity(0.35),
      splashColor: theme.accentColor.withOpacity(0.35),
      onTap: () => onDaySelected(date),
      child: Container(
        height: dayHeight,
        child: Column(
          children: <Widget>[
            SizedBox(height: 6),
            Container(
              height: 32,
              width: 32,
              decoration: decoration,
              child: Center(
                child: AnimatedDefaultTextStyle(
                  style: textStyle,
                  duration: Duration(milliseconds: 300),
                  child: Text(date.day.toString(), semanticsLabel: DateFormat.MMMMEEEEd().format(date)),
                ),
              ),
            ),
            SizedBox(height: 4),
            _eventIndicator(context),
          ],
        ),
      ),
    );
  }

  Widget _eventIndicator(BuildContext context) {
    int count = eventCount.getCountForDate(date) % 4;
    int itemCount = count < 1 ? count : (count * 2) - 1;

    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(itemCount, (index) {
        if (index % 2 == 1) return SizedBox(width: 4);
        return Container(
          width: 4,
          height: 4,
          decoration: BoxDecoration(color: Theme.of(context).accentColor, shape: BoxShape.circle),
        );
      }),
    );
  }
}
