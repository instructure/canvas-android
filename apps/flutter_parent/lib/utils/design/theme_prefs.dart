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

import 'package:shared_preferences/shared_preferences.dart';

class ThemePrefs {
  static String PREF_KEY_DARK_MODE = 'dark_mode';

  static String PREF_KEY_HC_MODE = 'high_contrast_mode';

  static SharedPreferences _prefs;

  static Future<void> init() async {
    if (_prefs == null) _prefs = await SharedPreferences.getInstance();
  }

  const ThemePrefs();

  /// Returns the stored preference for dark mode. The get the value for the theme in use, call ParentTheme.of(context).isDarkMode
  bool get darkMode => _prefs.getBool(PREF_KEY_DARK_MODE) ?? false;

  /// Sets the dark mode value. Note that calling this only changes the stored preference. To update the theme in use,
  /// prefer setting ParentTheme.of(context).isDarkMode
  set darkMode(bool value) => _prefs.setBool(PREF_KEY_DARK_MODE, value);

  /// Returns the stored preference for high-contrast mode. To get the value for the theme in use, call ParentTheme.of(context).isHC
  bool get hcMode => _prefs.getBool(PREF_KEY_HC_MODE) ?? false;

  /// Sets the high contrast mode value. Note that calling this only changes the stored preference. To update the theme
  /// in use, prefer setting ParentTheme.of(context).isHC
  set hcMode(bool value) => _prefs.setBool(PREF_KEY_HC_MODE, value);
}
