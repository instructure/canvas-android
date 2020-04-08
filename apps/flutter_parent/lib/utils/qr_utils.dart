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

class QRUtils {
  static const String QR_DOMAIN = 'domain';
  static const String QR_AUTH_CODE = 'code_android_parent';
  static const String QR_HOST = 'sso.canvaslms.com';
  static const String QR_HOST_BETA = 'sso.beta.canvaslms.com';
  static const String QR_HOST_TEST = 'sso.test.canvaslms.com';

  static bool verifySSOLogin(Uri uri) {
    if(uri == null) return false;
    var hostList = [QR_HOST, QR_HOST_BETA, QR_HOST_TEST];
    return hostList.contains(uri.host)
        && uri.queryParameters[QR_DOMAIN] != null
        && uri.queryParameters[QR_AUTH_CODE] != null;
  }
}