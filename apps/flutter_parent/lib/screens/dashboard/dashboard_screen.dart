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
import 'package:flutter_parent/router/parent_router.dart';
import 'package:flutter_parent/screens/alerts/alerts_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_screen.dart';
import 'package:flutter_parent/screens/courses/courses_screen.dart';
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:flutter_parent/screens/dashboard/student_expansion_widget.dart';
import 'package:flutter_parent/screens/dashboard/student_horizontal_list_view.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_screen.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/badges.dart';
import 'package:flutter_parent/utils/common_widgets/dropdown_arrow.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:package_info/package_info.dart';
import 'package:provider/provider.dart';

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

  bool expand = false;

  SelectedStudentNotifier _selectedStudentNotifier;

  @override
  void initState() {
    _selectedStudentNotifier = SelectedStudentNotifier();
    _loadSelf();
    if (widget.students?.isNotEmpty == true) {
      _students = widget.students;
      _selectedStudent = _students.first;
      _selectedStudentNotifier.value = _selectedStudent;
      ApiPrefs.setCurrentStudent(_students.first);
      _interactor.getAlertCountNotifier().update(_selectedStudent.id);
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
          _selectedStudentNotifier.value = _students.first;
          _selectedStudent = _students.first;
          ApiPrefs.setCurrentStudent(_students.first);
          _interactor.getAlertCountNotifier().update(_selectedStudent.id);
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
    return ChangeNotifierProvider<SelectedStudentNotifier>(
      create: (context) => _selectedStudentNotifier,
      child: Consumer<SelectedStudentNotifier>(
        builder: (context, model, _) {
          return Scaffold(
            key: DashboardScreen.scaffoldKey,
            appBar: PreferredSize(
              preferredSize: Size.fromHeight(107.0),
              child: AppBar(
                flexibleSpace: Semantics(
                  label: 'Tap to open the student selector',
                  child: _appBarStudents(_students, model.value),
                ),
                centerTitle: true,
                bottom: ParentTheme.of(context).appBarDivider(),
                leading: IconButton(
                  icon: WidgetBadge(
                    Icon(
                      Icons.menu,
                      color: Theme.of(context).primaryIconTheme.color,
                      key: Key("drawer_menu"),
                    ),
                    countListenable: _interactor.getInboxCountNotifier(),
                    options: BadgeOptions(includeBorder: true, onPrimarySurface: true),
                  ),
                  onPressed: () => DashboardScreen.scaffoldKey.currentState.openDrawer(),
                  tooltip: MaterialLocalizations.of(context).openAppDrawerTooltip,
                ),
              ),
            ),
            drawer: Drawer(
              child: SafeArea(child: _navDrawer(_self)),
            ),
            body: Column(children: [
              StudentExpansionWidget(
                expand: expand,
                child: Column(
                  children: <Widget>[
                    Container(
                      height: 108,
                      child: StudentHorizontalListView(
                        _students,
                        onTap: () => setState(() => expand = !expand),
                      ),
                    ),
                    PreferredSize(
                      preferredSize: Size.fromHeight(1),
                      child: Divider(
                          height: 1,
                          color: ParentTheme.of(context).isDarkMode
                              ? ParentColors.oxford
                              : ParentColors.appBarDividerLight),
                    ),
                  ],
                ),
              ),
              Expanded(child: _currentPage())
            ]),
            bottomNavigationBar: ParentTheme.of(context).bottomNavigationDivider(
              BottomNavigationBar(
                backgroundColor: Theme.of(context).scaffoldBackgroundColor,
                items: _bottomNavigationBarItems(),
                currentIndex: this._currentIndex,
                onTap: (item) => _handleBottomBarClick(item),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _appBarStudents(List<User> students, User selectedStudent) {
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
      child: GestureDetector(
        behavior: HitTestBehavior.translucent,
        onTap: () => setState(() => expand = !expand),
        child: Semantics(
          label: L10n(context).tapToShowStudentSelector,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Avatar(selectedStudent.avatarUrl,
                  name: selectedStudent.shortName, radius: 24, key: Key("student_expansion_touch_target")),
              SizedBox(height: 8),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: <Widget>[
                  UserName.fromUserShortName(selectedStudent, style: Theme.of(context).primaryTextTheme.subhead),
                  SizedBox(width: 6),
                  DropdownArrow(rotate: expand),
                ],
              )
            ],
          ),
        ),
      ),
    );
  }

  List<BottomNavigationBarItem> _bottomNavigationBarItems() {
    return [
      BottomNavigationBarItem(icon: Icon(CanvasIcons.courses), title: Text(L10n(context).coursesLabel)),
      BottomNavigationBarItem(icon: Icon(CanvasIcons.calendar_month), title: Text(L10n(context).calendarLabel)),
      BottomNavigationBarItem(
        icon: WidgetBadge(
          Icon(CanvasIcons.alerts),
          countListenable: _interactor.getAlertCountNotifier(),
          options: BadgeOptions(includeBorder: true),
          key: Key('alerts-count'),
        ),
        title: Text(L10n(context).alertsLabel),
      ),
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

    return ListTileTheme(
      style: ListTileStyle.list,
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        // Header
        _navDrawerHeader(user),

        // Tiles (Inbox, Manage Students, Sign Out, etc)
        Expanded(
          child: _navDrawerItemsList(),
        ),

        // App version
        _navDrawerAppVersion(),
      ]),
    );
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
        return CalendarScreen();
        break;
      case 2:
        return AlertsScreen();
        break;
      case 0:
      default:
        return CoursesScreen();
        break;
    }
  }

  _navigateToInbox(context) {
    // Close the drawer, then push the inbox in
    Navigator.of(context).pop();
    locator<QuickNav>().pushRoute(context, PandaRouter.conversations());
  }

  _navigateToManageStudents(context) {
    // Close the drawer, then push the Manage Children screen in
    Navigator.of(context).pop();
    locator<QuickNav>().push(context, ManageStudentsScreen(_students));
  }

  _navigateToHelp(context) {
    // Close the drawer, then push the Help screen in
    Navigator.of(context).pop();
    locator<QuickNav>().pushRoute(context, PandaRouter.help());
  }

  _performLogOut(BuildContext context, {bool switchingUsers = false}) async {
    ParentTheme.of(context).studentIndex = 0;
    await ApiPrefs.performLogout(switchingLogins: switchingUsers);
    locator<QuickNav>().pushRouteAndClearStack(context, PandaRouter.login());
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
      _navDrawerSwitchUsers(),
      _navDrawerLogOut(),
      null // to get trailing divider
    ];
    return ListView.separated(
      itemCount: items.length,
      itemBuilder: (context, index) => items[index],
      separatorBuilder: (context, index) => const Divider(height: 0, indent: 16),
    );
  }

  // Create the inbox tile with an infinite badge count, since there's lots of space we don't need to limit the count to 99+
  _navDrawerInbox() => ListTile(
        title: Text(L10n(context).inbox),
        onTap: () => _navigateToInbox(context),
        trailing: NumberBadge(
          listenable: _interactor.getInboxCountNotifier(),
          options: BadgeOptions(maxCount: null),
          key: Key('inbox-count'),
        ),
      );

  _navDrawerManageStudents() =>
      ListTile(title: Text(L10n(context).manageStudents), onTap: () => _navigateToManageStudents(context));

  _navDrawerSettings() => ListTile(
      title: Text(L10n(context).settings), onTap: () => locator<QuickNav>().pushRoute(context, PandaRouter.settings()));

  _navDrawerHelp() => ListTile(
        title: Text(L10n(context).help),
        onTap: () => _navigateToHelp(context),
      );

  _navDrawerLogOut() => ListTile(
        title: Text(L10n(context).logOut),
        onTap: () {
          showDialog(
            context: context,
            builder: (context) {
              return AlertDialog(
                content: Text(L10n(context).logoutConfirmation),
                actions: <Widget>[
                  FlatButton(
                    child: Text(MaterialLocalizations.of(context).cancelButtonLabel),
                    onPressed: () => Navigator.of(context).pop(),
                  ),
                  FlatButton(
                    child: Text(MaterialLocalizations.of(context).okButtonLabel),
                    onPressed: () => _performLogOut(context),
                  )
                ],
              );
            },
          );
        },
      );

  _navDrawerSwitchUsers() => ListTile(
        title: Text(L10n(context).switchUsers),
        onTap: () => _performLogOut(context, switchingUsers: true),
      );

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
