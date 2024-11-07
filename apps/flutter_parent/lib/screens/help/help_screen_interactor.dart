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

import 'package:built_collection/built_collection.dart';
import 'package:flutter_parent/models/help_link.dart';
import 'package:flutter_parent/models/help_links.dart';
import 'package:flutter_parent/network/api/help_links_api.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class HelpScreenInteractor {
  Future<List<HelpLink>> getObserverCustomHelpLinks({bool forceRefresh = false}) async {
    HelpLinks? links = await locator.get<HelpLinksApi>().getHelpLinks(forceRefresh: forceRefresh);

    if (links == null) return Future.value([]);

    // Filter observer custom links if we have any, otherwise return an empty list
    return Future.value(filterObserverLinks(
        containsObserverLinks(links.customHelpLinks) ? links.customHelpLinks : links.defaultHelpLinks));
  }

  bool containsObserverLinks(BuiltList<HelpLink> links) => links.any((link) =>
      link.availableTo.contains(AvailableTo.observer) ||
      link.availableTo.contains(AvailableTo.user));

  List<HelpLink> filterObserverLinks(BuiltList<HelpLink> list) => list
      .where((link) => link.url != null && link.text != null)
      .where((link) =>
          link.availableTo.contains(AvailableTo.observer) ||
          link.availableTo.contains(AvailableTo.user))
      .toList();
}
