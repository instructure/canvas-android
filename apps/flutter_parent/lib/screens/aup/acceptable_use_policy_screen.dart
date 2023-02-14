// Copyright (C) 2023 - present Instructure, Inc.
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
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';

class AcceptableUsePolicyScreen extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _AcceptableUsePolicyState();
}

class _AcceptableUsePolicyState extends State<AcceptableUsePolicyScreen> {
  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
        builder: (context) => Scaffold(
              appBar: AppBar(
                title: Text(L10n(context).acceptableUsePolicyTitle),
                leading: IconButton(
                  icon: Icon(CanvasIcons.x),
                  onPressed: () => _close(),
                ),
                actions: [
                  TextButton(
                      onPressed: () => _confirm,
                      child: Text(L10n(context).acceptableUsePolicyConfirm))
                ],
              ),
              body: Column(
                children: [
                  Divider(),
                  Container(
                      padding:
                          EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                      child: Text(
                        L10n(context).acceptableUsePolicyDescription,
                        style: TextStyle(
                            color: ParentColors.licorice, fontSize: 16),
                      )),
                  Divider(),
                  FlatButton(
                      onPressed: _readPolicy,
                      padding:
                          EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                      child: Row(
                        children: [
                          Text(L10n(context).acceptableUsePolicyTitle,
                              style: TextStyle(
                                  color: ParentColors.licorice, fontSize: 16)),
                          Spacer(),
                          Icon(
                            CanvasIcons.arrow_open_right,
                            color: ParentColors.licorice,
                            size: 12,
                          )
                        ],
                      )),
                  Divider(),
                  Container(
                    padding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    child: Row(
                      children: [
                        Text(
                          L10n(context).acceptableUsePolicyAgree,
                          style: TextStyle(
                              color: ParentColors.licorice, fontSize: 16),
                        ),
                        Spacer(),
                        Switch(
                            value: false,
                            onChanged: (isEnabled) =>
                                _onSwitchChanged(isEnabled))
                      ],
                    ),
                  ),
                  Divider()
                ],
              ),
            ));
  }

  void _close() {}

  void _confirm() {}

  void _readPolicy() {}

  void _onSwitchChanged(bool isEnabled) {}
}
