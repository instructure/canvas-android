import 'package:flutter/material.dart';
import 'package:flutter_parent/models/user.dart';

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

Widget userAvatar(User user) {
  if (user.avatarUrl != null) {
    return Container(
      margin: EdgeInsets.only(top: 16.0),
      decoration: BoxDecoration(
          shape: BoxShape.circle,
          image: DecorationImage(image: NetworkImage(user.avatarUrl), fit: BoxFit.contain)),
      width: 48,
      height: 48,
      alignment: AlignmentDirectional.centerStart,
    );
  } else {
    return CircleAvatar(child: Text('${user.sortableName.substring(0, 1)}'));
  }
}
