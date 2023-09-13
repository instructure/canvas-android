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
import 'package:collection/collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/events/event_details_interactor.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_screen.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/html_description_tile.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart';

class EventDetailsScreen extends StatefulWidget {
  final ScheduleItem? event;
  final String? eventId;

  // Course ID is used for messaging. The message FAB will not be shown if it or current student is null.
  final String? courseId;

  EventDetailsScreen.withEvent({
    required this.event,
    this.courseId,
    super.key
  }) : assert(event != null),
      eventId = event!.id;

  EventDetailsScreen.withId({
    required this.eventId,
    this.courseId,
    super.key
  }) :
      assert(eventId != null),
      event = null;

  @override
  _EventDetailsScreenState createState() => _EventDetailsScreenState();
}

class _EventDetailsScreenState extends State<EventDetailsScreen> {
  GlobalKey<RefreshIndicatorState> _refreshKey = GlobalKey<RefreshIndicatorState>();

  late Future<ScheduleItem?> _eventFuture;

  Future<ScheduleItem?> _loadEvent({bool forceRefresh = false}) => _interactor.loadEvent(widget.eventId, forceRefresh);

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
    return FutureBuilder(
      future: _eventFuture,
      builder: (context, AsyncSnapshot<ScheduleItem?> snapshot) {
        return Scaffold(
          appBar: AppBar(title: Text(L10n(context).eventDetailsTitle)),
          floatingActionButton: _fab(snapshot),
          body: RefreshIndicator(
            key: _refreshKey,
            onRefresh: () {
              setState(() {
                _eventFuture = _loadEvent(forceRefresh: true);
              });
              return _eventFuture.catchError((_) {});
            },
            child: _body(context, snapshot),
          ),
        );
      },
    );
  }

  Widget _body(BuildContext context, AsyncSnapshot<ScheduleItem?> snapshot) {
    if (snapshot.hasError) {
      return ErrorPandaWidget(
        L10n(context).unexpectedError,
        () => _refreshKey.currentState?.show(),
      );
    } else if (!snapshot.hasData) {
      return LoadingIndicator();
    } else {
      return _EventDetails(snapshot.data, widget.courseId);
    }
  }

  Widget? _fab(AsyncSnapshot<ScheduleItem?> snapshot) {
    User? student = ApiPrefs.getCurrentStudent();
    if (!snapshot.hasData || widget.courseId == null || student?.id == null || student?.name == null) {
      // The data hasn't loaded, or course/student info is missing (e.g. if we deep linked to this page)
      return null;
    }

    return FloatingActionButton(
      tooltip: L10n(context).assignmentMessageHint,
      child: Padding(padding: const EdgeInsets.only(left: 4, top: 4), child: Icon(CanvasIconsSolid.comment)),
      onPressed: () {
        final event = snapshot.data!;
        String subject = L10n(context).eventSubjectMessage(student!.name, event.title!);
        String postscript = L10n(context).messageLinkPostscript(student.name, event.htmlUrl ?? '');
        Widget screen = CreateConversationScreen(
          widget.courseId!,
          student.id,
          subject,
          postscript,
        );
        locator.get<QuickNav>().push(context, screen);
      },
    );
  }
}

class _EventDetails extends StatelessWidget {
  final ScheduleItem? event;
  final String? courseId;

  const _EventDetails(this.event, this.courseId, {super.key});

  @override
  Widget build(BuildContext context) {
    final l10n = L10n(context);

    // Get the date strings
    String? dateLine1, dateLine2;
    final date = event?.startAt ?? event?.endAt;
    if (event?.isAllDay == true) {
      dateLine1 = _dateFormat(date);
      dateLine2 = '';
    } else if (event?.startAt != null && event?.endAt != null && event?.startAt != event?.endAt) {
      dateLine1 = _dateFormat(date);
      dateLine2 = l10n.eventTime(_timeFormat(event?.startAt!)!, _timeFormat(event?.endAt!)!);
    } else {
      dateLine1 = _dateFormat(date);
      dateLine2 = _timeFormat(date);
    }

    // Get the location strings
    String? locationLine1, locationLine2;
    if ((event?.locationAddress == null || event?.locationAddress?.isEmpty == true) &&
        (event?.locationName == null || event?.locationName?.isEmpty == true)) {
      locationLine1 = l10n.eventNoLocation;
    } else if (event?.locationName == null || event?.locationName?.isEmpty == true) {
      locationLine1 = event?.locationAddress ?? '';
    } else {
      locationLine1 = event?.locationName ?? '';
      locationLine2 = event?.locationAddress ?? '';
    }

    final textTheme = Theme.of(context).textTheme;

    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
      children: [
        SizedBox(height: 16),
        Text(event?.title ?? '', style: textTheme.headlineMedium, key: ValueKey('event_details_title')),
        SizedBox(height: 16),
        Divider(),
        _SimpleTile(label: l10n.eventDateLabel, line1: dateLine1, line2: dateLine2, keyPrefix: 'event_details_date'),
        Divider(),
        _SimpleTile(
            label: l10n.eventLocationLabel,
            line1: locationLine1,
            line2: locationLine2,
            keyPrefix: 'event_details_location'),
        Divider(),
        _SimpleHeader(label: l10n.assignmentRemindMeLabel),
        _RemindMe(event, courseId, <String?>[dateLine1, dateLine2].nonNulls.where((d) => d.isNotEmpty).join('\n')),
        Divider(),
        HtmlDescriptionTile(html: event?.description),
        // Don't show the bottom divider if there's no content (no empty message shown either)
        if (event?.description != null && event?.description?.isNotEmpty == true)
          Divider(),
      ],
    );
  }

  String? _dateFormat(DateTime? time) {
    return time == null ? null : DateFormat.EEEE(supportedDateLocale).add_yMMMd().format(time.toLocal());
  }

  String? _timeFormat(DateTime? time) {
    return time == null ? null : DateFormat.jm(supportedDateLocale).format(time.toLocal());
  }
}

class _RemindMe extends StatefulWidget {
  final ScheduleItem? event;
  final String? courseId;
  final String formattedDate;

  const _RemindMe(this.event, this.courseId, this.formattedDate, {super.key});

  @override
  _RemindMeState createState() => _RemindMeState();
}

class _RemindMeState extends State<_RemindMe> {
  late Future<Reminder?> _reminderFuture;

  EventDetailsInteractor _interactor = locator<EventDetailsInteractor>();

  Future<Reminder?> _loadReminder() => _interactor.loadReminder(widget.event?.id);

  @override
  void initState() {
    _reminderFuture = _loadReminder();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    TextTheme textTheme = Theme.of(context).textTheme;
    return FutureBuilder(
      future: _reminderFuture,
      builder: (BuildContext context, AsyncSnapshot<Reminder?> snapshot) {
        Reminder? reminder = snapshot.data;
        return SwitchListTile(
          contentPadding: EdgeInsets.zero,
          value: reminder != null,
          title: Text(
            reminder?.date == null ? L10n(context).eventRemindMeDescription : L10n(context).eventRemindMeSet,
            style: textTheme.titleMedium,
          ),
          subtitle: reminder == null
              ? null
              : Padding(
                  padding: const EdgeInsets.only(top: 8),
                  child: Text(
                    reminder.date.l10nFormat(L10n(context).dateAtTime)!,
                    style: textTheme.titleMedium?.copyWith(color: ParentTheme.of(context)?.studentColor),
                  ),
                ),
          onChanged: (checked) => _handleAlarmSwitch(context, widget.event, checked, reminder, widget.formattedDate)
        );
      },
    );
  }

  _handleAlarmSwitch(
    BuildContext context,
    ScheduleItem? event,
    bool checked,
    Reminder? reminder,
    String formattedDate,
  ) async {
    if (reminder != null) _interactor.deleteReminder(reminder);
    if (checked) {
      var now = DateTime.now();
      var eventDate = event?.isAllDay == true ? event?.allDayDate?.toLocal() : event?.startAt?.toLocal();
      var initialDate = eventDate?.isAfter(now) == true ? eventDate! : now;

      DateTime? date;
      TimeOfDay? time;

      date = await showDatePicker(
        context: context,
        initialDate: initialDate,
        firstDate: now,
        lastDate: initialDate.add(Duration(days: 365)),
      );
      if (date != null) {
        time = await showTimePicker(context: context, initialTime: TimeOfDay.fromDateTime(initialDate));
      }

      if (date != null && time != null) {
        DateTime reminderDate = DateTime(date.year, date.month, date.day, time.hour, time.minute);
        await _interactor.createReminder(
          L10n(context),
          reminderDate,
          event?.id,
          widget.courseId,
          event?.title,
          formattedDate,
        );
      }
    }

    // Perform refresh
    setState(() {
      _reminderFuture = _loadReminder();
    });
  }
}

class _SimpleTile extends StatelessWidget {
  final String label, keyPrefix;
  final String? line1, line2;

  const _SimpleTile({required this.label, this.line1, this.line2, required this.keyPrefix, super.key});

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _SimpleHeader(label: label),
        Text(line1 ?? '', style: textTheme.titleMedium, key: ValueKey('${keyPrefix}_line1')),
        if (line2 != null) SizedBox(height: 8),
        if (line2 != null) Text(line2!, style: textTheme.titleMedium, key: ValueKey('${keyPrefix}_line2')),
        SizedBox(height: 16),
      ],
    );
  }
}

class _SimpleHeader extends StatelessWidget {
  final String label;

  const _SimpleHeader({required this.label, super.key});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        SizedBox(height: 16),
        Text(label, style: Theme.of(context).textTheme.labelSmall),
        SizedBox(height: 8),
      ],
    );
  }
}
