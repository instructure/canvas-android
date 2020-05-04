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

import 'package:flutter/material.dart';
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/utils/common_widgets/full_screen_scroll_container.dart';
import 'package:flutter_student_embed/utils/design/canvas_icons.dart';
import 'package:flutter_student_embed/utils/design/student_colors.dart';

/// Displays the error page used throughout the app
/// Contains a warning icon, an error message, and a retry button.
///
/// Use the [callback] to set what happens when the retry button is pressed
/// Use the [errorString] to specify what was supposed to be loaded
class ErrorPandaWidget extends StatelessWidget {
  final Function callback;
  final String errorString;
  final Widget header;

  ErrorPandaWidget(this.errorString, this.callback, {this.header});

  @override
  Widget build(BuildContext context) {
    return FullScreenScrollContainer(
      header: header,
      children: <Widget>[
        Icon(CanvasIcons.warning, size: 40, color: StudentColors.failure),
        Padding(
          padding: const EdgeInsets.fromLTRB(48, 28, 48, 32),
          child: Text(
            errorString,
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.caption.copyWith(fontSize: 16),
          ),
        ),
        FlatButton(
          onPressed: () {
            callback();
          },
          child: Text(L10n(context).retry, style: Theme.of(context).textTheme.caption.copyWith(fontSize: 16)),
          shape: RoundedRectangleBorder(
            borderRadius: new BorderRadius.circular(4.0),
            side: BorderSide(color: StudentColors.tiara),
          ),
        )
      ],
    );
  }
}
