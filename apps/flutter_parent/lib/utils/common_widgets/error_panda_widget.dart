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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';

import 'full_screen_scroll_container.dart';

/// Displays the error page used throughout the app
/// Contains a warning icon, an error message, and a retry button.
///
/// Use the [callback] to set what happens when the retry button is pressed
/// Use the [loadTarget] to specify what was supposed to be loaded
///
/// For example:
/// loadTarget = 'your students'
///
/// Results in an error message of:
/// 'There was an error loading your students'
class ErrorPandaWidget extends StatelessWidget {
  final Function callback;
  final String loadTarget;

  ErrorPandaWidget(this.loadTarget, this.callback);

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        return SingleChildScrollView(
          physics: AlwaysScrollableScrollPhysics(),
          child: Container(
            height: constraints.maxHeight,
            child: Padding(
              padding: const EdgeInsets.all(8.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  Icon(CanvasIcons.warning, size: 40, color: ParentTheme.failure),
                  Padding(
                    padding: const EdgeInsets.fromLTRB(48, 28, 48, 32),
                    child: Text(
                      L10n(context).genericLoadingErrorMessage(loadTarget),
                      textAlign: TextAlign.center,
                      style: Theme.of(context).textTheme.caption.copyWith(fontSize: 16),
                    ),
                  ),
                  FlatButton(
                    onPressed: () {
                      callback();
                    },
                    child: Text(
                      L10n(context).retry,
                      style: Theme.of(context).textTheme.caption.copyWith(fontSize: 16),
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: new BorderRadius.circular(4.0),
                      side: BorderSide(color: ParentTheme.tiara),
                    ),
                  )
                ],
              ),
            ),
          ),
        );
      },
    );
  }

}