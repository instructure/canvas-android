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
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';

void main() {
  _setupLocator({AnnouncementDetailsInteractor interactor}) {
    final _locator = GetIt.instance;
    _locator.reset();

    _locator.registerFactory<AnnouncementDetailsInteractor>(() => interactor ?? _MockAnnouncementDetailsInteractor());
    _locator.registerFactory<WebContentInteractor>(() => WebContentInteractor());
  }

  group('Loading', () {
    testWidgetsWithAccessibilityChecks('Shows while waiting for future', (tester) async {
      final interactor = _MockAnnouncementDetailsInteractor();
      when(interactor.getAnnouncement(any, any, any, any)).thenAnswer((_) => Future.value());
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget('', AnnouncementType.COURSE, ''));
      await tester.pump();

      expect(find.byType(LoadingIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Does not show once loaded', (tester) async {
      final interactor = _MockAnnouncementDetailsInteractor();
      when(interactor.getAnnouncement(any, any, any, any)).thenAnswer((_) => Future.value());
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget('', AnnouncementType.COURSE, ''));
      await tester.pump();
      await tester.pump();

      expect(find.byType(LoadingIndicator), findsNothing);
    });
  });

  testWidgetsWithAccessibilityChecks('Shows error', (tester) async {
    final interactor = _MockAnnouncementDetailsInteractor();
    when(interactor.getAnnouncement(any, any, any, any)).thenAnswer((_) => Future.error('error'));
    _setupLocator(interactor: interactor);

    await tester.pumpWidget(_testableWidget('', AnnouncementType.COURSE, ''));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().errorLoadingAnnouncement), findsOneWidget);
  });

  group('With data', () {
    testWidgetsWithAccessibilityChecks('Shows course announcement', (tester) async {
      final interactor = _MockAnnouncementDetailsInteractor();
      final announcementId = '123';
      final courseId = '123';
      final announcementMessage = 'hodor';
      final announcementSubject = 'hodor subject';
      final postedAt = DateTime.now();
      final courseName = 'flowers for hodornon';

      final response = AnnouncementViewState(courseName, announcementSubject, announcementMessage, postedAt, null);
      when(interactor.getAnnouncement(
              announcementId, AnnouncementType.COURSE, courseId, AppLocalizations().institutionAnnouncementTitle))
          .thenAnswer((_) => Future.value(response));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget(announcementId, AnnouncementType.COURSE, courseId));
      await tester.pumpAndSettle();

      expect(find.text(announcementSubject), findsOneWidget);
      expect(find.text(courseName), findsOneWidget);
      expect(find.text(postedAt.l10nFormat(AppLocalizations().dateAtTime)), findsOneWidget);
      expect(find.byType(WebView), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Shows course announcement with attachment', (tester) async {
      final interactor = _MockAnnouncementDetailsInteractor();
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
              announcementId, AnnouncementType.COURSE, courseId, AppLocalizations().institutionAnnouncementTitle))
          .thenAnswer((_) => Future.value(response));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget(announcementId, AnnouncementType.COURSE, courseId));
      await tester.pumpAndSettle();

      expect(find.text(announcementSubject), findsOneWidget);
      expect(find.text(courseName), findsOneWidget);
      expect(find.text(postedAt.l10nFormat(AppLocalizations().dateAtTime)), findsOneWidget);
      expect(find.byType(WebView), findsOneWidget);
      var attachmentWidget = find.byType(AttachmentIndicatorWidget);
      expect(attachmentWidget, findsOneWidget);
      await tester.tap(attachmentWidget);
      verify(interactor.viewAttachment(any, attachment)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Shows institution announcement', (tester) async {
      final interactor = _MockAnnouncementDetailsInteractor();
      final announcementId = '123';
      final courseId = '123';
      final announcementMessage = 'hodor';
      final announcementSubject = 'hodor subject';
      final postedAt = DateTime.now();
      final toolbarTitle = AppLocalizations().institutionAnnouncementTitle;

      final response = AnnouncementViewState(toolbarTitle, announcementSubject, announcementMessage, postedAt, null);
      when(interactor.getAnnouncement(announcementId, AnnouncementType.INSTITUTION, courseId, toolbarTitle))
          .thenAnswer((_) => Future.value(response));
      _setupLocator(interactor: interactor);

      await tester.pumpWidget(_testableWidget(announcementId, AnnouncementType.INSTITUTION, courseId));
      await tester.pumpAndSettle();

      expect(find.text(announcementSubject), findsOneWidget);
      expect(find.text(toolbarTitle), findsOneWidget);
      expect(find.text(postedAt.l10nFormat(AppLocalizations().dateAtTime)), findsOneWidget);
      expect(find.byType(WebView), findsOneWidget);
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
    highContrast: true,
    platformConfig: PlatformConfig(initWebview: true),
  );
}

class _MockAnnouncementDetailsInteractor extends Mock implements AnnouncementDetailsInteractor {}
