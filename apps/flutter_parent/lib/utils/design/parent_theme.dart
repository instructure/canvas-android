/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'dart:math';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import 'student_color_set.dart';

/// Provides Parent App [ThemeData] to the 'builder' callback. This theme data is styled to account for dark mode,
/// high-contrast mode, and a selectable student color set. This widget is designed to directly wrap (or be a nearby
/// ancestor of) the 'MaterialApp' widget which should consume the provided 'themeData' object.
///
/// Mapping of design text style names to Flutter text style names:
///
///   Design name:    Flutter name:  Size:  Weight:
///   ---------------------------------------------------
///   caption    ->   subtitle       12     Medium (500)
///   subhead    ->   overline       12     Bold (700)
///   body       ->   body1          14     Regular (400)
///   subtitle   ->   caption        14     Medium (500)
///   title      ->   subhead        16     Medium (500)
///   heading    ->   headline       18     Medium (500)
///   display    ->   display1       24     Medium (500)
///
class ParentTheme extends StatefulWidget {
  final Widget Function(BuildContext context, ThemeData themeData) builder;

  const ParentTheme({Key key, this.builder}) : super(key: key);

  /// The core 'dark' color used for text, icons, etc on light backgrounds
  static const licorice = Color(0xFF2D3B45);

  /// The core 'light' color, used for text, icons, etc on dark backgrounds
  static const tiara = Color(0xFFC7CDD1);

  /// The core 'faded' color, used for subtitles, inactive icons, etc on either light or dark backgrounds
  static const ash = Color(0xFF8B969E);

  /// A general 'success' color
  static const success = Color(0xFF00AC18);

  /// A general 'failure' color, crimson
  static const failure = Color(0xFFEE0612);

  /// Generates a [MaterialColor] swatch for a given color. For best results the source color should have a medium brightness.
  static MaterialColor makeSwatch(Color color) {
    var src = HSLColor.fromColor(color);

    var shades = <int, Color>{500: color};

    // Upper (darker) colors. Max saturation is 100, min lightness is 63% of source lightness
    var saturationIncrement = (1.0 - src.saturation) / 4;
    var valueIncrement = ((src.lightness * 0.63) - src.lightness) / 4;
    var saturation = src.saturation;
    var value = src.lightness;
    for (int shade = 600; shade <= 900; shade += 100) {
      saturation += saturationIncrement;
      value += valueIncrement;
      shades[shade] = HSLColor.fromAHSL(1.0, src.hue, min(saturation, 1.0), min(value, 1.0)).toColor();
    }

    // Lower (lighter) colors. Min saturation is 10, max lightness is 99
    saturationIncrement = (src.saturation - 0.10) / 5;
    valueIncrement = (0.99 - src.lightness) / 5;
    saturation = src.saturation;
    value = src.lightness;
    for (int shade = 400; shade >= 0; shade -= 100) {
      saturation += saturationIncrement;
      value += valueIncrement;
      var color = HSLColor.fromAHSL(1.0, src.hue, min(1.0, saturation), min(1.0, value)).toColor();
      if (shade == 0) {
        // A shade of 0 would be completely white, so 50 is the lightest color shade
        shades[50] = color;
      } else {
        shades[shade] = color;
      }
    }

    return MaterialColor(color.value, shades);
  }

  @override
  _ParentThemeState createState() => _ParentThemeState();

  static _ParentThemeState of(BuildContext context) {
    return context.ancestorStateOfType(const TypeMatcher<_ParentThemeState>());
  }
}

/// State for the [ParentTheme] widget. Holds state for dark mode, high contrast mode, and the dynamically set
/// student color. To obtain an instance of this state, call 'ParentTheme.of(context)' with any context that
/// descends from a ParentTheme widget.
class _ParentThemeState extends State<ParentTheme> {
  int _studentIndex = 0;

  bool _isDarkMode = false;

  bool _isHC = false;

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
  Widget build(BuildContext context) {
    return widget.builder(context, studentTheme);
  }

  ThemeData _buildTheme(Color themeColor) {
    var onSurfaceColor = isDarkMode ? ParentTheme.tiara : ParentTheme.licorice;
    var textTheme = _buildTextTheme(onSurfaceColor);

    // Use single text color for all styles in high-contrast mode
    if (isHC) textTheme = textTheme.apply(displayColor: onSurfaceColor, bodyColor: onSurfaceColor);

    var swatch = ParentTheme.makeSwatch(themeColor);
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
      primaryIconTheme: IconThemeData(color: isDarkMode ? ParentTheme.tiara : Colors.white),
      accentIconTheme: IconThemeData(color: isDarkMode ? Colors.black : Colors.white),
      dividerColor: isHC ? onSurfaceColor : isDarkMode ? Colors.grey[600] : null,
    );
  }

  TextTheme _buildTextTheme(Color color, {Color fadeColor = ParentTheme.ash}) {
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

/// Applies a 'default' Parent App theme to descendant widgets. This theme is identical to the one provided by
/// ParentTheme with the exception of the primary and accent colors, which are fixed and do not respond to changes
/// to the selected student color.
class DefaultParentTheme extends StatelessWidget {
  final WidgetBuilder builder;

  const DefaultParentTheme({Key key, @required this.builder}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Theme(
      data: ParentTheme.of(context).defaultTheme,
      child: Builder(builder: builder),
    );
  }
}
