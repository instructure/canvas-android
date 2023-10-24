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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_screen.dart';
import 'package:flutter_parent/screens/manage_students/student_color_picker_dialog.dart';
import 'package:flutter_parent/screens/pairing/pairing_util.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';

import 'manage_students_interactor.dart';

/// It is assumed that this page will not be deep linked, so
/// the list of students that it needs should be passed in.
///
/// Pull to refresh and updating when a pairing code is used are handled, however.
class ManageStudentsScreen extends StatefulWidget {
  final List<User>? _students;

  ManageStudentsScreen(this._students, {super.key});

  @override
  State<StatefulWidget> createState() => _ManageStudentsState();
}

class _ManageStudentsState extends State<ManageStudentsScreen> {
  Future<List<User>?>? _studentsFuture;
  Future<List<User>?> _loadStudents() => locator<ManageStudentsInteractor>().getStudents(forceRefresh: true);

  GlobalKey<RefreshIndicatorState> _refreshKey = GlobalKey<RefreshIndicatorState>();

  // Used to tell the Dashboard screen if it needs to update its list of students
  bool _addedStudentFlag = false;

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () {
        Navigator.pop(context, _addedStudentFlag);
        return Future.value(false);
      },
      child: DefaultParentTheme(
        useNonPrimaryAppBar: false,
        builder: (context) => Scaffold(
          appBar: AppBar(
            title: Text(L10n(context).manageStudents),
            bottom: ParentTheme.of(context)?.appBarDivider(),
          ),
          body: FutureBuilder(
            initialData: widget._students,
            future: _studentsFuture,
            builder: (context, AsyncSnapshot<List<User>?> snapshot) {
              // No wait view - users should be passed in on init, and the refresh indicator should handle the pull to refresh

              // Get the view based on the state of the snapshot
              Widget view;
              if (snapshot.hasError) {
                view = _error(context);
              } else if (snapshot.data == null || snapshot.data!.isEmpty) {
                view = _empty(context);
              } else {
                view = _StudentsList(snapshot.data!);
              }

              return RefreshIndicator(
                key: _refreshKey,
                onRefresh: _refresh,
                child: view,
              );
            },
          ),
          floatingActionButton: _createFloatingActionButton(context),
        ),
      ),
    );
  }

  Widget _StudentsList(List<User> students) {
    return ListView.builder(
      itemCount: students.length,
      itemBuilder: (context, index) => ListTile(
        contentPadding: const EdgeInsets.fromLTRB(16.0, 12.0, 8.0, 12.0),
        leading: Hero(
          tag: 'studentAvatar${students[index].id}',
          child: Avatar(students[index].avatarUrl, name: students[index].shortName),
        ),
        title: Hero(
          tag: 'studentText${students[index].id}',
          key: ValueKey('studentTextHero${students[index].id}'),
          child: UserName.fromUserShortName(
            students[index],
            style: Theme.of(context).textTheme.titleMedium,
          ),
        ),
        onTap: () async {
          var needsRefresh = await locator.get<QuickNav>().push(context, AlertThresholdsScreen(students[index]));
          if (needsRefresh == true) {
            _refreshKey.currentState?.show();
            _addedStudentFlag = true;
          }
        },
        trailing: FutureBuilder<StudentColorSet>(
          future: ParentTheme.of(context)?.getColorsForStudent(students[index].id),
          builder: (context, snapshot) {
            var color = snapshot.hasData
                ? ParentTheme.of(context)?.getColorVariantForCurrentState(snapshot.data!) ?? Colors.transparent
                : Colors.transparent;
            return Semantics(
              container: true,
              label: L10n(context).changeStudentColorLabel(students[index].shortName ?? ''),
              child: InkResponse(
                highlightColor: color,
                onTap: () {
                  showDialog(
                    context: context,
                    builder: (context) => StudentColorPickerDialog(
                      initialColor: color,
                      studentId: students[index].id,
                    ),
                  );
                },
                child: Container(
                  width: 48,
                  height: 48,
                  padding: EdgeInsets.all(10),
                  child: Center(
                    child: Container(
                      key: Key('color-circle-${students[index].id}'),
                      width: 20,
                      height: 20,
                      decoration: BoxDecoration(color: color, shape: BoxShape.circle),
                    ),
                  ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }

  Widget _error(BuildContext context) {
    return ErrorPandaWidget(L10n(context).errorLoadingStudents, () {
      _refresh();
    });
  }

  Widget _empty(BuildContext context) {
    return EmptyPandaWidget(
      svgPath: 'assets/svg/panda-manage-students.svg',
      subtitle: L10n(context).emptyStudentList,
      buttonText: L10n(context).retry,
      onButtonTap: () => _refresh(),
    );
  }

  Widget _createFloatingActionButton(BuildContext context) {
    return FloatingActionButton(
      child: Icon(
        Icons.add,
        semanticLabel: L10n(context).addNewStudent,
      ),
      onPressed: () {
        locator<PairingUtil>().pairNewStudent(
          context,
          () {
            _refreshKey.currentState?.show();
            _addedStudentFlag = true;
          },
        );
      },
    );
  }

  /// Force widget to reload with a refreshed future
  Future<void> _refresh() {
    setState(() {
      _studentsFuture = _loadStudents();
    });
    return _studentsFuture!.catchError((_) {});
  }
}
