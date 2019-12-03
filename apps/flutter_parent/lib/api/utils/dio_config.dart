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
import 'package:flutter_parent/api/utils/api_prefs.dart';

Dio canvasDio({
  bool forceRefresh: false,
  bool forceDeviceLanguage: false,
  String overrideToken: null,
  Map<String, String> extraHeaders: null,
  bool usePerPageParam: false,
  int perPageSize: 100,
}) {
  // Configure base options
  var options = BaseOptions(
    baseUrl: ApiPrefs.getApiUrl(),
    headers: ApiPrefs.getHeaderMap(
      forceDeviceLanguage: forceDeviceLanguage,
      token: overrideToken,
      extraHeaders: extraHeaders,
    ),
  );

  // Add per_page query param if requested
  if (usePerPageParam) options.queryParameters = {'per_page': perPageSize};

  // Add cache configuration to base options
  var extras = buildCacheOptions(Duration(hours: 1), forceRefresh: forceRefresh).extra;
  options.extra.addAll(extras);

  // Create Dio instance and add interceptors
  var dio = Dio(options);

  // Cache manager
  dio.interceptors.add(DioCacheManager(CacheConfig(baseUrl: ApiPrefs.getDomain())).interceptor);

  // Log interceptor
  dio.interceptors.add(LogInterceptor(
    request: false,
    requestHeader: false,
    requestBody: false,
    responseHeader: false,
    responseBody: false,
  ));

  return dio;
}
