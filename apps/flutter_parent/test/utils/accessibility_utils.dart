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

// Accessibility-related utilities for our widget tests.

import 'dart:async';
import 'dart:collection';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:meta/meta.dart';
import 'package:test/test.dart' as test_package;

// A testWidgets() wrapper that runs accessibility checks
@isTest
void testWidgetsWithAccessibilityChecks(
  String description,
  WidgetTesterCallback callback, {
  bool skip = false,
  test_package.Timeout timeout,
  Duration initialTimeout,
  bool semanticsEnabled = true,
}) {
  testWidgets(description, (tester) async {
    final handle = tester.ensureSemantics();
    await callback(tester);

    // Run our accessibility test suite at the end of the test.
    await runAccessibilityTests(tester);

    handle.dispose();
  }, skip: skip, timeout: timeout, initialTimeout: initialTimeout, semanticsEnabled: semanticsEnabled);
}

// Break this out into its own method, so that it can be used mid-test.
Future<void> runAccessibilityTests(WidgetTester tester) async {
  await expectLater(tester, meetsGuideline(textContrastGuideline));
  await expectLater(tester, meetsGuideline(labeledTapTargetGuideline));
  await expectLater(tester, meetsGuideline(androidTapTargetGuideline));
  await expectLater(tester, meetsGuideline(TextFieldNavigationGuideline())); // Needs to be last, because it fiddles with UI
}

// Here's an example of a custom guideline.  We can conceivably write
// additional custom guidelines to pick up the slack of what the out-of-the-box
// guidelines don't cover.
//
// This is just a sample.  Of course it's normally OK for widgets to have hints.
class NoHintsGuideline extends AccessibilityGuideline {
  @override
  String get description => 'Widgets should not have hints';

  @override
  FutureOr<Evaluation> evaluate(WidgetTester tester) {
    final SemanticsNode root = tester.binding.pipelineOwner.semanticsOwner.rootSemanticsNode;

    // Traversal logic that recurses to children
    Evaluation traverse(SemanticsNode node) {
      Evaluation result = const Evaluation.pass();
      node.visitChildren((SemanticsNode child) {
        result += traverse(child);
        return true;
      });

      if (node.hint != null && node.hint.isNotEmpty) {
        //print("Node $node hint = ${node.hint}");
        result += Evaluation.fail('$node has hint \'${node.hint}\'!\n');
      }

      return result; // Returns aggregate result
    }

    return traverse(root); // Start traversing at the root.
  }
}

/*
 * An a11y checker that insures that we can navigate out of TextFields using dpad arrows.
 */
class TextFieldNavigationGuideline extends AccessibilityGuideline {
  @override
  String get description => 'You should be able to direction-arrow out of TextFields';

  // Grab the FocusNodes associated with this screen
  List<FocusNode> _getFocusNodes(FocusNode root) {
    List<FocusNode> result = List<FocusNode>();
    Queue<FocusNode> nodeQueue = Queue<FocusNode>();
    nodeQueue.addLast(root);
    while(nodeQueue.isNotEmpty) {
      FocusNode node = nodeQueue.removeFirst();
      result.add(node);
      node.children.forEach( (fn) {
        nodeQueue.addLast(fn);
      });
    }

    return result;
  }

  // Grab the focusable SemanticsNodes associated with this screen.
  List<SemanticsNode> _getFocusableSemanticsNodes(SemanticsNode root) {
    List<SemanticsNode> result = List<SemanticsNode>();
    if(root.hasFlag(SemanticsFlag.isFocusable) && !root.isMergedIntoParent & !root.isInvisible && !root.hasFlag(SemanticsFlag.isHidden)) {
      result.add(root);
    }
    
    root.visitChildren((SemanticsNode child) {
      result.addAll(_getFocusableSemanticsNodes(child));
      return true;
    });

    return result;
  }

  // Attempt to find the SemanticsNode that is currently focused.
  SemanticsNode _findFocusedNode(SemanticsNode root) {
    //print("findFocusedNode: root = $root");

    if(root.hasFlag(SemanticsFlag.isFocused) ) {
      //print("Found focused node! $focusedNode");
      return root;
    }

    SemanticsNode result = null;
    root.visitChildren((SemanticsNode child) {
      if(result == null) {
        result = _findFocusedNode(child);
      }
      return true;
    });

    return result;
  }

  // Cause an arrow-down to be sent to the screen
  Future<FocusNode> _moveDown(WidgetTester tester) async {
    return _move(tester, LogicalKeyboardKey.arrowDown);
  }

  // Cause an arrow-up to be sent to the screen
  Future<FocusNode> _moveUp(WidgetTester tester) async {
    return _move(tester, LogicalKeyboardKey.arrowUp);
  }

  // Cause an arrow-left to be sent to the screen
  Future<FocusNode> _moveLeft(WidgetTester tester) async {
    return _move(tester, LogicalKeyboardKey.arrowLeft);
  }

  // Cause an arrow-right to be sent to the screen
  Future<FocusNode> _moveRight(WidgetTester tester) async {
    return _move(tester, LogicalKeyboardKey.arrowRight);
  }

  // Common logic for arrow-moving
  Future<FocusNode> _move(WidgetTester tester, LogicalKeyboardKey key) async {
    await tester.sendKeyEvent(key);
    await tester.pumpAndSettle();
    FocusNode  newFocus = tester.binding.focusManager.primaryFocus;
    return newFocus;
  }

  @override
  FutureOr<Evaluation> evaluate(WidgetTester tester) async {
    final SemanticsNode root = tester.binding.pipelineOwner.semanticsOwner.rootSemanticsNode;

    // Default result
    Evaluation result = Evaluation.pass();

    // Gather all focusable SemanticsNodes for our screen
    List<SemanticsNode> focusableSemanticsNodes = _getFocusableSemanticsNodes(root);

    // Compute all eligible FocusNodes on our screen.
    List<FocusNode> focusNodes = _getFocusNodes(tester.binding.focusManager.rootScope);


    if(focusableSemanticsNodes.length > 1) { // Only test navigability if there is something else to which to navigate.
      for(FocusNode fn in focusNodes) {
        if(fn.context != null && fn.context.widget is EditableText) {
          // For each EditableText that we encounter, tap on it to focus it...
          fn.requestFocus();
          await tester.pumpAndSettle();

          // and then try to navigate out of it.
          FocusNode currFocus = tester.binding.focusManager.primaryFocus;
          FocusNode newFocus = await _moveUp(tester);
          if(newFocus == currFocus) {
            newFocus = await _moveDown(tester);
          }
          if(newFocus == currFocus) {
            newFocus = await _moveRight(tester);
          }
          if(newFocus == currFocus) {
            newFocus = await _moveLeft(tester);
          }
          if(newFocus == currFocus) {
            // Attempt to correlate a SemanticsNode with our failed FocusNode
            SemanticsNode focusedSemanticsNode = _findFocusedNode(root);
            result += Evaluation.fail("Directional nav stuck in $currFocus, Semantics: $focusedSemanticsNode\n");
          }
        }
      }
    }

    return result;
  }
}