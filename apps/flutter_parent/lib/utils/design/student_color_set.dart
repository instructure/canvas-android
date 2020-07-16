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

import 'dart:ui';

import 'package:flutter/cupertino.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';

/// Holds a set of a color variants meant to be used with different combinations of dark mode and high-contrast mode
class StudentColorSet {
  /// Blue student color set
  static const electric = StudentColorSet(Color(0xFF007BC2), Color(0xFF065D89), Color(0xFF008EE2), Color(0xFF00A0FF));

  /// Purple student color set
  static const jeffGoldplum =
      StudentColorSet(Color(0xFF5F4DCE), Color(0xFF523ECC), Color(0xFF7667D5), Color(0xFF9584FF));

  /// Pink student color set
  static const barney = StudentColorSet(Color(0xFFBF32A4), Color(0xFFA30785), Color(0xFFCB34AF), Color(0xFFFF43DB));

  /// Red student color set
  static const raspberry = StudentColorSet(Color(0xFFE9162E), Color(0xFFAC182A), Color(0xFFEC3349), Color(0xFFFF6073));

  /// Orange student color set
  static const fire = StudentColorSet(Color(0xFFD34503), Color(0xFF9F3300), Color(0xFFFC5E13), Color(0xFFFF6319));

  /// Green student color set
  static const shamrock = StudentColorSet(Color(0xFF008A12), Color(0xFF006809), Color(0xFF00AC18), Color(0xFF00B119));

  /// List of all student color sets
  static const List<StudentColorSet> all = [electric, jeffGoldplum, barney, raspberry, fire, shamrock];

  const StudentColorSet(this.light, this.lightHC, this.dark, this.darkHC);

  final Color light;
  final Color lightHC;
  final Color dark;
  final Color darkHC;

  static String getA11yName(StudentColorSet colorSet, BuildContext context) {
    switch (colorSet) {
      case electric:
        return L10n(context).colorElectric;
      case jeffGoldplum:
        return L10n(context).colorPlum;
      case barney:
        return L10n(context).colorBarney;
      case raspberry:
        return L10n(context).colorRaspberry;
      case fire:
        return L10n(context).colorFire;
      case shamrock:
        return L10n(context).colorShamrock;
      default:
        return '';
    }
  }
}
