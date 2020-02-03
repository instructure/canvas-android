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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/screens/events/event_details_interactor.dart';
import 'package:flutter_parent/screens/events/event_details_screen.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';

void main() {
  // Setup
  final eventId = '123';
  final baseEvent = ScheduleItem((b) => b
    ..id = eventId
    ..type = ScheduleItem.typeCalendar);
  final interactor = _MockEventDetailsInteractor();

  final l10n = AppLocalizations();

  setupTestLocator((locator) {
    locator.registerLazySingleton<EventDetailsInteractor>(() => interactor);
  });

  setUp(() {
    reset(interactor);
  });

  // Start tests
  test('with id throws if id is null', () {
    expect(() => EventDetailsScreen.withId(eventId: null), throwsAssertionError);
  });

  test('with event throws if event is null', () {
    expect(() => EventDetailsScreen.withEvent(event: null), throwsAssertionError);
  });

  testWidgetsWithAccessibilityChecks('shows error', (tester) async {
    when(interactor.loadEvent(eventId, any)).thenAnswer((_) => Future<ScheduleItem>.error('Failed to load event'));

    await tester.pumpWidget(TestApp(EventDetailsScreen.withId(eventId: eventId), highContrast: true));
    await tester.pumpAndSettle(); // Let the future finish

    expect(find.byType(ErrorPandaWidget), findsOneWidget);
    expect(find.text(l10n.unexpectedError), findsOneWidget);

    await tester.tap(find.text(l10n.retry));
    await tester.pumpAndSettle();

    verify(interactor.loadEvent(eventId, any)).called(2); // Once for initial load, second for retry
  });

  group('shows loading', () {
    testWidgetsWithAccessibilityChecks('with id', (tester) async {
      await tester.pumpWidget(TestApp(EventDetailsScreen.withId(eventId: eventId)));
      await tester.pump(); // Let the widget build

      expect(find.byType(LoadingIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('with event', (tester) async {
      await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: baseEvent)));
      await tester.pump(); // Let the widget build

      expect(find.byType(LoadingIndicator), findsOneWidget);
    });
  });

  group('can refresh', () {
    testWidgetsWithAccessibilityChecks('with id', (tester) async {
      when(interactor.loadEvent(eventId, any)).thenAnswer((_) async => baseEvent);

      await tester.pumpWidget(TestApp(EventDetailsScreen.withId(eventId: eventId)));
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

      await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: baseEvent)));
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

      await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.byIcon(Icons.timer), findsOneWidget);
      expect(find.text(l10n.eventAllDayLabel), findsOneWidget);
      expect(find.text('Saturday Jan 1, 2000'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows all day events for endAt', (tester) async {
      final date = DateTime(2000);
      final event = baseEvent.rebuild((b) => b
        ..isAllDay = true
        ..endAt = date);

      await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.byIcon(Icons.timer), findsOneWidget);
      expect(find.text(l10n.eventAllDayLabel), findsOneWidget);
      expect(find.text('Saturday Jan 1, 2000'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows events with a duration', (tester) async {
      final startDate = DateTime(2000);
      final endDate = startDate.add(Duration(hours: 2));
      final event = baseEvent.rebuild((b) => b
        ..startAt = startDate
        ..endAt = endDate);

      await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.byIcon(Icons.timer), findsOneWidget);
      expect(find.text('Saturday Jan 1, 2000'), findsOneWidget);
      expect(find.text(l10n.eventTime('12:00 AM', '2:00 AM')), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows events with only a start time', (tester) async {
      final date = DateTime(2000);
      final event = baseEvent.rebuild((b) => b..startAt = date);

      await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.byIcon(Icons.timer), findsOneWidget);
      expect(find.text('Saturday Jan 1, 2000'), findsOneWidget);
      expect(find.text('12:00 AM'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows events with only an end time', (tester) async {
      final date = DateTime(2000);
      final event = baseEvent.rebuild((b) => b..endAt = date);

      await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.byIcon(Icons.timer), findsOneWidget);
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

      await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.byIcon(Icons.location_on), findsOneWidget);
      expect(find.text(name), findsOneWidget);
      expect(find.text(address), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows location name without address', (tester) async {
      final name = 'loc name';
      final event = baseEvent.rebuild((b) => b..locationName = name);

      await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.byIcon(Icons.location_on), findsOneWidget);
      expect(find.text(name), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows address without location name', (tester) async {
      final address = 'loc address';
      final event = baseEvent.rebuild((b) => b..locationAddress = address);

      await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.byIcon(Icons.location_on), findsOneWidget);
      expect(find.text(address), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows no location message', (tester) async {
      final event = baseEvent;

      await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: event)));
      await tester.pump(); // Let the widget build
      await tester.pump(); // Let the future finish

      expect(find.byIcon(Icons.location_on), findsOneWidget);
      expect(find.text(l10n.eventNoLocation), findsOneWidget);
    });
  });

  testWidgetsWithAccessibilityChecks('shows event title as app bar title', (tester) async {
    final title = 'Event Test Title';
    final event = baseEvent.rebuild((b) => b..title = title);

    await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: event)));
    await tester.pump(); // Let the widget build
    await tester.pump(); // Let the future finish

    expect(find.descendant(of: find.byType(AppBar), matching: find.text(title)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows default title as app bar title when event has no title', (tester) async {
    final event = baseEvent;

    await tester.pumpWidget(TestApp(EventDetailsScreen.withEvent(event: event)));
    await tester.pump(); // Let the widget build
    await tester.pump(); // Let the future finish

    expect(
        find.descendant(of: find.byType(AppBar), matching: find.text(l10n.eventDetailsDefaultTitle)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows webview', (tester) async {
    final description = 'test description';
    final event = baseEvent.rebuild((b) => b..description = description);

    await tester.pumpWidget(TestApp(
      EventDetailsScreen.withEvent(event: event),
      platformConfig: PlatformConfig(initWebview: true),
    ));
    await tester.pump(); // Let the widget build
    await tester.pump(); // Let the future finish

    expect(find.byType(WebView), findsOneWidget);
  });
}

class _MockEventDetailsInteractor extends Mock implements EventDetailsInteractor {}
