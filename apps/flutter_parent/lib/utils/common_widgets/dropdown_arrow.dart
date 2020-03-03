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

import 'package:flutter/material.dart';

/// A widget that renders a dropdown arrow which can animate between its 'collapsed' and 'expanded' states.
class DropdownArrow extends StatelessWidget {
  const DropdownArrow({
    Key key,
    this.size = 4,
    double strokeWidth,
    this.color = Colors.white,
    this.rotate = false,
    this.specificProgress = null,
  })  : this.strokeWidth = strokeWidth ?? size / 2,
        super(key: key);

  /// Specifies the height of the dropdown arrow. The width will always be twice this value.
  final double size;

  /// The thickness of the stroke used to draw the dropdown arrow. Defaults to half the value of [size].
  final double strokeWidth;

  /// The color of the dropdown arrow. Defaults to [Colors.white].
  final Color color;

  /// Whether this dropdown arrow should be rotated 180 degrees to indicate an 'expanded' state. Changing this value
  /// will animate the dropdown arrow to its new rotation. This value will be ignored if a non-null value has been
  /// specified for [specificProgress].
  final bool rotate;

  /// A specific rotation progress from 0.0 to 1.0, where 0.0 represents a 'collapsed' state and 1.0 represents an
  /// 'expanded' state. Specifying a non-null value here will cause the value of [rotate] to be ignored.
  ///
  /// This is useful for cases where the expand/collapse progress is manually tracked for elements associated with
  /// this dropdown arrow, e.g. the calendar widget where the user can swipe vertically to expand/collapse
  /// between a month view and a week view.
  final double specificProgress;

  @override
  Widget build(BuildContext context) {
    if (specificProgress != null) {
      return Transform.rotate(
        angle: specificProgress * -pi,
        child: CustomPaint(
          child: SizedBox(width: size * 2, height: size),
          painter: _DropdownArrowPainter(color, strokeWidth),
        ),
      );
    }
    return TweenAnimationBuilder(
      tween: Tween<double>(begin: 0, end: rotate ? -pi : 0),
      duration: Duration(milliseconds: 300),
      builder: (context, value, _) {
        return Transform.rotate(
          angle: value,
          child: CustomPaint(
            child: SizedBox(width: size * 2, height: size),
            painter: _DropdownArrowPainter(color, strokeWidth),
          ),
        );
      },
    );
  }
}

class _DropdownArrowPainter extends CustomPainter {
  _DropdownArrowPainter(Color color, double strokeWidth) {
    _arrowPaint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..color = color;
  }

  Paint _arrowPaint;

  @override
  void paint(Canvas canvas, Size size) {
    canvas.drawPath(
        Path()
          ..moveTo(0, 0)
          ..lineTo(size.width / 2, size.height)
          ..lineTo(size.width, 0),
        _arrowPaint);
  }

  @override
  bool shouldRepaint(CustomPainter oldDelegate) => false;
}
