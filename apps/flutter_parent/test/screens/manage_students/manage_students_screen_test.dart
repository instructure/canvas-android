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
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_interactor.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_screen.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_interactor.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_screen.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/network_image_response.dart';
import '../../utils/test_app.dart';

void main() {
  mockNetworkImageResponse();
  final analytics = _MockAnalytics();

  _setupLocator([_MockManageStudentsInteractor interactor]) {
    final locator = GetIt.instance;
    locator.reset();

    var thresholdInteractor = _MockAlertThresholdsInteractor();
    when(thresholdInteractor.getAlertThresholdsForStudent(any)).thenAnswer((_) => Future.value([]));

    locator.registerFactory<AlertThresholdsInteractor>(() => thresholdInteractor);
    locator.registerFactory<ManageStudentsInteractor>(() => interactor ?? _MockManageStudentsInteractor());
    locator.registerFactory<QuickNav>(() => QuickNav());
    locator.registerLazySingleton<Analytics>(() => analytics);
  }

  setUp(() {
    reset(analytics);
  });

  void _clickFAB(WidgetTester tester) async {
    await tester.tap(find.byType(FloatingActionButton));
    await tester.pumpAndSettle();
  }

  void _clickQR(WidgetTester tester) async {
    await tester.tap(find.text(AppLocalizations().qrCode));
    await tester.pumpAndSettle();
  }

  group('Refresh', () {
    testWidgetsWithAccessibilityChecks('Pulling gets list of students', (tester) async {
      var preRefreshStudent = [CanvasModelTestUtils.mockUser(shortName: 'Billy')];
      var postRefreshStudent = [CanvasModelTestUtils.mockUser(shortName: 'Sally')];

      // Mock the behavior of the interactor to return a student
      final interactor = _MockManageStudentsInteractor();
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
      var interactor = _MockManageStudentsInteractor();
      Completer completer = Completer<List<User>>();
      when(interactor.getStudents(forceRefresh: anyNamed('forceRefresh'))).thenAnswer((_) => completer.future);
      _setupLocator(interactor);

      // Start the screen with no users
      await tester.pumpWidget(TestApp(ManageStudentsScreen([]), highContrast: true));
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
      var interactor = _MockManageStudentsInteractor();
      Completer completer = Completer<List<User>>();
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

    testWidgetsWithAccessibilityChecks('Empty when null', (tester) async {
      _setupLocator();

      // Start the page with a 'null' list of students
      await tester.pumpWidget(TestApp(
        ManageStudentsScreen(null),
        highContrast: true,
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
        highContrast: true,
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

      // Find the thresholds screen
      expect(find.byType(AlertThresholdsScreen), findsOneWidget);
    });
  });

  group('Add Student', () {
    testWidgetsWithAccessibilityChecks('Tapping FAB opens bottom sheet', (tester) async {
      _setupLocator();

      var observedStudents = [
        CanvasModelTestUtils.mockUser(name: 'Billy', pronouns: 'he/him'),
      ];

      await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudents), highContrast: true));
      await tester.pumpAndSettle();

      await _clickFAB(tester);

      // Check if we are showing the header text of the bottom sheet
      expect(find.text(AppLocalizations().addStudentWith), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('QR code tap calls into QR reader via interactor', (tester) async {
      final interactor = _MockManageStudentsInteractor();
      when(interactor.getQrReading()).thenAnswer((_) => Future.value(''));

      _setupLocator(interactor);

      await tester.pumpWidget(
          TestApp(ManageStudentsScreen([CanvasModelTestUtils.mockUser(name: 'Canvas')]), highContrast: true));
      await tester.pumpAndSettle();

      await _clickFAB(tester);

      await _clickQR(tester);

      verify(interactor.getQrReading());
    });

    testWidgetsWithAccessibilityChecks('Pairing Code opens dialog', (tester) async {
      _setupLocator();

      var observedStudents = [CanvasModelTestUtils.mockUser(name: 'Billy')];

      // Start the page in high contrast mode with a single user
      // (the bottom sheet header text doesn't pass our a11y contrast ratio test by default)
      await tester.pumpWidget(TestApp(
        ManageStudentsScreen(observedStudents),
        highContrast: true,
      ));
      await tester.pumpAndSettle();

      await _clickFAB(tester);

      // Click pairing code option
      await tester.tap(find.text(AppLocalizations().pairingCode));
      await tester.pumpAndSettle();

      // Check for the dialog
      expect(find.byType(Dialog), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Refresh list on success', (tester) async {
      var observedStudent = [CanvasModelTestUtils.mockUser(shortName: 'Billy')];

      // Mock return value for success when pairing a student
      final interactor = _MockManageStudentsInteractor();
      when(interactor.pairWithStudent(any)).thenAnswer((_) => Future.value(true));

      // Mock retrieving students, also add an extra student to the list
      when(interactor.getStudents(forceRefresh: anyNamed('forceRefresh'))).thenAnswer((_) {
        observedStudent.add(CanvasModelTestUtils.mockUser(shortName: 'Trevor'));
        return Future.value(observedStudent);
      });

      _setupLocator(interactor);

      // Setup page
      await tester.pumpWidget(TestApp(ManageStudentsScreen(observedStudent), highContrast: true));
      await tester.pumpAndSettle();

      // Make sure we only have one student
      expect(find.byType(ListTile), findsNWidgets(1));
      expect(find.text('Billy'), findsOneWidget);

      // Click FAB
      await _clickFAB(tester);

      // Click pairing code option
      await tester.tap(find.text(AppLocalizations().pairingCode));
      await tester.pumpAndSettle();

      // Enter code
      await tester.enterText(find.byType(TextFormField), 'canvas');
      await tester.pumpAndSettle();

      // Tap 'OK'
      await tester.tap(find.text('OK'));
      await tester.pumpAndSettle();

      // Make sure we made the call to get students
      verify(interactor.getStudents(forceRefresh: true)).called(1);
      verify(analytics.logEvent(AnalyticsEventConstants.ADD_STUDENT_MANAGE_STUDENTS)).called(1);

      // Make sure the dialog is gone
      expect(find.byType(AlertDialog), findsNothing);

      // Check for two students in the list
      expect(find.byType(ListTile), findsNWidgets(2));
      expect(find.text('Billy'), findsOneWidget);
      expect(find.text('Trevor'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Show error on fail', (tester) async {
      var observedStudents = [CanvasModelTestUtils.mockUser(name: 'Billy')];

      // Set the interactor to return false when trying to pair with a student
      final interactor = _MockManageStudentsInteractor();
      when(interactor.pairWithStudent(any)).thenAnswer((_) => Future.value(false));
      _setupLocator(interactor);

      // Setup the main widget
      await tester.pumpWidget(TestApp(
        ManageStudentsScreen(observedStudents),
        highContrast: true,
      ));
      await tester.pumpAndSettle();

      // Click FAB
      await _clickFAB(tester);

      // Click on the pairing code option in bottom sheet
      await tester.tap(find.text(AppLocalizations().pairingCode));
      await tester.pumpAndSettle();

      // Click OK in Add Student Dialog
      await tester.tap(find.text(AppLocalizations().ok));
      await tester.pumpAndSettle();

      // Check for error message
      expect(find.text(AppLocalizations().errorPairingFailed), findsOneWidget);
    });
  });
}

class _MockManageStudentsInteractor extends Mock implements ManageStudentsInteractor {}

class _MockAlertThresholdsInteractor extends Mock implements AlertThresholdsInteractor {}

class _MockAnalytics extends Mock implements Analytics {}
