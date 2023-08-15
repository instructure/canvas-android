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

import 'dart:async';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/analytics_observer.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/common_widgets/masquerade_ui.dart';
import 'package:flutter_parent/utils/common_widgets/respawn.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';

import 'l10n/fallback_material_localizations_delegate.dart';

class ParentApp extends StatefulWidget {
  final Completer<void> _appCompleter;

  @override
  _ParentAppState createState() => _ParentAppState();

  ParentApp(this._appCompleter, {super.key});

  static _ParentAppState? of(BuildContext context) {
    return context.findAncestorStateOfType<_ParentAppState>();
  }
}

class _ParentAppState extends State<ParentApp> {
  late Locale? _locale;
  GlobalKey<NavigatorState> _navKey = GlobalKey();

  rebuild(locale) {
    setState(() => _locale = locale);
  }

  @override
  void initState() {
    super.initState();

    _locale = ApiPrefs.effectiveLocale();
  }

  @override
  Widget build(BuildContext context) {
    return Respawn(
      child: ParentTheme(
        builder: (context, themeData) => MaterialApp(
          builder: (context, child) {
            if (!widget._appCompleter.isCompleted) {
              widget._appCompleter.complete();
            }
            return MasqueradeUI(navKey: _navKey, child: child!);
          },
          title: 'Canvas Parent',
          locale: _locale,
          navigatorKey: _navKey,
          localizationsDelegates: const [
            AppLocalizations.delegate,
            // Material components use these delegate to provide default localization
            FallbackMaterialLocalizationsDelegate(),
            GlobalWidgetsLocalizations.delegate,
          ],
          supportedLocales: AppLocalizations.delegate.supportedLocales,
          localeResolutionCallback: _localeCallback(),
          theme: themeData,
          navigatorObservers: [AnalyticsObserver()],
          onGenerateRoute: PandaRouter.router.generator,
        ),
      ),
    );
  }

  // Get notified when there's a new system locale so we can rebuild the app with the new language
  LocaleResolutionCallback _localeCallback() => (locale, supportedLocales) {
        // If there is no user locale, they want the system locale. If there is a user locale, we should use it over the system locale
        Locale newLocale = ApiPrefs.getUser()?.locale == null ? locale! : _locale!;

        const fallback = Locale('en');
        Locale? resolvedLocale =
            AppLocalizations.delegate.resolution(fallback: fallback, matchCountry: false)(newLocale, supportedLocales);

        // Update the state if the locale changed
        if (_locale != resolvedLocale) {
          SchedulerBinding.instance.addPostFrameCallback((_) {
            rebuild(resolvedLocale);
          });
        }

        return resolvedLocale;
      };
}
