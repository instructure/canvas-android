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

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/grade_cell_data.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:percent_indicator/circular_percent_indicator.dart';

class GradeCell extends StatelessWidget {
  final GradeCellData data;

  const GradeCell(this.data, {Key key}) : super(key: key);

  GradeCell.forSubmission(
    BuildContext context,
    Assignment assignment,
    Submission submission, {
    Key key,
  })  : data = GradeCellData.forSubmission(
          assignment,
          submission,
          Theme.of(context),
          L10n(context),
        ),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    if (data.state == GradeCellState.empty)
      return Container(
        key: Key('grade-cell-empty-container'),
      );
    return MergeSemantics(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Divider(),
          SizedBox(height: 8),
          Text(L10n(context).assignmentGradeLabel, style: Theme.of(context).textTheme.overline),
          SizedBox(height: 8),
          data.state == GradeCellState.submitted ? _submitted(context, data) : _graded(context, data),
          SizedBox(height: 8),
        ],
      ),
    );
  }

  Widget _submitted(BuildContext context, GradeCellData data) {
    return Center(
      key: Key('grade-cell-submitted-container'),
      child: Column(
        children: <Widget>[
          Text(
            L10n(context).submissionStatusSuccessTitle,
            style: Theme.of(context).textTheme.headline.copyWith(color: ParentTheme.of(context).successColor),
          ),
          SizedBox(height: 6),
          Text(data.submissionText, textAlign: TextAlign.center),
        ],
      ),
    );
  }

  Widget _graded(BuildContext context, GradeCellData data) {
    return Row(
      key: Key('grade-cell-graded-container'),
      children: <Widget>[
        Stack(
          alignment: Alignment.center,
          children: <Widget>[
            CircularPercentIndicator(
              radius: 128,
              progressColor: data.accentColor,
              backgroundColor: ParentTheme.of(context).nearSurfaceColor,
              percent: data.graphPercent,
              lineWidth: 3,
              animation: true,
            ),
            Column(
              children: <Widget>[
                if (data.score.isNotEmpty)
                  Container(
                    constraints: BoxConstraints(maxWidth: 96),
                    child: FittedBox(
                      fit: BoxFit.contain,
                      child: Text(
                        data.score,
                        key: Key('grade-cell-score'),
                        style: TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
                      ),
                    ),
                  ),
                if (data.showPointsLabel)
                  Padding(
                    padding: const EdgeInsets.only(top: 2),
                    child: Text(
                      'Points',
                      key: Key('grade-cell-points-label'),
                    ),
                  ),
              ],
            ),
            if (data.showCompleteIcon)
              Icon(
                Icons.check,
                key: Key('grade-cell-complete-icon'),
                size: 64,
                color: data.accentColor,
              ),
            if (data.showIncompleteIcon)
              Icon(
                Icons.clear,
                key: Key('grade-cell-incomplete-icon'),
                size: 64,
                color: data.accentColor,
              ),
          ],
        ),
        SizedBox(width: 16),
        Expanded(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              if (data.grade.isNotEmpty)
                Text(
                  data.grade,
                  key: Key('grade-cell-grade'),
                  style: Theme.of(context).textTheme.display1,
                  semanticsLabel: data.gradeContentDescription,
                ),
              if (data.outOf.isNotEmpty) Text(data.outOf, key: Key('grade-cell-out-of')),
              if (data.latePenalty.isNotEmpty)
                Text(
                  data.latePenalty,
                  style: TextStyle(color: data.accentColor),
                  key: Key('grade-cell-late-penalty'),
                ),
              if (data.finalGrade.isNotEmpty)
                Padding(
                  padding: const EdgeInsets.only(top: 8),
                  child: Text(
                    data.finalGrade,
                    key: Key('grade-cell-final-grade'),
                    style: Theme.of(context).textTheme.subhead,
                  ),
                ),
            ],
          ),
        ),
      ],
    );
  }
}
