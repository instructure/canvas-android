// Copyright (C) 2019 - present Instructure, Inc.
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
import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/course_grade.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/grading_period.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:flutter_parent/screens/courses/details/grading_period_modal.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

class CourseGradesScreen extends StatefulWidget {
  @override
  _CourseGradesScreenState createState() => _CourseGradesScreenState();
}

class _CourseGradesScreenState extends State<CourseGradesScreen> with AutomaticKeepAliveClientMixin {
  Set<String> _collapsedGroupIds;
  Future<GradeDetails> _detailsFuture;
  static final GlobalKey<RefreshIndicatorState> _refreshIndicatorKey = new GlobalKey<RefreshIndicatorState>();

  @override
  bool get wantKeepAlive => true;

  @override
  void initState() {
    super.initState();
    _collapsedGroupIds = Set();
  }

  @override
  Widget build(BuildContext context) {
    super.build(context); // Required super call for AutomaticKeepAliveClientMixin
    CourseDetailsModel.selectedTab = 0;
    return Consumer<CourseDetailsModel>(
      builder: (context, model, _) {
        // Initialize the future here if it wasn't set (we need the model, otherwise could've been done in initState)
        if (_detailsFuture == null) _detailsFuture = model.loadAssignments();
        return RefreshIndicator(
          key: _refreshIndicatorKey,
          onRefresh: () => _refresh(model),
          child: FutureBuilder(
            future: _detailsFuture,
            builder: (context, AsyncSnapshot<GradeDetails> snapshot) => _body(snapshot, model),
          ),
        );
      },
    );
  }

  Future<GradeDetails> _refresh(CourseDetailsModel model) {
    setState(() {
      _detailsFuture = model.loadAssignments(forceRefresh: model.forceRefresh);
      model.forceRefresh = true;
    });
    return _detailsFuture.catchError((_) {});
  }

  Widget _body(AsyncSnapshot<GradeDetails> snapshot, CourseDetailsModel model) {
    final header = _CourseGradeHeader(context, snapshot.data?.gradingPeriods ?? [], snapshot.data?.termEnrollment);
    if (snapshot.connectionState == ConnectionState.waiting && !snapshot.hasData) {
      return LoadingIndicator();
    }

    if (snapshot.hasError) {
      return ErrorPandaWidget(
        L10n(context).unexpectedError,
        () {
          _refreshIndicatorKey.currentState.show();
        },
      );
    } else if (!snapshot.hasData ||
        snapshot.data.assignmentGroups == null ||
        snapshot.data.assignmentGroups.every((group) => group.assignments.isEmpty) == true) {
      return EmptyPandaWidget(
        svgPath: 'assets/svg/panda-space-no-assignments.svg',
        title: L10n(context).noAssignmentsTitle,
        subtitle: L10n(context).noAssignmentsMessage,
        // Don't show the header if we have no assignments at all (null grading period id corresponds to all grading periods)
        header: (model.currentGradingPeriod()?.id == null) ? null : header,
      );
    }

    return ListView(
      children: [
        header,
        ..._assignmentListChildren(context, snapshot.data.assignmentGroups),
      ],
    );
  }

  List<Widget> _assignmentListChildren(BuildContext context, List<AssignmentGroup> groups) {
    final children = List<Widget>();

    for (AssignmentGroup group in groups) {
      if (group.assignments.length == 0) continue; // Don't show empty assignment groups

      final isCollapsed = _collapsedGroupIds.contains(group.id);

      final tile = Theme(
        data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
        child: ListTileTheme(
          contentPadding: EdgeInsets.all(0),
          child: ExpansionTile(
            key: Key('assignmentGroup ${group.id}'),
            initiallyExpanded: !isCollapsed,
            onExpansionChanged: (expanding) {
              setState(() => expanding ? _collapsedGroupIds.remove(group.id) : _collapsedGroupIds.add(group.id));
            },
            title: Padding(
              padding: const EdgeInsetsDirectional.only(top: 16, start: 16, end: 16),
              child: Text(group.name, style: Theme.of(context).textTheme.overline),
            ),
            trailing: Padding(
              padding: const EdgeInsetsDirectional.only(top: 16, start: 16, end: 16),
              child: Icon(
                isCollapsed ? CanvasIcons.mini_arrow_down : CanvasIcons.mini_arrow_up,
                color: Theme.of(context).textTheme.overline.color,
                semanticLabel: isCollapsed ? L10n(context).allyCollapsed : L10n(context).allyExpanded,
              ),
            ),
            children: <Widget>[
              ...(group.assignments.toList()..sort((a, b) => a.position.compareTo(b.position)))
                  .map((assignment) => _AssignmentRow(assignment: assignment))
            ],
          ),
        ),
      );

      children.add(tile);
    }

    children.add(SizedBox(height: 64)); // Add a fab height at the bottom so we can see all the content

    return children;
  }
}

class _CourseGradeHeader extends StatelessWidget {
  final List<GradingPeriod> gradingPeriods;
  final Enrollment termEnrollment;

  _CourseGradeHeader(BuildContext context, List<GradingPeriod> gradingPeriods, this.termEnrollment, {Key key})
      : this.gradingPeriods = [GradingPeriod((b) => b..title = L10n(context).allGradingPeriods)] + gradingPeriods,
        super(key: key);

  @override
  Widget build(BuildContext context) {
    final model = Provider.of<CourseDetailsModel>(context, listen: false);
    final gradingPeriodHeader = _gradingPeriodHeader(context, model);
    final gradeTotalHeader = _gradeTotal(context, model);

    return Column(
      children: [
        if (gradingPeriodHeader != null || gradeTotalHeader != null) SizedBox(height: 16),
        if (gradingPeriodHeader != null) gradingPeriodHeader,
        if (gradingPeriodHeader != null && gradeTotalHeader != null) SizedBox(height: 4),
        if (gradeTotalHeader != null) gradeTotalHeader,
        if (gradingPeriodHeader != null || gradeTotalHeader != null) SizedBox(height: 8),
      ],
    );
  }

  Widget _gradingPeriodHeader(BuildContext context, CourseDetailsModel model) {
    // Don't show this if there's no grading periods (we always add 1 for 'All grading periods')
    if (gradingPeriods.length <= 1) return null;

    final studentColor = ParentTheme.of(context).studentColor;

    final gradingPeriod = model.currentGradingPeriod() ?? gradingPeriods.first;

    return Padding(
      // Only left padding, lets the filter button go past the margin for the ripple
      padding: const EdgeInsetsDirectional.only(start: 16),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        crossAxisAlignment: CrossAxisAlignment.baseline,
        textBaseline: TextBaseline.ideographic,
        children: <Widget>[
          Text(gradingPeriod.title, style: Theme.of(context).textTheme.display1),
          InkWell(
            child: ConstrainedBox(
              constraints: BoxConstraints(minHeight: 48, minWidth: 48), // For a11y
              child: Align(
                alignment: Alignment.bottomRight,
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  child: Text(
                    L10n(context).filter,
                    style: Theme.of(context).textTheme.caption.copyWith(color: studentColor),
                  ),
                ),
              ),
            ),
            onTap: () async {
              final gradingPeriod = await GradingPeriodModal.asBottomSheet(context, gradingPeriods);
              if (gradingPeriod != null) model.updateGradingPeriod(gradingPeriod);

              // Don't force refresh when switching grading periods
              model.forceRefresh = false;
              _CourseGradesScreenState._refreshIndicatorKey.currentState.show();
            },
          ),
        ],
      ),
    );
  }

  /// The total grade in the course/grading period
  Widget _gradeTotal(BuildContext context, CourseDetailsModel model) {
    final grade = model.course.getCourseGrade(
      model.student.id,
      enrollment: termEnrollment,
      gradingPeriodId: model.currentGradingPeriod()?.id,
      forceAllPeriods: termEnrollment == null && model.currentGradingPeriod()?.id == null,
    );

    // Don't show the total if the grade is locked
    if (grade.isCourseGradeLocked(forAllGradingPeriods: model.currentGradingPeriod()?.id == null)) return null;

    final textTheme = Theme.of(context).textTheme;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: <Widget>[
          Text(L10n(context).courseTotalGradeLabel, style: textTheme.body1),
          Text(_courseGrade(context, grade), style: textTheme.body1, key: Key("total_grade")),
        ],
      ),
    );
  }

  String _courseGrade(BuildContext context, CourseGrade grade) {
    final format = NumberFormat.percentPattern();
    format.maximumFractionDigits = 2;

    if (grade.noCurrentGrade()) {
      return L10n(context).noGrade;
    } else {
      return grade.currentGrade()?.isNotEmpty == true
          ? grade.currentGrade()
          : format.format(grade.currentScore() / 100); // format multiplies by 100 for percentages
    }
  }
}

class _AssignmentRow extends StatelessWidget {
  final Assignment assignment;

  const _AssignmentRow({Key key, this.assignment}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final model = Provider.of<CourseDetailsModel>(context, listen: false);
    final studentId = model.student.id;

    final textTheme = Theme.of(context).textTheme;
    final assignmentStatus = _assignmentStatus(context, assignment, studentId);

    return InkWell(
      onTap: () =>
          locator<QuickNav>().pushRoute(context, PandaRouter.assignmentDetails(assignment.courseId, assignment.id)),
      child: Padding(
        padding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          key: Key("assignment_${assignment.id}_row"),
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Container(
              padding: EdgeInsets.only(top: 4),
              width: 20,
              child: Icon(CanvasIcons.assignment, size: 20, color: ParentTheme.of(context).studentColor),
            ),
            SizedBox(width: 32),
            Expanded(
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: <Widget>[
                  Expanded(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: <Widget>[
                        Text(assignment.name, style: textTheme.subhead, key: Key("assignment_${assignment.id}_name")),
                        SizedBox(height: 2),
                        Text(_formatDate(context, assignment.dueAt),
                            style: textTheme.caption, key: Key("assignment_${assignment.id}_dueAt")),
                        if (assignmentStatus != null) SizedBox(height: 4),
                        if (assignmentStatus != null) assignmentStatus,
                      ],
                    ),
                  ),
                  SizedBox(width: 16),
                  _assignmentGrade(context, assignment, studentId),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _assignmentStatus(BuildContext context, Assignment assignment, String studentId) {
    final localizations = L10n(context);
    final textTheme = Theme.of(context).textTheme;
    final status = assignment.getStatus(studentId: studentId);
    final key = Key("assignment_${assignment.id}_status");
    switch (status) {
      case SubmissionStatus.NONE:
        return null; // An 'invisible' status, just don't show anything
      case SubmissionStatus.LATE:
        return Text(localizations.assignmentLateSubmittedLabel,
            style: textTheme.caption.copyWith(
              // Late will be orange, regardless of the current student
              color: ParentTheme.of(context).getColorVariantForCurrentState(StudentColorSet.fire),
            ),
            key: key);
      case SubmissionStatus.MISSING:
        return Text(localizations.assignmentMissingSubmittedLabel,
            style: textTheme.caption.copyWith(color: ParentColors.failure), key: key);
      case SubmissionStatus.SUBMITTED:
        return Text(localizations.assignmentSubmittedLabel, style: textTheme.caption, key: key);
      case SubmissionStatus.NOT_SUBMITTED:
        return Text(localizations.assignmentNotSubmittedLabel, style: textTheme.caption, key: key);
      default:
        return null;
    }
  }

  Widget _assignmentGrade(BuildContext context, Assignment assignment, String studentId) {
    dynamic points = assignment.pointsPossible;

    // Store the points as an int if possible
    if (points.toInt() == points) {
      points = points.toInt().toString();
    } else {
      points = points.toString();
    }

    String text, semantics;
    final localizations = L10n(context);

    final submission = assignment.submission(studentId);
    if (submission?.grade != null) {
      text = localizations.gradeFormatScoreOutOfPointsPossible(submission.grade, points);
      semantics = localizations.contentDescriptionScoreOutOfPointsPossible(submission.grade, points);
    } else {
      text = localizations.gradeFormatScoreOutOfPointsPossible(localizations.assignmentNoScore, points);
      semantics = localizations.contentDescriptionScoreOutOfPointsPossible('', points); // Read as "out of x points"
    }

    return Text(text,
        semanticsLabel: semantics,
        style: Theme.of(context).textTheme.subhead,
        key: Key("assignment_${assignment.id}_grade"));
  }

  String _formatDate(BuildContext context, DateTime date) {
    final l10n = L10n(context);
    return date.l10nFormat(l10n.dueDateAtTime) ?? l10n.noDueDate;
  }
}
