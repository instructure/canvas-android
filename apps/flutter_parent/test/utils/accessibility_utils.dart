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
  await expectLater(tester, meetsGuideline(SensibleDpadNavigationGuideline())); // Needs to be last, because it fiddles with UI
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

  void traverseFocusNodes(List<FocusNode> nodeList, FocusNode root) {
    Queue<FocusNode> nodeQueue = Queue<FocusNode>();
    nodeQueue.addLast(root);
    while(nodeQueue.isNotEmpty) {
      FocusNode node = nodeQueue.removeFirst();
      nodeList.add(node);
      node.children.forEach( (fn) {
        nodeQueue.addLast(fn);
//        bool isEditableText = fn.context.widget is EditableText;
//        print("FocusNode: $fn, Widget: ${fn.context.widget}, isEditableText: $isEditableText");
//
//        if(isEditableText) {
//          Widget w = fn.context.widget;
//          fn.requestFocus();
//          SemanticsNode sn = findFocusedNode(tester.binding.pipelineOwner.semanticsOwner.rootSemanticsNode);
//        }
      });
    }
  }

  void gatherFocusableNodes(Set<SemanticsNode> result, SemanticsNode root) {
    if(root.hasFlag(SemanticsFlag.isFocusable) && !root.isMergedIntoParent & !root.isInvisible && !root.hasFlag(SemanticsFlag.isHidden)) {
      result.add(root);
    }
    
    root.visitChildren((SemanticsNode child) {
      gatherFocusableNodes(result, child);
      return true;
    });
  }

  void showAllSemanticsNodes(SemanticsNode root) {
    print("SemanticsNode: $root");

    root.visitChildren( (child) {
      showAllSemanticsNodes(child);
      return true;
    });
  }
  
  void showFocusNodes(FocusNode root) {
    print("focusNode: $root");
    print("ancestors: ${root.ancestors.map( (fn) => fn.toString()).join(",")}");
    print("descendants: ${root.descendants.map( (fn) => fn.toString()).join(",")}");
    
    root.traversalChildren.forEach( (node) {showFocusNodes(node);});
  }

  SemanticsNode findFocusedNode(SemanticsNode root) {
    print("findFocusedNode: root = $root");
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

  Future<FocusNode> moveDown(WidgetTester tester) async {
    return move(tester, LogicalKeyboardKey.arrowDown);
  }

  Future<FocusNode> moveUp(WidgetTester tester) async {
    return move(tester, LogicalKeyboardKey.arrowUp);
  }

  Future<FocusNode> moveLeft(WidgetTester tester) async {
    return move(tester, LogicalKeyboardKey.arrowLeft);
  }

  Future<FocusNode> moveRight(WidgetTester tester) async {
    return move(tester, LogicalKeyboardKey.arrowRight);
  }

  Future<FocusNode> move(WidgetTester tester, LogicalKeyboardKey key) async {
    await tester.sendKeyEvent(key);
    await tester.pumpAndSettle();
    FocusNode  newFocus = tester.binding.focusManager.primaryFocus;
    return newFocus;
  }

  @override
  FutureOr<Evaluation> evaluate(WidgetTester tester) async {
    final SemanticsNode root = tester.binding.pipelineOwner.semanticsOwner
        .rootSemanticsNode;

    Evaluation result = Evaluation.pass();

    //List<FocusScope> allFocusScopeWidgets = tester.allWidgets.where( (w) => w.runtimeType == FocusScope);

//    tester.allWidgets.forEach((widget) {
//      if(widget.runtimeType == TextField) {
//        widget.
//        if(allFocusScopeWidgets.where( (fs) => fs.))
//        DiagnosticsNode dn = widget.toDiagnosticsNode();
//        tester.allWidgets.where( (w) => w.debugDescribeChildren().where( (d) => d.));
//      }
//
//    });
    String focusScopeWidgets = tester.allWidgets.where( (w) => w.runtimeType == FocusScope).map( (w) => w.toString()).join("\n");
    print("FocusScopes: $focusScopeWidgets");

    showAllSemanticsNodes(root);

    // Traversal logic that recurses to children
    Evaluation traverse(SemanticsNode node) {
      Evaluation result = const Evaluation.pass();

      node.visitChildren((node) {
        result += traverse(node);
        return true;
      });

      print("node = ${node}");
      if (node.hasFlag(SemanticsFlag.isTextField)) {
        node.sendEvent(TapSemanticEvent());
        SemanticsNode focusedNode = findFocusedNode(root);
        print("TextField $node, focused node = $focusedNode");
        SemanticsNode parent = node.parent;
        bool focusScopeFound = false;
        while (parent != null) {
          if (parent.runtimeType == FocusScope) {
            print("FOUND FocusScope $parent");
            focusScopeFound = true;
            break;
          }
          parent = parent.parent;
        }

        if (!focusScopeFound) {
          result += Evaluation.fail(
              "TextField $node does not have a wrapping FocusScope");
        }
      }

      return result;
    }

    Set<SemanticsNode> focusableNodes = Set<SemanticsNode>();
    gatherFocusableNodes(focusableNodes, root);

    //showFocusNodes(tester.binding.focusManager.primaryFocus);
    focusableNodes.forEach((SemanticsNode node) {
      print("Focusable: $node, id: ${node.id}");
    });

    List<FocusNode> focusNodeTraversal = List<FocusNode>();
    traverseFocusNodes(focusNodeTraversal, tester.binding.focusManager.rootScope);


    if(focusableNodes.length > 1) {
      for(FocusNode fn in focusNodeTraversal) {
        if(fn.context != null && fn.context.widget is EditableText) {
          fn.requestFocus();
          await tester.pumpAndSettle();
          FocusNode currFocus = tester.binding.focusManager.primaryFocus;
          print("EditableText Focusable: $fn, currFocus: $currFocus");
          FocusNode newFocus = null;

          newFocus = await moveUp(tester);
          if(newFocus == currFocus) {
            newFocus = await moveDown(tester);
          }
          if(newFocus == currFocus) {
            newFocus = await moveRight(tester);
          }
          if(newFocus == currFocus) {
            newFocus = await moveLeft(tester);
          }
          if(newFocus == currFocus) {
            SemanticsNode focusedSemanticsNode = findFocusedNode(root);
            result += Evaluation.fail("Directional nav stuck in $currFocus, Semantics: $focusedSemanticsNode"); // TODO: Left/Right
          }
        }
      }
    }

    return result;
  }
//      SemanticsNode upNode = await moveUp(tester);
//      print("up-node: $upNode");
//      upNode = await moveUp(tester);
//      print("up-node: $upNode");
//      upNode = await moveUp(tester);
//      print("up-node: $upNode");
//      upNode = await moveUp(tester);
//      print("up-node: $upNode");
//      upNode = await moveUp(tester);
//      print("up-node: $upNode");
//      upNode = await moveUp(tester);
//      print("up-node: $upNode");
//      upNode = await moveUp(tester);
//      print("up-node: $upNode");
//
//
//      //for(int i=0; i<10; i++) await moveUp(tester);
//
//      SemanticsNode leftNode = await moveLeft(tester);
//      print("left-node: $leftNode");
//      leftNode = await moveLeft(tester);
//      print("left-node: $leftNode");
//      leftNode = await moveLeft(tester);
//      print("left-node: $leftNode");
//      leftNode = await moveLeft(tester);
//      print("left-node: $leftNode");
//      leftNode = await moveLeft(tester);
//      print("left-node: $leftNode");
//      leftNode = await moveLeft(tester);
//      print("left-node: $leftNode");
//      leftNode = await moveLeft(tester);
//      print("left-node: $leftNode");
//      //for(int i=0; i<10; i++) await moveLeft(tester);
//
//
//
////      print("initial focus: ${findFocusedNode(root).toString()}");
////      await moveDown(tester);
////      print("focus after movedown: ${findFocusedNode(root).toString()}");
////
////      FocusNode focus = tester.binding.focusManager.primaryFocus;
////      print("initial focusNode:  $focus");
////      // Make sure we're the top-most focus node
////      if(focus.ancestors != null && focus.ancestors.length > 0) {
////        focus = focus.ancestors.last;
////        print("top-most focus node: $focus");
////        focus.requestFocus();
////        await tester.pumpAndSettle(Duration(milliseconds: 500));
////        await tester.pumpAndSettle();
////        print("focus after refocus: ${findFocusedNode(root).toString()}");
////      }
//
//      // Traverse the whole thing, starting from the top and going top-to-bottom, left-to-right
//      List<SemanticsNode> hitList = List<SemanticsNode>();
//      SemanticsNode currFocus = findFocusedNode(root);
//      print("initial focus: $currFocus");
//      SemanticsNode prevFocus = null;
//      while(currFocus != prevFocus) {
//        hitList.add(currFocus);
//        prevFocus = currFocus;
//        SemanticsNode rightFocus = await moveRight(tester);
//        if(rightFocus != currFocus) {
//          currFocus = rightFocus;
//        }
//        else {
//          currFocus = await moveDown(tester);
//        }
//      }
//
//      print("Nodes hit: ${hitList.map((node) => node.toString()).join("\n")}");
//
////      // Traverse downward
////      List<SemanticsNode> hitListDown = List<SemanticsNode>();
////      SemanticsNode prevFocus = null;
////      SemanticsNode currFocus = findFocusedNode(root);
////      if(currFocus != null) {
////        hitListDown.add(currFocus);
////      }
////      while(currFocus != prevFocus) {
////        prevFocus = currFocus;
////        currFocus = await moveDown(tester);
////        if (currFocus != prevFocus) {
////          hitListDown.add(currFocus);
////        }
////      }
////
////      // Traverse upward
////      List<SemanticsNode> hitListUp = List<SemanticsNode>();
////      prevFocus = null;
////      if(currFocus != null) {
////        hitListUp.add(currFocus);
////      }
////      while(currFocus != prevFocus) {
////        prevFocus = currFocus;
////        currFocus = await moveUp(tester);
////        if (currFocus != prevFocus) {
////          hitListUp.add(currFocus);
////        }
////      }
//
//
//
////      if(hitListUp.length != hitListDown.length) {
////        result += Evaluation.fail("""
////            down-navigation length different than up-navigation length
////            down-path=${hitListDown.map((node) => node.label).join(",")}
////            up-path=${hitListUp.map((node) => node.label).join(",")}
////            """);
////      }
//
//      focusableNodes.map((node) => node.label).forEach((label) {
//        if(!hitList.map((node) => node.label).contains(label)) {
//          result += Evaluation.fail("Label '$label' is focusable but did not appear in down-navigation\n");
//        }
////        if(!hitListUp.contains(focusableNode)) {
////          result += Evaluation.fail("$focusableNode is focusable but did not appear in up-navigation");
////        }
//      });
//
//      return result; // Returns aggregate result
//    }
//

//
//    List<FocusNode> focusNodeTraversal = List<FocusNode>();
//    traverseFocusNodes(focusNodeTraversal, tester.binding.focusManager.rootScope);
//
//    print("FOCUS Traversal order: ${focusNodeTraversal.map((fn) => fn.toString()).join("\n")}");
//
//    return traverse(root, focusableNodes); // Start traversing at the root.
//  }
}