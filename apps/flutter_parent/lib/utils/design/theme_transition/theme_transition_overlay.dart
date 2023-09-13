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
import 'dart:typed_data';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_parent/utils/design/theme_transition/theme_transition_target.dart';

/// An overlay shown during a theme transition, where the 'old' theme is rendered on top of the theme and a
/// clipping mask is used to animated a circular reveal to the theme. Refer to [ThemeTransitionTarget] for
/// usage details.
class ThemeTransitionOverlay extends StatefulWidget {
  final Uint8List imageBytes;
  final Offset anchorCenter;
  final Function() onReady;

  const ThemeTransitionOverlay(
    this.imageBytes,
    this.anchorCenter,
    this.onReady, {
    super.key,
  });

  static display(BuildContext context, GlobalKey? anchorKey, Function() onReady) async {
    // Get center of anchor, which will be the origin point of the transition animation
    RenderBox box = anchorKey?.currentContext!.findRenderObject() as RenderBox;
    var anchorCenter = box.localToGlobal(box.size.center(Offset(0, 0)));

    // Get the target widget over which the animation will be displayed
    var target = ThemeTransitionTarget.of(context);
    if (target == null) throw 'ThemeTransitionTarget not found in the widget tree';

    // Get the repaint boundary of the target, render at 1/2 scale to a PNG image
    RenderRepaintBoundary boundary = ThemeTransitionTarget.of(context)!.boundaryKey.currentContext!.findRenderObject() as RenderRepaintBoundary;
    var scale = WidgetsBinding.instance.window.devicePixelRatio / 2;
    var img = await boundary.toImage(pixelRatio: scale);
    var byteData = await img.toByteData(format: ImageByteFormat.png);
    Uint8List pngBytes = byteData!.buffer.asUint8List();

    // Custom route to ThemeTransitionOverlay
    Navigator.of(context).push(
      PageRouteBuilder(
        opaque: false, // Ensures the underlying screen is rendered during the reveal animation
        pageBuilder: (BuildContext context, _, __) => ThemeTransitionOverlay(pngBytes, anchorCenter, onReady),
      ),
    );
  }

  @override
  _ThemeTransitionOverlayState createState() => _ThemeTransitionOverlayState();
}

class _ThemeTransitionOverlayState extends State<ThemeTransitionOverlay> with TickerProviderStateMixin {
  static const maxSplashOpacity = 0.35;

  late AnimationController _animationController;
  late Animation _animation;
  bool _onReadyCalled = false;

  @override
  void initState() {
    super.initState();

    _animationController = AnimationController(vsync: this, duration: Duration(milliseconds: 1000));

    _animationController.addListener(() {
      // Remove this overlay once the transition animation is complete
      if (_animationController.isCompleted) {
        Navigator.of(context).pop();
      }
    });
    _animation = CurvedAnimation(
      parent: _animationController,
      curve: Curves.easeInBack,
    );
    _animationController.forward();
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    var image = Image.memory(
      widget.imageBytes,
      repeat: ImageRepeat.repeat,
      filterQuality: FilterQuality.none,
    );

    // Wait for the image to finish loading before calling onReady, otherwise a race condition might cause the screen
    // to briefly flicker between the old and themes.
    image.image.resolve(ImageConfiguration()).addListener(ImageStreamListener((_, __) {
      if (!_onReadyCalled) {
        _onReadyCalled = true;
        widget.onReady();
      }
    }));

    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) {
        var size = MediaQuery.of(context).size;

        // Set up splash color. This should fade to zero opacity as the animation progresses.
        var splashColor = Theme.of(context)
            .splashColor
            .withOpacity(maxSplashOpacity - (maxSplashOpacity * _animation.value.clamp(0.0, 1.0)));

        // Set up the circle Rect that will serve as both the clipping bounds and the splash shape
        var rect = Rect.fromCircle(
          center: widget.anchorCenter,
          radius: _animation.value * sqrt(pow(size.width, 2) + pow(size.height, 2)),
        );

        // Wrap the clipper inside the CustomPaint so the splash renders on top rather than being clipped
        return CustomPaint(
          painter: _ThemeTransitionSplashPainter(splashColor, rect),
          child: ClipPath(
            clipper: _ThemeTransitionClipper(rect),
            child: image,
          ),
        );
      },
    );
  }
}

/// Performs the clip operation which reveals the theme underneath
class _ThemeTransitionClipper extends CustomClipper<Path> {
  Rect clipRect;

  _ThemeTransitionClipper(this.clipRect);

  @override
  Path getClip(Size size) {
    return Path()
      ..addRect(Rect.fromLTRB(0, 0, size.width, size.height))
      ..addOval(clipRect)
      ..fillType = PathFillType.evenOdd;
  }

  @override
  bool shouldReclip(CustomClipper<Path> oldClipper) => true;
}

/// Draws a ripple/splash over the transition animation
class _ThemeTransitionSplashPainter extends CustomPainter {
  _ThemeTransitionSplashPainter(this.color, this.splashRect);

  Color color;
  Rect splashRect;

  @override
  void paint(Canvas canvas, Size size) {
    canvas.drawPath(Path()..addOval(splashRect), Paint()..color = color);
  }

  @override
  bool shouldRepaint(CustomPainter oldDelegate) => false;
}
