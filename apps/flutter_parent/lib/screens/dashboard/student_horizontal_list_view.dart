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
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:flutter_parent/screens/pairing/pairing_util.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:provider/provider.dart';

class StudentHorizontalListView extends StatefulWidget {
  final List<User> _students;
  final Function onTap;
  final Function onAddStudent;

  StudentHorizontalListView(this._students, {this.onTap, this.onAddStudent});

  @override
  State<StatefulWidget> createState() => StudentHorizontalListViewState();
}

class StudentHorizontalListViewState extends State<StudentHorizontalListView> {
  @override
  Widget build(BuildContext context) {
    return ListView.separated(
        padding: EdgeInsets.symmetric(horizontal: 8),
        scrollDirection: Axis.horizontal,
        itemBuilder: (context, idx) =>
            idx < widget._students.length ? _studentWidget(widget._students[idx]) : _addWidget(),
        separatorBuilder: (context, idx) {
          return SizedBox(width: 8);
        },
        itemCount: widget._students.length + 1); // Add one for the 'Add Student' button
  }

  Widget _studentWidget(User student) {
    return GestureDetector(
      behavior: HitTestBehavior.translucent,
      onTap: () {
        ApiPrefs.updateCurrentLogin((b) => b..selectedStudentId = student.id);
        ParentTheme.of(context).setSelectedStudent(student.id);
        Provider.of<SelectedStudentNotifier>(context, listen: false).update(student);
        ApiPrefs.setCurrentStudent(student);
        widget.onTap();
      },
      child: Semantics(
        label: L10n(context).tapToSelectStudent,
        child: Center(
          child: Container(
            width: 120,
            height: 86,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                SizedBox(height: 8),
                Container(
                  child: Avatar.fromUser(student, radius: 24),
                  decoration: BoxDecoration(
                      boxShadow: [BoxShadow(color: const Color(0x1E000000), offset: Offset(1.5, 1.5), blurRadius: 4)],
                      borderRadius: BorderRadius.all(Radius.circular(40))),
                ),
                SizedBox(height: 8),
                Text(
                  student.shortName,
                  key: Key("${student.shortName}_text"),
                  style: Theme.of(context).textTheme.subtitle2.copyWith(color: ParentTheme.of(context).onSurfaceColor),
                  overflow: TextOverflow.ellipsis,
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _addWidget() {
    return Center(
      child: Container(
        width: 120,
        height: 92,
        child: Column(
          children: <Widget>[
            SizedBox(height: 12),
            Container(
              width: 48,
              height: 48,
              child: RaisedButton(
                padding: EdgeInsets.zero,
                color: Theme.of(context).scaffoldBackgroundColor,
                child: Semantics(
                  label: L10n(context).tapToPairNewStudent,
                  child: Icon(
                    Icons.add,
                    color: Theme.of(context).accentColor,
                  ),
                ),
                shape: CircleBorder(
                  side: BorderSide(
                      color: ParentTheme.of(context).isDarkMode ? Theme.of(context).accentColor : Colors.white,
                      width: 1),
                ),
                elevation: 8,
                onPressed: () {
                  locator<PairingUtil>().pairNewStudent(context, () => widget.onAddStudent());
                },
              ),
            ),
            SizedBox(height: 8),
            Text(
              L10n(context).addStudent,
              style: Theme.of(context).textTheme.subtitle2.copyWith(color: ParentTheme.of(context).onSurfaceColor),
            ),
          ],
        ),
      ),
    );
  }
}
