import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/course_grade.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart';


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
  Widget build(BuildContext context) {
    return _content(context);
  }

  Widget _content(BuildContext context) {
    if (_error) return Text("Error!");

    if (_loading) return Center(child: CircularProgressIndicator());

    print("Creating listview");
    print("Courses:${_courses.length}");
    _courses.forEach((it) => print(it));

    return ListView(
        scrollDirection: Axis.vertical,
        physics: const AlwaysScrollableScrollPhysics(),
        children: _courses.where(_studentFilter).map((course) {
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
    print("Student id: ${widget._student?.id}");
    course.enrollments.forEach((it) => print(it.userId));

    CourseGrade grade = course.getCourseGrade(widget._student?.id);
    var format = NumberFormat();
    format.maximumFractionDigits = 2;

    var text = grade.noCurrentGrade()
        ? "No Grade"// TODO: Localize! WidgetsLocalizations.of(context).noGrade
        : (grade.currentGrade()?.isNotEmpty == false
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

  bool _studentFilter(Course course) {
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

class CoursesInteractor {
  Future<List<Course>> getCourses() async {
    var courses = await CourseApi.getObserveeCourses();
    return courses;
  }
}
