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

import 'package:flutter_parent/api/utils/dio_config.dart';
import 'package:flutter_parent/api/utils/fetch.dart';
import 'package:flutter_parent/models/enrollment.dart';

class EnrollmentsApi {
  static Future<List<Enrollment>> getObserveeEnrollments({bool forceRefresh = false}) async {
    var dio = canvasDio(pageSize: PageSize.canvasMax, forceRefresh: forceRefresh);
    var params = {
      'include': ['observed_users', 'avatar_url'],
      'state': ['creation_pending', 'invited', 'active']
    };
    return fetchList(dio.get('users/self/enrollments', queryParameters: params), depaginateWith: dio);
  }
}
