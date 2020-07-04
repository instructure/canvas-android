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

import 'package:flutter_parent/models/snicker_doodle.dart';

/// A list of SnickerDoodles that will show up in the right-hand drawer of the login landing page
///
/// To edit this file and have git ignore your local changes, run this command:
/// git update-index --skip-worktree apps/flutter_parent/lib/utils/snickers.dart
///
/// To start tracking local changes again, run this command:
/// git update-index --no-skip-worktree apps/flutter_parent/lib/utils/snickers.dart
const List<SnickerDoodle> SNICKERS = const [
  const SnickerDoodle(
    title: "Example",
    subtitle: "This does not work",
    username: "user",
    password: "pass",
    domain: "labs.aylearn.net",
  ),
];
