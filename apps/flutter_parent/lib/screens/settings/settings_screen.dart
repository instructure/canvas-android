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
import 'package:flutter_parent/screens/settings/settings_interactor.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/theme_transition/theme_transition_target.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_svg/svg.dart';

class SettingsScreen extends StatefulWidget {
  @override
  _SettingsScreenState createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  var _lightModeKey = GlobalKey();
  var _darkModeKey = GlobalKey();
  Key _highContrastModeKey = GlobalKey();

  SettingsInteractor _interactor = locator<SettingsInteractor>();

  @override
  Widget build(BuildContext context) {
    return ThemeTransitionTarget(
      child: DefaultParentTheme(
        builder: (context) => Scaffold(
          appBar: AppBar(
            title: Text(L10n(context).settings, style: Theme.of(context).textTheme.titleLarge),
            bottom: ParentTheme.of(context)?.appBarDivider(shadowInLightMode: false),
          ),
          body: ListView(
            children: [
              Container(
                child: ListTile(
                  title: Text(L10n(context).theme, style: Theme.of(context).textTheme.bodyMedium),
                ),
              ),
              _themeButtons(context),
              SizedBox(height: 16),
              if (ParentTheme.of(context)?.isDarkMode == true)
                _webViewDarkModeSwitch(context),
              _highContrastModeSwitch(context),
              _about(context),
              _legal(context),
              if (_interactor.isDebugMode()) _themeViewer(context),
              if (_interactor.isDebugMode()) _remoteConfigs(context)
            ],
          ),
        ),
      ),
    );
  }

  Widget _themeButtons(BuildContext context) {
    return Padding(
      padding: EdgeInsets.zero,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: <Widget>[
          _themeOption(
            anchorKey: _lightModeKey,
            buttonKey: Key('light-mode-button'),
            context: context,
            selected: ParentTheme.of(context)?.isDarkMode == false,
            semanticsLabel: L10n(context).lightModeLabel,
            child: SvgPicture.asset(
              'assets/svg/panda-light-mode.svg',
              excludeFromSemantics:
                  true, // Semantic label is set in _themeOption()
            ),
          ),
          _themeOption(
            anchorKey: _darkModeKey,
            buttonKey: Key('dark-mode-button'),
            context: context,
            selected: ParentTheme.of(context)?.isDarkMode == true,
            semanticsLabel: L10n(context).darkModeLabel,
            child: SvgPicture.asset(
              'assets/svg/panda-dark-mode.svg',
              excludeFromSemantics:
                  true, // Semantic label is set in _themeOption()
            ),
          ),
        ],
      ),
    );
  }

  Widget _themeOption({
    GlobalKey? anchorKey,
    Key? buttonKey,
    required BuildContext context,
    required bool selected,
    String? semanticsLabel,
    required Widget child,
  }) {
    double size = 140;
    return Semantics(
      selected: selected,
      label: semanticsLabel,
      inMutuallyExclusiveGroup: true,
      button: true,
      child: Container(
        key: anchorKey,
        width: size,
        height: size,
        padding: EdgeInsets.all(5),
        foregroundDecoration: selected
            ? BoxDecoration(
                borderRadius: BorderRadius.circular(100),
                border:
                    Border.all(color: Theme.of(context).colorScheme.secondary, width: 2),
              )
            : null,
        child: ClipRRect(
          borderRadius: BorderRadius.circular(100),
          child: Stack(
            children: <Widget>[
              child,
              Material(
                type: MaterialType.transparency,
                child: InkWell(
                  key: buttonKey,
                  onTap: selected
                      ? null
                      : () => _interactor.toggleDarkMode(context, anchorKey),
                ),
              )
            ],
          ),
        ),
      ),
    );
  }

  Widget _webViewDarkModeSwitch(BuildContext context) {
    return MergeSemantics(
      child: ListTile(
        title: Text(L10n(context).webViewDarkModeLabel, style: Theme.of(context).textTheme.bodyMedium),
        trailing: Switch(
          value: ParentTheme.of(context)?.isWebViewDarkMode == true,
          onChanged: (_) => _toggleWebViewDarkMode(context),
          materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
        ),
        onTap: () => _toggleWebViewDarkMode(context),
      ),
    );
  }

  _toggleWebViewDarkMode(BuildContext context) {
    if (ParentTheme.of(context)?.isWebViewDarkMode == true) {
      locator<Analytics>().logEvent(AnalyticsEventConstants.DARK_WEB_MODE_OFF);
    } else {
      locator<Analytics>().logEvent(AnalyticsEventConstants.DARK_WEB_MODE_ON);
    }
    ParentTheme.of(context)?.toggleWebViewDarkMode();
  }

  Widget _highContrastModeSwitch(BuildContext context) {
    return MergeSemantics(
      child: ListTile(
        title: Text(L10n(context).highContrastLabel, style: Theme.of(context).textTheme.bodyMedium),
        trailing: Switch(
          key: _highContrastModeKey,
          value: ParentTheme.of(context)?.isHC == true,
          onChanged: (_) => _onHighContrastModeChanged(context),
          materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
        ),
        onTap: () => _onHighContrastModeChanged(context),
      ),
    );
  }

  _onHighContrastModeChanged(BuildContext context) {
    _interactor.toggleHCMode(context);
  }

  Widget _about(BuildContext context) => ListTile(
      key: Key('about'),
      title: Row(
        children: [Text(L10n(context).about, style: Theme.of(context).textTheme.bodyMedium)],
      ),
      onTap: () => _interactor.showAboutDialog(context));

  Widget _legal(BuildContext context) => ListTile(
      title: Text(L10n(context).helpLegalLabel, style: Theme.of(context).textTheme.bodyMedium),
      onTap: () => _interactor.routeToLegal(context));

  Widget _themeViewer(BuildContext context) => ListTile(
        key: Key('theme-viewer'),
        title: Row(
          children: <Widget>[
            _debugLabel(context),
            SizedBox(width: 16),
            Text('Theme Viewer', style: Theme.of(context).textTheme.bodyMedium), // Not shown in release mode, not translated
          ],
        ),
        onTap: () => _interactor.routeToThemeViewer(context),
      );

  Widget _remoteConfigs(BuildContext context) => ListTile(
        key: Key('remote-configs'),
        title: Row(
          children: [
            _debugLabel(context),
            SizedBox(width: 16),
            Text('Remote Config Params', style: Theme.of(context).textTheme.bodyMedium)
          ],
        ),
        onTap: () => _interactor.routeToRemoteConfig(context),
      );

  Container _debugLabel(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.secondary,
        borderRadius: BorderRadius.circular(32),
      ),
      padding: const EdgeInsets.all(4),
      child: Icon(Icons.bug_report,
          color: Theme.of(context).colorScheme.secondary, size: 16),
    );
  }
}
