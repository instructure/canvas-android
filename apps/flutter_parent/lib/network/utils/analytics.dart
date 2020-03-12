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
class AnalyticsEventConstants {
  static String get REFRESH_TOKEN => 'refresh_token';
  static String get FOREVER_TOKEN => 'forever_token';
  static String get LOGIN_FAILURE => 'login_failure';
  static String get LOGIN_SUCCESS => 'login_success';
  static String get TOKEN_REFRESH_FAILURE => 'token_refresh_failure';
  static String get TOKEN_REFRESH_FAILURE_TOKEN_NOT_VALID => 'token_refresh_failure_token_not_valid';
  static String get TOKEN_REFRESH_FAILURE_NO_SECRET => 'token_refresh_failure_no_secret';
}
