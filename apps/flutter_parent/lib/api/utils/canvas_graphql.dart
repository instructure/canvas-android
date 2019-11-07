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

/*
 * A starting place for graph ql, once it is in a place where we can use it for parent. Currently, courses don't return
 * their syllabus, and assignments don't return any submission data for observers. The other areas of the app are not
 * available at all for graph ql (alerts, managing children, inbox). For now, all requests will need to be made via REST.

import 'package:graphql/client.dart';

/// A class to handle headers and authentication for talking to Canvas via Graph QL
class CanvasGraphQl {
  static String _authToken() {
//    return ApiPrefs.accessToken;
  }

  static String _baseDomain() {
//    return ApiPrefs.getDomain();
  }

//  region fields

  static final AuthLink _authLink = AuthLink(
    // getToken is called every time a request is made, so a dynamic lookup here will work.
    // Instead of _tokenToUse, could be ApiPrefs.accessToken
    getToken: () => "Bearer ${_authToken()}",
  );

  static final HttpLink _baseUrl = HttpLink(
    uri: "https://${_baseDomain()}/api/graphql/",
    headers: {
      "GraphQL-Metrics": "true", // Copied from canvas-android
      "User-Agent": ApiPrefs.getUserAgent(),
    },
  );

  static final GraphQLClient _client = GraphQLClient(
    link: _authLink.concat(_baseUrl),
    cache: NormalizedInMemoryCache(
      dataIdFromObject: typenameDataIdFromObject,
    ),
  );

  static bool _isAuthError(QueryResult result) {
    // There don't seem to get any headers in result, so can't check status code
    return result.errors?.isNotEmpty == true && result.errors.first.message == "Invalid access token.";
  }

  /// A private method to handle refreshing the access token using the refresh token.
  static Future<void> _refreshAuth() {
    // Update the token to work
    return AuthApi.refreshToken(_baseDomain, _refreshToken).then((token) {
      _tokenToUse = token;
      return token;
    });
  }

  /// Query a GraphQl endpoint, also handles any authentication refresh appropriately
  static Future<QueryResult> query(QueryOptions options) {
    return _client.query(options).then((result) async {
      if (!_isAuthError(result)) {
        return result; // No error, just return the result
      } else {
        // We need to refresh auth and then perform the query again
        await _refreshAuth();
        return _client.query(options);
      }
    });
  }

  /// We don't need to be able to mutate anything for parents, so no implementation for now
//  static Future<QueryResult> mutate(QueryOptions options) {}
}
*/