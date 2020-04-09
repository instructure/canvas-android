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
}
