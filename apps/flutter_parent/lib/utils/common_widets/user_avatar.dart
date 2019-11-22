/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter/material.dart';
import 'package:flutter_parent/models/user.dart';

class UserAvatar extends StatelessWidget {
  final User _user;

  UserAvatar(this._user, {Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    if (_user.avatarUrl != null) {
      return Container(
        margin: EdgeInsets.only(top: 16.0),
        decoration: BoxDecoration(
            shape: BoxShape.circle,
            image: DecorationImage(image: NetworkImage(_user.avatarUrl), fit: BoxFit.contain)),
        width: 48,
        height: 48,
        alignment: AlignmentDirectional.centerStart,
      );
    } else {
      return CircleAvatar(child: Text(getUserInitials(_user)));
    }
  }

  static String getUserInitials(User _user) {
    if (_user.shortName == null || _user.shortName.isEmpty) return '?';

    var name = _user.shortName;

    // Take the first letter of each word, uppercase it, and put it into a list
    var initials = name.trim().split(RegExp(r"\s+")).map((it) => it.toUpperCase()[0]).toList();

    if (initials.length == 2) {
      // We have two initials, put them together into one string and return it
      return initials.join('');
    } else {
      // Just take the first initial if we don't have exactly two
      return initials[0];
    }
  }

}
