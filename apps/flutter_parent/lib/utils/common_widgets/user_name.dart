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
import 'package:tuple/tuple.dart';

class UserName extends StatelessWidget {
  final String userName;
  final String pronouns;
  final TextStyle style;
  final TextOverflow overflow;

  UserName(this.userName, this.pronouns, {this.style, this.overflow});

  UserName.fromUser(User user, {this.style = null, this.overflow = null})
      : userName = user.name,
        pronouns = user.pronouns;

  UserName.fromBasicUser(BasicUser user, {this.style = null, this.overflow = null})
      : userName = user.name,
        pronouns = user.pronouns;

  UserName.fromRecipient(Recipient recipient, {this.style = null, this.overflow = null})
      : userName = recipient.name,
        pronouns = recipient.pronouns;

  static TextSpan stylize(String source, List<String> pronouns) {
    if (source == null || source.isEmpty) return TextSpan(text: '');
    if (pronouns == null || pronouns.isEmpty) return TextSpan(text: source);

    Set<Tuple2<int, int>> indexSet = {};
    pronouns.map((it) => '($it)').forEach((it) {
      var start = 0;
      while (start < source.length) {
        var index = source.indexOf(it, start);
        if (index == -1) break;
        indexSet.add(Tuple2(index, index + it.length));
        start = index + it.length;
      }
    });

    if (indexSet.isEmpty) return TextSpan(text: source);

    var indices = indexSet.toList();
    indices.sort((a, b) => a.item1.compareTo(b.item1));

    List<TextSpan> spans = [];

    for (var i = 0; i <= indices.length; i++) {
      var range = i == indices.length ? Tuple2(indices[i - 1].item2, source.length) : indices[i];
      var lastRange = i == 0 ? null : indices[i - 1];

      Tuple2<int, int> leadingRange;
      if (lastRange == null) {
        if (range.item1 != 0) leadingRange = Tuple2(0, range.item1);
      } else if (range.item1 > lastRange.item2) {
        leadingRange = Tuple2(lastRange.item2, range.item1);
      }

      if (leadingRange != null) {
        spans.add(TextSpan(text: source.substring(leadingRange.item1, leadingRange.item2)));
      }

      if (range.item1 != range.item2) {
        spans.add(
          TextSpan(
            text: source.substring(range.item1, range.item2),
            style: TextStyle(fontStyle: FontStyle.italic),
          ),
        );
      }
    }

    return TextSpan(children: spans);
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
    );
  }
}
