// Copyright (C) 2019 - present Instructure, Inc.
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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_screen.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:webview_flutter/webview_flutter.dart';

class WebLoginScreen extends StatelessWidget {
  WebLoginScreen(this.domain, {this.user, this.pass, this.authenticationProvider, Key key}) : super(key: key);

  final String user;
  final String pass;
  final String domain;
  final String authenticationProvider;

  final String successUrl = "/login/oauth2/auth?code=";
  final String errorUrl = "/login/oauth2/auth?error=access_denied";

  final _interactor = locator<WebLoginInteractor>();
  final Completer<WebViewController> _controllerCompleter = Completer<WebViewController>();

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        backgroundColor: Theme.of(context).scaffoldBackgroundColor,
        appBar: AppBar(
          textTheme: Theme.of(context).textTheme,
          iconTheme: Theme.of(context).iconTheme,
          title: Text(domain),
          elevation: 0,
          backgroundColor: Theme.of(context).scaffoldBackgroundColor,
        ),
        body: _webLoginBody(),
      ),
    );
  }

  Widget _webLoginBody() {
    return FutureBuilder(
      future: _interactor.mobileVerify(domain),
      builder: (context, AsyncSnapshot<MobileVerifyResult> snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Center(child: CircularProgressIndicator());
        } else {
          if (snapshot.hasError || (snapshot.hasData && snapshot.data.result != VerifyResultEnum.success)) {
            WidgetsBinding.instance.addPostFrameCallback((_) {
              _showHelpDialog(context, snapshot);
            });
          }

          // Load the WebView regardless of errors, so the user can see what they've done
          return _webView(context, snapshot);
        }
      },
    );
  }

  Widget _webView(BuildContext context, AsyncSnapshot<MobileVerifyResult> snapshot) {
    final verifyResult = snapshot.data;

    return WebView(
      navigationDelegate: (request) => _navigate(context, request, verifyResult),
      javascriptMode: JavascriptMode.unrestricted,
      onPageFinished: (url) {
        if (user != null && pass != null) {
          // SnickerDoodle login
          _controllerCompleter.future.then((controller) {
            controller.evaluateJavascript("""javascript: {
                      document.getElementsByName('pseudonym_session[unique_id]')[0].value = '${user}';
                      document.getElementsByName('pseudonym_session[password]')[0].value = '${pass}';
                      document.getElementsByClassName('Button')[0].click();
                };""");
          });
        }
      },
      onWebViewCreated: (controller) {
        CookieManager().clearCookies();
        controller.clearCache();

        controller.loadUrl(_buildAuthUrl(verifyResult));
        if (!_controllerCompleter.isCompleted) _controllerCompleter.complete(controller);
      },
    );
  }

  NavigationDecision _navigate(BuildContext context, NavigationRequest request, MobileVerifyResult result) {
    // TODO: Handle parent specific urls too
    /*
     *   private const val PARENT_SUCCESS_URL = "/oauthSuccess"
     *   private const val PARENT_CANCEL_URL = "/oauth2/deny"
     *   private const val PARENT_ERROR_URL = "/oauthFailure"
     *   private const val PARENT_TOKEN_URL = "/canvas/tokenReady"
     */

    if (request.url.contains(successUrl)) {
      var url = request.url;
      String oAuthRequest = url.substring(url.indexOf(successUrl) + successUrl.length);
      locator<WebLoginInteractor>().performLogin(result, oAuthRequest).then((_) {
        locator<QuickNav>().pushAndRemoveAll(context, DashboardScreen());
      });
      return NavigationDecision.prevent;
    } else if (request.url.contains(errorUrl)) {
      return NavigationDecision.prevent;
    } else {
      return NavigationDecision.navigate;
    }
  }

  String _buildAuthUrl(MobileVerifyResult verifyResult) {
    String baseUrl = verifyResult?.baseUrl;
    if ((baseUrl?.length ?? 0) == 0) {
      baseUrl = domain;
    }
    if (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length - 1);
    }
    if (!baseUrl.startsWith('http://') && !baseUrl.startsWith('https://')) {
      baseUrl = 'https://${baseUrl}';
    }

    var purpose = Uri.encodeQueryComponent("canvasParent"); // PackageInfo.fromPlatform().then((info) => info.appName);
    var clientId = verifyResult != null ? Uri.encodeQueryComponent(verifyResult?.clientId) : '';
    var redirect = Uri.encodeQueryComponent("https://canvas.instructure.com/login/oauth2/auth");

    // TODO: Support skipping mobile verify
//    if (forceAuthRedirect || || mCanvasLogin == MOBILE_VERIFY_FLOW || (domain.contains(".test."))) {
//      //Skip mobile verify
//      redirect = Uri.encodeQueryComponent("urn:ietf:wg:oauth:2.0:oob");
//    }

    var result =
        '$baseUrl/login/oauth2/auth?client_id=$clientId&response_type=code&mobile=1&purpose=$purpose&redirect_uri=$redirect';

    //If an authentication provider is supplied we need to pass that along. This should only be appended if one exists.
    if (authenticationProvider != null && authenticationProvider.length > 0) {
      result = '$result&authentication_provider=${Uri.encodeQueryComponent(authenticationProvider)}';
    }

    return result;
  }

  _showHelpDialog(BuildContext context, AsyncSnapshot<MobileVerifyResult> snapshot) => showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text(AppLocalizations.of(context).unexpectedError),
          content: Text(_getErrorMessage(context, snapshot)),
          actions: <Widget>[
            FlatButton(
              child: Text(AppLocalizations.of(context).ok),
              onPressed: () => Navigator.of(context).pop(),
            ),
          ],
        );
      });

  String _getErrorMessage(BuildContext context, AsyncSnapshot<MobileVerifyResult> snapshot) {
    final localizations = AppLocalizations.of(context);

    // No data means the request failed for some other reason that we don't know
    if (!snapshot.hasData) {
      debugPrint("Failed to do mobile verify with error: ${snapshot.error}");
      return localizations.domainVerificationErrorUnknown;
    }

    switch (snapshot.data.result) {
      case VerifyResultEnum.generalError:
        return localizations.domainVerificationErrorGeneral;
      case VerifyResultEnum.domainNotAuthorized:
        return localizations.domainVerificationErrorDomain;
      case VerifyResultEnum.unknownUserAgent:
        return localizations.domainVerificationErrorUserAgent;
      default:
        return localizations.domainVerificationErrorUnknown;
    }
  }
}
