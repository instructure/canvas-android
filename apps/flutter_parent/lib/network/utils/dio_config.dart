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

import 'dart:io';

import 'package:dio/dio.dart';
import 'package:dio_http_cache/dio_http_cache.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';

/// Class that helps to create and configure [Dio] instances for common use cases
class DioConfig {
  final String baseUrl;
  final Map<String, String> baseHeaders;
  final Duration cacheMaxAge;
  final bool forceRefresh;
  final PageSize pageSize;

  DioConfig({
    this.baseUrl = '',
    baseHeaders = null,
    this.cacheMaxAge = Duration.zero,
    this.forceRefresh = false,
    this.pageSize = PageSize.none,
  })  : this.baseHeaders = baseHeaders ?? {},
        assert(baseUrl != null),
        assert(cacheMaxAge != null),
        assert(forceRefresh != null),
        assert(pageSize != null);

  /// Creates a copy of this configuration with the given fields replaced with the new values
  DioConfig copyWith({
    String baseUrl,
    Map<String, String> baseHeaders,
    Duration cacheMaxAge,
    bool forceRefresh,
    PageSize pageSize,
  }) {
    return DioConfig(
      baseUrl: baseUrl ?? this.baseUrl,
      baseHeaders: baseHeaders ?? this.baseHeaders,
      cacheMaxAge: cacheMaxAge ?? this.cacheMaxAge,
      forceRefresh: forceRefresh ?? this.forceRefresh,
      pageSize: pageSize ?? this.pageSize,
    );
  }

  /// Creates a [Dio] instance using this configuration
  Dio get dio {
    // Add canvas-string-ids header to ensure Canvas IDs are returned as Strings
    baseHeaders[HttpHeaders.acceptHeader] = 'application/json+canvas-string-ids';

    // Configure base options
    var options = BaseOptions(baseUrl: baseUrl, headers: baseHeaders);

    // Add per_page query param if requested
    if (pageSize.size > 0) options.queryParameters = {'per_page': pageSize.size};

    // Add cache configuration to base options
    if (cacheMaxAge != Duration.zero) {
      var extras = buildCacheOptions(cacheMaxAge, forceRefresh: forceRefresh).extra;
      options.extra.addAll(extras);
    }

    // Create Dio instance and add interceptors
    final dio = Dio(options);

    // Cache manager
    if (cacheMaxAge != Duration.zero) {
      dio.interceptors.add(DioCacheManager(CacheConfig(baseUrl: baseUrl)).interceptor);
    }

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

  /// Creates a [DioConfig] targeted at typical Canvas API usage
  static DioConfig canvas({
    bool includeApiPath: true,
    bool forceRefresh: false,
    bool forceDeviceLanguage: false,
    String overrideToken: null,
    Map<String, String> extraHeaders: null,
    PageSize pageSize: PageSize.none,
  }) {
    return DioConfig(
        baseUrl: includeApiPath ? ApiPrefs.getApiUrl() : ApiPrefs.getDomain(),
        baseHeaders: ApiPrefs.getHeaderMap(
          forceDeviceLanguage: forceDeviceLanguage,
          token: overrideToken,
          extraHeaders: extraHeaders,
        ),
        cacheMaxAge: const Duration(hours: 1),
        forceRefresh: forceRefresh,
        pageSize: pageSize);
  }

  /// Creates a [DioConfig] targeted at core/free-for-teacher API usage (i.e. canvas.instructure.com)
  static DioConfig core({
    bool includeApiPath: true,
    Map<String, String> headers: null,
    Duration cacheMaxAge: Duration.zero,
    bool forceRefresh: false,
    PageSize pageSize: PageSize.none,
  }) {
    var baseUrl = 'https://canvas.instructure.com/';
    if (includeApiPath) baseUrl += 'api/v1/';

    return DioConfig(
        baseUrl: baseUrl,
        baseHeaders: headers,
        cacheMaxAge: cacheMaxAge,
        forceRefresh: forceRefresh,
        pageSize: pageSize);
  }
}

/// Class for configuring paging parameters
class PageSize {
  final int size;

  const PageSize(this.size);

  static const PageSize none = const PageSize(0);

  static const PageSize canvasDefault = const PageSize(10);

  static const PageSize canvasMax = const PageSize(10);

  @override
  bool operator ==(Object other) => identical(this, other) || other is PageSize && this.size == other.size;
}

/// Convenience method that returns a [Dio] instance configured by calling through to [DioConfig.canvas]
Dio canvasDio({
  bool includeApiPath: true,
  bool forceRefresh: false,
  bool forceDeviceLanguage: false,
  String overrideToken: null,
  Map<String, String> extraHeaders: null,
  PageSize pageSize: PageSize.none,
}) {
  return DioConfig.canvas(
    forceRefresh: forceRefresh,
    forceDeviceLanguage: forceDeviceLanguage,
    overrideToken: overrideToken,
    extraHeaders: extraHeaders,
    pageSize: pageSize,
  ).dio;
}
