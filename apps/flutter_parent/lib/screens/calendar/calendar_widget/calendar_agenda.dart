import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/models/user_color.dart';
import 'package:flutter_parent/models/user_colors.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

import '../calendar_day_list_tile.dart';
import '../planner_fetcher.dart';

class CalendarAgenda extends StatefulWidget {
  final DateTime _day;
  final bool _monthlyView;

  CalendarAgenda(this._day, this._monthlyView);

  @override
  State<StatefulWidget> createState() => CalendarAgendaState();
}

class CalendarAgendaState extends State<CalendarAgenda> {
  @override
  Widget build(BuildContext context) {
    return Selector<PlannerFetcher,
        AsyncSnapshot<Map<String, List<PlannerItem>>>>(
      selector: (_, fetcher) => fetcher.getSnapshotsForWeek(widget._day),
      builder: (_, snapshot, __) {
        Widget body;
        if (snapshot.hasError) {
          body = ErrorPandaWidget(L10n(context).errorLoadingEvents, _refresh,
              header: SizedBox(height: 32));
        } else if (!snapshot.hasData) {
          body = LoadingIndicator();
        } else {
          if (snapshot.data.isEmpty) {
            body = EmptyPandaWidget(
              svgPath: 'assets/svg/panda-no-events.svg',
              title: L10n(context).noEventsTitle,
              subtitle: L10n(context).noEventsMessage,
              header: SizedBox(height: 32),
            );
          } else {
            body = AgendaList(snapshot.data, widget._day);
          }
        }

        return RefreshIndicator(
          child: body,
          onRefresh: _refresh,
        );
      },
    );
  }

  Future<void> _refresh() =>
      Provider.of<PlannerFetcher>(context).refreshItemsForDate(widget._day);
}

class AgendaList extends StatelessWidget {
  final Map<String, List<PlannerItem>> _agenda;
  final DateTime _selectedDate;
  final _tileHeight = 65.0;
  final _scrollController = ScrollController();

  AgendaList(this._agenda, this._selectedDate);

  @override
  Widget build(BuildContext context) {
    final _list = List();
    _agenda.forEach((key, value) {
      if (value.isNotEmpty) {
        _list.add(key);
        _list.addAll(value);
      }
    });
    final widget = ListView.builder(
      itemCount: _list.length,
      controller: _scrollController,
      itemBuilder: (context, index) {
        if (_list[index] is String) {
          return _dayHeader(context, _list[index], index);
        } else {
          return _dayTile(context, _list[index], index);
        }
      },
    );
    SchedulerBinding.instance.addPostFrameCallback((timeStamp) {
      _scrollToIndex(_list.indexOf(_dayKeyForDate(_selectedDate)));
    });

    return widget;
  }

  _scrollToIndex(i) => _scrollController.animateTo(i * _tileHeight,
      duration: Duration(milliseconds: 500), curve: Curves.fastOutSlowIn);

  Widget _dayTile(BuildContext context, PlannerItem plannerItem, int index) =>
      SizedBox(height: _tileHeight, child: CalendarDayListTile(plannerItem));

  Widget _dayHeader(BuildContext context, String title, int index) {
    final date = DateFormat("yyyy-MM-dd").parse(title);
    String formattedDate = DateFormat('EEEE, MM/dd/yyyy').format(date);
    return SizedBox(
      height: _tileHeight,
      child: ListTile(
        key: Key(title),
        title: Text(formattedDate),
        tileColor: Colors.black12,
      ),
    );
  }

  String _dayKeyForDate(DateTime date) =>
      _dayKeyForYearMonthDay(date.year, date.month, date.day);

  String _dayKeyForYearMonthDay(int year, int month, int day) =>
      '$year-$month-$day';
}
