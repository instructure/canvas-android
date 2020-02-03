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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/screens/events/event_details_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/constrained_web_view.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart';

class EventDetailsScreen extends StatefulWidget {
  final ScheduleItem event;
  final String eventId;

  EventDetailsScreen.withEvent({Key key, this.event})
      : assert(event != null),
        eventId = event.id,
        super(key: key);

  EventDetailsScreen.withId({Key key, this.eventId})
      : assert(eventId != null),
        event = null,
        super(key: key);

  @override
  _EventDetailsScreenState createState() => _EventDetailsScreenState();
}

class _EventDetailsScreenState extends State<EventDetailsScreen> {
  GlobalKey<RefreshIndicatorState> _refreshKey = GlobalKey<RefreshIndicatorState>();

  Future<ScheduleItem> _eventFuture;

  Future<ScheduleItem> _loadEvent({bool forceRefresh = false}) => _interactor.loadEvent(widget.eventId, forceRefresh);

  EventDetailsInteractor get _interactor => locator<EventDetailsInteractor>();

  @override
  void initState() {
    if (widget.event != null) {
      _eventFuture = Future.value(widget.event);
    } else {
      _eventFuture = _loadEvent();
    }
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return RefreshIndicator(
      key: _refreshKey,
      onRefresh: () {
        setState(() {
          _eventFuture = _loadEvent(forceRefresh: true);
        });
        return _eventFuture.catchError((_) {});
      },
      child: FutureBuilder(
        future: _eventFuture,
        builder: (context, AsyncSnapshot<ScheduleItem> snapshot) {
          if (snapshot.hasError) {
            return ErrorPandaWidget(
              L10n(context).unexpectedError,
              () => _refreshKey.currentState.show(),
            );
          } else if (!snapshot.hasData) {
            return LoadingIndicator();
          } else {
            return _EventDetails(snapshot.data);
          }
        },
      ),
    );
  }
}

class _EventDetails extends StatelessWidget {
  final ScheduleItem event;

  const _EventDetails(this.event, {Key key})
      : assert(event != null),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    final l10n = L10n(context);

    // Get the date strings
    String dateLine1, dateLine2;
    final date = event.startAt ?? event.endAt;
    if (event.isAllDay) {
      dateLine1 = l10n.eventAllDayLabel;
      dateLine2 = _dateFormat(date);
    } else if (event.startAt != null && event.endAt != null && event.startAt != event.endAt) {
      dateLine1 = _dateFormat(date);
      dateLine2 = l10n.eventTime(_timeFormat(event.startAt), _timeFormat(event.endAt));
    } else {
      dateLine1 = _dateFormat(date);
      dateLine2 = _timeFormat(date);
    }

    // Get the location strings
    String locationLine1, locationLine2;
    if ((event.locationAddress == null || event.locationAddress.isEmpty) &&
        (event.locationName == null || event.locationName.isEmpty)) {
      locationLine1 = l10n.eventNoLocation;
    } else if (event.locationName == null || event.locationName.isEmpty) {
      locationLine1 = event.locationAddress;
    } else {
      locationLine1 = event.locationName;
      locationLine2 = event.locationAddress;
    }

    return Scaffold(
      appBar: AppBar(title: Text(event.title ?? l10n.eventDetailsDefaultTitle)),
      body: ListView(
        children: [
          SizedBox(height: 20),
          _SimpleTile(
            leading: Icon(Icons.timer),
            line1: dateLine1,
            line2: dateLine2,
          ),
          SizedBox(height: 20),
          _SimpleTile(
            leading: Icon(Icons.location_on),
            line1: locationLine1,
            line2: locationLine2,
          ),
          SizedBox(height: 16),
          ConstrainedWebView(content: event.description, horizontalPadding: 10),
        ],
      ),
    );
  }

  String _dateFormat(DateTime time) {
    return time == null ? null : DateFormat.EEEE().add_yMMMd().format(time.toLocal());
  }

  String _timeFormat(DateTime time) {
    return time == null ? null : DateFormat.jm().format(time.toLocal());
  }
}

class _SimpleTile extends StatelessWidget {
  final Widget leading;
  final String line1;
  final String line2;

  const _SimpleTile({Key key, this.leading, this.line1, this.line2}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final style = Theme.of(context).textTheme.subhead;

    return Row(
      // If wwe only have one line, center the content. Otherwise align start
      crossAxisAlignment: (line1?.isNotEmpty == true && line2?.isNotEmpty == true)
          ? CrossAxisAlignment.start
          : CrossAxisAlignment.center,
      children: [
        SizedBox(width: 16),
        leading,
        SizedBox(width: 16),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              if (line1?.isNotEmpty == true) Text(line1, style: style),
              if (line1?.isNotEmpty == true && line2?.isNotEmpty == true) SizedBox(height: 8),
              if (line2?.isNotEmpty == true) Text(line2, style: style),
            ],
          ),
        ),
        SizedBox(width: 16),
      ],
    );
  }
}
