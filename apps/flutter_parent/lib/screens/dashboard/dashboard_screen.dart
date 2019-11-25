/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter/material.dart';
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/alerts/alerts_screen.dart';
import 'package:flutter_parent/screens/courses/courses_screen.dart';
import 'package:flutter_parent/screens/dashboard/inbox_notifier.dart';
import 'package:flutter_parent/screens/inbox/conversation_list/conversation_list_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/utils/common_widets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widets/user_avatar.dart';
import 'package:flutter_parent/utils/common_widets/user_name.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:package_info/package_info.dart';

import 'dashboard_interactor.dart';

class DashboardScreen extends StatefulWidget {
  static final GlobalKey<ScaffoldState> scaffoldKey = GlobalKey<ScaffoldState>();

  @override
  State<StatefulWidget> createState() => DashboardState();
}

class DashboardState extends State<DashboardScreen> {
  DashboardInteractor _interactor = locator<DashboardInteractor>();

  // Dashboard State
  List<User> _students = [];
  User _self;

  bool _studentsLoading = false;
  bool _selfLoading = false;

  // These two will likely be used when we have specs for error screens
  // ignore: unused_field
  bool _selfError = false;
  // ignore: unused_field
  bool _studentsError = false;

  User _selectedStudent;
  int _currentIndex = 0;

  @override
  void initState() {
    _loadSelf();
    _loadStudents();
    super.initState();

    InboxCountNotifier.get().update();
  }

  void _loadSelf() {
    setState(() {
      _selfLoading = true;
      _selfError = false;
    });

    _interactor.getSelf().then((user) {
      print(user);
      _self = user;
      setState(() {
        _selfLoading = false;
      });
    }).catchError((error) {
      print('Error loading user: $error');
      setState(() {
        _selfLoading = false;
        _selfError = true;
      });
    });
  }

  void _loadStudents() {
    setState(() {
      _studentsLoading = true;
      _studentsError = false;
    });

    _interactor.getObservees().then((users) {
      print(users);
      _students = users;

      if (_selectedStudent == null && _students.isNotEmpty) {
        setState(() {
          _selectedStudent = _students.first;
        });
      }

      setState(() {
        _studentsLoading = false;
      });
    }).catchError((error) {
      setState(() {
        _studentsLoading = false;
        _studentsError = true;
        print(error);
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: DashboardScreen.scaffoldKey,
      appBar: PreferredSize(
        preferredSize: Size.fromHeight(107.0),
        child: AppBar(
          flexibleSpace: _appBarStudents(_students),
          centerTitle: true,
        ),
      ),
      drawer: Drawer(
        child: SafeArea(child: _navDrawer(_self)),
      ),
      body: _currentPage(),
      bottomNavigationBar: BottomNavigationBar(
        items: _bottomNavigationBarItems(),
        currentIndex: this._currentIndex,
        onTap: (item) {
          _handleBottomBarClick(item);
        },
      ),
    );
  }

  Widget _appBarStudents(List<User> students) {
    if (students.isEmpty) {
      // No students yet, we are either still loading, or there was an error
      if (_studentsLoading) {
        // Still loading, don't show anything
        return Text('');
      } else
        // Done loading: no students returned
        return Text(
          AppLocalizations.of(context).noStudents,
          style: Theme.of(context).primaryTextTheme.title,
        );
    }

    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: <Widget>[
        UserAvatar(_selectedStudent),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            UserName.fromUser(_selectedStudent, style: Theme.of(context).primaryTextTheme.title),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Icon(
                CanvasIconsSolid.arrow_open_down,
                size: 12.0,
                color: Theme.of(context).primaryIconTheme.color,
              ),
            )
          ],
        )
      ],
    );
  }

  List<BottomNavigationBarItem> _bottomNavigationBarItems() {
    return [
      BottomNavigationBarItem(icon: Icon(CanvasIcons.courses), title: Text(AppLocalizations.of(context).coursesLabel)),
      BottomNavigationBarItem(
          icon: Icon(CanvasIcons.calendar_month), title: Text(AppLocalizations.of(context).calendarLabel)),
      BottomNavigationBarItem(icon: Icon(CanvasIcons.alerts), title: Text(AppLocalizations.of(context).alertsLabel)),
    ];
  }

  Widget _navDrawer(User user) {
    if (user == null) {
      if (_selfLoading) {
        // Still loading...
        return Center(child: CircularProgressIndicator());
      } else {
        // Either we loaded a null user, or we got an error
        // TODO: Add in error screen when we get specs
        return Text('Error');
      }
    }

    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      // Header
      _navDrawerHeader(user),

      // Tiles (Inbox, Manage Students, Sign Out, etc)
      Expanded(
        child: _navDrawerItemsList(),
      ),

      // App version
      _navDrawerAppVersion(),
    ]);
  }

  _handleBottomBarClick(item) {
    setState(() {
      _currentIndex = item;
    });
  }

  Widget _currentPage() {
    if (_studentsLoading) {
      // We're still loading students, just show a loading indicator for now
      return LoadingIndicator();
    }

    switch (_currentIndex) {
      case 1:
//        return CalendarPage();
      case 2:
        return AlertsScreen(_selectedStudent);
      case 0:
      default:
        return CoursesScreen(_selectedStudent);
    }
  }

  _navigateToInbox(context) {
    // Close the drawer, then push the inbox in
    Navigator.of(context).pop();
    locator<QuickNav>().push(context, ConversationListScreen());
  }

  _navigateToManageStudents(context) {
    // Close the drawer, then push the Manage Children screen in
    Navigator.of(context).pop();
//      QuickNav.push(context, ManageChildrenScreen());
  }

  _navigateToHelp(context) {
    // Close the drawer, then push the Help screen in
    Navigator.of(context).pop();
//      QuickNav.push(context, HelpScreen());
  }

  _performSignOut(context) {
    ApiPrefs.performLogout();
    Navigator.of(context).pushAndRemoveUntil(MaterialPageRoute(builder: (context) {
      return LoginLandingScreen();
    }), (Route<dynamic> route) => false);
  }

  _navDrawerHeader(User user) => Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Padding(padding: const EdgeInsets.fromLTRB(16, 16, 0, 12), child: UserAvatar(user)),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: UserName.fromUser(user, style: TextStyle(fontSize: 20.0, fontWeight: FontWeight.bold)),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Text(
              user?.primaryEmail ?? '',
              style: Theme.of(context).textTheme.caption,
            ),
          ),
          SizedBox(height: 36)
        ],
      );

  _navDrawerItemsList() => ListView.separated(
      itemBuilder: (context, idx) {
        return (idx == 0)
            ? _navDrawerInbox()
            : (idx == 1)
                ? _navDrawerManageStudents()
                : (idx == 2) ? _navDrawerHelp() : (idx == 3) ? _navDrawerSignOut() : null;
      },
      separatorBuilder: (_, c) => Divider(
            height: 0,
            indent: 16,
          ),
      itemCount: 5 // One extra item to get the divider on the bottom
      );

  _navDrawerInbox() => ListTile(
        title: Text(AppLocalizations.of(context).inbox),
        onTap: () => _navigateToInbox(context),
        trailing: ValueListenableBuilder(
          valueListenable: InboxCountNotifier.get(),
          builder: (context, count, _) {
            return Visibility(
              visible: count > 0,
              child: Container(
                decoration: BoxDecoration(color: Colors.blue, shape: BoxShape.circle),
                child: Padding(
                  padding: const EdgeInsets.all(6.0),
                  child: Text(
                    '$count',
                    style: TextStyle(fontSize: 12, color: Colors.white, fontWeight: FontWeight.bold),
                  ),
                ),
              ),
            );
          },
        ),
      );

  _navDrawerManageStudents() => ListTile(
      title: Text(AppLocalizations.of(context).manageStudents), onTap: () => _navigateToManageStudents(context));

  _navDrawerHelp() => ListTile(title: Text(AppLocalizations.of(context).help), onTap: () => _navigateToHelp(context));

  _navDrawerSignOut() =>
      ListTile(title: Text(AppLocalizations.of(context).signOut), onTap: () => _performSignOut(context));

  _navDrawerAppVersion() => Column(
        children: <Widget>[
          Container(
            alignment: AlignmentDirectional.bottomStart,
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: FutureBuilder(
                future: PackageInfo.fromPlatform(),
                builder: (BuildContext context, AsyncSnapshot<PackageInfo> snapshot) {
                  return Text(
                    AppLocalizations.of(context).appVersion(snapshot.data?.version),
                    style: Theme.of(context).textTheme.subtitle,
                  );
                },
              ),
            ),
          )
        ],
      );
}
