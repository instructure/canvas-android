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

import 'package:dio/dio.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/fetch.dart';
import 'package:flutter_parent/network/utils/paged_list.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../utils/test_app.dart';
import '../utils/test_helpers/mock_helpers.dart';
import '../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  group('fetch', () {
    test('deserializes a response', () async {
      final User? response = await fetch(_request(_rawUser()));
      expect(response, _getUser());
    });

    test('catches errors and returns a Future.error', () async {
      bool fail = false;
      await fetch(_requestFail()).catchError((_) {
        fail = true; // Don't return, just update the flag
        return Future.value(null);
      });
      expect(fail, isTrue);
    });
  });

  group('fetch first page', () {
    test('deserializes a response', () async {
      final expected = [
        _getUser(id: '0'),
        _getUser(id: '1'),
      ];
      final PagedList<User>? response = await fetchFirstPage(_request(_rawUserList()));
      expect(response?.data, expected);
    });

    test('catches errors and returns a Future.error', () async {
      bool fail = false;
      await fetchFirstPage(_requestFail()).catchError((_) {
        fail = true;
        return Future.value(null);
      });
      expect(fail, isTrue);
    });
  });

  // TODO Fix test
  // Not able to test getting data for a next page, as we have no way of mocking Dio which is accessed directly in fetch
  group('fetch next page', () {
    test('catches errors and returns a Future.error', () async {
      await setupPlatformChannels();
      bool fail = false;
      await fetchNextPage(null).catchError((_) {
        fail = true;
        return Future.value(null);
      });
      expect(fail, isTrue);
    });
  }, skip: true);

  group('fetch list', () {
    test('deserializes a response', () async {
      final expected = [
        _getUser(id: '0'),
        _getUser(id: '1'),
      ];
      final List<User>? response = await fetchList(_request(_rawUserList()));
      expect(response, expected);
    });

    test('depaginates a response', () async {
      final expected = [
        _getUser(id: '0'),
        _getUser(id: '1'),
        _getUser(id: '2'),
        _getUser(id: '3'),
      ];

      final pageUrl = 'https://www.google.com';
      final request = _request(
        _rawUserList(),
        headers: Headers.fromMap({
          'link': ['<$pageUrl>; rel="next"']
        }),
      );

      final dio = MockDio();
      when(dio.options).thenReturn(BaseOptions());
      when(dio.get(pageUrl)).thenAnswer((_) => _request(_rawUserList(startIndex: 2)));

      final List<User>? response = await fetchList(request, depaginateWith: dio);
      expect(response, expected);
    });

    test('catches errors and returns a Future.error', () async {
      bool fail = false;
      await fetchList(_requestFail()).catchError((_) {
        fail = true; // Don't return, just update the flag
        return Future.value(null);
      });
      expect(fail, isTrue);
    });
  });
}

Future<Response<dynamic>> _request(data, {Headers? headers}) async => Response(data: data, headers: headers, requestOptions: RequestOptions(path: ''));

Future<Response<dynamic>> _requestFail() async => throw 'ErRoR';

// Mocks

User _getUser({String id = '0'}) {
  return User((b) => b..id = id);
}

List<Map<String, dynamic>> _rawUserList({int size = 2, int startIndex = 0}) =>
    List.generate(size, (index) => _rawUser(id: (startIndex + index).toString()));

Map<String, dynamic> _rawUser({String id = '0'}) => {
      'id': '$id',
      'name': '',
      'sortable_name': null,
      'short_name': null,
      'pronouns': null,
      'avatar_url': null,
      'primary_email': null,
      'locale': null,
      'effective_locale': null,
    };
