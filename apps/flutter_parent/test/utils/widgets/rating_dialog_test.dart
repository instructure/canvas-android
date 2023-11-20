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
import 'package:flutter_parent/utils/veneers/android_intent_veneer.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../accessibility_utils.dart';
import '../test_app.dart';
import '../test_helpers/mock_helpers.dart';
import '../test_helpers/mock_helpers.mocks.dart';

void main() {
  final analytics = MockAnalytics();
  final intentVeneer = MockAndroidIntentVeneer();
  final launcher = MockUrlLauncher();

  setupTestLocator((locator) {
    locator.registerLazySingleton<Analytics>(() => analytics);
    locator.registerLazySingleton<AndroidIntentVeneer>(() => intentVeneer);
    locator.registerLazySingleton<UrlLauncher>(() => launcher);
  });

  setUp(() {
    reset(analytics);
    reset(intentVeneer);
    reset(launcher);
  });

  Future<void> _showDialog(tester, {DateTime? nextShowDate, bool? dontShowAgain}) async {
    return TestApp.showWidgetFromTap(tester, (context) => RatingDialog.showDialogIfPossible(context, false),
        configBlock: () async {
      await ApiPrefs.setRatingNextShowDate(nextShowDate);
      await ApiPrefs.setRatingDontShowAgain(dontShowAgain);
    });
  }

  /// Tests

  testWidgetsWithAccessibilityChecks('asDialog does not show because this is a test', (tester) async {
    await TestApp.showWidgetFromTap(
      tester,
      (context) => RatingDialog.asDialog(context),
    );

    expect(find.byType(RatingDialog), findsNothing);
    expect(ApiPrefs.getRatingNextShowDate(), isNull); // Should not be changed
  });

  group('showDialog', () {
    testWidgetsWithAccessibilityChecks('does not show when hiding for tests', (tester) async {
      await TestApp.showWidgetFromTap(tester, (context) => RatingDialog.showDialogIfPossible(context, true));

      verifyNever(analytics.logEvent(AnalyticsEventConstants.RATING_DIALOG_SHOW));

      expect(find.byType(RatingDialog), findsNothing);
      expect(ApiPrefs.getRatingNextShowDate(), isNull); // Should not be set yet
    });

    testWidgetsWithAccessibilityChecks('does not show when dont show again is true', (tester) async {
      await _showDialog(tester, dontShowAgain: true);

      verifyNever(analytics.logEvent(AnalyticsEventConstants.RATING_DIALOG_SHOW));

      expect(find.byType(RatingDialog), findsNothing);
      expect(ApiPrefs.getRatingNextShowDate(), isNull); // Should not be set yet
    });

    testWidgetsWithAccessibilityChecks('does not show when next show is not set', (tester) async {
      await _showDialog(tester);

      verifyNever(analytics.logEvent(AnalyticsEventConstants.RATING_DIALOG_SHOW));

      expect(find.byType(RatingDialog), findsNothing);
      expect(ApiPrefs.getRatingNextShowDate(), isNotNull); // Should now be set
    });

    testWidgetsWithAccessibilityChecks('does not show when next show is after now', (tester) async {
      final date = DateTime.now().add(Duration(days: 2));

      await _showDialog(tester, nextShowDate: date);

      verifyNever(analytics.logEvent(AnalyticsEventConstants.RATING_DIALOG_SHOW));

      expect(find.byType(RatingDialog), findsNothing);
      expect(ApiPrefs.getRatingNextShowDate(), date); // Should not be changed
    });

    testWidgetsWithAccessibilityChecks('does show when first launch is past the show again date', (tester) async {
      final date = DateTime.now();
      await _showDialog(tester, nextShowDate: date);

      verify(analytics.logEvent(AnalyticsEventConstants.RATING_DIALOG_SHOW)).called(1);

      // Set four weeks from "now" ("now" is a little later when set in api prefs by rating dialog)
      expect(ApiPrefs.getRatingNextShowDate()?.isAfter(date.add(Duration(days: RatingDialog.FOUR_WEEKS))), isTrue);
      expect(ApiPrefs.getRatingNextShowDate()?.isBefore(date.add(Duration(days: RatingDialog.SIX_WEEKS))), isTrue);
      expect(find.byType(RatingDialog), findsOneWidget);
      expect(find.text(AppLocalizations().ratingDialogTitle), findsOneWidget);
      expect(find.text(AppLocalizations().ratingDialogDontShowAgain.toUpperCase()), findsOneWidget);
      expect(find.byIcon(Icons.star), findsNWidgets(5));
    });
  });

  testWidgetsWithAccessibilityChecks('dont show again button closes and sets pref', (tester) async {
    await _showDialog(tester, nextShowDate: DateTime.now());

    expect(find.byType(RatingDialog), findsOneWidget);
    await tester.tap(find.text(AppLocalizations().ratingDialogDontShowAgain.toUpperCase()));
    await tester.pumpAndSettle();

    expect(ApiPrefs.getRatingDontShowAgain(), true);
    verify(analytics.logEvent(AnalyticsEventConstants.RATING_DIALOG_DONT_SHOW_AGAIN)).called(1);
  });

  group('Send comments', () {
    testWidgetsWithAccessibilityChecks('are visible when less the 4 stars selected', (tester) async {
      await _showDialog(tester, nextShowDate: DateTime.now());

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
      final date = DateTime.now();
      await _showDialog(tester, nextShowDate: date);

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
      // Four weeks when without a comment ("now" is a little later when set in api prefs by rating dialog)
      expect(ApiPrefs.getRatingNextShowDate()?.isAfter(date.add(Duration(days: RatingDialog.FOUR_WEEKS))), isTrue);
      expect(ApiPrefs.getRatingNextShowDate()?.isBefore(date.add(Duration(days: RatingDialog.SIX_WEEKS))), isTrue);
      verify(analytics.logEvent(
        AnalyticsEventConstants.RATING_DIALOG,
        extras: {AnalyticsParamConstants.STAR_RATING: 1}, // First was clicked, should send a 1
      )).called(1);
    });

    testWidgetsWithAccessibilityChecks('shows correctly in ltr', (tester) async {
      await TestApp.showWidgetFromTap(
        tester,
        (context) => RatingDialog.showDialogIfPossible(context, false),
        locale: Locale('en'),
        configBlock: () async {
          await ApiPrefs.setRatingNextShowDate(DateTime.now());
        },
      );

      // Should verify that the 1 star rating is displayed to the left of the 5 star rating in LTR
      final icons = find.byIcon(Icons.star);
      expect(find.byType(RatingDialog), findsOneWidget);
      expect(await tester.getBottomLeft(icons.first).dx, lessThan(await tester.getBottomLeft(icons.last).dx));

      // Tap the rating
      await tester.tap(icons.first);
      await tester.pumpAndSettle();

      // Send feedback
      await tester.tap(find.text(AppLocalizations().ratingDialogSendFeedback.toUpperCase()));
      await tester.pumpAndSettle();

      // Verify 1 was clicked
      verify(analytics.logEvent(
        AnalyticsEventConstants.RATING_DIALOG,
        extras: {AnalyticsParamConstants.STAR_RATING: 1}, // First was clicked, should send a 1
      )).called(1);
    });

    testWidgetsWithAccessibilityChecks('shows correctly in rtl', (tester) async {
      await TestApp.showWidgetFromTap(
        tester,
        (context) => RatingDialog.showDialogIfPossible(context, false),
        locale: Locale('ar', 'AR'),
        configBlock: () async {
          await ApiPrefs.setRatingNextShowDate(DateTime.now());
        },
      );

      // Should verify that the 1 star rating is displayed to the right of the 5 star rating in RTL
      final icons = find.byIcon(Icons.star);
      expect(find.byType(RatingDialog), findsOneWidget);
      expect(await tester.getBottomLeft(icons.first).dx, greaterThan(await tester.getBottomLeft(icons.last).dx));

      // Tap the rating
      await tester.tap(icons.first);
      await tester.pumpAndSettle();

      // Send feedback
      await tester.tap(find.text(AppLocalizations().ratingDialogSendFeedback.toUpperCase()));
      await tester.pumpAndSettle();

      // Verify 1 was clicked
      verify(analytics.logEvent(
        AnalyticsEventConstants.RATING_DIALOG,
        extras: {AnalyticsParamConstants.STAR_RATING: 1}, // First was clicked, should send a 1
      )).called(1);
    });

    testWidgetsWithAccessibilityChecks('dismiss the dialog and open email when comments are not empty', (tester) async {
      final comment = 'comment here';
      final date = DateTime.now();
      await _showDialog(tester, nextShowDate: date);

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
      expect(ApiPrefs.getRatingDontShowAgain(), isNull); // Shouldn't set this if we are sending feedback
      // Six weeks when given a comment
      expect(ApiPrefs.getRatingNextShowDate()?.isAfter(date.add(Duration(days: RatingDialog.SIX_WEEKS))), isTrue);
      verify(intentVeneer.launchEmailWithBody(AppLocalizations().ratingDialogEmailSubject('1.0.0'), emailBody));
      verify(analytics.logEvent(
        AnalyticsEventConstants.RATING_DIALOG,
        extras: {AnalyticsParamConstants.STAR_RATING: 2}, // Second was clicked, should send a 2
      )).called(1);
    });
  });

  group('App store', () {
    testWidgetsWithAccessibilityChecks('4 stars goes to the app store', (tester) async {
      await _showDialog(tester, nextShowDate: DateTime.now());

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
      await _showDialog(tester, nextShowDate: DateTime.now());

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
