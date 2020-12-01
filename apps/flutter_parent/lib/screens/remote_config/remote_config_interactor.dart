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

import 'package:flutter_parent/utils/remote_config_utils.dart';

class RemoteConfigInteractor {

  Future<Map<RemoteConfigParams, String>> getRemoteConfigParams() async {
    return Map.fromIterable(RemoteConfigParams.values, key: (rc) => rc, value: (rc) => RemoteConfigUtils.getStringValue(rc));
  }

  void updateRemoteConfig(RemoteConfigParams rcKey, String value) {
    RemoteConfigUtils.updateRemoteConfig(rcKey, value);
  }
}