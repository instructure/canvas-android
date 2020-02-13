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
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/help/help_dialog.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_dialog.dart';
import 'package:flutter_parent/utils/veneers/AndroidIntentVeneer.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:url_launcher_platform_interface/url_launcher_platform_interface.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';

void main() {
  final l10n = AppLocalizations();

  testWidgetsWithAccessibilityChecks('displays all options', (tester) async {
    await TestApp.showWidgetFromTap(tester, (context) => HelpDialog.asDialog(context));

    expect(find.text(l10n.help), findsOneWidget);

    expect(find.text(l10n.helpSearchCanvasDocsLabel), findsOneWidget);
    expect(find.text(l10n.helpSearchCanvasDocsDescription), findsOneWidget);

    expect(find.text(l10n.helpReportProblemLabel), findsOneWidget);
    expect(find.text(l10n.helpReportProblemDescription), findsOneWidget);

    expect(find.text(l10n.helpRequestFeatureLabel), findsOneWidget);
    expect(find.text(l10n.helpRequestFeatureDescription), findsOneWidget);

    expect(find.text(l10n.helpShareLoveLabel), findsOneWidget);
    expect(find.text(l10n.helpShareLoveDescription), findsOneWidget);
    // TODO: Uncomment once legal has been added
//    expect(find.text(l10n.helpLegalLabel), findsOneWidget);
//    expect(find.text(l10n.helpLegalDescription), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('tapping search launches url', (tester) async {
    var mockLauncher = _MockUrlLauncherPlatform();
    UrlLauncherPlatform.instance = mockLauncher;

    await TestApp.showWidgetFromTap(tester, (context) => HelpDialog.asDialog(context));

    await tester.tap(find.text(l10n.helpSearchCanvasDocsLabel));
    await tester.pumpAndSettle();

    verify(
      mockLauncher.launch(
        'https://community.canvaslms.com/community/answers/guides/mobile-guide/content?filterID=contentstatus%5Bpublished%5D~category%5Btable-of-contents%5D',
        useSafariVC: anyNamed('useSafariVC'),
        useWebView: anyNamed('useWebView'),
        enableJavaScript: anyNamed('enableJavaScript'),
        enableDomStorage: anyNamed('enableDomStorage'),
        universalLinksOnly: anyNamed('universalLinksOnly'),
        headers: anyNamed('headers'),
      ),
    ).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping share love launches url', (tester) async {
    var mockLauncher = _MockUrlLauncherPlatform();
    UrlLauncherPlatform.instance = mockLauncher;

    await TestApp.showWidgetFromTap(tester, (context) => HelpDialog.asDialog(context));

    await tester.tap(find.text(l10n.helpShareLoveLabel));
    await tester.pumpAndSettle();

    verify(
      mockLauncher.launch(
        'https://play.google.com/store/apps/details?id=com.instructure.parentapp',
        useSafariVC: anyNamed('useSafariVC'),
        useWebView: anyNamed('useWebView'),
        enableJavaScript: anyNamed('enableJavaScript'),
        enableDomStorage: anyNamed('enableDomStorage'),
        universalLinksOnly: anyNamed('universalLinksOnly'),
        headers: anyNamed('headers'),
      ),
    ).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping report problem shows error report dialog', (tester) async {
    var mockLauncher = _MockUrlLauncherPlatform();
    UrlLauncherPlatform.instance = mockLauncher;

    await TestApp.showWidgetFromTap(tester, (context) => HelpDialog.asDialog(context), highContrast: true);

    await tester.tap(find.text(l10n.helpReportProblemLabel));
    await tester.pumpAndSettle();

    expect(find.byType(ErrorReportDialog), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('tapping request feature launches email intent', (tester) async {
    await setupPlatformChannels();

    final user = User((b) => b
      ..id = '123'
      ..primaryEmail = '123@321.com'
      ..effectiveLocale = 'en-jp');
    final login = Login((b) => b..domain = 'dough main');
    final veneer = _MockAndroidIntentVeneer();
    setupTestLocator((locator) {
      locator.registerLazySingleton<AndroidIntentVeneer>(() => veneer);
    });

    ApiPrefs.switchLogins(login);
    ApiPrefs.setUser(user);

    String emailBody = '' +
        '${l10n.featureRequestHeader}\r\n' +
        '${l10n.helpUserId} ${user.id}\r\n' +
        '${l10n.helpEmail} ${user.primaryEmail}\r\n' +
        '${l10n.helpDomain} ${login.domain}\r\n' +
        '${l10n.versionNumber}: Canvas v1.0.0 (3)\r\n' +
        '${l10n.helpLocale} ${user.effectiveLocale}\r\n' +
        '----------------------------------------------\r\n';

    final completer = Completer();
    await MethodChannel('intent').setMockMethodCallHandler((MethodCall call) async {
      expect(call.method, 'startActivity');
      expect(call.arguments['action'], 'android.intent.action.SENDTO');
      expect(call.arguments['data'], 'mailto:');
      expect(call.arguments['extra'], {
        'android.intent.extra.EMAIL': ['mobilesupport@instructure.com'],
        'android.intent.extra.SUBJECT': l10n.featureRequestSubject,
        'android.intent.extra.TEXT': emailBody,
      });

      completer.complete(); // Finish the completer so the test can finish
      return null;
    });

    await TestApp.showWidgetFromTap(tester, (context) => HelpDialog.asDialog(context));

    await tester.tap(find.text(l10n.helpRequestFeatureLabel));
    await tester.pumpAndSettle();

    await completer.future; // Wait for the completer to finish the test
  });

  // TODO: Test legal once it's added
}

class _MockUrlLauncherPlatform extends Mock with MockPlatformInterfaceMixin implements UrlLauncherPlatform {}

class _MockAndroidIntentVeneer extends Mock implements AndroidIntentVeneer {}
