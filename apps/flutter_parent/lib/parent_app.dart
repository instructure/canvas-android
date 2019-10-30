import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';

class ParentApp extends StatefulWidget {
  @override
  _ParentAppState createState() => _ParentAppState();
}

class _ParentAppState extends State<ParentApp> {
  Locale _locale;

  rebuild(locale) {
    setState(() => _locale = locale);
  }

  @override
  void initState() {
    super.initState();
    // TODO: Set locale from stored user
//    _locale = AuthService.effectiveLocale();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Canvas Parent',
      locale: _locale,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        // Material components use these delegate to provide default localization
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ],
      supportedLocales: AppLocalizations.delegate.supportedLocales,
      localeResolutionCallback: _localeCallback(),
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
//      home: AuthService.isLoggedIn() ? DashboardPage() : LoginLandingPage(),
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
