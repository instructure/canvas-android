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

import 'package:flutter/gestures.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/utils/style_slicer.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('returns basic span if slicer list is null', () {
    final String source = 'User Name';
    final List<StyleSlicer>? slicers = null;

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.text, source);
    expect(actual.children, null);
  });

  test('returns basic span if slicer list is empty', () {
    final String source = 'User Name';
    final List<StyleSlicer> slicers = [];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.text, source);
    expect(actual.children, null);
  });

  test('ignores empty pronouns', () {
    final String source = 'User Name';
    final List<StyleSlicer> slicers = [PronounSlice('')];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.text, source);
    expect(actual.children, null);
  });

  test('ignores null slicers', () {
    final String source = 'User Name';
    final List<StyleSlicer> slicers = [];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.text, source);
    expect(actual.children, null);
  });

  test('returns empty span if source is empty', () {
    final String source = '';
    final List<StyleSlicer> slicers = [];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.text, '');
    expect(actual.children, null);
  });

  test('returns empty span if source is null', () {
    final String? source = null;
    final List<StyleSlicer> slicers = [];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.text, '');
    expect(actual.children, null);
  });

  test('returns basic span if source contains no matches', () {
    final String source = 'User Name';
    final List<StyleSlicer> slicers = [PronounSlice('pro/noun')];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.text, source);
    expect(actual.children, null);
  });

  test('returns correct span for single middle match', () {
    final String source = 'user (pro/noun) name';
    final List<StyleSlicer> slicers = [PronounSlice('pro/noun')];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 3);

    var spans = actual.children?.map((it) => it as TextSpan).toList();
    expect(spans?[0].text, 'user ');
    expect(spans?[1].text, '(pro/noun)');
    expect(spans?[1].style?.fontStyle, FontStyle.italic);
    expect(spans?[2].text, ' name');
  });

  test('returns correct span for single beginning match', () {
    final String source = '(pro/noun) user name';
    final List<StyleSlicer> slicers = [PronounSlice('pro/noun')];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 2);

    var spans = actual.children?.map((it) => it as TextSpan).toList();
    expect(spans?[0].text, '(pro/noun)');
    expect(spans?[0].style?.fontStyle, FontStyle.italic);
    expect(spans?[1].text, ' user name');
  });

  test('returns correct span for single end match', () {
    final String source = 'user name (pro/noun)';
    final List<StyleSlicer> slicers = [PronounSlice('pro/noun')];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 2);

    var spans = actual.children?.map((it) => it as TextSpan).toList();
    expect(spans?[0].text, 'user name ');
    expect(spans?[1].text, '(pro/noun)');
    expect(spans?[1].style?.fontStyle, FontStyle.italic);
  });

  test('returns correct span for multiple middle match', () {
    final String source = 'user (pro/noun) middle (pro/noun) name';
    final List<StyleSlicer> slicers = [PronounSlice('pro/noun')];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 5);

    var spans = actual.children?.map((it) => it as TextSpan).toList();
    expect(spans?[0].text, 'user ');
    expect(spans?[1].text, '(pro/noun)');
    expect(spans?[1].style?.fontStyle, FontStyle.italic);
    expect(spans?[2].text, ' middle ');
    expect(spans?[3].text, '(pro/noun)');
    expect(spans?[3].style?.fontStyle, FontStyle.italic);
    expect(spans?[4].text, ' name');
  });

  test('returns correct span for duplicate pronouns', () {
    final String source = 'user (pro/noun) middle (pro/noun) name';
    final List<StyleSlicer> slicers = [PronounSlice('pro/noun'), PronounSlice('pro/noun')];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 5);

    var spans = actual.children?.map((it) => it as TextSpan).toList();
    expect(spans?[0].text, 'user ');
    expect(spans?[1].text, '(pro/noun)');
    expect(spans?[1].style?.fontStyle, FontStyle.italic);
    expect(spans?[2].text, ' middle ');
    expect(spans?[3].text, '(pro/noun)');
    expect(spans?[3].style?.fontStyle, FontStyle.italic);
    expect(spans?[4].text, ' name');
  });

  test('returns correct span for multiple beginning match', () {
    final String source = '(pro/noun) user middle (pro/noun) name';
    final List<StyleSlicer> slicers = [PronounSlice('pro/noun')];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 4);

    var spans = actual.children?.map((it) => it as TextSpan).toList();
    expect(spans?[0].text, '(pro/noun)');
    expect(spans?[0].style?.fontStyle, FontStyle.italic);
    expect(spans?[1].text, ' user middle ');
    expect(spans?[2].text, '(pro/noun)');
    expect(spans?[2].style?.fontStyle, FontStyle.italic);
    expect(spans?[3].text, ' name');
  });

  test('returns correct span for multiple end match', () {
    final String source = 'user (pro/noun) middle name (pro/noun)';
    final List<StyleSlicer> slicers = [PronounSlice('pro/noun')];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 4);

    var spans = actual.children?.map((it) => it as TextSpan).toList();
    expect(spans?[0].text, 'user ');
    expect(spans?[1].text, '(pro/noun)');
    expect(spans?[1].style?.fontStyle, FontStyle.italic);
    expect(spans?[2].text, ' middle name ');
    expect(spans?[3].text, '(pro/noun)');
    expect(spans?[3].style?.fontStyle, FontStyle.italic);
  });

  test('returns correct span for multiple pronouns', () {
    final String source = 'user (pro/noun) middle (noun/pro) name';
    final List<StyleSlicer> slicers = [PronounSlice('pro/noun'), PronounSlice('noun/pro')];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 5);

    var spans = actual.children?.map((it) => it as TextSpan).toList();
    expect(spans?[0].text, 'user ');
    expect(spans?[1].text, '(pro/noun)');
    expect(spans?[1].style?.fontStyle, FontStyle.italic);
    expect(spans?[2].text, ' middle ');
    expect(spans?[3].text, '(noun/pro)');
    expect(spans?[3].style?.fontStyle, FontStyle.italic);
    expect(spans?[4].text, ' name');
  });

  test('returns correct span for adjacent pronouns', () {
    final String source = 'user (pro/noun)(noun/pro) name';
    final List<StyleSlicer> slicers = [PronounSlice('pro/noun'), PronounSlice('noun/pro')];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 4);

    var spans = actual.children?.map((it) => it as TextSpan).toList();
    expect(spans?[0].text, 'user ');
    expect(spans?[1].text, '(pro/noun)');
    expect(spans?[1].style?.fontStyle, FontStyle.italic);
    expect(spans?[2].text, '(noun/pro)');
    expect(spans?[2].style?.fontStyle, FontStyle.italic);
    expect(spans?[3].text, ' name');
  });

  test('returns correct span for overlapping styles', () {
    final String source = 'Normal Bold Bold-Small Small Normal';
    final List<StyleSlicer> slicers = [
      PatternSlice('Bold Bold-Small', style: TextStyle(fontWeight: FontWeight.bold)),
      PatternSlice('Bold-Small Small', style: TextStyle(fontSize: 8)),
    ];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 5);

    var spans = actual.children?.map((it) => it as TextSpan).toList();
    expect(spans?[0].text, 'Normal ');
    expect(spans?[0].style, TextStyle());

    expect(spans?[1].text, 'Bold ');
    expect(spans?[1].style, TextStyle(fontWeight: FontWeight.bold));

    expect(spans?[2].text, 'Bold-Small');
    expect(spans?[2].style, TextStyle(fontWeight: FontWeight.bold, fontSize: 8));

    expect(spans?[3].text, ' Small');
    expect(spans?[3].style, TextStyle(fontSize: 8));

    expect(spans?[4].text, ' Normal');
    expect(spans?[4].style, TextStyle());
  });

  test('uses last-declared gesture recognizer', () {
    final String source = 'Click here or here to proceed';
    final GestureRecognizer recognizer1 = TapGestureRecognizer();
    final GestureRecognizer recognizer2 = TapGestureRecognizer();

    final List<StyleSlicer> slicers = [
      PatternSlice('here or here to', style: TextStyle(), recognizer: recognizer1),
      PatternSlice('or here', style: TextStyle(), recognizer: recognizer2),
    ];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 5);

    var spans = actual.children?.map((it) => it as TextSpan).toList();
    expect(spans?[0].text, 'Click ');
    expect(spans?[0].recognizer, isNull);

    expect(spans?[1].text, 'here ');
    expect(spans?[1].recognizer, recognizer1);

    expect(spans?[2].text, 'or here');
    expect(spans?[2].recognizer, recognizer2);

    expect(spans?[3].text, ' to');
    expect(spans?[3].recognizer, recognizer1);

    expect(spans?[4].text, ' proceed');
    expect(spans?[4].recognizer, null);
  });

  test('PatternSlice finds all matches by default', () {
    final String source = List.generate(100, (_) => 'Hello World').join();
    final List<StyleSlicer> slicers = [PatternSlice('Hello World')];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 100);
  });

  test('PatternSlice limits matches if maxMatches is specified', () {
    final String source = List.generate(100, (_) => 'Hello World').join();
    final List<StyleSlicer> slicers = [PatternSlice('Hello World', maxMatches: 1)];

    TextSpan actual = StyleSlicer.apply(source, slicers);

    expect(actual.children?.length, 2);
  });
}
