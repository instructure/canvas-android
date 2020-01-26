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
import 'package:flutter_parent/screens/assignments/assignment_details_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:flutter_parent/screens/under_construction_screen.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
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

class _CourseGradesScreenState extends State<CourseGradesScreen> {
  Set<String> _collapsedGroupIds;
  final GlobalKey<RefreshIndicatorState> _refreshIndicatorKey = new GlobalKey<RefreshIndicatorState>();

  @override
  void initState() {
    super.initState();
    _collapsedGroupIds = Set();
  }

  @override
  Widget build(BuildContext context) {
    CourseDetailsModel.selectedTab = 0;
    return Consumer<CourseDetailsModel>(
      builder: (context, model, _) {
        return RefreshIndicator(
          key: _refreshIndicatorKey,
          onRefresh: () => model.loadData(refreshCourse: true, refreshAssignmentGroups: true),
          child: FutureBuilder(
            future: model.assignmentGroupFuture,
            builder: (context, AsyncSnapshot<List<AssignmentGroup>> snapshot) => _body(snapshot),
          ),
        );
      },
    );
  }

  Widget _body(AsyncSnapshot<List<AssignmentGroup>> snapshot) {
    if (snapshot.connectionState == ConnectionState.waiting) {
      return LoadingIndicator();
    } else if (snapshot.hasError) {
      return ErrorPandaWidget(L10n(context).unexpectedError, () {
        _refreshIndicatorKey.currentState.show();
      });
    } else if (!snapshot.hasData || snapshot.data.every((group) => group.assignments.isEmpty) == true) {
      return EmptyPandaWidget(
        svgPath: 'assets/svg/panda-space-no-assignments.svg',
        title: L10n(context).noAssignmentsTitle,
        subtitle: L10n(context).noAssignmentsMessage,
      );
    }

    return ListView(
      children: [
        _CourseGradeHeader(),
        ..._assignmentListChildren(context, snapshot.data),
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

// TODO: Finish logic and functionality when doing MBL-13226
class _CourseGradeHeader extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final model = Provider.of<CourseDetailsModel>(context, listen: false);

    final gradingPeriodHeader = _gradingPeriodHeader(context, model);
    final gradeTotalHeader = _gradeTotal(context, model);
    return Column(
      children: [
        SizedBox(height: 16),
        if (gradingPeriodHeader != null) gradingPeriodHeader,
        if (gradingPeriodHeader != null && gradeTotalHeader != null) SizedBox(height: 4),
        if (gradeTotalHeader != null) gradeTotalHeader,
        if (gradingPeriodHeader != null || gradeTotalHeader != null) SizedBox(height: 8),
      ],
    );
  }

  // TODO: Don't show this if there's no grading periods
  Widget _gradingPeriodHeader(BuildContext context, CourseDetailsModel model) {
    final studentColor = ParentTheme.of(context).studentColor;

    return Padding(
      // Only left padding, lets the filter button go past the margin for the ripple
      padding: const EdgeInsetsDirectional.only(start: 16),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        crossAxisAlignment: CrossAxisAlignment.baseline,
        textBaseline: TextBaseline.ideographic,
        children: <Widget>[
          // TODO: gradingPeriod.name instead of always all grading periods
          Text(L10n(context).allGradingPeriods, style: Theme.of(context).textTheme.display1),
          InkWell(
            child: Padding(
              padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
              child: Text(
                L10n(context).filter,
                style: Theme.of(context).textTheme.caption.copyWith(color: studentColor),
              ),
            ),
            onTap: () {
              // TODO: Add grading periods filter to model
              locator<QuickNav>().push(
                  context,
                  UnderConstructionScreen(
                    showAppBar: true,
                  ));
            },
          ),
        ],
      ),
    );
  }

  /// The total grade in the course/grading period
  Widget _gradeTotal(BuildContext context, CourseDetailsModel model) {
    // TODO: Don't show this in certain cases, refer to Course.kt in canvas-api2
    //  If 'all grading periods' are selected in a multipleGradingPeriodsEnabled enrollment and totalsForAllGradingPeriodsOption is true
    //  If hideFinalGrades is enabled on the course
    final textTheme = Theme.of(context).textTheme;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: <Widget>[
          Text(L10n(context).courseTotalGradeLabel, style: textTheme.body1),
          Text(_courseGrade(context, model.course.getCourseGrade(model.studentId)), style: textTheme.body1),
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
    final studentId = Provider.of<CourseDetailsModel>(context, listen: false).studentId;

    final textTheme = Theme.of(context).textTheme;
    final assignmentStatus = _assignmentStatus(context, assignment, studentId);

    return ListTile(
      onTap: () => locator<QuickNav>().push(
        context,
        AssignmentDetailsScreen(courseId: assignment.courseId, assignmentId: assignment.id, studentId: studentId),
      ),
      contentPadding: EdgeInsets.symmetric(vertical: 8, horizontal: 16),
      leading: Container(
        alignment: Alignment.topLeft,
        width: 20,
        child: Icon(CanvasIcons.assignment, size: 20, color: ParentTheme.of(context).studentColor),
      ),
      title: Text(assignment.name, style: textTheme.subhead),
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(_formatDate(context, assignment.dueAt), style: textTheme.caption),
          if (assignmentStatus != null) SizedBox(height: 4),
          if (assignmentStatus != null) assignmentStatus,
        ],
      ),
      trailing: _assignmentGrade(context, assignment, studentId),
    );
  }

  Widget _assignmentStatus(BuildContext context, Assignment assignment, String studentId) {
    final localizations = L10n(context);
    final textTheme = Theme.of(context).textTheme;
    final status = assignment.getStatus(studentId: studentId);

    switch (status) {
      case SubmissionStatus.NONE:
        return null; // An 'invisible' status, just don't show anything
      case SubmissionStatus.LATE:
        return Text(
          localizations.assignmentLateSubmittedLabel,
          style: textTheme.caption.copyWith(
            // Late will be orange, regardless of the current student
            color: ParentTheme.of(context).getColorVariantForCurrentState(StudentColorSet.fire),
          ),
        );
      case SubmissionStatus.MISSING:
        return Text(
          localizations.assignmentMissingSubmittedLabel,
          style: textTheme.caption.copyWith(color: ParentColors.failure),
        );
      case SubmissionStatus.SUBMITTED:
        return Text(localizations.assignmentSubmittedLabel, style: textTheme.caption);
      case SubmissionStatus.NOT_SUBMITTED:
        return Text(localizations.assignmentNotSubmittedLabel, style: textTheme.caption);
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

    return Text(text, semanticsLabel: semantics, style: Theme.of(context).textTheme.subhead);
  }

  String _formatDate(BuildContext context, DateTime date) {
    final localizations = L10n(context);
    if (date == null) return localizations.noDueDate;

    return DateFormat(L10n(context).dueDateTimeFormat).format(date.toLocal());
  }
}
