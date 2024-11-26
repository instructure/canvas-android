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

import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/school_domain.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/arrow_aware_focus_scope.dart';
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

class WebLoginScreen extends StatefulWidget {
  WebLoginScreen(this.domain, {
    this.accountName,
    this.user,
    this.pass,
    this.authenticationProvider,
    this.loginFlow = LoginFlow.normal,
    super.key,
  });

  final String? user;
  final String? accountName;
  final String? pass;
  final String domain;
  final String? authenticationProvider;
  final LoginFlow loginFlow;

  static const String PROTOCOL_SKIP_VERIFY_KEY = 'skip-protocol';
  static const String ID_SKIP_VERIFY_KEY = 'skip-id';
  static const String SECRET_SKIP_VERIFY_KEY = 'skip-secret';

  @override
  _WebLoginScreenState createState() => _WebLoginScreenState();
}

class _WebLoginScreenState extends State<WebLoginScreen> {
  static const String SUCCESS_URL = '/login/oauth2/auth?code=';
  static const String ERROR_URL = '/login/oauth2/auth?error=access_denied';

  final Completer<WebViewController> _controllerCompleter = Completer<WebViewController>();

  WebLoginInteractor get _interactor => locator<WebLoginInteractor>();

  Future<MobileVerifyResult?>? _verifyFuture;
  WebViewController? _controller;
  late String _authUrl;
  late String _domain;
  bool _showLoading = false;
  bool _isMobileVerifyError = false;
  bool loadStarted = false;

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        appBar: AppBar(
          title: Text(widget.domain),
          bottom: ParentTheme.of(context)?.appBarDivider(shadowInLightMode: false),
        ),
        body: _loginBody(),
        // MBL-14271: When in landscape mode, set this to false in order to avoid the situation
        // where the soft keyboard keeps flickering on and off.
        resizeToAvoidBottomInset: MediaQuery.of(context).orientation == Orientation.portrait,
      ),
    );
  }

  Widget _loginBody() {
    if (_verifyFuture == null) {
      _verifyFuture = (widget.loginFlow == LoginFlow.skipMobileVerify)
          ? Future.delayed(Duration.zero, () => _SkipVerifyDialog.asDialog(context, widget.domain)).then((result) {
              // Use the result if we have it, otherwise continue on with mobile verify
              if (result != null) { return result; }
              return _interactor.mobileVerify(widget.domain);
          })
          : _interactor.mobileVerify(widget.domain);
    }

    return FutureBuilder(
      future: _verifyFuture,
      builder: (context, AsyncSnapshot<MobileVerifyResult?> snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return LoadingIndicator();
        } else {
          _isMobileVerifyError = snapshot.hasError || (snapshot.hasData && snapshot.data!.result != VerifyResultEnum.success);
          if (_isMobileVerifyError) {
            WidgetsBinding.instance.addPostFrameCallback((_) {
              _showErrorDialog(context, snapshot);
            });
          }

          // Load the WebView regardless of errors, so the user can see what they've done
          return _webView(context, snapshot);
        }
      },
    );
  }

  Widget _webView(BuildContext context, AsyncSnapshot<MobileVerifyResult?> snapshot) {
    final verifyResult = snapshot.data;

    return Stack(
      children: [
        WebView(
            navigationDelegate: (request) =>
                _navigate(context, request, verifyResult),
            javascriptMode: JavascriptMode.unrestricted,
            userAgent: ApiPrefs.getUserAgent(),
            onPageFinished: (url) => _pageFinished(url, verifyResult),
            onPageStarted: (url) => _pageStarted(url),
            onWebViewCreated: (controller) =>
                _webViewCreated(controller, verifyResult),
        ),
        if (_showLoading) ...[
          Container(color: Theme.of(context).scaffoldBackgroundColor),
          LoadingIndicator(),
        ],
      ],
    );
  }

  void _webViewCreated(WebViewController controller, MobileVerifyResult? verifyResult) async {
    controller.clearCache();
    _controller = controller;

    // WebView's created, time to load
    await _buildAuthUrl(verifyResult);
    _loadAuthUrl();

    if (!_controllerCompleter.isCompleted) _controllerCompleter.complete(controller);
  }

  void _pageFinished(String url, MobileVerifyResult? verifyResult) {
    _controllerCompleter.future.then((controller) async {
      if (widget.user != null && widget.pass != null) {
        // SnickerDoodle login
        await controller.evaluateJavascript("""javascript: {
                      document.getElementsByName('pseudonym_session[unique_id]')[0].value = '${widget.user}';
                      document.getElementsByName('pseudonym_session[password]')[0].value = '${widget.pass}';
                      document.getElementsByClassName('Button')[0].click();
                };""");
      }

      // If the institution does not support skipping the authentication screen this will catch that error and force the
      // rebuilding of the authentication url with the authorization screen flow. Example: canvas.sfu.ca
      // NOTE: the example institution doesn't work when mobile verify is called with canvasParent in the user-agent
      final htmlError = await controller.evaluateJavascript("""
            (function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();
          """);
      if (htmlError != null && htmlError.contains("redirect_uri does not match client settings")) {
        await _buildAuthUrl(verifyResult, forceAuthRedirect: true);
        controller.loadUrl("about:blank");
        _loadAuthUrl();
      }
      if (loadStarted) {
        _hideLoadingDialog();
      }
    });
  }

  void _pageStarted(String url) {
    loadStarted = true;
    _showLoadingState();
  }

  void _showLoadingState() {
    if (!_isMobileVerifyError) {
      setState(() => _showLoading = true);
    }
  }

  void _hideLoadingDialog() {
    if (!_isMobileVerifyError) {
      setState(() => _showLoading = false);
    }
  }

  NavigationDecision _navigate(BuildContext context, NavigationRequest request, MobileVerifyResult? result) {
    if (request.url.contains(SUCCESS_URL)) {
      // Success! Try to get tokens now
      var url = request.url;
      String oAuthRequest = url.substring(url.indexOf(SUCCESS_URL) + SUCCESS_URL.length);
      locator<WebLoginInteractor>().performLogin(result, oAuthRequest).then((_) {
        locator<Analytics>().logEvent(
          AnalyticsEventConstants.LOGIN_SUCCESS,
          extras: {AnalyticsParamConstants.DOMAIN_PARAM: result?.baseUrl},
        );
        final lastAccount = new SchoolDomain((builder) =>
        builder
          ..authenticationProvider = widget.authenticationProvider
          ..domain = widget.domain
          ..name = widget.accountName);
        ApiPrefs.setLastAccount(lastAccount, widget.loginFlow);
        locator<QuickNav>().pushRouteAndClearStack(
            context, PandaRouter.rootSplash());
      }).catchError((_) {
        locator<Analytics>().logEvent(
          AnalyticsEventConstants.LOGIN_FAILURE,
          extras: {AnalyticsParamConstants.DOMAIN_PARAM: result?.baseUrl},
        );
        // Load the original auth url so the user can try again
        _loadAuthUrl();
      });
      return NavigationDecision.prevent;
    } else if (request.url.contains(ERROR_URL)) {
      // Load the original auth url so the user can try again
      _loadAuthUrl();
      return NavigationDecision.prevent;
    } else {
      return NavigationDecision.navigate;
    }
  }

  /// Load the authenticated url with any necessary cookies
  void _loadAuthUrl() async {
    _showLoadingState();
    final cookieManager = CookieManager();
    cookieManager.clearCookies();

    if (widget.loginFlow == LoginFlow.siteAdmin) {
      if (_domain.contains('.instructure.com')) {
        cookieManager.setCookie(WebViewCookie(name: 'canvas_sa_delegated', value: '1', domain: _domain));
        cookieManager.setCookie(WebViewCookie(name: 'canvas_sa_delegated', value: '1', domain: '.instructure.com'));
      } else {
        cookieManager.setCookie(WebViewCookie(name: 'canvas_sa_delegated', value: '1', domain: _domain));
      }
    }

    _controller?.loadUrl(_authUrl);
  }

  /// Sets an authenticated login url as well as the base url of the institution
  Future<void> _buildAuthUrl(
    MobileVerifyResult? verifyResult, {
    bool forceAuthRedirect = false,
  }) async {
    // Sanitize the url
    String? baseUrl = verifyResult?.baseUrl;
    if ((baseUrl?.length ?? 0) == 0) {
      baseUrl = widget.domain;
    }
    if (baseUrl?.endsWith('/') == true) {
      baseUrl = baseUrl!.substring(0, baseUrl.length - 1);
    }
    final scheme = baseUrl == null ? null : Uri.parse(baseUrl).scheme;
    if (scheme == null || scheme.isEmpty) {
      baseUrl = 'https://${baseUrl}';
    }

    // Prepare login information
    var purpose = await DeviceInfoPlugin().androidInfo.then((info) => info.model.replaceAll(' ', '_'));
    var clientId = verifyResult != null ? Uri.encodeQueryComponent(verifyResult.clientId) : '';
    var redirect = Uri.encodeQueryComponent('https://canvas.instructure.com/login/oauth2/auth');

    if (forceAuthRedirect || widget.domain.contains(".test.") || widget.loginFlow == LoginFlow.skipMobileVerify) {
      // Skip mobile verify
      redirect = Uri.encodeQueryComponent("urn:ietf:wg:oauth:2.0:oob");
    }

    var result =
        '$baseUrl/login/oauth2/auth?client_id=$clientId&response_type=code&mobile=1&purpose=$purpose&redirect_uri=$redirect';

    // If an authentication provider is supplied we need to pass that along. This should only be appended if one exists.
    if (widget.authenticationProvider != null &&
        widget.authenticationProvider!.length > 0 &&
        widget.authenticationProvider!.toLowerCase() != 'null') {
      locator<Analytics>().logMessage('authentication_provider=${widget.authenticationProvider}');
      result = '$result&authentication_provider=${Uri.encodeQueryComponent(widget.authenticationProvider!)}';
    }

    if (widget.loginFlow == LoginFlow.canvas) result += '&canvas_login=1';

    // Set the variables to use when doing a load
    _authUrl = result;
    _domain = baseUrl ?? '';
  }

  /// Shows a simple alert dialog with an error message that correlates to the result code
  _showErrorDialog(BuildContext context, AsyncSnapshot<MobileVerifyResult?> snapshot) => showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text(L10n(context).unexpectedError),
          content: Text(_getErrorMessage(context, snapshot)),
          actions: <Widget>[
            TextButton(
              child: Text(L10n(context).ok),
              onPressed: () => Navigator.of(context).pop(),
            ),
          ],
        );
      });

  String _getErrorMessage(BuildContext context, AsyncSnapshot<MobileVerifyResult?> snapshot) {
    final localizations = L10n(context);

    // No data means the request failed for some other reason that we don't know
    if (!snapshot.hasData) {
      debugPrint('Failed to do mobile verify with error: ${snapshot.error}');
      return localizations.domainVerificationErrorUnknown;
    }

    switch (snapshot.data!.result) {
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

/// Private dialog to show when skipping mobile verify. Let's users provide their own protocol, and client id/secret.
class _SkipVerifyDialog extends StatefulWidget {
  final String domain;

  const _SkipVerifyDialog(this.domain, {super.key});

  @override
  __SkipVerifyDialogState createState() => __SkipVerifyDialogState();

  static Future<MobileVerifyResult?> asDialog(BuildContext context, String domain) {
    return showDialog<MobileVerifyResult>(context: context, builder: (_) => _SkipVerifyDialog(domain));
  }
}

class __SkipVerifyDialogState extends State<_SkipVerifyDialog> {
  final _formKey = GlobalKey<FormState>();

  FocusScopeNode _focusScopeNode = FocusScopeNode();

  String _protocol = 'https';
  String _clientId = '';
  String _clientSecret = '';

  bool _autoValidate = false;

  @override
  void dispose() {
    _focusScopeNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8.0)),
      title: Text(L10n(context).skipMobileVerifyTitle), // Non translated string
      content: _content(),
      actions: <Widget>[
        TextButton(
          child: Text(L10n(context).cancel.toUpperCase()),
          onPressed: () => Navigator.of(context).pop(null),
        ),
        TextButton(
          child: Text(L10n(context).ok.toUpperCase()),
          onPressed: () => _popWithResult(),
        ),
      ],
    );
  }

  void _popWithResult() {
    if (_formKey.currentState?.validate() == true) {
      Navigator.of(context).pop(MobileVerifyResult((b) => b
        ..clientId = _clientId
        ..clientSecret = _clientSecret
        ..baseUrl = '$_protocol://${widget.domain}'));
    } else {
      // Now that they've tried to submit, let the form start validating
      setState(() {
        _autoValidate = true;
      });
    }
  }

  Widget _content() {
    return SingleChildScrollView(
      child: ArrowAwareFocusScope(
        node: _focusScopeNode,
        child: Form(
          key: _formKey,
          autovalidateMode: _autoValidate ? AutovalidateMode.always : AutovalidateMode.disabled,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              TextFormField(
                key: Key(WebLoginScreen.PROTOCOL_SKIP_VERIFY_KEY),
                decoration: _decoration(L10n(context).skipMobileVerifyProtocol),
                initialValue: _protocol,
                onChanged: (text) => _protocol = text,
                validator: (text) => text?.isEmpty == true ? L10n(context).skipMobileVerifyProtocolMissing : null,
                textInputAction: TextInputAction.next,
                onFieldSubmitted: (_) => _focusScopeNode.nextFocus(),
              ),
              SizedBox(height: 16),
              TextFormField(
                key: Key(WebLoginScreen.ID_SKIP_VERIFY_KEY),
                decoration: _decoration(L10n(context).skipMobileVerifyClientId),
                onChanged: (text) => _clientId = text,
                validator: (text) => text?.isEmpty == true ? L10n(context).skipMobileVerifyClientIdMissing : null,
                textInputAction: TextInputAction.next,
                onFieldSubmitted: (_) => _focusScopeNode.nextFocus(),
              ),
              SizedBox(height: 16),
              TextFormField(
                key: Key(WebLoginScreen.SECRET_SKIP_VERIFY_KEY),
                decoration: _decoration(L10n(context).skipMobileVerifyClientSecret),
                onChanged: (text) => _clientSecret = text,
                validator: (text) => text?.isEmpty == true ? L10n(context).skipMobileVerifyClientSecretMissing : null,
                textInputAction: TextInputAction.done,
                onFieldSubmitted: (_) => _focusScopeNode.nextFocus(),
                onEditingComplete: _popWithResult,
              ),
            ],
          ),
        ),
      ),
    );
  }

  InputDecoration _decoration(String label) => InputDecoration(
        labelText: label,
        fillColor: ParentTheme.of(context)?.nearSurfaceColor,
        filled: true,
      );
}
