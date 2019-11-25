/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

// Accessibility-related utilities for our widget tests.

import 'dart:async';

import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
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
  bool initWebViewPlugin = false,
}) {
  testWidgets(description, (tester) async {
    _initPlatformForTests(initWebViewPlugin);

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

/// A helper method for doing any kind of shared platform setup
void _initPlatformForTests(bool initWebViewPlugin) {
  // Setup for package_info
  const MethodChannel('plugins.flutter.io/package_info').setMockMethodCallHandler((MethodCall methodCall) async {
    switch (methodCall.method) {
      case 'getAll':
        return <String, dynamic>{
          'appName': 'android_parent',
          'buildNumber': '10',
          'packageName': 'com.instructure.parentapp',
          'version': '2.0.0',
        };
      default:
        assert(false);
        return null;
    }
  });

  if (initWebViewPlugin) _initPlatformWebView();
}

/// WebView helpers. These are needed as web views tie into platform views. These are special though as the channel
/// name depends on the platform view's ID. This makes mocking these generically difficult as each id has a different
/// platform channel to register.
///
/// Inspired solution is a slimmed down version of the WebView test:
/// https://github.com/flutter/plugins/blob/3b71d6e9a4456505f0b079074fcbc9ba9f8e0e15/packages/webview_flutter/test/webview_flutter_test.dart
void _initPlatformWebView() {
  const MethodChannel('plugins.flutter.io/cookie_manager', const StandardMethodCodec())
      .setMockMethodCallHandler((_) => Future<bool>.sync(() => null));

  // Intercept when a web view is getting created so we can set up the platform channel
  SystemChannels.platform_views.setMockMethodCallHandler((call) {
    switch(call.method) {
      case 'create':
        final id = call.arguments['id'];
        MethodChannel('plugins.flutter.io/webview_$id', const StandardMethodCodec())
            .setMockMethodCallHandler((_) => Future<void>.sync(() {}));
        return Future<int>.sync(() => 1);
      default:
        return Future<void>.sync(() {});
    }
  });
}
