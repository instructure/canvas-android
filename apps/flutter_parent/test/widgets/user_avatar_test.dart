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

import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/utils/common_widets/user_avatar.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('Question mark when short name is empty or null', () {
    var blank = User((b) => b
      ..shortName = ''
      ..build());

    var nullName = User((b) => b..build());

    expect(UserAvatar.getUserInitials(blank), equals('?'));
    expect(UserAvatar.getUserInitials(nullName), equals('?'));
  });

  test('Two initials when exactly two initials in short name', () {
    var user = User((b) => b
      ..shortName = 'Canvas Instructure'
      ..build());

    expect(UserAvatar.getUserInitials(user), equals('CI'));
  });

  test('One initial when more or less than two initials in short name', () {
    var more = User((b) => b
      ..shortName = 'Canvas by Instructure'
      ..build());

    var less = User((b) => b
      ..shortName = 'Canvas'
      ..build());

    expect(UserAvatar.getUserInitials(more), equals('C'));
    expect(UserAvatar.getUserInitials(less), equals('C'));
  });
}