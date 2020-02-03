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
import 'package:flutter_parent/screens/courses/details/course_details_screen.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart';

import 'courses_interactor.dart';

class CoursesScreen extends StatefulWidget {
  final User _student;

  const CoursesScreen(this._student, {Key key}) : super(key: key);

  @override
  _CoursesScreenState createState() => _CoursesScreenState();
}

class _CoursesScreenState extends State<CoursesScreen> {
  CoursesInteractor _interactor = locator<CoursesInteractor>();

  Future<List<Course>> _coursesFuture;

  @override
  void initState() {
    _loadCourses();
    super.initState();
  }

  Future<List<Course>> _loadCourses({bool isRefresh: false}) {
    _coursesFuture = _interactor.getCourses(widget._student?.id, isRefresh);
    if (isRefresh) setState(() {});
    return _coursesFuture.catchError((_) {});
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: _coursesFuture,
      builder: (BuildContext context, AsyncSnapshot<List<Course>> snapshot) {
        if (snapshot.hasData) {
          return RefreshIndicator(
            onRefresh: () => _loadCourses(isRefresh: true),
            child: _success(context, snapshot.data),
          );
        } else if (snapshot.hasError && snapshot.connectionState == ConnectionState.done) {
          return ErrorPandaWidget(L10n(context).errorLoadingCourses, () => _loadCourses(isRefresh: true));
        } else {
          return LoadingIndicator();
        }
      },
    );
  }

  Widget _success(BuildContext context, List<Course> courses) {
    if (courses.isEmpty) {
      return EmptyPandaWidget(
        svgPath: 'assets/svg/panda-book.svg',
        title: L10n(context).noCoursesTitle,
        subtitle: L10n(context).noCoursesMessage,
      );
    }

    return ListView.builder(
      padding: EdgeInsets.symmetric(vertical: 8),
      itemCount: courses.length,
      itemBuilder: (context, index) {
        var course = courses[index];
        return ListTile(
          onTap: () => _courseTapped(context, course),
          title: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              SizedBox(height: 8),
              Text(course.name ?? '', style: Theme.of(context).textTheme.subhead),
              SizedBox(height: 2),
              Text(course.courseCode ?? '', style: Theme.of(context).textTheme.caption),
              SizedBox(height: 4),
              _courseGrade(context, course),
              SizedBox(height: 8),
            ],
          ),
        );
      },
    );
  }

  Widget _courseGrade(context, Course course) {
    CourseGrade grade = course.getCourseGrade(widget._student?.id);
    var format = NumberFormat.percentPattern();
    format.maximumFractionDigits = 2;

    // If there is no current grade, return 'No grade'
    // Otherwise, we have a grade, so check if we have the actual grade string
    // or a score
    var text = grade.noCurrentGrade()
        ? L10n(context).noGrade
        : grade.currentGrade()?.isNotEmpty == true ? grade.currentGrade() : format.format(grade.currentScore() / 100);

    return Text(text, style: Theme.of(context).textTheme.caption.copyWith(color: Theme.of(context).accentColor));
  }

  void _courseTapped(context, Course course) {
    locator<QuickNav>().push(context, CourseDetailsScreen.withCourse(widget._student.id, widget._student.name, course));
  }
}
