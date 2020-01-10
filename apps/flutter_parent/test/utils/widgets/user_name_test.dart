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

import 'package:flutter/cupertino.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:test/test.dart';

void main() {
  test('stylize returns basic span if pronouns is null', () {
    final String source = 'User Name';
    final List<String> pronouns = null;

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.text, source);
    expect(actual.children, null);
  });

  test('stylize returns basic span if pronouns is empty', () {
    final String source = 'User Name';
    final List<String> pronouns = [];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.text, source);
    expect(actual.children, null);
  });

  test('stylize ignores empty pronouns', () {
    final String source = 'User Name';
    final List<String> pronouns = [''];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.text, source);
    expect(actual.children, null);
  });

  test('stylize ignores null pronouns', () {
    final String source = 'User Name';
    final List<String> pronouns = [null];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.text, source);
    expect(actual.children, null);
  });

  test('stylize returns empty span if source is empty', () {
    final String source = '';
    final List<String> pronouns = [];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.text, '');
    expect(actual.children, null);
  });

  test('stylize returns empty span if source is null', () {
    final String source = null;
    final List<String> pronouns = [];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.text, '');
    expect(actual.children, null);
  });

  test('stylize returns basic span source contains no matches', () {
    final String source = 'User Name';
    final List<String> pronouns = ['pro/noun'];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.text, source);
    expect(actual.children, null);
  });

  test('stylize returns correct span for single middle match', () {
    final String source = 'user (pro/noun) name';
    final List<String> pronouns = ['pro/noun'];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.children.length, 3);

    var spans = actual.children.map((it) => it as TextSpan).toList();
    expect(spans[0].text, 'user ');
    expect(spans[1].text, '(pro/noun)');
    expect(spans[1].style.fontStyle, FontStyle.italic);
    expect(spans[2].text, ' name');
  });

  test('stylize returns correct span for single beginning match', () {
    final String source = '(pro/noun) user name';
    final List<String> pronouns = ['pro/noun'];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.children.length, 2);

    var spans = actual.children.map((it) => it as TextSpan).toList();
    expect(spans[0].text, '(pro/noun)');
    expect(spans[0].style.fontStyle, FontStyle.italic);
    expect(spans[1].text, ' user name');
  });

  test('stylize returns correct span for single end match', () {
    final String source = 'user name (pro/noun)';
    final List<String> pronouns = ['pro/noun'];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.children.length, 2);

    var spans = actual.children.map((it) => it as TextSpan).toList();
    expect(spans[0].text, 'user name ');
    expect(spans[1].text, '(pro/noun)');
    expect(spans[1].style.fontStyle, FontStyle.italic);
  });

  test('stylize returns correct span for multiple middle match', () {
    final String source = 'user (pro/noun) middle (pro/noun) name';
    final List<String> pronouns = ['pro/noun'];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.children.length, 5);

    var spans = actual.children.map((it) => it as TextSpan).toList();
    expect(spans[0].text, 'user ');
    expect(spans[1].text, '(pro/noun)');
    expect(spans[1].style.fontStyle, FontStyle.italic);
    expect(spans[2].text, ' middle ');
    expect(spans[3].text, '(pro/noun)');
    expect(spans[3].style.fontStyle, FontStyle.italic);
    expect(spans[4].text, ' name');
  });

  test('stylize returns correct span for multiple beginning match', () {
    final String source = '(pro/noun) user middle (pro/noun) name';
    final List<String> pronouns = ['pro/noun'];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.children.length, 4);

    var spans = actual.children.map((it) => it as TextSpan).toList();
    expect(spans[0].text, '(pro/noun)');
    expect(spans[0].style.fontStyle, FontStyle.italic);
    expect(spans[1].text, ' user middle ');
    expect(spans[2].text, '(pro/noun)');
    expect(spans[2].style.fontStyle, FontStyle.italic);
    expect(spans[3].text, ' name');
  });

  test('stylize returns correct span for multiple end match', () {
    final String source = 'user (pro/noun) middle name (pro/noun)';
    final List<String> pronouns = ['pro/noun'];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.children.length, 4);

    var spans = actual.children.map((it) => it as TextSpan).toList();
    expect(spans[0].text, 'user ');
    expect(spans[1].text, '(pro/noun)');
    expect(spans[1].style.fontStyle, FontStyle.italic);
    expect(spans[2].text, ' middle name ');
    expect(spans[3].text, '(pro/noun)');
    expect(spans[3].style.fontStyle, FontStyle.italic);
  });

  test('stylize returns correct span for multiple pronouns', () {
    final String source = 'user (pro/noun) middle (noun/pro) name';
    final List<String> pronouns = ['pro/noun', 'noun/pro'];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.children.length, 5);

    var spans = actual.children.map((it) => it as TextSpan).toList();
    expect(spans[0].text, 'user ');
    expect(spans[1].text, '(pro/noun)');
    expect(spans[1].style.fontStyle, FontStyle.italic);
    expect(spans[2].text, ' middle ');
    expect(spans[3].text, '(noun/pro)');
    expect(spans[3].style.fontStyle, FontStyle.italic);
    expect(spans[4].text, ' name');
  });

  test('stylize returns correct span for adjacent pronouns', () {
    final String source = 'user (pro/noun)(noun/pro) name';
    final List<String> pronouns = ['pro/noun', 'noun/pro'];

    TextSpan actual = UserName.stylize(source, pronouns);

    expect(actual.children.length, 4);

    var spans = actual.children.map((it) => it as TextSpan).toList();
    expect(spans[0].text, 'user ');
    expect(spans[1].text, '(pro/noun)');
    expect(spans[1].style.fontStyle, FontStyle.italic);
    expect(spans[2].text, '(noun/pro)');
    expect(spans[2].style.fontStyle, FontStyle.italic);
    expect(spans[3].text, ' name');
  });
}
