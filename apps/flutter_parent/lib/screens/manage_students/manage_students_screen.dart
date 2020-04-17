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
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_screen.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
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
  Future<List<User>> _loadStudents() => locator<ManageStudentsInteractor>().getStudents(forceRefresh: true);

  GlobalKey<RefreshIndicatorState> _refreshKey = GlobalKey<RefreshIndicatorState>();

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      useNonPrimaryAppBar: false,
      builder: (context) => Scaffold(
        appBar: AppBar(
          title: Text(L10n(context).manageStudents),
          bottom: ParentTheme.of(context).appBarDivider(),
        ),
        body: FutureBuilder(
          initialData: widget._students,
          future: _studentsFuture,
          builder: (context, AsyncSnapshot<List<User>> snapshot) {
            // No wait view - users should be passed in on init, and the refresh indicator should handle the pull to refresh

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
              key: _refreshKey,
              onRefresh: _refresh,
              child: view,
            );
          },
        ),
        floatingActionButton: _createFloatingActionButton(context),
      ),
    );
  }

  Widget _StudentsList(List<User> students) {
    return ListView.builder(
      itemCount: students.length,
      itemBuilder: (context, index) => ListTile(
        contentPadding: const EdgeInsets.fromLTRB(16.0, 12.0, 16.0, 12.0),
        leading: Hero(
          tag: 'studentAvatar${students[index].id}',
          child: Avatar(students[index].avatarUrl, name: students[index].shortName),
        ),
        title: Hero(
          tag: 'studentText${students[index].id}',
          child: UserName.fromUserShortName(
            students[index],
            style: Theme.of(context).textTheme.subhead,
          ),
        ),
        onTap: () {
          locator.get<QuickNav>().push(context, AlertThresholdsScreen(students[index]));
        },
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

  /// Dialog for pairing with a new student
  /// Optional [pairingCode] for QR reader results
  Future<bool> _addStudentDialog(BuildContext context, {String pairingCode = ''}) async {
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
        semanticLabel: L10n(context).addNewStudent,
      ),
      onPressed: () async {
        _showAddStudentDialog();
        // TODO: Uncomment when we're ready with a QR code reader lib
        /*showModalBottomSheet(
            context: context,
            isScrollControlled: true,
            builder: (context) {
              return Row(
                mainAxisSize: MainAxisSize.max,
                children: <Widget>[
                  Expanded(
                    child: Padding(
                      padding: const EdgeInsets.fromLTRB(0, 8.0, 0, 10.0),
                      child: Wrap(
                        direction: Axis.vertical,
                        children: <Widget>[
                          Container(
                            height: 40,
                            alignment: Alignment.centerLeft,
                            child: Padding(
                              padding: const EdgeInsets.symmetric(horizontal: 16.0),
                              child: Text(
                                L10n(context).addStudentWith,
                                style: Theme.of(context).textTheme.caption,
                              ),
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
            });*/
      },
    );
  }

  Widget _qrCode() {
    return InkWell(
        child: Container(
          width: 10000,
          height: 48,
          alignment: Alignment.centerLeft,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: Text(
              L10n(context).qrCode,
              style: Theme.of(context).textTheme.body1.copyWith(fontSize: 16),
            ),
          ),
        ),
        onTap: () async {
          Navigator.of(context).pop();
          // ignore: unused_local_variable, will use when we get the QR reader in place
          String cameraScanResult = await widget._interactor.getQrReading();
//          print('Camera results: $cameraScanResult');
//          if (cameraScanResult != null && cameraScanResult.isNotEmpty && cameraScanResult != '-1') {
//            bool studentPaired = await _addStudentDialog(context, pairingCode: cameraScanResult);
//            if (studentPaired) {
//              _refresh();
//            }
//          }
        });
  }

//  padding: const EdgeInsets.fromLTRB(16.0, 8.0, 16.0, 10.0),
  Widget _pairingCode() {
    return InkWell(
        child: Container(
          width: 10000,
          height: 48,
          alignment: Alignment.centerLeft,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            child: Text(
              L10n(context).pairingCode,
              style: Theme.of(context).textTheme.body1.copyWith(fontSize: 16),
            ),
          ),
        ),
        onTap: () async {
          Navigator.of(context).pop();
          _showAddStudentDialog();
        });
  }

  void _showAddStudentDialog() async {
    locator<Analytics>().logEvent(AnalyticsEventConstants.ADD_STUDENT_MANAGE_STUDENTS);
    bool studentPaired = await _addStudentDialog(context);
    if (studentPaired) {
      _refreshKey.currentState.show();
    }
  }

  /// Force widget to reload with a refreshed future
  Future<void> _refresh() {
    setState(() {
      _studentsFuture = _loadStudents();
    });
    return _studentsFuture.catchError((_) {});
  }
}
