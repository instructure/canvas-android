/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:flutter/material.dart';

import 'canvas_web_view.dart';

class CanvasHtml extends StatelessWidget {
  final String content;
  final String emptyDescription;

  const CanvasHtml(this.content, {this.emptyDescription, Key key})
      : assert(content != null),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      physics: AlwaysScrollableScrollPhysics(),
      child: Padding(
        padding: const EdgeInsets.only(top: 16.0),
        child: CanvasWebView(
          content: content,
          emptyDescription: emptyDescription,
          horizontalPadding: 16,
          fullScreen: false,
        ),
      ),
    );
  }
}
