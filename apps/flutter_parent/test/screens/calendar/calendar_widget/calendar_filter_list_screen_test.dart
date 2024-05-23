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

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_screen.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../../utils/accessibility_utils.dart';
import '../../../utils/test_app.dart';
import '../../../utils/test_helpers/mock_helpers.dart';
import '../../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows loading indicator', (tester) async {
      var interactor = MockCalendarFilterListInteractor();
      var completer = Completer<List<Course>>();

      when(interactor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => completer.future);

      setupTestLocator((locator) => locator.registerLazySingleton<CalendarFilterListInteractor>(() => interactor));

      await tester.pumpWidget(TestApp(
        CalendarFilterListScreen(<String>{}),
        darkMode: true,
      ));
      await tester.pump();

      expect(find.byType(LoadingIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows empty panda screen', (tester) async {
      var interactor = MockCalendarFilterListInteractor();
      var completer = Completer<List<Course>>();

      when(interactor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => completer.future);

      setupTestLocator((locator) => locator.registerLazySingleton<CalendarFilterListInteractor>(() => interactor));

      await tester.pumpWidget(TestApp(
        CalendarFilterListScreen(<String>{}),
        darkMode: true,
      ));
      await tester.pump();

      completer.complete([]);
      await tester.pumpAndSettle();

      expect(find.byType(EmptyPandaWidget), findsOneWidget);
      expect(find.text(AppLocalizations().noCoursesMessage), findsOneWidget);
      expect(find.text(AppLocalizations().noCoursesTitle), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows error panda screen', (tester) async {
      var interactor = MockCalendarFilterListInteractor();
      var completer = Completer<List<Course>>();

      when(interactor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => completer.future);

      setupTestLocator((locator) => locator.registerLazySingleton<CalendarFilterListInteractor>(() => interactor));

      await tester.pumpWidget(TestApp(
        CalendarFilterListScreen(<String>{}),
        darkMode: true,
      ));
      await tester.pump();

      completer.completeError('Error');
      await tester.pumpAndSettle();

      expect(find.byType(ErrorPandaWidget), findsOneWidget);
      expect(find.text(AppLocalizations().errorLoadingCourses), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows appbar title', (tester) async {
      Set<String> selectedCourses = {'course_123', 'course_234', 'course_345'};
      var interactor = MockCalendarFilterListInteractor();
      when(interactor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => Future.value(_mockCourses()));

      setupTestLocator((locator) => locator.registerLazySingleton<CalendarFilterListInteractor>(() => interactor));

      await tester.pumpWidget(TestApp(
        CalendarFilterListScreen(selectedCourses),
      ));
      await tester.pump();
      await tester.pump();

      expect(find.text(AppLocalizations().calendars), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows description', (tester) async {
      var interactor = MockCalendarFilterListInteractor();
      when(interactor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => Future.value(_mockCourses()));

      setupTestLocator((locator) => locator.registerLazySingleton<CalendarFilterListInteractor>(() => interactor));

      await tester.pumpWidget(TestApp(
        CalendarFilterListScreen(<String>{}),
      ));
      await tester.pump();
      await tester.pump();

      expect(find.text(AppLocalizations().calendarTapToFavoriteDesc), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows course list with header item', (tester) async {
      var interactor = MockCalendarFilterListInteractor();
      when(interactor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => Future.value(_mockCourses()));

      setupTestLocator((locator) => locator.registerLazySingleton<CalendarFilterListInteractor>(() => interactor));

      await tester.pumpWidget(TestApp(
        CalendarFilterListScreen(<String>{}),
      ));
      await tester.pump();
      await tester.pump();

      expect(find.text(AppLocalizations().coursesLabel), findsOneWidget);
      expect(find.byType(LabeledCheckbox), findsNWidgets(3));
    });

    testWidgetsWithAccessibilityChecks('empty selected list selects all items', (tester) async {
      var interactor = MockCalendarFilterListInteractor();
      when(interactor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => Future.value(_mockCourses()));

      setupTestLocator((locator) => locator.registerLazySingleton<CalendarFilterListInteractor>(() => interactor));

      await tester.pumpWidget(TestApp(
        CalendarFilterListScreen(<String>{}),
      ));
      await tester.pump();
      await tester.pump();

      // Check for a text widget that has the 'pts' substring
      expect(find.byWidgetPredicate((widget) {
        if (widget is Checkbox) {
          final Checkbox checkboxWidget = widget;
          return checkboxWidget.value!;
        }
        return false;
      }), findsNWidgets(3));
    });

    testWidgetsWithAccessibilityChecks('items in initial selected list are selected', (tester) async {
      var interactor = MockCalendarFilterListInteractor();
      when(interactor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => Future.value(_mockCourses()));

      setupTestLocator((locator) => locator.registerLazySingleton<CalendarFilterListInteractor>(() => interactor));

      await tester.pumpWidget(TestApp(
        CalendarFilterListScreen({'course_123'}),
      ));
      await tester.pump();
      await tester.pump();

      // Check for a checkbox widgets that are checked
      expect(find.byWidgetPredicate((widget) {
        if (widget is Checkbox) {
          final Checkbox checkboxWidget = widget;
          return checkboxWidget.value!;
        }
        return false;
      }), findsNWidgets(1));
    });
  });

  group('Interaction', () {
    testWidgetsWithAccessibilityChecks('clicking list item updates selected list', (tester) async {
      var interactor = MockCalendarFilterListInteractor();
      when(interactor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => Future.value(_mockCourses()));

      Set<String> selectedContexts = {'course_123'};
      setupTestLocator((locator) => locator.registerLazySingleton<CalendarFilterListInteractor>(() => interactor));

      await tester.pumpWidget(TestApp(
        CalendarFilterListScreen(selectedContexts),
      ));
      await tester.pump();
      await tester.pump();

      CalendarFilterListScreenState state = await tester.state(find.byType(CalendarFilterListScreen));
      var initial = state.selectedContextIds.length;

      var checkedCheckBoxFinder = find.byWidgetPredicate((widget) {
        if (widget is Checkbox) {
          // Check for a checkbox widgets that are checked
          final Checkbox checkboxWidget = widget;
          return checkboxWidget.value!;
        }
        return false;
      });

      // Make sure we've got the correct selected number of items
      expect(checkedCheckBoxFinder, findsNWidgets(1));

      // Click on a context to add it to the selected list
      await tester.tap(find.text('Course2'));
      await tester.pumpAndSettle(); // Let checkbox animation finish

      // Check to make sure we have two items selected
      expect(checkedCheckBoxFinder, findsNWidgets(2));

      // Make sure the selected list was updated
      var now = state.selectedContextIds.length;
      expect(now > initial, true);

      // Tap the first course, deselecting it and removing it from the selected list
      await tester.tap(find.text('Course1'));
      await tester.pumpAndSettle(); // Let checkbox animation finish

      // Check to make sure we have one item selected
      expect(checkedCheckBoxFinder, findsNWidgets(1));

      // Make sure the selected list was updated
      now = state.selectedContextIds.length;
      expect(now == initial, true);
    });

    /*
    testWidgetsWithAccessibilityChecks('deselecting all items empties the selected list', (tester) async {
      var interactor = _MockCalendarFilterListInteractor();
      when(interactor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => Future.value(_mockCourses()));

      Set<String> selectedContexts = {'course_123'};
      setupTestLocator((locator) => locator.registerLazySingleton<CalendarFilterListInteractor>(() => interactor));

      await tester.pumpWidget(TestApp(
        CalendarFilterListScreen(selectedContexts),
      ));
      await tester.pump();
      await tester.pump();

      CalendarFilterListScreenState state = await tester.state(find.byType(CalendarFilterListScreen));

      expect(find.byType(Checkbox), findsNWidgets(3));

      var checkedCheckBoxFinder = find.byWidgetPredicate((widget) {
        if (widget is Checkbox) {
          // Check for a checkbox widgets that are checked
          final Checkbox checkboxWidget = widget;
          return checkboxWidget.value;
        }
        return false;
      });

      // Make sure we've got the correct selected number of items
      expect(checkedCheckBoxFinder, findsNWidgets(1));

      // Click on the first course, the only selected context, to add it to the selected list
      expect(find.text('Course1'), findsOneWidget);
      await tester.tap(find.text('Course1'));
      await tester.pumpAndSettle(Duration(seconds: 4)); // Let checkbox animation finish

      // Check to make sure we have no items selected
      expect(checkedCheckBoxFinder, findsNothing);

      // Make sure the selected list was updated
      var now = state.selectedContextIds.length;
      expect(now == 0, true);
    });
     */

    testWidgetsWithAccessibilityChecks('deselecting items reduces the selected list', (tester) async {
      var interactor = MockCalendarFilterListInteractor();
      when(interactor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => Future.value(_mockCourses()));

      Set<String> selectedContexts = {'course_123', 'course_234'};
      setupTestLocator((locator) => locator.registerLazySingleton<CalendarFilterListInteractor>(() => interactor));

      await tester.pumpWidget(TestApp(
        CalendarFilterListScreen(selectedContexts),
      ));
      await tester.pump();
      await tester.pump();

      CalendarFilterListScreenState state = await tester.state(find.byType(CalendarFilterListScreen));

      expect(find.byType(Checkbox), findsNWidgets(3));

      var checkedCheckBoxFinder = find.byWidgetPredicate((widget) {
        if (widget is Checkbox) {
          // Check for a checkbox widgets that are checked
          final Checkbox checkboxWidget = widget;
          return checkboxWidget.value!;
        }
        return false;
      });

      // Make sure we've got the correct selected number of items
      expect(checkedCheckBoxFinder, findsNWidgets(2));

      // Click on the first course, deselecting it
      expect(find.text('Course1'), findsOneWidget);
      await tester.tap(find.text('Course1'));
      await tester.pumpAndSettle(Duration(seconds: 4)); // Let checkbox animation finish

      // Check to make sure we have no items selected
      expect(checkedCheckBoxFinder, findsOneWidget);

      // Make sure the selected list was updated
      expect(state.selectedContextIds.length, 1);
    });

    testWidgetsWithAccessibilityChecks('attempting to deselect the last calendar shows an error', (tester) async {
      var interactor = MockCalendarFilterListInteractor();
      when(interactor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => Future.value(_mockCourses()));

      Set<String> selectedContexts = {
        'course_123',
      };
      setupTestLocator((locator) => locator.registerLazySingleton<CalendarFilterListInteractor>(() => interactor));

      await tester.pumpWidget(TestApp(
        CalendarFilterListScreen(selectedContexts),
      ));
      await tester.pump();
      await tester.pump();

      CalendarFilterListScreenState state = await tester.state(find.byType(CalendarFilterListScreen));

      expect(find.byType(Checkbox), findsNWidgets(3));

      var checkedCheckBoxFinder = find.byWidgetPredicate((widget) {
        if (widget is Checkbox) {
          // Check for a checkbox widgets that are checked
          final Checkbox checkboxWidget = widget;
          return checkboxWidget.value!;
        }
        return false;
      }, skipOffstage: false);

      // Make sure we've got the correct selected number of items
      expect(checkedCheckBoxFinder, findsNWidgets(1));

      // attempt to deselect the only selected course
      expect(find.text('Course1'), findsOneWidget);
      await tester.tap(find.text('Course1'));
      await tester.pump();

      expect(find.text(AppLocalizations().minimumCalendarsError), findsOneWidget);

      var selectedCount = state.selectedContextIds.length;
      expect(selectedCount, 1);
    });
  });
}

List<Course> _mockCourses() {
  return [
    Course((b) => b
      ..id = '123'
      ..name = 'Course1'),
    Course((b) => b
      ..id = '234'
      ..name = 'Course2'),
    Course((b) => b
      ..id = '345'
      ..name = 'Course3')
  ];
}

List<Course> _mockCoursesBigList() {
  return [
    Course((b) => b
      ..id = '1'
      ..name = 'Course1'),
    Course((b) => b
      ..id = '2'
      ..name = 'Course2'),
    Course((b) => b
      ..id = '3'
      ..name = 'Course3'),
    Course((b) => b
      ..id = '4'
      ..name = 'Course4'),
    Course((b) => b
      ..id = '5'
      ..name = 'Course5'),
    Course((b) => b
      ..id = '6'
      ..name = 'Course6'),
    Course((b) => b
      ..id = '7'
      ..name = 'Course7'),
    Course((b) => b
      ..id = '8'
      ..name = 'Course8'),
    Course((b) => b
      ..id = '9'
      ..name = 'Course9'),
    Course((b) => b
      ..id = '10'
      ..name = 'Course10'),
    Course((b) => b
      ..id = '11'
      ..name = 'Course11'),
  ];
}
