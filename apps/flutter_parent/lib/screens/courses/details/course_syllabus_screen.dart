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

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:provider/provider.dart';
import 'package:webview_flutter/webview_flutter.dart';
import 'package:flutter_parent/utils/WebViewUtils.dart';

class CourseSyllabusScreen extends StatelessWidget {
  final String syllabus;
  CourseSyllabusScreen(this.syllabus);

  @override
  Widget build(BuildContext context) {
    CourseDetailsModel.selectedTab = 1;
    return Consumer<CourseDetailsModel>(
      builder: (context, model, _) => WebView(
        javascriptMode: JavascriptMode.unrestricted,
        gestureRecognizers: Set()..add(Factory<WebViewGestureRecognizer>(() => WebViewGestureRecognizer())),
        onWebViewCreated: (controller) {
          controller.loadHtml(syllabus);
        },
      ),
    );
  }
}