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

import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:test/test.dart';

void main() {
  String _getQRUrl(
      {String host = QRUtils.QR_HOST, String code = QRUtils.QR_AUTH_CODE, String domain = QRUtils.QR_DOMAIN}) {
    return 'https://$host/canvas/login?$code=1234'
        '&$domain=mobiledev.instructure.com';
  }

  test('verifySSOLogin returns true with valid Uri', () {
    final testUri = Uri.parse(_getQRUrl());
    var verified = QRUtils.verifySSOLogin(testUri);
    expect(verified, isTrue);
  });

  test('verifySSOLogin returns false with null Uri', () {
    var verified = QRUtils.verifySSOLogin(null);
    expect(verified, isFalse);
  });

  test('verifySSOLogin returns false with invalid host param', () {
    final testUri = Uri.parse(_getQRUrl(host: 'hodor'));
    var verified = QRUtils.verifySSOLogin(testUri);
    expect(verified, isFalse);
  });

  test('verifySSOLogin returns false with missing domain param', () {
    final testUri = Uri.parse(_getQRUrl(domain: 'hodor'));
    var verified = QRUtils.verifySSOLogin(testUri);
    expect(verified, isFalse);
  });

  test('verifySSOLogin returns false with missing auth code param', () {
    final testUri = Uri.parse(_getQRUrl(code: 'hodor'));
    var verified = QRUtils.verifySSOLogin(testUri);
    expect(verified, isFalse);
  });
}
