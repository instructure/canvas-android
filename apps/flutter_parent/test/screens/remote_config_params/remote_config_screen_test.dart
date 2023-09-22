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

import 'package:flutter/widgets.dart';
import 'package:flutter_parent/screens/remote_config/remote_config_interactor.dart';
import 'package:flutter_parent/screens/remote_config/remote_config_screen.dart';
import 'package:flutter_parent/utils/remote_config_utils.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {

  testWidgetsWithAccessibilityChecks('Shows the correct list', (tester) async {
    var interactor = MockRemoteConfigInteractor();
    setupTestLocator((locator) => locator.registerFactory<RemoteConfigInteractor>(() => interactor));

    Map<RemoteConfigParams, String> remoteConfigs = {
      RemoteConfigParams.MOBILE_VERIFY_BETA_ENABLED: 'false',
      RemoteConfigParams.TEST_STRING: 'fetched value'
    };

    when(interactor.getRemoteConfigParams()).thenReturn(remoteConfigs);

    await tester.pumpWidget(TestApp(RemoteConfigScreen()));
    await tester.pumpAndSettle();

    expect(find.text(RemoteConfigUtils.getRemoteConfigName(RemoteConfigParams.MOBILE_VERIFY_BETA_ENABLED)), findsOneWidget);
    expect(find.text('false'), findsOneWidget);
    expect(find.text(RemoteConfigUtils.getRemoteConfigName(RemoteConfigParams.TEST_STRING)), findsOneWidget);
    expect(find.text('fetched value'), findsOneWidget);
  });
}
