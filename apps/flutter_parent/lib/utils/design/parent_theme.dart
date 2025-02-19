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
import 'package:flutter/services.dart';
import 'package:flutter_parent/models/user_color.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/db/user_colors_db.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/theme_prefs.dart';
import 'package:flutter_parent/utils/service_locator.dart';
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
    required this.builder,
    this.themePrefs = const ThemePrefs(),
    super.key
  });

  final ThemePrefs themePrefs;

  @override
  _ParentThemeState createState() => _ParentThemeState();

  static _ParentThemeState? of(BuildContext context) {
    return context.findAncestorStateOfType<_ParentThemeState>();
  }
}

/// State for the [ParentTheme] widget. Holds state for dark mode, high contrast mode, and the dynamically set
/// student color. To obtain an instance of this state, call 'ParentTheme.of(context)' with any context that
/// descends from a ParentTheme widget.
class _ParentThemeState extends State<ParentTheme> {
  ParentThemeStateChangeNotifier _notifier = ParentThemeStateChangeNotifier();

  StudentColorSet? _studentColorSet;

  String? _selectedStudentId;

  Future<void> refreshStudentColor() => setSelectedStudent(_selectedStudentId);

  /// Set the id of the selected student, used for updating the student color. Setting this to null
  /// effectively resets the color state.
  Future<void> setSelectedStudent(String? studentId) async {
    _studentColorSet = null;
    _selectedStudentId = studentId;

    _studentColorSet = await getColorsForStudent(studentId);

    setState(() {});
  }

  Future<StudentColorSet> getColorsForStudent(String? studentId) async {
    // Get saved color for this user
    UserColor? userColor = await locator<UserColorsDb>().getByContext(
      ApiPrefs.getDomain(),
      ApiPrefs.getUser()?.id,
      'user_$studentId',
    );

    StudentColorSet colorSet;
    if (userColor == null && studentId != null) {
      // No saved color for this user, fall back to existing color sets based on user id
      var numId = studentId.replaceAll(RegExp(r'[^\d]'), '');
      var index = (int.tryParse(numId) ?? studentId.length) % StudentColorSet.all.length;
      colorSet = StudentColorSet.all[index];
    } else {
      // Check if there is a matching color set and prefer that for a better dark/HC mode experience
      Color? color = userColor?.color;
      colorSet = StudentColorSet.all.firstWhere(
        (colorSet) => colorSet.light == color,
        orElse: () => color != null ? StudentColorSet(color, color, color, color) : StudentColorSet.all.first,
      );
    }
    return colorSet;
  }

  /// Returns the currently selected student color set
  StudentColorSet get studentColorSet => _studentColorSet ?? StudentColorSet.all[0];

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
  set isDarkMode(bool isDark) {
    setState(() {
      widget.themePrefs.darkMode = isDark;
    });
  }

  /// Sets whether the current theme should use dark mode for WebViews
  set isWebViewDarkMode(bool isDark) {
    setState(() {
      widget.themePrefs.webViewDarkMode = isDark;
    });
  }

  /// Toggles dark mode for the current theme
  toggleDarkMode() => isDarkMode = !isDarkMode;

  /// Toggles WebView dark mode for the current theme
  toggleWebViewDarkMode() => isWebViewDarkMode = !isWebViewDarkMode;

  /// Sets whether the current theme should use high-contrast mode
  set isHC(bool isHC) {
    setState(() {
      widget.themePrefs.hcMode = isHC;
    });
  }

  /// Toggles high-contrast for the current theme
  toggleHC() => isHC = !isHC;

  /// Returns true if dark mode is enabled for the current theme
  bool get isDarkMode => widget.themePrefs.darkMode;

  /// Returns true if dark mode for WebViews is enabled for the current theme
  bool get isWebViewDarkMode => widget.themePrefs.darkMode && widget.themePrefs.webViewDarkMode;

  /// Returns true if high-contrast mode is enabled for the current theme
  bool get isHC => widget.themePrefs.hcMode;

  /// Returns true if both dark mode and high-contrast mode are disabled for the current theme
  bool get isLightNormal => !isDarkMode && !isHC;

  /// Returns true if dark mode is disable and high-contrast mode is enabled for the current theme
  bool get isLightHC => !isDarkMode && isHC;

  /// Returns true if dark mode is enabled and high-contrast mode is disabled for the current theme
  bool get isDarkNormal => isDarkMode && !isHC;

  /// Returns true if both dark mode and high-contrast mode are enabled for the current theme
  bool get isDarkHC => isDarkMode && isHC;

  /// Returns a Parent App theme that ignores student color
  ThemeData get defaultTheme => _buildTheme(getColorVariantForCurrentState(StudentColorSet.electric), isDarkMode);

  /// Returns a light Parent App theme that ignores student color
  ThemeData get defaultLightTheme => _buildTheme(getColorVariantForCurrentState(StudentColorSet.electric), false);

  /// Returns a Parent App theme styled with the color of the currently selected student
  ThemeData get studentTheme => _buildTheme(studentColor, isDarkMode);

  /// Create a preferred size divider that can be used as the bottom of an app bar
  PreferredSize _appBarDivider(Color color) => PreferredSize(
        preferredSize: Size.fromHeight(1),
        child: Divider(height: 1, color: color),
      );

  /// Returns a light divider if in dark mode, otherwise a light divider that changes color with HC mode
  PreferredSize get _appBarDividerThemed =>
      _appBarDivider(isDarkMode ? ParentColors.oxford : ParentColors.appBarDividerLight);

  /// Returns a light divider if in dark mode, dark divider in light mode unless shadowInLightMode is true, wrapping the optional bottom passed in
  PreferredSizeWidget? appBarDivider({PreferredSizeWidget? bottom, bool shadowInLightMode = true}) =>
      (isDarkMode || !shadowInLightMode)
          ? PreferredSize(
              preferredSize: Size.fromHeight(1.0 + (bottom?.preferredSize.height ?? 0)), // Bottom height plus divider
              child: Column(
                children: [
                  if (bottom != null) bottom,
                  _appBarDividerThemed,
                ],
              ),
            )
          : bottom;

  /// Returns a widget wrapping a divider on top of the passed in bottom
  Widget bottomNavigationDivider(Widget bottom) => Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          _appBarDividerThemed,
          bottom,
        ],
      );

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
  Color get nearSurfaceColor => isDarkMode ? Colors.grey[850] ?? ParentColors.porcelain : ParentColors.porcelain;

  /// The green 'success' color appropriate for the current light/dark/HC mode
  Color get successColor => getColorVariantForCurrentState(StudentColorSet.shamrock);

  ThemeData _buildTheme(Color themeColor, bool isDarkMode) {
    var textTheme = _buildTextTheme(onSurfaceColor);

    // Use single text color for all styles in high-contrast mode
    if (isHC) {
      textTheme = textTheme.apply(displayColor: onSurfaceColor, bodyColor: onSurfaceColor);
    }
    else {
      textTheme = isDarkMode ?
      _buildTextTheme(onSurfaceColor, fadeColor: ParentColors.ash) :
      _buildTextTheme(onSurfaceColor);
    }

    var primaryTextTheme = _buildTextTheme(Colors.white, fadeColor: ParentColors.tiara);

    if (isHC) {
      primaryTextTheme = primaryTextTheme.apply(displayColor: Colors.white, bodyColor: Colors.white);
    } else if (isDarkMode) {
      primaryTextTheme = _buildTextTheme(ParentColors.porcelain, fadeColor: ParentColors.tiara);
    }

    var primaryIconTheme = isDarkMode
        ? IconThemeData(color: ParentColors.tiara)
        : IconThemeData(color: Colors.white);

    var brightness = isDarkMode ? Brightness.dark : Brightness.light;
    var backgroundColor = isDarkMode ? Colors.black : Colors.white;
    var iconTheme = isDarkMode
        ? IconThemeData(color: ParentColors.porcelain)
        : IconThemeData(color: ParentColors.licorice);
    var dividerColor = isHC ? onSurfaceColor : isDarkMode ? ParentColors
        .licorice : ParentColors.tiara;
    var dialogBackgroundColor = isDarkMode ? ParentColors.licorice : ParentColors.tiara;

    var swatch = ParentColors.makeSwatch(themeColor);

    return ThemeData(
      primarySwatch: swatch,
      primaryColor: isDarkMode ? Colors.black : null,
      colorScheme: ColorScheme.fromSwatch(primarySwatch: swatch).copyWith(
          secondary: swatch[500], brightness: brightness),
      textSelectionTheme: TextSelectionThemeData(
        selectionHandleColor: swatch[300],
      ),
      scaffoldBackgroundColor: backgroundColor,
      canvasColor: backgroundColor,
      textTheme: textTheme,
      primaryTextTheme: primaryTextTheme,
      iconTheme: iconTheme,
      primaryIconTheme: primaryIconTheme,
      dividerColor: dividerColor,
      dividerTheme: DividerThemeData(color: dividerColor),
      buttonTheme: ButtonThemeData(
          height: 48, minWidth: 120, textTheme: ButtonTextTheme.primary),
      fontFamily: 'Lato',
      tabBarTheme: TabBarTheme(
        labelStyle: primaryTextTheme.titleMedium?.copyWith(fontSize: 14),
        labelColor: primaryTextTheme.titleMedium?.color,
        unselectedLabelStyle: primaryTextTheme.bodySmall?.copyWith(
            fontSize: 14),
        unselectedLabelColor: primaryTextTheme.bodySmall?.color,
      ),
      appBarTheme: AppBarTheme(
        backgroundColor: isDarkMode ? Colors.white12 : themeColor,
        foregroundColor: primaryIconTheme.color,
        systemOverlayStyle: SystemUiOverlayStyle.light,
      ),
    );
  }

  TextTheme _buildTextTheme(Color color, {Color fadeColor = ParentColors.oxford}) {
    return TextTheme(
      /// Design-provided styles

      // Comments for each text style represent the nomenclature of the designs we have
      // Caption
      titleSmall: TextStyle(color: fadeColor, fontSize: 12, fontWeight: FontWeight.w500),

      // Subhead
      labelSmall: TextStyle(color: fadeColor, fontSize: 12, fontWeight: FontWeight.bold, letterSpacing: 0),

      // Body
      bodyMedium: TextStyle(color: color, fontSize: 14, fontWeight: FontWeight.normal),

      // Subtitle
      bodySmall: TextStyle(color: fadeColor, fontSize: 14, fontWeight: FontWeight.w500),

      // Title
      titleMedium: TextStyle(color: color, fontSize: 16, fontWeight: FontWeight.w500),

      // Heading
      headlineSmall: TextStyle(color: color, fontSize: 18, fontWeight: FontWeight.w500),

      // Display
      headlineMedium: TextStyle(color: color, fontSize: 24, fontWeight: FontWeight.w500),

      /// Other/unmapped styles

      titleLarge: TextStyle(color: color),

      displayLarge: TextStyle(color: fadeColor),

      displayMedium: TextStyle(color: fadeColor),

      displaySmall: TextStyle(color: fadeColor),

      bodyLarge: TextStyle(color: color),

      labelLarge: TextStyle(color: color),
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

  const DefaultParentTheme({required this.builder, this.useNonPrimaryAppBar = true, super.key});

  @override
  Widget build(BuildContext context) {
    var theme = ParentTheme.of(context)!.defaultTheme;
    if (useNonPrimaryAppBar) theme = theme.copyWith(appBarTheme: _scaffoldColoredAppBarTheme(context));

    return Consumer<ParentThemeStateChangeNotifier>(
      builder: (context, state, _) => Theme(
        child: Builder(builder: builder),
        data: theme,
      ),
    );
  }

  AppBarTheme _scaffoldColoredAppBarTheme(BuildContext context) {
    final theme = Theme.of(context);
    return AppBarTheme(
      color: theme.scaffoldBackgroundColor,
      toolbarTextStyle: theme.textTheme.bodyMedium,
      titleTextStyle: theme.textTheme.titleLarge,
      iconTheme: theme.iconTheme,
      elevation: 0,
    );
  }
}
