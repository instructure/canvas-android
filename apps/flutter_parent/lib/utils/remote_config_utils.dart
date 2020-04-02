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

import 'package:firebase_remote_config/firebase_remote_config.dart';
import 'package:flutter/widgets.dart';
import 'package:shared_preferences/shared_preferences.dart';

// All of the Remote Config params that we choose to care about.
enum RemoteConfigParams { TEST_STRING, QR_LOGIN_ENABLED_PARENT }

class RemoteConfigUtils {
  static RemoteConfig _remoteConfig = null;
  static SharedPreferences _prefs;

  // I bifurcated initialize() into initialize() and initializeExplicit() to allow for
  // tests to pass in a mocked RemoteConfig object.
  // This is the normal initializer that should be used from production code.
  static Future<void> initialize() async {
    RemoteConfig freshRemoteConfig = await RemoteConfig.instance;
    await initializeExplicit(freshRemoteConfig);
  }

  @visibleForTesting
  static void clean() {
    _remoteConfig = null;
    _prefs = null;
  }

  @visibleForTesting
  static Future<void> initializeExplicit(RemoteConfig remoteConfig) async {
    if (_remoteConfig != null) throw StateError('double-initialization of RemoteConfigUtils');

    _remoteConfig = remoteConfig;

    // fetch data from Firebase
    await _remoteConfig.fetch(expiration: const Duration(hours: 1));
    var updated = await _remoteConfig.activateFetched();

    // Grab a SharedPreferences instance
    _prefs = await SharedPreferences.getInstance();

    if (updated) {
      // If we actually fetched something, then store the fetched info into _prefs
      RemoteConfigParams.values.forEach((rc) {
        String rcParamName = _getRemoteConfigName(rc);
        String rcParamValue = _remoteConfig.getString(rcParamName);
        String rcPreferencesName = _getSharedPreferencesName(rc);
        print(
            "RemoteConfigUtils.initialize(): fetched $rcParamName=${rcParamValue == null ? "null" : "\"$rcParamValue\""}");
        _prefs.setString(rcPreferencesName, rcParamValue);
      });
    } else {
      // Otherwise, some log info.  The log info here and above will serve as a substitute for
      // a local remote-config settings page, which is not supported at this time.
      print("RemoteConfigUtils.initialize(): No update");
      RemoteConfigParams.values.forEach((rc) {
        String rcParamName = _getRemoteConfigName(rc);
        String rcPreferencesName = _getSharedPreferencesName(rc);
        String rcParamValue = _prefs.getString(rcPreferencesName);
        print(
            "RemoteConfigUtils.initialize(): cached $rcParamName value = ${rcParamValue == null ? "null" : "\"$rcParamValue\""}");
      });
    }
  }

  static String getStringValue(RemoteConfigParams rcParam) {
    if (_remoteConfig == null) throw StateError('RemoteConfigUtils not yet initialized');

    var rcName = _getRemoteConfigName(rcParam);
    var rcDefault = _getRemoteConfigDefaultValue(rcParam);
    var rcPreferencesName = _getSharedPreferencesName(rcParam);
    var result = _prefs.getString(rcPreferencesName);
    if (result == null) {
      result = rcDefault;
      _prefs.setString(rcPreferencesName, rcDefault);
    }
    return result;
  }

  // TODO: Get bool, double, int.  But we have historically just used string values.

  // Utility method to fetch the remote config variable name associated with rcParam.
  // Switch statements are required to cover all possible cases, so if we add
  // a new element in RemoveConfigParams, we'll be forced to add handling for
  // it here.
  static String _getRemoteConfigName(RemoteConfigParams rcParam) {
    switch (rcParam) {
      case RemoteConfigParams.TEST_STRING:
        return "test_string";
      case RemoteConfigParams.QR_LOGIN_ENABLED_PARENT:
        return "qr_login_enabled_parent";
    }
  }

  // Utility method to fetch the default (string) value associated with rcParam.
  // Switch statements are required to cover all possible cases, so if we add
  // a new element in RemoveConfigParams, we'll be forced to add handling for
  // it here.
  static String _getRemoteConfigDefaultValue(RemoteConfigParams rcParam) {
    switch (rcParam) {
      case RemoteConfigParams.TEST_STRING:
        return "hey there";
      case RemoteConfigParams.QR_LOGIN_ENABLED_PARENT:
        return "false";
    }
  }

  // Utility method to fetch the name of the SharedPreferences entry
  // that corresponds to rcParam.  Just prepends an "rc_" to the
  // remote config name for rcParam.
  static String _getSharedPreferencesName(RemoteConfigParams rcParam) {
    return "rc_${_getRemoteConfigName(rcParam)}";
  }
}
