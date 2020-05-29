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

  Future<AccountPermissions> getAccountPermissions() {
    return fetch(canvasDio().get('accounts/self/permissions'));
  }

  Future<bool> getPairingAllowed() async {
    var response = await canvasDio().get('accounts/self/authentication_providers/canvas');
    var selfRegistration = response.data['self_registration'];
    return selfRegistration == 'observer' || selfRegistration == 'all';
  }
}
