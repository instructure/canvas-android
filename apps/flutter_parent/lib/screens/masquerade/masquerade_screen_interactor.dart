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

import 'package:flutter_parent/network/api/user_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class MasqueradeScreenInteractor {
  String? getDomain() => ApiPrefs.getDomain();

  static final siteAdminDomain = 'siteadmin.instructure.com';

  Future<bool> startMasquerading(String masqueradingUserId, String masqueradingDomain) async {
    try {
      var user = await locator<UserApi>().getUserForDomain(masqueradingDomain, masqueradingUserId);
      ApiPrefs.updateCurrentLogin((b) => b
        ..masqueradeDomain = masqueradingDomain
        ..masqueradeUser = user?.toBuilder());
      return true;
    } catch (e) {
      return false;
    }
  }

  /// Cleans up the input domain and adds protocol and '.instructure.com' as necessary. Returns an empty string
  /// if sanitizing failed.
  String sanitizeDomain(String? domain) {
    if (domain == null || domain.isEmpty) return '';

    // Remove white space
    domain = domain.replaceAll(RegExp('\\s'), '');

    // Add '.instructure.com' if necessary
    if (!domain.contains('.') || domain.endsWith('.beta')) domain = '$domain.instructure.com';

    // Add protocol if missing
    if (!domain.startsWith('http')) domain = 'https://$domain';

    // Run through Uri parser for a final validity check
    try {
      String parsed = Uri.parse(domain).toString();
      return domain == parsed ? domain : '';
    } catch (e) {
      return '';
    }
  }
}
