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
import 'package:flutter_parent/screens/qr_login/qr_login_tutorial_screen_interactor.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/veneers/barcode_scan_veneer.dart';
import 'package:get_it/get_it.dart';
import 'package:test/test.dart';
import 'package:mockito/mockito.dart';

void main() {
  final mockScanner = _MockScanner();

  setUp(() {
    final _locator = GetIt.instance;
    _locator.reset();
    _locator.registerLazySingleton<BarcodeScanVeneer>(() => mockScanner);
  });

  test('returns success when given valid barcode', () async {
    final barcodeResultUrl =
        'https://${QRUtils.QR_HOST}/canvas/login?${QRUtils.QR_AUTH_CODE}=1234'
        '&${QRUtils.QR_DOMAIN}=mobiledev.instructure.com';

    when(mockScanner.scanBarcode()).thenAnswer((_) => Future.value(barcodeResultUrl));

    var interactor = QRLoginTutorialScreenInteractor();

    final result = await interactor.scan();
    expect(result.isSuccess, true);
    expect(result.result, barcodeResultUrl);
  });

  test('returns failure when given invalid barcode', () async {
    final barcodeResultUrl =
        'https://hodor.com/canvas/login?hodor_code=1234'
        '&hodor_domain=mobiledev.instructure.com';

    when(mockScanner.scanBarcode()).thenAnswer((_) => Future.value(barcodeResultUrl));

    var interactor = QRLoginTutorialScreenInteractor();

    final result = await interactor.scan();
    expect(result.isSuccess, false);
    expect(result.errorType, QRError.invalidQR);
    expect(result.result, null);
  });

  test('returns camera error camera access denied', () async {
    when(mockScanner.scanBarcode()).thenAnswer((_) => throw PlatformException(code: BarcodeScanner.CameraAccessDenied));

    var interactor = QRLoginTutorialScreenInteractor();

    final result = await interactor.scan();
    expect(result.isSuccess, false);
    expect(result.errorType, QRError.cameraError);
    expect(result.result, null);
  });

  test('returns error when given platform error occurs', () async {
    when(mockScanner.scanBarcode()).thenAnswer((_) => throw PlatformException);

    var interactor = QRLoginTutorialScreenInteractor();

    final result = await interactor.scan();
    expect(result.isSuccess, false);
    expect(result.errorType, QRError.invalidQR);
    expect(result.result, null);
  });

  test('returns error when given FormatException', () async {
    when(mockScanner.scanBarcode()).thenAnswer((_) => throw FormatException);

    var interactor = QRLoginTutorialScreenInteractor();

    final result = await interactor.scan();
    expect(result.isSuccess, false);
    expect(result.errorType, QRError.invalidQR);
    expect(result.result, null);
  });

}

class _MockScanner extends Mock implements BarcodeScanVeneer {}
