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
import 'package:flutter_parent/models/color_change_response.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/user_color.dart';
import 'package:flutter_parent/network/api/user_api.dart';
import 'package:flutter_parent/screens/manage_students/student_color_picker_interactor.dart';
import 'package:flutter_parent/utils/db/user_colors_db.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/canvas_model_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() async {
  Login login = Login((b) => b
    ..user = CanvasModelTestUtils.mockUser(id: '123').toBuilder()
    ..domain = 'test-domain');
  MockUserApi userApi = MockUserApi();
  MockUserColorsDb db = MockUserColorsDb();

  await setupTestLocator((locator) {
    locator.registerLazySingleton<UserApi>(() => userApi);
    locator.registerLazySingleton<UserColorsDb>(() => db);
  });

  setUp(() async {
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));
    reset(userApi);
    reset(db);
  });

  test('Calls UserApi with correct parameters', () async {
    String studentId = '456';
    Color color = Colors.pinkAccent;

    final colorChangeResponse =
        ColorChangeResponse((b) => b..hexCode = '#123456');
    when(userApi.setUserColor('user_456', color))
        .thenAnswer((_) async => Future.value(colorChangeResponse));

    await StudentColorPickerInteractor().save(studentId, color);
    verify(userApi.setUserColor('user_456', color));
  });

  test('Throws exception when color change response color is null', () async {
    String studentId = '456';
    Color color = Colors.pinkAccent;

    final colorChangeResponse = ColorChangeResponse((b) => b..hexCode = null);
    when(userApi.setUserColor('user_456', color))
        .thenAnswer((_) async => Future.value(colorChangeResponse));

    expect(
        () async => await StudentColorPickerInteractor().save(studentId, color),
        throwsException);
  });

  test('Calls database with correct data', () async {
    String studentId = '456';
    Color color = Colors.pinkAccent;

    final colorChangeResponse =
        ColorChangeResponse((b) => b..hexCode = '#123456');
    when(userApi.setUserColor('user_456', color))
        .thenAnswer((_) async => Future.value(colorChangeResponse));

    UserColor expectedData = UserColor((b) => b
      ..userId = login.user.id
      ..userDomain = login.domain
      ..canvasContext = 'user_456'
      ..color = color);

    await StudentColorPickerInteractor().save(studentId, color);
    verify(db.insertOrUpdate(expectedData));
  });
}
