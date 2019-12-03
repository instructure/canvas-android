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

class UserName extends StatelessWidget {
  final String userName;
  final String pronouns;
  final TextStyle style;

  UserName(this.userName, this.pronouns, {this.style});

  UserName.fromUser(User user, {this.style = null})
      : userName = user.name,
        pronouns = user.pronouns;



  @override
  Widget build(BuildContext context) {
    return Text.rich(
      TextSpan(children: [
        TextSpan(text: userName),
        if (pronouns != null) TextSpan(text: ' (${pronouns})', style: TextStyle(fontStyle: FontStyle.italic))
      ]),
      style: style,
    );
  }
}
