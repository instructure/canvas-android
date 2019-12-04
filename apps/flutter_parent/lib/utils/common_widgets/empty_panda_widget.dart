//  Copyright (C) 2019 - present Instructure, Inc.
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, version 3 of the License.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter/material.dart';
import 'package:flutter_parent/utils/common_widgets/full_screen_scroll_container.dart';
import 'package:flutter_svg/svg.dart';

/// A simple empty widget that shows a centered SVG above a title/subtitle. All components are optionally, though
/// ideally all are present. Spacing is added based on which components are present.
class EmptyPandaWidget extends StatelessWidget {
  final String svgPath;
  final String title;
  final String subtitle;

  const EmptyPandaWidget({Key key, this.svgPath, this.title, this.subtitle}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return FullScreenScrollContainer(
      children: <Widget>[
        if (svgPath != null) SvgPicture.asset(svgPath, excludeFromSemantics: true),
        if (svgPath != null && (title != null || subtitle != null)) SizedBox(height: 64),
        if (title != null)
          Text(
            title,
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.title.copyWith(fontSize: 20, fontWeight: FontWeight.bold),
          ),
        if (title != null && subtitle != null) SizedBox(height: 8),
        if (subtitle != null)
          Text(
            subtitle,
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.subhead.copyWith(fontWeight: FontWeight.normal),
          ),
      ],
    );
  }
}
