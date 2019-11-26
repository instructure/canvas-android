/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:dio/dio.dart';
import 'package:flutter_parent/api/utils/paged_list.dart';
import 'package:flutter_parent/models/serializers.dart';

/// Fetches and deserializes a response using the given [request].
Future<T> fetch<T>(Future<Response<dynamic>> request) async {
  try {
    final response = await request;
    return deserialize<T>(response.data);
  } catch (e) {
    print(e);
    return Future.error(e);
  }
}

/// Fetches and deserializes a list of items using the given [request]. To depaginate the list (i.e. perform multiple
/// requests to exhaust pagination), provide a [Dio] instance for [depaginateWith] that is configured for use
/// with subsequent page requests (cache behavior, authentications headers, etc).
Future<List<T>> fetchList<T>(
  Future<Response<dynamic>> request, {
  Dio depaginateWith: null,
}) async {
  try {
    var response = await request;
    if (depaginateWith == null) return deserializeList(response.data);
    depaginateWith.options.baseUrl = null;
    var pagedList = PagedList<T>(response);
    while (pagedList.nextUrl != null) {
      response = await depaginateWith.get(pagedList.nextUrl);
      pagedList.updateWithResponse(response);
    }
    return pagedList.data;
  } catch (e) {
    print(e);
    return Future.error(e);
  }
}
