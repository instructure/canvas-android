import 'package:firebase_remote_config/firebase_remote_config.dart';
import 'package:flutter/widgets.dart';
import 'package:shared_preferences/shared_preferences.dart';

enum RemoteConfigParams { TEST_STRING, QR_LOGIN_ENABLED_PARENT }

class RemoteConfigUtils {
  static RemoteConfig _remoteConfig = null;
  static SharedPreferences _prefs;
  static final _RC_PREFS_PREFIX = "rc_";

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
        print(
            "RemoteConfigUtils.initialize(): fetched $rcParamName=${rcParamValue == null ? "null" : "\"$rcParamValue\""}");
        _prefs.setString("$_RC_PREFS_PREFIX$rcParamName", rcParamValue);
      });
    } else {
      // Otherwise, some log info.  The log info here and above will serve as a substitute for
      // a local remote-config settings page, which is not supported at this time.
      print("RemoteConfigUtils.initialize(): No update");
      RemoteConfigParams.values.forEach((rc) {
        String rcParamName = _getRemoteConfigName(rc);
        String rcParamValue = _prefs.getString("$_RC_PREFS_PREFIX$rcParamName");
        print(
            "RemoteConfigUtils.initialize(): cached $rcParamName value = ${rcParamValue == null ? "null" : "\"$rcParamValue\""}");
      });
    }
  }

  static String getStringValue(RemoteConfigParams rcParam) {
    if (_remoteConfig == null) throw StateError('RemoteConfigUtils not yet initialized');

    var rcName = _getRemoteConfigName(rcParam);
    var rcDefault = _getRemoteConfigDefaultValue(rcParam);
    var result = _prefs.getString("$_RC_PREFS_PREFIX$rcName");
    if (result == null) {
      result = rcDefault;
      _prefs.setString("$_RC_PREFS_PREFIX$rcName", rcDefault);
    }
    return result;
  }

  // TODO: Get bool, double, int

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
}
