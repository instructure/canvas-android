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
import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/utils/qr_utils.dart';

class QRTutorialScreenInteractor {

  Future<BarcodeScanResult> scan(BuildContext context) async {
    try {
      String barcodeResult = await BarcodeScanner.scan();
      var uri = Uri.parse(barcodeResult);
      if (QRUtils.verifySSOLogin(uri)) {
        return BarcodeScanResult(true, result: barcodeResult);
      } else {
        return BarcodeScanResult(false, errorMessage: L10n(context).invalidQRCodeError);
      }
    } on PlatformException catch (e) {
      if (e.code == BarcodeScanner.CameraAccessDenied) {
        return BarcodeScanResult(false, errorMessage: L10n(context).qrCodeNoCameraError);
      } else {
        // Unknown error while scanning
        return BarcodeScanResult(false, errorMessage: L10n(context).invalidQRCodeError);
      }
    } on FormatException {
      // User returned, do nothing
    } catch (e) {
      // Unknown error while scanning
      return BarcodeScanResult(false, errorMessage: L10n(context).invalidQRCodeError);
    }
  }

}

class BarcodeScanResult {
  final bool isSuccess;
  final String errorMessage;
  final String result;

  BarcodeScanResult(this.isSuccess, {this.errorMessage = '', this.result = ''});
}
