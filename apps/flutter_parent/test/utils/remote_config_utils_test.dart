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

import 'package:firebase_remote_config/firebase_remote_config.dart';
import 'package:flutter_parent/utils/remote_config_utils.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import 'platform_config.dart';
import 'test_app.dart';
import 'test_helpers/mock_helpers.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  tearDown(() {
    RemoteConfigUtils.clean();
  });

  test('retrieval without initialization throws', () {
    expect(() => RemoteConfigUtils.getStringValue(RemoteConfigParams.TEST_STRING), throwsStateError);
  });

  test('double initialization throws', () async {
    await setupPlatformChannels();
    final mockRemoteConfig = setupMockRemoteConfig();
    await RemoteConfigUtils.initializeExplicit(mockRemoteConfig);
    expect(() async => await RemoteConfigUtils.initializeExplicit(mockRemoteConfig), throwsStateError);
  });

  test('unfetched, uncached value yields default', () async {
    // No cached values, no fetched values
    await setupPlatformChannels();
    final mockRemoteConfig = setupMockRemoteConfig();
    await RemoteConfigUtils.initializeExplicit(mockRemoteConfig);

    // default value = 'hey there'
    expect(RemoteConfigUtils.getStringValue(RemoteConfigParams.TEST_STRING), 'hey there');
  });

  test('fetched value trumps default value', () async {
    // Start up with no cached values
    await setupPlatformChannels();

    // Create a mocked RemoteConfig object that will fetch a 'test_string' value
    final mockRemoteConfig = setupMockRemoteConfig(valueSettings: {'test_string': 'fetched value'});
    await RemoteConfigUtils.initializeExplicit(mockRemoteConfig);

    expect(RemoteConfigUtils.getStringValue(RemoteConfigParams.TEST_STRING), 'fetched value');
  });

  test('cached value trumps default value', () async {
    // Create a cached value for the 'test string' flag.
    var platformConfig = PlatformConfig(mockPrefs: {'rc_test_string': 'cached value'});
    await setupPlatformChannels(config: platformConfig);

    // Create a mocked RemoteConfig object that does not refresh its data.
    final mockRemoteConfig = setupMockRemoteConfig();
    await RemoteConfigUtils.initializeExplicit(mockRemoteConfig);

    expect(RemoteConfigUtils.getStringValue(RemoteConfigParams.TEST_STRING), 'cached value');
  });

  test('fetched value trumps cached value', () async {
    // Create a cached value for the 'test string' flag.
    var platformConfig = PlatformConfig(mockPrefs: {'rc_test_string': 'cached value'});
    await setupPlatformChannels(config: platformConfig);

    // Create a mocked RemoteConfig object that refreshes with new data.
    final mockRemoteConfig = setupMockRemoteConfig(valueSettings: {'test_string': 'fetched value'});
    await RemoteConfigUtils.initializeExplicit(mockRemoteConfig);

    expect(RemoteConfigUtils.getStringValue(RemoteConfigParams.TEST_STRING), 'fetched value');
  });
}