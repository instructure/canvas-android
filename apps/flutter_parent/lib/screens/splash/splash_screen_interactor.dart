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

import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/screens/masquerade/masquerade_screen_interactor.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class SplashScreenInteractor {
  Future<SplashScreenData> getData() async {
    // Use same call as the dashboard so results will be cached
    var students = await locator<DashboardInteractor>().getStudents(forceRefresh: true);
    var isObserver = students.isNotEmpty;

    // Check for masquerade permissions if we haven't already
    if (ApiPrefs.getCurrentLogin().canMasquerade == null) {
      if (ApiPrefs.getDomain().contains(MasqueradeScreenInteractor.siteAdminDomain)) {
        ApiPrefs.updateCurrentLogin((b) => b..canMasquerade = true);
      } else {
        try {
          var permissions = await locator<AccountsApi>().getAccountPermissions();
          ApiPrefs.updateCurrentLogin((b) => b..canMasquerade = permissions.becomeUser);
        } catch (e) {
          ApiPrefs.updateCurrentLogin((b) => b..canMasquerade = false);
        }
      }
    }

    return SplashScreenData(isObserver, ApiPrefs.getCurrentLogin().canMasquerade);
  }
}

class SplashScreenData {
  final bool isObserver;
  final bool canMasquerade;

  SplashScreenData(this.isObserver, this.canMasquerade);
}
