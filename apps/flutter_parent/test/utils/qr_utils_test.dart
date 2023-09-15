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
import 'package:flutter_parent/utils/veneers/barcode_scan_veneer.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import 'test_app.dart';
import 'test_helpers/mock_helpers.dart';
import 'test_helpers/mock_helpers.mocks.dart';

void main() {
  String _getQRUrl(
      {String host = QRUtils.QR_HOST, String code = QRUtils.QR_AUTH_CODE, String domain = QRUtils.QR_DOMAIN}) {
    return 'https://$host/canvas/login?$code=1234'
        '&$domain=mobiledev.instructure.com';
  }

  group('verifySSOLogin', () {
    test('verifySSOLogin returns true with valid Uri', () {
      var verified = QRUtils.verifySSOLogin(_getQRUrl());
      expect(verified, isNotNull);
    });

    test('verifySSOLogin returns false with null Uri', () {
      var verified = QRUtils.verifySSOLogin(null);
      expect(verified, isNull);
    });

    test('verifySSOLogin returns false with invalid host param', () {
      var verified = QRUtils.verifySSOLogin(_getQRUrl(host: 'hodor'));
      expect(verified, isNull);
    });

    test('verifySSOLogin returns false with missing domain param', () {
      var verified = QRUtils.verifySSOLogin(_getQRUrl(domain: 'hodor'));
      expect(verified, isNull);
    });

    test('verifySSOLogin returns false with missing auth code param', () {
      var verified = QRUtils.verifySSOLogin(_getQRUrl(code: 'hodor'));
      expect(verified, isNull);
    });
  });

  group('parsePairingInfo', () {
    test('parsePairingInfo returns invalidCode error for null input', () {
      var result = QRUtils.parsePairingInfo(null);
      expect(result, isA<QRPairingScanError>());
      expect((result as QRPairingScanError).type, QRPairingScanErrorType.invalidCode);
    });

    test('parsePairingInfo returns invalidCode error for empty input', () {
      var result = QRUtils.parsePairingInfo('');
      expect(result, isA<QRPairingScanError>());
      expect((result as QRPairingScanError).type, QRPairingScanErrorType.invalidCode);
    });

    test('parsePairingInfo returns invalidCode error for invalid input', () {
      var result = QRUtils.parsePairingInfo('invalid input');
      expect(result, isA<QRPairingScanError>());
      expect((result as QRPairingScanError).type, QRPairingScanErrorType.invalidCode);
    });

    test('parsePairingInfo returns invalidCode error if input has wrong path', () {
      var input = 'canvas-parent://test.instructure.com/addStudent?code=abcd&account_id=1234';
      var result = QRUtils.parsePairingInfo(input);
      expect(result, isA<QRPairingScanError>());
      expect((result as QRPairingScanError).type, QRPairingScanErrorType.invalidCode);
    });

    test('parsePairingInfo returns invalidCode error if input is missing pairing code', () {
      var input = 'canvas-parent://test.instructure.com/pair?account_id=1234';
      var result = QRUtils.parsePairingInfo(input);
      expect(result, isA<QRPairingScanError>());
      expect((result as QRPairingScanError).type, QRPairingScanErrorType.invalidCode);
    });

    test('parsePairingInfo returns QRPairingInfo on successful parse', () {
      var input = 'canvas-parent://test.instructure.com/pair?code=aBc123&account_id=1234';
      var result = QRUtils.parsePairingInfo(input);
      expect(result, isA<QRPairingInfo>());
      expect((result as QRPairingInfo).domain, 'test.instructure.com');
      expect((result as QRPairingInfo).code, 'aBc123');
      expect((result as QRPairingInfo).accountId, '1234');
    });
  });

  group('scanPairingCode', () {
    MockBarcodeScanVeneer barcodeScanner = MockBarcodeScanVeneer();
    setupTestLocator((locator) => locator.registerLazySingleton<BarcodeScanVeneer>(() => barcodeScanner));

    setUp(() {
      reset(barcodeScanner);
    });

    test('scanPairingCode returns QRPairingInfo on successful scan of a valid code', () async {
      var validCode = 'canvas-parent://test.instructure.com/pair?code=aBc123&account_id=1234';
      when(barcodeScanner.scanBarcode()).thenAnswer((_) async => ScanResult(rawContent: validCode));
      var result = await QRUtils.scanPairingCode();
      expect(result, isA<QRPairingInfo>());
      expect((result as QRPairingInfo).domain, 'test.instructure.com');
      expect((result).code, 'aBc123');
      expect((result).accountId, '1234');
    });

    test('scanPairingCode returns canceled result if scan was canceled', () async {
      when(barcodeScanner.scanBarcode()).thenAnswer((_) async => ScanResult(type: ResultType.Cancelled));
      var result = await QRUtils.scanPairingCode();
      expect(result, isA<QRPairingScanError>());
      expect((result as QRPairingScanError).type, QRPairingScanErrorType.canceled);
    });

    test('scanPairingCode returns invalidCode error if there was a scan error', () async {
      when(barcodeScanner.scanBarcode()).thenAnswer((_) async => ScanResult(type: ResultType.Error));
      var result = await QRUtils.scanPairingCode();
      expect(result, isA<QRPairingScanError>());
      expect((result as QRPairingScanError).type, QRPairingScanErrorType.invalidCode);
    });

    test('scanPairingCode returns camera error if camera permission was denied', () async {
      var exception = PlatformException(code: BarcodeScanner.cameraAccessDenied);
      when(barcodeScanner.scanBarcode()).thenAnswer((_) async => throw exception);
      var result = await QRUtils.scanPairingCode();
      expect(result, isA<QRPairingScanError>());
      expect((result as QRPairingScanError).type, QRPairingScanErrorType.cameraError);
    });

    test('scanPairingCode returns unknown error if camera permission was denied', () async {
      when(barcodeScanner.scanBarcode()).thenAnswer((_) async => throw 'Fake Exception');
      var result = await QRUtils.scanPairingCode();
      expect(result, isA<QRPairingScanError>());
      expect((result as QRPairingScanError).type, QRPairingScanErrorType.unknown);
    });
  });
}
