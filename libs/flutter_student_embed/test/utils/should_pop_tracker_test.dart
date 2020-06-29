// Copyright (C) 2020 - present Instructure, Inc.
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
import 'package:flutter_student_embed/screens/calendar/calendar_screen.dart';
import 'package:flutter_student_embed/utils/common_widgets/colored_status_bar.dart';
import 'package:flutter_student_embed/utils/design/student_colors.dart';
import 'package:flutter_student_embed/utils/native_comm.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';

import '../testutils/dummy_widget.dart';

void main() {
  test('didPush calls onUpdate', () {
    bool onUpdateCalled = false;
    var tracker = ShouldPopTracker((_, __) => onUpdateCalled = true);
    tracker.didPush(makeRoute(), null);
    expect(onUpdateCalled, isTrue);
  });

  test('didPop calls onUpdate', () {
    bool onUpdateCalled = false;
    var tracker = ShouldPopTracker((_, __) => onUpdateCalled = true);
    tracker.didPop(null, makeRoute());
    expect(onUpdateCalled, isTrue);
  });

  test('didRemove calls onUpdate', () {
    bool onUpdateCalled = false;
    var tracker = ShouldPopTracker((_, __) => onUpdateCalled = true);
    tracker.didRemove(null, makeRoute());
    expect(onUpdateCalled, isTrue);
  });

  test('didReplace calls onUpdate', () {
    bool onUpdateCalled = false;
    var tracker = ShouldPopTracker((_, __) => onUpdateCalled = true);
    tracker.didReplace(newRoute: makeRoute());
    expect(onUpdateCalled, isTrue);
  });

  test('shouldPop is true for calendar screens', () {
    bool shouldPop = false;
    var tracker = ShouldPopTracker((pop, __) => shouldPop = pop);
    var route = PageRouteBuilder(
      pageBuilder: (_, __, ___) => DummyWidget(),
      settings: RouteSettings(name: CalendarScreen.routeName), // Set route name for the benefit of ShouldPopTracker
      transitionsBuilder: (_, __, ___, child) => child,
      transitionDuration: Duration.zero,
    );
    tracker.update(route);
    expect(shouldPop, isTrue);
  });

  test('Uses default status bar color if screen is not a ColoredStatusBar', () {
    Color statusBarColor;
    var tracker = ShouldPopTracker((_, color) => statusBarColor = color);
    tracker.didReplace(newRoute: makeRoute());
    expect(statusBarColor, StudentColors.primaryColor);
  });

  test('Uses status bar color specified by ColoredStatusBar widget', () {
    Color statusBarColor;
    var tracker = ShouldPopTracker((_, color) => statusBarColor = color);
    tracker.didReplace(newRoute: makeRoute(screen: _TestColoredStatusBar()));
    expect(statusBarColor, _TestColoredStatusBar.color);
  });
}

Route makeRoute({Widget screen}) {
  return QuickFadeRoute(screen ?? DummyWidget());
}

class _TestColoredStatusBar extends StatelessWidget with ColoredStatusBar {
  static Color color = Colors.pink;

  @override
  Color getStatusBarColor() => color;

  @override
  Widget build(BuildContext context) {
    throw UnimplementedError();
  }
}
