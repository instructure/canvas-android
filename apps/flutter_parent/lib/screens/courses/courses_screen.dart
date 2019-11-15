/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/course_grade.dart';
import 'package:flutter_parent/models/user.dart';
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
  List<Course> _courses = [];

  bool _loading = false;
  bool _error = false;

  CoursesInteractor _interactor = locator<CoursesInteractor>();

  @override
  void initState() {
    _loadCourses();
    super.initState();
  }

  void _loadCourses() {
    setState(() {
      _loading = true;
      _error = false;
    });

    _interactor.getCourses().then((courses) {
      print(courses);
      _courses = courses;
      setState(() {
        _loading = false;
      });
    }).catchError((error) {
      setState(() {
        _loading = false;
        _error = true;
        print(error);
      });
    });
  }

  @override
  Widget build(BuildContext context) => _content(context);

  Widget _content(BuildContext context) {
    if (_error) return Text("Error!"); // TODO: Show an error screen

    if (_loading) return Center(child: CircularProgressIndicator());

    _courses.forEach((it) => print(it));

    return ListView(
        scrollDirection: Axis.vertical,
        physics: const AlwaysScrollableScrollPhysics(),
        children: _courses.where(_enrollmentFilter).map((course) {
          return ListTile(
            onTap: () => _courseTapped(context, course),
            title: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                SizedBox(height: 12),
                Text(
                  course.name ?? "",
                  style: TextStyle(
                    fontSize: 19,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                SizedBox(height: 2),
                Text(
                  course.courseCode ?? "",
                  style: TextStyle(
                    color: Colors.grey,
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                SizedBox(height: 4),
                _courseGrade(context, course),
                SizedBox(height: 12),
              ],
            ),
          );
        }).toList());
  }

  Widget _courseGrade(context, Course course) {
    course.enrollments.forEach((it) => print(it.userId));

    CourseGrade grade = course.getCourseGrade(widget._student?.id);
    var format = NumberFormat();
    format.maximumFractionDigits = 2;

    // If there is no current grade, return 'No grade'
    // Otherwise, we have a grade, so check if we have the actual grade string
    // or a score
    var text = grade.noCurrentGrade()
        ? AppLocalizations.of(context).noGrade
        : (grade.currentGrade()?.isNotEmpty == true
        ? grade.currentGrade()
        : format.format(grade.currentScore()) + "%");

    return Text(
      text,
      style: TextStyle(
        color: Theme.of(context).primaryColor,
        fontSize: 16,
        fontWeight: FontWeight.w500,
      ),
    );
  }

  /// Filters enrollments by those associated with the currently selected user
  bool _enrollmentFilter(Course course) {
    return course.enrollments
        ?.any((enrollment) => enrollment.userId == widget._student?.id) ??
        false;
  }

  void _courseTapped(context, Course course) {
    Navigator.of(context).push(MaterialPageRoute(builder: (context) {
      // TODO: Route to course page
//      return CoursePage(widget._student, course);
        return null;
    }));
  }
}