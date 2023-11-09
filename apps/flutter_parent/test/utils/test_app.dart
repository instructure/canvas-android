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

import 'package:encrypted_shared_preferences/encrypted_shared_preferences.dart';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/common_widgets/masquerade_ui.dart';
import 'package:flutter_parent/utils/common_widgets/respawn.dart';
import 'package:flutter_parent/utils/db/user_colors_db.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/theme_prefs.dart';
import 'package:flutter_parent/utils/remote_config_utils.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:webview_flutter/webview_flutter.dart';
import 'package:webview_flutter_platform_interface/webview_flutter_platform_interface.dart';

import 'platform_config.dart';
import 'test_helpers/mock_helpers.mocks.dart';

class TestApp extends StatefulWidget {
  TestApp(
    this.home, {
    this.platformConfig = const PlatformConfig(),
    this.navigatorObservers = const [],
    this.darkMode = false,
    this.highContrast = false,
    this.locale = null,
  });

  static final GlobalKey<NavigatorState> navigatorKey = new GlobalKey<NavigatorState>();

  final Widget home;
  final PlatformConfig platformConfig;
  final List<NavigatorObserver> navigatorObservers;
  final bool darkMode;
  final bool highContrast;
  final Locale? locale;

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
  ///     config: PlatformConfig(initWebView: true, initLoggedInUser: login, initRemoteConfig: mockRemoteConfig),
  ///     configBlock: () async {
  ///       await ApiPrefs.setCurrentStudent(student);
  ///     }
  /// });
  ///
  static showWidgetFromTap(
    WidgetTester tester,
    Future tapCallback(BuildContext context), {
    Locale? locale,
    PlatformConfig config = const PlatformConfig(),
    Future configBlock()?,
  }) async {
    await tester.pumpWidget(TestApp(
      Builder(builder: (context) {
        return ElevatedButton(
          style: ElevatedButton.styleFrom(backgroundColor: Colors.black),
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
    await tester.tap(find.byType(ElevatedButton));
    await tester.pumpAndSettle();
  }
}

class _TestAppState extends State<TestApp> {
  Locale? _locale;

  rebuild(locale) {
    setState(() => _locale = locale);
  }

  @override
  void initState() {
    super.initState();
    _locale = widget.locale;
    PandaRouter.init();
    setupPlatformChannels(config: widget.platformConfig);
  }

  @override
  Widget build(BuildContext context) {
    return Respawn(
      child: ParentTheme(
        themePrefs: MockThemePrefs(widget.darkMode, widget.highContrast),
        builder: (context, themeData) => MaterialApp(
          title: 'Canvas Parent',
          locale: _locale,
          builder: (context, child) =>
              MasqueradeUI(navKey: TestApp.navigatorKey,
                  child: child ?? Container()),
          navigatorKey: TestApp.navigatorKey,
          navigatorObservers: widget.navigatorObservers,
          localizationsDelegates: const [
            AppLocalizations.delegate,
            // Material components use these delegate to provide default localization
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: AppLocalizations.delegate.supportedLocales,
          localeResolutionCallback: _localeCallback(),
          theme: themeData,
          home: Material(child: widget.home),
          onGenerateRoute: PandaRouter.router.generator,
        ),
      ),
    );
  }

  // Get notified when there's a new system locale so we can rebuild the app with the new language
  LocaleResolutionCallback _localeCallback() => (locale, supportedLocales) {
        const fallback = Locale('en', '');
        Locale? resolvedLocale =
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

Future<void> setupTestLocator(config(GetIt locator)) async {
  final locator = GetIt.instance;
  await locator.reset();
  locator.allowReassignment = true; // Allows reassignment by the config block

  // Register things that needed by default
  locator.registerLazySingleton<Analytics>(() => Analytics());
  locator.registerLazySingleton<UserColorsDb>(() => MockUserColorsDb());

  config(locator);
}

class MockThemePrefs extends ThemePrefs {
  MockThemePrefs(this._darkMode, this._hcMode);

  bool _darkMode;
  bool _webViewDarkMode = false;
  bool _hcMode;

  @override
  bool get darkMode => _darkMode;

  @override
  set darkMode(bool value) => _darkMode = value;

  @override
  bool get webViewDarkMode => _webViewDarkMode;

  @override
  set webViewDarkMode(bool value) => _webViewDarkMode = value;

  @override
  bool get hcMode => _hcMode;

  @override
  set hcMode(bool value) => _hcMode = value;
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

  if (config.initPackageInfo) _initPackageInfo();

  if (config.initDeviceInfo) _initPlatformDeviceInfo();

  if (config.initPathProvider) _initPathProvider();

  Future<void>? apiPrefsInitFuture;
  if (config.mockApiPrefs != null) {
    ApiPrefs.clean();
    EncryptedSharedPreferences.setMockInitialValues(
        config.mockApiPrefs!..putIfAbsent(ApiPrefs.KEY_HAS_MIGRATED_TO_ENCRYPTED_PREFS, () => true));
    apiPrefsInitFuture = ApiPrefs.init();
  }

  Future<void>? remoteConfigInitFuture;
  if (config.initRemoteConfig != null || config.mockPrefs != null) {
    SharedPreferences.setMockInitialValues(config.mockPrefs ?? {});
    if (config.initRemoteConfig != null) {
      RemoteConfigUtils.clean();
      remoteConfigInitFuture = RemoteConfigUtils.initializeExplicit(config.initRemoteConfig!);
    }
  }

  if (config.initWebview) _initPlatformWebView();

  await Future.wait([
    if (apiPrefsInitFuture != null) apiPrefsInitFuture,
    if (remoteConfigInitFuture != null) remoteConfigInitFuture,
  ]);

  if (config.initLoggedInUser != null) {
    ApiPrefs.addLogin(config.initLoggedInUser!);
    ApiPrefs.switchLogins(config.initLoggedInUser!);
  }
}

/// WebView helpers. These are needed as web views tie into platform views.
///
/// Inspired solution is a slimmed down version of the WebView test:
/// https://github.com/flutter/plugins/blob/webview_flutter-v3.0.4/packages/webview_flutter/webview_flutter/test/webview_flutter_test.dart
void _initPlatformWebView() {
  final mockWebViewPlatformController = MockWebViewPlatformController();
  final mockWebViewPlatform = MockWebViewPlatform();
  when(mockWebViewPlatform.build(
    context: anyNamed('context'),
    creationParams: anyNamed('creationParams'),
    webViewPlatformCallbacksHandler:
    anyNamed('webViewPlatformCallbacksHandler'),
    javascriptChannelRegistry: anyNamed('javascriptChannelRegistry'),
    onWebViewPlatformCreated: anyNamed('onWebViewPlatformCreated'),
    gestureRecognizers: anyNamed('gestureRecognizers'),
  )).thenAnswer((Invocation invocation) {
    final WebViewPlatformCreatedCallback onWebViewPlatformCreated =
    invocation.namedArguments[const Symbol('onWebViewPlatformCreated')]
    as WebViewPlatformCreatedCallback;
    return TestPlatformWebView(
      mockWebViewPlatformController: mockWebViewPlatformController,
      onWebViewPlatformCreated: onWebViewPlatformCreated,
    );
  });

  WebView.platform = mockWebViewPlatform;
  WebViewCookieManagerPlatform.instance = FakeWebViewCookieManager();
}

/// Mocks the platform channel used by the package_info plugin
void _initPackageInfo() {
  const MethodChannel('dev.fluttercommunity.plus/package_info').setMockMethodCallHandler((MethodCall methodCall) async {
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
  const MethodChannel('dev.fluttercommunity.plus/device_info').setMockMethodCallHandler((MethodCall methodCall) async {
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
        'displayMetrics': <String, dynamic> {
          'widthPx': 100.0,
          'heightPx': 100.0,
          'xDpi': 100.0,
          'yDpi': 100.0,
        },
        'serialNumber': 'fake-serialNumber',
      };
    }
    if (methodCall.method == 'getDeviceInfo') {
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
        'displayMetrics': <String, dynamic> {
          'widthPx': 100.0,
          'heightPx': 100.0,
          'xDpi': 100.0,
          'yDpi': 100.0,
        },
        'serialNumber': 'fake-serialNumber',
      };
    }
    return null;
  });
}

/// Mocks the platform channel used by the path_provider plugin
void _initPathProvider() {
  const MethodChannel('plugins.flutter.io/path_provider').setMockMethodCallHandler((MethodCall methodCall) async {
    if (methodCall.method == 'getApplicationCacheDirectory') {
      return "fake-path";
    }
    return null;
  });
}

class TestPlatformWebView extends StatefulWidget {
  const TestPlatformWebView({
    Key? key,
    required this.mockWebViewPlatformController,
    this.onWebViewPlatformCreated,
  }) : super(key: key);

  final MockWebViewPlatformController mockWebViewPlatformController;
  final WebViewPlatformCreatedCallback? onWebViewPlatformCreated;

  @override
  State<StatefulWidget> createState() => TestPlatformWebViewState();
}

class TestPlatformWebViewState extends State<TestPlatformWebView> {
  @override
  void initState() {
    super.initState();
    final WebViewPlatformCreatedCallback? onWebViewPlatformCreated =
        widget.onWebViewPlatformCreated;
    if (onWebViewPlatformCreated != null) {
      onWebViewPlatformCreated(widget.mockWebViewPlatformController);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container();
  }
}

class FakeWebViewCookieManager extends WebViewCookieManagerPlatform {
  @override
  Future<bool> clearCookies() {
    return Future.value(false);
  }

  @override
  Future<void> setCookie(WebViewCookie cookie) {
    return Future.value(null);
  }
}