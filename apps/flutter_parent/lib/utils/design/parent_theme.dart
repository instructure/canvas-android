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

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:provider/provider.dart';

import 'student_color_set.dart';

/// Provides Parent App [ThemeData] to the 'builder' callback. This theme data is styled to account for dark mode,
/// high-contrast mode, and a selectable student color set. This widget is designed to directly wrap (or be a nearby
/// ancestor of) the 'MaterialApp' widget which should consume the provided 'themeData' object.
///
/// Mapping of design text style names to Flutter text style names:
///
///   Design name:    Flutter name:  Size:  Weight:
///   ---------------------------------------------------
///   caption    ->   subtitle       12     Medium (500) - faded
///   subhead    ->   overline       12     Bold (700) - faded
///   body       ->   body1          14     Regular (400)
///   subtitle   ->   caption        14     Medium (500) - faded
///   title      ->   subhead        16     Medium (500)
///   heading    ->   headline       18     Medium (500)
///   display    ->   display1       24     Medium (500)
///
class ParentTheme extends StatefulWidget {
  final Widget Function(BuildContext context, ThemeData themeData) builder;

  const ParentTheme({
    Key key,
    this.builder,
    this.initWithDarkMode = false,
    this.initWithHCMode = false,
  }) : super(key: key);

  final bool initWithDarkMode;

  final bool initWithHCMode;

  @override
  _ParentThemeState createState() => _ParentThemeState(initWithDarkMode, initWithHCMode);

  static _ParentThemeState of(BuildContext context) {
    return context.findAncestorStateOfType<_ParentThemeState>();
  }
}

/// State for the [ParentTheme] widget. Holds state for dark mode, high contrast mode, and the dynamically set
/// student color. To obtain an instance of this state, call 'ParentTheme.of(context)' with any context that
/// descends from a ParentTheme widget.
class _ParentThemeState extends State<ParentTheme> {
  _ParentThemeState(bool initWithDarkMode, bool initWithHCMode) {
    _isDarkMode = initWithDarkMode;
    _isHC = initWithHCMode;
  }

  ParentThemeStateChangeNotifier _notifier = ParentThemeStateChangeNotifier();

  int _studentIndex = 0;

  bool _isDarkMode;

  bool _isHC;

  /// The index of the currently selected student color set
  int get studentIndex => _studentIndex;

  /// Set the index of the selected student color set
  set studentIndex(index) {
    setState(() => _studentIndex = index);
  }

  /// Returns the currently selected student color set
  StudentColorSet get studentColorSet => StudentColorSet.all[_studentIndex % StudentColorSet.all.length];

  /// Returns the color variant of the provided 'colorSet' that is appropriate for the current state of dark mode and
  /// high-contrast mode in this theme
  Color getColorVariantForCurrentState(StudentColorSet colorSet) {
    if (isDarkMode) {
      return isHC ? colorSet.darkHC : colorSet.dark;
    } else {
      return isHC ? colorSet.lightHC : colorSet.light;
    }
  }

  /// Returns the current student color based on the state of dark mode and high-contrast mode
  Color get studentColor => getColorVariantForCurrentState(studentColorSet);

  /// Sets whether the current theme should use dark mode
  set isDarkMode(bool isDark) => setState(() => _isDarkMode = isDark);

  /// Toggles dark mode for the current theme
  toggleDarkMode() => setState(() => _isDarkMode = !_isDarkMode);

  /// Sets whether the current theme should use high-contrast mode
  set isHC(bool isHC) => setState(() => _isHC = isHC);

  /// Toggles high-contrast for the current theme
  toggleHC() => setState(() => _isHC = !_isHC);

  /// Returns true if dark mode is enabled for the current theme
  bool get isDarkMode => _isDarkMode;

  /// Returns true if high-contrast mode is enabled for the current theme
  bool get isHC => _isHC;

  /// Returns true if both dark mode and high-contrast mode are disabled for the current theme
  bool get isLightNormal => !isDarkMode && !isHC;

  /// Returns true if dark mode is disable and high-contrast mode is enabled for the current theme
  bool get isLightHC => !isDarkMode && isHC;

  /// Returns true if dark mode is enabled and high-contrast mode is disabled for the current theme
  bool get isDarkNormal => isDarkMode && !isHC;

  /// Returns true if both dark mode and high-contrast mode are enabled for the current theme
  bool get isDarkHC => isDarkMode && isHC;

  /// Returns a Parent App theme that ignores student color
  ThemeData get defaultTheme => _buildTheme(getColorVariantForCurrentState(StudentColorSet.electric));

  /// Returns a Parent App theme styled with the color of the currently selected student
  ThemeData get studentTheme => _buildTheme(studentColor);

  @override
  void setState(fn) {
    super.setState(fn);
    _notifier.notify();
  }

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider<ParentThemeStateChangeNotifier>.value(
      value: _notifier,
      child: widget.builder(context, studentTheme),
    );
  }

  /// Color for text, icons, etc that contrasts sharply with the scaffold (i.e. surface) color
  Color get onSurfaceColor => isDarkMode ? ParentColors.tiara : ParentColors.licorice;

  /// Color similar to the surface color but is slightly darker in light mode and slightly lighter in dark mode.
  /// This should be used elements that should be visually distinguishable from the surface color but must also contrast
  /// sharply with the [onSurfaceColor]. Examples are chip backgrounds, progressbar backgrounds, avatar backgrounds, etc.
  Color get nearSurfaceColor => isDarkMode ? Colors.grey[850] : ParentColors.porcelain;

  ThemeData _buildTheme(Color themeColor) {
    var textTheme = _buildTextTheme(onSurfaceColor);

    // Use single text color for all styles in high-contrast mode
    if (isHC) textTheme = textTheme.apply(displayColor: onSurfaceColor, bodyColor: onSurfaceColor);

    var swatch = ParentColors.makeSwatch(themeColor);
    return ThemeData(
      brightness: isDarkMode ? Brightness.dark : Brightness.light,
      primarySwatch: swatch,
      primaryColor: isDarkMode ? Colors.black : null,
      accentColor: swatch[500],
      toggleableActiveColor: swatch[500],
      textSelectionHandleColor: swatch[300],
      scaffoldBackgroundColor: isDarkMode ? Colors.black : Colors.white,
      accentColorBrightness: isDarkMode ? Brightness.light : Brightness.dark,
      textTheme: textTheme,
      primaryTextTheme: isDarkMode ? textTheme : _buildTextTheme(Colors.white, fadeColor: Colors.white70),
      accentTextTheme: isDarkMode ? textTheme : _buildTextTheme(Colors.white, fadeColor: Colors.white70),
      iconTheme: IconThemeData(color: onSurfaceColor),
      primaryIconTheme: IconThemeData(color: isDarkMode ? ParentColors.tiara : Colors.white),
      accentIconTheme: IconThemeData(color: isDarkMode ? Colors.black : Colors.white),
      dividerColor: isHC ? onSurfaceColor : isDarkMode ? nearSurfaceColor : ParentColors.tiara,
      buttonTheme: ButtonThemeData(height: 48, minWidth: 120),
    );
  }

  TextTheme _buildTextTheme(Color color, {Color fadeColor = ParentColors.ash}) {
    return TextTheme(
      /// Design-provided styles

      // Comments for each text style represent the nomenclature of the designs we have
      // Caption
      subtitle: TextStyle(color: fadeColor, fontSize: 12, fontWeight: FontWeight.w500),

      // Subhead
      overline: TextStyle(color: fadeColor, fontSize: 12, fontWeight: FontWeight.bold, letterSpacing: 0),

      // Body
      body1: TextStyle(color: color, fontSize: 14, fontWeight: FontWeight.normal),

      // Subtitle
      caption: TextStyle(color: fadeColor, fontSize: 14, fontWeight: FontWeight.w500),

      // Title
      subhead: TextStyle(color: color, fontSize: 16, fontWeight: FontWeight.w500),

      // Heading
      headline: TextStyle(color: color, fontSize: 18, fontWeight: FontWeight.w500),

      // Display
      display1: TextStyle(color: color, fontSize: 24, fontWeight: FontWeight.w500),

      /// Other/unmapped styles

      title: TextStyle(color: color),

      display4: TextStyle(color: fadeColor),

      display3: TextStyle(color: fadeColor),

      display2: TextStyle(color: fadeColor),

      body2: TextStyle(color: color),

      button: TextStyle(color: color),
    );
  }
}

/// A [ChangeNotifier] used to notify consumers when the ParentTheme state changes. Ideally the state itself would
/// be a [ChangeNotifier] so we wouldn't need this extra class, but there is currently a mixin-related limitation
/// that prevents the state's dispose method from being called: https://github.com/flutter/flutter/issues/24293
class ParentThemeStateChangeNotifier with ChangeNotifier {
  notify() => notifyListeners(); // notifyListeners is protected, so we expose it through another method
}

/// Applies a 'default' Parent App theme to descendant widgets. This theme is identical to the one provided by
/// ParentTheme with the exception of the primary and accent colors, which are fixed and do not respond to changes
/// to the selected student color.
/// Additionally, the app bar can be specified to the non primary app bar when emphasizing that there is no student
/// context. This makes the app bar use the scaffold background color, altering the text and icon themes so that they
/// still show properly as well.
class DefaultParentTheme extends StatelessWidget {
  final WidgetBuilder builder;
  final bool useNonPrimaryAppBar;

  const DefaultParentTheme({Key key, @required this.builder, this.useNonPrimaryAppBar = true}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    var theme = ParentTheme.of(context).defaultTheme;
    if (useNonPrimaryAppBar) theme = theme.copyWith(appBarTheme: _scaffoldColoredAppBarTheme(context));

    return Consumer<ParentThemeStateChangeNotifier>(
      builder: (context, state, _) => Theme(
        child: builder(context),
        data: theme,
      ),
    );
  }

  AppBarTheme _scaffoldColoredAppBarTheme(BuildContext context) {
    final theme = Theme.of(context);
    return AppBarTheme(
      color: theme.scaffoldBackgroundColor,
      textTheme: theme.textTheme,
      iconTheme: theme.iconTheme,
    );
  }
}
