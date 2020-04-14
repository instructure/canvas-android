// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'package:encrypted_shared_preferences/shared_preferences_platform_interface.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group(EncryptedSharedPreferencesStorePlatform, () {
    test('disallows implementing interface', () {
      expect(
        () {
          EncryptedSharedPreferencesStorePlatform.instance = IllegalImplementation();
        },
        throwsAssertionError,
      );
    });
  });
}

class IllegalImplementation implements EncryptedSharedPreferencesStorePlatform {
  // Intentionally declare self as not a mock to trigger the
  // compliance check.
  @override
  bool get isMock => false;

  @override
  Future<bool> clear() {
    throw UnimplementedError();
  }

  @override
  Future<Map<String, Object>> getAll() {
    throw UnimplementedError();
  }

  @override
  Future<bool> remove(String key) {
    throw UnimplementedError();
  }

  @override
  Future<bool> setValue(String valueType, String key, Object value) {
    throw UnimplementedError();
  }
}
