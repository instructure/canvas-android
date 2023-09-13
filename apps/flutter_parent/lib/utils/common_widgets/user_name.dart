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

import 'package:flutter/material.dart';
import 'package:flutter_parent/models/basic_user.dart';
import 'package:flutter_parent/models/recipient.dart';
import 'package:flutter_parent/models/user.dart';

class UserName extends StatelessWidget {
  final String userName;
  final String? pronouns;
  final TextStyle? style;
  final TextOverflow? overflow;

  UserName(this.userName, this.pronouns, {this.style, this.overflow});

  UserName.fromUser(User user, {this.style = null, this.overflow = null})
      : userName = user.name,
        pronouns = user.pronouns;

  UserName.fromUserShortName(User user, {this.style = null, this.overflow = null})
      : userName = user.shortName!,
        pronouns = user.pronouns;

  UserName.fromBasicUser(BasicUser user, {this.style = null, this.overflow = null})
      : userName = user.name!,
        pronouns = user.pronouns;

  UserName.fromRecipient(Recipient recipient, {this.style = null, this.overflow = null})
      : userName = recipient.name,
        pronouns = recipient.pronouns;

  String get text {
    if (pronouns != null && pronouns!.isNotEmpty) {
      return ('$userName ($pronouns)');
    } else {
      return userName;
    }
  }

  TextSpan get span => TextSpan(
        children: [
          TextSpan(text: userName),
          if (pronouns != null) TextSpan(text: ' (${pronouns})', style: TextStyle(fontStyle: FontStyle.italic))
        ],
        style: style,
      );

  @override
  Widget build(BuildContext context) {
    return Text.rich(
      span,
      overflow: overflow,
      style: Theme.of(context).textTheme.titleMedium
    );
  }
}
