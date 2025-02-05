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
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_interactor.dart';
import 'package:flutter_parent/screens/assignments/grade_cell.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_screen.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/html_description_tile.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/permission_handler.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_svg/svg.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../network/utils/analytics.dart';
import '../../utils/veneers/flutter_snackbar_veneer.dart';

class AssignmentDetailsScreen extends StatefulWidget {
  final String courseId;
  final String assignmentId;

  const AssignmentDetailsScreen({
    required this.courseId,
    required this.assignmentId,
    super.key
  });

  @override
  _AssignmentDetailsScreenState createState() => _AssignmentDetailsScreenState();
}

class _AssignmentDetailsScreenState extends State<AssignmentDetailsScreen> {
  GlobalKey<RefreshIndicatorState> _refreshKey = GlobalKey<RefreshIndicatorState>();

  // State variables
  late Future<AssignmentDetails?> _assignmentFuture;
  late Future<Reminder?> _reminderFuture;
  Future<void>? _animationFuture;
  User? _currentStudent;

  @override
  void initState() {
    _currentStudent = ApiPrefs.getCurrentStudent();
    _reminderFuture = _loadReminder();
    _assignmentFuture = _loadAssignment();
    super.initState();
  }

  AssignmentDetailsInteractor get _interactor => locator<AssignmentDetailsInteractor>();

  Future<AssignmentDetails?> _loadAssignment({bool forceRefresh = false}) => _interactor.loadAssignmentDetails(
        forceRefresh,
        widget.courseId,
        widget.assignmentId,
        _currentStudent?.id,
      );

  Future<Reminder?> _loadReminder() => _interactor.loadReminder(widget.assignmentId);

  PermissionHandler get _permissionHandler => locator<PermissionHandler>();

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _assignmentFuture,
      builder: (context, AsyncSnapshot<AssignmentDetails?> snapshot) => Scaffold(
        appBar: _appBar(snapshot),
        floatingActionButton: snapshot.hasData && snapshot.data?.assignment != null ? _fab(snapshot) : null,
        body: RefreshIndicator(
          key: _refreshKey,
          child: _body(snapshot),
          onRefresh: () {
            setState(() {
              _assignmentFuture = _loadAssignment(forceRefresh: true);
              _reminderFuture = _loadReminder();
            });
            return _assignmentFuture.catchError((_) {}); // Catch errors so they don't crash the app
          },
        ),
      ),
    );
  }

  AppBar _appBar(AsyncSnapshot<AssignmentDetails?> snapshot) => AppBar(
        bottom: ParentTheme.of(context)?.appBarDivider(),
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(L10n(context).assignmentDetailsTitle),
            if (snapshot.hasData)
              Text(snapshot.data?.course?.name ?? '',
                  style: Theme.of(context).primaryTextTheme.bodySmall?.copyWith(color: Colors.white), key: Key("assignment_details_coursename")),
          ],
        ),
      );

  Widget _fab(AsyncSnapshot<AssignmentDetails?> snapshot) {
    return FloatingActionButton(
      onPressed: () => _sendMessage(snapshot.data!),
      tooltip: L10n(context).assignmentMessageHint,
      child: Padding(padding: const EdgeInsets.only(left: 4, top: 4), child: Icon(CanvasIconsSolid.comment)),
    );
  }

  Widget _body(AsyncSnapshot<AssignmentDetails?> snapshot) {
    if (snapshot.hasError) {
      return ErrorPandaWidget(
        L10n(context).unexpectedError,
        () => _refreshKey.currentState?.show(),
      );
    }

    if (!snapshot.hasData) return LoadingIndicator();

    // Load the content
    if (_animationFuture == null) _animationFuture = Future.delayed(Duration(milliseconds: 1000));
    final textTheme = Theme.of(context).textTheme;

    final l10n = L10n(context);
    final course = snapshot.data?.course;
    final restrictQuantitativeData = course?.settings?.restrictQuantitativeData ?? false;
    final assignment = snapshot.data!.assignment!;
    final assignmentEnhancementEnabled = snapshot.data?.assignmentEnhancementEnabled ?? false;
    final submission = assignment.submission(_currentStudent?.id);
    final fullyLocked = assignment.isFullyLocked;
    final missing = submission?.missing == true;
    final showStatus = missing || assignment.isSubmittable() || submission?.isGraded() == true;
    final submitted = submission?.submittedAt != null;
    final graded = submission?.isGraded() == true;
    final submittedColor = submitted || graded ? ParentTheme.of(context)?.successColor : textTheme.bodySmall?.color;

    final points = (assignment.pointsPossible.toInt() == assignment.pointsPossible)
        ? assignment.pointsPossible.toInt().toString()
        : assignment.pointsPossible.toString();

    return SingleChildScrollView(
      physics: AlwaysScrollableScrollPhysics(),
      padding: EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          ..._rowTile(
            title: assignment.name ?? '',
            titleStyle: textTheme.headlineMedium!,
            child: Row(
              children: <Widget>[
                if (!restrictQuantitativeData)
                  Text(l10n.assignmentTotalPoints(points),
                      style: textTheme.bodySmall,
                      semanticsLabel: l10n.assignmentTotalPointsAccessible(points),
                      key: Key("assignment_details_total_points")),
                if (showStatus && !restrictQuantitativeData) SizedBox(width: 16),
                if (showStatus) _statusIcon(submitted || graded, submittedColor!),
                if (showStatus) SizedBox(width: 8),
                if (showStatus)
                  Text(
                      missing ? l10n.assignmentMissingSubmittedLabel :
                      graded ? l10n.assignmentGradedLabel :
                      submitted ? l10n.assignmentSubmittedLabel : l10n.assignmentNotSubmittedLabel,
                      style: textTheme.bodySmall?.copyWith(
                        color: submittedColor,
                      ),
                      key: Key("assignment_details_status")),
              ],
            ),
          ),
          Divider(),
          Padding(
              padding: const EdgeInsets.only(top: 16.0, bottom: 16.0),
              child: OutlinedButton(
                  onPressed: () {
                    _onSubmissionAndRubricClicked(
                      assignment.htmlUrl,
                      assignmentEnhancementEnabled,
                      l10n.submission,
                    );
                  },
                  child: Align(
                      alignment: Alignment.center,
                      child: Row(mainAxisSize: MainAxisSize.min, children: [
                        Text(
                          l10n.submissionAndRubric,
                          style: textTheme.titleMedium?.copyWith(color: ParentTheme.of(context)?.studentColor),
                        ),
                        SizedBox(width: 6),
                        Icon(CanvasIconsSolid.arrow_open_right, color: ParentTheme.of(context)?.studentColor, size: 14)
                      ])),
                  style: OutlinedButton.styleFrom(
                    minimumSize: Size(double.infinity, 48),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(6.0),
                    ),
                    side: BorderSide(width: 0.5, color: ParentTheme.of(context)?.onSurfaceColor ?? Colors.grey),
                  ))),
          if (!fullyLocked) ...[
            Divider(),
            ..._rowTile(
              title: l10n.assignmentDueLabel,
              child: Text(_dateFormat(assignment.dueAt?.toLocal()) ?? l10n.noDueDate,
                  style: textTheme.titleMedium, key: Key("assignment_details_due_date")),
            ),
          ],
          GradeCell.forSubmission(context, course, assignment, submission),
          ..._lockedRow(assignment),
          Divider(),
          ..._rowTile(
            title: l10n.assignmentRemindMeLabel,
            child: FutureBuilder(
              future: _reminderFuture,
              builder: (BuildContext context, AsyncSnapshot<Reminder?> snapshot) {
                Reminder? reminder = snapshot.data;
                return SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  value: reminder != null,
                  title: Text(
                    reminder?.date == null
                        ? L10n(context).assignmentRemindMeDescription
                        : L10n(context).assignmentRemindMeSet,
                    style: textTheme.titleMedium,
                  ),
                  subtitle: reminder == null
                      ? null
                      : Padding(
                          padding: const EdgeInsets.only(top: 8),
                          child: Text(
                            _dateFormat(reminder.date?.toLocal()) ?? '',
                            style: textTheme.titleMedium?.copyWith(color: ParentTheme.of(context)?.studentColor),
                          ),
                        ),
                  onChanged: (checked) => _handleAlarmSwitch(context, assignment, checked, reminder),
                );
              },
            ),
          ),
          Divider(),
          _descriptionContent(assignment, fullyLocked),
          if (!fullyLocked)
            Divider(),
          // TODO: Add in 'Learn more' feature
//        ..._rowTile(
//          title: l10n.assignmentLearnMoreLabel,
//          child: Text('nothing to learn here'),
//        ),
          SizedBox(height: 48), // Fab size at bottom to see whole description
        ],
      ),
    );
  }

  Widget _descriptionContent(Assignment assignment, bool fullyLocked) {
    final l10n = L10n(context);

    if (fullyLocked) {
      return FutureBuilder(
        future: _animationFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return Padding(
              padding: const EdgeInsets.only(top: 16.0),
              child: LoadingIndicator(),
            );
          }

          // No good way to center this image vertically in a scrollable view's remaining space. Settling for padding for now
          return Padding(
            padding: const EdgeInsets.only(top: 32),
            child: Center(child: SvgPicture.asset('assets/svg/panda-locked.svg', excludeFromSemantics: true)),
          );
        },
      );
    } else {
      return HtmlDescriptionTile(
        html: assignment.description,
        emptyDescription: l10n.noDescriptionBody,
        descriptionTitle: assignment.submissionTypes?.contains(SubmissionTypes.onlineQuiz) == true
            ? l10n.assignmentInstructionsLabel
            : l10n.descriptionTitle,
      );
    }
  }

  Widget _statusIcon(bool submitted, Color submittedColor) {
    if (!submitted) return Icon(Icons.do_not_disturb, color: submittedColor, size: 18);
    return Container(
      width: 18,
      height: 18,
      decoration: BoxDecoration(shape: BoxShape.circle, color: submittedColor),
      child: Icon(CanvasIconsSolid.check, color: Theme.of(context).colorScheme.onPrimary, size: 8),
    );
  }

  List<Widget> _rowTile({String? title, TextStyle? titleStyle, Widget? child}) {
    return [
      SizedBox(height: 16),
      Text(title ?? '', style: titleStyle ?? Theme.of(context).textTheme.labelSmall),
      SizedBox(height: 8),
      if (child != null) child,
      SizedBox(height: 16),
    ];
  }

  List<Widget> _lockedRow(Assignment assignment) {
    String? message = null;
    if (assignment.lockInfo?.hasModuleName == true) {
      message = L10n(context).assignmentLockedModule(assignment.lockInfo!.contextModule!.name!);
    } else if (assignment.isFullyLocked ||
        (assignment.lockExplanation?.isNotEmpty == true && assignment.lockAt?.isBefore(DateTime.now()) == true)) {
      message = assignment.lockExplanation;
    }

    if (message?.isNotEmpty != true) return [];

    return [
      Divider(),
      ..._rowTile(
        title: L10n(context).assignmentLockLabel,
        child: Text(message!, style: Theme.of(context).textTheme.titleMedium),
      ),
    ];
  }

  String? _dateFormat(DateTime? time) => time?.l10nFormat(L10n(context).dateAtTime);

  _handleAlarmSwitch(BuildContext context, Assignment assignment, bool checked, Reminder? reminder) async {
    if (reminder != null) await _interactor.deleteReminder(reminder);
    if (checked) {
      var now = DateTime.now();
      var initialDate = assignment.dueAt?.isAfter(now) == true ? assignment.dueAt!.toLocal() : now;

      DateTime? date;
      TimeOfDay? time;

      final permissionResult = await _permissionHandler.checkPermissionStatus(Permission.scheduleExactAlarm);
      if (permissionResult != PermissionStatus.granted) {
        final permissionGranted = await locator<NotificationUtil>().requestScheduleExactAlarmPermission();
        if (permissionGranted != true) {
          locator<FlutterSnackbarVeneer>().showSnackBar(context, L10n(context).needToEnablePermission);
          return;
        }
      }

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
        var body = assignment.dueAt?.l10nFormat(L10n(context).dueDateAtTime) ?? L10n(context).noDueDate;
        await _interactor.createReminder(
          L10n(context),
          reminderDate,
          assignment.id,
          assignment.courseId,
          assignment.name ?? '',
          body,
        );
      }
    }

    // Perform refresh
    setState(() {
      _reminderFuture = _loadReminder();
    });
  }

  _sendMessage(AssignmentDetails details) {
    if (_currentStudent != null) {
      String subject = L10n(context).assignmentSubjectMessage(
          _currentStudent?.name ?? '', details.assignment?.name ?? '');
      String postscript = L10n(context).messageLinkPostscript(
          _currentStudent?.name ?? '', details.assignment?.htmlUrl ?? '');
      Widget screen = CreateConversationScreen(
          widget.courseId, _currentStudent!.id, subject, postscript);
      locator.get<QuickNav>().push(context, screen);
    }
  }

  _onSubmissionAndRubricClicked(String? assignmentUrl, bool assignmentEnhancementEnabled, String title) async {
    if (assignmentUrl == null) return;
    final parentId = ApiPrefs.getUser()?.id ?? 0;
    final currentStudentId = _currentStudent?.id ?? 0;
    final url = assignmentEnhancementEnabled ? assignmentUrl : assignmentUrl + "/submissions/$currentStudentId";
    locator<QuickNav>().pushRoute(context, PandaRouter.submissionWebViewRoute(
        await locator<WebContentInteractor>().getAuthUrl(url),
        title,
        {"k5_observed_user_for_$parentId": "$currentStudentId"},
        false
    ));
    locator<Analytics>().logEvent(AnalyticsEventConstants.SUBMISSION_AND_RUBRIC_INTERACTION);
  }
}
