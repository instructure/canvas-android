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
import 'package:vector_math/vector_math.dart' as Vector;

class CanvasLoadingIndicator extends StatefulWidget {
  const CanvasLoadingIndicator({
    this.size = 48,
    this.color = Colors.white,
    super.key,
  });

  final double size;
  final Color color;

  @override
  _CanvasLoadingIndicatorState createState() => _CanvasLoadingIndicatorState();
}

class _CanvasLoadingIndicatorState extends State<CanvasLoadingIndicator> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;
  late _CanvasLoadingIndicatorPainter _painter;

  @override
  void initState() {
    _controller = AnimationController(vsync: this);
    _animation = CurvedAnimation(parent: _controller, curve: Curves.easeInOutQuad);
    _controller.repeat(period: Duration(milliseconds: 600));
    _painter = _CanvasLoadingIndicatorPainter(
      _animation,
      widget.color,
    );
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      painter: _painter,
      child: SizedBox(
        width: widget.size,
        height: widget.size,
      ),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }
}

class _CanvasLoadingIndicatorPainter extends CustomPainter {
  _CanvasLoadingIndicatorPainter(this.animation, Color color) : super(repaint: animation) {
    _circlePaint = Paint()..color = color;
  }

  // Distance from view center where circles will 'spawn', as a percent of max ring radius
  static const fractionSpawnPointRadius = 0.31;

  // Maximum circle radius, as a percent of max ring radius
  static const fractionMaxCircleRadius = 0.29;

  // Radius if the inner ring, as a percent of max ring radius
  static const fractionInnerRingRadius = 1 / 3.0;

  // Number of circles in the design
  static const circleCount = 8;

  // The driving animation
  final Animation<double> animation;

  // The last progress value tracked, used to track iterations
  double _lastProgress = -1.0;

  // The current iteration
  int _iteration = 0;

  // Paint used to draw each circle
  late Paint _circlePaint;

  @override
  void paint(Canvas canvas, Size size) {
    // Determine animation progress and track iteration count
    if (animation.value < _lastProgress) _iteration++;
    _lastProgress = animation.value;

    // Set clipping mask */
    Path clipPath = Path();
    clipPath.addOval(Rect.fromPoints(size.topLeft(Offset.zero), size.bottomRight(Offset.zero)));
    canvas.clipPath(clipPath);

    // Determine full angle offset based iteration count
    var offset = 360.0 / circleCount;
    if ((_iteration + 1) % 4 != 0) offset = -offset;

    var center = size.center(Offset.zero);
    var maxRingRadius = center.dx;
    var spawnPointRadius = maxRingRadius * fractionSpawnPointRadius;
    var maxCircleRadius = maxRingRadius * fractionMaxCircleRadius;

    // Method that draws a single circle
    void _drawCircle(double radius, double angleRadians, double distanceFromCenter) {
      double x = center.dx + distanceFromCenter * cos(angleRadians);
      double y = center.dy + distanceFromCenter * sin(angleRadians);
      canvas.drawCircle(Offset(x, y), radius, _circlePaint);
    }

    // Method that draws a ring of circles
    void _drawCircleRing(double angleOffset, double growthPercent) {
      double radius = growthPercent * maxCircleRadius;
      double ringRadius = spawnPointRadius + (growthPercent * (maxRingRadius - spawnPointRadius));
      for (int i = 0; i < circleCount; i++) {
        _drawCircle(radius, Vector.radians(i * 45 + angleOffset), ringRadius);
      }
    }

    if (_iteration % 2 == 0) {
      // Draw zoom animation on even iterations
      double innerRingPercentage = animation.value * fractionInnerRingRadius;
      double outerRingPercentage = fractionInnerRingRadius + animation.value * (1 - fractionInnerRingRadius);
      double exitingRingPercentage = 1 + animation.value;
      _drawCircleRing(offset, innerRingPercentage);
      if (_iteration >= 1) _drawCircleRing(0, outerRingPercentage);
      if (_iteration >= 3) _drawCircleRing(0, exitingRingPercentage);
    } else {
      // Draw rotation animation on odd iterations
      offset = offset * (1 - animation.value);
      _drawCircleRing(offset, fractionInnerRingRadius);
      if (_iteration >= 2) _drawCircleRing(0, 1);
    }
  }

  @override
  bool shouldRepaint(CustomPainter oldDelegate) => false;
}
