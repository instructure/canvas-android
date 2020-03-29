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
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:webview_flutter/webview_flutter.dart';

enum LoginFlow {
  normal,
  canvas,
  siteAdmin,
  skipMobileVerify,
}

class WebLoginScreen extends StatelessWidget {
  WebLoginScreen(
    this.domain, {
    this.user,
    this.pass,
    this.authenticationProvider,
    this.loginFlow = LoginFlow.normal,
    Key key,
  }) : super(key: key);

  final String user;
  final String pass;
  final String domain;
  final String authenticationProvider;
  final LoginFlow loginFlow;

  final String successUrl = '/login/oauth2/auth?code=';
  final String errorUrl = '/login/oauth2/auth?error=access_denied';

  get _interactor => locator<WebLoginInteractor>();
  final Completer<WebViewController> _controllerCompleter = Completer<WebViewController>();

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        appBar: AppBar(
          title: Text(domain),
          bottom: ParentTheme.of(context).appBarDivider(shadowInLightMode: false),
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
          return LoadingIndicator();
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
      onWebViewCreated: (controller) async {
        CookieManager().clearCookies();
        controller.clearCache();

        var authUrl = _buildAuthUrl(verifyResult);

        if (loginFlow == LoginFlow.siteAdmin) {
          await controller.setAcceptThirdPartyCookies(true);
          if (domain.contains('.instructure.com')) {
            String cookie = 'canvas_sa_delegated=1;domain=.instructure.com;path=/;';
            await controller.setCookie(domain, cookie);
            await controller.setCookie('.instructure.com', cookie);
          } else {
            await controller.setCookie(domain, 'canvas_sa_delegated=1');
          }
        }

        controller.loadUrl(authUrl);
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
        locator<Analytics>().logEvent(
          AnalyticsEventConstants.LOGIN_SUCCESS,
          extras: {AnalyticsParamConstants.DOMAIN_PARAM: result.baseUrl},
        );
        locator<QuickNav>().pushRouteAndClearStack(context, PandaRouter.rootSplash());
      });
      return NavigationDecision.prevent;
    } else if (request.url.contains(errorUrl)) {
      locator<Analytics>().logEvent(
        AnalyticsEventConstants.LOGIN_FAILURE,
        extras: {AnalyticsParamConstants.DOMAIN_PARAM: result.baseUrl},
      );
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
    if (baseUrl.endsWith('/')) {
      baseUrl = baseUrl.substring(0, baseUrl.length - 1);
    }
    if (!baseUrl.startsWith('http://') && !baseUrl.startsWith('https://')) {
      baseUrl = 'https://${baseUrl}';
    }

    var purpose = Uri.encodeQueryComponent('canvasParent'); // PackageInfo.fromPlatform().then((info) => info.appName);
    var clientId = verifyResult != null ? Uri.encodeQueryComponent(verifyResult?.clientId) : '';
    var redirect = Uri.encodeQueryComponent('https://canvas.instructure.com/login/oauth2/auth');

    // TODO: Support skipping mobile verify better
    // forceAuthRedirect || mCanvasLogin == MOBILE_VERIFY_FLOW || domain.contains(".test.")
    if (domain.contains(".test.") || loginFlow == LoginFlow.skipMobileVerify) {
      // Skip mobile verify
      redirect = Uri.encodeQueryComponent("urn:ietf:wg:oauth:2.0:oob");
    }

    var result =
        '$baseUrl/login/oauth2/auth?client_id=$clientId&response_type=code&mobile=1&purpose=$purpose&redirect_uri=$redirect';

    //If an authentication provider is supplied we need to pass that along. This should only be appended if one exists.
    if (authenticationProvider != null && authenticationProvider.length > 0) {
      result = '$result&authentication_provider=${Uri.encodeQueryComponent(authenticationProvider)}';
    }

    if (loginFlow == LoginFlow.canvas) result += '&canvas_login=1';

    return result;
  }

  _showHelpDialog(BuildContext context, AsyncSnapshot<MobileVerifyResult> snapshot) => showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text(L10n(context).unexpectedError),
          content: Text(_getErrorMessage(context, snapshot)),
          actions: <Widget>[
            FlatButton(
              child: Text(L10n(context).ok),
              onPressed: () => Navigator.of(context).pop(),
            ),
          ],
        );
      });

  String _getErrorMessage(BuildContext context, AsyncSnapshot<MobileVerifyResult> snapshot) {
    final localizations = L10n(context);

    // No data means the request failed for some other reason that we don't know
    if (!snapshot.hasData) {
      debugPrint('Failed to do mobile verify with error: ${snapshot.error}');
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
