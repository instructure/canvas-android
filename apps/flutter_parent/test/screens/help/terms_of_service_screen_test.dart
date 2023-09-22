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

import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/terms_of_service.dart';
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/screens/help/terms_of_use_screen.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  AppLocalizations l10n = AppLocalizations();

  final accountApi = MockAccountsApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<AccountsApi>(() => accountApi);
  });

  setUpAll(() {
    reset(accountApi);
  });

  testWidgetsWithAccessibilityChecks('Displays title and loading indicator', (tester) async {
    Completer<TermsOfService> completer = Completer();
    when(accountApi.getTermsOfService()).thenAnswer((_) => completer.future);

    await tester.pumpWidget(TestApp(TermsOfUseScreen()));
    await tester.pump();

    expect(find.text(l10n.termsOfUse), findsOneWidget);
    expect(find.byType(LoadingIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays error state and reloads', (tester) async {
    when(accountApi.getTermsOfService()).thenAnswer((_) => Future.error(''));

    await tester.pumpWidget(TestApp(TermsOfUseScreen()));
    await tester.pumpAndSettle();

    expect(find.byType(ErrorPandaWidget), findsOneWidget);
    expect(find.text(l10n.errorLoadingTermsOfUse), findsOneWidget);

    Completer<TermsOfService> completer = Completer();
    when(accountApi.getTermsOfService()).thenAnswer((_) => completer.future);

    await tester.tap(find.text(l10n.retry));
    await tester.pump();

    expect(find.byType(ErrorPandaWidget), findsNothing);
    expect(find.byType(LoadingIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays WebView', (tester) async {
    when(accountApi.getTermsOfService()).thenAnswer((_) async => TermsOfService((b) => b
      ..id = '123'
      ..passive = false
      ..accountId = '1'
      ..content = 'Fake Terms of Service'));

    await tester.pumpWidget(TestApp(
      TermsOfUseScreen(),
      platformConfig: PlatformConfig(initWebview: true),
    ));
    await tester.pumpAndSettle();

    expect(find.byType(WebView), findsOneWidget);
  });
}