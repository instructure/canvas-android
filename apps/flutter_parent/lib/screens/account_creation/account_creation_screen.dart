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

import 'package:flutter/cupertino.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_svg/svg.dart';

class AccountCreationScreen extends StatefulWidget {
  final QRPairingInfo pairingInfo;

  const AccountCreationScreen(this.pairingInfo, {Key key}) : super(key: key);

  @override
  _AccountCreationScreenState createState() => _AccountCreationScreenState();
}

class _AccountCreationScreenState extends State<AccountCreationScreen> {
  final _formKey = GlobalKey<FormState>();
  bool _obscurePassword = true;
  Widget _passwordIcon = Padding(
    padding: const EdgeInsets.fromLTRB(12, 10, 12, 10),
    child: SvgPicture.asset(
      'assets/svg/eye_off.svg',
    ),
  );

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => Scaffold(
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
            _accountCreationForm(context),
            SizedBox(height: 16),
            _createAccountTOS(context),
            SizedBox(height: 8),
            _createAccountButton(context),
            SizedBox(height: 24),
            _alreadyHaveAnAccount(context),
            SizedBox(height: 8),
          ],
        ),
      )),
    );
  }

  Widget _accountCreationForm(BuildContext context) {
    final FocusNode _nameFocus = FocusNode();
    final FocusNode _emailFocus = FocusNode();
    final FocusNode _passwordFocus = FocusNode();

    return Form(
      key: _formKey,
      child: Column(
        children: <Widget>[
          _formFieldLabel(context, L10n(context).qrCreateAccountLabelName),
          TextFormField(
            focusNode: _nameFocus,
            textInputAction: TextInputAction.next,
            autocorrect: false,
            decoration: InputDecoration(
                errorStyle: TextStyle(color: ParentColors.failure, fontSize: 14, fontWeight: FontWeight.w500),
                errorBorder: OutlineInputBorder(
                  borderRadius: new BorderRadius.circular(4.0),
                  borderSide: new BorderSide(color: ParentColors.failure),
                ),
                hintText: 'Full name...',
                hintStyle: TextStyle(color: ParentColors.tiara, fontSize: 16, fontWeight: FontWeight.normal),
                border: OutlineInputBorder(
                  borderRadius: new BorderRadius.circular(4.0),
                  borderSide: new BorderSide(),
                )),
            onFieldSubmitted: (term) {
              _fieldFocusChange(context, _nameFocus, _emailFocus);
            },
            validator: (value) {
              if (value.isEmpty) {
                return 'Name Is Required';
              } else {
                return null;
              }
            },
          ),
          _formFieldLabel(context, L10n(context).qrCreateAccountLabelEmail),
          TextFormField(
            focusNode: _emailFocus,
            keyboardType: TextInputType.emailAddress,
            textInputAction: TextInputAction.next,
            autocorrect: false,
            decoration: InputDecoration(
                errorStyle: TextStyle(color: ParentColors.failure, fontSize: 14, fontWeight: FontWeight.w500),
                errorBorder: OutlineInputBorder(
                  borderRadius: new BorderRadius.circular(4.0),
                  borderSide: new BorderSide(color: ParentColors.failure),
                ),
                hintText: 'Email...',
                hintStyle: TextStyle(color: ParentColors.tiara, fontSize: 16, fontWeight: FontWeight.normal),
                border: OutlineInputBorder(
                  borderRadius: new BorderRadius.circular(4.0),
                  borderSide: new BorderSide(),
                )),
            onFieldSubmitted: (term) {
              _fieldFocusChange(context, _emailFocus, _passwordFocus);
            },
            validator: (value) {
              if (value.isEmpty) {
                return 'Email Is Required';
              } else {
                return null;
              }
            },
          ),
          _formFieldLabel(context, L10n(context).qrCreateAccountLabelPassword),
          TextFormField(
            focusNode: _passwordFocus,
            obscureText: _obscurePassword,
            autocorrect: false,
            textInputAction: TextInputAction.done,
            decoration: InputDecoration(
                suffixIcon: GestureDetector(
                  child: _passwordIcon,
                  onTap: () {
                    setState(() {
                      if (_obscurePassword) {
                        _obscurePassword = false;
                        _passwordIcon = Padding(
                          padding: const EdgeInsets.fromLTRB(12, 9, 12, 10),
                          child: SvgPicture.asset(
                            'assets/svg/eye_off.svg',
                          ),
                        );
                      } else {
                        _obscurePassword = true;
                        _passwordIcon = Icon(CanvasIcons.eye);
                      }
                    });
                  },
                ),
                errorStyle: TextStyle(color: ParentColors.failure, fontSize: 14, fontWeight: FontWeight.w500),
                errorBorder: OutlineInputBorder(
                  borderRadius: new BorderRadius.circular(4.0),
                  borderSide: new BorderSide(color: ParentColors.failure),
                ),
                hintText: 'Password...',
                hintStyle: TextStyle(color: ParentColors.tiara, fontSize: 16, fontWeight: FontWeight.normal),
                border: OutlineInputBorder(
                  borderRadius: new BorderRadius.circular(4.0),
                  borderSide: new BorderSide(),
                )),
            onFieldSubmitted: (term) {
              _formKey.currentState.validate();
              // TODO - button press tooooo
            },
            validator: (value) {
              if (value.isEmpty) {
                return 'Password Is Required';
              } else {
                return null;
              }
            },
          ),
        ],
      ),
    );
  }
  /*
  suffixIcon: GestureDetector(
                    onTap: () {
                      setState(() {
                        if (_obscurePassword) {
                          _obscurePassword = false;
                          _passwordIcon = SvgPicture.asset(
                            'assets/svg/eye_off.svg',
                          );
                        } else {
                          _obscurePassword = true;
                          _passwordIcon = Icon(CanvasIcons.eye);
                        }
                      });
                    },
                    child: Container(width: 24, height: 24, child: _passwordIcon)),
   */

  _fieldFocusChange(BuildContext context, FocusNode currentFocus, FocusNode nextFocus) {
    currentFocus.unfocus();
    FocusScope.of(context).requestFocus(nextFocus);
  }

  Widget _createAccountButton(BuildContext context) {
    return ButtonTheme(
      child: RaisedButton(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Text(
            L10n(context).qrCreateAccount,
            style: TextStyle(fontSize: 16),
          ),
        ),
        color: Theme.of(context).accentColor,
        textColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(4))),
        onPressed: () {
          _formKey.currentState.validate();
        },
      ),
    );
  }

  Widget _formFieldLabel(BuildContext context, String text) {
    return Align(
        alignment: Alignment.centerLeft,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(0, 12, 0, 8),
          child: Text(text, style: TextStyle(color: ParentColors.licorice, fontSize: 16, fontWeight: FontWeight.w500)),
        ));
  }

  Widget _createAccountTOS(BuildContext context) {
    TextStyle defaultStyle = TextStyle(color: ParentColors.ash, fontSize: 14.0, fontWeight: FontWeight.normal);
    TextStyle linkStyle = TextStyle(color: ParentColors.parentApp, fontSize: 14.0, fontWeight: FontWeight.normal);
    return Center(
      child: RichText(
        text: TextSpan(
          style: defaultStyle,
          children: <TextSpan>[
            TextSpan(text: 'By tapping \'Create Account\', you agree to the '),
            TextSpan(
                text: 'Terms of Service',
                style: linkStyle,
                recognizer: TapGestureRecognizer()
                  ..onTap = () {
                    locator<UrlLauncher>().launch('https://www.instructure.com/policies/terms-of-use-canvas/');
                  }),
            TextSpan(text: ' and acknowledge the '),
            TextSpan(
                text: 'Privacy Policy',
                style: linkStyle,
                recognizer: TapGestureRecognizer()
                  ..onTap = () {
                    locator<UrlLauncher>().launch('https://www.instructure.com/policies/privacy/');
                  }),
          ],
        ),
      ),
    );
  }

  Widget _alreadyHaveAnAccount(BuildContext context) {
    TextStyle defaultStyle = TextStyle(color: ParentColors.ash, fontSize: 14.0, fontWeight: FontWeight.normal);
    TextStyle linkStyle = TextStyle(color: ParentColors.parentApp, fontSize: 14.0, fontWeight: FontWeight.normal);
    return Center(
      child: RichText(
        text: TextSpan(
          style: defaultStyle,
          children: <TextSpan>[
            TextSpan(text: 'Already have an account? '),
            TextSpan(
                text: 'Sign In',
                style: linkStyle,
                recognizer: TapGestureRecognizer()
                  ..onTap = () {
                    locator<QuickNav>().pushRoute(context, PandaRouter.loginWeb(widget.pairingInfo.domain));
                  }),
          ],
        ),
      ),
    );
  }
}
