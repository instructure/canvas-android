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

import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/school_domain.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/qr_login/qr_login_util.dart';
import 'package:flutter_parent/screens/web_login/web_login_screen.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_dialog.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/full_screen_scroll_container.dart';
import 'package:flutter_parent/utils/common_widgets/two_finger_double_tap_gesture_detector.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/debug_flags.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/snickers.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_svg/svg.dart';
import 'package:tuple/tuple.dart';

class LoginLandingScreen extends StatelessWidget {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();

  int loginFlowIndex = 0;

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        key: _scaffoldKey,
        endDrawer: !DebugFlags.isDebug
            ? null // Don't show snickers in release mode
            : Drawer(
                child: SafeArea(
                  child: Center(
                    child: ListView.builder(
                      shrinkWrap: true,
                      itemCount: SNICKERS.length,
                      itemBuilder: (context, index) {
                        var snicker = SNICKERS[index];
                        return ListTile(
                          title: Text(snicker.title),
                          subtitle: Text(snicker.subtitle),
                          onTap: () {
                            // TODO: needs test
                            locator<QuickNav>().push(
                              context,
                              WebLoginScreen(snicker.domain, user: snicker.username, pass: snicker.password),
                            );
                          },
                        );
                      },
                    ),
                  ),
                ),
              ),
        body: TwoFingerDoubleTapGestureDetector(
          onDoubleTap: () => _changeLoginFlow(context),
          child: SafeArea(
            child: Column(
              children: <Widget>[
                Expanded(
                  child: FullScreenScrollContainer(
                    horizontalPadding: 0,
                    children: <Widget>[
                      // _helpRequestButton(context), // add back after we make mobile login better
                      Expanded(child: _body(context)),
                      SizedBox(height: 56.0), // Sizedbox to offset helpRequestButton
                    ],
                  ),
                ),
                _previousLogins(context),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _body(BuildContext context) {
    final lastLoginAccount = ApiPrefs.getLastAccount();
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          SvgPicture.asset(
            'assets/svg/canvas-parent-login-logo.svg',
            semanticsLabel: L10n(context).canvasLogoLabel,
          ),
          SizedBox(height: 64),
          if (lastLoginAccount == null)
            _filledButton(context, L10n(context).findSchool, () {
              onFindSchoolPressed(context);
            }),
          if (lastLoginAccount != null)
            _filledButton(
                context,
                lastLoginAccount.item1.name == null || lastLoginAccount.item1.name.isEmpty
                    ? lastLoginAccount.item1.domain
                    : lastLoginAccount.item1.name, () {
              onSavedSchoolPressed(context, lastLoginAccount);
            }),
          SizedBox(height: 16),
          if (lastLoginAccount != null)
            _outlineButton(context, L10n(context).findAnotherSchool, () {
              onFindSchoolPressed(context);
            }),
          SizedBox(height: 8),
          if (_hasCameras()) _qrLogin(context),
        ],
      ),
    );
  }

  Widget _filledButton(
      BuildContext context, String title, VoidCallback onPressed) {
    return ButtonTheme(
      minWidth: 260,
      child: RaisedButton(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Text(
            title,
            style: TextStyle(fontSize: 16),
          ),
        ),
        color: Theme.of(context).accentColor,
        textColor: Colors.white,
        shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.all(Radius.circular(4))),
        onPressed: onPressed,
      ),
    );
  }

  Widget _outlineButton(
      BuildContext context, String title, VoidCallback onPressed) {
    return ButtonTheme(
      minWidth: 260,
      child: OutlinedButton(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Text(
            title,
            style: Theme.of(context).textTheme.subtitle1,
          ),
        ),
        style: OutlinedButton.styleFrom(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(4.0),
          ),
          side: BorderSide(
              width: 1, color: ParentTheme.of(context).onSurfaceColor),
        ),
        onPressed: onPressed,
      ),
    );
  }

  bool _hasCameras() {
    return ApiPrefs.getCameraCount() != null && ApiPrefs.getCameraCount() != 0;
  }

  Widget _qrLogin(BuildContext context) {
    return InkWell(
        onTap: () {
          // Launches the choice between qr login and qr create
          locator<QRLoginUtil>().launchQRTutorial(context);
        },
        child: Container(
          padding: EdgeInsets.all(12.0),
          child: Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              mainAxisAlignment: MainAxisAlignment.center,
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                SvgPicture.asset('assets/svg/qr-code.svg'),
                SizedBox(width: 8),
                Text(
                  L10n(context).loginWithQRCode,
                  style: TextStyle(fontSize: 16, color: ParentColors.ash),
                ),
              ]),
        ));
  }

  Widget _previousLogins(BuildContext context) {
    final itemHeight = 72.0;
    return StatefulBuilder(
      builder: (context, setState) {
        var logins = ApiPrefs.getLogins();
        if (logins.isEmpty) return Container();
        return Column(
          key: Key('previous-logins'),
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Text(L10n(context).previousLogins, style: Theme.of(context).textTheme.caption),
            ),
            SizedBox(height: 6),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Divider(height: 1),
            ),
            AnimatedContainer(
              curve: Curves.easeInOutBack,
              duration: Duration(milliseconds: 400),
              height: min(itemHeight * 2, itemHeight * logins.length),
              child: ListView.builder(
                padding: EdgeInsets.symmetric(vertical: 0),
                itemCount: logins.length,
                itemBuilder: (context, index) {
                  Login login = logins[index];
                  return ListTile(
                    onTap: () {
                      ApiPrefs.switchLogins(login);
                      locator<QuickNav>().pushRouteAndClearStack(context, PandaRouter.rootSplash());
                    },
                    leading: Stack(
                      overflow: Overflow.visible,
                      children: <Widget>[
                        Avatar.fromUser(login.currentUser),
                        if (login.isMasquerading)
                          Positioned(
                            right: -6,
                            top: -6,
                            child: Container(
                              padding: EdgeInsets.all(4),
                              decoration: BoxDecoration(
                                  shape: BoxShape.circle,
                                  color: ParentColors.masquerade,
                                  border: Border.all(color: Theme.of(context).scaffoldBackgroundColor, width: 2)),
                              child: Icon(CanvasIconsSolid.masquerade, color: Colors.white, size: 10),
                            ),
                          ),
                      ],
                    ),
                    title: UserName.fromUser(login.currentUser),
                    subtitle: Text(login.currentDomain),
                    trailing: IconButton(
                      tooltip: L10n(context).delete,
                      onPressed: () async {
                        await ApiPrefs.removeLogin(login);
                        setState(() {});
                      },
                      icon: Icon(Icons.clear),
                    ),
                  );
                },
              ),
            ),
          ],
        );
      },
    );
  }

  onFindSchoolPressed(BuildContext context) {
    LoginFlow flow = LoginFlow.values[loginFlowIndex % LoginFlow.values.length];
    locator<QuickNav>()
        .pushRoute(context, PandaRouter.domainSearch(loginFlow: flow));
  }

  onSavedSchoolPressed(BuildContext context, Tuple2<SchoolDomain, LoginFlow> lastAccount) {
    locator<QuickNav>().pushRoute(
        context,
        PandaRouter.loginWeb(lastAccount.item1.domain,
            accountName: lastAccount.item1.name,
        loginFlow: lastAccount.item2));
  }

  void _changeLoginFlow(BuildContext context) {
    loginFlowIndex++;
    LoginFlow flow = LoginFlow.values[loginFlowIndex % LoginFlow.values.length];
    String flowDescription;
    switch (flow) {
      case LoginFlow.normal:
        flowDescription = L10n(context).loginFlowNormal;
        break;
      case LoginFlow.canvas:
        flowDescription = L10n(context).loginFlowCanvas;
        break;
      case LoginFlow.siteAdmin:
        flowDescription = L10n(context).loginFlowSiteAdmin;
        break;
      case LoginFlow.skipMobileVerify:
        flowDescription = L10n(context).loginFlowSkipMobileVerify;
        break;
    }

    _scaffoldKey.currentState.removeCurrentSnackBar();
    _scaffoldKey.currentState.showSnackBar(SnackBar(content: Text(flowDescription)));
  }
}
