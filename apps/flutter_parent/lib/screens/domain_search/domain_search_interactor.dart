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

import 'package:flutter_parent/models/school_domain.dart';
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/url_launcher.dart';

class DomainSearchInteractor {
  Future<List<SchoolDomain>?> performSearch(String query) {
    return locator<AccountsApi>().searchDomains(query);
  }

  openCanvasGuides() {
    locator<UrlLauncher>()
        .launch('https://community.canvaslms.com/docs/DOC-9902-canvas-parent-android-guide-table-of-contents');
  }

  openCanvasSupport() {
    locator<UrlLauncher>().launch('https://community.canvaslms.com/docs/DOC-17624-how-to-contact-canvas-support');
  }
}
