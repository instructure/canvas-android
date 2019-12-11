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
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/full_screen_scroll_container.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/service_locator.dart';

import 'add_student_dialog.dart';
import 'manage_students_interactor.dart';

/// It is assumed that this page will not be deep linked, so
/// the list of students that it needs should be passed in.
///
/// Pull to refresh and updating when a pairing code is used are handled, however.
class ManageStudentsScreen extends StatefulWidget {
  final _interactor = locator<ManageStudentsInteractor>();
  final List<User> _students;

  ManageStudentsScreen(this._students, {Key key}) : super(key: key);

  @override
  State<StatefulWidget> createState() => _ManageStudentsState();
}

class _ManageStudentsState extends State<ManageStudentsScreen> {
  Future<List<User>> _studentsFuture;
  Future<List<User>> _loadStudents() => widget._interactor.getStudents(forceRefresh: true);

  @override
  void initState() {
    super.initState();
    _studentsFuture = Future.value(widget._students);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(AppLocalizations.of(context).manageStudents),
      ),
      body: FutureBuilder(
          future: _studentsFuture,
          builder: (context, AsyncSnapshot<List<User>> snapshot) {
            // Handle waiting state
            if (snapshot.connectionState == ConnectionState.waiting) return LoadingIndicator();

            // Get the view based on the state of the snapshot
            Widget view;
            if (snapshot.hasError) {
              view = _error(context);
            } else if (snapshot.data == null || snapshot.data.isEmpty) {
              view = _empty(context);
            } else {
              view = _StudentsList(snapshot.data);
            }

            return RefreshIndicator(
              onRefresh: () {
                _refresh();
                return _studentsFuture;
              },
              child: view,
            );
          }),
      floatingActionButton: _createFloatingActionButton(context),
    );
  }

  Widget _StudentsList(List<User> students) {
    return ListView.builder(
      itemCount: students.length,
      itemBuilder: (context, index) => Padding(
        padding: const EdgeInsets.fromLTRB(8.0, 16.0, 8.0, 0),
        child: ListTile(
          leading: Hero(
            tag: 'studentAvatar${students[index].id}',
            child: Avatar(students[index].avatarUrl, name: students[index].shortName),
          ),
          title: Hero(
            tag: 'studentText${students[index].id}',
            child: GestureDetector(
                child: UserName.fromUser(
                  students[index],
                  style: Theme.of(context).textTheme.subhead,
                ),
                onTap: () {
                  // TODO: Tapping on a user
//                  locator.get<QuickNav>().push(context, StudentSettingsScreen(students[index]));
                }),
          ),
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
    return FullScreenScrollContainer(
      children: [
        Text(AppLocalizations.of(context).emptyStudentList),
        RaisedButton(
            onPressed: () {
              _refresh();
            },
            child: Text(AppLocalizations.of(context).retry))
      ],
    );
  }

  /// Dialog for pairing with a new student
  /// Optional [pairingCode] for QR reader results
  Future<bool> _addStudentDialog(BuildContext context, {String pairingCode}) async {
    return showDialog<bool>(
        context: context,
        barrierDismissible: true,
        builder: (BuildContext context) {
          return AddStudentDialog(pairingCode);
        });
  }

  Widget _createFloatingActionButton(BuildContext context) {
    return FloatingActionButton(
      child: Icon(
        Icons.add,
        semanticLabel: AppLocalizations.of(context).addNewStudent,
      ),
      onPressed: () async {
        showModalBottomSheet(
            context: context,
            isScrollControlled: true,
            builder: (context) {
              return Row(
                mainAxisSize: MainAxisSize.max,
                children: <Widget>[
                  Expanded(
                    child: Padding(
                      padding: const EdgeInsets.fromLTRB(16.0, 8.0, 16.0, 10.0),
                      child: Wrap(
                        direction: Axis.vertical,
                        children: <Widget>[
                          Container(
                            height: 40,
                            alignment: Alignment.centerLeft,
                            child: Text(
                              AppLocalizations.of(context).addStudentWith,
                              style: Theme.of(context).textTheme.caption,
                            ),
                          ),
                          _qrCode(),
                          _pairingCode(),
                        ],
                      ),
                    ),
                  ),
                ],
              );
            });
      },
    );
  }

  Widget _qrCode() {
    return GestureDetector(
      behavior: HitTestBehavior.translucent,
      child: Container(
        width: 10000,
        height: 48,
        alignment: Alignment.centerLeft,
        child: Text(
          AppLocalizations.of(context).qrCode,
          style: Theme.of(context).textTheme.body1.copyWith(fontSize: 16),
        ),
      ),
      onTap: () async {
        Navigator.of(context).pop();
        String cameraScanResult = await widget._interactor.getQrReading();
        if (cameraScanResult != null && cameraScanResult.isNotEmpty) {
          bool studentPaired = await _addStudentDialog(context, pairingCode: cameraScanResult);
          if (studentPaired) {
            _refresh();
          }
        }
      },
    );
  }

  Widget _pairingCode() {
    return GestureDetector(
      behavior: HitTestBehavior.translucent,
      child: InkWell(
        child: Container(
          width: 10000,
          height: 48,
          alignment: Alignment.centerLeft,
          child: Text(
            AppLocalizations.of(context).pairingCode,
            style: Theme.of(context).textTheme.body1.copyWith(fontSize: 16),
          ),
        ),
      ),
      onTap: () async {
        Navigator.of(context).pop();
        bool studentPaired = await _addStudentDialog(context);
        if (studentPaired) {
          _refresh();
        }
      },
    );
  }

  /// Force widget to reload with a refreshed future
  void _refresh() {
    _studentsFuture = _loadStudents();
//    setState(() {});
  }
}
