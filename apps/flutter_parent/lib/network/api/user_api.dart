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

import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/models/user_colors.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class UserApi {
  Future<User> getSelf() => fetch(canvasDio(forceDeviceLanguage: true, forceRefresh: true).get('users/self/profile'));

  Future<User> getUserForDomain(String domain, String userId) async {
    var dio = DioConfig.canvas().copyWith(baseUrl: '$domain/api/v1/').dio;
    return fetch(dio.get('users/$userId/profile'));
  }

  Future<UserPermission> getSelfPermissions() =>
      fetch<User>(canvasDio(forceRefresh: true).get('users/self')).then((user) => user.permissions);

  Future<UserColors> getUserColors({bool refresh = false}) async {
    return fetch(canvasDio(forceRefresh: refresh).get('users/self/colors'));
  }

  Future<UserColors> setUserColor(String contextId, Color color) async {
    var hexCode = '#' + color.value.toRadixString(16).substring(2);
    var queryParams = {'hexcode': hexCode};
    return fetch(canvasDio().put('users/self/colors/$contextId', queryParameters: queryParams));
  }
}
