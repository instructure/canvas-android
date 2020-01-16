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

import 'package:flutter_parent/models/user.dart';

class CanvasModelTestUtils {
  static User mockUser({String name, String pronouns, String primaryEmail, String id}) => User((b) => b
    ..id = id ?? Random(name.hashCode).nextInt(100000).toString()
    ..sortableName = name ?? 'sortableName'
    ..name = name ?? 'name'
    ..primaryEmail = primaryEmail ?? 'email'
    ..pronouns = pronouns ?? null
    ..locale = 'en'
    ..effectiveLocale = 'jp'
    ..avatarUrl = ''
    ..build());
}
