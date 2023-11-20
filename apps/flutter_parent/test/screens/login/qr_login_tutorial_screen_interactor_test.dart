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
import 'package:flutter_parent/screens/qr_login/qr_login_tutorial_screen_interactor.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/veneers/barcode_scan_veneer.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final interactor = QRLoginTutorialScreenInteractor();
  final mockScanner = MockBarcodeScanVeneer();

  setupTestLocator((locator) {
    locator.registerLazySingleton<BarcodeScanVeneer>(() => mockScanner);
  });

  setUp(() {
    reset(mockScanner);
  });

  test('returns success when given valid barcode', () async {
    final barcodeResultUrl = 'https://${QRUtils.QR_HOST}/canvas/login?${QRUtils.QR_AUTH_CODE}=1234'
        '&${QRUtils.QR_DOMAIN}=mobiledev.instructure.com';
    final scanResult = ScanResult(rawContent: barcodeResultUrl);

    when(mockScanner.scanBarcode()).thenAnswer((_) => Future.value(scanResult));

    final result = await interactor.scan();
    expect(result.isSuccess, isTrue);
    expect(result.result, barcodeResultUrl);
  });

  test('returns failure when given invalid barcode', () async {
    final barcodeResultUrl = 'https://hodor.com/canvas/login?hodor_code=1234'
        '&hodor_domain=mobiledev.instructure.com';
    final scanResult = ScanResult(rawContent: barcodeResultUrl);

    when(mockScanner.scanBarcode()).thenAnswer((_) => Future.value(scanResult));

    final result = await interactor.scan();
    expect(result.isSuccess, isFalse);
    expect(result.errorType, QRError.invalidQR);
    expect(result.result, isNull);
  });

  test('returns camera error camera access denied', () async {
    when(mockScanner.scanBarcode()).thenAnswer((_) => throw PlatformException(code: BarcodeScanner.cameraAccessDenied));

    final result = await interactor.scan();
    expect(result.isSuccess, isFalse);
    expect(result.errorType, QRError.cameraError);
    expect(result.result, isNull);
  });

  test('returns error when given platform error occurs', () async {
    when(mockScanner.scanBarcode()).thenAnswer((_) => throw PlatformException(code: ''));

    final result = await interactor.scan();
    expect(result.isSuccess, isFalse);
    expect(result.errorType, QRError.invalidQR);
    expect(result.result, isNull);
  });

  test('returns cancelled error when given ResultType.Cancelled', () async {
    final scanResult = ScanResult(type: ResultType.Cancelled);
    when(mockScanner.scanBarcode()).thenAnswer((_) => Future.value(scanResult));

    final result = await interactor.scan();
    expect(result.isSuccess, isFalse);
    expect(result.errorType, QRError.cancelled);
    expect(result.result, isNull);
  });

  test('returns error when given ResultType.Error', () async {
    final scanResult = ScanResult(type: ResultType.Error);
    when(mockScanner.scanBarcode()).thenAnswer((_) => Future.value(scanResult));

    final result = await interactor.scan();
    expect(result.isSuccess, isFalse);
    expect(result.errorType, QRError.invalidQR);
    expect(result.result, isNull);
  });

  test('returns default error case on generic exception', () async {
    when(mockScanner.scanBarcode()).thenAnswer((_) => throw Exception());

    final result = await interactor.scan();
    expect(result.isSuccess, isFalse);
    expect(result.errorType, QRError.invalidQR);
    expect(result.result, isNull);
  });

  test('returns error when given generic Exception', () async {
    when(mockScanner.scanBarcode()).thenAnswer((_) => throw Exception('ErRoR'));

    var interactor = QRLoginTutorialScreenInteractor();

    final result = await interactor.scan();
    expect(result.isSuccess, isFalse);
    expect(result.errorType, QRError.invalidQR);
    expect(result.result, isNull);
  });
}
