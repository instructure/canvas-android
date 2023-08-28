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
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:intl/intl.dart';

class DayOfWeekHeaders extends StatelessWidget {
  static const double headerHeight = 16;

  @override
  Widget build(BuildContext context) {
    final weekendTheme = Theme.of(context).textTheme.titleSmall;
    final weekdayTheme = weekendTheme?.copyWith(color: ParentTheme.of(context)?.onSurfaceColor);

    final symbols = DateFormat(null, supportedDateLocale).dateSymbols;
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
