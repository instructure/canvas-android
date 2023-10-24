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
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/remote_config/remote_config_screen.dart';
import 'package:flutter_parent/utils/debug_flags.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/theme_transition/theme_transition_target.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:package_info_plus/package_info_plus.dart';

import '../../router/panda_router.dart';
import '../theme_viewer_screen.dart';

class SettingsInteractor {
  bool isDebugMode() => DebugFlags.isDebug;

  void routeToThemeViewer(BuildContext context) {
    locator<QuickNav>().push(context, ThemeViewerScreen());
  }

  void routeToRemoteConfig(BuildContext context) {
    locator<QuickNav>().push(context, RemoteConfigScreen());
  }

  void routeToLegal(BuildContext context) {
    locator<QuickNav>().pushRoute(context, PandaRouter.legal());
  }

  void toggleDarkMode(BuildContext context, GlobalKey<State<StatefulWidget>>? anchorKey) {
    if (ParentTheme.of(context)?.isDarkMode == true) {
      locator<Analytics>().logEvent(AnalyticsEventConstants.DARK_MODE_OFF);
    } else {
      locator<Analytics>().logEvent(AnalyticsEventConstants.DARK_MODE_ON);
    }
    ThemeTransitionTarget.toggleDarkMode(context, anchorKey);
  }

  void toggleHCMode(context) {
    if (ParentTheme.of(context)?.isHC == true) {
      locator<Analytics>().logEvent(AnalyticsEventConstants.HC_MODE_OFF);
    } else {
      locator<Analytics>().logEvent(AnalyticsEventConstants.HC_MODE_ON);
    }
    ParentTheme.of(context)?.toggleHC();
  }

  void showAboutDialog(context) {
    showDialog(
        context: context,
        builder: (context) => AlertDialog(
            title: Text(L10n(context).about),
            content: FutureBuilder(
              future: PackageInfo.fromPlatform(),
              builder:
                  (BuildContext context, AsyncSnapshot<PackageInfo> snapshot) {
                return SingleChildScrollView(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(L10n(context).aboutAppTitle,
                          style: TextStyle(fontSize: 16)),
                      Text(snapshot.data!.appName, style: TextStyle(fontSize: 14)),
                      SizedBox(height: 24),
                      Text(L10n(context).aboutDomainTitle,
                          style: TextStyle(fontSize: 16)),
                      Text(ApiPrefs.getDomain() ?? '', style: TextStyle(fontSize: 14)),
                      SizedBox(height: 24),
                      Text(L10n(context).aboutLoginIdTitle,
                          style: TextStyle(fontSize: 16)),
                      Text(ApiPrefs.getUser()?.loginId ?? '',
                          style: TextStyle(fontSize: 14)),
                      SizedBox(height: 24),
                      Text(L10n(context).aboutEmailTitle,
                          style: TextStyle(fontSize: 16)),
                      Text(ApiPrefs.getUser()?.primaryEmail ?? '',
                          style: TextStyle(fontSize: 14)),
                      SizedBox(height: 24),
                      Text(L10n(context).aboutVersionTitle,
                          style: TextStyle(fontSize: 16)),
                      Text(snapshot.data!.version, style: TextStyle(fontSize: 14)),
                      SizedBox(height: 32),
                      SvgPicture.asset(
                        'assets/svg/ic_instructure_logo.svg',
                        alignment: Alignment.bottomCenter,
                        semanticsLabel: L10n(context).aboutLogoSemanticsLabel,
                      )
                    ],
                  ),
                );
              },
            )));
  }
}
