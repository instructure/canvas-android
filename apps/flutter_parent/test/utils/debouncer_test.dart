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

import 'package:flutter_parent/utils/debouncer.dart';
import 'package:test/test.dart';

void main() {
  test('debouncer calls callback', () async {
    var called = false;

    var debouncer = Debouncer(Duration(milliseconds: 10));
    debouncer.debounce(() {
      called = true;
    });

    await Future.delayed(const Duration(seconds: 20), () {});
    expect(called, true);
  });

  test('debouncer calls callback after duration', () async {
    var called = false;

    var debouncer = Debouncer(Duration(milliseconds: 30));
    debouncer.debounce(() {
      called = true;
    });

    // Make sure we haven't been called yet
    await Future.delayed(const Duration(milliseconds: 10), () {});
    expect(called, false);

    // Check to make sure we got called
    await Future.delayed(const Duration(milliseconds: 20), () {});
    expect(called, true);
  });

  test('debouncer does not call callback if called again before debounce', () async {
    var called = false;

    // First debounce call
    var debouncer = Debouncer(Duration(milliseconds: 50));
    debouncer.debounce(() {
      called = true;
    });

    // Second one should cancel out the first
    await Future.delayed(const Duration(milliseconds: 20), () {});
    debouncer.debounce(() {
      called = true;
    });

    // Should be at the threshold for the first debounce after this call
    await Future.delayed(const Duration(milliseconds: 30), () {});

    // Make sure we didn't call either of the debounce callbacks
    expect(called, false);
  });
}
