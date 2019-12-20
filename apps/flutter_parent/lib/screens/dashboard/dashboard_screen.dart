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
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/alerts/alerts_screen.dart';
import 'package:flutter_parent/screens/courses/courses_screen.dart';
import 'package:flutter_parent/screens/inbox/conversation_list/conversation_list_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_screen.dart';
import 'package:flutter_parent/screens/settings/settings_screen.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/dropdown_arrow.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:package_info/package_info.dart';

import 'dashboard_interactor.dart';

class DashboardScreen extends StatefulWidget {
  static final GlobalKey<ScaffoldState> scaffoldKey = GlobalKey<ScaffoldState>();

  DashboardScreen({Key key, this.students}) : super(key: key);

  final List<User> students;

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
    if (widget.students?.isNotEmpty == true) {
      _students = widget.students;
      _selectedStudent = _students.first;
    } else {
      _loadStudents();
    }
    super.initState();

    _interactor.getInboxCountNotifier().update();
  }

  void _loadSelf() {
    setState(() {
      _selfLoading = true;
      _selfError = false;
    });

    _interactor.getSelf().then((user) {
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

    _interactor.getStudents().then((users) {
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
        print('Error loading students: $error');
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
          L10n(context).noStudents,
          style: Theme.of(context).primaryTextTheme.title,
        );
    }

    return SafeArea(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Avatar(
            _selectedStudent.avatarUrl,
            name: _selectedStudent.shortName,
            radius: 24,
          ),
          SizedBox(height: 8),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: <Widget>[
              UserName.fromUser(_selectedStudent, style: Theme.of(context).primaryTextTheme.subhead),
              SizedBox(width: 6),
              DropdownArrow(),
            ],
          )
        ],
      ),
    );
  }

  List<BottomNavigationBarItem> _bottomNavigationBarItems() {
    return [
      BottomNavigationBarItem(icon: Icon(CanvasIcons.courses), title: Text(L10n(context).coursesLabel)),
      BottomNavigationBarItem(icon: Icon(CanvasIcons.calendar_month), title: Text(L10n(context).calendarLabel)),
      BottomNavigationBarItem(icon: Icon(CanvasIcons.alerts), title: Text(L10n(context).alertsLabel)),
    ];
  }

  Widget _navDrawer(User user) {
    if (user == null) {
      if (_selfLoading) {
        // Still loading...
        return LoadingIndicator();
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
        break;
      case 0:
      default:
        return CoursesScreen(_selectedStudent);
        break;
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
    locator<QuickNav>().push(context, ManageStudentsScreen(_students));
  }

  _navigateToHelp(context) {
    // Close the drawer, then push the Help screen in
    Navigator.of(context).pop();
    // TODO: Navigate to the help screen
//    locator<QuickNav>().push(context, HelpScreen());
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
          Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 0, 12), child: Avatar(user.avatarUrl, name: user.shortName)),
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

  Widget _navDrawerItemsList() {
    var items = [
      _navDrawerInbox(),
      _navDrawerManageStudents(),
      _navDrawerSettings(),
      _navDrawerHelp(),
      _navDrawerSignOut(),
      null // to get trailing divider
    ];
    return ListView.separated(
      itemCount: items.length,
      itemBuilder: (context, index) => items[index],
      separatorBuilder: (context, index) => const Divider(height: 0, indent: 16),
    );
  }

  _navDrawerInbox() => ListTile(
        title: Text(L10n(context).inbox),
        onTap: () => _navigateToInbox(context),
        trailing: ValueListenableBuilder(
          valueListenable: _interactor.getInboxCountNotifier(),
          builder: (context, count, _) {
            if (count <= 0) return SizedBox();
            return Container(
              key: Key('inbox-count'),
              decoration: BoxDecoration(color: Theme.of(context).accentColor, shape: BoxShape.circle),
              child: Padding(
                padding: const EdgeInsets.all(6.0),
                child: Text(
                  '$count',
                  style: TextStyle(
                    fontSize: 12,
                    color: Theme.of(context).accentIconTheme.color,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            );
          },
        ),
      );

  _navDrawerManageStudents() =>
      ListTile(title: Text(L10n(context).manageStudents), onTap: () => _navigateToManageStudents(context));

  _navDrawerSettings() => ListTile(
        title: Text(L10n(context).settings),
        onTap: () => locator<QuickNav>().push(context, SettingsScreen()),
      );

  _navDrawerHelp() => ListTile(title: Text(L10n(context).help), onTap: () => _navigateToHelp(context));

  _navDrawerSignOut() => ListTile(title: Text(L10n(context).signOut), onTap: () => _performSignOut(context));

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
                    L10n(context).appVersion(snapshot.data?.version),
                    style: Theme.of(context).textTheme.subtitle,
                  );
                },
              ),
            ),
          )
        ],
      );
}
