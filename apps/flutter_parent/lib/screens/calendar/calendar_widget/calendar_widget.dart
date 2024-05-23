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

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/calendar/calendar_today_click_notifier.dart';
import 'package:flutter_parent/screens/calendar/calendar_today_notifier.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_day.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_month.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_week.dart';
import 'package:flutter_parent/screens/calendar/planner_fetcher.dart';
import 'package:flutter_parent/utils/common_widgets/dropdown_arrow.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart' hide TextDirection;
import 'package:provider/provider.dart';
import 'package:tuple/tuple.dart';

import 'calendar_day_of_week_headers.dart';

class CalendarWidget extends StatefulWidget {
  /// The curve to be used by all calendar animations
  static const Curve animCurve = Curves.easeInOutCubic;

  /// The duration to be used by all calendar animations
  static const Duration animDuration = const Duration(milliseconds: 300);

  /// Called to obtain the child widget for the specified [day]
  final Widget Function(BuildContext context, DateTime day) dayBuilder;

  /// A [PlannerFetcher] that handles fetching planner events from the API
  final PlannerFetcher fetcher;

  final VoidCallback? onFilterTap;

  /// Starting DateTime, defaults to today's date
  final DateTime? startingDate;

  /// Starting view, either 'week' or 'calendar'
  final CalendarView? startingView;

  const CalendarWidget({
    required this.dayBuilder,
    required this.fetcher,
    this.onFilterTap,
    this.startingDate,
    this.startingView,
    super.key
  });

  @override
  CalendarWidgetState createState() => CalendarWidgetState();
}

@visibleForTesting
enum CalendarPageChangeBehavior { none, jump, animate }

@visibleForTesting
class CalendarWidgetState extends State<CalendarWidget> with TickerProviderStateMixin {
  // Day, week, and month page indices for 'today'.
  //
  // Rather than track dates from the beginning of time/unix epoch/whatever, they are tracked
  // from the current DateTime such that the 'center' of the calendar range will always be the current date. The indices
  // here denote which page marks the center (i.e. initial page) of the respective PageViews for day, week, and month.
  //
  // The advantage of this approach is the ease of date tracking when constrained by the mechanics of PageView. The
  // disadvantage is that behavior is undefined at the min/max page indices. This means that if a user chooses to
  // manually swipe through 80+ years of calendar pages, they will discover a magical easter egg of sadness
  // at the end of their journey.
  static const int _todayDayIndex = 100000;
  static const int _todayWeekIndex = 10000;
  static const int _todayMonthIndex = 1000;

  // The max indices for the day, week, and month pagers. Because the 'today' indices represent the center of the
  // calendar range, these indices are simply double that of the 'today' indices.
  static const int _maxDayIndex = _todayDayIndex * 2;
  static const int _maxWeekIndex = _todayWeekIndex * 2;
  static const int _maxMonthIndex = _todayMonthIndex * 2;

  /// The currently-selected date
  DateTime selectedDay = DateTime.now();

  // Global keys for the PageViews
  Key _dayKey = GlobalKey();
  Key _weekKey = GlobalKey();
  Key _monthKey = GlobalKey();

  // Page controllers
  late PageController _dayController;
  late PageController _weekController;
  late PageController _monthController;

  // Notifier that tracks the current month collapse/expand progress
  MonthExpansionNotifier _monthExpansionNotifier = MonthExpansionNotifier(0.0);

  // Whether the calendar can be expanded to show the month view. This will only be false in cases where there
  // is not enough vertical space available to display the entire month
  bool _canExpandMonth = true;

  // Whether the month view is expanded, either partially or fully. This is updated by _monthExpansionNotifier
  // when the expansion value changes to or from zero.
  bool _isMonthExpanded = false;

  // The height of the fully-expanded month widget, which will vary depending on the number of week in the month.
  double _fullMonthHeight = _calculateFullMonthHeight(_todayMonthIndex);

  // Returns the allowed expansion height of the month calendar, i.e. the difference between the height of the
  // week widget and the height of the fully-expanded month widget.
  double get _monthExpansionHeight => _fullMonthHeight - CalendarWeek.weekHeight;

  // Controller for animating the month expand/collapse progress when the user presses the expand/collapse button
  late AnimationController _monthExpandAnimController;

  // Controller for animating the month expand/collapse progress when the user swipes vertically on the calendar
  late AnimationController? _monthFlingAnimController;

  // Controller for animating the full month height when switching between months that have a different number of weeks
  late AnimationController _monthHeightAdjustAnimController;

  // Returns the full month height for the month found at the specified month pager index
  static double _calculateFullMonthHeight(int monthIndex) {
    var yearMonth = _yearAndMonthForIndex(monthIndex);
    var weeks = CalendarMonth.generateWeekStarts(yearMonth.item1, yearMonth.item2).length;
    return DayOfWeekHeaders.headerHeight + (weeks * CalendarDay.dayHeight);
  }

  // Returns the DateTime that represents the day associated with the specified day pager index
  static DateTime _dayForIndex(int index) {
    final today = DateTime.now();
    final diff = index - _todayDayIndex;
    return DateTime(today.year, today.month, today.day).add(Duration(days: diff)).roundToMidnight()!;
  }

  // Returns the DateTime that represents the first day of the week associated with the specified week pager index
  static DateTime _weekStartForIndex(int index) {
    final today = DateTime.now();
    int weekOffset = index - _todayWeekIndex;
    return DateTime(today.year, today.month, today.day + (weekOffset * 7)).withFirstDayOfWeek()!;
  }

  // Returns the year and month associated with the specified month pager index
  static Tuple2<int, int> _yearAndMonthForIndex(int index) {
    final today = DateTime.now();
    int monthOffset = index - _todayMonthIndex;
    int monthOffsetFromYearStart = today.month - 1 + monthOffset;
    int yearOffset = (monthOffsetFromYearStart / 12).floor();

    var month = 1 + ((today.month - 1 + monthOffset) % 12);
    var year = today.year + yearOffset;

    return Tuple2(year, month);
  }

  // Returns the month pager index associated with the specified year and month
  static int _indexForYearAndMonth(int year, int month) {
    final today = DateTime.now();
    int yearOffset = year - today.year;
    int monthOffset = month - today.month;
    return _todayMonthIndex + monthOffset + (yearOffset * 12);
  }

  // Returns the week pager index associated with the week in which the specified day falls
  static int _weekIndexForDay(DateTime day) {
    final weekStart = day.withFirstDayOfWeek();
    final thisWeekStart = DateTime.now().withFirstDayOfWeek();
    double weeksDiff = thisWeekStart!.difference(weekStart!).inDays / 7;
    return _todayWeekIndex - weeksDiff.round();
  }

  // Returns the day pager index associated with the specified day
  static int _dayIndexForDay(DateTime day) {
    final todayNow = DateTime.now();
    final today = DateTime(todayNow.year, todayNow.month, todayNow.day);
    final diff = today.difference(day).inMicroseconds / Duration.microsecondsPerDay;
    return _todayDayIndex - diff.round();
  }

  @override
  void initState() {
    locator<CalendarTodayClickNotifier>().addListener(_todayClicked);

    // Update _isMonthExpanded when expansion value changes to or from zero
    _monthExpansionNotifier.addListener(() {
      bool isExpanded = _monthExpansionNotifier.value > 0.0;
      if (isExpanded != _isMonthExpanded) {
        _isMonthExpanded = isExpanded;
        setState(() {});
      }
    });

    if (_canExpandMonth) {
      if (widget.startingView == CalendarView.Month) {
        _isMonthExpanded = true;
        _monthExpansionNotifier.value = 1.0;
      } else
        _isMonthExpanded = false;
    }

    // Set up animation controller for tap-to-expand/collapse button
    _monthExpandAnimController = AnimationController(duration: CalendarWidget.animDuration, vsync: this);

    // Set up animation controller for animating month height changes due to different week counts
    _monthHeightAdjustAnimController = AnimationController(duration: CalendarWidget.animDuration, vsync: this);

    // Set up animation controller for expand/collapse fling animation
    _monthFlingAnimController = AnimationController(duration: CalendarWidget.animDuration, vsync: this);
    _monthFlingAnimController?.addListener(() {
      _monthExpansionNotifier.value = _monthFlingAnimController!.value;
    });

    // Set up controllers for day, week, and month pagers
    _dayController = PageController(initialPage: _todayDayIndex);
    _weekController = PageController(initialPage: _todayWeekIndex);
    _monthController = PageController(initialPage: _todayMonthIndex);

    if (widget.startingDate != null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        selectDay(
          widget.startingDate!,
          dayPagerBehavior: CalendarPageChangeBehavior.jump,
          weekPagerBehavior: CalendarPageChangeBehavior.jump,
          monthPagerBehavior: CalendarPageChangeBehavior.jump,
        );
      });
    }

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider<PlannerFetcher>(
      create: (context) => widget.fetcher,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          _calendarHeader(),
          Expanded(
            child: Stack(
              children: [
                _heightMonitor(),
                Column(
                  children: <Widget>[
                    _calendar(),
                    Divider(height: 1),
                    Expanded(child: _dayPager()),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _calendarHeader() {
    List<Widget> a11yButtons = [];
    if (MediaQuery.of(context).accessibleNavigation) {
      if (_isMonthExpanded) {
        a11yButtons = _a11yMonthButtons();
      } else {
        a11yButtons = _a11yWeekButtons();
      }
    }

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      crossAxisAlignment: CrossAxisAlignment.end,
      children: <Widget>[
        InkWell(
          key: Key('expand-button'),
          onTap: _canExpandMonth ? _toggleExpanded : null,
          child: Semantics(
            label: L10n(context).selectedMonthLabel(DateFormat.yMMMM(supportedDateLocale).format(selectedDay)),
            onTapHint: _isMonthExpanded ? L10n(context).monthTapCollapseHint : L10n(context).monthTapExpandHint,
            excludeSemantics: true,
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    DateFormat.y(supportedDateLocale).format(selectedDay),
                    style: Theme.of(context).textTheme.labelSmall,
                  ),
                  Row(
                    children: <Widget>[
                      Text(
                        DateFormat.MMMM(supportedDateLocale).format(selectedDay),
                        style: Theme.of(context).textTheme.headlineMedium,
                      ),
                      SizedBox(width: 10),
                      Visibility(
                        visible: _canExpandMonth,
                        child: ValueListenableBuilder(
                          builder: (BuildContext context, value, Widget? child) {
                            return DropdownArrow(
                                specificProgress: value, color: ParentTheme.of(context)!.onSurfaceColor);
                          },
                          valueListenable: _monthExpansionNotifier,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ),
        ...a11yButtons,
        InkWell(
          onTap: widget.onFilterTap,
          child: Container(
            height: 48,
            alignment: Alignment.bottomRight,
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 15),
              child: Text(
                L10n(context).calendars,
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Theme.of(context).colorScheme.secondary),
              ),
            ),
          ),
        )
      ],
    );
  }

  Widget _calendar() {
    return GestureDetector(
      onVerticalDragUpdate: _canExpandMonth
          ? (details) {
              var expansionDiff = details.primaryDelta! / _monthExpansionHeight;
              _monthExpansionNotifier.value = (_monthExpansionNotifier.value + expansionDiff).clamp(0.0, 1.0);
            }
          : null,
      onVerticalDragEnd: _canExpandMonth
          ? (details) {
              _monthFlingAnimController?.stop();
              if (_isMonthExpanded) {
                _monthFlingAnimController?.value = _monthExpansionNotifier.value;
                _monthFlingAnimController?.fling(velocity: details.primaryVelocity! / _monthExpansionHeight);
              }
            }
          : null,
      child: ValueListenableBuilder(
        child: Stack(
          children: <Widget>[
            Offstage(offstage: _isMonthExpanded, child: _weekPager()),
            Offstage(offstage: !_isMonthExpanded, child: _monthPager()),
          ],
        ),
        valueListenable: _monthExpansionNotifier,
        builder: (BuildContext context, double value, Widget? child) {
          return Container(
            height: CalendarWeek.weekHeight + (value * _monthExpansionHeight),
            child: child,
          );
        },
      ),
    );
  }

  Widget _dayPager() {
    final pager = PageView.builder(
      key: _dayKey,
      controller: _dayController,
      itemCount: _maxDayIndex,
      itemBuilder: (context, index) {
        DateTime date = _dayForIndex(index);
        return widget.dayBuilder(context, date);
      },
      onPageChanged: (index) {
        selectDay(_dayForIndex(index), dayPagerBehavior: CalendarPageChangeBehavior.none);
      },
    );

    GestureDragUpdateCallback? updateCallback = null;
    GestureDragEndCallback? endCallback = null;

    if (_isMonthExpanded) {
      updateCallback = (details) {
        var expansionDiff = details.primaryDelta! / _monthExpansionHeight;
        _monthExpansionNotifier.value = (_monthExpansionNotifier.value + expansionDiff).clamp(0.0, 1.0);
      };
      endCallback = (details) {
        _monthFlingAnimController?.stop();
        _monthFlingAnimController?.value = _monthExpansionNotifier.value;
        _monthFlingAnimController?.fling(velocity: details.primaryVelocity! / _monthExpansionHeight);
      };
    }

    return Stack(
      fit: StackFit.expand,
      children: <Widget>[
        pager,
        if (_isMonthExpanded)
          GestureDetector(
            excludeFromSemantics: true,
            behavior: HitTestBehavior.translucent,
            onVerticalDragUpdate: updateCallback,
            onVerticalDragEnd: endCallback,
          ),
      ],
    );
  }

  Widget _monthPager() {
    return PageView.builder(
      key: _monthKey,
      itemCount: _maxMonthIndex,
      controller: _monthController,
      itemBuilder: (context, index) {
        final yearMonth = _yearAndMonthForIndex(index);
        return CalendarMonth(
          year: yearMonth.item1,
          month: yearMonth.item2,
          selectedDay: selectedDay,
          monthExpansionListener: _monthExpansionNotifier,
          onDaySelected: (day) {
            selectDay(day, dayPagerBehavior: CalendarPageChangeBehavior.jump);
          },
        );
      },
      onPageChanged: (index) {
        final oldHeight = _fullMonthHeight;
        final newHeight = _calculateFullMonthHeight(index);

        var yearMonth = _yearAndMonthForIndex(index);

        var maxDays = DateTime(yearMonth.item1, yearMonth.item2 + 1, 0).day;
        int dayOfMonth = min(maxDays, selectedDay.day);
        selectDay(DateTime(yearMonth.item1, yearMonth.item2, dayOfMonth),
            monthPagerBehavior: CalendarPageChangeBehavior.none, weekPagerBehavior: CalendarPageChangeBehavior.jump);

        if (oldHeight != newHeight) {
          double begin = oldHeight / newHeight;
          double end = 1.0;
          _monthHeightAdjustAnimController.stop();
          _monthHeightAdjustAnimController.reset();
          Tween<double> tween = Tween<double>(begin: begin, end: end);
          Animation<double> anim = tween.animate(
            CurvedAnimation(parent: _monthHeightAdjustAnimController, curve: CalendarWidget.animCurve),
          );
          VoidCallback? listener = null;
          listener = () {
            if (anim.status == AnimationStatus.completed && listener != null) anim.removeListener(listener);
            _fullMonthHeight = anim.value * newHeight;
            _monthExpansionNotifier.notify();
          };
          anim.addListener(listener);
          _monthHeightAdjustAnimController.forward();
        }
      },
    );
  }

  void selectDay(
    DateTime day, {
    CalendarPageChangeBehavior dayPagerBehavior = CalendarPageChangeBehavior.jump,
    CalendarPageChangeBehavior weekPagerBehavior = CalendarPageChangeBehavior.animate,
    CalendarPageChangeBehavior monthPagerBehavior = CalendarPageChangeBehavior.animate,
  }) {
    // Do nothing if the day is already selected
    if (selectedDay.isSameDayAs(day)) return;

    selectedDay = day;

    // Enable/disable the today button
    Provider.of<CalendarTodayNotifier>(context, listen: false).value = !DateTime.now().isSameDayAs(selectedDay);

    // Month change
    if (monthPagerBehavior == CalendarPageChangeBehavior.animate) {
      _monthController.animateToPage(
        _indexForYearAndMonth(selectedDay.year, selectedDay.month),
        duration: CalendarWidget.animDuration,
        curve: CalendarWidget.animCurve,
      );
    } else if (monthPagerBehavior == CalendarPageChangeBehavior.jump) {
      _monthController.jumpToPage(_indexForYearAndMonth(selectedDay.year, selectedDay.month));
    }

    // Week change
    if (weekPagerBehavior == CalendarPageChangeBehavior.animate) {
      _weekController.animateToPage(
        _weekIndexForDay(selectedDay),
        duration: CalendarWidget.animDuration,
        curve: CalendarWidget.animCurve,
      );
    } else if (weekPagerBehavior == CalendarPageChangeBehavior.jump) {
      _weekController.jumpToPage(_weekIndexForDay(selectedDay));
    }

    // Day change
    if (dayPagerBehavior == CalendarPageChangeBehavior.animate) {
      _dayController.animateToPage(
        _dayIndexForDay(day),
        duration: CalendarWidget.animDuration,
        curve: CalendarWidget.animCurve,
      );
    } else if (dayPagerBehavior == CalendarPageChangeBehavior.jump) {
      _dayController.jumpToPage(_dayIndexForDay(day));
    }
    setState(() {});
  }

  Widget _weekPager() {
    return PageView.builder(
      key: _weekKey,
      itemCount: _maxWeekIndex,
      controller: _weekController,
      itemBuilder: (context, index) {
        final weekStart = _weekStartForIndex(index);
        return CalendarWeek(
          selectedDay: selectedDay,
          firstDay: weekStart,
          displayDayOfWeekHeader: true,
          onDaySelected: (day) {
            selectDay(day,
                dayPagerBehavior: CalendarPageChangeBehavior.jump, weekPagerBehavior: CalendarPageChangeBehavior.none);
          },
        );
      },
      onPageChanged: (index) {
        if (_isMonthExpanded) return;
        var weekStart = _weekStartForIndex(index);
        var selectedDayOffset = (selectedDay.weekday - weekStart.weekday) % 7;
        var newSelectedDay = DateTime(weekStart.year, weekStart.month, weekStart.day + selectedDayOffset);

        selectDay(
          newSelectedDay,
          weekPagerBehavior: CalendarPageChangeBehavior.none,
          monthPagerBehavior: CalendarPageChangeBehavior.jump,
        );
      },
    );
  }

  List<Widget> _a11yWeekButtons() {
    int index = _todayWeekIndex;
    if (_weekController.hasClients) {
      index = _weekController.page?.toInt() ?? 0;
    }

    final format = DateFormat.MMMMd(supportedDateLocale).add_y();

    final previousWeek = _weekStartForIndex(index - 1);
    final previousWeekName = format.format(previousWeek);

    final nextWeek = _weekStartForIndex(index + 1);
    final nextWeekName = format.format(nextWeek);

    return [
      IconButton(
        key: Key('calendar-a11y-previous-week'),
        tooltip: L10n(context).previousWeek(previousWeekName),
        icon: Icon(CanvasIcons.arrow_left),
        onPressed: () {
          _weekController.jumpToPage(index - 1);
        },
      ),
      IconButton(
        key: Key('calendar-a11y-next-week'),
        tooltip: L10n(context).nextWeek(nextWeekName),
        icon: Icon(CanvasIcons.arrow_right),
        onPressed: () {
          _weekController.jumpToPage(index + 1);
        },
      ),
    ];
  }

  List<Widget> _a11yMonthButtons() {
    int index = _todayMonthIndex;
    if (_monthController.hasClients) {
      index = _monthController.page?.toInt() ?? 0;
    }

    final format = DateFormat.MMMM(supportedDateLocale).add_y();

    final previousMonth = _yearAndMonthForIndex(index - 1);
    final previousMonthName = format.format(DateTime(previousMonth.item1, previousMonth.item2));

    final nextMonth = _yearAndMonthForIndex(index + 1);
    final nextMonthName = format.format(DateTime(nextMonth.item1, nextMonth.item2));

    return [
      IconButton(
        key: Key('calendar-a11y-previous-month'),
        tooltip: L10n(context).previousMonth(previousMonthName),
        icon: Icon(CanvasIcons.arrow_left),
        onPressed: () {
          _monthController.jumpToPage(index - 1);
        },
      ),
      IconButton(
        key: Key('calendar-a11y-next-month'),
        tooltip: L10n(context).nextMonth(nextMonthName),
        icon: Icon(CanvasIcons.arrow_right),
        onPressed: () {
          _monthController.jumpToPage(index + 1);
        },
      ),
    ];
  }

  /// Builds a widget that monitors the available vertical space available for a fully-expanded month view. If the
  /// space ever becomes insufficient, the month view will be collapsed and disabled.
  Widget _heightMonitor() {
    return LayoutBuilder(
      builder: (context, constraints) {
        bool expansionAllowed = constraints.maxHeight > CalendarMonth.maxHeight;
        if (expansionAllowed != _canExpandMonth) {
          if (!expansionAllowed && _isMonthExpanded) _toggleExpanded();
          WidgetsBinding.instance.addPostFrameCallback((_) {
            setState(() {
              _canExpandMonth = expansionAllowed;
            });
          });
        }
        return Container(width: double.infinity, height: double.infinity);
      },
    );
  }

  _toggleExpanded() {
    double begin = _monthExpansionNotifier.value;
    double end = _isMonthExpanded ? 0.0 : 1.0;
    _monthExpandAnimController.stop();
    _monthExpandAnimController.reset();
    Tween<double> tween = Tween<double>(begin: begin, end: end);
    final Animation<double> anim = tween.animate(
      CurvedAnimation(parent: _monthExpandAnimController, curve: CalendarWidget.animCurve),
    );
    VoidCallback? listener = null;
    listener = () {
      _monthExpansionNotifier.value = anim.value;
      if (anim.status == AnimationStatus.completed && listener != null) anim.removeListener(listener);
    };
    anim.addListener(listener);
    _monthExpandAnimController.forward();
  }

  // Use a date without a time (default to midnight), otherwise the second call to selectDay() in the day/month pager
  // will be off by one day
  _todayClicked() => selectDay(DateTime(DateTime.now().year, DateTime.now().month, DateTime.now().day));

  @override
  void dispose() {
    locator<CalendarTodayClickNotifier>().removeListener(_todayClicked);
    super.dispose();
  }
}

enum CalendarView { Month, Week }
