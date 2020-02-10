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

import 'package:intl/intl.dart';

extension Format on DateTime {
  /// Formats this [DateTime] for the current locale using the provided localization function
  String l10nFormat(
    String Function(String date, String time) localizer, {
    DateFormat dateFormat,
    DateFormat timeFormat,
  }) {
    if (this == null || localizer == null) return null;
    DateTime local = toLocal();
    String date = (dateFormat ?? DateFormat.MMMd()).format(local);
    String time = (timeFormat ?? DateFormat.jm()).format(local);
    return localizer(date, time);
  }
}
