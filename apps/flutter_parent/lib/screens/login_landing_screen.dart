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

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/web_login/web_login_screen.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/snickers.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_svg/svg.dart';

import 'domain_search/domain_search_screen.dart';

class LoginLandingScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        endDrawer: kReleaseMode
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
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              SvgPicture.asset(
                'assets/svg/canvas-parent-login-logo.svg',
                semanticsLabel: L10n(context).canvasLogoLabel,
              ),
              SizedBox(height: 64),
              RaisedButton(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Text(
                    L10n(context).findSchoolOrDistrict,
                    style: TextStyle(fontSize: 16),
                  ),
                ),
                color: Theme.of(context).accentColor,
                textColor: Colors.white,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(4))),
                onPressed: () {
                  onFindSchoolPressed(context);
                },
              ),
            ],
          ),
        ),
      ),
    );
  }

  onFindSchoolPressed(BuildContext context) {
    locator<QuickNav>().push(context, DomainSearchScreen());
  }
}
