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
import 'package:tuple/tuple.dart';

/// Utility class for applying styles to substrings ('slices') within a body of text.
abstract class StyleSlicer {
  const StyleSlicer({
    this.style,
    this.recognizer,
  });

  final TextStyle? style;
  final GestureRecognizer? recognizer;

  List<Tuple2> getSlices(String src);

  /// Applies styling to the given [source] text - as specified by the provided [slicers] - and returns the result
  /// as a TextSpan.
  ///
  /// If two or more slices overlap (i.e. multiple styles are specified for the same substring) then the intersection
  /// of the slices will be treated as an independent sub-slice whose style is a merger of the styles of the overlapping
  /// slices, where style properties of slicers located later in the list of [slicers] take precedence.
  ///
  /// Similarly, if a sub-slice would receive multiple gesture recognizers, only the [recognizer] of the slicer located
  /// later in the list of [slicers] will be used.
  ///
  /// A base style for the entire text can be applied by specifying [baseStyle]
  static TextSpan apply(String? source, List<StyleSlicer>? slicers, {TextStyle? baseStyle = const TextStyle()}) {
    if (source == null || source.isEmpty) return TextSpan(text: '');
    if (slicers == null || slicers.isEmpty) return TextSpan(text: source);

    // Create full-length slice for base style
    slicers.insert(0, RangeSlice(0, source.length, style: baseStyle));

    Map<int, List<_SlicerOp>> opsMap = {};

    slicers.forEach((slicer) {
      slicer.getSlices(source).forEach((slice) {
        opsMap.putIfAbsent(slice.item1, () => []).add(_SlicerOp(true, slicer));
        opsMap.putIfAbsent(slice.item2, () => []).add(_SlicerOp(false, slicer));
      });
    });

    List<int> slicePoints = opsMap.keys.toList()..sort();
    List<StyleSlicer?> currentSlicers = [];
    List<TextSpan> spans = [];

    for (int i = 0; i < slicePoints.length - 1; i++) {
      int start = slicePoints[i];
      int end = slicePoints[i + 1];

      opsMap[start]?.forEach((op) {
        if (op.isAdd) {
          currentSlicers.add(op.slicer);
        } else {
          currentSlicers.remove(op.slicer);
        }
      });

      String slice = source.substring(start, end);
      TextStyle style = _mergeStyles(currentSlicers);
      var recognizer = currentSlicers.lastWhere((it) => it?.recognizer != null, orElse: () => null)?.recognizer;

      spans.add(TextSpan(text: slice, style: style, recognizer: recognizer));
    }
    if (spans.length == 1) return spans[0];
    return TextSpan(children: spans);
  }

  static TextStyle _mergeStyles(List<StyleSlicer?> currentSlicers) {
    TextStyle style = TextStyle();
    currentSlicers.forEach((it) => style = style.merge(it?.style));
    return style;
  }
}

/// Represents an operation to add or remove a slicer from the list of currently-applied slicers
class _SlicerOp {
  final bool isAdd;
  final StyleSlicer slicer;
  _SlicerOp(this.isAdd, this.slicer);
}

/// Provides a single slice for the range starting at [start] and ending at [end].
class RangeSlice extends StyleSlicer {
  final Tuple2 range;

  RangeSlice(
    int start,
    int end, {
    TextStyle? style,
    GestureRecognizer? recognizer,
  })  : range = Tuple2(start, end),
        super(style: style, recognizer: recognizer);

  @override
  List<Tuple2> getSlices(String src) {
    return [range];
  }
}

/// Provides slices that match the given [pattern]. To limit the number of matches, specify a non-negative value
/// for [maxMatches].
class PatternSlice extends StyleSlicer {
  final Pattern? pattern;
  final int maxMatches;

  PatternSlice(
    this.pattern, {
    this.maxMatches = -1,
    TextStyle? style,
    GestureRecognizer? recognizer,
  }) : super(style: style, recognizer: recognizer);

  @override
  List<Tuple2> getSlices(String src) {
    if (pattern == null || pattern == '') return [];
    var matches = pattern!.allMatches(src);
    if (maxMatches > -1) matches = matches.take(maxMatches);
    return matches.map((it) => Tuple2(it.start, it.end)).toList();
  }
}

/// Provides italicized slices that match instances of the specified [pronoun] wrapped in parentheses. To limit the
/// number of matches, specify a non-negative value for [maxMatches].
class PronounSlice extends PatternSlice {
  PronounSlice(
    String? pronoun, {
    int maxMatches = -1,
    GestureRecognizer? recognizer,
  }) : super(
          pronoun == null || pronoun.isEmpty ? null : '($pronoun)',
          maxMatches: maxMatches,
          style: TextStyle(fontStyle: FontStyle.italic),
          recognizer: recognizer,
        );
}
