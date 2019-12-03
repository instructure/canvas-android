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
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:flutter_parent/screens/courses/details/course_grades_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_summary_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_syllabus_screen.dart';
import 'package:flutter_parent/utils/base_model.dart';
import 'package:flutter_parent/utils/common_widgets/full_screen_scroll_container.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:provider/provider.dart';

class CourseDetailsScreen extends StatefulWidget {
  final CourseDetailsModel _model;

  CourseDetailsScreen(int studentId, int courseId, {Key key})
      : this._model = CourseDetailsModel(studentId, courseId),
        super(key: key);

  // A convenience constructor when we already have the course data, so we don't load something we already have
  CourseDetailsScreen.withCourse(int studentId, Course course, {Key key})
      : this._model = CourseDetailsModel.withCourse(studentId, course),
        super(key: key);

  @override
  _CourseDetailsScreenState createState() => _CourseDetailsScreenState();
}

class _CourseDetailsScreenState extends State<CourseDetailsScreen> {
  @override
  void initState() {
    super.initState();
    widget._model.loadData();
  }

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider<CourseDetailsModel>(
      builder: (context) => widget._model,
      child: Consumer<CourseDetailsModel>(
        builder: (context, model, _) {
          return DefaultTabController(
            length: 3,
            child: Scaffold(
              appBar: AppBar(
                title: Text(model.course?.name ?? ''),
                bottom: TabBar(
                  tabs: [
                    Tab(text: AppLocalizations.of(context).courseGradesLabel.toUpperCase()),
                    Tab(text: AppLocalizations.of(context).courseSyllabusLabel.toUpperCase()),
                    Tab(text: AppLocalizations.of(context).courseSummaryLabel.toUpperCase()),
                  ],
                ),
              ),
              body: _body(context, model),
              floatingActionButton: FloatingActionButton(
                onPressed: () => _sendMessage(),
                child: Semantics(
                  label: AppLocalizations.of(context).courseMessageHint,
                  child: Padding(
                    padding: const EdgeInsets.only(left: 4, top: 4),
                    child: Icon(CanvasIconsSolid.comment),
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _body(BuildContext context, CourseDetailsModel model) {
    // Show loading if we're waiting for data, not inside the refresh indicator as it's unnecessary
    if (model.state == ViewState.Busy) {
      return Center(child: CircularProgressIndicator());
    }

    // Get the child widget to show in the refresh indicator
    if (model.state == ViewState.Error) {
      return RefreshIndicator(
        child: FullScreenScrollContainer(children: [Text(AppLocalizations.of(context).unexpectedError)]),
        onRefresh: () async {
          return model.loadData(refreshCourse: true);
        },
      );
    } else {
      return TabBarView(children: [
        CourseGradesScreen(),
        CourseSyllabusScreen(),
        CourseSummaryScreen(),
      ]);
    }
  }

  void _sendMessage() {
    // TODO: Send a message in this course
//    QuickNav.push(context, MessagesScreen());
  }
}
