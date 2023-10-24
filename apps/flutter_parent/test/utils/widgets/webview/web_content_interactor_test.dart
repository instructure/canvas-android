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
import 'package:flutter/services.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/authenticated_url.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/network/api/oauth_api.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../../platform_config.dart';
import '../../test_app.dart';
import '../../test_helpers/mock_helpers.dart';
import '../../test_helpers/mock_helpers.mocks.dart';

void main() {
  final oauthApi = MockOAuthApi();
  final domain = 'https://www.instructure.com';
  final login = Login((b) => b.domain = domain);

  setupTestLocator((locator) {
    locator.registerLazySingleton<OAuthApi>(() => oauthApi);
  });

  // Reset the interactions for the shared mocks
  setUp(() async {
    reset(oauthApi);
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));
  });

  group('getAuthUrl', () {
    test('failure returns target_url', () async {
      final target = '$domain/target_url';
      when(oauthApi.getAuthenticatedUrl(target))
          .thenAnswer((_) async => Future<AuthenticatedUrl?>.error('Failed to authenticate url').catchError((_) { return Future.value(null);}));
      final actual = await WebContentInteractor().getAuthUrl(target);

      expect(actual, target);
      verify(oauthApi.getAuthenticatedUrl(target)).called(1);
    });

    test('returns session_url if target is in domain', () async {
      final target = '$domain/target_url';
      final expected = 'session_url';
      when(oauthApi.getAuthenticatedUrl(target))
          .thenAnswer((_) async => AuthenticatedUrl((b) => {
            b..sessionUrl = expected,
            b..requiresTermsAcceptance = false
          }));
      final actual = await WebContentInteractor().getAuthUrl(target);

      expect(actual, expected);
      verify(oauthApi.getAuthenticatedUrl(target)).called(1);
    });

    test('returns target if it is not in the domain', () async {
      final target = 'https://www.pandas.com';

      final actual = await WebContentInteractor().getAuthUrl(target);

      expect(actual, target);
    });
  });

  test('ltiToolPressedChannel has a name that matches in html_wrapper', () async {
    String fileText = await rootBundle.loadString('assets/html/html_wrapper.html');
    JavascriptChannel channel = WebContentInteractor().ltiToolPressedChannel((_) {});

    expect(fileText, contains('${channel.name}.postMessage'));
  });

  group('authContent', () {
    test('returns empty string if content is empty or null', () async {
      expect(await WebContentInteractor().authContent('', ''), '');
      expect(await WebContentInteractor().authContent(null, null), null);
    });

    test('returns content when no iframes are present', () async {
      final content = '<html><p>This is some content to display<br>It does not need to be authenticated</p></html>';
      final actual = await WebContentInteractor().authContent(content, null);

      expect(actual, content);
    });

    test('returns authenticated content when iframe has external_tools', () async {
      final target = 'external_tools';
      final authenticated = 'auth_tool';
      final buttonText = AppLocalizations().launchExternalTool;
      final content = _makeIframe(src: target);
      final expected = _makeIframe(src: authenticated, target: target, ltiButtonText: buttonText);

      when(oauthApi.getAuthenticatedUrl(target)).thenAnswer((_) async =>
          AuthenticatedUrl((b) => {
                b..sessionUrl = authenticated,
                b..requiresTermsAcceptance = false
              }));

      final actual = await WebContentInteractor().authContent(content, buttonText);

      expect(actual, expected);
      verify(oauthApi.getAuthenticatedUrl(target)).called(1);
    });

    test('returns authenticated content when iframe has cnvs_content id', () async {
      final id = 'cnvs_content';
      final target = 'target';
      final authenticated = 'auth_tool';
      final content = _makeIframe(id: id, src: target);
      final expected = _makeIframe(id: id, src: authenticated);

      when(oauthApi.getAuthenticatedUrl(target)).thenAnswer((_) async =>
          AuthenticatedUrl((b) => {
                b..sessionUrl = authenticated,
                b..requiresTermsAcceptance = false
              }));

      final actual = await WebContentInteractor().authContent(content, null);

      expect(actual, expected);
      verify(oauthApi.getAuthenticatedUrl(target)).called(1);
    });

    test('returns authenticated content with multiple iframes', () async {
      final id = 'cnvs_content';
      final target = 'external_tools';
      final authenticated = 'auth_tool';
      final buttonText = AppLocalizations().launchExternalTool;
      final content = _makeIframe(src: target) + _makeIframe(id: id, src: target);
      final expected = _makeIframe(src: authenticated, target: target, ltiButtonText: buttonText) +
          _makeIframe(id: id, src: authenticated, target: target);

      when(oauthApi.getAuthenticatedUrl(target)).thenAnswer((_) async =>
          AuthenticatedUrl((b) => {
                b..sessionUrl = authenticated,
                b..requiresTermsAcceptance = false
              }));

      final actual = await WebContentInteractor().authContent(content, buttonText);

      expect(actual, expected);
      verify(oauthApi.getAuthenticatedUrl(target)).called(2);
    });
  });
}

String _makeIframe({String? id, String? src, String? target, String? ltiButtonText}) {
  String ltiButton = ltiButtonText != null
      ? '</br><p><div class="lti_button" onClick="onLtiToolButtonPressed(\'$target\')">$ltiButtonText</div></p>'
      : '';
  return '<iframe id="$id" src="$src"></iframe>$ltiButton';
}
