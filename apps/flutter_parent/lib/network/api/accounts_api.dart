// Copyright (C) 2019 - present Instructure, Inc.
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

import 'package:flutter_parent/models/account_permissions.dart';
import 'package:flutter_parent/models/school_domain.dart';
import 'package:flutter_parent/models/terms_of_service.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

class AccountsApi {
  Future<List<SchoolDomain>> searchDomains(String query) async {
    var dio = DioConfig.core(cacheMaxAge: Duration(minutes: 5)).dio;
    return fetchList(dio.get('accounts/search', queryParameters: {'search_term': query}));
  }

  Future<TermsOfService> getTermsOfService() {
    return fetch(canvasDio().get('accounts/self/terms_of_service'));
  }

  Future<TermsOfService> getTermsOfServiceForAccount(String accountId, String domain) {
    var dio = DioConfig.canvas(noHeaders: true).copyWith(baseUrl: 'https://$domain/api/v1/', forceRefresh: true).dio;
    return fetch(dio.get('accounts/$accountId/terms_of_service'));
  }

  Future<AccountPermissions> getAccountPermissions() {
    return fetch(canvasDio().get('accounts/self/permissions'));
  }

  Future<bool> getPairingAllowed() async {
    var response = await canvasDio().get('accounts/self/authentication_providers/canvas');
    var selfRegistration = response.data['self_registration'];
    return selfRegistration == 'observer' || selfRegistration == 'all';
  }

  /*
  class ErrorReportApi {
  static const DEFAULT_DOMAIN = 'https://canvas.instructure.com';

  Future<void> submitErrorReport({
    String subject,
    String description,
    String email,
    String severity,
    String stacktrace,
    String domain,
    String name,
    String becomeUser,
    String userRoles,
  }) {
    var config = domain == DEFAULT_DOMAIN ? DioConfig.core() : DioConfig.canvas();

    return config.dio.post(
      '/error_reports.json',
      queryParameters: {
        'error[subject]': subject,
        'error[url]': domain,
        'error[email]': email,
        'error[comments]': description,
        'error[user_perceived_severity]': severity,
        'error[name]': name,
        'error[user_roles]': userRoles,
        'error[become_user]': becomeUser,
        if (stacktrace != null) 'error[backtrace]': stacktrace,
      },
    );
  }
}
   */

  Future<User> createNewAccount(
      String accountId, String pairingCode, String fullname, String email, String password, String domain) async {
    var dio = DioConfig.canvas().copyWith(baseUrl: 'https://$domain').dio;

    // TODO - might need to be a post body rather than query params....
    return fetch(dio.post<User>('/api/v1/accounts/$accountId/users', queryParameters: {
      'user[name]': fullname,
      'user[terms_of_use]': true,
      'user[initial_enrollment]': 'observer',
      'pairing_code[code]': pairingCode,
      'pseudonym[unique_id]': email,
      'pseudonym[password]': password
    }));
  }
}
