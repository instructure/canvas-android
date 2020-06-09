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
import 'package:flutter/widgets.dart';
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/models/planner_item.dart';
import 'package:flutter_student_embed/network/api/planner_api.dart';
import 'package:flutter_student_embed/screens/to_do/create_update_to_do_screen.dart';
import 'package:flutter_student_embed/utils/common_widgets/appbar_dynamic_style.dart';
import 'package:flutter_student_embed/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_student_embed/utils/design/student_colors.dart';
import 'package:flutter_student_embed/utils/design/student_theme.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';
import 'package:flutter_student_embed/utils/service_locator.dart';

class ToDoDetailsScreen extends StatelessWidget {
  final PlannerItem toDo;

  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();

  ToDoDetailsScreen(this.toDo, {Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    Color contextColor = StudentTheme.of(context).getCanvasContextColor(toDo.contextCode());
    return Scaffold(
      key: _scaffoldKey,
      appBar: dynamicStyleAppBar(
        context: context,
        appBar: AppBar(
          title: Text(L10n(context).toDo),
          actions: <Widget>[
            PopupMenuButton<int>(
              itemBuilder: (context) {
                return [
                  PopupMenuItem(value: 0, child: Text(L10n(context).edit)),
                  PopupMenuItem(value: 1, child: Text(L10n(context).delete)),
                ];
              },
              onSelected: (option) {
                if (option == 0) _edit(context);
                if (option == 1) _delete(context);
              },
            ),
          ],
        ),
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              Text(
                toDo.plannable.title,
                style: Theme.of(context).textTheme.display1,
              ),
              if (toDo.contextName != null)
                Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text(
                    toDo.contextName,
                    style: Theme.of(context).textTheme.caption.copyWith(color: contextColor),
                  ),
                ),
              Divider(height: 32),
              Text(
                L10n(context).date,
                style: Theme.of(context).textTheme.overline,
              ),
              SizedBox(height: 8),
              Text(
                toDo.plannable.toDoDate.l10nFormat(L10n(context).dateAtTime),
                style: Theme.of(context).textTheme.subhead,
              ),
              Divider(height: 32),
              Text(
                L10n(context).descriptionLabel,
                style: Theme.of(context).textTheme.overline,
              ),
              SizedBox(height: 8),
              if (toDo.plannable.details == null || toDo.plannable.details.isEmpty)
                Container(
                  width: double.infinity,
                  height: 72,
                  padding: EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: StudentTheme.of(context).nearSurfaceColor,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Center(
                    child: Text(
                      L10n(context).noToDoDescription,
                      style: Theme.of(context).textTheme.caption.copyWith(color: StudentColors.licorice),
                    ),
                  ),
                ),
              if (toDo.plannable.details != null) Text(toDo.plannable.details),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _edit(BuildContext context) async {
    var updatedDates = await locator<QuickNav>().push(context, CreateUpdateToDoScreen(editToDo: toDo));
    if (updatedDates != null) {
      // The planner API does not provide a way to got a single planner note with its surrounding PlannerItem
      // data (like context name), so rather than try cobble together updated information in this screen,
      // we will pop back to the calendar screen and have that refresh the specified dates.
      Navigator.pop(context, updatedDates);
    }
  }

  void _delete(BuildContext context) {
    bool deleting = false;
    bool error = false;
    showDialog(
      barrierDismissible: false,
      context: context,
      builder: (context) {
        return StatefulBuilder(builder: (context, updateState) {
          return AlertDialog(
            title: Text(L10n(context).areYouSure),
            content: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                Text(L10n(context).deleteToDoConfirmationMessage),
                if (error)
                  Padding(
                    padding: const EdgeInsets.only(top: 12),
                    child: Text(
                      L10n(context).errorDeletingToDo,
                      style: Theme.of(context).textTheme.subtitle.copyWith(color: StudentColors.failure),
                    ),
                  ),
              ],
            ),
            actions: <Widget>[
              if (deleting)
                FlatButton(
                  onPressed: null,
                  child: Container(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      valueColor: AlwaysStoppedAnimation<Color>(Theme.of(context).buttonColor),
                      strokeWidth: 2,
                    ),
                  ),
                ),
              if (!deleting)
                FlatButton(
                  child: Text(L10n(context).cancel.toUpperCase()),
                  onPressed: () => Navigator.pop(context),
                ),
              if (!deleting)
                FlatButton(
                  child: Text(L10n(context).delete.toUpperCase()),
                  onPressed: () async {
                    updateState(() {
                      deleting = true;
                      error = false;
                    });
                    try {
                      await locator<PlannerApi>().deletePlannerNote(toDo.plannable.id);

                      // Pop once to dismiss the dialog
                      Navigator.pop(context);

                      // Pop again to dismiss the details screen, returning the calendar date that must be updated
                      Navigator.pop(context, [toDo.plannable.toDoDate]);
                    } catch (e) {
                      updateState(() {
                        deleting = false;
                        error = true;
                      });
                    }
                  },
                ),
            ],
          );
        });
      },
    );
  }
}
