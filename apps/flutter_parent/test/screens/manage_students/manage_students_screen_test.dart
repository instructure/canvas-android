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

import 'dart:async';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/models/user_color.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_interactor.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_screen.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_interactor.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_screen.dart';
import 'package:flutter_parent/screens/manage_students/student_color_picker_dialog.dart';
import 'package:flutter_parent/screens/pairing/pairing_util.dart';
import 'package:flutter_parent/utils/db/user_colors_db.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/network_image_response.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  mockNetworkImageResponse();
  final analytics = MockAnalytics();
  final MockPairingUtil pairingUtil = MockPairingUtil();
  final MockUserColorsDb userColorsDb = MockUserColorsDb();

  _setupLocator([MockManageStudentsInteractor? interactor]) async {
    final locator = GetIt.instance;
    await locator.reset();

    var thresholdInteractor = MockAlertThresholdsInteractor();
    when(thresholdInteractor.getAlertThresholdsForStudent(any)).thenAnswer((_) => Future.value([]));

    locator.registerFactory<AlertThresholdsInteractor>(() => thresholdInteractor);
    locator.registerFactory<ManageStudentsInteractor>(() => interactor ?? MockManageStudentsInteractor());
    locator.registerFactory<QuickNav>(() => QuickNav());
    locator.registerLazySingleton<Analytics>(() => analytics);
    locator.registerLazySingleton<PairingUtil>(() => pairingUtil);
    locator.registerLazySingleton<UserColorsDb>(() => userColorsDb);
  }

  setUp(() {
    reset(analytics);
    reset(pairingUtil);
    reset(userColorsDb);
  });

  Future<void> _clickFAB(WidgetTester? tester) async {
    await tester?.tap(find.byType(FloatingActionButton));
    await tester?.pumpAndSettle();
  }

  group('Refresh', () {
    testWidgetsWithAccessibilityChecks('Pulling gets list of students', (tester) async {
      var preRefreshStudent = [CanvasModelTestUtils.mockUser(shortName: 'Billy')];
      var postRefreshStudent = [CanvasModelTestUtils.mockUser(shortName: 'Sally')];

      // Mock the behavior of the interactor to return a student
      final interactor = MockManageStudentsInteractor();
      when(interactor.getStudents(forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value(postRefreshStudent));
      _setupLocator(interactor);

      // Start the page with a single student
      await tester.pumpWidget(TestApp(ManageStudentsScreen(preRefreshStudent)));
      await tester.pumpAndSettle();

      // Check if we're showing the initial student
      expect(find.text('Billy'), findsOneWidget);

      // Pull to refresh\
      final matchedWidget = find.byType(RefreshIndicator);
      await tester.drag(matchedWidget, const Offset(0, 200));
      await tester.pumpAndSettle();

      // See if we got our new student
      expect(find.text('Sally'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Error on pull to refresh', (tester) async {
      var interactor = MockManageStudentsInteractor();
      Completer<List<User>?> completer = Completer<List<User>?>();
      when(interactor.getStudents(forceRefresh: anyNamed('forceRefresh'))).thenAnswer((_) => completer.future);
      _setupLocator(interactor);

      // Start the screen with no users
      await tester.pumpWidget(TestApp(ManageStudentsScreen([])));
      await tester.pumpAndSettle();

      // Pull to refresh
      await tester.drag(find.byType(RefreshIndicator), const Offset(0, 250));
      await tester.pump();
      await tester.pump(Duration(milliseconds: 300));

      // Make sure we called into the interactor to get the student list
      verify(interactor.getStudents(forceRefresh: true)).called(1);

      completer.completeError('Fake Error');
      await tester.pumpAndSettle();

      // Check if we show the error message
      expect(find.text(AppLocalizations().errorLoadingStudents), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Retry button on error page loads students', (tester) async {
      var observedStudents = [CanvasModelTestUtils.mockUser(shortName: 'Billy')];

      // Mock interactor to return an error when retrieving student list
      var interactor = MockManageStudentsInteractor();
      Completer<List<User>?> completer = Completer<List<User>?>();
      when(interactor.getStudents(forceRefresh: anyNamed('forceRefresh'))).thenAnswer((_) => completer.future);
      _setupLocator(interactor);

      // Start the page with a single student
      await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudents)));
      await tester.pumpAndSettle();

      // Pull to refresh, causing an error which will show the error screen with the retry button
      await tester.drag(find.byType(RefreshIndicator), const Offset(0, 250));
      await tester.pump();
      await tester.pump(Duration(milliseconds: 300));

      // Make sure we called into the interactor to get the student list
      verify(interactor.getStudents(forceRefresh: true)).called(1);

      completer.completeError('Fake Error');
      await tester.pumpAndSettle();

      // Tap retry button to refresh list
      await tester.tap(find.text(AppLocalizations().retry));
      await tester.pumpAndSettle();

      // Change the interactor to return a student instead of an error
      when(interactor.getStudents(forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value(observedStudents));

      await tester.drag(find.byType(RefreshIndicator), const Offset(0, 200));
      await tester.pumpAndSettle();

      // See if we got the student back from the retry
      expect(find.text('Billy'), findsOneWidget);
    });
  });

  group('Student List', () {
    testWidgetsWithAccessibilityChecks('Displays', (tester) async {
      _setupLocator();

      var observedStudents = [
        CanvasModelTestUtils.mockUser(name: 'Billy'),
        CanvasModelTestUtils.mockUser(name: 'Sally'),
        CanvasModelTestUtils.mockUser(name: 'Trevor'),
      ];

      // Start the page with three students
      await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudents)));
      await tester.pumpAndSettle();

      // See if we are showing the list tiles for those students
      expect(find.byType(ListTile), findsNWidgets(3));
    });

    testWidgetsWithAccessibilityChecks('Displays username', (tester) async {
      _setupLocator();

      var observedStudents = [
        CanvasModelTestUtils.mockUser(shortName: 'Billy', pronouns: null),
      ];

      // Start the page with a user that has no pronouns set
      await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudents)));
      await tester.pumpAndSettle();

      // See if we displaying the username
      expect(find.text('Billy'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Display username and pronouns ', (tester) async {
      _setupLocator();

      var observedStudents = [
        CanvasModelTestUtils.mockUser(shortName: 'Billy', pronouns: 'he/him'),
      ];

      // Start the page with a user that has pronouns set
      await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudents)));
      await tester.pumpAndSettle();

      // See if we are correctly displaying the username and pronouns of the user
      expect(find.text('Billy (he/him)'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Displays stored user color', (tester) async {
      _setupLocator();

      var observedStudents = [
        CanvasModelTestUtils.mockUser(shortName: 'Billy', id: '123'),
      ];

      UserColor userColor = UserColor((b) => b..color = Colors.pinkAccent);
      when(userColorsDb.getByContext(any, any, 'user_123')).thenAnswer((_) async => userColor);

      await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudents)));
      await tester.pumpAndSettle();

      // Should show the color circle
      var key = Key('color-circle-123');
      expect(find.byKey(key), findsOneWidget);

      // Circle should be the correct color
      Container circleContainer = tester.widget<Container>(find.byKey(key));
      expect((circleContainer.decoration as BoxDecoration).color, userColor.color);
    });

    testWidgetsWithAccessibilityChecks('Displays default user color', (tester) async {
      _setupLocator();

      int studentIndex = 2;
      var expectedColor = StudentColorSet.all[studentIndex].light;

      var observedStudents = [
        CanvasModelTestUtils.mockUser(shortName: 'Billy', id: studentIndex.toString()),
      ];

      await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudents)));
      await tester.pumpAndSettle();

      // Should show the color circle
      var key = Key('color-circle-$studentIndex');
      expect(find.byKey(key), findsOneWidget);

      // Circle should be the correct color
      Container circleContainer = tester.widget<Container>(find.byKey(key));
      expect((circleContainer.decoration as BoxDecoration).color, expectedColor);
    });

    testWidgetsWithAccessibilityChecks('Clicking student color opens color picker', (tester) async {
      _setupLocator();

      var observedStudents = [
        CanvasModelTestUtils.mockUser(shortName: 'Billy', id: '123'),
      ];

      // Start the page with the user
      await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudents)));
      await tester.pumpAndSettle();

      // Tap the color circle
      var key = Key('color-circle-123');
      await tester.tap(find.byKey(key));
      await tester.pumpAndSettle();

      // Should show the color picker dialog
      expect(find.byType(StudentColorPickerDialog), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Empty when null', (tester) async {
      _setupLocator();

      // Start the page with a 'null' list of students
      await tester.pumpWidget(TestApp(
        ManageStudentsScreen(null),
      ));
      await tester.pumpAndSettle();

      // See if we are showing the empty message
      expect(find.text(AppLocalizations().emptyStudentList), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Empty when… empty', (tester) async {
      _setupLocator();

      List<User> observedStudents = [];

      // Start the page with an empty list of students
      await tester.pumpWidget(TestApp(
        ManageStudentsScreen(observedStudents),
      ));
      await tester.pumpAndSettle();

      // See if we are showing the empty message
      expect(find.text(AppLocalizations().emptyStudentList), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Tap goes to the Threshold Screen', (tester) async {
      _setupLocator();

      var observedStudent = [CanvasModelTestUtils.mockUser(shortName: 'Billy')];

      await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudent)));
      await tester.pumpAndSettle();

      // Make sure the user was loaded
      expect(find.text('Billy'), findsOneWidget);

      // Tap on the user
      await tester.tap(find.text('Billy'));

      // Pump and settle the page transition animation
      await tester.pump();
      await tester.pump();
      await tester.pumpAndSettle();

      // Find the thresholds screen
      expect(find.byType(AlertThresholdsScreen), findsOneWidget);
    });
  });

  group('Add Student', () {
    testWidgetsWithAccessibilityChecks('Displays FAB for pairing', (tester) async {
      var interactor = MockManageStudentsInteractor();
      _setupLocator(interactor);

      await tester.pumpWidget(TestApp(
        ManageStudentsScreen([]),
      ));
      await tester.pumpAndSettle();

      // Should display FAB
      expect(find.byType(FloatingActionButton), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Tapping FAB calls PairingUtil', (tester) async {
      var interactor = MockManageStudentsInteractor();
      _setupLocator(interactor);

      var observedStudents = [CanvasModelTestUtils.mockUser(name: 'Billy')];

      await tester.pumpWidget(TestApp(
        ManageStudentsScreen(observedStudents),
      ));
      await tester.pumpAndSettle();

      await _clickFAB(tester);
      await tester.pumpAndSettle();

      // Verify that PairingUtil was called
      verify(pairingUtil.pairNewStudent(any, any));
    });

    testWidgetsWithAccessibilityChecks('Refresh list on success', (tester) async {
      var observedStudent = [CanvasModelTestUtils.mockUser(shortName: 'Billy')];

      // Mock return value for success when pairing a student
      final interactor = MockManageStudentsInteractor();
      when(pairingUtil.pairNewStudent(any, any)).thenAnswer((inv) => inv.positionalArguments[1]());

      // Mock retrieving students, also add an extra student to the list
      when(interactor.getStudents(forceRefresh: anyNamed('forceRefresh'))).thenAnswer((_) {
        observedStudent.add(CanvasModelTestUtils.mockUser(shortName: 'Trevor'));
        return Future.value(observedStudent);
      });

      _setupLocator(interactor);

      // Setup page
      await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudent)));
      await tester.pumpAndSettle();

      // Make sure we only have one student
      expect(find.byType(ListTile), findsNWidgets(1));
      expect(find.text('Billy'), findsOneWidget);

      // Click FAB
      await _clickFAB(tester);
      await tester.pumpAndSettle();

      // Make sure we made the call to get students
      verify(interactor.getStudents(forceRefresh: true)).called(1);

      // Check for two students in the list
      expect(find.byType(ListTile), findsNWidgets(2));
      expect(find.text('Billy'), findsOneWidget);
      expect(find.text('Trevor'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('screen returns true when paired successfully', (tester) async {
      var observedStudent = [CanvasModelTestUtils.mockUser(shortName: 'Billy', id: "1771")];

      // Mock return value for success when pairing a student
      final interactor = MockManageStudentsInteractor();
      when(pairingUtil.pairNewStudent(any, any)).thenAnswer((inv) => inv.positionalArguments[1]());

      // Mock retrieving students, also add an extra student to the list
      when(interactor.getStudents(forceRefresh: anyNamed('forceRefresh'))).thenAnswer((_) {
        observedStudent.add(CanvasModelTestUtils.mockUser(shortName: 'Trevor'));
        return Future.value(observedStudent);
      });

      _setupLocator(interactor);

      final observer = MockNavigatorObserver();

      // Setup page
      await _pumpTestableWidgetWithBackButton(tester, ManageStudentsScreen(observedStudent), observer);

      // Make sure we only have one student
      expect(find.byType(ListTile), findsNWidgets(1));
      expect(find.text('Billy'), findsOneWidget);

      // Click FAB
      await _clickFAB(tester);
      await tester.pumpAndSettle();

      // Make sure we made the call to get students
      verify(interactor.getStudents(forceRefresh: true)).called(1);

      // Check for two students in the list
      expect(find.byType(ListTile), findsNWidgets(2));
      expect(find.text('Billy'), findsOneWidget);
      expect(find.text('Trevor'), findsOneWidget);

      // Setup for getting the popped result from the Manage Students screen
      bool studentAdded = false;
      Route route = verify(observer.didPush(captureAny, any)).captured[1];
      route.popped.then((value) => studentAdded = value);

      // Go back to the widget with the back button
      await tester.pageBack();
      await tester.pumpAndSettle(Duration(seconds: 1));

      expect(studentAdded, true);
    });
  });
}

/// Load up a temp page with a button to navigate to our screen, that way the back button exists in the app bar
Future<void> _pumpTestableWidgetWithBackButton(tester, Widget widget, MockNavigatorObserver observer) async {
  var mockObserver = MockNavigatorObserver();
  final app = TestApp(
    Builder(
      builder: (context) => TextButton(
        child: Semantics(label: 'test', child: const SizedBox()),
        onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: (context) => widget)),
      ),
    ),
    navigatorObservers: [mockObserver, if (observer != null) observer],
  );

  await tester.pumpWidget(app);
  await tester.pumpAndSettle();
  await tester.tap(find.byType(TextButton));
  await tester.pumpAndSettle();
  verify(mockObserver.didPush(any, any)).called(2); // Twice, first for the initial page, then for the navigator route
}