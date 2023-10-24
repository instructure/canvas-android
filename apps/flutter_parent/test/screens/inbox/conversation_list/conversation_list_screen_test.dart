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

import 'package:built_collection/built_collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/basic_user.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/inbox/conversation_list/conversation_list_interactor.dart';
import 'package:flutter_parent/screens/inbox/conversation_list/conversation_list_screen.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_svg/svg.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:intl/intl.dart';
import 'package:mockito/mockito.dart';

import '../../../utils/accessibility_utils.dart';
import '../../../utils/network_image_response.dart';
import '../../../utils/test_app.dart';
import '../../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  mockNetworkImageResponse();
  final l10n = AppLocalizations();

  testWidgetsWithAccessibilityChecks('Displays loading state', (tester) async {
    var interactor = MockConversationListInteractor();
    setupTestLocator((locator) => locator.registerFactory<ConversationListInteractor>(() => interactor));

    var completer = Completer<List<Conversation>>();
    when(interactor.getConversations()).thenAnswer((_) => completer.future);

    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pump();

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });

  // TODO Fix test
  testWidgetsWithAccessibilityChecks('Displays empty state', (tester) async {
    var interactor = MockConversationListInteractor();
    setupTestLocator((locator) => locator.registerFactory<ConversationListInteractor>(() => interactor));

    when(interactor.getConversations()).thenAnswer((_) => Future.value([]));

    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.text(l10n.emptyInboxTitle), findsOneWidget);
    expect(find.text(l10n.emptyInboxSubtitle), findsOneWidget);
  }, skip: true);

  // TODO Fix test
  testWidgetsWithAccessibilityChecks('Displays error state with retry', (tester) async {
    var interactor = MockConversationListInteractor();
    setupTestLocator((locator) => locator.registerFactory<ConversationListInteractor>(() => interactor));

    when(interactor.getConversations()).thenAnswer((_) => Future.error('Error'));

    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    // Expect error state
    var errorIcon = await tester.widget<Icon>(find.byType(Icon).first).icon;
    expect(errorIcon, equals(CanvasIcons.warning));
    expect(find.text('There was an error loading your inbox messages.'), findsOneWidget);
    expect(find.widgetWithText(TextButton, l10n.retry), findsOneWidget);

    // Retry with success
    reset(interactor);
    when(interactor.getConversations(forceRefresh: anyNamed('forceRefresh'))).thenAnswer((_) => Future.value([]));
    await tester.tap(find.text(l10n.retry));
    await tester.pumpAndSettle(Duration(seconds: 1));

    // Should no longer show error state
    expect(find.text('There was an error loading your inbox messages.'), findsNothing);
    expect(find.widgetWithText(TextButton, l10n.retry), findsNothing);
  }, skip: true);

  testWidgetsWithAccessibilityChecks('Displays subject, course name, message preview, and date', (tester) async {
    var interactor = MockConversationListInteractor();
    setupTestLocator((locator) => locator.registerFactory<ConversationListInteractor>(() => interactor));

    var now = DateTime.now();
    var messageDate = DateTime(now.year, 12, 25, now.hour, now.minute, 0, 0, 0);

    var conversation = Conversation((b) => b
      ..contextName = 'Test Course'
      ..subject = 'Message Subject'
      ..lastMessage = 'Last Message'
      ..lastMessageAt = messageDate);

    when(interactor.getConversations()).thenAnswer((_) => Future.value([conversation]));
    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    expect(find.text(conversation.subject), findsOneWidget);
    expect(find.text(conversation.contextName!), findsOneWidget);
    expect(find.text(conversation.lastMessage!), findsOneWidget);
    expect(find.text('Dec 25'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Adds year to date if not this year', (tester) async {
    var interactor = MockConversationListInteractor();
    setupTestLocator((locator) => locator.registerFactory<ConversationListInteractor>(() => interactor));

    var now = DateTime.now();
    var messageDate = DateTime(now.year - 1, 12, 25, now.hour, 0, 0, 0, 0);

    var conversation = Conversation((b) => b
      ..contextName = ''
      ..lastMessage = ''
      ..lastMessageAt = messageDate);

    when(interactor.getConversations()).thenAnswer((_) => Future.value([conversation]));
    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    expect(find.text('Dec 25 ${now.year - 1}'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays time if date is today', (tester) async {
    var interactor = MockConversationListInteractor();
    setupTestLocator((locator) => locator.registerFactory<ConversationListInteractor>(() => interactor));

    var now = DateTime.now();

    var conversation = Conversation((b) => b
      ..contextName = ''
      ..lastMessage = ''
      ..lastMessageAt = now);

    when(interactor.getConversations()).thenAnswer((_) => Future.value([conversation]));
    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    expect(find.text(DateFormat.jm().format(now)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays single avatar for single participant', (tester) async {
    var interactor = MockConversationListInteractor();
    setupTestLocator((locator) => locator.registerFactory<ConversationListInteractor>(() => interactor));

    var now = DateTime.now();
    var messageDate = DateTime(now.year, 12, 25, now.hour, 0, 0, 0, 0);

    var conversation = Conversation((b) => b
      ..contextName = ''
      ..lastMessage = ''
      ..lastMessageAt = messageDate
      ..audience = ListBuilder<String>(['123'])
      ..participants = ListBuilder<BasicUser>(
        [BasicUser((b) => b..id = '123')],
      ));

    when(interactor.getConversations()).thenAnswer((_) => Future.value([conversation]));
    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    expect(find.byType(Avatar), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays double avatar for two participants', (tester) async {
    var interactor = MockConversationListInteractor();
    setupTestLocator((locator) => locator.registerFactory<ConversationListInteractor>(() => interactor));

    var now = DateTime.now();
    var messageDate = DateTime(now.year, 12, 25, now.hour, 0, 0, 0, 0);

    var conversation = Conversation((b) => b
      ..contextName = ''
      ..lastMessage = ''
      ..lastMessageAt = messageDate
      ..audience = ListBuilder<String>(['123', '456']) // Only two users in audience, UI should filter out third user
      ..participants = ListBuilder<BasicUser>([
        BasicUser((b) => b..id = '123'),
        BasicUser((b) => b..id = '456'),
        BasicUser((b) => b..id = '789'),
      ]));

    when(interactor.getConversations()).thenAnswer((_) => Future.value([conversation]));
    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    expect(find.byType(Avatar), findsNWidgets(2));
  });

  testWidgetsWithAccessibilityChecks('Displays group icon for more than two participants', (tester) async {
    var interactor = MockConversationListInteractor();
    setupTestLocator((locator) => locator.registerFactory<ConversationListInteractor>(() => interactor));

    var now = DateTime.now();
    var messageDate = DateTime(now.year, 12, 25, now.hour, 0, 0, 0, 0);

    var conversation = Conversation((b) => b
      ..contextName = ''
      ..lastMessage = ''
      ..lastMessageAt = messageDate
      ..audience = ListBuilder<String>(['123', '456', '789'])
      ..participants = ListBuilder<BasicUser>([
        BasicUser((b) => b..id = '123'),
        BasicUser((b) => b..id = '456'),
        BasicUser((b) => b..id = '789'),
      ]));

    when(interactor.getConversations()).thenAnswer((_) => Future.value([conversation]));
    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    expect(find.byType(Icon), findsNWidgets(2)); // One for group icon, one for app bar nav button

    var icon = await tester.widget<Icon>(find.byType(Icon).first).icon;
    expect(icon, equals(CanvasIcons.group));
  });

  testWidgetsWithAccessibilityChecks('Tapping add button shows messageable course list', (tester) async {
    var interactor = MockConversationListInteractor();
    setupTestLocator((locator) => locator.registerFactory<ConversationListInteractor>(() => interactor));

    when(interactor.getConversations()).thenAnswer((_) => Future.error(''));

    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    var courseCompleter = Completer<List<Course>>();
    var enrollmentCompleter = Completer<List<Enrollment>>();

    when(interactor.getCoursesForCompose()).thenAnswer((_) => courseCompleter.future);
    when(interactor.getStudentEnrollments()).thenAnswer((_) => enrollmentCompleter.future);

    await tester.tap(find.byTooltip(l10n.newMessageTitle));
    await tester.pump();

    expect(find.byType(CircularProgressIndicator), findsOneWidget);

    var enrollments = [
      Enrollment((b) => b
        ..courseId = 'Course 1'
        ..observedUser = User((b) => b
          ..shortName = 'Bill'
          ..id = 'Bill').toBuilder()),
      Enrollment((b) => b
        ..courseId = 'Course 2'
        ..observedUser = User((b) => b
          ..shortName = 'Ted'
          ..id = 'Ted').toBuilder()),
    ];

    var courses = [
      Course((b) => b
        ..id = 'Course 1'
        ..name = 'Course 1'
        ..enrollments = ListBuilder([enrollments[0]])),
      Course((b) => b
        ..id = 'Course 2'
        ..name = 'Course 2'
        ..enrollments = ListBuilder([enrollments[1]])),
    ];

    var _combined = ConversationListInteractor().combineEnrollmentsAndCourses(courses, enrollments);
    when(interactor.combineEnrollmentsAndCourses(any, any)).thenAnswer((_) => _combined);

    courseCompleter.complete(courses);
    enrollmentCompleter.complete(enrollments);
    await tester.pumpAndSettle();

    expect(find.text('Course 1'), findsOneWidget);
    expect(find.text('Course 2'), findsOneWidget);

    expect(find.text('for Bill'), findsOneWidget);
    expect(find.text('for Ted'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows error in messegable course list', (tester) async {
    var interactor = MockConversationListInteractor();
    setupTestLocator((locator) => locator.registerFactory<ConversationListInteractor>(() => interactor));

    var completer = Completer<List<Course>>();
    when(interactor.getCoursesForCompose()).thenAnswer((_) => completer.future);
    when(interactor.getConversations()).thenAnswer((_) => Future.error(''));

    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.byTooltip(l10n.newMessageTitle));
    await tester.pump();

    completer.completeError('');
    await tester.pumpAndSettle();

    expect(find.text(l10n.errorFetchingCourses), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays unread message indicator', (tester) async {
    var interactor = MockConversationListInteractor();
    setupTestLocator((locator) => locator.registerFactory<ConversationListInteractor>(() => interactor));

    var now = DateTime.now();
    var messageDate = DateTime(now.year, 12, 25, now.hour, 0, 0, 0, 0);

    var read = Conversation((b) => b
      ..contextName = ''
      ..lastMessage = ''
      ..workflowState = ConversationWorkflowState.read
      ..lastMessageAt = messageDate);

    var unread = read.rebuild((b) => b..workflowState = ConversationWorkflowState.unread);

    when(interactor.getConversations()).thenAnswer((_) => Future.value([read, unread, unread]));
    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    expect(find.byKey(Key('unread-indicator')), findsNWidgets(2));
  });

  testWidgetsWithAccessibilityChecks('Refreshes on new message created', (tester) async {
    var interactor = MockConversationListInteractor();
    var nav = MockQuickNav();
    setupTestLocator((locator) {
      locator.registerFactory<QuickNav>(() => nav);
      locator.registerFactory<ConversationListInteractor>(() => interactor);
    });

    when(interactor.getConversations()).thenAnswer((_) => Future.error(''));
    when(nav.push(any, any)).thenAnswer((_) => Future.value(true));

    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    var courseCompleter = Completer<List<Course>>();
    var enrollmentCompleter = Completer<List<Enrollment>>();
    when(interactor.getCoursesForCompose()).thenAnswer((_) => courseCompleter.future);
    when(interactor.getStudentEnrollments()).thenAnswer((_) => enrollmentCompleter.future);

    await tester.tap(find.byTooltip(l10n.newMessageTitle));
    await tester.pump();

    expect(find.byType(CircularProgressIndicator), findsOneWidget);

    var enrollment = Enrollment((b) => b
      ..courseId = 'Course 1'
      ..observedUser = User((b) => b..shortName = 'Bill').toBuilder());

    var course = Course((b) => b
      ..name = 'Course 1'
      ..id = 'Course 1'
      ..enrollments = ListBuilder([enrollment]));

    var _combined = ConversationListInteractor().combineEnrollmentsAndCourses([course], [enrollment]);
    when(interactor.combineEnrollmentsAndCourses(any, any)).thenAnswer((_) => _combined);

    courseCompleter.complete([course]);
    enrollmentCompleter.complete([enrollment]);
    await tester.pumpAndSettle();

    var conversation = Conversation((b) => b
      ..contextName = ''
      ..lastMessage = 'This is a new message!'
      ..lastMessageAt = DateTime.now());
    when(interactor.getConversations(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([conversation]));

    await tester.tap(find.text('Course 1'));
    await tester.pumpAndSettle(Duration(seconds: 1));

    expect(find.text(conversation.lastMessage!), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Refreshes on conversation updated', (tester) async {
    var interactor = MockConversationListInteractor();
    var nav = MockQuickNav();
    setupTestLocator((locator) {
      locator.registerFactory<QuickNav>(() => nav);
      locator.registerFactory<ConversationListInteractor>(() => interactor);
    });

    final conversation = Conversation((b) => b
      ..contextName = ''
      ..subject = ''
      ..lastMessage = 'Message 1'
      ..workflowState = ConversationWorkflowState.unread
      ..lastMessageAt = DateTime.now());

    when(interactor.getConversations()).thenAnswer((_) => Future.value([conversation]));
    when(nav.push(any, any)).thenAnswer((_) async => true); // return 'true', meaning conversation was updated

    await tester.pumpWidget(TestApp(ConversationListScreen()));
    await tester.pumpAndSettle();

    expect(find.text(conversation.lastMessage!), findsOneWidget);
    expect(find.byKey(Key('unread-indicator')), findsOneWidget);

    final updatedConversation = conversation.rebuild((b) => b
      ..lastMessage = 'Message 2'
      ..workflowState = ConversationWorkflowState.read);
    when(interactor.getConversations(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([updatedConversation]));

    await tester.tap(find.text(conversation.lastMessage!));
    await tester.pumpAndSettle();

    expect(find.text(updatedConversation.lastMessage!), findsOneWidget);
    expect(find.byKey(Key('unread-indicator')), findsNothing);
  });
}