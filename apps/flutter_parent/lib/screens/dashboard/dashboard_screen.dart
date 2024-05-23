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
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/parent_app.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/alerts/alerts_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_today_click_notifier.dart';
import 'package:flutter_parent/screens/calendar/calendar_today_notifier.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_widget.dart';
import 'package:flutter_parent/screens/courses/courses_screen.dart';
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:flutter_parent/screens/dashboard/student_expansion_widget.dart';
import 'package:flutter_parent/screens/dashboard/student_horizontal_list_view.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_screen.dart';
import 'package:flutter_parent/screens/masquerade/masquerade_screen.dart';
import 'package:flutter_parent/screens/pairing/pairing_util.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/badges.dart';
import 'package:flutter_parent/utils/common_widgets/dropdown_arrow.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/masquerade_ui.dart';
import 'package:flutter_parent/utils/common_widgets/rating_dialog.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/features_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:provider/provider.dart';

import 'dashboard_interactor.dart';

class DashboardScreen extends StatefulWidget {
  DashboardScreen({this.students, this.startingPage, this.deepLinkParams, super.key});

  final List<User>? students;

  // Used when deep linking into the courses, calendar, or alert screen
  final DashboardContentScreens? startingPage;
  final Map<String, Object>? deepLinkParams;

  @override
  State<StatefulWidget> createState() => DashboardState();
}

class DashboardState extends State<DashboardScreen> {
  late GlobalKey<ScaffoldState> scaffoldKey;
  DashboardInteractor _interactor = locator<DashboardInteractor>();

  // Dashboard State
  List<User> _students = [];
  late User? _self;

  bool _studentsLoading = false;
  bool _selfLoading = false;

  // This will likely be used when we have specs for the error state
  // ignore: unused_field
  bool _studentsError = false;

  User? _selectedStudent;
  late DashboardContentScreens _currentIndex;

  bool expand = false;

  late SelectedStudentNotifier _selectedStudentNotifier;
  late CalendarTodayNotifier _showTodayNotifier;

  @visibleForTesting
  Map<String, Object>? currentDeepLinkParams;

  late Function() _onStudentAdded;

  @override
  void initState() {
    scaffoldKey = GlobalKey<ScaffoldState>();
    currentDeepLinkParams = widget.deepLinkParams;
    _currentIndex = widget.startingPage ?? DashboardContentScreens.Courses;
    _selectedStudentNotifier = SelectedStudentNotifier();
    _showTodayNotifier = CalendarTodayNotifier();
    _loadSelf();
    if (widget.students?.isNotEmpty == true) {
      _students = widget.students!;
      String? selectedStudentId = ApiPrefs.getCurrentLogin()?.selectedStudentId;
      _selectedStudent = _students.firstWhere((it) => it.id == selectedStudentId, orElse: () => _students.first);
      _updateStudentColor(_selectedStudent!.id);
      _selectedStudentNotifier.value = _selectedStudent!;
      ApiPrefs.setCurrentStudent(_selectedStudent);
      _interactor.getAlertCountNotifier().update(_selectedStudent!.id);
    } else {
      _loadStudents();
    }
    _onStudentAdded = () => _addStudent();
    locator<StudentAddedNotifier>().addListener(_onStudentAdded);
    super.initState();

    _interactor.getInboxCountNotifier().update();
    _showOldReminderMessage();

    // Try to show the rating dialog
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      RatingDialog.asDialog(context);
    });

    _interactor.requestNotificationPermission();
  }

  @override
  void dispose() {
    locator<StudentAddedNotifier>().removeListener(_onStudentAdded);
    super.dispose();
  }

  void _updateStudentColor(String studentId) {
    WidgetsBinding.instance.scheduleFrameCallback((_) {
      ParentTheme.of(context)?.setSelectedStudent(studentId);
    });
  }

  void _loadSelf() {
    setState(() {
      _self = ApiPrefs.getUser();
      _selfLoading = true;
    });

    _interactor.getSelf(app: ParentApp.of(context)).then((user) {
      _self = user!;
      setState(() {
        _selfLoading = false;
      });
    }).catchError((error) {
      print('Error loading user: $error');
      setState(() {
        _selfLoading = false;
      });
    });
  }

  void _loadStudents() {
    setState(() {
      _studentsLoading = true;
      _studentsError = false;
    });

    _interactor.getStudents().then((users) {
      _students = users!;

      if (_selectedStudent == null && _students.isNotEmpty) {
        setState(() {
          String? selectedStudentId = ApiPrefs.getCurrentLogin()?.selectedStudentId;
          _selectedStudent = _students.firstWhere((it) => it.id == selectedStudentId, orElse: () => _students.first);
          updateStudent();
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

  void _addStudent() {
    setState(() {
      _studentsLoading = true;
      _studentsError = false;
    });

    _interactor.getStudents(forceRefresh: true).then((users) {
      setState(() {
        print('users: $users');

        if (users != null && users.length > _students.length) {
          var newStudents = users.toSet().difference(_students.toSet());
          _selectedStudent = newStudents.first;
          updateStudent();
        }
        _students = users!;
        if (!users.map((e) => e.id).contains(_selectedStudent?.id)){
          _selectedStudent = _students.first;
          updateStudent();
        }
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

  void updateStudent() {
    _selectedStudentNotifier.value = _selectedStudent!;
    _updateStudentColor(_selectedStudent!.id);
    ApiPrefs.setCurrentStudent(_selectedStudent);
    _interactor.getAlertCountNotifier().update(_selectedStudent!.id);
  }

  @override
  Widget build(BuildContext context) {
    if (_currentIndex != DashboardContentScreens.Calendar) {
      _showTodayNotifier.value = false;
    }

    return MultiProvider(
      providers: [
        ChangeNotifierProvider<SelectedStudentNotifier>(create: (context) => _selectedStudentNotifier),
        ChangeNotifierProvider<CalendarTodayNotifier>(create: (context) => _showTodayNotifier),
      ],
      child: Consumer<SelectedStudentNotifier>(
        builder: (context, model, _) {
          return Scaffold(
            key: scaffoldKey,
            appBar: PreferredSize(
              preferredSize: Size.fromHeight(107.0),
              child: AppBar(
                // Today button is only for the calendar and the notifier value is set in the calendar screen
                actions: [
                  Consumer<CalendarTodayNotifier>(builder: (context, model, _) {
                    if (model.value) {
                      return Semantics(
                        label: L10n(context).gotoTodayButtonLabel,
                        child: InkResponse(
                          onTap: () => {locator<CalendarTodayClickNotifier>().trigger()},
                          child: Padding(
                            padding: EdgeInsets.symmetric(horizontal: 16.0),
                            child: SvgPicture.asset(
                              'assets/svg/calendar-today.svg',
                            ),
                          ),
                        ),
                      );
                    } else {
                      return SizedBox.shrink();
                    }
                  })
                ],

                flexibleSpace: Semantics(
                  label: 'Tap to open the student selector',
                  child: _appBarStudents(_students, model.value),
                ),
                centerTitle: true,
                bottom: ParentTheme.of(context)?.appBarDivider(),
                leading: IconButton(
                  icon: WidgetBadge(
                    Icon(
                      Icons.menu,
                      key: Key("drawer_menu"),
                    ),
                    countListenable: _interactor.getInboxCountNotifier(),
                    options: BadgeOptions(includeBorder: true, onPrimarySurface: true),
                  ),
                  onPressed: () => scaffoldKey.currentState?.openDrawer(),
                  tooltip: MaterialLocalizations.of(context).openAppDrawerTooltip,
                ),
              ),
            ),
            drawer: SafeArea(
              child: Drawer(
                backgroundColor: Theme.of(context).scaffoldBackgroundColor,
                child: SafeArea(child: _navDrawer(_self)),
              ),
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
                        onAddStudent: () {
                          _addStudent();
                        },
                      ),
                    ),
                    PreferredSize(
                      preferredSize: Size.fromHeight(1),
                      child: Divider(
                          height: 1,
                          color: ParentTheme.of(context)?.isDarkMode == true
                              ? ParentColors.oxford
                              : ParentColors.appBarDividerLight),
                    ),
                  ],
                ),
              ),
              Expanded(child: _currentPage())
            ]),
            bottomNavigationBar: ParentTheme.of(context)?.bottomNavigationDivider(
              _students.isEmpty
                  ? Container()
                  : BottomNavigationBar(
                      unselectedItemColor: ParentTheme.of(context)?.onSurfaceColor,
                      selectedFontSize: 12,
                      unselectedFontSize: 12,
                      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
                      items: _bottomNavigationBarItems(),
                      currentIndex: this._currentIndex.index,
                      onTap: (item) => _handleBottomBarClick(item),
                    ),
            ),
          );
        },
      ),
    );
  }

  Widget _appBarStudents(List<User> students, User? selectedStudent) {
    if (students.isEmpty) {
      // No students yet, we are either still loading, or there was an error
      if (_studentsLoading) {
        // Still loading, don't show anything
        return Text('');
      } else
        // Done loading: no students returned
        return Center(
          child: Text(
            L10n(context).noStudents,
          ),
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
              Avatar(selectedStudent?.avatarUrl,
                  name: selectedStudent?.shortName ?? '',
                  radius: 24,
                  key: Key("student_expansion_touch_target")),
              SizedBox(height: 8),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: <Widget>[
                  UserName.fromUserShortName(selectedStudent!, style: TextStyle(color: Theme.of(context).primaryIconTheme.color ?? Colors.white)),
                  SizedBox(width: 6),
                  DropdownArrow(rotate: expand, color: Theme.of(context).primaryIconTheme.color ?? Colors.white),
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
      BottomNavigationBarItem(
        icon: _navBarIcon(
          light: 'assets/svg/bottom-nav/courses-light.svg',
          dark: 'assets/svg/bottom-nav/courses-dark.svg',
        ),
        activeIcon: _navBarIcon(
          active: true,
          light: 'assets/svg/bottom-nav/courses-light-selected.svg',
          dark: 'assets/svg/bottom-nav/courses-dark-selected.svg',
        ),
        label: L10n(context).coursesLabel,
      ),
      BottomNavigationBarItem(
        icon: _navBarIcon(
          light: 'assets/svg/bottom-nav/calendar-light.svg',
          dark: 'assets/svg/bottom-nav/calendar-dark.svg',
        ),
        activeIcon: _navBarIcon(
          active: true,
          light: 'assets/svg/bottom-nav/calendar-light-selected.svg',
          dark: 'assets/svg/bottom-nav/calendar-dark-selected.svg',
        ),
        label: L10n(context).calendarLabel,
      ),
      BottomNavigationBarItem(
        icon: WidgetBadge(
          _navBarIcon(
            light: 'assets/svg/bottom-nav/alerts-light.svg',
            dark: 'assets/svg/bottom-nav/alerts-dark.svg',
          ),
          countListenable: _interactor.getAlertCountNotifier(),
          options: BadgeOptions(includeBorder: true),
          key: Key('alerts-count'),
        ),
        activeIcon: WidgetBadge(
          _navBarIcon(
            active: true,
            light: 'assets/svg/bottom-nav/alerts-light-selected.svg',
            dark: 'assets/svg/bottom-nav/alerts-dark-selected.svg',
          ),
          countListenable: _interactor.getAlertCountNotifier(),
          options: BadgeOptions(includeBorder: true),
          key: Key('alerts-count'),
        ),
        label: L10n(context).alertsLabel
      ),
    ];
  }

  Widget _navBarIcon({required String light, required String dark, bool active = false}) {
    bool darkMode = ParentTheme.of(context)?.isDarkMode ?? false;
    return SvgPicture.asset(
      darkMode ? dark : light,
      color: active? ParentTheme.of(context)?.studentColor : null,
      width: 24,
      height: 24,
    );
  }

  Widget _navDrawer(User? user) {
    if (_selfLoading) {
      // Still loading...
      return LoadingIndicator();
    }

    return Container(
      color: Theme.of(context).scaffoldBackgroundColor,
      child: ListTileTheme(
        style: ListTileStyle.list,
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          // Header
          _navDrawerHeader(user),
          Divider(),
          // Tiles (Inbox, Manage Students, Sign Out, etc)
          Expanded(
            child: _navDrawerItemsList(),
          ),

          // App version
          if (ApiPrefs.getCurrentLogin()?.canMasquerade == true && !ApiPrefs.isMasquerading())
            _navDrawerActAsUser(),
          if (ApiPrefs.isMasquerading())
            _navDrawerStopActingAsUser(),
          _navDrawerAppVersion(),
        ]),
      ),
    );
  }

  _handleBottomBarClick(item) {
    setState(() {
      _currentIndex = DashboardContentScreens.values[item];
    });
  }

  Widget _currentPage() {
    if (_studentsLoading) {
      // We're still loading students, just show a loading indicator for now
      return LoadingIndicator();
    }

    if (_students.isEmpty) {
      return EmptyPandaWidget(
        svgPath: 'assets/svg/panda-manage-students.svg',
        title: L10n(context).noStudents,
        subtitle: L10n(context).emptyStudentList,
      );
    }

    Widget _page;

    switch (_currentIndex) {
      case DashboardContentScreens.Calendar:
        _page = CalendarScreen(
          startDate: currentDeepLinkParams != null
              ? (currentDeepLinkParams?.containsKey(CalendarScreen.startDateKey) == true
                  ? currentDeepLinkParams![CalendarScreen.startDateKey] as DateTime?
                  : null)
              : null,
          startView: currentDeepLinkParams != null
              ? (currentDeepLinkParams?.containsKey(CalendarScreen.startViewKey) == true
                  ? currentDeepLinkParams![CalendarScreen.startViewKey] as CalendarView
                  : null)
              : null,
        );
        break;
      case DashboardContentScreens.Alerts:
        _page = AlertsScreen();
        break;
      case DashboardContentScreens.Courses:
      default:
        _page = CoursesScreen();
        break;
    }

    // Deep link params are handled, set them to null so we don't use them again
    currentDeepLinkParams = null;

    return _page;
  }

  _showOldReminderMessage() async {
    if ((await _interactor.shouldShowOldReminderMessage()) == true) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        showDialog(
            context: context,
            barrierDismissible: false,
            builder: (context) {
              return AlertDialog(
                title: Text(L10n(context).oldReminderMessageTitle),
                content: Text(L10n(context).oldReminderMessage),
                actions: <Widget>[
                  TextButton(
                    child: Text(L10n(context).ok),
                    onPressed: () {
                      locator<Analytics>().logEvent(AnalyticsEventConstants.VIEWED_OLD_REMINDER_MESSAGE);
                      Navigator.of(context).pop();
                    },
                  )
                ],
              );
            });
      });
    }
  }

  _navigateToInbox(context) {
    // Close the drawer, then push the inbox in
    Navigator.of(context).pop();
    locator<QuickNav>().pushRoute(context, PandaRouter.conversations());
  }

  _navigateToManageStudents(context) async {
    // Close the drawer, then push the Manage Children screen in
    Navigator.of(context).pop();
    await locator<QuickNav>().push(context, ManageStudentsScreen(_students));
    _addStudent();
  }

  _navigateToSettings(context) {
    // Close the drawer, then push the Settings screen in
    Navigator.of(context).pop();
    locator<QuickNav>().pushRoute(context, PandaRouter.settings());
  }

  _navigateToHelp(context) {
    // Close the drawer, then push the Help screen in
    Navigator.of(context).pop();
    locator<QuickNav>().pushRoute(context, PandaRouter.help());
  }

  _performLogOut(BuildContext context, {bool switchingUsers = false}) async {
    try {
      await ParentTheme.of(context)?.setSelectedStudent(null);
      locator<Analytics>().logEvent(switchingUsers ? AnalyticsEventConstants.SWITCH_USERS : AnalyticsEventConstants.LOGOUT);
      await ApiPrefs.performLogout(switchingLogins: switchingUsers, app: ParentApp.of(context));
      MasqueradeUI.of(context)?.refresh();
      locator<QuickNav>().pushRouteAndClearStack(context, PandaRouter.login());
      await FeaturesUtils.performLogout();
    } catch (e) {
      // Just in case we experience any error we still need to go back to the login screen.
      locator<QuickNav>().pushRouteAndClearStack(context, PandaRouter.login());
    }
  }

  _navDrawerHeader(User? user) => Column(
    crossAxisAlignment: CrossAxisAlignment.start,
    children: [
      Padding(
        padding: const EdgeInsets.fromLTRB(24, 16, 0, 8),
        child: Avatar(user?.avatarUrl, name: user?.shortName, radius: 40),
      ),
      Padding(
        padding: const EdgeInsets.symmetric(horizontal: 24),
        child: UserName.fromUser(user!, style: TextStyle(fontSize: 16.0, fontWeight: FontWeight.bold)),
      ),
      Padding(
        padding: const EdgeInsets.fromLTRB(24, 4, 24, 16),
        child: Text(
          user.primaryEmail ?? '',
          style: Theme.of(context).textTheme.bodySmall,
          overflow: TextOverflow.fade,
        ),
      )
    ],
  );

  Widget _navDrawerItemsList() {
    var items = [
      _navDrawerInbox(),
      _navDrawerManageStudents(),
      _navDrawerSettings(),
      Divider(),
      _navDrawerHelp(),
      _navDrawerSwitchUsers(),
      _navDrawerLogOut(),
    ];
    return ListView.builder(
      itemCount: items.length,
      itemBuilder: (context, index) => items[index],
    );
  }

  // Create the inbox tile with an infinite badge count, since there's lots of space we don't need to limit the count to 99+
  _navDrawerInbox() => ListTile(
        title: Text(L10n(context).inbox, style: Theme.of(context).textTheme.titleMedium),
        onTap: () => _navigateToInbox(context),
        leading: Padding(
          padding: const EdgeInsets.only(left: 8.0),
          child: SvgPicture.asset('assets/svg/ic_inbox.svg', height: 24, width: 24),
        ),
        trailing: NumberBadge(
          listenable: _interactor.getInboxCountNotifier(),
          options: BadgeOptions(maxCount: null),
          key: Key('inbox-count'),
        ),
      );

  _navDrawerManageStudents() => ListTile(
        title: Text(L10n(context).manageStudents, style: Theme.of(context).textTheme.titleMedium),
        onTap: () => _navigateToManageStudents(context),
        leading: Padding(
          padding: const EdgeInsets.only(left: 8.0),
          child: SvgPicture.asset('assets/svg/ic_manage_student.svg', height: 24, width: 24),
        ),
      );

  _navDrawerSettings() => ListTile(
        title: Text(L10n(context).settings, style: Theme.of(context).textTheme.titleMedium),
        onTap: () => _navigateToSettings(context),
        leading: Padding(
          padding: const EdgeInsets.only(left: 8.0),
          child: SvgPicture.asset('assets/svg/ic_settings.svg', height: 24, width: 24),
        ),
      );

  _navDrawerHelp() => ListTile(
        title: Text(L10n(context).help, style: Theme.of(context).textTheme.titleMedium),
        onTap: () => _navigateToHelp(context),
        leading: Padding(
          padding: const EdgeInsets.only(left: 8.0),
          child: SvgPicture.asset('assets/svg/ic_help.svg', height: 24, width: 24),
        ),
      );

  _navDrawerLogOut() => ListTile(
        title: Text(L10n(context).logOut, style: Theme.of(context).textTheme.titleMedium),
        leading: Padding(
          padding: const EdgeInsets.only(left: 8.0),
          child: SvgPicture.asset('assets/svg/ic_logout.svg', height: 24, width: 24,),
        ),
        onTap: () {
          showDialog(
            context: context,
            builder: (context) {
              return AlertDialog(
                content: Text(L10n(context).logoutConfirmation),
                actions: <Widget>[
                  TextButton(
                    child: Text(
                        MaterialLocalizations.of(context).cancelButtonLabel),
                    onPressed: () => Navigator.of(context).pop(),
                  ),
                  TextButton(
                    child:
                        Text(MaterialLocalizations.of(context).okButtonLabel),
                    onPressed: () => _performLogOut(context),
                  )
                ],
              );
            },
          );
        },
      );

  _navDrawerSwitchUsers() => ListTile(
        title: Text(L10n(context).switchUsers, style: Theme.of(context).textTheme.titleMedium),
        leading: Padding(
          padding: const EdgeInsets.only(left: 8.0),
          child: SvgPicture.asset('assets/svg/ic_change_user.svg', height: 24, width: 24),
        ),
        onTap: () => _performLogOut(context, switchingUsers: true),
      );

  _navDrawerActAsUser() => ListTile(
        leading: Padding(
          padding: const EdgeInsets.only(left: 8.0),
          child: Icon(CanvasIcons.masquerade),
        ),
        title: Text(L10n(context).actAsUser, style: Theme.of(context).textTheme.titleMedium),
        onTap: () {
          Navigator.of(context).pop();
          locator<QuickNav>().push(context, MasqueradeScreen());
        },
      );

  _navDrawerStopActingAsUser() => ListTile(
        leading: Padding(
          padding: const EdgeInsets.only(left: 8.0),
          child: Icon(CanvasIcons.masquerade),
        ),
        title: Text(L10n(context).stopActAsUser, style: Theme.of(context).textTheme.titleMedium),
        onTap: () {
          Navigator.of(context).pop();
          MasqueradeUI.showMasqueradeCancelDialog(GlobalKey(), context);
        },
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
                    L10n(context).appVersion(snapshot.data?.version ?? ''),
                    style: Theme.of(context).textTheme.titleSmall,
                  );
                },
              ),
            ),
          )
        ],
      );
}

enum DashboardContentScreens { Courses, Calendar, Alerts }
