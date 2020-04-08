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
import 'package:flutter/services.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/veneers/barcode_scan_veneer.dart';

class QRLoginTutorialScreenInteractor {

  Future<BarcodeScanResult> scan() async {
    try {
      String barcodeResult = await locator<BarcodeScanVeneer>().scanBarcode();
      var uri = Uri.parse(barcodeResult);
      if (QRUtils.verifySSOLogin(uri)) {
        return BarcodeScanResult(true, result: barcodeResult);
      } else {
        return BarcodeScanResult(false, errorType: QRError.invalidQR);
      }
    } on PlatformException catch (e) {
      if (e.code == BarcodeScanner.CameraAccessDenied) {
        return BarcodeScanResult(false, errorType: QRError.cameraError);
      } else {
        // Unknown error while scanning
        return BarcodeScanResult(false, errorType: QRError.invalidQR);
      }
    } on FormatException {
      // User returned, do nothing
    } catch (e) {
      // Unknown error while scanning
      return BarcodeScanResult(false, errorType: QRError.invalidQR);
    }
  }

}

class BarcodeScanResult {
  final bool isSuccess;
  final QRError errorType;
  final String result;

  BarcodeScanResult(this.isSuccess, {this.errorType = null, this.result = null});
}

enum QRError {
  invalidQR,
  cameraError
}
