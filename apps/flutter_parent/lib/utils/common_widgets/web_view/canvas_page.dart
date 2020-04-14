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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/page.dart';

import 'canvas_web_view.dart';

class CanvasPage extends StatelessWidget {
  final Page homePage;

  const CanvasPage(this.homePage, {Key key})
      : assert(homePage != null),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      physics: AlwaysScrollableScrollPhysics(),
      child: Padding(
        padding: const EdgeInsets.only(top: 16.0),
        child: CanvasWebView(
          content: homePage.body,
          emptyDescription: homePage.lockExplanation ?? L10n(context).noPageFound,
          horizontalPadding: 16,
          fullScreen: false,
        ),
      ),
    );
  }
}
