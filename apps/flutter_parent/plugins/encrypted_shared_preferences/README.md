# Encrypted Shared preferences plugin

Wraps EncryptedSharedPreferences (on Android) no iOS support, providing
a persistent store for simple data. Data is persisted to disk asynchronously.
Neither platform can guarantee that writes will be persisted to disk after
returning and this plugin must not be used for storing critical data.

## Usage
To use this plugin, add `encrypted_shared_preferences` as a [dependency in your pubspec.yaml file]

### Example

``` dart
import 'package:flutter/material.dart';
import 'package:encrypted_shared_preferences/encrypted_shared_preferences.dart';

void main() {
  runApp(MaterialApp(
    home: Scaffold(
      body: Center(
      child: RaisedButton(
        onPressed: _incrementCounter,
        child: Text('Increment Counter'),
        ),
      ),
    ),
  ));
}

_incrementCounter() async {
  EncryptedSharedPreferences prefs = await EncryptedSharedPreferences.getInstance();
  int counter = (prefs.getInt('counter') ?? 0) + 1;
  print('Pressed $counter times.');
  await prefs.setInt('counter', counter);
}
```

### Testing

You can populate `EncryptedSharedPreferences` with initial values in your tests by running this code:

```dart
EncryptedSharedPreferences.setMockInitialValues (Map<String, dynamic> values);
```

# encrypted_shared_preferences_platform_interface

A common platform interface for the [`encrypted_shared_preferences`][1] plugin.

This interface allows platform-specific implementations of the `encrypted_shared_preferences`
plugin, as well as the plugin itself, to ensure they are supporting the
same interface.

# Usage

To implement a new platform-specific implementation of `encrypted_shared_preferences`, extend
[`EncryptedSharedPreferencesPlatform`][2] with an implementation that performs the
platform-specific behavior, and when you register your plugin, set the default
`EncryptedSharedPreferencesLoader` by calling the `EncryptedSharedPreferencesPlatform.loader` setter.

# Note on breaking changes

Strongly prefer non-breaking changes (such as adding a method to the interface)
over breaking changes for this package.

See https://flutter.dev/go/platform-interface-breaking-changes for a discussion
on why a less-clean interface is preferable to a breaking change.

[1]: ../encrypted_shared_preferences
[2]: lib/encrypted_shared_preferences_platform_interface.dart
