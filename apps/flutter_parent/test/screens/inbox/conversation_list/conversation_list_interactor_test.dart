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

import 'package:built_collection/built_collection.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/screens/inbox/conversation_list/conversation_list_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:tuple/tuple.dart';

import '../../../utils/test_app.dart';
import '../../../utils/test_helpers/mock_helpers.dart';
import '../../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  test('getConversations calls api for normal scope and sent scope', () async {
    var inboxApi = MockInboxApi();

    when(inboxApi.getConversations(
      scope: anyNamed('scope'),
      forceRefresh: anyNamed('forceRefresh'),
    )).thenAnswer((_) => Future.value([]));

    await setupTestLocator((locator) {
      locator.registerLazySingleton<InboxApi>(() => inboxApi);
    });

    var interactor = ConversationListInteractor();
    interactor.getConversations();
    verify(inboxApi.getConversations(forceRefresh: false)).called(1);
    verify(inboxApi.getConversations(scope: 'sent', forceRefresh: false)).called(1);
  });

  test('getConversations merges scopes and removes duplicates from sent scope', () async {
    var inboxApi = MockInboxApi();

    await setupTestLocator((locator) {
      locator.registerLazySingleton<InboxApi>(() => inboxApi);
    });

    var normalScopeItems = [
      Conversation((c) => c
        ..id = '0'
        ..lastMessageAt = DateTime.now()
        ..lastMessage = 'Message to user'),
      Conversation((c) => c
        ..id = '1'
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 1))
        ..lastMessage = 'Message to user'),
    ];

    var sentScopeItems = [
      Conversation((c) => c
        ..id = '1'
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 1, hours: 1))
        ..lastMessage = 'Message from User'),
      Conversation((c) => c
        ..id = '2'
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 2))
        ..lastMessage = 'Message from User'),
    ];

    var expectedItems = [normalScopeItems[0], normalScopeItems[1], sentScopeItems[1]];

    when(inboxApi.getConversations(
      scope: anyNamed('scope'),
      forceRefresh: anyNamed('forceRefresh'),
    )).thenAnswer((_) => Future.value(normalScopeItems));

    when(inboxApi.getConversations(
      scope: argThat(equals('sent'), named: 'scope'),
      forceRefresh: anyNamed('forceRefresh'),
    )).thenAnswer((_) => Future.value(sentScopeItems));

    var interactor = ConversationListInteractor();
    var actualItems = await interactor.getConversations();
    expect(listEquals(expectedItems, actualItems), isTrue);
  });

  test('getConversations orders items by date (descending)', () async {
    var inboxApi = MockInboxApi();

    await setupTestLocator((locator) {
      locator.registerLazySingleton<InboxApi>(() => inboxApi);
    });

    var apiItems = [
      Conversation((c) => c
        ..id = '0'
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 3))
        ..lastMessage = 'Message to user'),
      Conversation((c) => c
        ..id = '1'
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 5))
        ..lastMessage = 'Message to user'),
      Conversation((c) => c
        ..id = '2'
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 1))
        ..lastMessage = 'Message to user'),
      Conversation((c) => c
        ..id = '3'
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 2))
        ..lastMessage = 'Message to user'),
      Conversation((c) => c
        ..id = '4'
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 4))
        ..lastMessage = 'Message to user'),
    ];

    var expectedItems = [
      apiItems[2],
      apiItems[3],
      apiItems[0],
      apiItems[4],
      apiItems[1],
    ];

    when(inboxApi.getConversations(
      scope: anyNamed('scope'),
      forceRefresh: anyNamed('forceRefresh'),
    )).thenAnswer((_) => Future.value(List.from(apiItems)));

    var interactor = ConversationListInteractor();
    var actualItems = await interactor.getConversations();
    expect(listEquals(expectedItems, actualItems), isTrue);
  });

  test('getConversations produces error when API fails', () async {
    var inboxApi = MockInboxApi();

    await setupTestLocator((locator) {
      locator.registerLazySingleton<InboxApi>(() => inboxApi);
    });

    var expectedError = 'fail';
    when(inboxApi.getConversations(
      scope: anyNamed('scope'),
      forceRefresh: anyNamed('forceRefresh'),
    )).thenAnswer((_) => Future.error(expectedError));

    try {
      await ConversationListInteractor().getConversations();
      fail('Interactor did not propogate failure');
    } catch (e) {
      expect(e, equals(expectedError));
    }
  });

  test('getCoursesForCompose calls CourseApi', () async {
    var api = MockCourseApi();
    await setupTestLocator((locator) => locator.registerLazySingleton<CourseApi>(() => api));
    ConversationListInteractor().getCoursesForCompose();
    verify(api.getObserveeCourses()).called(1);
  });

  test('getStudentEnrollments calls EnrollmentsApi', () async {
    var api = MockEnrollmentsApi();
    await setupTestLocator((locator) => locator.registerLazySingleton<EnrollmentsApi>(() => api));
    ConversationListInteractor().getStudentEnrollments();
    verify(api.getObserveeEnrollments(forceRefresh: anyNamed('forceRefresh'))).called(1);
  });

  test('combineEnrollmentsAndCourses creates a map of courses with a sorted list of corresponding enorllments', () {
    String arithmetic = 'Arithmetic';
    String boxing = 'Boxing';
    String choir = 'Choir';

    List<Enrollment> andyEnrollments = _createEnrollments('Andy', [boxing, choir]);
    List<Enrollment> billEnrollments = _createEnrollments('Bill', [choir, arithmetic]);
    List<Enrollment> chericeEnrollments = _createEnrollments('Cherice', [choir, arithmetic, boxing]);

    var enrollments = [...andyEnrollments, ...billEnrollments, ...chericeEnrollments];

    Course arithmeticCourse = Course((b) => b
      ..id = arithmetic
      ..name = arithmetic
      ..enrollments = ListBuilder(enrollments.where((e) => e.courseId == arithmetic)));
    Course boxingCourse = Course((b) => b
      ..id = boxing
      ..name = boxing
      ..enrollments = ListBuilder(enrollments.where((e) => e.courseId == boxing)));
    Course choirCourse = Course((b) => b
      ..id = choir
      ..name = choir
      ..enrollments = ListBuilder(enrollments.where((e) => e.courseId == choir)));

    List<Tuple2<User, Course>> expectedResult = [
      Tuple2(andyEnrollments[0].observedUser!, boxingCourse),
      Tuple2(andyEnrollments[1].observedUser!, choirCourse),
      Tuple2(billEnrollments[1].observedUser!, arithmeticCourse),
      Tuple2(billEnrollments[0].observedUser!, choirCourse),
      Tuple2(chericeEnrollments[1].observedUser!, arithmeticCourse),
      Tuple2(chericeEnrollments[2].observedUser!, boxingCourse),
      Tuple2(chericeEnrollments[0].observedUser!, choirCourse),
    ];

    List<Tuple2<User, Course>> actual = ConversationListInteractor()
        .combineEnrollmentsAndCourses([choirCourse, boxingCourse, arithmeticCourse], enrollments);

    for (var i = 0; i < expectedResult.length; i++) {
      expect(actual[i].item1, expectedResult[i].item1);
      expect(actual[i].item2, expectedResult[i].item2);
    }
  });

  // This test simulates a 'pending' enrollment situation, where we get the enrollment but not the course
  test('combineEnrollmentsAndCourses handles enrollments without matching courses', () {
    String arithmetic = 'Arithmetic';
    String boxing = 'Boxing';
    String choir = 'Choir';

    List<Enrollment> andyEnrollments = _createEnrollments('Andy', [boxing, choir]);
    List<Enrollment> billEnrollments = _createEnrollments('Bill', [choir, arithmetic]);
    List<Enrollment> chericeEnrollments = _createEnrollments('Cherice', [choir, arithmetic, boxing]);
    List<Enrollment> pendingEnrollments = _createEnrollments('pending', ['pending']);

    var enrollments = [...andyEnrollments, ...billEnrollments, ...chericeEnrollments, ...pendingEnrollments];

    Course arithmeticCourse = Course((b) => b
      ..id = arithmetic
      ..name = arithmetic
      ..enrollments = ListBuilder(enrollments.where((e) => e.courseId == arithmetic)));
    Course boxingCourse = Course((b) => b
      ..id = boxing
      ..name = boxing
      ..enrollments = ListBuilder(enrollments.where((e) => e.courseId == boxing)));
    Course choirCourse = Course((b) => b
      ..id = choir
      ..name = choir
      ..enrollments = ListBuilder(enrollments.where((e) => e.courseId == choir)));

    List<Tuple2<User, Course>> expectedResult = [
      Tuple2(andyEnrollments[0].observedUser!, boxingCourse),
      Tuple2(andyEnrollments[1].observedUser!, choirCourse),
      Tuple2(billEnrollments[1].observedUser!, arithmeticCourse),
      Tuple2(billEnrollments[0].observedUser!, choirCourse),
      Tuple2(chericeEnrollments[1].observedUser!, arithmeticCourse),
      Tuple2(chericeEnrollments[2].observedUser!, boxingCourse),
      Tuple2(chericeEnrollments[0].observedUser!, choirCourse),
    ];

    List<Tuple2<User, Course>> actual = ConversationListInteractor()
        .combineEnrollmentsAndCourses([choirCourse, boxingCourse, arithmeticCourse], enrollments);

    for (var i = 0; i < expectedResult.length; i++) {
      expect(actual[i].item1, expectedResult[i].item1);
      expect(actual[i].item2, expectedResult[i].item2);
    }
  });

  test('sortCourses sorts courses by the first enrolled student in that course', () {
    String arithmetic = 'Arithmetic';
    String boxing = 'Boxing';
    String choir = 'Choir';

    List<Enrollment> andyEnrollments = _createEnrollments('Andy', [boxing, choir]);
    List<Enrollment> billEnrollments = _createEnrollments('Bill', [choir, arithmetic]);
    List<Enrollment> chericeEnrollments = _createEnrollments('Cherice', [choir, arithmetic, boxing]);

    var enrollments = [...andyEnrollments, ...billEnrollments, ...chericeEnrollments];

    Course arithmeticCourse = Course((b) => b
      ..id = arithmetic
      ..name = arithmetic
      ..enrollments = ListBuilder(enrollments.where((e) => e.courseId == arithmetic)));
    Course boxingCourse = Course((b) => b
      ..id = boxing
      ..name = boxing
      ..enrollments = ListBuilder(enrollments.where((e) => e.courseId == boxing)));
    Course choirCourse = Course((b) => b
      ..id = choir
      ..name = choir
      ..enrollments = ListBuilder(enrollments.where((e) => e.courseId == choir)));

    List<Tuple2<User, Course>> expected = [
      Tuple2(andyEnrollments[0].observedUser!, boxingCourse),
      Tuple2(andyEnrollments[1].observedUser!, choirCourse),
      Tuple2(billEnrollments[1].observedUser!, arithmeticCourse),
      Tuple2(billEnrollments[0].observedUser!, choirCourse),
      Tuple2(chericeEnrollments[1].observedUser!, arithmeticCourse),
      Tuple2(chericeEnrollments[2].observedUser!, boxingCourse),
      Tuple2(chericeEnrollments[0].observedUser!, choirCourse),
    ];

    List<Tuple2<User, Course>> actual = ConversationListInteractor().combineEnrollmentsAndCourses(
        [choirCourse, boxingCourse, arithmeticCourse], [...billEnrollments, ...andyEnrollments, ...chericeEnrollments]);

    for (int item = 0; item < actual.length; item++) {
      expect(actual[item].item1, expected[item].item1);
      expect(actual[item].item2, expected[item].item2);
    }
  });
}

List<Enrollment> _createEnrollments(String studentName, List<String> courseIds) {
  return courseIds
      .map((id) => Enrollment((b) => b
        ..observedUser = User((b) => b
          ..shortName = studentName
          ..id = studentName).toBuilder()
        ..courseId = id))
      .toList();
}