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
import 'package:flutter/services.dart';
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/models/plannable.dart';
import 'package:flutter_student_embed/models/planner_item.dart';
import 'package:flutter_student_embed/network/api/planner_api.dart';
import 'package:flutter_student_embed/screens/to_do/create_update_to_do_screen.dart';
import 'package:flutter_student_embed/screens/to_do/to_do_details_screen.dart';
import 'package:flutter_student_embed/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_student_embed/utils/design/student_colors.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../testutils/accessibility_utils.dart';
import '../../testutils/finders.dart';
import '../../testutils/mock_helpers.dart';
import '../../testutils/test_app.dart';

void main() {
  final simplePlannerItem = PlannerItem((b) => b
    ..courseId = 'course123'
    ..contextName = 'Cousre 123 yo'
    ..contextType = 'course'
    ..plannableType = 'assignment'
    ..plannableDate = DateTime.now()
    ..plannable = Plannable((p) => p
      ..id = '123'
      ..courseId = 'course123'
      ..title = 'Assignment Plannable'
      ..toDoDate = DateTime.now()
      ..assignmentId = 'assignment123'
      ..details = 'Assignment deets').toBuilder());

  final plannerApi = MockPlannerApi();
  final quickNav = MockQuickNav();

  setupTestLocator((locator) {
    locator.registerLazySingleton<PlannerApi>(() => plannerApi);
    locator.registerLazySingleton<QuickNav>(() => quickNav);
  });

  setUp(() {
    StudentColors.reset();
    reset(plannerApi);
    reset(quickNav);
  });

  group('renders', () {
    testWidgetsWithAccessibilityChecks('with all data', (tester) async {
      final item = simplePlannerItem;
      final plannable = simplePlannerItem.plannable;
      final contextColor = Color(0xFF4452A6); // Ultramarine (one of the colors that passes the contrast checker)
      StudentColors.contextColors.putIfAbsent(item.contextCode(), () => contextColor);

      await tester.pumpWidget(TestApp(ToDoDetailsScreen(item)));
      await tester.pump();

      expect((tester.widget(find.text(simplePlannerItem.contextName)) as Text).style.color, contextColor);
      expect(find.text(plannable.title), findsOneWidget);
      expect(find.text(AppLocalizations().date), findsOneWidget);
      expect(find.text(plannable.toDoDate.l10nFormat(AppLocalizations().dateAtTime)), findsOneWidget);
      expect(find.text(AppLocalizations().descriptionLabel), findsOneWidget);
      expect(find.text(plannable.details), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('with empty description', (tester) async {
      final plannable = simplePlannerItem.plannable.rebuild((b) => b..details = '');
      final item = simplePlannerItem.rebuild((b) => b..plannable = plannable.toBuilder());

      await tester.pumpWidget(TestApp(ToDoDetailsScreen(item)));
      await tester.pump();

      expect(find.text(plannable.title), findsOneWidget);
      expect(find.text(AppLocalizations().date), findsOneWidget);
      expect(find.text(plannable.toDoDate.l10nFormat(AppLocalizations().dateAtTime)), findsOneWidget);
      expect(find.text(AppLocalizations().descriptionLabel), findsOneWidget);
      expect(find.text(AppLocalizations().noToDoDescription), findsOneWidget);
    });
  });

  group('PopupMenu edit', () {
    testWidgetsWithAccessibilityChecks('shows and dismisses without pop', (tester) async {
      when(quickNav.push(any, any)).thenAnswer((_) async => null);

      final item = simplePlannerItem;

      // Show from a tap so we could pop
      await TestApp.showWidgetFromTap(tester, (context) => QuickNav().push(context, ToDoDetailsScreen(item)));

      final popupMenu = find.byType(typeOf<PopupMenuButton<int>>());
      expect(popupMenu, findsOneWidget);

      await tester.tap(popupMenu);
      await tester.pumpAndSettle();

      final editButton = find.text(AppLocalizations().edit);
      expect(editButton, findsOneWidget);

      await tester.tap(editButton);
      await tester.pumpAndSettle();

      var verification = verify(quickNav.push(any, captureAny));

      verification.called(1);
      expect(verification.captured[0], isA<CreateUpdateToDoScreen>());
      expect(find.byType(ToDoDetailsScreen), findsOneWidget); // Should still show this details
    });

    testWidgetsWithAccessibilityChecks('shows and dismisses with pop', (tester) async {
      when(quickNav.push(any, any)).thenAnswer((_) async => true);

      final item = simplePlannerItem;

      // Show from a tap so we could pop
      await TestApp.showWidgetFromTap(tester, (context) => QuickNav().push(context, ToDoDetailsScreen(item)));

      final popupMenu = find.byType(typeOf<PopupMenuButton<int>>());
      expect(popupMenu, findsOneWidget);

      await tester.tap(popupMenu);
      await tester.pumpAndSettle();

      final editButton = find.text(AppLocalizations().edit);
      expect(editButton, findsOneWidget);

      await tester.tap(editButton);
      await tester.pumpAndSettle();

      var verification = verify(quickNav.push(any, captureAny));

      verification.called(1);
      expect(verification.captured[0], isA<CreateUpdateToDoScreen>());
      expect(find.byType(ToDoDetailsScreen), findsNothing); // Should be popped
    });
  });

  group('PopupMenu delete', () {
    testWidgetsWithAccessibilityChecks('shows and dismisses when cancelled', (tester) async {
      final String channelId = "ToDoChannel";
      final item = simplePlannerItem;

      await tester.pumpWidget(TestApp(ToDoDetailsScreen(item, channelId: channelId)));
      await tester.pump();

      final popupMenu = find.byType(typeOf<PopupMenuButton<int>>());
      expect(popupMenu, findsOneWidget);

      await tester.tap(popupMenu);
      await tester.pumpAndSettle();

      final deleteButton = find.text(AppLocalizations().delete);
      expect(deleteButton, findsOneWidget);

      MethodCall methodCall;
      MethodChannel(channelId).setMockMethodCallHandler((MethodCall call) async {
        methodCall = call;
        return false; // We return false from the platform dialog if the user dismisses it or clicks cancel.
      });

      await tester.tap(deleteButton);
      await tester.pumpAndSettle();

      expect(methodCall.method, 'showDialog');
      expect(methodCall.arguments, {
        "title": AppLocalizations().areYouSure,
        "message": AppLocalizations().deleteToDoConfirmationMessage,
        "positiveButtonText": AppLocalizations().delete,
        "negativeButtonText": AppLocalizations().cancel
      });

      verifyNever(plannerApi.deletePlannerNote(any));
    });

    testWidgetsWithAccessibilityChecks('shows loading and errors', (tester) async {
      final String channelId = "ToDoChannel";
      final completer = Completer<Plannable>();
      final item = simplePlannerItem;
      when(plannerApi.deletePlannerNote(any)).thenAnswer((_) => completer.future);

      await tester.pumpWidget(TestApp(ToDoDetailsScreen(item, channelId: channelId)));
      await tester.pump();

      final popupMenu = find.byType(typeOf<PopupMenuButton<int>>());
      expect(popupMenu, findsOneWidget);

      await tester.tap(popupMenu);
      await tester.pumpAndSettle();

      final deleteButton = find.text(AppLocalizations().delete);
      expect(deleteButton, findsOneWidget);

      MethodCall methodCall;
      MethodChannel(channelId).setMockMethodCallHandler((MethodCall call) async {
        methodCall = call;
        return true; // We return true from the platform dialog if the user clicks delete.
      });

      await tester.tap(deleteButton);
      await tester.pump();

      expect(methodCall.method, 'showDialog');
      expect(methodCall.arguments, {
        "title": AppLocalizations().areYouSure,
        "message": AppLocalizations().deleteToDoConfirmationMessage,
        "positiveButtonText": AppLocalizations().delete,
        "negativeButtonText": AppLocalizations().cancel
      });

      expect(find.byType(CircularProgressIndicator), findsOneWidget);

      completer.completeError('Intentional Test Error');
      await tester.pump();

      expect(find.byType(SnackBar), findsOneWidget);
      expect(find.text(AppLocalizations().errorDeletingToDo), findsOneWidget);
      verify(plannerApi.deletePlannerNote(any)).called(1);
    });

    testWidgetsWithAccessibilityChecks('can delete items', (tester) async {
      final String channelId = "ToDoChannel";
      final item = simplePlannerItem;
      when(plannerApi.deletePlannerNote(any)).thenAnswer((_) async => item.plannable);

      await tester.pumpWidget(TestApp(ToDoDetailsScreen(item, channelId: channelId)));
      await tester.pump();

      final popupMenu = find.byType(typeOf<PopupMenuButton<int>>());
      expect(popupMenu, findsOneWidget);

      await tester.tap(popupMenu);
      await tester.pumpAndSettle();

      MethodCall methodCall;
      MethodChannel(channelId).setMockMethodCallHandler((MethodCall call) async {
        methodCall = call;
        return true; // We return true from the platform dialog if the user clicks delete.
      });

      final deleteButton = find.text(AppLocalizations().delete);
      expect(deleteButton, findsOneWidget);

      await tester.tap(deleteButton);
      await tester.pump();

      expect(methodCall.method, 'showDialog');
      expect(methodCall.arguments, {
        "title": AppLocalizations().areYouSure,
        "message": AppLocalizations().deleteToDoConfirmationMessage,
        "positiveButtonText": AppLocalizations().delete,
        "negativeButtonText": AppLocalizations().cancel
      });

      // Loading finished and there is no error
      expect(find.byType(CircularProgressIndicator), findsNothing);
      expect(find.byType(SnackBar), findsNothing);
      expect(find.text(AppLocalizations().errorDeletingToDo), findsNothing);
      verify(plannerApi.deletePlannerNote(any)).called(1);
    });
  });
}
