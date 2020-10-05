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

import 'dart:convert';

import 'package:built_value/built_value.dart';
import 'package:collection/collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/models/planner_item.dart';
import 'package:flutter_student_embed/models/serializers.dart';
import 'package:flutter_student_embed/network/utils/api_prefs.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_widget/calendar_widget.dart';
import 'package:flutter_student_embed/screens/calendar/planner_fetcher.dart';
import 'package:flutter_student_embed/screens/to_do/create_update_to_do_screen.dart';
import 'package:flutter_student_embed/screens/to_do/to_do_details_screen.dart';
import 'package:flutter_student_embed/utils/common_widgets/appbar_dynamic_style.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';
import 'package:flutter_student_embed/utils/service_locator.dart';
import 'package:flutter_svg/flutter_svg.dart';

import 'calendar_day_planner.dart';
import 'calendar_widget/calendar_filter_screen/calendar_filter_list_screen.dart';

class CalendarScreen extends StatefulWidget {
  static const String routeName = 'calendar';

  final DateTime startDate;
  final CalendarView startView;

  // Keys for the deep link parameter map passed in via DashboardScreen
  static final startDateKey = 'startDate';
  static final startViewKey = 'startView';
  final String channelId;

  CalendarScreen({Key key, this.startDate, this.startView = CalendarView.Week, this.channelId}) : super(key: key);

  @override
  State<StatefulWidget> createState() => CalendarScreenState();
}

class CalendarScreenState extends State<CalendarScreen> {
  PlannerFetcher _fetcher;

  CalendarScreenChannel _channel;

  GlobalKey<CalendarWidgetState> _calendarKey = GlobalKey();

  bool _showTodayButton = false;

  DateTime _currentDay = DateTime.now();

  @override
  void initState() {
    _fetcher = PlannerFetcher(userId: ApiPrefs.getUser().id, userDomain: ApiPrefs.getDomain());
    if (widget.channelId != null) {
      _channel = CalendarScreenChannel(widget.channelId);
    }
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: dynamicStyleAppBar(
        context: context,
        appBar: AppBar(
          title: Text(L10n(context).calendar),
          leading: IconButton(
            icon: Icon(Icons.menu),
            onPressed: () => _channel.openDrawer(),
            tooltip: MaterialLocalizations.of(context).openAppDrawerTooltip,
          ),
          actions: <Widget>[
            if (_showTodayButton)
              Tooltip(
                message: L10n(context).gotoTodayButtonLabel,
                child: InkResponse(
                  child: Padding(
                    padding: const EdgeInsets.all(12),
                    child: SvgPicture.asset(
                      'assets/svg/calendar-today.svg',
                      width: 24,
                      height: 24,
                      color: Theme.of(context).primaryIconTheme.color,
                    ),
                  ),
                  onTap: () {
                    var now = DateTime.now();
                    _calendarKey.currentState.selectDay(
                      DateTime(now.year, now.month, now.day),
                      dayPagerBehavior: CalendarPageChangeBehavior.jump,
                      weekPagerBehavior: CalendarPageChangeBehavior.animate,
                      monthPagerBehavior: CalendarPageChangeBehavior.animate,
                    );
                  },
                ),
              )
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          var updatedDates = await locator<QuickNav>().push(context, CreateUpdateToDoScreen(initialDate: _currentDay));
          _refreshDates(updatedDates);
        },
        child: Icon(Icons.add, semanticLabel: L10n(context).newToDo),
      ),
      body: CalendarWidget(
        key: _calendarKey,
        fetcher: _fetcher,
        startingDate: widget.startDate,
        startingView: widget.startView,
        onTodaySelected: (isTodaySelected) {
          if (_showTodayButton == isTodaySelected) {
            setState(() {
              _showTodayButton = !isTodaySelected;
            });
          }
        },
        onFilterTap: () async {
          Set<String> currentContexts = await _fetcher.getContexts();
          Set<String> updatedContexts = await locator<QuickNav>().push(
            context,
            CalendarFilterListScreen(currentContexts),
          );
          // Check if the list changed or not
          if (!SetEquality().equals(currentContexts, updatedContexts)) {
            _fetcher.setContexts(updatedContexts);
          }
        },
        dayBuilder: (BuildContext context, DateTime day) {
          _currentDay = day;
          return CalendarDayPlanner(day, onItemSelected: (item) async {
            if (item.plannableType == 'planner_note') {
              // Display planner to-do details in flutter, refreshing changed dates if necessary
              var updatedDates = await locator<QuickNav>().push(context, ToDoDetailsScreen(item));
              _refreshDates(updatedDates);
            } else {
              _channel.nativeRouteToItem(item);
            }
          });
        },
      ),
    );
  }

  void _refreshDates(@nullable List<DateTime> dates) {
    if (dates == null) return;
    PlannerFetcher.notifyDatesChanged(dates);
  }

  @override
  void dispose() {
    _channel?.dispose();
    super.dispose();
  }
}

class CalendarScreenChannel extends MethodChannel {
  CalendarScreenChannel(String channelId) : super(channelId) {
    setMethodCallHandler((methodCall) async {
      // Set up call handling here
    });
  }

  void dispose() {
    setMethodCallHandler(null);
  }

  void openDrawer() => invokeMethod('openDrawer');

  void nativeRouteToItem(PlannerItem item) {
    var payload = json.encode(serialize<PlannerItem>(item));
    invokeMethod('routeToItem', payload);
  }
}
