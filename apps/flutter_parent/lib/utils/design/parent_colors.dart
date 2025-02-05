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

import 'dart:math';
import 'dart:ui';

import 'package:flutter/material.dart';

class ParentColors {
  /// The core 'dark' color used for text, icons, etc on light backgrounds
  static const licorice = Color(0xFF2D3B45);

  /// The core 'light' color, used for text, icons, etc on dark backgrounds
  static const tiara = Color(0xFFC7CDD1);

  /// A very light color
  static const porcelain = Color(0xFFF5F5F5);

  /// The core 'faded' color, used for subtitles, inactive icons, etc on either light or dark backgrounds
  static const ash = Color(0xFF6B7780);

  /// Another dark color, slightly lighter than licorice
  static const oxford = Color(0xFF394B58);

  /// A general 'failure' color, crimson
  static const failure = Color(0xFFEE0612);

  /// Core color for the parent app
  static const parentApp = Color(0xFF007BC2);

  /// Core color for the student app
  static const studentApp = Color(0xFFE62429);

  /// Core color for the teacher app
  static const teacherApp = Color(0xFF9E58BD);

  /// Color for light mode divider under the app bar
  static const appBarDividerLight = Color(0x1F000000);

  /// Color for masquerade-related UI elements
  static const masquerade = Color(0xFFBE32A3);

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
}
