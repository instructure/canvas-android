// Accessibility-related utilities for our widget tests.

import 'dart:async';

import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';
// For testWidgetsWithAccessibilityChecks call, timeout parameter
import 'package:test_api/test_api.dart' as test_package;
import 'package:meta/meta.dart';

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

  testWidgets(
      description,
          (tester) async {
        final handle = tester.ensureSemantics();
        await callback(tester);
        //await expectLater(tester, meetsGuideline(NoHintsGuideline()));
        await expectLater(tester, meetsGuideline(textContrastGuideline));
        await expectLater(tester, meetsGuideline(labeledTapTargetGuideline));
        await expectLater(tester, meetsGuideline(androidTapTargetGuideline));
        handle.dispose();
      },
      skip: skip,
      timeout: timeout,
      initialTimeout: initialTimeout,
      semanticsEnabled: semanticsEnabled );

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

      if(node.hint != null && node.hint.isNotEmpty ) {
        //print("Node $node hint = ${node.hint}");
        result += Evaluation.fail(
          '$node has hint \'${node.hint}\'!\n'
        );
      }

      return result; // Returns aggregate result
    }

    return traverse(root); // Start traversing at the root.
  }

}