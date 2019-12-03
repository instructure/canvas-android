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

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:transparent_image/transparent_image.dart';

class Avatar extends StatelessWidget {
  final Color backgroundColor;
  final String url;
  final double radius;
  final Widget overlay;
  final String name; // Generally should be the shortname of the user

  const Avatar(this.url, {
    Key key,
    this.backgroundColor,
    this.radius = 20,
    this.overlay,
    this.name,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Semantics(
      excludeSemantics: true,
      child: ClipRRect(
        borderRadius: BorderRadius.circular(radius),
        child: Container(
          color: backgroundColor ?? ParentTheme.of(context).nearSurfaceColor,
          width: radius * 2,
          height: radius * 2,
          child: Stack(
            children: <Widget>[
              url != null ?
              FadeInImage.memoryNetwork(
                fadeInDuration: const Duration(milliseconds: 300),
                fit: BoxFit.cover,
                width: radius * 2,
                height: radius * 2,
                image: url ?? '',
                placeholder: kTransparentImage,
              ) : CircleAvatar(
                child: Text(getUserInitials(name)),
                radius: radius,
              ),
              if (overlay != null)
                SizedBox(
                  width: radius * 2,
                  height: radius * 2,
                  child: overlay,
                ),
            ],
          ),
        ),
      ),
    );
  }

  // This method is static to make it easier to test!
  static String getUserInitials(String shortName) {
    if (shortName == null || shortName.isEmpty) return '?';

    var name = shortName;

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
