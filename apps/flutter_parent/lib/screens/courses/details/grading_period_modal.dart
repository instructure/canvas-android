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
import 'package:flutter_parent/models/grading_period.dart';

class GradingPeriodModal extends StatelessWidget {
  final List<GradingPeriod> gradingPeriods;

  const GradingPeriodModal._internal({required this.gradingPeriods, super.key});

  static Future<GradingPeriod?> asBottomSheet(BuildContext context, List<GradingPeriod> gradingPeriods) =>
      showModalBottomSheet(
        context: context,
        builder: (context) => GradingPeriodModal._internal(gradingPeriods: gradingPeriods),
      );

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      shrinkWrap: true,
      itemCount: gradingPeriods.length + 1, // Add one for the header
      itemBuilder: (context, index) {
        if (index == 0) {
          return Padding(
            padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
            child: Text(L10n(context).filterBy, style: Theme.of(context).textTheme.bodySmall),
          );
        }
        final gradingPeriod = gradingPeriods[index - 1];
        return ListTile(
          title: Text(gradingPeriod.title!, style: Theme.of(context).textTheme.titleMedium),
          onTap: () => Navigator.of(context).pop(gradingPeriod),
        );
      },
    );
  }
}
