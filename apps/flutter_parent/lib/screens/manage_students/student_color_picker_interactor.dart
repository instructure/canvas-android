// Copyright (C) 2020 - present Instructure, Inc.
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

import 'package:flutter/material.dart';
import 'package:flutter_parent/models/user_color.dart';
import 'package:flutter_parent/network/api/user_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/db/user_colors_db.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class StudentColorPickerInteractor {
  Future<void> save(String studentId, Color newColor) async {
    var contextId = 'user_$studentId';
    final userColorsResponse = await locator<UserApi>().setUserColor(contextId, newColor);
    if (userColorsResponse?.hexCode != null) {
      UserColor data = UserColor((b) => b
        ..userId = ApiPrefs.getUser()?.id
        ..userDomain = ApiPrefs.getDomain()
        ..canvasContext = contextId
        ..color = newColor);
      await locator<UserColorsDb>().insertOrUpdate(data);
    } else {
      throw Exception('Failed to set user color');
    }
  }
}
