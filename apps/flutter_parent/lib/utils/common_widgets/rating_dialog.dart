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
import 'package:device_info/device_info.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/common_widgets/arrow_aware_focus_scope.dart';
import 'package:flutter_parent/utils/common_widgets/full_screen_scroll_container.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/veneers/AndroidIntentVeneer.dart';
import 'package:package_info/package_info.dart';

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
  /// 1. User presses 5 stars -> take user to play store and don't show dialog again
  /// 2. User presses < 5 stars with no comment -> show again 4 weeks later
  /// 3. User presses < 5 stars with a comment -> show again 6 weeks later
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

    final date = DateTime.now().millisecondsSinceEpoch;
    if ((ApiPrefs.getRatingFirstLaunchDate() ?? 0) == 0) {
      ApiPrefs.setRatingFirstLaunchDate(date);
      return Future.value();
    }

    if (date < (ApiPrefs.getRatingFirstLaunchDate() + _showAgainDate())) return Future.value();

    ApiPrefs.setRatingShowAgainWait(FOUR_WEEKS); // Update the show again to 4 weeks, in case they cancel the dialog
    ApiPrefs.setRatingFirstLaunchDate(date); // Reset the date first launched to now

    return showDialog(
      context: context,
      builder: (context) => RatingDialog._internal(),
    );
  }

  /// Gets the show again date from prefs and converts it to milliseconds, defaulting to four weeks
  static int _showAgainDate() {
    final showAgainDate = ApiPrefs.getRatingShowAgainWait() ?? FOUR_WEEKS;
    return showAgainDate * 24 * 60 * 60 * 1000;
  }

  const RatingDialog._internal({Key key}) : super(key: key);

  @override
  _RatingDialogState createState() => _RatingDialogState();
}

class _RatingDialogState extends State<RatingDialog> {
  String _comment;
  int _focusedStar;
  int _selectedStar;
  bool _sending;

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
              FlatButton(
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
        RaisedButton(
          child: Text(L10n(context).ratingDialogSendFeedback.toUpperCase()),
          color: Theme.of(context).accentColor,
          textColor: Colors.white,
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
      ApiPrefs.setRatingShowAgainWait(RatingDialog.SIX_WEEKS); // Show again in 6 weeks since they're leaving a comment

      final l10n = L10n(context);

      final parentId = ApiPrefs.getUser()?.id ?? 0;
      final email = ApiPrefs.getUser()?.primaryEmail ?? '';
      final domain = ApiPrefs.getDomain() ?? '';

      final info = await Future.wait([PackageInfo.fromPlatform(), DeviceInfoPlugin().androidInfo]);
      PackageInfo package = info[0];
      AndroidDeviceInfo device = info[1];

      final subject = l10n.ratingDialogEmailSubject(package.version);

      // Populate the email body with information about the user
      String emailBody = '' +
          '$_comment\r\n' +
          '\r\n' +
          '${l10n.helpUserId} $parentId\r\n' +
          '${l10n.helpEmail} $email\r\n' +
          '${l10n.helpDomain} $domain\r\n' +
          '${l10n.versionNumber}: ${package.appName} v${package.version} (${package.buildNumber})\r\n' +
          '${l10n.device}: ${device.manufacturer} ${device.model}\r\n' +
          '${l10n.osVersion}: Android ${device.version.release}\r\n' +
          '----------------------------------------------\r\n';

      locator<AndroidIntentVeneer>().launchEmailWithBody(subject, emailBody);
    } catch (_) {} // Catch any errors that come from trying to launch an email
  }
}
