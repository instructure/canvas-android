// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:meta/meta.dart';

import 'shared_preferences_platform_interface.dart';

/// Wraps EncryptedSharedPreferences (on Android), providing a persistent store for simple data. No iOS support.
///
/// Data is persisted to disk asynchronously.
class EncryptedSharedPreferences {
  EncryptedSharedPreferences._(this._preferenceCache);

  static const String _prefix = 'flutter.';
  static Completer<EncryptedSharedPreferences>? _completer;

  static EncryptedSharedPreferencesStorePlatform get _store => EncryptedSharedPreferencesStorePlatform.instance;

  /// Loads and parses the [EncryptedSharedPreferences] for this app from disk.
  ///
  /// Because this is reading from disk, it shouldn't be awaited in
  /// performance-sensitive blocks.
  static Future<EncryptedSharedPreferences> getInstance() async {
    if (_completer == null) {
      _completer = Completer<EncryptedSharedPreferences>();
      try {
        final Map<String, Object> preferencesMap = await _getSharedPreferencesMap();
        _completer!.complete(EncryptedSharedPreferences._(preferencesMap));
      } on Exception catch (e) {
        // If there's an error, explicitly return the future with an error.
        // then set the completer to null so we can retry.
        _completer!.completeError(e);
        final Future<EncryptedSharedPreferences> sharedPrefsFuture = _completer!.future;
        _completer = null;
        return sharedPrefsFuture;
      }
    }
    return _completer!.future;
  }

  /// The cache that holds all preferences.
  ///
  /// It is instantiated to the current state of the EncryptedSharedPreferences or
  /// and then kept in sync via setter methods in this class.
  ///
  /// It is NOT guaranteed that this cache and the device prefs will remain
  /// in sync since the setter method might fail for any reason.
  final Map<String, dynamic> _preferenceCache;

  /// Returns all keys in the persistent storage.
  Set<String> getKeys() => Set<String>.from(_preferenceCache.keys);

  /// Reads a value of any type from persistent storage.
  dynamic get(String key) => _preferenceCache[key];

  /// Reads a value from persistent storage, throwing an exception if it's not a
  /// bool.
  bool? getBool(String key) => _preferenceCache[key] as bool?;

  /// Reads a value from persistent storage, throwing an exception if it's not
  /// an int.
  int? getInt(String key) => _preferenceCache[key] as int?;

  /// Reads a value from persistent storage, throwing an exception if it's not a
  /// double.
  double? getDouble(String key) => _preferenceCache[key] as double?;

  /// Reads a value from persistent storage, throwing an exception if it's not a
  /// String.
  String? getString(String key) => _preferenceCache[key] as String?;

  /// Returns true if persistent storage the contains the given [key].
  bool containsKey(String key) => _preferenceCache.containsKey(key);

  /// Reads a set of string values from persistent storage, throwing an
  /// exception if it's not a string set.
  List<String> getStringList(String key) {
    dynamic list = _preferenceCache[key];
    List<String>? castedList;
    try {
      castedList = (list as List<Object?>).map((e) => e as String).toList();
    } catch (e) {
      castedList = [];
    }
    // Make a copy of the list so that later mutations won't propagate
    return castedList;
  }

  /// Saves a boolean [value] to persistent storage in the background.
  ///
  /// If [value] is null, this is equivalent to calling [remove()] on the [key].
  Future<bool> setBool(String key, bool? value) => _setValue('Bool', key, value);

  /// Saves an integer [value] to persistent storage in the background.
  ///
  /// If [value] is null, this is equivalent to calling [remove()] on the [key].
  Future<bool> setInt(String key, int? value) => _setValue('Int', key, value);

  /// Saves a double [value] to persistent storage in the background.
  ///
  /// Android doesn't support storing doubles, so it will be stored as a float.
  ///
  /// If [value] is null, this is equivalent to calling [remove()] on the [key].
  Future<bool> setDouble(String key, double? value) => _setValue('Double', key, value);

  /// Saves a string [value] to persistent storage in the background.
  ///
  /// If [value] is null, this is equivalent to calling [remove()] on the [key].
  Future<bool> setString(String key, String? value) => _setValue('String', key, value);

  /// Saves a list of strings [value] to persistent storage in the background.
  ///
  /// If [value] is null, this is equivalent to calling [remove()] on the [key].
  Future<bool> setStringList(String key, List<String>? value) => _setValue('StringList', key, value);

  /// Removes an entry from persistent storage.
  Future<bool> remove(String key) => _setValue(null, key, null);

  Future<bool> _setValue(String? valueType, String key, Object? value) {
    final String prefixedKey = '$_prefix$key';
    if (value == null) {
      _preferenceCache.remove(key);
      return _store.remove(prefixedKey);
    } else {
      if (value is List<String>) {
        // Make a copy of the list so that later mutations won't propagate
        _preferenceCache[key] = [...value.toList()];
      } else {
        _preferenceCache[key] = value;
      }
      return _store.setValue(valueType!, prefixedKey, value);
    }
  }

  /// Always returns true.
  /// On iOS, synchronize is marked deprecated. On Android, we commit every set.
  @deprecated
  Future<bool> commit() async => true;

  /// Completes with true once the user preferences for the app has been cleared.
  Future<bool> clear() async {
    _preferenceCache.clear();
    return _store.clear();
  }

  /// Fetches the latest values from the host platform.
  ///
  /// Use this method to observe modifications that were made in native code
  /// (without using the plugin) while the app is running.
  Future<void> reload() async {
    final Map<String, Object> preferences = await EncryptedSharedPreferences._getSharedPreferencesMap();
    _preferenceCache.clear();
    _preferenceCache.addAll(preferences);
  }

  static Future<Map<String, Object>> _getSharedPreferencesMap() async {
    final Map<String, Object> fromSystem = await _store.getAll();
    // Strip the flutter. prefix from the returned preferences.
    final Map<String, Object> preferencesMap = <String, Object>{};
    for (String key in fromSystem.keys) {
      assert(key.startsWith(_prefix));
      preferencesMap[key.substring(_prefix.length)] = fromSystem[key]!;
    }
    return preferencesMap;
  }

  /// Initializes the shared preferences with mock values for testing.
  ///
  /// If the singleton instance has been initialized already, it is nullified.
  @visibleForTesting
  static void setMockInitialValues(Map<String, Object> values) {
    final Map<String, Object> newValues = values.map<String, Object>((String key, Object value) {
      String newKey = key;
      if (!key.startsWith(_prefix)) {
        newKey = '$_prefix$key';
      }
      return MapEntry<String, Object>(newKey, value);
    });
    EncryptedSharedPreferencesStorePlatform.instance = InMemoryEncryptedSharedPreferencesStore.withData(newValues);
    _completer = null;
  }
}
