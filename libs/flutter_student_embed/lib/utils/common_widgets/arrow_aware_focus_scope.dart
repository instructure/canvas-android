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

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

// A key-catch callback for the class below
FocusOnKeyCallback _onDirectionKeyCallback = (node, event) {
  if (event is RawKeyDownEvent) {
    if (event.logicalKey == LogicalKeyboardKey.arrowDown) {
      node.focusInDirection(TraversalDirection.down);
      return true; // event handled
    }
    if (event.logicalKey == LogicalKeyboardKey.arrowUp) {
      node.focusInDirection(TraversalDirection.up);
      return true; // event handled
    }
    if (event.logicalKey == LogicalKeyboardKey.arrowLeft) {
      node.focusInDirection(TraversalDirection.left);
      return true; // event handled
    }
    if (event.logicalKey == LogicalKeyboardKey.arrowRight) {
      node.focusInDirection(TraversalDirection.right);
      return true; // event handled
    }
  }

  return false; // event not handled
};

// A FocusScope that properly handles directional-arrow presses (and dpad).
class ArrowAwareFocusScope extends FocusScope {
  ArrowAwareFocusScope({Widget child, FocusScopeNode node})
      : super(child: child, node: node, onKey: _onDirectionKeyCallback);
}
