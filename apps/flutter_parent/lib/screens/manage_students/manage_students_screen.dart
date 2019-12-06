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
import 'package:flutter_parent/utils/common_widgets/full_screen_scroll_container.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:qrscan/qrscan.dart' as scanner;

import 'manage_students_interactor.dart';

/// It is assumed that this page will not be deep linked, so
/// the list of students that it needs should be passed in.
///
/// Pull to refresh and updating when a pairing code is used will be handled, however.
class ManageStudentsScreen extends StatefulWidget {
  final _interactor = locator<ManageStudentsInteractor>();
  final List<User> _students;

  ManageStudentsScreen(this._students, {Key key}) : super(key: key);

  @override
  State<StatefulWidget> createState() => ManageStudentsState();
}

class ManageStudentsState extends State<ManageStudentsScreen> {
  Future<List<User>> _studentsFuture;

  Future<List<User>> _loadStudents() => widget._interactor.getStudents();

  @override
  void initState() {
    _studentsFuture = Future.value(widget._students);
    super.initState();
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
                  _studentsFuture = _loadStudents();

                  // Force widget to reload with the new future
                  setState(() {});

                  return _studentsFuture;
                },
                child: view);
          }),
      floatingActionButton: _createFloatingActionButton(context),
    );
  }

  Widget _error(BuildContext context) {
    return FullScreenScrollContainer(children: [
      Center(
          child: Column(
        children: <Widget>[
          Icon(
            CanvasIcons.warning,
            color: Colors.red,
            size: 40.0,
          ),
          SizedBox(
            height: 28,
          ),
          Text(AppLocalizations.of(context).errorLoadingStudents),
          SizedBox(height: 32),
          GestureDetector(
            child: Material(
              child: Container(
                  decoration: BoxDecoration(
                    border: Border.all(
                      width: 1,
                      color: Color(0xFFC7CDD1),
                    ),
                    borderRadius: BorderRadius.all(Radius.circular(4)),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 40, vertical: 15),
                    child: Text(AppLocalizations.of(context).retry,
                        style: Theme.of(context).textTheme.subhead.copyWith(color: ParentTheme.ash)),
                  )),
            ),
            onTap: () {
              _studentsFuture = _loadStudents();
              setState(() {});
            },
          ),
        ],
      ))
    ]);
  }

  Widget _empty(BuildContext context) {
    return FullScreenScrollContainer(
      children: [
        Text(AppLocalizations.of(context).unexpectedError),
        RaisedButton(
            onPressed: () {
              _studentsFuture = _loadStudents();
              setState(() {});
            },
            child: Text(AppLocalizations.of(context).retry))
      ],
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
            child: CircleAvatar(
              radius: 20.0,
              backgroundImage: NetworkImage(students[index].avatarUrl ?? ''),
            ),
          ),
          title: Hero(
            tag: 'studentText${students[index].id}',
            child: GestureDetector(
                child: Material(
                  child: Text(
                    students[index].name,
                    style: Theme.of(context).textTheme.subhead,
                  ),
                ),
                // TODO: Tapping on a user
                onTap: () {
//                  locator.get<QuickNav>().push(context, StudentSettingsScreen(students[index]));
                }),
          ),
        ),
      ),
    );
  }

  Future<String> _addChildDialog(BuildContext context) async {
    String _pairingCode = '';

    return showDialog<String>(
        context: context,
        barrierDismissible: true,
        builder: (BuildContext context) {
          return AlertDialog(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8.0)),
            title: Text(AppLocalizations.of(context).addStudent),
            content:
                Column(mainAxisAlignment: MainAxisAlignment.center, mainAxisSize: MainAxisSize.min, children: <Widget>[
              Padding(
                padding: const EdgeInsets.only(bottom: 20.0),
                child: Text(
                  AppLocalizations.of(context).pairingCodeEntryExplanation,
                  style: Theme.of(context).textTheme.body1.copyWith(fontSize: 12.0),
                ),
              ),
              TextField(
                autofocus: true,
                autocorrect: false,
                onChanged: (value) {
                  _pairingCode = value;
                },
                decoration: InputDecoration(
                  hintText: AppLocalizations.of(context).pairingCode,
                  hintStyle: TextStyle(color: Colors.grey),
                  contentPadding: EdgeInsets.only(bottom: 2),
                ),
              ),
            ]),
            actions: <Widget>[
              FlatButton(
                onPressed: () {
                  Navigator.of(context).pop('');
                },
                child: Text(AppLocalizations.of(context).cancel.toUpperCase()),
              ),
              FlatButton(
                onPressed: () {
                  Navigator.of(context).pop(_pairingCode);
                },
                child: Text(AppLocalizations.of(context).ok),
              ),
            ],
          );
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
                      padding: const EdgeInsets.fromLTRB(16.0, 20.0, 16.0, 24.0),
                      child: Wrap(
                        direction: Axis.vertical,
                        children: <Widget>[
                          Text(AppLocalizations.of(context).addStudentWith,
                              style: Theme.of(context).textTheme.caption.copyWith(color: ParentTheme.ash)),
                          SizedBox(height: 27),
                          Container(
                            child: GestureDetector(
                              onTap: () async {
                                Navigator.of(context).pop();
                                String cameraScanResult = await scanner.scan();
                                if (cameraScanResult.isNotEmpty) {
                                  bool success = await widget._interactor.pairWithStudent(cameraScanResult);
                                  if (success) {
                                    _studentsFuture = _loadStudents();
                                    setState(() {});
                                  } else {
                                    Scaffold.of(context).showSnackBar(SnackBar(
                                      content: Text(AppLocalizations.of(context).pairingFailed),
                                      action: SnackBarAction(
                                        label: AppLocalizations.of(context).ok,
                                        textColor: Colors.blue,
                                        onPressed: () {
                                          Scaffold.of(context).hideCurrentSnackBar();
                                        },
                                      ),
                                    ));
                                  }
                                }
                              },
                              child: Text(
                                AppLocalizations.of(context).qrCode,
                                style: Theme.of(context).textTheme.body1.copyWith(fontSize: 16),
                              ),
                            ),
                          ),
                          SizedBox(height: 29),
                          GestureDetector(
                            onTap: () async {
                              Navigator.of(context).pop();
                              var pairingCode = await _addChildDialog(context);
                              if (pairingCode.isNotEmpty) {
                                bool success = await widget._interactor.pairWithStudent(pairingCode);
                                if (success) {
                                  _studentsFuture = _loadStudents();
                                  setState(() {});
                                } else {
                                  Scaffold.of(context).showSnackBar(SnackBar(
                                    content: Text(AppLocalizations.of(context).pairingFailed),
                                    action: SnackBarAction(
                                      label: AppLocalizations.of(context).ok,
                                      textColor: Colors.blue,
                                      onPressed: () {
                                        Scaffold.of(context).hideCurrentSnackBar();
                                      },
                                    ),
                                  ));
                                }
                              }
                            },
                            child: Text(
                              AppLocalizations.of(context).pairingCode,
                              style: Theme.of(context).textTheme.body1.copyWith(fontSize: 16),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              );
            });
      },
    );

//    Widget _bottomSheetPairingCodeOptions() {}
  }
}
