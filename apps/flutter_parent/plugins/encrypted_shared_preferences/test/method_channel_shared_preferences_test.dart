// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:encrypted_shared_preferences/method_channel_shared_preferences.dart';
import 'package:encrypted_shared_preferences/shared_preferences_platform_interface.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group(MethodChannelEncryptedSharedPreferencesStore, () {
    const MethodChannel channel = MethodChannel(
      'com.instructure.parentapp/encrypted_shared_preferences',
    );

    const Map<String, Object> kTestValues = <String, Object>{
      'flutter.String': 'hello world',
      'flutter.Bool': true,
      'flutter.Int': 42,
      'flutter.Double': 3.14159,
      'flutter.StringList': <String>['foo', 'bar'],
    };

    InMemoryEncryptedSharedPreferencesStore? testData;

    final List<MethodCall> log = <MethodCall>[];
    MethodChannelEncryptedSharedPreferencesStore? store;

    setUp(() async {
      testData = InMemoryEncryptedSharedPreferencesStore.empty();

      channel.setMockMethodCallHandler((MethodCall methodCall) async {
        log.add(methodCall);
        if (methodCall.method == 'getAll') {
          return await testData?.getAll();
        }
        if (methodCall.method == 'remove') {
          final String key = methodCall.arguments['key'];
          return await testData?.remove(key);
        }
        if (methodCall.method == 'clear') {
          return await testData?.clear();
        }
        final RegExp setterRegExp = RegExp(r'set(.*)');
        final Match? match = setterRegExp.matchAsPrefix(methodCall.method);
        if (match != null && match.groupCount == 1) {
          final String valueType = match.group(1) ?? '';
          final String key = methodCall.arguments['key'];
          final Object value = methodCall.arguments['value'];
          return await testData?.setValue(valueType, key, value);
        }
        fail('Unexpected method call: ${methodCall.method}');
      });
      store = MethodChannelEncryptedSharedPreferencesStore();
      log.clear();
    });

    tearDown(() async {
      await testData?.clear();
      store = null;
      testData = null;
    });

    test('getAll', () async {
      testData = InMemoryEncryptedSharedPreferencesStore.withData(kTestValues);
      expect(await store?.getAll(), kTestValues);
      expect(log.single.method, 'getAll');
    });

    test('remove', () async {
      testData = InMemoryEncryptedSharedPreferencesStore.withData(kTestValues);
      expect(await store?.remove('flutter.String'), true);
      expect(await store?.remove('flutter.Bool'), true);
      expect(await store?.remove('flutter.Int'), true);
      expect(await store?.remove('flutter.Double'), true);
      expect(await testData?.getAll(), <String, dynamic>{
        'flutter.StringList': <String>['foo', 'bar'],
      });

      expect(log, hasLength(4));
      for (MethodCall call in log) {
        expect(call.method, 'remove');
      }
    });

    test('setValue', () async {
      expect(await testData?.getAll(), isEmpty);
      for (String key in kTestValues.keys) {
        final dynamic value = kTestValues[key];
        expect(await store?.setValue(key.split('.').last, key, value), true);
      }
      expect(await testData?.getAll(), kTestValues);

      expect(log, hasLength(5));
      expect(log[0].method, 'setString');
      expect(log[1].method, 'setBool');
      expect(log[2].method, 'setInt');
      expect(log[3].method, 'setDouble');
      expect(log[4].method, 'setStringList');
    });

    test('clear', () async {
      testData = InMemoryEncryptedSharedPreferencesStore.withData(kTestValues);
      expect(await testData?.getAll(), isNotEmpty);
      expect(await store?.clear(), true);
      expect(await testData?.getAll(), isEmpty);
      expect(log.single.method, 'clear');
    });
  });
}
