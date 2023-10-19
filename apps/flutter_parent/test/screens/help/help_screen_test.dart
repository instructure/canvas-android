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
import 'package:built_collection/built_collection.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/help_link.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/help/help_screen.dart';
import 'package:flutter_parent/screens/help/help_screen_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_dialog.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_parent/utils/veneers/android_intent_veneer.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final l10n = AppLocalizations();
  MockUrlLauncher launcher = MockUrlLauncher();
  MockAndroidIntentVeneer intentVeneer = MockAndroidIntentVeneer();
  MockHelpScreenInteractor interactor = MockHelpScreenInteractor();

  setupTestLocator((locator) {
    locator.registerSingleton<QuickNav>(QuickNav());
    locator.registerLazySingleton<AndroidIntentVeneer>(() => intentVeneer);
    locator.registerLazySingleton<HelpScreenInteractor>(() => interactor);
    locator.registerLazySingleton<UrlLauncher>(() => launcher);
  });

  setUp(() {
    reset(intentVeneer);
    reset(interactor);
    reset(launcher);
  });

  testWidgetsWithAccessibilityChecks('displays links', (tester) async {
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([_createHelpLink()]));

    await tester.pumpWidget(TestApp(HelpScreen()));
    await tester.pumpAndSettle();

    expect(find.text('text'), findsOneWidget);
    expect(find.text('subtext'), findsOneWidget);

    expect(find.text(l10n.helpShareLoveLabel), findsOneWidget);
    expect(find.text(l10n.helpShareLoveDescription), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('tapping search launches url', (tester) async {
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh'))).thenAnswer(
        (_) => Future.value([_createHelpLink(id: 'search_the_canvas_guides', text: 'Search the Canvas Guides')]));

    await tester.pumpWidget(TestApp(HelpScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Search the Canvas Guides'));
    await tester.pumpAndSettle();

    verify(
      launcher.launch(
        'https://community.canvaslms.com/community/answers/guides/mobile-guide/content?filterID=contentstatus%5Bpublished%5D~category%5Btable-of-contents%5D',
      ),
    ).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping share love launches url', (tester) async {
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([]));

    await tester.pumpWidget(TestApp(HelpScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text(l10n.helpShareLoveLabel));
    await tester.pumpAndSettle();

    verify(
      launcher.launchAppStore(),
    ).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping report problem shows error report dialog', (tester) async {
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([_createHelpLink(url: '#create_ticket', text: 'Report a Problem')]));

    await tester.pumpWidget(TestApp(HelpScreen()));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Report a Problem'));
    await tester.pumpAndSettle();

    expect(find.byType(ErrorReportDialog), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('tapping request feature launches email intent', (tester) async {
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([_createHelpLink(id: 'submit_feature_idea', text: 'Request a Feature')]));

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

    await tester.tap(find.text('Request a Feature'));
    await tester.pumpAndSettle();

    verify(intentVeneer.launchEmailWithBody(l10n.featureRequestSubject, emailBody)).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping telephone link launches correct intent', (tester) async {
    var telUri = 'tel:+123';
    var text = 'Telephone';

    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([_createHelpLink(url: telUri, text: text)]));

    await tester.pumpWidget(TestApp(HelpScreen(), highContrast: true));
    await tester.pumpAndSettle();

    await tester.tap(find.text(text));
    await tester.pumpAndSettle();

    verify(intentVeneer.launchPhone(telUri)).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping canvas chat support launches mobile browser intent', (tester) async {
    var url = 'https://cases.canvaslms.com/liveagentchat';
    var text = 'Chat';

    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([_createHelpLink(url: url, text: text)]));

    await tester.pumpWidget(TestApp(HelpScreen(), highContrast: true));
    await tester.pumpAndSettle();

    await tester.tap(find.text(text));
    await tester.pumpAndSettle();

    verify(launcher.launch(url)).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping mailto link launches correct intent', (tester) async {
    var mailto = 'mailto:pandas@instructure.com';
    var text = 'Mailto';

    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([_createHelpLink(url: mailto, text: text)]));

    await tester.pumpWidget(TestApp(HelpScreen(), highContrast: true));
    await tester.pumpAndSettle();

    await tester.tap(find.text(text));
    await tester.pumpAndSettle();

    verify(intentVeneer.launchEmail(mailto)).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping an unhandled link launches', (tester) async {
    var url = 'https://www.instructure.com';
    var text = 'Some link';
    when(interactor.getObserverCustomHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([_createHelpLink(url: url, text: text)]));

    await tester.pumpWidget(TestApp(HelpScreen(), highContrast: true));
    await tester.pumpAndSettle();

    await tester.tap(find.text(text));
    await tester.pumpAndSettle();

    verify(launcher.launch(url)).called(1);
  });
}

HelpLink _createHelpLink({String? id, String? text, String? url}) => HelpLink((b) => b
  ..id = id ?? ''
  ..type = ''
  ..availableTo = BuiltList.of(<AvailableTo>[]).toBuilder()
  ..url = url ?? 'https://www.instructure.com'
  ..text = text ?? 'text'
  ..subtext = 'subtext');
