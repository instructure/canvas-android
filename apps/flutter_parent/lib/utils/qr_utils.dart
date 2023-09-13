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
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/veneers/barcode_scan_veneer.dart';

class QRUtils {
  // QR Login
  static const String QR_DOMAIN = 'domain';
  static const String QR_AUTH_CODE = 'code_android_parent';
  static const String QR_HOST = 'sso.canvaslms.com';
  static const String QR_HOST_BETA = 'sso.beta.canvaslms.com';
  static const String QR_HOST_TEST = 'sso.test.canvaslms.com';

  // QR Pairing
  static const String QR_PAIR_PATH = 'pair';
  static const String QR_PAIR_PARAM_CODE = 'code';
  static const String QR_PAIR_PARAM_ACCOUNT_ID = 'account_id';

  static Uri? verifySSOLogin(String? url) {
    if (url == null) return null;
    try {
      var uri = Uri.parse(url);
      var hostList = [QR_HOST, QR_HOST_BETA, QR_HOST_TEST];
      if (hostList.contains(uri.host) &&
          uri.queryParameters[QR_DOMAIN] != null &&
          uri.queryParameters[QR_AUTH_CODE] != null) {
        return uri;
      } else {
        return null;
      }
    } catch (e) {
      return null;
    }
  }

  /// Opens the bar code scanner and attempts to scan for a pairing QR code
  static Future<QRPairingScanResult> scanPairingCode() async {
    try {
      ScanResult scanResult = await locator<BarcodeScanVeneer>().scanBarcode();
      switch (scanResult.type) {
        case ResultType.Barcode:
          return QRUtils.parsePairingInfo(scanResult.rawContent);
        case ResultType.Cancelled:
          return QRPairingScanResult.error(QRPairingScanErrorType.canceled);
        case ResultType.Error:
          return QRPairingScanResult.error(QRPairingScanErrorType.invalidCode);
      }
    } on PlatformException catch (e) {
      if (e.code == BarcodeScanner.cameraAccessDenied) {
        return QRPairingScanResult.error(QRPairingScanErrorType.cameraError);
      }
    } catch (e) {
      // Intentionally left blank
    }
    return QRPairingScanResult.error(QRPairingScanErrorType.unknown);
  }

  /// Attempts to parse and return QR pairing information from the provided uri. Returns null if parsing failed.
  static QRPairingScanResult parsePairingInfo(String? rawUri) {
    if (rawUri == null) return QRPairingScanResult.error(QRPairingScanErrorType.invalidCode);
    try {
      var uri = Uri.parse(rawUri);
      var params = uri.queryParameters;
      if (QR_PAIR_PATH == uri.pathSegments.first &&
          params[QR_PAIR_PARAM_CODE] != null &&
          params[QR_PAIR_PARAM_ACCOUNT_ID] != null) {
        return QRPairingScanResult.success(params[QR_PAIR_PARAM_CODE]!, uri.host, params[QR_PAIR_PARAM_ACCOUNT_ID]!);
      }
    } catch (e) {
      // Intentionally left blank
    }
    return QRPairingScanResult.error(QRPairingScanErrorType.invalidCode);
  }
}

class QRPairingScanResult {
  QRPairingScanResult._();

  factory QRPairingScanResult.success(String code, String domain, String accountId) = QRPairingInfo._;

  factory QRPairingScanResult.error(QRPairingScanErrorType type) = QRPairingScanError._;
}

class QRPairingInfo extends QRPairingScanResult {
  final String code;
  final String domain;
  final String accountId;

  QRPairingInfo._(this.code, this.domain, this.accountId) : super._();
}

class QRPairingScanError extends QRPairingScanResult {
  final QRPairingScanErrorType type;

  QRPairingScanError._(this.type) : super._();
}

enum QRPairingScanErrorType { invalidCode, cameraError, canceled, unknown }
