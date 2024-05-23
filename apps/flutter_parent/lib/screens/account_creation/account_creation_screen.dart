/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:dio/dio.dart';
import 'package:email_validator/email_validator.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/terms_of_service.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/account_creation/account_creation_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_svg/svg.dart';

class AccountCreationScreen extends StatefulWidget {
  @visibleForTesting
  static final GlobalKey accountCreationTextSpanKey = GlobalKey();

  final QRPairingInfo pairingInfo;

  const AccountCreationScreen(this.pairingInfo, {super.key});

  @override
  _AccountCreationScreenState createState() => _AccountCreationScreenState();
}

class _AccountCreationScreenState extends State<AccountCreationScreen> {
  TextStyle _defaultSpanStyle = TextStyle(color: ParentColors.ash, fontSize: 14.0, fontWeight: FontWeight.normal);
  TextStyle _linkSpanStyle = TextStyle(color: ParentColors.parentApp, fontSize: 14.0, fontWeight: FontWeight.normal);

  Future<TermsOfService?>? _tosFuture;

  Future<TermsOfService?> _getToS() {
    return locator<AccountCreationInteractor>()
        .getToSForAccount(widget.pairingInfo.accountId, widget.pairingInfo.domain);
  }

  final FocusNode _nameFocus = FocusNode();
  final _nameController = TextEditingController();

  final FocusNode _emailFocus = FocusNode();
  final _emailController = TextEditingController();
  String? _emailErrorText = null;

  final FocusNode _passwordFocus = FocusNode();
  final _passwordController = TextEditingController();

  final _formKey = GlobalKey<FormState>();
  final _scaffoldKey = GlobalKey<ScaffoldState>();

  bool _isLoading = false;
  bool _obscurePassword = true;

  @override
  Widget build(BuildContext context) {
    if (_tosFuture == null) {
      _tosFuture = _getToS();
    }

    return DefaultParentTheme(
      builder: (context) => Scaffold(
          key: _scaffoldKey,
          body: SafeArea(
            child: ListView(
              padding: EdgeInsets.fromLTRB(16, 0, 16, 0),
              children: <Widget>[
                SizedBox(height: 64),
                SvgPicture.asset(
                  'assets/svg/canvas-parent-login-logo.svg',
                  semanticsLabel: L10n(context).canvasLogoLabel,
                ),
                SizedBox(height: 56),
                _accountCreationForm(),
                SizedBox(height: 16),
                _createAccountTOS(),
                SizedBox(height: 8),
                _createAccountButton(),
                SizedBox(height: 24),
                _alreadyHaveAnAccount(),
                SizedBox(height: 8),
              ],
            ),
          )),
    );
  }

  Widget _accountCreationForm() {
    return Form(
      key: _formKey,
      child: Column(
        children: <Widget>[
          _formFieldLabel(L10n(context).qrCreateAccountLabelName),
          _nameFormField(),
          _formFieldLabel(L10n(context).qrCreateAccountLabelEmail),
          _emailFormField(),
          _formFieldLabel(L10n(context).qrCreateAccountLabelPassword),
          _passwordFormField(),
        ],
      ),
    );
  }

  Widget _formFieldLabel(String text) {
    return Align(
        alignment: Alignment.centerLeft,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(0, 12, 0, 8),
          child: Text(text, style: TextStyle(color: ParentColors.licorice, fontSize: 16, fontWeight: FontWeight.w500)),
        ));
  }

  Widget _nameFormField() {
    return TextFormField(
      focusNode: _nameFocus,
      textInputAction: TextInputAction.next,
      autocorrect: false,
      controller: _nameController,
      decoration: InputDecoration(
          errorStyle: TextStyle(color: ParentColors.failure, fontSize: 14, fontWeight: FontWeight.w500),
          errorBorder: OutlineInputBorder(
            borderRadius: new BorderRadius.circular(4.0),
            borderSide: new BorderSide(color: ParentColors.failure),
          ),
          hintText: L10n(context).qrCreateAccountHintName,
          hintStyle: TextStyle(color: ParentColors.tiara, fontSize: 16, fontWeight: FontWeight.normal),
          border: OutlineInputBorder(
            borderRadius: new BorderRadius.circular(4.0),
            borderSide: new BorderSide(),
          )),
      onFieldSubmitted: (term) {
        _fieldFocusChange(_nameFocus, _emailFocus);
      },
      validator: (value) {
        if (value == null || value.isEmpty) {
          return L10n(context).qrCreateAccountNameError;
        } else {
          return null;
        }
      },
    );
  }

  Widget _emailFormField() {
    return TextFormField(
        focusNode: _emailFocus,
        keyboardType: TextInputType.emailAddress,
        textInputAction: TextInputAction.next,
        autocorrect: false,
        controller: _emailController,
        decoration: InputDecoration(
            errorText: _emailErrorText,
            errorStyle: TextStyle(color: ParentColors.failure, fontSize: 14, fontWeight: FontWeight.w500),
            errorBorder: OutlineInputBorder(
              borderRadius: new BorderRadius.circular(4.0),
              borderSide: new BorderSide(color: ParentColors.failure),
            ),
            hintText: L10n(context).qrCreateAccountHintEmail,
            hintStyle: TextStyle(color: ParentColors.tiara, fontSize: 16, fontWeight: FontWeight.normal),
            border: OutlineInputBorder(
              borderRadius: new BorderRadius.circular(4.0),
              borderSide: new BorderSide(),
            )),
        onFieldSubmitted: (term) {
          _fieldFocusChange(_emailFocus, _passwordFocus);
        },
        validator: (value) => _validateEmail(value));
  }

  String? _validateEmail(String? value, {bool apiError = false}) {
    if (apiError) {
      return L10n(context).qrCreateAccountInvalidEmailError;
    } else if (value == null || value.isEmpty) {
      return L10n(context).qrCreateAccountEmailError;
    } else if (!EmailValidator.validate(value)) {
      return L10n(context).qrCreateAccountInvalidEmailError;
    } else {
      _emailErrorText = null;
      return _emailErrorText;
    }
  }

  Widget _passwordFormField() {
    return TextFormField(
      focusNode: _passwordFocus,
      obscureText: _obscurePassword,
      autocorrect: false,
      controller: _passwordController,
      textInputAction: TextInputAction.done,
      decoration: InputDecoration(
          suffixIcon: GestureDetector(
            child: _passwordIcon(_obscurePassword),
            onTap: () {
              setState(() {
                if (_obscurePassword) {
                  _obscurePassword = false;
                } else {
                  _obscurePassword = true;
                }
              });
            },
          ),
          errorStyle: TextStyle(color: ParentColors.failure, fontSize: 14, fontWeight: FontWeight.w500),
          errorBorder: OutlineInputBorder(
            borderRadius: new BorderRadius.circular(4.0),
            borderSide: new BorderSide(color: ParentColors.failure),
          ),
          hintText: L10n(context).qrCreateAccountHintPassword,
          hintStyle: TextStyle(color: ParentColors.tiara, fontSize: 16, fontWeight: FontWeight.normal),
          border: OutlineInputBorder(
            borderRadius: new BorderRadius.circular(4.0),
            borderSide: new BorderSide(),
          )),
      onFieldSubmitted: (term) {
        _clearFieldFocus();
        _formKey.currentState?.validate();
      },
      validator: (value) {
        if (value == null || value.isEmpty) {
          return L10n(context).qrCreateAccountPasswordError;
        } else if (value.length < 8) {
          return L10n(context).qrCreateAccountPasswordLengthError;
        } else {
          return null;
        }
      },
    );
  }

  Widget _passwordIcon(bool obscurePassword) {
    if (obscurePassword) {
      return Icon(CanvasIcons.eye, semanticLabel: L10n(context).qrCreateAccountEyeSemantics);
    } else {
      return Padding(
        padding: const EdgeInsets.fromLTRB(12, 10, 12, 10),
        child: SvgPicture.asset(
          'assets/svg/eye_off.svg',
          semanticsLabel: L10n(context).qrCreateAccountEyeOffSemantics,
        ),
      );
    }
  }

  _fieldFocusChange(FocusNode currentFocus, FocusNode nextFocus) {
    currentFocus.unfocus();
    FocusScope.of(context).requestFocus(nextFocus);
  }

  _clearFieldFocus() {
    _nameFocus.unfocus();
    _emailFocus.unfocus();
    _passwordFocus.unfocus();
  }

  Widget _createAccountTOS() {
    return FutureBuilder(
        future: _tosFuture,
        builder: (context, AsyncSnapshot<TermsOfService?> snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return Container(color: Theme.of(context).scaffoldBackgroundColor, child: LoadingIndicator());
          }

          if (snapshot.hasError || snapshot.data == null) {
            return _getTosSpan(null);
          } else {
            var terms = snapshot.data;
            if (terms == null || terms.passive) {
              return _getPrivacyPolicySpan();
            } else {
              return _getTosSpan(snapshot.data);
            }
          }
        });
  }

  Widget _getPrivacyPolicySpan() {
    return Align(
      child: RichText(
        text: TextSpan(
            text: L10n(context).qrCreateAccountViewPrivacy,
            style: _linkSpanStyle,
            recognizer: TapGestureRecognizer()
              ..onTap = () {
                locator<AccountCreationInteractor>().launchPrivacyPolicy();
              }),
      ),
    );
  }

  TextSpan _getTosSpanHelper({required String text, required List<TextSpan> inputSpans}) {
    var indexedSpans = inputSpans.map((it) => MapEntry(text.indexOf(it.text!), it)).toList();
    indexedSpans.sort((a, b) => a.key.compareTo(b.key));

    int index = 0;
    List<TextSpan> spans = [];

    for (var indexedSpan in indexedSpans) {
      spans.add(TextSpan(text: text.substring(index, indexedSpan.key)));
      spans.add(indexedSpan.value);
      index = indexedSpan.key + indexedSpan.value.text!.length;
    }
    spans.add(TextSpan(text: text.substring(index)));

    return TextSpan(children: spans);
  }

  Widget _getTosSpan(TermsOfService? terms) {
    var termsOfService = L10n(context).qrCreateAccountTermsOfService;
    var privacyPolicy = L10n(context).qrCreateAccountPrivacyPolicy;
    var body = L10n(context).qrCreateAccountTos(termsOfService, privacyPolicy);

    return Center(
        child: Text.rich(
            _getTosSpanHelper(text: body, inputSpans: [
              TextSpan(
                text: termsOfService,
                style: _linkSpanStyle,
                recognizer: TapGestureRecognizer()
                  ..onTap = () {
                    if (terms == null) {
                      locator<AccountCreationInteractor>().launchDefaultToS();
                    } else {
                      locator<QuickNav>().pushRoute(
                          context,
                          PandaRouter.termsOfUse(
                              accountId: widget.pairingInfo.accountId, domain: widget.pairingInfo.domain));
                    }
                  },
              ),
              TextSpan(
                  text: privacyPolicy,
                  style: _linkSpanStyle,
                  recognizer: TapGestureRecognizer()
                    ..onTap = () => locator<AccountCreationInteractor>().launchPrivacyPolicy()),
            ]),
            style: _defaultSpanStyle,
            key: AccountCreationScreen.accountCreationTextSpanKey));
  }

  Widget _createAccountButton() {
    return ButtonTheme(
      child: ElevatedButton(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: _isLoading
              ? Center(child: CircularProgressIndicator(valueColor: AlwaysStoppedAnimation<Color>(Colors.white)))
              : Text(
                  L10n(context).qrCreateAccount,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(fontSize: 16, color: Colors.white),
                ),
        ),
        style: ElevatedButton.styleFrom(
          backgroundColor: Theme.of(context).colorScheme.secondary,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(4))),
        ),
        onPressed: () {
          if (!_isLoading) _handleCreateAccount();
        },
      ),
    );
  }

  Widget _alreadyHaveAnAccount() {
    // Since this one is always alone, the touch target size has been boosted
    return GestureDetector(
      onTap: () => locator<QuickNav>().pushRoute(context, PandaRouter.loginWeb(widget.pairingInfo.domain)),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(0, 14, 0, 14),
        child: ConstrainedBox(
          constraints: BoxConstraints(minHeight: 48),
          child: Center(
            child: RichText(
              text: TextSpan(
                style: _defaultSpanStyle,
                children: <TextSpan>[
                  TextSpan(text: L10n(context).qrCreateAccountSignIn1),
                  TextSpan(text: L10n(context).qrCreateAccountSignIn2, style: _linkSpanStyle)
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  void _handleCreateAccount() async {
    if (_formKey.currentState?.validate() == true) {
      setState(() => _isLoading = true);
      try {
        var response = await locator<AccountCreationInteractor>().createNewAccount(
            widget.pairingInfo.accountId,
            widget.pairingInfo.code,
            _nameController.text,
            _emailController.text,
            _passwordController.text,
            widget.pairingInfo.domain);

        if (response.statusCode == 200) {
          setState(() => _isLoading = false);

          locator<Analytics>().logEvent(
            AnalyticsEventConstants.QR_ACCOUNT_SUCCESS,
            extras: {AnalyticsParamConstants.DOMAIN_PARAM: widget.pairingInfo.domain},
          );

          // Route them to the login page with their domain
          locator<QuickNav>().pushRoute(context, PandaRouter.loginWeb(widget.pairingInfo.domain));
        }
      } catch (e) {
        setState(() => _isLoading = false);

        locator<Analytics>().logEvent(
          AnalyticsEventConstants.QR_ACCOUNT_FAILURE,
          extras: {AnalyticsParamConstants.DOMAIN_PARAM: widget.pairingInfo.domain},
        );

        if (e is DioError) {
          _handleDioError(e);
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(L10n(context).unexpectedError)),
          );
        }
      }
    }
  }

  _handleDioError(DioError e) {
    String emailError = '';
    String pairingError = '';
    try {
      emailError = e.response?.data['errors']['user']['pseudonyms'][0]['message'];
      if (emailError.isNotEmpty) {
        setState(() {
          _emailErrorText = _validateEmail('', apiError: true);
        });
      }
    } catch (e) {
      // If we catch it means the error isn't present
    }

    try {
      pairingError = e.response?.data['errors']['pairing_code']['code'][0]['message'];
      if (pairingError.isNotEmpty) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(L10n(context).errorPairingFailed)),
        );
      }
    } catch (e) {
      // If we catch it means the error isn't present
    }

    if (pairingError.isEmpty && emailError.isEmpty) {
      // Show generic error case
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(L10n(context).errorGenericPairingFailed)),
      );
    }
  }
}
