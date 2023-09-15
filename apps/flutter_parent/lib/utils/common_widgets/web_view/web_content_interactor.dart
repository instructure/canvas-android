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
import 'package:flutter_parent/network/api/oauth_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:webview_flutter/webview_flutter.dart';

class WebContentInteractor {
  /// See [assets/html/html_wrapper.html] for usage of these strings
  final _onLtiToolButtonPressedChannel = 'OnLtiToolButtonPressed';
  final _onLtiToolButtonPressedFunction = 'onLtiToolButtonPressed';

  OAuthApi get _api => locator.get<OAuthApi>();

  Future<String> _authUrl(String targetUrl) async {
    return (await _api.getAuthenticatedUrl(targetUrl))?.sessionUrl ?? targetUrl;
  }

  Future<String> getAuthUrl(String targetUrl) async {
    if (targetUrl.contains(ApiPrefs.getDomain()!)) {
      return _authUrl(targetUrl);
    } else {
      return targetUrl;
    }
  }

  Future<String?>? authContent(String? content, String? externalToolButtonText) async {
    if (content == null || content.isEmpty) return Future.value(content);

    String authContent = content;
    final iframeMatcher = RegExp('<iframe(.|\\n)*?iframe>');
    final matches = iframeMatcher.allMatches(content);

    for (RegExpMatch element in matches) {
      final iframe = element.group(0);
      if (iframe != null) {
        if (RegExp('id=\"cnvs_content\"').hasMatch(iframe)) {
          authContent = await _handleCanvasContent(iframe, authContent);
        } else if (RegExp('external_tool').hasMatch(iframe)) {
          authContent = await _handleLti(iframe, authContent, externalToolButtonText);
        }
      }
    }

    return authContent;
  }

  /// This is a general workaround for institutions that want to include any kind of content in an iframe
  Future<String> _handleCanvasContent(String iframe, String content) async {
    final matcher = RegExp('src=\"([^\"]+)\"').firstMatch(iframe);

    if (matcher != null) {
      final sourceUrl = matcher.group(1);
      if (sourceUrl != null) {
        final authUrl = await _authUrl(sourceUrl);
        final newIframe = iframe.replaceFirst(sourceUrl, authUrl);
        content = content.replaceFirst(iframe, newIframe);
      }
    }

    return content;
  }

  Future<String> _handleLti(String iframe, String content, externalToolButtonText) async {
    final matcher = RegExp('src=\"([^\"]+)\"').firstMatch(iframe);
    if (matcher != null) {
      final sourceUrl = matcher.group(1);
      // Make sure this REALLY is an LTI src, this check might need to be upgraded in the future...
      if (sourceUrl != null && sourceUrl.contains('external_tools')) {
        final authUrl = await _authUrl(sourceUrl);
        final newIframe = iframe.replaceFirst(sourceUrl, authUrl);

        final button =
            '</br><p><div class=\"lti_button\" onClick=\"$_onLtiToolButtonPressedFunction(\'$sourceUrl\')\">$externalToolButtonText</div></p>';

        content = content.replaceFirst(iframe, newIframe + button);
      }
    }

    return content;
  }

  JavascriptChannel ltiToolPressedChannel(JavascriptMessageHandler handler) {
    return JavascriptChannel(name: _onLtiToolButtonPressedChannel, onMessageReceived: handler);
  }
}
