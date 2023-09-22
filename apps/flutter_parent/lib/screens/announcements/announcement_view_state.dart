/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:flutter_parent/models/attachment.dart';

class AnnouncementViewState {
  final String _toolbarTitle;
  final String _announcementTitle;
  final String _announcementMessage;
  final DateTime? _postedAt;
  final Attachment? _attachment;

  AnnouncementViewState(
    this._toolbarTitle,
    this._announcementTitle,
    this._announcementMessage,
    this._postedAt,
    this._attachment,
  );

  String get toolbarTitle => _toolbarTitle;

  String get announcementTitle => _announcementTitle;

  String get announcementMessage => _announcementMessage;

  DateTime? get postedAt => _postedAt;

  Attachment? get attachment => _attachment;
}
