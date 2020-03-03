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

import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_event_count.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_widget.dart';
import 'package:intl/intl.dart';

class CalendarScreen extends StatefulWidget {
  final User _student;

  CalendarScreen(this._student, {Key key}) : super(key: key);

  @override
  State<StatefulWidget> createState() => CalendarScreenState();
}

class CalendarScreenState extends State<CalendarScreen> {
  @override
  Widget build(BuildContext context) {
    return CalendarWidget(
      eventCount: _sampleEventCount(),
      dayBuilder: (BuildContext context, DateTime day) {
        // TODO: This is just a sample page
        return _buildSamplePage(context, day);
      },
    );
  }

  CalendarEventCount _sampleEventCount() {
    final eventCount = CalendarEventCount();
    final Random randy = Random();
    for (int i = -60; i <= 60; i++) {
      if (randy.nextDouble() < 0.2) {
        final today = DateTime.now();
        var date = DateTime(today.year, today.month, today.day).add(Duration(days: i));
        eventCount.setCountForDate(date, 1 + randy.nextInt(3));
      }
    }
    return eventCount;
  }

  _buildSamplePage(BuildContext context, DateTime day) {
    return ListView.builder(
      itemCount: 11,
      itemBuilder: (BuildContext context, int index) {
        if (index == 0) {
          return Padding(
            padding: const EdgeInsets.all(16),
            child: Center(
              child: Text(
                DateFormat.yMMMMEEEEd().format(day),
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              ),
            ),
          );
        }
        return ListTile(title: Text("Item $index"));
      },
    );
  }
}
