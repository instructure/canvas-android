// Copyright (C) 2020 - present Instructure, Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 3 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:barcode_scan2/barcode_scan2.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/screens/pairing/pairing_interactor.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/veneers/barcode_scan_veneer.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  MockBarcodeScanVeneer scanner = MockBarcodeScanVeneer();
  MockEnrollmentsApi enrollmentsApi = MockEnrollmentsApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<BarcodeScanVeneer>(() => scanner);
    locator.registerLazySingleton<EnrollmentsApi>(() => enrollmentsApi);
  });

  setUp(() {
    reset(scanner);
    reset(enrollmentsApi);
  });

  test('scanQRCode calls barcode scan veneer', () async {
    when(scanner.scanBarcode()).thenAnswer((_) async => ScanResult(type: ResultType.Error));
    var result = await PairingInteractor().scanQRCode();
    verify(scanner.scanBarcode());
    expect(result, isA<QRPairingScanError>());
  });

  test('pairWithStudent calls EnrollmentsApi', () async {
    String pairingCode = '123aBc';
    when(enrollmentsApi.pairWithStudent(any)).thenAnswer((_) async => true);
    var result = await PairingInteractor().pairWithStudent(pairingCode);
    verify(enrollmentsApi.pairWithStudent(pairingCode));
    expect(result, isTrue);
  });
}
