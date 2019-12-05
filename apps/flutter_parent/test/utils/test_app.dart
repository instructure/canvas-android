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

import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:get_it/get_it.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'platform_config.dart';

class TestApp extends StatefulWidget {
  TestApp(
    this.home, {
    this.platformConfig = const PlatformConfig(),
    this.navigatorObservers = const [],
    this.darkMode = false,
    this.highContrast = false,
  });

  final Widget home;
  final PlatformConfig platformConfig;
  final List<NavigatorObserver> navigatorObservers;
  final bool darkMode;
  final bool highContrast;

  @override
  _TestAppState createState() => _TestAppState();
}

class _TestAppState extends State<TestApp> {
  Locale _locale;

  rebuild(locale) {
    setState(() => _locale = locale);
  }

  @override
  void initState() {
    super.initState();

    setupPlatformChannels(config: widget.platformConfig);
  }

  @override
  Widget build(BuildContext context) {
    return ParentTheme(
      initWithDarkMode: widget.darkMode,
      initWithHCMode: widget.highContrast,
      builder: (context, themeData) => MaterialApp(
        title: 'Canvas Parent',
        locale: _locale,
        navigatorObservers: widget.navigatorObservers,
        localizationsDelegates: const [
          AppLocalizations.delegate,
          // Material components use these delegate to provide default localization
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
        ],
        supportedLocales: AppLocalizations.delegate.supportedLocales,
        localeResolutionCallback: _localeCallback(),
        theme: themeData,
        home: Material(child: widget.home),
      ),
    );
  }

  // Get notified when there's a new system locale so we can rebuild the app with the new language
  LocaleResolutionCallback _localeCallback() => (locale, supportedLocales) {
        const fallback = Locale("en", "");
        Locale resolvedLocale =
            AppLocalizations.delegate.resolution(fallback: fallback, matchCountry: false)(locale, supportedLocales);

        // Update the state if the locale changed
        if (_locale != resolvedLocale) {
          SchedulerBinding.instance.addPostFrameCallback((_) {
            setState(() => _locale = resolvedLocale);
          });
        }

        return resolvedLocale;
      };
}

void setupTestLocator(config(GetIt locator)) {
  final locator = GetIt.instance;
  locator.reset();
  config(locator);
}

/// Set up the platform channels used by the app.
///
/// Returned is a future that completes when all async tasks spawned by this method call have completed. Waiting isn't
/// required, though is probably good practice and results in more stable tests.
Future<void> setupPlatformChannels({PlatformConfig config = const PlatformConfig()}) {
  if (config.initPackageInfo) {
    const MethodChannel('plugins.flutter.io/package_info').setMockMethodCallHandler((MethodCall methodCall) async {
      if (methodCall.method == 'getAll') {
        return <String, dynamic>{
          'appName': 'Canvas',
          'packageName': 'com.instructure',
          'version': '1.0.0',
          'buildNumber': '3',
        };
      }
      return null;
    });
  }

  Future<void> apiPrefsInitFuture;
  if (config.mockPrefs != null) {
    SharedPreferences.setMockInitialValues(config.safeMockPrefs);
    apiPrefsInitFuture = ApiPrefs.init();
  }

  if (config.initWebview) _initPlatformWebView();

  // Return all the futures that were created
  return Future.wait([
    if (apiPrefsInitFuture != null) apiPrefsInitFuture,
  ]);
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
    switch (call.method) {
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
