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
import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/common_widgets/arrow_aware_focus_scope.dart';
import 'package:flutter_parent/utils/common_widgets/full_screen_scroll_container.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/veneers/android_intent_veneer.dart';
import 'package:package_info_plus/package_info_plus.dart';

import '../url_launcher.dart';

class RatingDialog extends StatefulWidget {
  static const FOUR_WEEKS = 28;
  static const SIX_WEEKS = 42;
  static const _RATING_TO_APP_STORE_THRESHOLD = 3;

  /// Will show the rating dialog when:
  /// - user has used the app for 4 weeks
  /// - when the user sees the dialog again there will be a "don't show again" button
  /// - when the user sees the dialog, there are a few use cases for when to show the dialog again
  ///
  /// 1. User presses 4-5 stars -> take user to play store and don't show dialog again
  /// 2. User presses < 4 stars with no comment -> show again 4 weeks later
  /// 3. User presses < 4 stars with a comment -> show again 6 weeks later
  /// 4. User presses back -> show again 4 weeks later
  static Future<void> asDialog(BuildContext context) {
    // Don't show dialog in tests, so that they run more stable. Testing this rating dialog can be achieved by
    // using the method `RatingDialog.showDialogIfPossible()`
    final hideForAutomatedTests = WidgetsBinding.instance.runtimeType != WidgetsFlutterBinding;
    return showDialogIfPossible(context, hideForAutomatedTests);
  }

  /// A helper function for showing the dialog, so that this dialog can still be widget tested
  @visibleForTesting
  static Future<void> showDialogIfPossible(BuildContext context, bool hideForAutomatedTests) {
    if (ApiPrefs.getRatingDontShowAgain() == true || hideForAutomatedTests) return Future.value();

    final nextShowDate = ApiPrefs.getRatingNextShowDate();
    final date = DateTime.now();

    if (nextShowDate == null) {
      ApiPrefs.setRatingNextShowDate(date.add(Duration(days: FOUR_WEEKS)));
      return Future.value();
    }

    if (date.isBefore(nextShowDate)) return Future.value();

    ApiPrefs.setRatingNextShowDate(date.add(Duration(days: FOUR_WEEKS)));

    locator<Analytics>().logEvent(AnalyticsEventConstants.RATING_DIALOG_SHOW);

    return showDialog(
      context: context,
      builder: (context) => RatingDialog._internal(),
    );
  }

  const RatingDialog._internal({super.key});

  @override
  _RatingDialogState createState() => _RatingDialogState();
}

class _RatingDialogState extends State<RatingDialog> {
  late String _comment;
  late int _focusedStar;
  late int _selectedStar;
  late bool _sending;

  @override
  void initState() {
    super.initState();
    _comment = '';
    _focusedStar = -1;
    _selectedStar = -1;
    _sending = false;
  }

  /// Widget building

  @override
  Widget build(BuildContext context) {
    return FullScreenScrollContainer(
      horizontalPadding: 0,
      children: [
        ArrowAwareFocusScope(
          child: AlertDialog(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8.0)),
            title: Text(L10n(context).ratingDialogTitle),
            actions: <Widget>[
              TextButton(
                child: Text(L10n(context).ratingDialogDontShowAgain.toUpperCase()),
                onPressed: _handleDontShowAgain,
              )
            ],
            content: Column(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                _stars(),
                if (_selectedStar >= 0 && _selectedStar < RatingDialog._RATING_TO_APP_STORE_THRESHOLD) _commentWidget(),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _stars() {
    return Semantics(
      container: true,
      label: L10n(context).starRating(_selectedStar + 1),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: List.generate(5, (index) {
          return _materialButton(index);
        }),
      ),
    );
  }

  Widget _materialButton(int index) {
    final starColor = _focusedStar >= index ? ParentColors.parentApp : ParentColors.ash;
    return Semantics(
      child: MaterialButton(
        child: Icon(Icons.star, color: starColor, size: 48, semanticLabel: L10n(context).starRating(index + 1)),
        height: 48,
        minWidth: 48,
        shape: CircleBorder(),
        onPressed: () => _handleStarPressed(index),
        onHighlightChanged: (highlighted) => setState(() => _focusedStar = highlighted ? index : _selectedStar),
        padding: EdgeInsets.zero,
      ),
    );
  }

  Widget _commentWidget() {
    return Column(
      children: <Widget>[
        MergeSemantics(
          // If not wrapped in merge semantics, it will not be focusable with talkback
          child: TextField(
            maxLines: null,
            textCapitalization: TextCapitalization.sentences,
            decoration: InputDecoration(hintText: L10n(context).ratingDialogCommentDescription),
            onChanged: (text) => setState(() => _comment = text),
          ),
        ),
        SizedBox(height: 8),
        ElevatedButton(
          child: Text(L10n(context).ratingDialogSendFeedback.toUpperCase()),
          style: ElevatedButton.styleFrom(
            backgroundColor: Theme.of(context).colorScheme.secondary,
            foregroundColor: Colors.white,
            textStyle: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.white),
          ),
          onPressed: _sending ? null : _sendFeedbackPressed,
        ),
      ],
    );
  }

  /// Helper functions

  _handleDontShowAgain() {
    ApiPrefs.setRatingDontShowAgain(true);
    locator<Analytics>().logEvent(AnalyticsEventConstants.RATING_DIALOG_DONT_SHOW_AGAIN);
    Navigator.of(context).pop();
  }

  _popAndSendAnalytics(int index) {
    locator<Analytics>().logEvent(
      AnalyticsEventConstants.RATING_DIALOG,
      extras: {AnalyticsParamConstants.STAR_RATING: index + 1},
    );
    Navigator.of(context).pop();
  }

  _handleStarPressed(int index) {
    // If the rating is high, don't show the rating dialog again and launch the app store so they can review there too
    if (index >= RatingDialog._RATING_TO_APP_STORE_THRESHOLD) {
      ApiPrefs.setRatingDontShowAgain(true);
      locator<UrlLauncher>().launchAppStore();
      _popAndSendAnalytics(index);
      return;
    }

    setState(() {
      _focusedStar = index;
      _selectedStar = index;
    });
  }

  _sendFeedbackPressed() async {
    setState(() {
      _sending = true; // Prevent the send button from getting clicked multiple times
    });

    if (_comment.isNotEmpty) await _sendFeedback();
    _popAndSendAnalytics(_selectedStar);
  }

  _sendFeedback() async {
    try {
      // Show again in 6 weeks since they're leaving a comment
      ApiPrefs.setRatingNextShowDate(DateTime.now().add(Duration(days: RatingDialog.SIX_WEEKS)));

      final l10n = L10n(context);

      final parentId = ApiPrefs.getUser()?.id ?? 0;
      final email = ApiPrefs.getUser()?.primaryEmail ?? '';
      final domain = ApiPrefs.getDomain() ?? '';

      DeviceInfoPlugin deviceInfo = DeviceInfoPlugin();
      AndroidDeviceInfo androidInfo = await deviceInfo.androidInfo;
      PackageInfo packageInfo = await PackageInfo.fromPlatform();


      final subject = l10n.ratingDialogEmailSubject(packageInfo.version);

      // Populate the email body with information about the user
      String emailBody = '' +
          '$_comment\r\n' +
          '\r\n' +
          '${l10n.helpUserId} $parentId\r\n' +
          '${l10n.helpEmail} $email\r\n' +
          '${l10n.helpDomain} $domain\r\n' +
          '${l10n.versionNumber}: ${packageInfo.appName} v${packageInfo.version} (${packageInfo.buildNumber})\r\n' +
          '${l10n.device}: ${androidInfo.manufacturer} ${androidInfo.model}\r\n' +
          '${l10n.osVersion}: Android ${androidInfo.version.release}\r\n' +
          '----------------------------------------------\r\n';

      locator<AndroidIntentVeneer>().launchEmailWithBody(subject, emailBody);
    } catch (_) {} // Catch any errors that come from trying to launch an email
  }
}
