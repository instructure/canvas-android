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
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/analytics_observer.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../utils/test_app.dart';
import '../utils/test_helpers/mock_helpers.dart';
import '../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final observer = AnalyticsObserver();
  final analytics = MockAnalytics();

  setUpAll(() {
    PandaRouter.init(); // Initialize the router so when we matches are found it works as expected
    setupTestLocator((locator) {
      locator.registerLazySingleton<Analytics>(() => analytics);
    });
  });

  setUp(() {
    reset(analytics);
  });

  group('didPop', () {
    test('logs analytics with a PageRoute', () {
      final screenName = '/courses/:courseId';
      final courseId = '1234';
      final settings = RouteSettings(name: PandaRouter.courseDetails(courseId));

      observer.didPop(
        MaterialPageRoute(builder: (_) => Container()),
        MaterialPageRoute(builder: (_) => Container(), settings: settings),
      );

      verify(analytics.setCurrentScreen(screenName));
      verify(analytics.logMessage('Pushing widget: $screenName with params: {courseId: [$courseId]}'));
    });

    test('does not log analytics with a non PageRoute', () {
      observer.didPop(
        _NonPageRoute(),
        _NonPageRoute(),
      );

      verifyNever(analytics.setCurrentScreen(any));
      verifyNever(analytics.logMessage(any));
    });
  });

  group('didReplace', () {
    test('logs analytics with a PageRoute', () {
      final screenName = '/courses/:courseId';
      final courseId = '1234';
      final settings = RouteSettings(name: PandaRouter.courseDetails(courseId));

      observer.didReplace(newRoute: MaterialPageRoute(builder: (_) => Container(), settings: settings));

      verify(analytics.setCurrentScreen(screenName));
      verify(analytics.logMessage('Pushing widget: $screenName with params: {courseId: [$courseId]}'));
    });

    test('does not log analytics with a non PageRoute', () {
      observer.didReplace(
        newRoute: _NonPageRoute(),
        oldRoute: MaterialPageRoute(builder: (_) => Container()),
      );

      verifyNever(analytics.setCurrentScreen(any));
      verifyNever(analytics.logMessage(any));
    });
  });

  group('didPush', () {
    test('logs analytics with a PageRoute', () {
      final screenName = '/courses/:courseId';
      final courseId = '1234';
      final settings = RouteSettings(name: PandaRouter.courseDetails(courseId));

      observer.didPush(
        MaterialPageRoute(builder: (_) => Container(), settings: settings),
        MaterialPageRoute(builder: (_) => Container()),
      );

      verify(analytics.setCurrentScreen(screenName));
      verify(analytics.logMessage('Pushing widget: $screenName with params: {courseId: [$courseId]}'));
    });

    test('does not log analytics with a non PageRoute', () {
      observer.didPush(
        _NonPageRoute(),
        MaterialPageRoute(builder: (_) => Container()),
      );

      verifyNever(analytics.setCurrentScreen(any));
      verifyNever(analytics.logMessage(any));
    });
  });

  test('does not log analytics with a non null route name', () {
    observer.didPush(
      MaterialPageRoute(builder: (_) => Container(), settings: RouteSettings()),
      MaterialPageRoute(builder: (_) => Container()),
    );

    verifyNever(analytics.setCurrentScreen(any));
    verifyNever(analytics.logMessage(any));
  });
}

class _NonPageRoute extends ModalRoute {
  @override
  bool get opaque => false;

  @override
  Color? get barrierColor => null;

  @override
  bool get maintainState => false;

  @override
  String? get barrierLabel => null;

  @override
  bool get barrierDismissible => false;

  @override
  Duration get transitionDuration => Duration.zero;

  @override
  Widget buildPage(BuildContext context, Animation<double> animation, Animation<double> secondaryAnimation) => Container();
}
