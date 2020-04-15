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

import 'package:flutter_driver/flutter_driver.dart';

// Extensions on FlutterDriver that allow us to try pull-to-refresh if the initial attempt fails
// for some operations.
extension AutoRefresh on FlutterDriver {
  Future<String> getTextWithRefreshes(SerializableFinder finder,
      {int refreshes = 3, String expectedText = null}) async {
    final refreshFinder = find.byType("RefreshIndicator");
    for (int i = 0; i < refreshes; i++) {
      try {
        var result = await this.getText(finder, timeout: Duration(seconds: 1));
        if (expectedText != null && result != expectedText) {
          throw Error(); // Cause a refresh
        }
        return result;
      } catch (err) {
        await this.scroll(refreshFinder, 0, 200, Duration(milliseconds: 200));
        await Future.delayed(Duration(milliseconds: 500)); // Give ourselves time to load
      }
    }

    // We're out of retries; one more unprotected attempt
    return await this.getText(finder);
  }

  Future<void> tapWithRefreshes(SerializableFinder finder, {int refreshes = 3}) async {
    final refreshFinder = find.byType("RefreshIndicator");
    for (int i = 0; i < refreshes; i++) {
      try {
        await this.tap(finder, timeout: Duration(seconds: 1));
        return;
      } catch (err) {
        await this.scroll(refreshFinder, 0, 200, Duration(milliseconds: 200));
        await Future.delayed(Duration(milliseconds: 500)); // Give ourselves time to load
      }
    }

    // We're out of retries; one more unprotected attempt
    await this.tap(finder);
  }
}
