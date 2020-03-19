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

import 'package:flutter/widgets.dart';
import 'package:flutter_parent/utils/debug_flags.dart';
import 'package:flutter_parent/utils/design/theme_transition/theme_transition_target.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';

import '../theme_viewer_screen.dart';

class SettingsInteractor {
  bool isDebugMode() => DebugFlags.isDebug;

  void routeToThemeViewer(BuildContext context) {
    locator<QuickNav>().push(context, ThemeViewerScreen());
  }

  void toggleDarkMode(context, anchorKey) {
    ThemeTransitionTarget.toggleDarkMode(context, anchorKey);
  }

  void toggleHCMode(context, anchorKey) {
    ThemeTransitionTarget.toggleHCMode(context, anchorKey);
  }
}
