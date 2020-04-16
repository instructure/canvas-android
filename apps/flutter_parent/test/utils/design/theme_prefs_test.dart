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
import 'package:flutter_parent/utils/design/theme_prefs.dart';
import 'package:test/test.dart';

import '../platform_config.dart';
import '../test_app.dart';

void main() async {
  setUp(() async {
    // Setup the platform channel with an empty map so our prefs work
    await setupPlatformChannels(config: PlatformConfig(mockPrefs: {}));
    await ThemePrefs.init();
    await ThemePrefs.clear();
  });

  test('defaults dark mode to false', () {
    expect(ThemePrefs().darkMode, isFalse);
  });

  test('defaults webview dark mode to false', () {
    expect(ThemePrefs().webViewDarkMode, isFalse);
  });

  test('defaults hc mode to false', () {
    expect(ThemePrefs().hcMode, isFalse);
  });

  test('can set dark mode', () {
    final prefs = ThemePrefs();
    prefs.darkMode = true;
    expect(prefs.darkMode, isTrue);
  });

  test('can set webview dark mode', () {
    final prefs = ThemePrefs();
    prefs.webViewDarkMode = true;
    expect(prefs.webViewDarkMode, isTrue);
  });

  test('can set hc mode', () {
    final prefs = ThemePrefs();
    prefs.hcMode = true;
    expect(prefs.hcMode, isTrue);
  });
}
