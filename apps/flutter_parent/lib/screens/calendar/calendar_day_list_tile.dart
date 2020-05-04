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
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class CalendarDayListTile extends StatelessWidget {
  final ScheduleItem _item;

  CalendarDayListTile(this._item);

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    Widget tile = InkWell(
      onTap: () {
        switch (_item.getItemType()) {
          case ScheduleItemType.assignment:
            locator<QuickNav>()
                .pushRoute(context, PandaRouter.assignmentDetails(_item.assignment.courseId, _item.assignment.id));
            break;
          case ScheduleItemType.quiz:
            if (_item.assignment.quizId != null) {
              // This is a quiz assignment, go to the assignment page
              locator<QuickNav>().pushRoute(
                  context, PandaRouter.quizAssignmentDetails(_item.assignment.courseId, _item.assignment.id));
            } else {
              // No routes will match this url currently, so routing internally will throw it in an implicit intent
              PandaRouter.routeInternally(context, ApiPrefs.getDomain() + _item.htmlUrl);
            }
            break;
          case ScheduleItemType.discussion:
            // TODO - do we have to handle non-assignment discussions from the calendar? Perhaps ungrade discussions?
//            locator<QuickNav>().pushRoute(context, PandaRouter.discussionDetails(_item.courseId, _item.plannable.id));
            break;
          case ScheduleItemType.event:
            // Case where the observed user has a personal calendar event
            locator<QuickNav>().pushRoute(context, PandaRouter.eventDetails(_item.getContextId(), _item.id));
            break;
          default:
            // This is a type that we don't handle - do nothing
            break;
        }
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
                Text('PLACEHOLDER COURSE NAME',
                    style: textTheme.caption), //_getContextName(context, _item), style: textTheme.caption),
                SizedBox(height: 2),
                Text(_item.title, style: textTheme.subhead),
                ..._getDueDate(context, _item),
                // TODO - add assignment state work here ..._getPointsOrStatus(context, _item),
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

  String _getContextName(BuildContext context, PlannerItem item) {
    if (item.contextName != null) return item.contextName;

    // Planner notes don't have a context name so we'll use 'Planner Note'
    if (item.plannableType == 'planner_note') return L10n(context).plannerNote;

    return '';
  }

  Widget _getIcon(BuildContext context, ScheduleItem item) {
    IconData icon;
    switch (item.getItemType()) {
      case ScheduleItemType.assignment:
        icon = CanvasIcons.assignment;
        break;
      case ScheduleItemType.quiz:
        icon = CanvasIcons.quiz;
        break;
      case ScheduleItemType.discussion:
        icon = CanvasIcons.discussion;
        break;
      case ScheduleItemType.event:
        icon = CanvasIcons.calendar_day;
        break;
    }
    return Icon(icon, size: 20, semanticLabel: '', color: Theme.of(context).accentColor);
  }

  List<Widget> _getDueDate(BuildContext context, ScheduleItem plannerItem) {
    if (plannerItem.assignment.dueAt != null) {
      return [
        SizedBox(height: 4),
        Text(plannerItem.assignment.dueAt.l10nFormat(L10n(context).dueDateAtTime),
            style: Theme.of(context).textTheme.caption),
      ];
    }
    return [];
  }

  /*
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
   */
}
