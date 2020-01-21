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

import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:provider/provider.dart';
import 'package:webview_flutter/webview_flutter.dart';

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
          controller.loadUrl(
            Uri.dataFromString(syllabusWrapper.replaceAll('{\$CONTENT\$}', syllabus),
                    mimeType: 'text/html', encoding: Encoding.getByName('utf-8'))
                .toString(),
          );
        },
      ),
    );
  }

  // This HTML wrapper makes large media content scale to fit the screen
  final String syllabusWrapper = '''
  <!DOCTYPE html>
<!--
  ~ Copyright (C) 2016 - present Instructure, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU General Public License as published by
  ~     the Free Software Foundation, version 3 of the License.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU General Public License for more details.
  ~
  ~     You should have received a copy of the GNU General Public License
  ~     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ~
  -->
<html>
    <head>
        <meta name="viewport" content="width=device-width" charset="utf-8" />
    </head>
    <style>
        html,body {
            width: 100%;
            height: auto;
            margin: 0;
            padding: 0;
        }
        img {
            max-width: 100% !important;
            height: auto;
            margin: 0;
            padding: 0;
        }
        video {
            width: 100%    !important;
            height: auto   !important;
            margin: 0;
            padding: 0;
        }
        iframe {
            width: 100%    !important;
            margin: 0;
            padding-top: 0;
        }
        /* makes the videos in fullscreen black */
        :-webkit-full-screen-ancestor:not(iframe) { background-color: black }
        pre {
            white-space: pre-wrap;       /* Since CSS 2.1 */
            white-space: -moz-pre-wrap;  /* Mozilla, since 1999 */
            white-space: -pre-wrap;      /* Opera 4-6 */
            white-space: -o-pre-wrap;    /* Opera 7 */
            word-wrap: break-word;       /* Internet Explorer 5.5+ */
        }
        #content {
            padding: 0px 10px 10px;
        }
        a {
            word-wrap: break-word;
        }
    </style>
    <body>
        <div id="content">
            {\$CONTENT\$}
        </div>
    </body>
</html>''';
}

///
/// Due to the Gesture Arena currently favoring basically but the WebView,
/// this will tell the arena to respect vertical swipe gestures on the WebView
/// so it can scroll
/// Code taken from:
///   https://stackoverflow.com/questions/57069716/scrolling-priority-when-combining-horizontal-scrolling-with-webview/57150906#57150906
/// Related issues:
///   https://github.com/flutter/flutter/issues/36304
///   https://github.com/flutter/flutter/issues/35394
class WebViewGestureRecognizer extends VerticalDragGestureRecognizer {
  WebViewGestureRecognizer({PointerDeviceKind kind}) : super(kind: kind);

  Offset _dragDistance = Offset.zero;

  @override
  void addPointer(PointerEvent event) {
    startTrackingPointer(event.pointer);
  }

  @override
  void handleEvent(PointerEvent event) {
    _dragDistance = _dragDistance + event.delta;
    if (event is PointerMoveEvent) {
      final double dy = _dragDistance.dy.abs();
      final double dx = _dragDistance.dx.abs();

      if (dy > dx && dy > kTouchSlop) {
        // Vertical drag - accept
        resolve(GestureDisposition.accepted);
        _dragDistance = Offset.zero;
      } else if (dx > kTouchSlop && dx > dy) {
        // horizontal drag - stop tracking
        stopTrackingPointer(event.pointer);
        _dragDistance = Offset.zero;
      }
    }
  }

  @override
  void didStopTrackingLastPointer(int pointer) {}
}
