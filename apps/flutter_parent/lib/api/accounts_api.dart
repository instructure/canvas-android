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
import 'package:dio_http_cache/dio_http_cache.dart';
import 'package:flutter_parent/models/school_domain.dart';
import 'package:flutter_parent/models/serializers.dart';

class AccountsApi {
  static Future<List<SchoolDomain>> searchDomains(String query) async {
    var dio = Dio();
    dio.interceptors.add(DioCacheManager(CacheConfig(baseUrl: "canvas.instructure.com")).interceptor);
    var response = await dio.get(
      "https://canvas.instructure.com/api/v1/accounts/search",
      queryParameters: {'search_term': query},
      options: buildCacheOptions(Duration(minutes: 5)),
    );
    if (response.statusCode == 200) {
      return Future.value(deserializeList<SchoolDomain>(response.data));
    }
    return Future.error(response.statusCode);
  }
}
