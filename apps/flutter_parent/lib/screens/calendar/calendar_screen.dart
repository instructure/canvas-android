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
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/calendar/calendar_day_planner.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_widget.dart';
import 'package:flutter_parent/screens/calendar/planner_fetcher.dart';
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:provider/provider.dart';

class CalendarScreen extends StatefulWidget {
  CalendarScreen({Key key}) : super(key: key);

  @override
  State<StatefulWidget> createState() => CalendarScreenState();
}

class CalendarScreenState extends State<CalendarScreen> {
  User _student;
  PlannerFetcher _fetcher;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    var _selectedStudent = Provider.of<SelectedStudentNotifier>(context, listen: true).value;
    if (_student != _selectedStudent) {
      // The student was changed by the user, create/reset the fetcher
      _student = _selectedStudent;
      if (_fetcher == null) {
        _fetcher = PlannerFetcher(userId: _student.id);
      } else {
        _fetcher.reset(_student.id, []);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return CalendarWidget(
      fetcher: _fetcher,
      onFilterTap: () {
        // TODO: MBL-13920 course filter. On courses changed, reset _fetcher with new contexts.
      },
      dayBuilder: (BuildContext context, DateTime day) {
        return CalendarDayPlanner(day);
      },
    );
  }
}
