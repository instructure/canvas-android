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

import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:meta/meta.dart';
import 'package:test_api/test_api.dart' as test_package;

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

    //await expectLater(tester, meetsGuideline(NoHintsGuideline()));
    await expectLater(tester, meetsGuideline(textContrastGuideline));
    await expectLater(tester, meetsGuideline(labeledTapTargetGuideline));
    await expectLater(tester, meetsGuideline(androidTapTargetGuideline));

    handle.dispose();
  }, skip: skip, timeout: timeout, initialTimeout: initialTimeout, semanticsEnabled: semanticsEnabled);
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
