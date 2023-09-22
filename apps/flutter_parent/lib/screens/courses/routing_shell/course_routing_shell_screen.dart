/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/courses/routing_shell/course_routing_shell_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/canvas_web_view.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';

enum CourseShellType {
  frontPage,
  syllabus,
}

class CourseRoutingShellScreen extends StatefulWidget {
  final String courseId;
  final CourseShellType type;

  CourseRoutingShellScreen(this.courseId, this.type, {super.key});

  @override
  State<StatefulWidget> createState() => _CourseRoutingShellScreenState();
}

class _CourseRoutingShellScreenState extends State<CourseRoutingShellScreen> {
  Future<CourseShellData?>? _dataFuture;

  Future<CourseShellData?>? _refresh() {
    setState(() {
      _dataFuture =
          locator<CourseRoutingShellInteractor>().loadCourseShell(widget.type, widget.courseId, forceRefresh: true);
    });
    return _dataFuture?.catchError((_) { return Future.value(null); });
  }

  @override
  Widget build(BuildContext context) {
    if (_dataFuture == null) {
      _dataFuture = locator<CourseRoutingShellInteractor>().loadCourseShell(widget.type, widget.courseId);
    }

    return FutureBuilder(
      future: _dataFuture,
      builder: (context, AsyncSnapshot<CourseShellData?> snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Container(color: Theme.of(context).scaffoldBackgroundColor, child: LoadingIndicator());
        }

        if (snapshot.hasError || snapshot.data == null) {
          return _error();
        } else {
          return _scaffold(widget.type, snapshot.data!);
        }
      },
    );
  }

  Widget _scaffold(CourseShellType type, CourseShellData data) {
    return Scaffold(
        appBar: AppBar(
          actions: <Widget>[
            IconButton(
              tooltip: L10n(context).refresh,
              icon: Icon(CanvasIconsSolid.refresh, size: 18.0),
              onPressed: () async {
                _refresh();
              },
            ),
          ],
          title: _appBarTitle(
              (widget.type == CourseShellType.frontPage)
                  ? L10n(context).courseFrontPageLabel.toUpperCase()
                  : L10n(context).courseSyllabusLabel.toUpperCase(),
              data.course?.name ?? ''),
          bottom: ParentTheme.of(context)?.appBarDivider(),
        ),
        body: _body(data));
  }

  Widget _body(CourseShellData data) {
    return widget.type == CourseShellType.frontPage
        ? _webView(data.frontPage!.body!, emptyDescription: data.frontPage?.lockExplanation ?? L10n(context).noPageFound)
        : _webView(data.course?.syllabusBody ?? '');
  }

  Widget _webView(String html, {String? emptyDescription}) {
    return Padding(
        padding: const EdgeInsets.only(top: 16.0),
        child: CanvasWebView(
          content: html,
          emptyDescription: emptyDescription,
          horizontalPadding: 16,
          fullScreen: true,
        ));
  }

  Widget _appBarTitle(String title, String subtitle) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(title),
        Text(subtitle, style: Theme.of(context).primaryTextTheme.bodySmall),
      ],
    );
  }

  Widget _error() {
    return Container(
        color: Theme.of(context).scaffoldBackgroundColor,
        child: ErrorPandaWidget(L10n(context).unexpectedError, () => _refresh()));
  }
}
