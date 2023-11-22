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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/screens/calendar/calendar_day_list_tile.dart';
import 'package:flutter_parent/screens/calendar/planner_fetcher.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:provider/provider.dart';

class CalendarDayPlanner extends StatefulWidget {
  final DateTime _day;

  CalendarDayPlanner(this._day);

  @override
  State<StatefulWidget> createState() => CalendarDayPlannerState();
}

class CalendarDayPlannerState extends State<CalendarDayPlanner> {
  @override
  Widget build(BuildContext context) {
    return Selector<PlannerFetcher, AsyncSnapshot<List<PlannerItem>>>(
      selector: (_, fetcher) => fetcher.getSnapshotForDate(widget._day),
      builder: (_, snapshot, __) {
        Widget body;
        if (snapshot.hasError) {
          body = ErrorPandaWidget(L10n(context).errorLoadingEvents, _refresh, header: SizedBox(height: 32));
        } else if (!snapshot.hasData) {
          body = LoadingIndicator();
        } else {
          if (snapshot.data!.isEmpty) {
            body = EmptyPandaWidget(
              svgPath: 'assets/svg/panda-no-events.svg',
              title: L10n(context).noEventsTitle,
              subtitle: L10n(context).noEventsMessage,
              header: SizedBox(height: 32),
            );
          } else {
            body = CalendarDayList(snapshot.data!);
          }
        }

        return RefreshIndicator(
          child: body,
          onRefresh: _refresh,
        );
      },
    );
  }

  Future<void> _refresh() => Provider.of<PlannerFetcher>(context, listen: false).refreshItemsForDate(widget._day);
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
