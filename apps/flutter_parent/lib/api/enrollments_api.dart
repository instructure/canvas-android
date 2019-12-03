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
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/api/utils/paged_list.dart';
import 'package:flutter_parent/models/enrollment.dart';

class EnrollmentsApi {
  static Future<List<Enrollment>> getObserveeEnrollments() async {
    var observeesResponse = await Dio().get(ApiPrefs.getApiUrl() + 'users/self/enrollments',
        queryParameters: {
          'include': ['observed_users', 'avatar_url'],
          'state': ['creation_pending', 'invited', 'active']
        },
        options: Options(headers: ApiPrefs.getHeaderMap()));

    if (observeesResponse.statusCode == 200 || observeesResponse.statusCode == 201) {
      final response = PagedList<Enrollment>(observeesResponse);
      return (response.nextUrl == null) ? response.data : _getObserveesDepaginated(response);
    } else {
      return Future.error(observeesResponse.statusMessage);
    }
  }

  static Future<List<Enrollment>> _getObserveesDepaginated(PagedList<Enrollment> prevResponse) async {
    // Query params already specified in url
    var enrollmentResponse = await Dio().get(prevResponse.nextUrl, options: Options(headers: ApiPrefs.getHeaderMap()));

    if (enrollmentResponse.statusCode == 200 || enrollmentResponse.statusCode == 201) {
      prevResponse.updateWithResponse(enrollmentResponse);
      return (prevResponse.nextUrl == null) ? prevResponse.data : _getObserveesDepaginated(prevResponse);
    } else {
      return Future.error(enrollmentResponse.statusMessage);
    }
  }
}
