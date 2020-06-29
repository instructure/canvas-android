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
import 'package:flutter_localizations/flutter_localizations.dart';

/// A MaterialLocalizations delegate which wraps GlobalMaterialLocalizations.delegate and adds a fallback
/// to the default locale ('en') for unsupported locales
class FallbackMaterialLocalizationsDelegate extends LocalizationsDelegate<MaterialLocalizations> {
  static const LocalizationsDelegate<MaterialLocalizations> delegate = GlobalMaterialLocalizations.delegate;

  const FallbackMaterialLocalizationsDelegate();

  @override
  bool isSupported(Locale locale) => true;

  @override
  Future<MaterialLocalizations> load(Locale locale) {
    if (delegate.isSupported(locale)) return delegate.load(locale);
    return delegate.load(Locale('en'));
  }

  @override
  bool shouldReload(FallbackMaterialLocalizationsDelegate old) => false;

  @override
  String toString() => 'FallbackMaterialLocalizationsDelegate - ${kMaterialSupportedLanguages.length} locales)';
}
