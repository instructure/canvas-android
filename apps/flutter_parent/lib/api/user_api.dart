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
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';

class UserApi {
  static Future<User> getSelf() async {
    var selfResponse = await Dio().get(ApiPrefs.getApiUrl() + 'users/self/profile',
        options: Options(headers: ApiPrefs.getHeaderMap()));

    if (selfResponse.statusCode == 200 || selfResponse.statusCode == 201) {
      return deserialize<User>(selfResponse.data);
    } else {
      return Future.error(selfResponse.statusMessage);
    }
  }
}
