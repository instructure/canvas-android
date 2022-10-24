// Copyright (C) 2020 - present Instructure, Inc.
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
import 'package:flutter_student_embed/utils/design/student_colors.dart';
import 'package:flutter_student_embed/utils/native_comm.dart';
import 'package:provider/provider.dart';

/// Provides Student App [ThemeData] to the 'builder' callback. This widget is designed to directly wrap (or be a nearby
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
class StudentTheme extends StatefulWidget {
  final Widget Function(BuildContext context, ThemeData themeData) builder;

  const StudentTheme({Key key, this.builder}) : super(key: key);

  @override
  _StudentThemeState createState() => _StudentThemeState();

  static _StudentThemeState of(BuildContext context) {
    return context.findAncestorStateOfType<_StudentThemeState>();
  }
}

/// State for the [StudentTheme] widget. To obtain an instance of this state, call 'StudentTheme.of(context)' with any
/// context that inherits from a StudentTheme widget.
class _StudentThemeState extends State<StudentTheme> {
  StudentThemeStateChangeNotifier _notifier = StudentThemeStateChangeNotifier();

  @override
  void initState() {
    NativeComm.onThemeUpdated = () => setState(() {});
    super.initState();
  }

  /// Returns a theme with the institution colors
  ThemeData get defaultTheme => _buildTheme(
        StudentColors.primaryColor,
        StudentColors.accentColor,
        StudentColors.buttonColor,
        StudentColors.primaryTextColor,
        StudentColors.textButtonColor
      );

  ThemeData getCanvasContextTheme(String contextCode) {
    var contextColor = getCanvasContextColor(contextCode);
    return _buildTheme(contextColor, contextColor, StudentColors.buttonColor, Colors.white, StudentColors.textButtonColor);
  }

  Color getCanvasContextColor(String contextCode) {
    if (contextCode == null || contextCode.isEmpty || contextCode.startsWith('user')) {
      return StudentColors.accentColor;
    } else {
      return StudentColors.contextColors[contextCode] ?? StudentColors.generateContextColor(contextCode);
    }
  }

  @override
  void setState(fn) {
    super.setState(fn);
    _notifier.notify();
  }

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider<StudentThemeStateChangeNotifier>.value(
      value: _notifier,
      child: widget.builder(context, defaultTheme),
    );
  }

  /// Color for text, icons, etc that contrasts sharply with the scaffold (i.e. surface) color
  Color get onSurfaceColor => StudentColors.textDarkest;

  /// Color similar to the surface color but is slightly darker in light mode and slightly lighter in dark mode.
  /// This should be used elements that should be visually distinguishable from the surface color but must also contrast
  /// sharply with the [onSurfaceColor]. Examples are chip backgrounds, progressbar backgrounds, avatar backgrounds, etc.
  Color get nearSurfaceColor => StudentColors.backgroundLight;

  ThemeData _buildTheme(Color primaryColor, Color accentColor, Color buttonColor, Color primaryTextColor, Color textButtonColor) {
    var textTheme = _buildTextTheme(onSurfaceColor, StudentColors.textDark);

    var primarySwatch = StudentColors.makeSwatch(primaryColor);

    var buttonColorScheme = ColorScheme.light().copyWith(
      primary: textButtonColor,
      primaryVariant: textButtonColor,
      secondary: textButtonColor,
      secondaryVariant: textButtonColor,
    );

    return ThemeData(
      brightness: Brightness.light,
      primarySwatch: primarySwatch,
      accentColor: accentColor,
      textSelectionHandleColor: primarySwatch[300],
      scaffoldBackgroundColor: StudentColors.backgroundLightest,
      canvasColor: StudentColors.backgroundLightest,
      accentColorBrightness: Brightness.dark,
      textTheme: textTheme,
      primaryTextTheme: _buildTextTheme(primaryTextColor, primaryTextColor.withOpacity(0.7)),
      accentTextTheme: _buildTextTheme(Colors.white, Colors.white70),
      iconTheme: IconThemeData(color: onSurfaceColor),
      primaryIconTheme: IconThemeData(color: primaryTextColor),
      accentIconTheme: IconThemeData(color: Colors.white),
      popupMenuTheme: PopupMenuThemeData(color: StudentColors.backgroundLightestElevated),
      floatingActionButtonTheme: FloatingActionButtonThemeData(
        backgroundColor: buttonColor,
        foregroundColor: Colors.white,
      ),
      dividerColor: StudentColors.tiara,
      buttonColor: textButtonColor,
      hintColor: StudentColors.textDark,
      buttonTheme: ButtonThemeData(height: 48, minWidth: 120, colorScheme: buttonColorScheme),
      fontFamily: 'Lato',
      unselectedWidgetColor: StudentColors.textDarkest
    );
  }

  TextTheme _buildTextTheme(Color color, Color fadeColor) {
    return TextTheme(
      /// Design-provided styles

      // Comments for each text style represent the nomenclature of the designs we have
      // Caption
      subtitle2: TextStyle(color: fadeColor, fontSize: 12, fontWeight: FontWeight.w500),

      // Subhead
      overline: TextStyle(color: fadeColor, fontSize: 12, fontWeight: FontWeight.bold, letterSpacing: 0),

      // Body
      bodyText2: TextStyle(color: color, fontSize: 14, fontWeight: FontWeight.normal),

      // Subtitle
      caption: TextStyle(color: fadeColor, fontSize: 14, fontWeight: FontWeight.w500),

      // Title
      subtitle1: TextStyle(color: color, fontSize: 16, fontWeight: FontWeight.w500),

      // Heading
      headline5: TextStyle(color: color, fontSize: 18, fontWeight: FontWeight.w500),

      // Display
      headline4: TextStyle(color: color, fontSize: 24, fontWeight: FontWeight.w500),

      /// Other/unmapped styles

      headline6: TextStyle(color: color),

      headline1: TextStyle(color: fadeColor),

      headline2: TextStyle(color: fadeColor),

      headline3: TextStyle(color: fadeColor),

      bodyText1: TextStyle(color: color),

      button: TextStyle(color: color),
    );
  }
}

/// A [ChangeNotifier] used to notify consumers when the StudentTheme state changes. Ideally the state itself would
/// be a [ChangeNotifier] so we wouldn't need this extra class, but there is currently a mixin-related limitation
/// that prevents the state's dispose method from being called: https://github.com/flutter/flutter/issues/24293
class StudentThemeStateChangeNotifier with ChangeNotifier {
  notify() => notifyListeners(); // notifyListeners is protected, so we expose it through another method
}

/// Applies a theme to descendant widgets using the color associated with the specified canvas context
class CanvasContextTheme extends StatelessWidget {
  final WidgetBuilder builder;
  final String contextCode;

  const CanvasContextTheme({
    Key key,
    @required this.contextCode,
    @required this.builder,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Consumer<StudentThemeStateChangeNotifier>(
      builder: (context, state, _) => Theme(
        child: Builder(builder: builder),
        data: StudentTheme.of(context).getCanvasContextTheme(contextCode),
      ),
    );
  }
}

/// A theme that uses a white app bar with dark text
class WhiteAppBarTheme extends StatelessWidget {
  final WidgetBuilder builder;

  const WhiteAppBarTheme({Key key, @required this.builder}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    var baseTheme = Theme.of(context);
    var theme = StudentTheme.of(context).defaultTheme.copyWith(
          appBarTheme: AppBarTheme(
            color: StudentColors.backgroundLightestElevated,
            toolbarTextStyle: baseTheme.textTheme.bodyText2,
            titleTextStyle: baseTheme.textTheme.headline6,
            iconTheme: baseTheme.iconTheme,
            elevation: 2,
          ),
        );

    return Consumer<StudentThemeStateChangeNotifier>(
      builder: (context, state, _) => Theme(
        child: Builder(builder: builder),
        data: theme,
      ),
    );
  }
}
