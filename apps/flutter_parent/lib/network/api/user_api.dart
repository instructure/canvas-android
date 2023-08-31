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

import 'dart:ui';

import 'package:dio/dio.dart';
import 'package:flutter_parent/models/color_change_response.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/models/user_colors.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class UserApi {
  Future<User?> getSelf() async {
    var dio = canvasDio(forceDeviceLanguage: true, forceRefresh: true);
    return fetch(dio.get('users/self/profile'));
  }

  Future<User?> getUserForDomain(String domain, String userId) async {
    var dio = DioConfig.canvas().copyWith(baseUrl: '$domain/api/v1/').dio;
    return fetch(dio.get('users/$userId/profile'));
  }

  Future<UserPermission?> getSelfPermissions() async {
    var dio = canvasDio(forceRefresh: true);
    return fetch<User>(dio.get('users/self')).then((user) => user?.permissions);
  }

  Future<UserColors?> getUserColors({bool refresh = false}) async {
    var dio = canvasDio(forceRefresh: refresh);
    return fetch(dio.get('users/self/colors'));
  }

  Future<User?> acceptUserTermsOfUse() async {
    final queryParams = {'user[terms_of_use]': 1};
    var dio = canvasDio();
    return fetch(dio.put('users/self', queryParameters: queryParams));
  }

  Future<ColorChangeResponse?> setUserColor(String contextId, Color color) async {
    var hexCode = '#' + color.value.toRadixString(16).substring(2);
    var queryParams = {'hexcode': hexCode};
    var dio = canvasDio();
    return fetch(dio.put(
        'users/self/colors/$contextId',
        queryParameters: queryParams,
        options: Options(validateStatus: (status) => status != null && status < 500))); // Workaround, because this request fails for some legacy users, but we can't catch the error.));
  }
}
