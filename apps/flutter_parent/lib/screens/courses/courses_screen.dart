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
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/course_grade.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';

import 'courses_interactor.dart';

class CoursesScreen extends StatefulWidget {
  @override
  _CoursesScreenState createState() => _CoursesScreenState();
}

class _CoursesScreenState extends State<CoursesScreen> {
  User? _student;

  Future<List<Course>?>? _coursesFuture;

  CoursesInteractor _interactor = locator<CoursesInteractor>();

  final GlobalKey<RefreshIndicatorState> _refreshKey = new GlobalKey<RefreshIndicatorState>();

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    var _selectedStudent = Provider.of<SelectedStudentNotifier>(context, listen: true).value;
    if (_student != _selectedStudent) {
      // The student was changed by the user
      _student = _selectedStudent;
      // Update the courses - this is done in case a new user was added after the initial course grabbing
      _coursesFuture = _loadCourses(forceRefresh: true);
    }
  }

  Future<List<Course>?> _loadCourses({bool forceRefresh = false}) =>
      _interactor.getCourses(isRefresh: forceRefresh, studentId: _student?.id.isEmpty == true ? null : _student!.id);

  @override
  Widget build(BuildContext context) => _content(context);

  Widget _content(BuildContext context) {
    return FutureBuilder(
      future: _coursesFuture,
      builder: (context, AsyncSnapshot<List<Course>?> snapshot) {
        Widget _body;
        if (snapshot.hasError) {
          _body = ErrorPandaWidget(L10n(context).errorLoadingCourses, () => _refreshKey.currentState?.show());
        } else if (snapshot.hasData) {
          _body = (snapshot.data!.isEmpty)
              ? EmptyPandaWidget(
                  svgPath: 'assets/svg/panda-book.svg',
                  title: L10n(context).noCoursesTitle,
                  subtitle: L10n(context).noCoursesMessage,
                )
              : _success(snapshot.data!);
        } else {
          return LoadingIndicator();
        }

        return RefreshIndicator(
          key: _refreshKey,
          onRefresh: () => _refresh(),
          child: _body,
        );
      },
    );
  }

  Widget _success(Iterable<Course> courses) {
    return ListView.builder(
        scrollDirection: Axis.vertical,
        physics: const AlwaysScrollableScrollPhysics(),
        itemCount: courses.length,
        itemBuilder: (context, idx) {
          var course = courses.toList()[idx];
          final grade = _courseGrade(context, course);
          return ListTile(
            onTap: () => _courseTapped(context, course),
            contentPadding: const EdgeInsets.symmetric(horizontal: 16),
            title: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                SizedBox(height: 8),
                Text(course.name,
                    style: Theme.of(context).textTheme.titleMedium, key: Key("${course.courseCode}_name")),
                SizedBox(height: 2),
                Text(course.courseCode ?? '',
                    style: Theme.of(context).textTheme.bodySmall, key: Key("${course.courseCode}_code")),
                if (grade != null) SizedBox(height: 4),
                if (grade != null) grade,
                SizedBox(height: 8),
              ],
            ),
          );
        });
  }

  Widget? _courseGrade(context, Course course) {
    CourseGrade grade = course.getCourseGrade(_student?.id);
    var format = NumberFormat.percentPattern();
    format.maximumFractionDigits = 2;

    if (grade.isCourseGradeLocked(forAllGradingPeriods: course.enrollments?.any((enrollment) => enrollment.hasActiveGradingPeriod()) != true) ||
        (course.settings?.restrictQuantitativeData == true && grade.currentGrade() == null)) {
      return null;
    }
    // If there is no current grade, return 'No grade'
    // Otherwise, we have a grade, so check if we have the actual grade string
    // or a score
    var formattedScore = (grade.currentScore() != null && !(course.settings?.restrictQuantitativeData ?? false))
        ? format.format(grade.currentScore()! / 100)
        : '';
    var text = grade.noCurrentGrade()
        ? L10n(context).noGrade
        : grade.currentGrade()?.isNotEmpty == true
            ? "${grade.currentGrade()}${formattedScore.isNotEmpty ? ' $formattedScore' : ''}"
            : formattedScore;

    return Text(
      text,
      key: Key("${course.courseCode}_grade"),
      style: TextStyle(
        color: Theme.of(context).colorScheme.secondary,
        fontSize: 16,
        fontWeight: FontWeight.w500,
      ),
    );
  }

  void _courseTapped(context, Course course) {
    locator<QuickNav>().pushRoute(context, PandaRouter.courseDetails(course.id));
  }

  Future<void> _refresh() async {
    setState(() {
      _coursesFuture = _loadCourses(forceRefresh: true);
    });
    await _coursesFuture;
  }
}
