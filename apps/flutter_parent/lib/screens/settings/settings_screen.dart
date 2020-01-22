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
import 'package:flutter/semantics.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/settings/settings_interactor.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/theme_transition/theme_transition_target.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class SettingsScreen extends StatefulWidget {
  @override
  _SettingsScreenState createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  Key _lightModeKey = GlobalKey();
  Key _darkModeKey = GlobalKey();
  Key _highContrastModeKey = GlobalKey();

  SettingsInteractor _interactor = locator<SettingsInteractor>();

  @override
  Widget build(BuildContext context) {
    return ThemeTransitionTarget(
      child: DefaultParentTheme(
        builder: (context) => Scaffold(
          appBar: AppBar(
            title: Text(L10n(context).settings),
            bottom: ParentTheme.of(context).appBarDivider(shadowInLightMode: false),
          ),
          body: ListView(
            children: [
              Padding(
                padding: const EdgeInsetsDirectional.fromSTEB(16, 16, 16, 0),
                child: Text(L10n(context).appearance.toUpperCase(), style: Theme.of(context).textTheme.subtitle),
              ),
              Container(
                child: ListTile(
                  title: Text(L10n(context).theme),
                ),
              ),
              _themeButtons(context),
              SizedBox(height: 16),
              _highContrastModeSwitch(context),
              if (_interactor.isDebugMode()) _themeViewer(context),
            ],
          ),
        ),
      ),
    );
  }

  Widget _themeButtons(BuildContext context) {
    return Padding(
      padding: EdgeInsets.zero,
      child: Container(
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            _themeOption(
              anchorKey: _lightModeKey,
              buttonKey: Key('light-mode-button'),
              context: context,
              selected: !ParentTheme.of(context).isDarkMode,
              semanticsLabel: L10n(context).lightModeLabel,
              child: Container(
                // Placeholder until panda image is ready
                color: Colors.white,
                child: Center(
                  child: Icon(
                    Icons.brightness_7,
                    color: Colors.amber,
                    size: 64,
                  ),
                ),
              ),
            ),
            SizedBox(width: 8),
            _themeOption(
              anchorKey: _darkModeKey,
              buttonKey: Key('dark-mode-button'),
              context: context,
              selected: ParentTheme.of(context).isDarkMode,
              semanticsLabel: L10n(context).darkModeLabel,
              child: Container(
                // Placeholder until panda image is ready
                color: Colors.black,
                child: Center(
                  child: Icon(
                    Icons.brightness_7,
                    color: Colors.grey[200],
                    size: 64,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _themeOption({
    GlobalKey anchorKey,
    Key buttonKey,
    BuildContext context,
    bool selected,
    String semanticsLabel,
    Widget child,
  }) {
    double size = 130; // May change once placeholders are replaced with panda images
    return Container(
      key: anchorKey,
      width: size,
      height: size,
      padding: EdgeInsets.all(4),
      decoration: selected
          ? BoxDecoration(
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: Theme.of(context).accentColor, width: 3),
            )
          : null,
      child: ClipRRect(
        borderRadius: BorderRadius.circular(10),
        child: Stack(
          children: <Widget>[
            child,
            Material(
              type: MaterialType.transparency,
              child: Semantics(
                label: semanticsLabel,
                child: InkWell(
                  key: buttonKey,
                  onTap: selected ? null : () => _interactor.toggleDarkMode(context, anchorKey),
                ),
              ),
            )
          ],
        ),
      ),
    );
  }

  Widget _highContrastModeSwitch(BuildContext context) {
    return MergeSemantics(
      child: ListTile(
        title: Text(L10n(context).highContrastLabel),
        trailing: Switch(
          key: _highContrastModeKey,
          value: ParentTheme.of(context).isHC,
          onChanged: (_) => _onHighContrastModeChanged(context),
          materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
        ),
        onTap: () => _onHighContrastModeChanged(context),
      ),
    );
  }

  _onHighContrastModeChanged(BuildContext context) {
    _interactor.toggleHCMode(context, _highContrastModeKey);
  }

  Widget _themeViewer(BuildContext context) => ListTile(
        key: Key('theme-viewer'),
        title: Row(
          children: <Widget>[
            _debugLabel(context),
            SizedBox(width: 16),
            Text('Theme Viewer'), // Not shown in release mode, not translated
          ],
        ),
        onTap: () => _interactor.routeToThemeViewer(context),
      );

  Container _debugLabel(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: Theme.of(context).accentColor,
        borderRadius: BorderRadius.circular(32),
      ),
      padding: const EdgeInsets.all(4),
      child: Icon(Icons.bug_report, color: Theme.of(context).accentIconTheme.color, size: 16),
    );
  }
}
