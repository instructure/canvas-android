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

import 'package:built_collection/built_collection.dart';
import 'package:flutter_parent/models/account_notification.dart';
import 'package:flutter_parent/models/announcement.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/remote_file.dart';
import 'package:flutter_parent/network/api/announcement_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_interactor.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_screen.dart';
import 'package:flutter_parent/screens/announcements/announcement_view_state.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  //region data config
  final remoteFile = RemoteFile((b) => b
    ..id = '123'
    ..url = 'hodor.com'
    ..filename = 'hodor.jpg'
    ..contentType = 'jpg'
    ..previewUrl = 'hodor.com/preview'
    ..thumbnailUrl = 'hodor.com/thumbnail'
    ..displayName = 'hodor');

  final attachment = remoteFile.toAttachment();

  final course = Course((b) => b
    ..id = '123'
    ..enrollments = ListBuilder<Enrollment>()
    ..name = 'flowers for hodornon'
    ..needsGradingCount = 0
    ..hideFinalGrades = false
    ..isPublic = false
    ..applyAssignmentGroupWeights = false
    ..isFavorite = false
    ..accessRestrictedByDate = false
    ..hasWeightedGradingPeriods = false
    ..hasGradingPeriods = false
    ..restrictEnrollmentsToCourseDates = false);

  final accountNotification = AccountNotification((b) => b
    ..id = '123'
    ..startAt = DateTime.now().toIso8601String()
    ..message = 'hodor'
    ..subject = 'hodor subject');

  _getAnnouncement({bool hasAttachments = true}) {
    return Announcement((b) => b
      ..id = '123'
      ..postedAt = DateTime.now()
      ..message = 'hodor'
      ..title = 'hodor subject'
      ..htmlUrl = ''
      ..attachments = hasAttachments ? ListBuilder<RemoteFile>([remoteFile]) : null);
  }

  //endregion

  final announcementApi = MockAnnouncementApi();
  final courseApi = MockCourseApi();

  setupTestLocator((locator) {
    locator.registerFactory<AnnouncementApi>(() => announcementApi);
    locator.registerFactory<CourseApi>(() => courseApi);
  });

  setUp(() {
    reset(announcementApi);
    reset(courseApi);
  });

  test('get course announcement returns a proper view state', () async {
    final announcement = _getAnnouncement();
    final expectedViewState =
        AnnouncementViewState(course.name, announcement.title, announcement.message, announcement.postedAt, attachment);

    when(announcementApi.getCourseAnnouncement(course.id, announcement.id, any))
        .thenAnswer((_) => Future.value(announcement));
    when(courseApi.getCourse(course.id)).thenAnswer((_) => Future.value(course));

    final actualViewState = await AnnouncementDetailsInteractor()
        .getAnnouncement(announcement.id, AnnouncementType.COURSE, course.id, '', true);

    verify(announcementApi.getCourseAnnouncement(course.id, announcement.id, true)).called(1);
    verify(courseApi.getCourse(course.id)).called(1);

    expect(actualViewState?.toolbarTitle, expectedViewState.toolbarTitle);
    expect(actualViewState?.announcementMessage, expectedViewState.announcementMessage);
    expect(actualViewState?.announcementTitle, expectedViewState.announcementTitle);
    expect(actualViewState?.postedAt, expectedViewState.postedAt);
    expect(actualViewState?.attachment, attachment);
  });

  test('get course announcement returns a proper view state with no attachments', () async {
    final announcement = _getAnnouncement(hasAttachments: false);
    final expectedViewState =
        AnnouncementViewState(course.name, announcement.title, announcement.message, announcement.postedAt, null);

    when(announcementApi.getCourseAnnouncement(course.id, announcement.id, any))
        .thenAnswer((_) => Future.value(announcement));
    when(courseApi.getCourse(course.id)).thenAnswer((_) => Future.value(course));

    final actualViewState = await AnnouncementDetailsInteractor()
        .getAnnouncement(announcement.id, AnnouncementType.COURSE, course.id, '', true);

    verify(announcementApi.getCourseAnnouncement(course.id, announcement.id, true)).called(1);
    verify(courseApi.getCourse(course.id)).called(1);

    expect(actualViewState?.toolbarTitle, expectedViewState.toolbarTitle);
    expect(actualViewState?.announcementMessage, expectedViewState.announcementMessage);
    expect(actualViewState?.announcementTitle, expectedViewState.announcementTitle);
    expect(actualViewState?.postedAt, expectedViewState.postedAt);
    expect(actualViewState?.attachment, expectedViewState.attachment);
  });

  test('get institution announcement returns a proper view state', () async {
    final toolbarTitle = 'Institution Announcement';

    final expectedViewState = AnnouncementViewState(toolbarTitle, accountNotification.subject,
        accountNotification.message, DateTime.parse(accountNotification.startAt), null);

    when(announcementApi.getAccountNotification(accountNotification.id, any))
        .thenAnswer((_) => Future.value(accountNotification));

    final actualViewState = await AnnouncementDetailsInteractor()
        .getAnnouncement(accountNotification.id, AnnouncementType.INSTITUTION, course.id, toolbarTitle, true);

    verify(announcementApi.getAccountNotification(accountNotification.id, true)).called(1);

    expect(actualViewState?.toolbarTitle, expectedViewState.toolbarTitle);
    expect(actualViewState?.announcementMessage, expectedViewState.announcementMessage);
    expect(actualViewState?.announcementTitle, expectedViewState.announcementTitle);
    expect(actualViewState?.postedAt, expectedViewState.postedAt);
  });
}