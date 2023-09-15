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

import 'package:barcode_scan2/barcode_scan2.dart';
import 'package:flutter/services.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/veneers/barcode_scan_veneer.dart';

class QRLoginTutorialScreenInteractor {
  Future<BarcodeScanResult> scan() async {
    BarcodeScanResult result = BarcodeScanResult(false, errorType: QRError.invalidQR);
    try {
      ScanResult scanResult = await locator<BarcodeScanVeneer>().scanBarcode();
      String barcodeResult = scanResult.rawContent;
      switch (scanResult.type) {
        case ResultType.Barcode:
          if (QRUtils.verifySSOLogin(barcodeResult) != null) {
            result = BarcodeScanResult(true, result: barcodeResult);
          } else {
            result = BarcodeScanResult(false, errorType: QRError.invalidQR);
          }
          break;
        case ResultType.Error:
          result = BarcodeScanResult(false, errorType: QRError.invalidQR);
          break;
        case ResultType.Cancelled:
          // Do nothing
          result = BarcodeScanResult(false, errorType: QRError.cancelled);
          break;
      }
    } on PlatformException catch (e) {
      if (e.code == BarcodeScanner.cameraAccessDenied) {
        result = BarcodeScanResult(false, errorType: QRError.cameraError);
      } else {
        // Unknown error while scanning
        result = BarcodeScanResult(false, errorType: QRError.invalidQR);
      }
    } catch (e) {
      // Just in case
      result = BarcodeScanResult(false, errorType: QRError.invalidQR);
    }

    return result;
  }
}

class BarcodeScanResult {
  final bool isSuccess;
  final QRError? errorType;
  final String? result;

  BarcodeScanResult(this.isSuccess, {this.errorType = null, this.result = null});
}

enum QRError { invalidQR, cameraError, cancelled }
