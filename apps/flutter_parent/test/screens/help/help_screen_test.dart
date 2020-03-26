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

import 'package:built_collection/built_collection.dart';
import 'package:flutter/services.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/help_link.dart';
import 'package:flutter_parent/models/help_links.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/help/help_screen.dart';
import 'package:flutter_parent/screens/help/help_screen_interactor.dart';
import 'package:flutter_parent/screens/help/legal_screen.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_dialog.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/veneers/AndroidIntentVeneer.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:url_launcher_platform_interface/url_launcher_platform_interface.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';

void main() {
  final l10n = AppLocalizations();
  HelpScreenInteractor interactor = _MockHelpScreenInteractor();

  setupTestLocator((locator) {
    locator.registerSingleton<QuickNav>(QuickNav());
    locator.registerLazySingleton<AndroidIntentVeneer>(() => _MockAndroidIntentVeneer());
    locator.registerLazySingleton<HelpScreenInteractor>(() => interactor);
  });

  testWidgetsWithAccessibilityChecks('displays links', (tester) async {
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([createHelpLink()]));

    await tester.pumpWidget(TestApp(HelpScreen()));
    await tester.pumpAndSettle();

    expect(find.text('text'), findsOneWidget);
    expect(find.text('subtext'), findsOneWidget);

    expect(find.text(l10n.helpShareLoveLabel), findsOneWidget);
    expect(find.text(l10n.helpShareLoveDescription), findsOneWidget);

    expect(find.text(l10n.helpLegalLabel), findsOneWidget);
    expect(find.text(l10n.helpLegalDescription), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('tapping search launches url', (tester) async {
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh'))).thenAnswer(
        (_) => Future.value([createHelpLink(id: 'search_the_canvas_guides', text: 'Search the Canvas Guides')]));

    var mockLauncher = _MockUrlLauncherPlatform();
    UrlLauncherPlatform.instance = mockLauncher;

    await tester.pumpWidget(TestApp(HelpScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Search the Canvas Guides'));
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
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([]));

    var mockLauncher = _MockUrlLauncherPlatform();
    UrlLauncherPlatform.instance = mockLauncher;

    await tester.pumpWidget(TestApp(HelpScreen()));
    await tester.pumpAndSettle();

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
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([createHelpLink(url: '#create_ticket', text: 'Report a Problem')]));

    var mockLauncher = _MockUrlLauncherPlatform();
    UrlLauncherPlatform.instance = mockLauncher;

    await tester.pumpWidget(TestApp(HelpScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Report a Problem'));
    await tester.pumpAndSettle();

    expect(find.byType(ErrorReportDialog), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('tapping request feature launches email intent', (tester) async {
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([createHelpLink(id: 'submit_feature_idea', text: 'Request a Feature')]));

    final user = User((b) => b
      ..id = '123'
      ..primaryEmail = '123@321.com'
      ..effectiveLocale = 'en-jp');
    final login = Login((b) => b
      ..user = user.toBuilder()
      ..domain = 'dough main');

    String emailBody = '' +
        '${l10n.featureRequestHeader}\r\n' +
        '${l10n.helpUserId} ${user.id}\r\n' +
        '${l10n.helpEmail} ${user.primaryEmail}\r\n' +
        '${l10n.helpDomain} ${login.domain}\r\n' +
        '${l10n.versionNumber}: Canvas v1.0.0 (3)\r\n' +
        '${l10n.helpLocale} ${user.effectiveLocale}\r\n' +
        '----------------------------------------------\r\n';

    await tester.pumpWidget(TestApp(
      HelpScreen(),
      platformConfig: PlatformConfig(initLoggedInUser: login),
    ));
    await tester.pumpAndSettle();

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

    await tester.tap(find.text('Request a Feature'));
    await tester.pumpAndSettle();

    await completer.future; // Wait for the completer to finish the test
  });

  testWidgetsWithAccessibilityChecks('tapping legal shows legal screen', (tester) async {
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([]));

    await tester.pumpWidget(TestApp(HelpScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text(l10n.helpLegalLabel));
    await tester.pumpAndSettle();

    expect(find.byType(LegalScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('tapping telephone link launches correct intent', (tester) async {
    var telUri = 'tel:+123';
    var text = 'Telephone';

    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([createHelpLink(url: telUri, text: text)]));

    // Get the platform channels going
    await setupPlatformChannels();

    final completer = Completer();
    await MethodChannel('intent').setMockMethodCallHandler((MethodCall call) async {
      expect(call.method, 'startActivity');
      expect(call.arguments['action'], 'android.intent.action.DIAL');
      expect(call.arguments['data'], Uri.parse(telUri).toString());

      completer.complete(); // Finish the completer so the test can finish
      return null;
    });

    await tester.pumpWidget(TestApp(HelpScreen(), highContrast: true));
    await tester.pumpAndSettle();

    await tester.tap(find.text(text));
    await tester.pumpAndSettle();

    await completer.future;
  });

  testWidgetsWithAccessibilityChecks('tapping canvas chat support launches mobile browser intent', (tester) async {
    var url = 'https://cases.canvaslms.com/liveagentchat';
    var text = 'Chat';

    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([createHelpLink(url: url, text: text)]));

    var mockLauncher = _MockUrlLauncherPlatform();
    UrlLauncherPlatform.instance = mockLauncher;

    await tester.pumpWidget(TestApp(HelpScreen(), highContrast: true));
    await tester.pumpAndSettle();

    await tester.tap(find.text(text));
    await tester.pumpAndSettle();

    verify(
      mockLauncher.launch(
        url,
        useSafariVC: anyNamed('useSafariVC'),
        useWebView: anyNamed('useWebView'),
        enableJavaScript: anyNamed('enableJavaScript'),
        enableDomStorage: anyNamed('enableDomStorage'),
        universalLinksOnly: anyNamed('universalLinksOnly'),
        headers: anyNamed('headers'),
      ),
    ).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping mailto link launches correct intent', (tester) async {
    var mailto = 'mailto:pandas@instructure.com';
    var text = 'Mailto';

    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([createHelpLink(url: mailto, text: text)]));

    // Get the platform channels going
    await setupPlatformChannels();

    final completer = Completer();
    await MethodChannel('intent').setMockMethodCallHandler((MethodCall call) async {
      expect(call.method, 'startActivity');
      expect(call.arguments['action'], 'android.intent.action.SENDTO');
      expect(call.arguments['data'], Uri.parse(mailto).toString());

      completer.complete(); // Finish the completer so the test can finish
      return null;
    });

    await tester.pumpWidget(TestApp(HelpScreen(), highContrast: true));
    await tester.pumpAndSettle();

    await tester.tap(find.text(text));
    await tester.pumpAndSettle();

    await completer.future;
  });

  testWidgetsWithAccessibilityChecks('tapping an unhandled link launches', (tester) async {
    var url = 'https://www.instructure.com';
    var text = 'Some link';
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([createHelpLink(url: url, text: text)]));

    var mockLauncher = _MockUrlLauncherPlatform();
    UrlLauncherPlatform.instance = mockLauncher;

    await tester.pumpWidget(TestApp(HelpScreen(), highContrast: true));
    await tester.pumpAndSettle();

    await tester.tap(find.text(text));
    await tester.pumpAndSettle();

    verify(
      mockLauncher.launch(
        url,
        useSafariVC: anyNamed('useSafariVC'),
        useWebView: anyNamed('useWebView'),
        enableJavaScript: anyNamed('enableJavaScript'),
        enableDomStorage: anyNamed('enableDomStorage'),
        universalLinksOnly: anyNamed('universalLinksOnly'),
        headers: anyNamed('headers'),
      ),
    ).called(1);
  });
}

HelpLinks createHelpLinks({bool customLinks = false, bool defaultLinks = false}) => HelpLinks((b) => b
  ..customHelpLinks = customLinks ? BuiltList.from(<HelpLink>[createHelpLink()]).toBuilder() : []
  ..defaultHelpLinks = defaultLinks ? BuiltList.from(<HelpLink>[createHelpLink()]).toBuilder() : []);

HelpLink createHelpLink({String id, String text, String url}) => HelpLink((b) => b
  ..id = id ?? ''
  ..type = ''
  ..availableTo = BuiltList.of(<AvailableTo>[]).toBuilder()
  ..url = url ?? 'https://www.instructure.com'
  ..text = text ?? 'text'
  ..subtext = 'subtext');

class _MockUrlLauncherPlatform extends Mock with MockPlatformInterfaceMixin implements UrlLauncherPlatform {}

class _MockAndroidIntentVeneer extends Mock implements AndroidIntentVeneer {}

class _MockHelpScreenInteractor extends Mock implements HelpScreenInteractor {}
