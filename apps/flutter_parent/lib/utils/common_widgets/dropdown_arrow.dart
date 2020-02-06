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

class DropdownArrow extends StatelessWidget {
  const DropdownArrow({
    Key key,
    this.size = 4,
    double strokeWidth,
    this.color = Colors.white,
    this.rotate = false,
  })  : this.strokeWidth = strokeWidth ?? size / 2,
        super(key: key);

  final double size;
  final double strokeWidth;
  final Color color;
  final bool rotate;

  @override
  Widget build(BuildContext context) {
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
