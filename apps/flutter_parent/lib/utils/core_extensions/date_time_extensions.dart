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

/// Returns a locale usable by DateTime formatters. This will generally be the current locale, but will fall back
/// to the 'en' locale if the current locale is unsupported.
String get supportedDateLocale => DateFormat.localeExists(Intl.getCurrentLocale()) ? Intl.getCurrentLocale() : 'en';

extension Format on DateTime? {
  /// Formats this [DateTime] for the current locale using the provided localization function
  String? l10nFormat(
    String Function(String date, String time)? localizer, {
    DateFormat? dateFormat,
    DateFormat? timeFormat,
  }) {
    if (this == null || localizer == null) return null;
    DateTime local = this!.toLocal();
    String date = (dateFormat ?? DateFormat.MMMd(supportedDateLocale)).format(local);
    String time = (timeFormat ?? DateFormat.jm(supportedDateLocale)).format(local);
    return localizer(date, time);
  }

  bool isSameDayAs(DateTime? other) {
    if (this == null || other == null) return false;
    return this!.year == other.year && this!.month == other.month && this!.day == other.day;
  }

  DateTime? withFirstDayOfWeek() {
    if (this == null) return null;
    final firstDay = DateFormat(null, supportedDateLocale).dateSymbols.FIRSTDAYOFWEEK;
    var offset = (this!.weekday - 1 - firstDay) % 7;
    return DateTime(this!.year, this!.month, this!.day - offset);
  }

  int? get localDayOfWeek {
    if (this == null) return null;
    final firstDay = DateFormat(null, supportedDateLocale).dateSymbols.FIRSTDAYOFWEEK;
    return (this!.weekday - 1 - firstDay) % 7;
  }

  bool isWeekend() {
    if (this == null) return false;
    return DateFormat(null, supportedDateLocale).dateSymbols.WEEKENDRANGE.contains((this!.weekday - 1) % 7);
  }

  DateTime? withStartOfDay() => this == null ? null : DateTime(this!.year, this!.month, this!.day);

  DateTime? withEndOfDay() => this == null ? null : DateTime(this!.year, this!.month, this!.day, 23, 59, 59, 999);

  DateTime? withStartOfMonth() => this == null ? null : DateTime(this!.year, this!.month, 1);

  DateTime? withEndOfMonth() => this == null ? null : DateTime(this!.year, this!.month + 1, 0, 23, 59, 59, 999);

  /// Returns this DateTime rounded to the nearest date at midnight. In other words, if the time is before noon this
  /// will return the same date but with the time set to midnight. If the time is at noon or after noon, this will
  /// return the following day at midnight.
  DateTime? roundToMidnight() {
    if (this == null) {
      return null;
    } else if (this!.hour >= 12) {
      return DateTime(this!.year, this!.month, this!.day + 1);
    } else {
      return withStartOfDay();
    }
  }
}
