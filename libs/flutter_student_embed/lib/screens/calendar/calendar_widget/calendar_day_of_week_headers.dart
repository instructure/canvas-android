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
import 'package:flutter_student_embed/utils/design/student_theme.dart';
import 'package:intl/intl.dart' hide TextDirection;

class DayOfWeekHeaders extends StatelessWidget {
  static const double headerHeight = 14;

  @override
  Widget build(BuildContext context) {
    final weekendTheme = Theme.of(context).textTheme.subtitle2;
    final weekdayTheme = weekendTheme.copyWith(color: StudentTheme.of(context).onSurfaceColor);

    final symbols = DateFormat().dateSymbols;
    final firstDayOfWeek = symbols.FIRSTDAYOFWEEK;

    return ExcludeSemantics(
      child: Container(
        height: headerHeight,
        child: Row(
          mainAxisSize: MainAxisSize.max,
          children: List.generate(7, (index) {
            final day = (firstDayOfWeek + index + 1) % 7;
            return Expanded(
              child: Text(
                symbols.STANDALONESHORTWEEKDAYS[day],
                textAlign: TextAlign.center,
                style: symbols.WEEKENDRANGE.contains((day - 1) % 7) ? weekendTheme : weekdayTheme,
              ),
            );
          }),
        ),
      ),
    );
  }
}
