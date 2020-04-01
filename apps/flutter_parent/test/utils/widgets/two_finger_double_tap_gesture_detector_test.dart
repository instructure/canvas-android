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

import 'package:flutter/cupertino.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/utils/common_widgets/two_finger_double_tap_gesture_detector.dart';
import 'package:flutter_test/flutter_test.dart';

import '../accessibility_utils.dart';
import '../test_app.dart';

void main() {
  testWidgetsWithAccessibilityChecks('Invokes callback on two-finger double-tap', (tester) async {
    bool called = false;
    await tester.pumpWidget(
      TestApp(
        TwoFingerDoubleTapGestureDetector(
          child: Container(color: Colors.grey),
          onDoubleTap: () => called = true,
        ),
      ),
    );
    await tester.pump();

    Offset center = tester.getCenter(find.byType(TwoFingerDoubleTapGestureDetector));

    // Perform first two-finger tap
    TestGesture pointer1 = await tester.startGesture(center.translate(-64, 0));
    TestGesture pointer2 = await tester.startGesture(center.translate(64, 0));
    await pointer1.up();
    await pointer2.up();

    // Perform second two-finger tap
    await tester.pump(Duration(milliseconds: 100));
    pointer1 = await tester.startGesture(center.translate(-64, 0));
    pointer2 = await tester.startGesture(center.translate(64, 0));
    await pointer1.up();
    await pointer2.up();
    await tester.pumpAndSettle();

    expect(called, isTrue);
  });

  testWidgetsWithAccessibilityChecks('Invokes callback on two-finger double-tap after two-finger-tap timeout',
      (tester) async {
    bool called = false;
    await tester.pumpWidget(
      TestApp(
        TwoFingerDoubleTapGestureDetector(
          child: Container(color: Colors.grey),
          onDoubleTap: () => called = true,
        ),
      ),
    );
    await tester.pump();

    Offset center = tester.getCenter(find.byType(TwoFingerDoubleTapGestureDetector));

    // Perform first two-finger tap
    TestGesture pointer1 = await tester.startGesture(center.translate(-64, 0));
    TestGesture pointer2 = await tester.startGesture(center.translate(64, 0));
    await tester.pump(Duration(milliseconds: 50));
    await pointer1.up();
    await pointer2.up();

    // Wait past double tap timeout (using runAsync here since pump won't advance the real clock)
    await tester.runAsync(() => Future.delayed(kDoubleTapTimeout + Duration(milliseconds: 100)));

    // Perform second two-finger tap
    pointer1 = await tester.startGesture(center.translate(-64, 0));
    pointer2 = await tester.startGesture(center.translate(64, 0));
    await tester.pump(Duration(milliseconds: 50));
    await pointer1.up();
    await pointer2.up();

    // Perform third two-finger tap
    await tester.pump(Duration(milliseconds: 200));
    pointer1 = await tester.startGesture(center.translate(-64, 0));
    pointer2 = await tester.startGesture(center.translate(64, 0));
    await tester.pump(Duration(milliseconds: 50));
    await pointer1.up();
    await pointer2.up();
    await tester.pumpAndSettle();

    expect(called, isTrue);
  });

  testWidgetsWithAccessibilityChecks('Does not invoke callback on single-finger tap', (tester) async {
    bool called = false;
    await tester.pumpWidget(
      TestApp(
        TwoFingerDoubleTapGestureDetector(
          child: Container(color: Colors.grey),
          onDoubleTap: () => called = true,
        ),
      ),
    );
    await tester.pump();

    // Perform single tap
    await tester.tap(find.byType(TwoFingerDoubleTapGestureDetector));
    await tester.pumpAndSettle();

    expect(called, isFalse);
  });

  testWidgetsWithAccessibilityChecks('Does not invoke callback on single-finger double tap', (tester) async {
    bool called = false;
    await tester.pumpWidget(
      TestApp(
        TwoFingerDoubleTapGestureDetector(
          child: Container(color: Colors.grey),
          onDoubleTap: () => called = true,
        ),
      ),
    );
    await tester.pump();

    // Perform single-finger double tap
    await tester.tap(find.byType(TwoFingerDoubleTapGestureDetector));
    await tester.pump(Duration(milliseconds: 100));
    await tester.tap(find.byType(TwoFingerDoubleTapGestureDetector));
    await tester.pumpAndSettle();

    expect(called, isFalse);
  });

  testWidgetsWithAccessibilityChecks('Does not invoke callback on two-finger tap', (tester) async {
    bool called = false;
    await tester.pumpWidget(
      TestApp(
        TwoFingerDoubleTapGestureDetector(
          child: Container(color: Colors.grey),
          onDoubleTap: () => called = true,
        ),
      ),
    );
    await tester.pump();

    Offset center = tester.getCenter(find.byType(TwoFingerDoubleTapGestureDetector));

    // Perform first two-finger tap
    TestGesture pointer1 = await tester.startGesture(center.translate(-64, 0));
    TestGesture pointer2 = await tester.startGesture(center.translate(64, 0));
    await pointer1.up();
    await pointer2.up();
    await tester.pumpAndSettle();

    expect(called, isFalse);
  });

  testWidgetsWithAccessibilityChecks('Does not invoke callback on triple-finger double-tap', (tester) async {
    bool called = false;
    await tester.pumpWidget(
      TestApp(
        TwoFingerDoubleTapGestureDetector(
          child: Container(color: Colors.grey),
          onDoubleTap: () => called = true,
        ),
      ),
    );
    await tester.pump();

    Offset center = tester.getCenter(find.byType(TwoFingerDoubleTapGestureDetector));

    // Perform first two-finger tap
    TestGesture pointer1 = await tester.startGesture(center.translate(-64, 0));
    TestGesture pointer2 = await tester.startGesture(center.translate(64, 0));
    TestGesture pointer3 = await tester.startGesture(center);
    await pointer1.up();
    await pointer2.up();
    await pointer3.up();

    // Perform second two-finger tap
    await tester.pump(Duration(milliseconds: 100));
    pointer1 = await tester.startGesture(center.translate(-64, 0));
    pointer2 = await tester.startGesture(center.translate(64, 0));
    pointer3 = await tester.startGesture(center);
    await pointer1.up();
    await pointer2.up();
    await pointer3.up();
    await tester.pumpAndSettle();

    expect(called, isFalse);
  });
}
