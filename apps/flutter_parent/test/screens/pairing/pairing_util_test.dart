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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/pairing/pairing_util.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  AppLocalizations l10n = AppLocalizations();

  MockQuickNav nav = MockQuickNav();

  setupTestLocator((locator) {
    locator.registerLazySingleton<QuickNav>(() => nav);
  });

  setUp(() async {
    final mockRemoteConfig = setupMockRemoteConfig(valueSettings: {'qr_pair_observer_enabled': 'true'});
    await setupPlatformChannels(config: PlatformConfig(initRemoteConfig: mockRemoteConfig));
    reset(nav);
  });

  testWidgetsWithAccessibilityChecks('Displays Pairing Code and QR options if device has cameras', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    ApiPrefs.setCameraCount(1);

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    PairingUtil().pairNewStudent(context, () => null);
    await tester.pumpAndSettle();

    expect(find.text(l10n.pairingCode), findsOneWidget);
    expect(find.text(l10n.qrCode), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Only displays Pairing Code option if device has no cameras', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pump();

    ApiPrefs.setCameraCount(0);

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    PairingUtil().pairNewStudent(context, () => {});
    await tester.pumpAndSettle();

    expect(find.text(l10n.pairingCode), findsOneWidget);
    expect(find.text(l10n.qrCode), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Pairing Code option opens dialog', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    PairingUtil().pairNewStudent(context, () => null);
    await tester.pumpAndSettle();

    when(
      nav.showDialog(
        context: anyNamed('context'),
        barrierDismissible: anyNamed('barrierDismissible'),
        builder: anyNamed('builder'),
      ),
    ).thenAnswer((_) async => true);

    await tester.tap(find.text(l10n.pairingCode));
    await tester.pumpAndSettle();

    verify(
        nav.showDialog(
          context: anyNamed('context'),
          barrierDismissible: anyNamed('barrierDismissible'),
          builder: anyNamed('builder'),
        )
    );
  });

  testWidgetsWithAccessibilityChecks('QR Code option navigates to QR pairing screen', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    ApiPrefs.setCameraCount(1);

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    PairingUtil().pairNewStudent(context, () => null);
    await tester.pumpAndSettle();

    await tester.tap(find.text(l10n.qrCode));
    await tester.pumpAndSettle();

    logInvocations([nav]);

    verify(nav.pushRoute(any, PandaRouter.qrPairing()));
  });

  testWidgetsWithAccessibilityChecks('Pairing Code success calls onSuccess', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    bool onSuccessCalled = false;

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    PairingUtil().pairNewStudent(context, () => onSuccessCalled = true);
    await tester.pumpAndSettle();

    when(
      nav.showDialog(
        context: anyNamed('context'),
        barrierDismissible: anyNamed('barrierDismissible'),
        builder: anyNamed('builder'),
      ),
    ).thenAnswer((_) async => true);

    await tester.tap(find.text(l10n.pairingCode));
    await tester.pumpAndSettle();

    expect(onSuccessCalled, isTrue);
  });

  testWidgetsWithAccessibilityChecks('QR Code success calls onSuccess', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    ApiPrefs.setCameraCount(1);

    bool onSuccessCalled = false;

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    PairingUtil().pairNewStudent(context, () => onSuccessCalled = true);
    await tester.pumpAndSettle();

    when(nav.pushRoute(any, PandaRouter.qrPairing())).thenAnswer((_) async => true);

    await tester.tap(find.text(l10n.qrCode));
    await tester.pumpAndSettle();

    expect(onSuccessCalled, isTrue);
  });
}

class DummyWidget extends StatefulWidget {
  @override
  _DummyWidgetState createState() => _DummyWidgetState();
}

class _DummyWidgetState extends State<DummyWidget> {
  @override
  Widget build(Object context) => Material();
}
