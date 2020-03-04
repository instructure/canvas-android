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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';

class CalendarDayListTile extends StatelessWidget {
  final PlannerItem _item;

  CalendarDayListTile(this._item);

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    Widget tile = InkWell(
      onTap: () {
        // TODO: route stuff
      },
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          SizedBox(width: 18),
          Padding(
            padding: const EdgeInsets.only(top: 14),
            child: _getIcon(context, _item),
          ),
          SizedBox(width: 34),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                SizedBox(height: 16),
                Text(_item.contextName, style: textTheme.caption),
                SizedBox(height: 2),
                Text(_item.plannable.title, style: textTheme.subhead),
                ..._getDueDate(context, _item),
                Text(
                  L10n(context).assignmentTotalPoints(_item.plannable.pointsPossible.toString()),
                  style: textTheme.caption.copyWith(color: Theme.of(context).accentColor),
                  semanticsLabel: L10n(context).pointsPossible(_item.plannable.pointsPossible.toString()),
                ),
                SizedBox(height: 12),
              ],
            ),
          ),
          SizedBox(width: 16),
        ],
      ),
    );

    return tile;
  }

  Widget _getIcon(BuildContext context, PlannerItem item) {
    IconData icon;
    switch (item.plannableType) {
      case 'assignment':
        icon = CanvasIcons.assignment;
        break;
      case 'quiz':
        icon = CanvasIcons.quiz;
        break;
      case 'announcement':
        icon = CanvasIcons.announcement;
        break;
      case 'calendar_event':
        icon = CanvasIcons.calendar_day;
        break;
    }
    return Icon(icon, size: 20, semanticLabel: '', color: Theme.of(context).accentColor);
  }

  List<Widget> _getDueDate(BuildContext context, PlannerItem plannerItem) {
    if (plannerItem.plannable.dueAt != null) {
      return [
        SizedBox(height: 4),
        Text(L10n(context).due(_formatDate(context, plannerItem.plannable.dueAt)),
            style: Theme.of(context).textTheme.caption),
      ];
    }
    return [];
  }

  String _formatDate(BuildContext context, DateTime date) {
    return date?.l10nFormat(L10n(context).dateAtTime) ?? '';
  }
}
