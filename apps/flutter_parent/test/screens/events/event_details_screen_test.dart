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
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/events/event_details_interactor.dart';
import 'package:flutter_parent/screens/events/event_details_screen.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_screen.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/html_description_tile.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';
import '../../utils/test_utils.dart';

void main() {
  // Setup
  final studentName = 'Panda';
  final studentId = '123';
  final student = User((b) => b
    ..id = studentId
    ..name = studentName);

  final eventId = '123';
  final courseId = 'course_123';
  final baseEvent = ScheduleItem((b) => b
    ..id = eventId
    ..type = ScheduleItem.apiTypeCalendar);
  final reminder = Reminder((b) => b
    ..id = 123
    ..userId = 'user-123'
    ..type = 'type'
    ..itemId = eventId
    ..courseId = courseId
    ..date = DateTime(2100)
    ..userDomain = 'domain');

  final interactor = MockEventDetailsInteractor();
  final mockNav = MockQuickNav();

  final l10n = AppLocalizations();

  setupTestLocator((locator) {
    locator.registerLazySingleton<EventDetailsInteractor>(() => interactor);
    locator.registerLazySingleton<QuickNav>(() => mockNav);
    locator.registerFactory<WebContentInteractor>(() => WebContentInteractor());
  });

  tearDown(() {
    ApiPrefs.clean();
  });

  setUp(() async {
    reset(interactor);
    await setupPlatformChannels(
      config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    );
  });

  // Start tests
  test('with id throws if id is null', () {
    expect(() => EventDetailsScreen.withId(eventId: null), throwsAssertionError);
  });

  test('with event throws if event is null', () {
    expect(() => EventDetailsScreen.withEvent(event: null), throwsAssertionError);
  });

  testWidgetsWithAccessibilityChecks('shows error', (tester) async {
    when(interactor.loadEvent(eventId, any)).thenAnswer((_) => Future<ScheduleItem?>.error('Failed to load event'));

    await tester.pumpWidget(_testableWidget(EventDetailsScreen.withId(eventId: eventId)));
    await tester.pumpAndSettle(); // Let the future finish

    expect(find.byType(ErrorPandaWidget), findsOneWidget);
    expect(find.text(l10n.unexpectedError), findsOneWidget);

    await tester.tap(find.text(l10n.retry));
    await tester.pumpAndSettle();

    verify(interactor.loadEvent(eventId, any)).called(2); // Once for initial load, second for retry
  });

  group('shows loading', () {
    testWidgetsWithAccessibilityChecks('with id', (tester) async {
      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withId(eventId: eventId)));
      await tester.pump(); // Let the widget build

      expect(find.byType(LoadingIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('with event', (tester) async {
      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: baseEvent)));
      await tester.pump(); // Let the widget build

      expect(find.byType(LoadingIndicator), findsOneWidget);
    });
  });

  group('can refresh', () {
    testWidgetsWithAccessibilityChecks('with id', (tester) async {
      when(interactor.loadEvent(eventId, any)).thenAnswer((_) async => baseEvent);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withId(eventId: eventId)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      final refresh = find.byType(RefreshIndicator);
      expect(refresh, findsOneWidget);
      await tester.drag(refresh, const Offset(0, 200));
      await tester.pumpAndSettle();

      verify(interactor.loadEvent(eventId, any)).called(2); // Once for initial load, second for refresh
    });

    testWidgetsWithAccessibilityChecks('with event', (tester) async {
      when(interactor.loadEvent(eventId, any)).thenAnswer((_) async => baseEvent);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withId(eventId: eventId)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      final refresh = find.byType(RefreshIndicator);
      expect(refresh, findsOneWidget);
      await tester.drag(refresh, const Offset(0, 200));
      await tester.pumpAndSettle();

      verify(interactor.loadEvent(eventId, true)).called(1); // Only once for refresh, since initial load isn't required
    });
  });

  group('date', () {
    testWidgetsWithAccessibilityChecks('shows all day events', (tester) async {
      final date = DateTime(2000);
      final event = baseEvent.rebuild((b) => b
        ..isAllDay = true
        ..startAt = date);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.text(l10n.eventDateLabel), findsOneWidget);
      expect(find.text('Saturday Jan 1, 2000'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows all day events for endAt', (tester) async {
      final date = DateTime(2000);
      final event = baseEvent.rebuild((b) => b
        ..isAllDay = true
        ..endAt = date);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.text(l10n.eventDateLabel), findsOneWidget);
      expect(find.text('Saturday Jan 1, 2000'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows events with a duration', (tester) async {
      final startDate = DateTime(2000);
      final endDate = startDate.add(Duration(hours: 2));
      final event = baseEvent.rebuild((b) => b
        ..startAt = startDate
        ..endAt = endDate);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.text(l10n.eventDateLabel), findsOneWidget);
      expect(find.text('Saturday Jan 1, 2000'), findsOneWidget);
      expect(find.text(l10n.eventTime('12:00 AM', '2:00 AM')), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows events with only a start time', (tester) async {
      final date = DateTime(2000);
      final event = baseEvent.rebuild((b) => b..startAt = date);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.text(l10n.eventDateLabel), findsOneWidget);
      expect(find.text('Saturday Jan 1, 2000'), findsOneWidget);
      expect(find.text('12:00 AM'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows events with only an end time', (tester) async {
      final date = DateTime(2000);
      final event = baseEvent.rebuild((b) => b..endAt = date);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.text(l10n.eventDateLabel), findsOneWidget);
      expect(find.text('Saturday Jan 1, 2000'), findsOneWidget);
      expect(find.text('12:00 AM'), findsOneWidget);
    });
  });

  group('location', () {
    testWidgetsWithAccessibilityChecks('shows name and address', (tester) async {
      final name = 'loc name';
      final address = 'loc address';
      final event = baseEvent.rebuild((b) => b
        ..locationName = name
        ..locationAddress = address);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.text(l10n.eventLocationLabel), findsOneWidget);
      expect(find.text(name), findsOneWidget);
      expect(find.text(address), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows location name without address', (tester) async {
      final name = 'loc name';
      final event = baseEvent.rebuild((b) => b..locationName = name);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.text(l10n.eventLocationLabel), findsOneWidget);
      expect(find.text(name), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows address without location name', (tester) async {
      final address = 'loc address';
      final event = baseEvent.rebuild((b) => b..locationAddress = address);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.text(l10n.eventLocationLabel), findsOneWidget);
      expect(find.text(address), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows no location message', (tester) async {
      final event = baseEvent;

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.text(l10n.eventLocationLabel), findsOneWidget);
      expect(find.text(l10n.eventNoLocation), findsOneWidget);
    });
  });

  group('reminder', () {
    testWidgetsWithAccessibilityChecks('shows correct state when reminder is set', (tester) async {
      final title = 'Event Test Title';
      final event = baseEvent.rebuild((b) => b..title = title);

      when(interactor.loadReminder(any)).thenAnswer((_) async => reminder);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the event future finish
      await tester.pump(); // Let the reminder future finish

      expect(find.text(l10n.eventRemindMeDescription), findsNothing);
      expect(find.text(l10n.eventRemindMeSet), findsOneWidget);
      expect((tester.widget(find.byType(Switch)) as Switch).value, true);
      expect(find.text(reminder.date.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows correct state when no reminder is set', (tester) async {
      final title = 'Event Test Title';
      final event = baseEvent.rebuild((b) => b..title = title);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the event future finish
      await tester.pump(); // Let the reminder future finish

      expect(find.text(l10n.eventRemindMeDescription), findsOneWidget);
      expect((tester.widget(find.byType(Switch)) as Switch).value, false);
    });

    testWidgetsWithAccessibilityChecks('creates reminder with all day date', (tester) async {
      final title = 'Event Test Title';
      final event = baseEvent.rebuild((b) => b
        ..title = title
        ..isAllDay = true
        ..startAt = DateTime(2000)
        ..allDayDate = DateTime(2000));

      when(interactor.loadReminder(any)).thenAnswer((_) async => null);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event, courseId: courseId)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the event future finish
      await tester.pump(); // Let the reminder future finish

      when(interactor.loadReminder(any)).thenAnswer((_) async => reminder);

      // Tap on switch to open date picker
      await ensureVisibleByScrolling(find.byType(Switch), tester, scrollFrom: ScreenVerticalLocation.MID_BOTTOM);
      await tester.pumpAndSettle();
      await tester.tap(find.byType(Switch));
      await tester.pumpAndSettle();

      // Tap on 'OK' button in date picker to open time picker
      await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
      await tester.pumpAndSettle();

      // Tap on 'OK' button in time picker
      await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
      await tester.pumpAndSettle();

      verify(interactor.createReminder(any, any, eventId, courseId, event.title, 'Saturday Jan 1, 2000'));

      expect(find.text(AppLocalizations().eventRemindMeSet), findsOneWidget);
      expect((tester.widget(find.byType(Switch)) as Switch).value, true);
    });

    testWidgetsWithAccessibilityChecks('creates reminder with date range', (tester) async {
      final title = 'Event Test Title';
      final startDate = DateTime(2000);
      final endDate = startDate.add(Duration(hours: 2));
      final event = baseEvent.rebuild((b) => b
        ..title = title
        ..startAt = startDate
        ..endAt = endDate);

      when(interactor.loadReminder(any)).thenAnswer((_) async => null);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event, courseId: courseId)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the event future finish
      await tester.pump(); // Let the reminder future finish

      when(interactor.loadReminder(any)).thenAnswer((_) async => reminder);

      // Tap on switch to open date picker
      await ensureVisibleByScrolling(find.byType(Switch), tester, scrollFrom: ScreenVerticalLocation.MID_BOTTOM);
      await tester.pumpAndSettle();
      await tester.tap(find.byType(Switch));
      await tester.pumpAndSettle();

      // Tap on 'OK' button in date picker to open time picker
      await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
      await tester.pumpAndSettle();

      // Tap on 'OK' button in time picker
      await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
      await tester.pumpAndSettle();

      verify(interactor.createReminder(
        any,
        any,
        eventId,
        courseId,
        event.title,
        'Saturday Jan 1, 2000\n12:00 AM - 2:00 AM',
      ));

      expect(find.text(AppLocalizations().eventRemindMeSet), findsOneWidget);
      expect((tester.widget(find.byType(Switch)) as Switch).value, true);
    });

    testWidgetsWithAccessibilityChecks('deletes reminder', (tester) async {
      final title = 'Event Test Title';
      final event = baseEvent.rebuild((b) => b..title = title);

      when(interactor.loadReminder(any)).thenAnswer((_) async => reminder);

      await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the event future finish
      await tester.pump(); // Let the reminder future finish

      expect(find.text(l10n.eventRemindMeDescription), findsNothing);
      expect(find.text(l10n.eventRemindMeSet), findsOneWidget);
      expect((tester.widget(find.byType(Switch)) as Switch).value, true);

      when(interactor.loadReminder(any)).thenAnswer((_) async => null);

      await ensureVisibleByScrolling(find.byType(Switch), tester, scrollFrom: ScreenVerticalLocation.MID_BOTTOM);
      await tester.pumpAndSettle();
      await tester.tap(find.byType(Switch));
      await tester.pumpAndSettle();

      expect(find.text(l10n.eventRemindMeSet), findsNothing);
      expect(find.text(l10n.eventRemindMeDescription), findsOneWidget);
      expect((tester.widget(find.byType(Switch)) as Switch).value, false);
    });
  });

  testWidgetsWithAccessibilityChecks('shows event title', (tester) async {
    final title = 'Event Test Title';
    final event = baseEvent.rebuild((b) => b..title = title);

    await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event)));
    await tester.pump(); // Let the widget build
    await tester.pump(); // Let the future finish

    expect(find.text(l10n.eventDetailsTitle), findsOneWidget);
    expect(find.text(title), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows description button', (tester) async {
    final description = 'test description';
    final event = baseEvent.rebuild((b) => b..description = description);

    await tester.pumpWidget(_testableWidget(
      EventDetailsScreen.withEvent(event: event),
      // Set mockApiPrefs to null so our initial mockApiPrefs are not overridden
      config: PlatformConfig(initWebview: true, mockApiPrefs: null),
    ));
    await tester.pump(); // Let the widget build
    await tester.pump(); // Let the future finish
    await tester.pump(); // Let the webview future finish

    expect(find.text(l10n.descriptionTitle), findsOneWidget);
    await ensureVisibleByScrolling(find.byType(HtmlDescriptionTile), tester,
        scrollFrom: ScreenVerticalLocation.MID_BOTTOM);
    await tester.pumpAndSettle();
    expect(find.byType(HtmlDescriptionTile), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows create conversation screen with correct subject', (tester) async {
    final title = 'Event Test Title';
    final event = baseEvent.rebuild((b) => b..title = title);

    await tester.pumpWidget(_testableWidget(EventDetailsScreen.withEvent(event: event, courseId: 'whatever')));
    await tester.pumpAndSettle(); // Let the widget build

    final fab = find.byType(FloatingActionButton);
    expect(fab, findsOneWidget);
    await tester.tap(fab);

    final widget = verify(mockNav.push(any, captureAny)).captured[0] as CreateConversationScreen;
    expect(widget.subjectTemplate, l10n.eventSubjectMessage(studentName, title));
  });
}

// Default to null mock prefs, since set up is already called with a current student
Widget _testableWidget(
  EventDetailsScreen eventDetailsScreen, {
  PlatformConfig config = const PlatformConfig(mockApiPrefs: null),
}) {
  return TestApp(
    eventDetailsScreen,
    platformConfig: config,
  );
}