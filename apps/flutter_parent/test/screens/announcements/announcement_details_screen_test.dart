/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:built_value/json_object.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_interactor.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_screen.dart';
import 'package:flutter_parent/screens/announcements/announcement_view_state.dart';
import 'package:flutter_parent/utils/common_widgets/attachment_indicator_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/html_description_tile.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final interactor = MockAnnouncementDetailsInteractor();

  setupTestLocator((locator) {
    locator.registerFactory<AnnouncementDetailsInteractor>(() => interactor);
    locator.registerFactory<WebContentInteractor>(() => WebContentInteractor());
  });

  setUp(() {
    reset(interactor);
  });

  group('Loading', () {
    testWidgetsWithAccessibilityChecks('Shows while waiting for future', (tester) async {
      when(interactor.getAnnouncement(any, any, any, any, any)).thenAnswer((_) => Future.value(null));

      await tester.pumpWidget(_testableWidget('', AnnouncementType.COURSE, ''));
      await tester.pump();

      expect(find.byType(LoadingIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Does not show once loaded', (tester) async {
      when(interactor.getAnnouncement(any, any, any, any, any)).thenAnswer((_) => Future.value(null));

      await tester.pumpWidget(_testableWidget('', AnnouncementType.COURSE, ''));
      await tester.pump();
      await tester.pump();

      expect(find.byType(LoadingIndicator), findsNothing);
    });
  });

  testWidgetsWithAccessibilityChecks('Shows error', (tester) async {
    when(interactor.getAnnouncement(any, any, any, any, any)).thenAnswer((_) => Future.error('error'));

    await tester.pumpWidget(_testableWidget('', AnnouncementType.COURSE, ''));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().errorLoadingAnnouncement), findsOneWidget);

    when(interactor.getAnnouncement(any, any, any, any, any))
        .thenAnswer((_) async => AnnouncementViewState('', '', '', null, null));
    await tester.tap(find.text(AppLocalizations().retry));

    verify(interactor.getAnnouncement(any, any, any, any, false)).called(1);
    verify(interactor.getAnnouncement(any, any, any, any, true)).called(1);
  });

  group('With data', () {
    testWidgetsWithAccessibilityChecks('Can pull to refresh', (tester) async {
      final announcementId = '123';
      final courseId = '123';
      final announcementMessage = 'hodor';
      final announcementSubject = 'hodor subject';
      final postedAt = DateTime.now();
      final courseName = 'flowers for hodornon';

      final response = AnnouncementViewState(courseName, announcementSubject, announcementMessage, postedAt, null);
      when(interactor.getAnnouncement(
              announcementId, AnnouncementType.COURSE, courseId, AppLocalizations().globalAnnouncementTitle, any))
          .thenAnswer((_) => Future.value(response));

      await tester.pumpWidget(_testableWidget(announcementId, AnnouncementType.COURSE, courseId));
      await tester.pumpAndSettle();

      // Pull to refresh
      final matchedWidget = find.byType(RefreshIndicator);
      await tester.drag(matchedWidget, const Offset(0, 200));
      await tester.pumpAndSettle();

      // Once for initial (non forced) load, once for forced refresh
      verify(interactor.getAnnouncement(any, any, any, any, false)).called(1);
      verify(interactor.getAnnouncement(any, any, any, any, true)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Shows course announcement', (tester) async {
      final announcementId = '123';
      final courseId = '123';
      final announcementMessage = 'hodor';
      final announcementSubject = 'hodor subject';
      final postedAt = DateTime.now();
      final courseName = 'flowers for hodornon';

      final response = AnnouncementViewState(courseName, announcementSubject, announcementMessage, postedAt, null);
      when(interactor.getAnnouncement(
              announcementId, AnnouncementType.COURSE, courseId, AppLocalizations().globalAnnouncementTitle, any))
          .thenAnswer((_) => Future.value(response));

      await tester.pumpWidget(_testableWidget(announcementId, AnnouncementType.COURSE, courseId));
      await tester.pumpAndSettle();

      expect(find.text(announcementSubject), findsOneWidget);
      expect(find.text(courseName), findsOneWidget);
      expect(find.text(postedAt.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
      expect(find.byType(HtmlDescriptionTile), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Shows course announcement with attachment', (tester) async {
      final announcementId = '123';
      final courseId = '123';
      final announcementMessage = 'hodor';
      final announcementSubject = 'hodor subject';
      final postedAt = DateTime.now();
      final courseName = 'flowers for hodornon';
      final attachment = Attachment((b) => b
        ..jsonId = JsonObject('1')
        ..displayName = 'Attachment 1');

      final response =
          AnnouncementViewState(courseName, announcementSubject, announcementMessage, postedAt, attachment);
      when(interactor.getAnnouncement(
              announcementId, AnnouncementType.COURSE, courseId, AppLocalizations().globalAnnouncementTitle, any))
          .thenAnswer((_) => Future.value(response));

      await tester.pumpWidget(_testableWidget(announcementId, AnnouncementType.COURSE, courseId));
      await tester.pumpAndSettle();

      expect(find.text(announcementSubject), findsOneWidget);
      expect(find.text(courseName), findsOneWidget);
      expect(find.text(postedAt.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
      expect(find.byType(HtmlDescriptionTile), findsOneWidget);
      var attachmentWidget = find.byType(AttachmentIndicatorWidget);
      expect(attachmentWidget, findsOneWidget);
      await tester.tap(attachmentWidget);
      verify(interactor.viewAttachment(any, attachment)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Shows institution announcement', (tester) async {
      final announcementId = '123';
      final courseId = '123';
      final announcementMessage = 'hodor';
      final announcementSubject = 'hodor subject';
      final postedAt = DateTime.now();
      final toolbarTitle = AppLocalizations().globalAnnouncementTitle;

      final response = AnnouncementViewState(toolbarTitle, announcementSubject, announcementMessage, postedAt, null);
      when(interactor.getAnnouncement(announcementId, AnnouncementType.INSTITUTION, courseId, toolbarTitle, any))
          .thenAnswer((_) => Future.value(response));

      await tester.pumpWidget(_testableWidget(announcementId, AnnouncementType.INSTITUTION, courseId));
      await tester.pumpAndSettle();

      expect(find.text(announcementSubject), findsOneWidget);
      expect(find.text(toolbarTitle), findsOneWidget);
      expect(find.text(postedAt.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
      expect(find.byType(HtmlDescriptionTile), findsOneWidget);
    });
  });
}

Widget _testableWidget(String announcementId, AnnouncementType type, String courseId) {
  return TestApp(
    Builder(
      builder: (BuildContext context) {
        return AnnouncementDetailScreen(announcementId, type, courseId, context);
      },
    ),
  );
}
