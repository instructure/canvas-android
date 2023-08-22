// Copyright (C) 2023 - present Instructure, Inc.
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

import 'package:encrypted_shared_preferences/encrypted_shared_preferences.dart';
import 'package:flutter_parent/network/api/features_api.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class FeaturesUtils {

  static const String KEY_SEND_USAGE_METRICS = 'send_usage_metrics';

  static EncryptedSharedPreferences? _prefs;

  static Future<void> init() async {
    if (_prefs == null) _prefs = await EncryptedSharedPreferences.getInstance();
  }

  static Future<void> checkUsageMetricFeatureFlag() async {
    await init();
    final featureFlags = await locator<FeaturesApi>().getFeatureFlags();
    await _prefs?.setBool(KEY_SEND_USAGE_METRICS, featureFlags?.sendUsageMetrics);
  }

  static Future<bool> getUsageMetricFeatureFlag() async {
    await init();
    return await _prefs?.getBool(KEY_SEND_USAGE_METRICS) == true;
  }

  static Future<void> performLogout() async {
    if (_prefs != null) {
      await _prefs?.remove(KEY_SEND_USAGE_METRICS);
    }
  }
}