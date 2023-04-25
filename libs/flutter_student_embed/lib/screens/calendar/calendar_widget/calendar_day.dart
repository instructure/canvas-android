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
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/models/planner_item.dart';
import 'package:flutter_student_embed/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_student_embed/utils/design/student_colors.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

import '../planner_fetcher.dart';

typedef DaySelectedCallback(DateTime day);

class CalendarDay extends StatelessWidget {
  static const double dayHeight = 50;

  final DateTime date;
  final DateTime selectedDay;
  final DaySelectedCallback onDaySelected;

  const CalendarDay({
    Key key,
    @required this.date,
    @required this.selectedDay,
    @required this.onDaySelected,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    final isToday = date.isSameDayAs(DateTime.now());
    final isSelected = date.isSameDayAs(selectedDay);

    TextStyle textStyle = theme.textTheme.headline5;
    if (date.isWeekend() || date.month != selectedDay.month) textStyle = textStyle.copyWith(color: StudentColors.textDark);
    BoxDecoration decoration;

    if (isToday) {
      textStyle = Theme.of(context).accentTextTheme.headline5;
      decoration = BoxDecoration(color: StudentColors.buttonColor, shape: BoxShape.circle);
    } else if (isSelected) {
      textStyle = textStyle.copyWith(color: StudentColors.textButtonColor);
      decoration = BoxDecoration(
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: StudentColors.textButtonColor,
          width: 2,
        ),
      );
    }

    return Selector<PlannerFetcher, AsyncSnapshot<List<PlannerItem>>>(
        selector: (_, fetcher) => fetcher.getSnapshotForDate(date),
        builder: (_, snapshot, __) {
          int eventCount = snapshot.hasData ? snapshot.data.length : 0;
          return InkResponse(
            enableFeedback: true,
            highlightColor: StudentColors.buttonColor.withOpacity(0.35),
            splashColor: StudentColors.buttonColor.withOpacity(0.35),
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
                        child: Text(date.day.toString(), semanticsLabel: L10n(context).calendarDaySemanticsLabel(DateFormat.MMMMEEEEd().format(date), eventCount)),
                      ),
                    ),
                  ),
                  SizedBox(height: 4),
                  _eventIndicator(context, snapshot),
                ],
              ),
            ),
          );
        });
  }

  Widget _eventIndicator(BuildContext context, AsyncSnapshot<List<PlannerItem>> snapshot) {
    /// On error, show nothing
    if (snapshot.hasError) return Container();

    /// On success, show dots for activities
    if (snapshot.hasData && snapshot.connectionState == ConnectionState.done) {
      // Show at most three dots for events
      int count = min(snapshot.data.length, 3);
      int itemCount = count < 1 ? count : (count * 2) - 1;
      if (count == 0) return Container();
      return Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: List.generate(itemCount, (index) {
          if (index % 2 == 1) return SizedBox(width: 4);
          return Container(
            width: 4,
            height: 4,
            decoration: BoxDecoration(color: StudentColors.textButtonColor, shape: BoxShape.circle),
          );
        }),
      );
    }

    /// Otherwise, show loading
    return Container(
      width: 4,
      height: 4,
      child: _RepeatTween(
        duration: Duration(milliseconds: 350),
        delay: Duration(milliseconds: 100 * (date.localDayOfWeek)),
        builder: (BuildContext context, Animation animation) {
          return ScaleTransition(
            scale: animation,
            child: Container(
              width: 4,
              height: 4,
              decoration: BoxDecoration(color: StudentColors.textDark, shape: BoxShape.circle),
            ),
          );
        },
      ),
    );
  }
}

class _RepeatTween extends StatefulWidget {
  final Duration duration;

  final Duration delay;

  final Widget Function(BuildContext context, Animation<double> animation) builder;

  const _RepeatTween({Key key, @required this.duration, this.delay, @required this.builder}) : super(key: key);

  @override
  __RepeatTweenState createState() => __RepeatTweenState();
}

class __RepeatTweenState extends State<_RepeatTween> with SingleTickerProviderStateMixin {
  AnimationController _controller;
  Tween<double> tween = Tween<double>(begin: 0.0, end: 1.0);

  @override
  void initState() {
    _controller = AnimationController(duration: widget.duration, vsync: this);
    _startWithDelay();
    super.initState();
  }

  _startWithDelay() async {
    if (widget.delay != null) await Future.delayed(widget.delay);
    if (mounted) _controller.repeat(reverse: true);
  }

  @override
  void dispose() {
    if (_controller.isAnimating) _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return widget.builder(context, tween.animate(CurvedAnimation(parent: _controller, curve: Curves.easeInOutCubic)));
  }
}
