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
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:flutter_parent/screens/courses/details/course_grades_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_home_page_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_summary_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_syllabus/course_syllabus_screen.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_screen.dart';
import 'package:flutter_parent/utils/base_model.dart';
import 'package:flutter_parent/utils/common_widgets/full_screen_scroll_container.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:provider/provider.dart';

class CourseDetailsScreen extends StatefulWidget {
  final CourseDetailsModel _model;

  CourseDetailsScreen(String courseId, {Key key})
      : this._model = CourseDetailsModel(ApiPrefs.getCurrentStudent(), courseId),
        super(key: key);

  // A convenience constructor when we already have the course data, so we don't load something we already have
  CourseDetailsScreen.withCourse(Course course, {Key key})
      : this._model = CourseDetailsModel.withCourse(ApiPrefs.getCurrentStudent(), course),
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
      create: (context) => widget._model,
      child: Consumer<CourseDetailsModel>(
        builder: (context, model, _) {
          // Show loading if we're waiting for data, not inside the refresh indicator as it's unnecessary
          if (model.state == ViewState.Busy) {
            return Material(
              color: Theme.of(context).scaffoldBackgroundColor,
              child: LoadingIndicator(),
            );
          }
          return _body(context, model);
        },
      ),
    );
  }

  Widget _body(BuildContext context, CourseDetailsModel model) {
    final tabCount = model.tabCount();
    return DefaultTabController(
      length: tabCount,
      child: Scaffold(
        appBar: AppBar(
            title: Text(model.course?.name ?? ''),
            bottom: ParentTheme.of(context).appBarDivider(
              bottom: (tabCount <= 1)
                  ? null // Don't show the tab bar if we only have one tab
                  : TabBar(
                      tabs: [
                        Tab(text: L10n(context).courseGradesLabel.toUpperCase()),
                        if (model.hasHomePageAsFrontPage) Tab(text: L10n(context).courseFrontPageLabel.toUpperCase()),
                        if (model.hasHomePageAsSyllabus) Tab(text: L10n(context).courseSyllabusLabel.toUpperCase()),
                        if (model.showSummary) Tab(text: L10n(context).courseSummaryLabel.toUpperCase()),
                      ],
                    ),
            )),
        body: _tabBody(context, model),
        floatingActionButton: FloatingActionButton(
          onPressed: () => _sendMessage(model.hasHomePageAsSyllabus),
          child: Semantics(
            label: L10n(context).courseMessageHint,
            child: Padding(
              padding: const EdgeInsets.only(left: 4, top: 4),
              child: Icon(CanvasIconsSolid.comment),
            ),
          ),
        ),
      ),
    );
  }

  Widget _tabBody(BuildContext context, CourseDetailsModel model) {
    // Get the child widget to show in the refresh indicator
    if (model.state == ViewState.Error) {
      return RefreshIndicator(
        child: FullScreenScrollContainer(children: [Text(L10n(context).unexpectedError)]),
        onRefresh: () async {
          return model.loadData(refreshCourse: true);
        },
      );
    } else {
      return TabBarView(
        children: [
          CourseGradesScreen(),
          if (model.hasHomePageAsFrontPage) CourseHomePageScreen(courseId: model.courseId),
          if (model.hasHomePageAsSyllabus) CourseSyllabusScreen(model.course.syllabusBody, model.course.name),
          if (model.showSummary) CourseSummaryScreen(),
        ],
      );
    }
  }

  void _sendMessage(bool hasSyllabus) {
    String subject;
    String urlLink = '${ApiPrefs.getDomain()}/courses/${widget._model.courseId}';
    if (CourseDetailsModel.selectedTab == 0) {
      // Grades
      subject = L10n(context).gradesSubjectMessage(widget._model.student.name);
      urlLink += '/grades';
    } else if (hasSyllabus) {
      // Syllabus
      subject = L10n(context).syllabusSubjectMessage(widget._model.student.name);
      urlLink += '/assignments/syllabus';
    } else {
      // Front Page
      subject = L10n(context).frontPageSubjectMessage(widget._model.student.name);
    }

    String postscript = L10n(context).messageLinkPostscript(widget._model.student.name, urlLink);
    Widget screen = CreateConversationScreen(
      widget._model.courseId,
      widget._model.student.id,
      subject,
      postscript,
    );
    locator.get<QuickNav>().push(context, screen);
  }
}
