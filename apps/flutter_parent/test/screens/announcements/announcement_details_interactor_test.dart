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
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

void main() {
  void _setupLocator({AnnouncementApi announcementApi, CourseApi courseApi}) {
    final _locator = GetIt.instance;
    _locator.reset();

    _locator.registerFactory<AnnouncementApi>(() => announcementApi ?? _MockAnnouncementApi());
    _locator.registerFactory<CourseApi>(() => courseApi ?? _MockCourseApi());
  }

  test('get course announcement returns a proper view state', () async {
    final announcementId = '123';
    final courseId = '123';
    final announcementMessage = 'hodor';
    final announcementSubject = 'hodor subject';
    final announcementType = AnnouncementType.COURSE;
    final postedAt = DateTime.now();
    final courseName = 'flowers for hodornon';
    final remoteFile = RemoteFile((b) => b
      ..id = '123'
      ..url = 'hodor.com'
      ..filename = 'hodor.jpg'
      ..contentType = 'jpg'
      ..previewUrl = 'hodor.com/preview'
      ..thumbnailUrl = 'hodor.com/thumbnail'
      ..displayName = 'hodor');

    final attachment = remoteFile.toAttachment();

    final announcementData = Announcement((b) => b
      ..id = announcementId
      ..postedAt = postedAt
      ..message = announcementMessage
      ..title = announcementSubject
      ..htmlUrl = ''
      ..attachments = ListBuilder<RemoteFile>([remoteFile]));

    final courseData = Course((b) => b
      ..id = courseId
      ..enrollments = ListBuilder<Enrollment>()
      ..name = courseName
      ..needsGradingCount = 0
      ..hideFinalGrades = false
      ..isPublic = false
      ..applyAssignmentGroupWeights = false
      ..isFavorite = false
      ..accessRestrictedByDate = false
      ..hasWeightedGradingPeriods = false
      ..hasGradingPeriods = false
      ..restrictEnrollmentsToCourseDates = false);

    final expectedViewState =
        AnnouncementViewState(courseName, announcementSubject, announcementMessage, postedAt, attachment);

    final announcementApi = _MockAnnouncementApi();
    final courseApi = _MockCourseApi();
    when(announcementApi.getCourseAnnouncement(courseId, announcementId))
        .thenAnswer((_) => Future.value(announcementData));
    when(courseApi.getCourse(courseId)).thenAnswer((_) => Future.value(courseData));

    _setupLocator(announcementApi: announcementApi, courseApi: courseApi);

    final actualViewState =
        await AnnouncementDetailsInteractor().getAnnouncement(announcementId, announcementType, courseId, '');

    verify(announcementApi.getCourseAnnouncement(courseId, announcementId)).called(1);
    verify(courseApi.getCourse(courseId)).called(1);

    expect(actualViewState.toolbarTitle, expectedViewState.toolbarTitle);
    expect(actualViewState.announcementMessage, expectedViewState.announcementMessage);
    expect(actualViewState.announcementTitle, expectedViewState.announcementTitle);
    expect(actualViewState.postedAt, expectedViewState.postedAt);
    expect(actualViewState.attachment, attachment);
  });

  test('get course announcement returns a proper view state with no attachments', () async {
    final announcementId = '123';
    final courseId = '123';
    final announcementMessage = 'hodor';
    final announcementSubject = 'hodor subject';
    final announcementType = AnnouncementType.COURSE;
    final postedAt = DateTime.now();
    final courseName = 'flowers for hodornon';

    final announcementData = Announcement((b) => b
      ..id = announcementId
      ..postedAt = postedAt
      ..message = announcementMessage
      ..title = announcementSubject
      ..htmlUrl = ''
      ..attachments = ListBuilder<RemoteFile>([]));

    final courseData = Course((b) => b
      ..id = courseId
      ..enrollments = ListBuilder<Enrollment>()
      ..name = courseName
      ..needsGradingCount = 0
      ..hideFinalGrades = false
      ..isPublic = false
      ..applyAssignmentGroupWeights = false
      ..isFavorite = false
      ..accessRestrictedByDate = false
      ..hasWeightedGradingPeriods = false
      ..hasGradingPeriods = false
      ..restrictEnrollmentsToCourseDates = false);

    final expectedViewState =
        AnnouncementViewState(courseName, announcementSubject, announcementMessage, postedAt, null);

    final announcementApi = _MockAnnouncementApi();
    final courseApi = _MockCourseApi();
    when(announcementApi.getCourseAnnouncement(courseId, announcementId))
        .thenAnswer((_) => Future.value(announcementData));
    when(courseApi.getCourse(courseId)).thenAnswer((_) => Future.value(courseData));

    _setupLocator(announcementApi: announcementApi, courseApi: courseApi);

    final actualViewState =
        await AnnouncementDetailsInteractor().getAnnouncement(announcementId, announcementType, courseId, '');

    verify(announcementApi.getCourseAnnouncement(courseId, announcementId)).called(1);
    verify(courseApi.getCourse(courseId)).called(1);

    expect(actualViewState.toolbarTitle, expectedViewState.toolbarTitle);
    expect(actualViewState.announcementMessage, expectedViewState.announcementMessage);
    expect(actualViewState.announcementTitle, expectedViewState.announcementTitle);
    expect(actualViewState.postedAt, expectedViewState.postedAt);
    expect(actualViewState.attachment, null);
  });

  test('get institution announcement returns a proper view state', () async {
    final announcementId = '123';
    final courseId = '123';
    final announcementMessage = 'hodor';
    final announcementSubject = 'hodor subject';
    final announcementType = AnnouncementType.INSTITUTION;
    final postedAt = DateTime.now();
    final toolbarTitle = 'Institution Announcement';

    final accountNotificationData = AccountNotification((b) => b
      ..id = announcementId
      ..startAt = postedAt.toIso8601String()
      ..message = announcementMessage
      ..subject = announcementSubject);

    final expectedViewState =
        AnnouncementViewState(toolbarTitle, announcementSubject, announcementMessage, postedAt, null);

    final announcementApi = _MockAnnouncementApi();
    when(announcementApi.getAccountNotification(announcementId))
        .thenAnswer((_) => Future.value(accountNotificationData));

    _setupLocator(announcementApi: announcementApi);

    final actualViewState =
        await AnnouncementDetailsInteractor().getAnnouncement(announcementId, announcementType, courseId, toolbarTitle);

    verify(announcementApi.getAccountNotification(announcementId)).called(1);

    expect(actualViewState.toolbarTitle, expectedViewState.toolbarTitle);
    expect(actualViewState.announcementMessage, expectedViewState.announcementMessage);
    expect(actualViewState.announcementTitle, expectedViewState.announcementTitle);
    expect(actualViewState.postedAt, expectedViewState.postedAt);
  });
}

class _MockAnnouncementApi extends Mock implements AnnouncementApi {}

class _MockCourseApi extends Mock implements CourseApi {}
