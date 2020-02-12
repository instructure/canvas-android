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
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/recipient.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../../utils/test_app.dart';

void main() {
  String studentId = 'student_123';

  final inboxApi = _MockInboxApi();
  final courseApi = _MockCourseApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<InboxApi>(() => inboxApi);
    locator.registerLazySingleton<CourseApi>(() => courseApi);
  });

  setUp(() {
    reset(inboxApi);
    reset(courseApi);
  });

  test('createConversation calls InboxApi.createConversation', () {
    var interactor = CreateConversationInteractor();
    final course = Course();
    final recipients = ['1', '2', '3'];
    final subject = 'Message Subject';
    final body = 'Message Body';
    final attachments = ['4', '5', '6'];

    interactor.createConversation(course.id, recipients, subject, body, attachments);
    verify(inboxApi.createConversation(course.id, recipients, subject, body, attachments)).called(1);
  });

  test('getAllRecipients calls InboxApi and CourseApi', () async {
    when(inboxApi.getRecipients(any)).thenAnswer((_) async => []);
    when(courseApi.getCourse(any)).thenAnswer((_) async => Course());

    final course = Course();
    await CreateConversationInteractor().loadData(course.id, studentId);

    verify(inboxApi.getRecipients(course.id));
    verify(courseApi.getCourse(course.id));
  });

  test('getAllRecipients returns only teachers and specified student', () async {
    final course = Course((c) => c..id = 'course_1');
    when(courseApi.getCourse(any)).thenAnswer((_) async => course);

    var teacher = Recipient((r) => r
      ..id = 'teacher_789'
      ..name = 'Teacher'
      ..commonCourses = MapBuilder({
        course.id: BuiltList<String>(['TeacherEnrollment'])
      }));

    var observer = Recipient((r) => r
      ..id = 'observer_456'
      ..name = 'Observer'
      ..commonCourses = MapBuilder({
        course.id: BuiltList<String>(['ObserverEnrollment'])
      }));

    var student = Recipient((r) => r
      ..id = studentId
      ..name = 'Student'
      ..commonCourses = MapBuilder({
        course.id: BuiltList<String>(['StudentEnrollment'])
      }));

    var otherStudent = Recipient((r) => r
      ..id = 'other_student'
      ..name = 'Other student'
      ..commonCourses = MapBuilder({
        'course_2': BuiltList<String>(['StudentEnrollment'])
      }));

    final allRecipients = [teacher, observer, student, otherStudent];
    final expectedRecipients = [teacher, student];

    when(inboxApi.getRecipients(any)).thenAnswer((_) async => allRecipients);

    final actual = await CreateConversationInteractor().loadData(course.id, studentId);

    expect(actual.recipients, expectedRecipients);
  });
}

class _MockInboxApi extends Mock implements InboxApi {}

class _MockCourseApi extends Mock implements CourseApi {}
