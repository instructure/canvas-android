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

import 'dart:async';

/// A simple helper class that puts a [_duration] delay on the callback passed to [debounce].
/// Mainly used for when large amounts of user input can cause an unnecessary large amount of network calls
class Debouncer {
  final Duration _duration;
  Timer? _timer;

  Debouncer(this._duration);

  void debounce(void Function() callback) {
    if (_timer != null) {
      _timer!.cancel();
    }
    _timer = Timer(_duration, callback);
  }
}
