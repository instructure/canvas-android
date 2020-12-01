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
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/screens/remote_config/remote_config_interactor.dart';
import 'package:flutter_parent/utils/remote_config_utils.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';

void main() {
  test('getRemoteConfigParams returns correct map', () async {
    final mockRemoteConfig = setupMockRemoteConfig(valueSettings: {'test_string': 'fetched value', 'mobile_verify_beta_enabled' : 'false'});
    await setupPlatformChannels(
        config: PlatformConfig(initRemoteConfig: mockRemoteConfig));
    expect(RemoteConfigInteractor().getRemoteConfigParams(), equals({RemoteConfigParams.TEST_STRING: 'fetched value', RemoteConfigParams.MOBILE_VERIFY_BETA_ENABLED: 'false'}));
  });
}