/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/parent_app.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/features_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/url_launcher.dart';

/// Displays an error page for routes that we can handle internally but don't match the current domain
/// Could also be used for failed routes and other route related errors.
///
/// _route -> is the url we attempted to route with, used to launch to a browser
class RouterErrorScreen extends StatelessWidget {
  final String _route;

  RouterErrorScreen(this._route);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(L10n(context).routerErrorTitle),
      ),
      body: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            Icon(CanvasIcons.warning, size: 40, color: ParentColors.failure),
            Padding(
              padding: const EdgeInsets.fromLTRB(48, 28, 48, 32),
              child: Text(
                L10n(context).routerErrorMessage,
                textAlign: TextAlign.center,
                style: Theme.of(context).textTheme.bodySmall?.copyWith(fontSize: 16),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(48, 0, 48, 0),
              child: TextButton(
                onPressed: () {
                  locator<UrlLauncher>().launch(_route);
                },
                child: Text(L10n(context).openInBrowser,
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(fontSize: 16)),
                style: TextButton.styleFrom(
                  shape: RoundedRectangleBorder(
                    borderRadius: new BorderRadius.circular(4.0),
                    side: BorderSide(color: ParentColors.tiara),
                  ),
                ),
              ),
            ),
            SizedBox(height: 28),
            Padding(
              padding: const EdgeInsets.fromLTRB(48, 0, 48, 0),
              child: TextButton(
                onPressed: () {
                  _switchUsers(context);
                },
                child:
                    Text(L10n(context).switchUsers, style: Theme.of(context).textTheme.bodySmall?.copyWith(fontSize: 16)),
                style: TextButton.styleFrom(
                  shape: RoundedRectangleBorder(
                    borderRadius: new BorderRadius.circular(4.0),
                    side: BorderSide(color: ParentColors.tiara),
                  ),
                ),
              ),
            ),
          ]),
    );
  }

  Future<void> _switchUsers(BuildContext context) async {
    await ParentTheme.of(context)?.setSelectedStudent(null); // TODO - Test this, do we need it here?
    await ApiPrefs.performLogout(switchingLogins: true, app: ParentApp.of(context));
    await FeaturesUtils.performLogout();
    await locator<QuickNav>().pushRouteAndClearStack(context, PandaRouter.login());
  }
}
