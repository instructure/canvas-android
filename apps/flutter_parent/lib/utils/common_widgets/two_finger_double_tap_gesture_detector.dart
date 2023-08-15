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
import 'package:flutter/material.dart';

class TwoFingerDoubleTapGestureDetector extends StatelessWidget {
  final Widget child;
  final VoidCallback onDoubleTap;
  final bool excludeFromSemantics;

  const TwoFingerDoubleTapGestureDetector({
    required this.child,
    required this.onDoubleTap,
    this.excludeFromSemantics = false,
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return RawGestureDetector(
      behavior: HitTestBehavior.opaque,
      child: child,
      excludeFromSemantics: excludeFromSemantics,
      gestures: {
        TwoFingerDoubleTapGestureRecognizer: GestureRecognizerFactoryWithHandlers<TwoFingerDoubleTapGestureRecognizer>(
          () => TwoFingerDoubleTapGestureRecognizer(),
          (TwoFingerDoubleTapGestureRecognizer instance) => instance.onDoubleTap = onDoubleTap,
        ),
      },
    );
  }
}

class TwoFingerDoubleTapGestureRecognizer extends MultiTapGestureRecognizer {
  VoidCallback? onDoubleTap;

  Map<int, DateTime> _downPointers = {};
  Map<int, DateTime> _upPointers = {};
  DateTime? _lastTwoFingerTap;

  TwoFingerDoubleTapGestureRecognizer() {
    onTapDown = _trackTapDown;
    onTapUp = _trackTapUp;
    onTapCancel = (_) => _reset();
  }

  void _trackTapDown(int pointer, TapDownDetails details) {
    _downPointers[pointer] = DateTime.now();
    if (_downPointers.length > 2) _reset();
  }

  void _trackTapUp(int pointer, TapUpDetails details) {
    DateTime? downTime = _downPointers.remove(pointer);
    if (downTime == null) return;
    DateTime upTime = DateTime.now();
    if (upTime.difference(downTime) < kLongPressTimeout) _upPointers[pointer] = upTime;
    if (_upPointers.length >= 2) {
      var upTimes = _upPointers.values.toList();
      if (upTimes[0].difference(upTimes[1]).abs() < kPressTimeout) {
        _trackTwoFingerTap();
      }
      _upPointers.clear();
      _downPointers.clear();
    }
  }

  void _trackTwoFingerTap() {
    DateTime tapTime = DateTime.now();
    DateTime? lastTap = _lastTwoFingerTap;
    _lastTwoFingerTap = tapTime;
    if (lastTap != null && tapTime.difference(lastTap) < kDoubleTapTimeout) {
      if (onDoubleTap != null) onDoubleTap!();
      _reset();
    }
  }

  void _reset() {
    _downPointers.clear();
    _upPointers.clear();
    _lastTwoFingerTap = null;
  }
}
