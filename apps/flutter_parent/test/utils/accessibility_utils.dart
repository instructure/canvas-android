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
  await expectLater(tester, meetsGuideline(SensibleDpadNavigationGuideline()));
  //await expectLater(tester, meetsGuideline(textContrastGuideline));
  await expectLater(tester, meetsGuideline(labeledTapTargetGuideline));
  await expectLater(tester, meetsGuideline(androidTapTargetGuideline));
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

class SensibleDpadNavigationGuideline extends AccessibilityGuideline {
  @override
  String get description => 'Dpad navigation should be sensible';

  SemanticsNode focusedNode = null;

  void gatherFocusableNodes(Set<SemanticsNode> result, SemanticsNode root) {
    if(root.hasFlag(SemanticsFlag.isFocusable) && !root.isMergedIntoParent & !root.isInvisible && !root.hasFlag(SemanticsFlag.isHidden)) {
      result.add(root);
    }
    
    root.visitChildren((SemanticsNode child) {
      gatherFocusableNodes(result, child);
      return true;
    });
  }

  SemanticsNode findFocusedNode(SemanticsNode root) {
    if(root.hasFlag(SemanticsFlag.isFocused) ) {
      focusedNode = root;
      //print("Found focused node! $focusedNode");
      return root;
    }

    focusedNode = null;
    root.visitChildren((SemanticsNode child) {
      if(focusedNode == null) {
        findFocusedNode(child);
      }
      return true;
    });

    return focusedNode;
  }

  @override
  FutureOr<Evaluation> evaluate(WidgetTester tester) {
    final SemanticsNode root = tester.binding.pipelineOwner.semanticsOwner.rootSemanticsNode;

    // Traversal logic that recurses to children
    Future<Evaluation> traverse(SemanticsNode node, Set<SemanticsNode> focusableNodes) async {
      Evaluation result = const Evaluation.pass();

      FocusNode focus = tester.binding.focusManager.primaryFocus;

      List<SemanticsNode> hitListDown = List<SemanticsNode>();
      FocusNode prevFocus = null;
      while(focus != prevFocus) {
        prevFocus = focus;
        await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
        await tester.pumpAndSettle(Duration(milliseconds: 1000));
        focus = tester.binding.focusManager.primaryFocus;
        if(focus != prevFocus) {
          SemanticsNode focusedNode = findFocusedNode(root);
          if (hitListDown.contains(focusedNode)) {
            result +=
                Evaluation.fail("$focusedNode hit twice in down-navigation");
          }
          else {
            hitListDown.add(focusedNode);
          }
        }
      }

      List<SemanticsNode> hitListUp = List<SemanticsNode>();
      SemanticsNode initialUpNode = findFocusedNode(root);
      if(initialUpNode != null) hitListUp.add(initialUpNode);
      prevFocus = null;
      while(focus != prevFocus) {
        prevFocus = focus;
        await tester.sendKeyEvent(LogicalKeyboardKey.arrowUp);
        await tester.pumpAndSettle(Duration(milliseconds: 1000));
        focus = tester.binding.focusManager.primaryFocus;
        if(focus != prevFocus) {
          SemanticsNode focusedNode = findFocusedNode(root);
          if (hitListUp.contains(focusedNode)) {
            result += Evaluation.fail(
                "$focusedNode hit twice in up-navigation, path=${hitListUp.map((
                    node) => node.label).join(",")}");
          }
          else {
            hitListUp.add(focusedNode);
          }
        }
      }

      if(hitListUp.length != hitListDown.length) {
        result += Evaluation.fail("""
            down-navigation length different than up-navigation length
            down-path=${hitListDown.map((node) => node.label).join(",")}
            up-path=${hitListUp.map((node) => node.label).join(",")}
            """);
      }

      focusableNodes.forEach((SemanticsNode focusableNode) {
        if(!hitListDown.contains(focusableNode)) {
          result += Evaluation.fail("$focusableNode is focusable but did not appear in down-navigation");
        }
        if(!hitListUp.contains(focusableNode)) {
          result += Evaluation.fail("$focusableNode is focusable but did not appear in up-navigation");
        }
      });

      return result; // Returns aggregate result
    }

    Set<SemanticsNode> focusableNodes = Set<SemanticsNode>();
    gatherFocusableNodes(focusableNodes, root);
//    focusableNodes.forEach((SemanticsNode node) {
//      print("Focusable: $node, id: ${node.id}");
//    });
    
    return traverse(root, focusableNodes); // Start traversing at the root.
  }
}