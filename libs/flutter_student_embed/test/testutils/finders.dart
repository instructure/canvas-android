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

import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

Type typeOf<T>() => T;

extension Finders on CommonFinders {
  Finder richText(String text, {bool skipOffstage = true}) => RichTextFinder(text, skipOffstage: skipOffstage);
}

class RichTextFinder extends MatchFinder {
  RichTextFinder(this.text, {bool skipOffstage = true}) : super(skipOffstage: skipOffstage);

  final String text;

  @override
  String get description => 'rich text "$text"';

  @override
  bool matches(Element candidate) {
    final Widget widget = candidate.widget;
    if (widget is RichText) {
      return widget.text.toPlainText() == text;
    }
    return false;
  }
}
