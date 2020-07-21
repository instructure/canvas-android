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

import 'package:flutter_parent/models/terms_of_service.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class AccountCreationInteractor {
  Future<TermsOfService> getToSForAccount(String accountId, String domain) {
    return locator<AccountsApi>().getTermsOfServiceForAccount(accountId, domain);
  }

  Future<User> createNewAccount(
      String accountId, String pairingCode, String fullname, String email, String password, String domain) async {
    return null;
  }
}
