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
import 'package:built_value/serializer.dart';
import 'package:dio/dio.dart';
import 'package:flutter_student_embed/models/serializers.dart';
import 'package:flutter_student_embed/models/user.dart';
import 'package:flutter_student_embed/network/utils/paged_list.dart';
import 'package:test/test.dart';

void main() {
  test('has no data', () {
    PagedList list = PagedList(Response());
    expect(list.data, []);
  });

  test('has no headers', () {
    PagedList list = PagedList(Response());
    expect(list.nextUrl, null);
  });

  test('has no link headers', () {
    final map = {
      'key': ['value']
    };
    PagedList list = PagedList(Response(headers: Headers.fromMap(map)));
    expect(list.nextUrl, null);
  });

  test('has no next in link headers', () {
    final map = {
      'link': ['<https://www.google.com>; rel="last"']
    };
    PagedList list = PagedList(Response(headers: Headers.fromMap(map)));
    expect(list.nextUrl, null);
  });

  test('parses the next link', () {
    final testHeaders = Headers.fromMap({
      'link': [
        '''
<https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=1&per_page=10>; rel="current",<https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=2&per_page=10>; rel="next",<https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=1&per_page=10>; rel="first",<https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=5&per_page=10>; rel="last"
    '''
            .trim()
      ]
    });
    PagedList list = PagedList(Response(headers: testHeaders));
    expect(list.nextUrl,
        'https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=2&per_page=10');
  });

  test('updates the next link from a response', () {
    final testHeaders = Headers.fromMap({
      'link': [
        '''
<https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=1&per_page=10>; rel="current",<https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=2&per_page=10>; rel="next",<https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=1&per_page=10>; rel="first",<https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=5&per_page=10>; rel="last"
    '''
            .trim()
      ]
    });
    PagedList list = PagedList(Response());
    expect(list.nextUrl, null);

    list.updateWithResponse(Response(headers: testHeaders));
    expect(list.nextUrl,
        'https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=2&per_page=10');
  });

  test('updates the next link from a paged list', () {
    final testHeaders = Headers.fromMap({
      'link': [
        '''
<https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=1&per_page=10>; rel="current",<https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=2&per_page=10>; rel="next",<https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=1&per_page=10>; rel="first",<https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=5&per_page=10>; rel="last"
    '''
            .trim()
      ]
    });
    PagedList list = PagedList(Response());
    expect(list.nextUrl, null);

    list.updateWithPagedList(PagedList(Response(headers: testHeaders)));
    expect(list.nextUrl,
        'https://mobiledev.instructure.com/api/v1/courses/549835/assignments?include%5B%5D=rubric_assessment&needs_grading_count_by_section=true&order_by=position&override_assignment_dates=true&page=2&per_page=10');
  });

  test('parses data', () {
    // Set up the serializer to handle this custom type
    final type = FullType(BuiltList, [FullType(User)]);
    final serializer = (jsonSerializers.toBuilder()..addBuilderFactory(type, () => ListBuilder<User>())).build();

    // Generate the data
    final data = List.generate(4, (index) {
      return User((builder) => builder
        ..name = 'Person $index'
        ..avatarUrl = 'https://domain.instructure.com/user_avatar/$index'
        ..id = '$index');
    });
    final serializedData = serializer.serialize(BuiltList<User>(data), specifiedType: type);
    PagedList<User> list = PagedList<User>(Response(data: serializedData));

    expect(list.data, data);
  });

  test('updates data with resposne', () {
    // Set up the serializer to handle this custom type
    final type = FullType(BuiltList, [FullType(User)]);
    final serializer = (jsonSerializers.toBuilder()..addBuilderFactory(type, () => ListBuilder<User>())).build();

    // Generate the data
    final data = List.generate(4, (index) {
      return User((builder) => builder
        ..name = 'Person $index'
        ..avatarUrl = 'https://domain.instructure.com/user_avatar/$index'
        ..id = '$index');
    });
    final serializedData = serializer.serialize(BuiltList<User>(data), specifiedType: type);
    PagedList<User> list = PagedList<User>(Response(data: serializedData));

    final dataAlt = List.generate(4, (index) {
      return User((builder) => builder
        ..name = 'alt Person $index'
        ..avatarUrl = 'https://domain.instructure.com/user_avatar/alt_$index'
        ..id = 'alt_$index');
    });
    final serializedDataAlt = serializer.serialize(BuiltList<User>(dataAlt), specifiedType: type);

    list.updateWithResponse(Response(data: serializedDataAlt));

    expect(list.data, data + dataAlt);
  });

  test('updates data with paged list', () {
    // Set up the serializer to handle this custom type
    final type = FullType(BuiltList, [FullType(User)]);
    final serializer = (jsonSerializers.toBuilder()..addBuilderFactory(type, () => ListBuilder<User>())).build();

    // Generate the data
    final data = List.generate(4, (index) {
      return User((builder) => builder
        ..name = 'Person $index'
        ..avatarUrl = 'https://domain.instructure.com/user_avatar/$index'
        ..id = '$index');
    });
    final serializedData = serializer.serialize(BuiltList<User>(data), specifiedType: type);
    PagedList<User> list = PagedList<User>(Response(data: serializedData));

    final dataAlt = List.generate(4, (index) {
      return User((builder) => builder
        ..name = 'alt Person $index'
        ..avatarUrl = 'https://domain.instructure.com/user_avatar/alt_$index'
        ..id = 'alt_$index');
    });
    final serializedDataAlt = serializer.serialize(BuiltList<User>(dataAlt), specifiedType: type);
    PagedList<User> listAlt = PagedList<User>(Response(data: serializedDataAlt));

    list.updateWithPagedList(listAlt);

    expect(list.data, data + dataAlt);
  });
}
