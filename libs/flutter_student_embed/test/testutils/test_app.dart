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
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/network/utils/api_prefs.dart';
import 'package:flutter_student_embed/utils/design/student_theme.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';

import 'platform_config.dart';

class TestApp extends StatefulWidget {
  TestApp(
    this.home, {
    this.platformConfig = const PlatformConfig(),
    this.navigatorObservers = const [],
    this.locale,
  });

  static final GlobalKey<NavigatorState> navigatorKey = new GlobalKey<NavigatorState>();

  final Widget home;
  final PlatformConfig platformConfig;
  final List<NavigatorObserver> navigatorObservers;
  final Locale locale;

  @override
  _TestAppState createState() => _TestAppState();

  /// Allows a widget to be shown that requires a BuildContext by using the [tapCallback]. Awaiting the future returned
  /// by this function will drop you off right after the tap callback has finished with a pumpAndSettle().
  ///
  /// * [tester] The widget tester from the testWidgets function
  /// * [tapCallback] a future that can use a BuildContext show a dialog, launch a route, etc...
  /// * [locale] Passed into TestApp to set the locale of the app under test
  /// * [config] A [PlatformConfig] object passed to TestApp that is used during [setupPlatformChannels]
  /// * [configBlock] If you want to do custom configuration of the app or prefs, this is an easy place to setup custom data
  ///
  /// Example call:
  /// testWidgetsWithAccessibility((tester) {
  ///   await showWidgetFromTap(
  ///     tester,
  ///     (context) => ErrorReportDialog.asDialog(context),
  ///     locale: Locale('ar'),
  ///     config: PlatformConfig(initWebView: true, initLoggedInUser: login),
  ///     configBlock: () async {
  ///       await ApiPrefs.setCurrentStudent(student);
  ///     }
  /// });
  ///
  static showWidgetFromTap(
    WidgetTester tester,
    Future tapCallback(BuildContext context), {
    Locale locale,
    PlatformConfig config = const PlatformConfig(),
    Future configBlock(),
  }) async {
    await tester.pumpWidget(TestApp(
      Builder(builder: (context) {
        return RaisedButton(
          color: Colors.black,
          child: Text('tap me', style: TextStyle(color: Colors.white)),
          onPressed: () => tapCallback(context),
        );
      }),
      locale: locale,
      platformConfig: config,
    ));
    await tester.pumpAndSettle();

    // Await any other setup before tapping the button for the tap callback
    if (configBlock != null) await configBlock();

    // Tap the button to trigger the onPressed
    await tester.tap(find.byType(RaisedButton));
    await tester.pumpAndSettle();
  }
}

class _TestAppState extends State<TestApp> {
  Locale _locale;

  rebuild(locale) {
    setState(() => _locale = locale);
  }

  @override
  void initState() {
    super.initState();
    _locale = widget.locale;
    setupPlatformChannels(config: widget.platformConfig);
  }

  @override
  Widget build(BuildContext context) {
    return StudentTheme(
      builder: (context, themeData) => MaterialApp(
        title: 'Canvas Parent',
        locale: _locale,
        builder: (context, child) => child,
        navigatorKey: TestApp.navigatorKey,
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
        const fallback = Locale('en', '');
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
  locator.allowReassignment = true; // Allows reassignment by the config block

  config(locator);
}

/// Set up the platform channels used by the app.
///
/// Returned is a future that completes when all async tasks spawned by this method call have completed. Waiting isn't
/// required, though is probably good practice and results in more stable tests.
///
/// TODO: Make this method not async, so that all initialization can be done in TestApp#initState (and synchronously elsewhere)
/// As is, this can cause an issue if anything relies on a platform channel while building TestApp. To get around that,
/// we'd need to wrap TestApp in a future builder, which leads to an unstable amount of pumps before testing can really
/// occur. By moving ApiPrefs and RemoteConfigUtils to locator, we should be able to provide mocks in a synchronous
/// fashion and avoid any asynchronous setup in this setupPlatformChannels method.
Future<void> setupPlatformChannels({PlatformConfig config = const PlatformConfig()}) async {
  TestWidgetsFlutterBinding.ensureInitialized();

  Future<void> apiPrefsInitFuture;

  if (config.initPackageInfo) {
    _initPackageInfo();
    ApiPrefs.clean();
    apiPrefsInitFuture = ApiPrefs.init();
  }

  if (config.initDeviceInfo) _initPlatformDeviceInfo();

  await Future.wait([
    if (apiPrefsInitFuture != null) apiPrefsInitFuture,
  ]);

  if (config.initLoggedInUser != null) {
    ApiPrefs.setLogin(config.initLoggedInUser);
  }
}

/// Mocks the platform channel used by the package_info plugin
void _initPackageInfo() {
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

/// Mocks the platform channel used by the device_info plugin
void _initPlatformDeviceInfo() {
  const MethodChannel('plugins.flutter.io/device_info').setMockMethodCallHandler((MethodCall methodCall) async {
    if (methodCall.method == 'getAndroidDeviceInfo') {
      return <String, dynamic>{
        'version': <String, dynamic>{
          'baseOS': 'fake-baseOD',
          'codename': 'fake-codename',
          'incremental': 'fake-incremental',
          'previewSdkInt': 9001,
          'release': 'FakeOS 9000',
          'sdkInt': 9000,
          'securityPatch': 'fake-securityPatch',
        },
        'board': 'fake-board',
        'bootloader': 'fake-bootloader',
        'brand': 'Canvas',
        'device': 'fake-device',
        'display': 'fake-display',
        'fingerprint': 'fake-fingerprint',
        'hardware': 'fake-hardware',
        'host': 'fake-host',
        'id': 'fake-id',
        'manufacturer': 'Instructure',
        'model': 'Canvas Phone',
        'product': 'fake-product',
        'supported32BitAbis': [],
        'supported64BitAbis': [],
        'supportedAbis': [],
        'tags': 'fake-tags',
        'type': 'take-types',
        'isPhysicalDevice': false,
        'androidId': 'fake-androidId',
        'systemFeatures': [],
      };
    }
    return null;
  });
}
