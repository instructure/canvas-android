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
import 'package:intl/intl.dart';

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
                ..._getPointsOrStatus(context, _item),
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
        Text(plannerItem.plannable.dueAt.l10nFormat(L10n(context).dueDateAtTime),
            style: Theme.of(context).textTheme.caption),
      ];
    }
    return [];
  }

  List<Widget> _getPointsOrStatus(BuildContext context, PlannerItem plannerItem) {
    var submissionStatus = plannerItem.submissionStatus;
    String pointsOrStatus = null;
    String semanticLabel = null;
    // Submission status can be null for non-assignment contexts like announcements
    if (submissionStatus != null) {
      if (submissionStatus.excused) {
        pointsOrStatus = L10n(context).excused;
      } else if (submissionStatus.missing) {
        pointsOrStatus = L10n(context).missing;
      } else if (submissionStatus.graded) {
        pointsOrStatus = L10n(context).assignmentGradedLabel;
      } else if (submissionStatus.needsGrading) {
        pointsOrStatus = L10n(context).assignmentSubmittedLabel;
      } else if (plannerItem.plannable.pointsPossible != null) {
        // We don't have a status, but we should have points
        String score = NumberFormat.decimalPattern().format(plannerItem.plannable.pointsPossible);
        pointsOrStatus = L10n(context).assignmentTotalPoints(score);
        semanticLabel = L10n(context).pointsPossible(score);
      }
    }

    // Don't show this row if it doesn't have a score or status (e.g. announcement)
    if (pointsOrStatus != null) {
      return [
        SizedBox(height: 4),
        Text(
          pointsOrStatus,
          style: Theme.of(context).textTheme.caption.copyWith(color: Theme.of(context).accentColor),
          semanticsLabel: semanticLabel,
        ),
      ];
    }

    return [];
  }
}
