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

import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_test/src/deprecated.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('launch passes correct parameters to channel', () async {
    UrlLauncher launcher = UrlLauncher();
    String expectedUrl = 'www.instructure.com';
    bool expectedExclude = true;

    launcher.channel.setMockMethodCallHandler((call) async {
      expect(call.method, UrlLauncher.launchMethod);
      expect(call.arguments['url'], expectedUrl);
      expect(call.arguments['excludeInstructure'], expectedExclude);
    });
    await launcher.launch(expectedUrl, excludeInstructure: expectedExclude);
  });

  test('canLaunch passes correct parameters to channel', () async {
    UrlLauncher launcher = UrlLauncher();
    String expectedUrl = 'www.instructure.com';
    bool expectedExclude = true;

    launcher.channel.setMockMethodCallHandler((call) async {
      expect(call.method, UrlLauncher.canLaunchMethod);
      expect(call.arguments['url'], expectedUrl);
      expect(call.arguments['excludeInstructure'], expectedExclude);
    });
    await launcher.canLaunch(expectedUrl, excludeInstructure: expectedExclude);
  });
}
