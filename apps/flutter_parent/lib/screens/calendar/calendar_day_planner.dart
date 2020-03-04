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

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/planner_api.dart';
import 'package:flutter_parent/screens/calendar/calendar_day_list_tile.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class CalendarDayPlanner extends StatefulWidget {
  final DateTime _day;
  final User _student;

  CalendarDayPlanner(this._student, this._day);

  @override
  State<StatefulWidget> createState() => CalendarDayPlannerState();
}

class CalendarDayPlannerState extends State<CalendarDayPlanner> {
  Future<List<PlannerItem>> _eventsFuture;

  @override
  void initState() {
    super.initState();
    _eventsFuture = locator
        .get<PlannerApi>()
        .getUserPlannerItems(widget._student.id, widget._day, widget._day.add(Duration(days: 1)), forceRefresh: true);
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
        future: _eventsFuture,
        builder: (BuildContext context, AsyncSnapshot<List<PlannerItem>> snapshot) {
          Widget body;
          if (snapshot.hasError) {
            body = ErrorPandaWidget('There was an error', () {});
          } else if (!snapshot.hasData) {
            body = LoadingIndicator();
          } else {
            if (snapshot.data.isEmpty) {
              body = EmptyPandaWidget(title: 'No events for this day', subtitle: 'Get some real events n00b');
            } else {
              body = CalendarDayList(snapshot.data);
            }
          }

          return RefreshIndicator(
            child: body,
            onRefresh: () {
              // TODO:
              return Future.value([]);
//              setState(() {
////                _eventsFuture = PlannerApi().getUserPlannerItems(widget._student.id, day, forceRefresh: true);
//              });
//              return _eventsFuture;
            },
          );
        });
  }
}

class CalendarDayList extends StatelessWidget {
  final List<PlannerItem> _plannerItems;

  CalendarDayList(this._plannerItems);

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      itemCount: _plannerItems.length,
      itemBuilder: (context, index) => _dayTile(context, _plannerItems[index], index),
    );
  }

  Widget _dayTile(BuildContext context, PlannerItem plannerItem, int index) => CalendarDayListTile(plannerItem);
}
