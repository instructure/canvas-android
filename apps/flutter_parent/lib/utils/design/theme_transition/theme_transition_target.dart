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
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/theme_transition/theme_transition_overlay.dart';

/// A widget targeted to display a circular reveal theme transition animation when the theme changes. For example, to
/// animate a transition to or from dark mode, wrap a child with this widget and call
/// ThemeTransitionTarget.toggleDark(context, anchorKey), where anchorKey is a [GlobalKey] assigned to a widget from
/// which the animation transition should originate.
///
/// Note that this this widget is intended to be used with a full-screen [child] (e.g. a settings screen) and will not
/// work correctly with smaller nested widgets within a screen.
class ThemeTransitionTarget extends StatefulWidget {
  final Widget? child;

  const ThemeTransitionTarget({this.child, super.key});

  @override
  _ThemeTransitionTargetState createState() => _ThemeTransitionTargetState();

  /// Toggles dark mode and initiates an animated circular reveal transition to the new theme. [context] must be
  /// a [BuildContext] that contains a [ThemeTransitionTarget], and [anchorKey] must be a [GlobalKey] assigned
  /// to a widget from which the animation transition will originate.
  static void toggleDarkMode(BuildContext context, GlobalKey? anchorKey) {
    _toggleMode(context, anchorKey, () => ParentTheme.of(context)?.toggleDarkMode());
  }

  static void _toggleMode(BuildContext context, GlobalKey? anchorKey, Function() toggle) {
    // If testing, just toggle without doing the theme transition overlay
    if (WidgetsBinding.instance.runtimeType != WidgetsFlutterBinding) {
      toggle();
      return;
    }

    WidgetsBinding.instance.addPostFrameCallback((_) {
      ThemeTransitionOverlay.display(context, anchorKey, () {
        toggle();
      });
    });
  }

  static _ThemeTransitionTargetState? of(BuildContext context) {
    return context.findAncestorStateOfType<_ThemeTransitionTargetState>();
  }
}

class _ThemeTransitionTargetState extends State<ThemeTransitionTarget> {
  final boundaryKey = GlobalKey();

  @override
  Widget build(BuildContext context) {
    return RepaintBoundary(
      key: boundaryKey,
      child: widget.child,
    );
  }
}
