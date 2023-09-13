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

import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:test/test.dart';

void main() {
  test('text returns only username if pronouns is null', () {
    String name = 'User Name';
    String? pronouns = null;

    UserName userName = UserName(name, pronouns);
    expect(userName.text, name);
  });

  test('text returns only username if pronouns is empty', () {
    String name = 'User Name';
    String pronouns = '';

    UserName userName = UserName(name, pronouns);
    expect(userName.text, name);
  });

  test('text returns formatted value if pronouns is valid', () {
    String name = 'User Name';
    String pronouns = 'pro/noun';

    UserName userName = UserName(name, pronouns);
    expect(userName.text, 'User Name (pro/noun)');
  });
}
