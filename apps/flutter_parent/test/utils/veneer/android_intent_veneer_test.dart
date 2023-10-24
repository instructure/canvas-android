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
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_parent/utils/veneers/android_intent_veneer.dart';
import 'package:flutter_test/src/deprecated.dart';
import 'package:test/test.dart';

import '../test_app.dart';

// TODO Fix test
// We shouldn't test the platform channel interactions, instead we should test how we interact with the library.
void main() {
  setUp(() {
    setupPlatformChannels();
  });

  test('launch email with body calls a platform channel', () async {
    final subject = 'subject here';
    final emailBody = 'multi\r\nline\r\nbody\r\n';
    final completer = Completer();

    MethodChannel('intent').setMockMethodCallHandler((MethodCall call) async {
      expect(call.method, 'startActivity');
      expect(call.arguments['action'], 'android.intent.action.SENDTO');
      expect(call.arguments['data'], 'mailto:');
      expect(call.arguments['extra'], {
        'android.intent.extra.EMAIL': ['mobilesupport@instructure.com'], // By default this is used
        'android.intent.extra.SUBJECT': subject,
        'android.intent.extra.TEXT': emailBody,
      });

      completer.complete(); // Finish the completer so the test can finish
      return null;
    });

    AndroidIntentVeneer().launchEmailWithBody(subject, emailBody);

    await completer.future; // Wait for the completer to finish the test
  }, skip: true);

  test('launch telephone uri', () async {
    var telUri = 'tel:+123';
    final completer = Completer();

    MethodChannel('intent').setMockMethodCallHandler((MethodCall call) async {
      expect(call.method, 'startActivity');
      expect(call.arguments['action'], 'android.intent.action.DIAL');
      expect(call.arguments['data'], Uri.parse(telUri).toString());

      completer.complete(); // Finish the completer so the test can finish
      return null;
    });

    AndroidIntentVeneer().launchPhone(telUri);

    await completer.future; // Wait for the completer to finish the test
  }, skip: true);

  test('launches email uri', () async {
    var mailto = 'mailto:pandas@instructure.com';
    final completer = Completer();

    MethodChannel('intent').setMockMethodCallHandler((MethodCall call) async {
      expect(call.method, 'startActivity');
      expect(call.arguments['action'], 'android.intent.action.SENDTO');
      expect(call.arguments['data'], Uri.parse(mailto).toString());

      completer.complete(); // Finish the completer so the test can finish
      return null;
    });

    AndroidIntentVeneer().launchEmail(mailto);

    await completer.future;
  }, skip: true);
}
