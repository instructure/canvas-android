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

import 'package:barcode_scan/barcode_scan.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class QRTutorialScreen extends StatelessWidget {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
        builder: (context) => Scaffold(
              key: _scaffoldKey,
              appBar: AppBar(
                elevation: 4,
                title: Text(L10n(context).locateQRCode),
                automaticallyImplyLeading: true,
                actions: <Widget>[
                  _nextButton(context)
                ],
              ),
              body: _body(context),
            ));
  }

  Widget _nextButton(BuildContext context) {
    return MaterialButton(
      minWidth: 20,
      highlightColor: Colors.transparent,
      splashColor: Theme.of(context).accentColor.withAlpha(100),
      textColor: Theme.of(context).accentColor,
      shape: CircleBorder(side: BorderSide(color: Colors.transparent)),
      onPressed: () {
        scan(context);
      },
      child: Text(
        L10n(context).next.toUpperCase(),
        textAlign: TextAlign.end,
        style: TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }

  Widget _body(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.start,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: <Widget>[
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 24, 16, 0),
          child: Text(
            L10n(context).qrCodeExplanation,
            style: TextStyle(fontSize: 16),
          ),
        ),
        SizedBox(height: 24),
        FractionallySizedBox(
          alignment: Alignment.center,
          widthFactor: 0.75,
          child: Image(image: AssetImage('assets/png/locate-qr-code-tutorial.png'), fit: BoxFit.contain),
        )
      ],
    );
  }

  Future scan(BuildContext context) async {
    try {
      String barcodeResult = await BarcodeScanner.scan();
      var uri = Uri.parse(barcodeResult);
      if (QRUtils.verifySSOLogin(uri)) {
        locator<QuickNav>().pushRoute(context, PandaRouter.qrLogin(barcodeResult));
      } else {
        _showSnackBarError(context, L10n(context).invalidQRCodeError);
      }
    } on PlatformException catch (e) {
      if (e.code == BarcodeScanner.CameraAccessDenied) {
        _showSnackBarError(context, L10n(context).qrCodeNoCameraError);
      } else {
        _showSnackBarError(context, L10n(context).invalidQRCodeError);
      }
    } on FormatException {
      // User returned, do nothing
    } catch (e) {
      _showSnackBarError(context, L10n(context).invalidQRCodeError);
    }
  }

  _showSnackBarError(BuildContext context, String error) {
    _scaffoldKey.currentState.removeCurrentSnackBar();
    _scaffoldKey.currentState.showSnackBar(SnackBar(content: Text(error)));
  }
}
