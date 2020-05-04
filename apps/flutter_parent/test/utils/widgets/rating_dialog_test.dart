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
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/common_widgets/rating_dialog.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_parent/utils/veneers/AndroidIntentVeneer.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../accessibility_utils.dart';
import '../platform_config.dart';
import '../test_app.dart';
import '../test_helpers/mock_helpers.dart';

void main() {
  final analytics = MockAnalytics();
  final intentVeneer = MockAndroidIntentVeneer();
  final launcher = MockUrlLauncher();

  setupTestLocator((locator) {
    locator.registerLazySingleton<Analytics>(() => analytics);
    locator.registerLazySingleton<AndroidIntentVeneer>(() => intentVeneer);
    locator.registerLazySingleton<UrlLauncher>(() => launcher);
  });

  _validShowDate() => DateTime.now().subtract(Duration(days: 7 * 4)).millisecondsSinceEpoch;

  Future<void> _showDialog(tester, {Map<String, dynamic> mockApiPrefs = const {}}) async {
    return TestApp.showWidgetFromTap(
      tester,
      (context) => RatingDialog.showDialogIfPossible(context, false),
      config: PlatformConfig(mockApiPrefs: mockApiPrefs),
    );
  }

  /// Tests

  testWidgetsWithAccessibilityChecks('asDialog does not show because this is a test', (tester) async {
    final date = DateTime.now().millisecondsSinceEpoch;
    await TestApp.showWidgetFromTap(
      tester,
      (context) => RatingDialog.asDialog(context),
      config: PlatformConfig(mockApiPrefs: {
        ApiPrefs.KEY_RATING_FIRST_LAUNCH_DATE: date,
        ApiPrefs.KEY_RATING_SHOW_AGAIN_WAIT: 0,
      }),
    );

    expect(find.byType(RatingDialog), findsNothing);
    expect(ApiPrefs.getRatingFirstLaunchDate(), date); // Should not be changed
    expect(ApiPrefs.getRatingShowAgainWait(), 0); // Should not be changed
  });

  group('showDialog', () {
    testWidgetsWithAccessibilityChecks('does not show when hiding for tests', (tester) async {
      await TestApp.showWidgetFromTap(tester, (context) => RatingDialog.showDialogIfPossible(context, true));

      expect(find.byType(RatingDialog), findsNothing);
      expect(ApiPrefs.getRatingFirstLaunchDate(), isNull); // Should not be set yet
    });

    testWidgetsWithAccessibilityChecks('does not show when dont show again is true', (tester) async {
      await _showDialog(tester, mockApiPrefs: {ApiPrefs.KEY_RATING_DONT_SHOW_AGAIN: true});

      expect(find.byType(RatingDialog), findsNothing);
      expect(ApiPrefs.getRatingFirstLaunchDate(), isNull); // Should not be set yet
    });

    testWidgetsWithAccessibilityChecks('does not show when first launch is not set', (tester) async {
      await _showDialog(tester, mockApiPrefs: {ApiPrefs.KEY_RATING_FIRST_LAUNCH_DATE: null});

      expect(find.byType(RatingDialog), findsNothing);
      expect(ApiPrefs.getRatingFirstLaunchDate(), isNotNull); // Should now be set
    });

    testWidgetsWithAccessibilityChecks('does not show when first launch is not past the show again date',
        (tester) async {
      final date = DateTime.now().millisecondsSinceEpoch;

      await _showDialog(tester, mockApiPrefs: {
        ApiPrefs.KEY_RATING_FIRST_LAUNCH_DATE: date,
        ApiPrefs.KEY_RATING_SHOW_AGAIN_WAIT: date + 10,
      });

      expect(find.byType(RatingDialog), findsNothing);
      expect(ApiPrefs.getRatingFirstLaunchDate(), date); // Should not be changed
    });

    testWidgetsWithAccessibilityChecks('does show when first launch is past the show again date', (tester) async {
      final date = _validShowDate();
      await _showDialog(tester, mockApiPrefs: {
        ApiPrefs.KEY_RATING_FIRST_LAUNCH_DATE: date,
      });

      verify(analytics.logEvent(AnalyticsEventConstants.RATING_DIALOG_SHOW)).called(1);

      expect(ApiPrefs.getRatingFirstLaunchDate(), isNot(date)); // Should not be changed
      expect(ApiPrefs.getRatingShowAgainWait(), RatingDialog.FOUR_WEEKS); // Should be set to four weeks now
      expect(find.byType(RatingDialog), findsOneWidget);
      expect(find.text(AppLocalizations().ratingDialogTitle), findsOneWidget);
      expect(find.text(AppLocalizations().ratingDialogDontShowAgain.toUpperCase()), findsOneWidget);
      expect(find.byIcon(Icons.star), findsNWidgets(5));
    });
  });

  testWidgetsWithAccessibilityChecks('dont show again button closes and sets pref', (tester) async {
    await _showDialog(tester, mockApiPrefs: {
      ApiPrefs.KEY_RATING_FIRST_LAUNCH_DATE: _validShowDate(),
    });

    expect(find.byType(RatingDialog), findsOneWidget);
    await tester.tap(find.text(AppLocalizations().ratingDialogDontShowAgain.toUpperCase()));
    await tester.pumpAndSettle();

    expect(ApiPrefs.getRatingDontShowAgain(), true);
    verify(analytics.logEvent(AnalyticsEventConstants.RATING_DIALOG_DONT_SHOW_AGAIN)).called(1);
  });

  group('Send comments', () {
    testWidgetsWithAccessibilityChecks('are visible when less the 4 stars selected', (tester) async {
      await _showDialog(tester, mockApiPrefs: {
        ApiPrefs.KEY_RATING_FIRST_LAUNCH_DATE: _validShowDate(),
      });

      expect(find.byType(RatingDialog), findsOneWidget);
      expect(find.byIcon(Icons.star), findsNWidgets(5));

      // Tap each icon, expect that comments are still visible
      final icons = find.byIcon(Icons.star);
      for (int i = 0; i < 3; i++) {
        await tester.tap(icons.at(i));
        await tester.pumpAndSettle();

        expect(find.byType(RatingDialog), findsOneWidget);
        expect(find.text(AppLocalizations().ratingDialogCommentDescription), findsOneWidget);
        expect(find.text(AppLocalizations().ratingDialogSendFeedback.toUpperCase()), findsOneWidget);
      }
    });

    testWidgetsWithAccessibilityChecks('dismiss the dialog when comments are empty', (tester) async {
      await _showDialog(tester, mockApiPrefs: {
        ApiPrefs.KEY_RATING_FIRST_LAUNCH_DATE: _validShowDate(),
      });

      expect(find.byType(RatingDialog), findsOneWidget);

      // Tap the rating
      final icons = find.byIcon(Icons.star);
      await tester.tap(icons.first);
      await tester.pumpAndSettle();

      // Send feedback
      await tester.tap(find.text(AppLocalizations().ratingDialogSendFeedback.toUpperCase()));
      await tester.pumpAndSettle();

      // Asserts
      expect(find.byType(RatingDialog), findsNothing);
      expect(ApiPrefs.getRatingDontShowAgain(), isNot(true)); // Shouldn't set this if we are sending feedback
      expect(ApiPrefs.getRatingShowAgainWait(), RatingDialog.FOUR_WEEKS); // Four weeks when without a comment
      verify(analytics.logEvent(
        AnalyticsEventConstants.RATING_DIALOG,
        extras: {AnalyticsParamConstants.STAR_RATING: 1}, // First was clicked, should send a 1
      )).called(1);
    });

    testWidgetsWithAccessibilityChecks('dismiss the dialog and open email when comments are not empty', (tester) async {
      final comment = 'comment here';
      await _showDialog(tester, mockApiPrefs: {
        ApiPrefs.KEY_RATING_FIRST_LAUNCH_DATE: _validShowDate(),
      });

      expect(find.byType(RatingDialog), findsOneWidget);

      // Tap the rating
      final icons = find.byIcon(Icons.star);
      await tester.tap(icons.at(1));
      await tester.pumpAndSettle();

      // Type a comment
      await tester.enterText(find.byType(TextField), comment);

      // Send feedback
      await tester.tap(find.text(AppLocalizations().ratingDialogSendFeedback.toUpperCase()));
      await tester.pumpAndSettle();

      final emailBody = '' +
          '$comment\r\n' +
          '\r\n' +
          '${AppLocalizations().helpUserId} 0\r\n' +
          '${AppLocalizations().helpEmail} \r\n' +
          '${AppLocalizations().helpDomain} \r\n' +
          '${AppLocalizations().versionNumber}: Canvas v1.0.0 (3)\r\n' +
          '${AppLocalizations().device}: Instructure Canvas Phone\r\n' +
          '${AppLocalizations().osVersion}: Android FakeOS 9000\r\n' +
          '----------------------------------------------\r\n';

      // Asserts
      expect(find.byType(RatingDialog), findsNothing);
      expect(ApiPrefs.getRatingDontShowAgain(), isNot(true)); // Shouldn't set this if we are sending feedback
      expect(ApiPrefs.getRatingShowAgainWait(), RatingDialog.SIX_WEEKS); // Four weeks when without a comment
      verify(intentVeneer.launchEmailWithBody(AppLocalizations().ratingDialogEmailSubject('1.0.0'), emailBody));
      verify(analytics.logEvent(
        AnalyticsEventConstants.RATING_DIALOG,
        extras: {AnalyticsParamConstants.STAR_RATING: 2}, // Second was clicked, should send a 2
      )).called(1);
    });
  });

  group('App store', () {
    testWidgetsWithAccessibilityChecks('4 stars goes to the app store', (tester) async {
      await _showDialog(tester, mockApiPrefs: {
        ApiPrefs.KEY_RATING_FIRST_LAUNCH_DATE: _validShowDate(),
      });

      expect(find.byType(RatingDialog), findsOneWidget);
      expect(find.byIcon(Icons.star), findsNWidgets(5));

      // Tap 4 star icon
      await tester.tap(find.byIcon(Icons.star).at(3));
      await tester.pumpAndSettle();

      expect(find.byType(RatingDialog), findsNothing);
      expect(ApiPrefs.getRatingDontShowAgain(), isTrue);
      verify(launcher.launchAppStore());
      verify(analytics.logEvent(
        AnalyticsEventConstants.RATING_DIALOG,
        extras: {AnalyticsParamConstants.STAR_RATING: 4},
      )).called(1);
    });

    testWidgetsWithAccessibilityChecks('5 stars goes to the app store', (tester) async {
      await _showDialog(tester, mockApiPrefs: {
        ApiPrefs.KEY_RATING_FIRST_LAUNCH_DATE: _validShowDate(),
      });

      expect(find.byType(RatingDialog), findsOneWidget);
      expect(find.byIcon(Icons.star), findsNWidgets(5));

      // Tap 5 star icon
      await tester.tap(find.byIcon(Icons.star).last);
      await tester.pumpAndSettle();

      expect(find.byType(RatingDialog), findsNothing);
      expect(ApiPrefs.getRatingDontShowAgain(), isTrue);
      verify(launcher.launchAppStore());
      verify(analytics.logEvent(
        AnalyticsEventConstants.RATING_DIALOG,
        extras: {AnalyticsParamConstants.STAR_RATING: 5},
      )).called(1);
    });
  });
}
