/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

// Create a mocked RemoteConfig object.
// If valueSettings != null, then (1) a mocked settings fetch will occur, and (2) the retrieved
// settings will correspond the specified values.
import 'package:firebase_remote_config/firebase_remote_config.dart';
import 'package:mockito/mockito.dart';

MockRemoteConfig setupMockRemoteConfig({Map<String, String> valueSettings = null}) {
  final mockRemoteConfig = MockRemoteConfig();
  when(mockRemoteConfig.fetch()).thenAnswer((_) => Future.value());
  when(mockRemoteConfig.activateFetched()).thenAnswer((_) => Future.value(valueSettings != null));
  if (valueSettings != null) {
    valueSettings.forEach((key, value) {
      when(mockRemoteConfig.getString(key)).thenAnswer((_) => value);
    });
  }

  return mockRemoteConfig;
}

class MockRemoteConfig extends Mock implements RemoteConfig {}