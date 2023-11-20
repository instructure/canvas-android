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
import 'package:flutter_parent/models/serializers.dart';
import 'package:collection/collection.dart';

/// A helper class to communicate back to api callers what the data and next url is for paged lists.
class PagedList<T> {
  String? nextUrl;
  List<T> data;

  PagedList(Response<dynamic> response)
      : data = deserializeList<T>(response.data),
        nextUrl = _parseNextUrl(response.headers);

  void updateWithResponse(Response<dynamic> response) => updateWithPagedList(PagedList(response));

  void updateWithPagedList(PagedList<T> pagedList) {
    data.addAll(pagedList.data);
    nextUrl = pagedList.nextUrl;
  }

  static String? _parseNextUrl(Headers? headers) {
    if (headers == null) return null;

    final links = headers['link']?.first.split(',');
    final next = links?.firstWhereOrNull((link) => link.contains('rel="next"'));

    return next?.substring(1, next.lastIndexOf('>'));
  }
}
