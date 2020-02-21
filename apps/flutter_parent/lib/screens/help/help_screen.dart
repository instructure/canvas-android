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
import 'package:android_intent/android_intent.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_dialog.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/veneers/AndroidIntentVeneer.dart';
import 'package:intent/action.dart' as android;
import 'package:intent/extra.dart' as android;
import 'package:intent/intent.dart' as android;
import 'package:package_info/package_info.dart';
import 'package:url_launcher/url_launcher.dart';

import 'legal_screen.dart';

class HelpScreen extends StatefulWidget {
  @override
  _HelpScreenState createState() => _HelpScreenState();
}

class _HelpScreenState extends State<HelpScreen> {
  @override
  Widget build(BuildContext context) {
    final l10n = L10n(context);
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        appBar: AppBar(
          title: Text(l10n.help),
          bottom: ParentTheme.of(context).appBarDivider(shadowInLightMode: false),
        ),
        body: ListView(
          children: <Widget>[
            ListTile(
              title: Text(l10n.helpSearchCanvasDocsLabel),
              subtitle: Text(l10n.helpSearchCanvasDocsDescription),
              onTap: _showSearch,
            ),
            ListTile(
              title: Text(l10n.helpReportProblemLabel),
              subtitle: Text(l10n.helpReportProblemDescription),
              onTap: _showReportProblem,
            ),
            ListTile(
              title: Text(l10n.helpRequestFeatureLabel),
              subtitle: Text(l10n.helpRequestFeatureDescription),
              onTap: _showRequestFeature,
            ),
            ListTile(
              title: Text(l10n.helpShareLoveLabel),
              subtitle: Text(l10n.helpShareLoveDescription),
              onTap: _showShareLove,
            ),
            ListTile(
              title: Text(l10n.helpLegalLabel),
              subtitle: Text(l10n.helpLegalDescription),
              onTap: _showLegal,
            ),
          ],
        ),
      ),
    );
  }

  void _showSearch() => launch(
      'https://community.canvaslms.com/community/answers/guides/mobile-guide/content?filterID=contentstatus%5Bpublished%5D~category%5Btable-of-contents%5D');

  void _showReportProblem() => ErrorReportDialog.asDialog(context);

  void _showRequestFeature() async {
    final l10n = L10n(context);

    final parentId = ApiPrefs.getUser()?.id ?? 0;
    final email = ApiPrefs.getUser()?.primaryEmail ?? '';
    final domain = ApiPrefs.getDomain() ?? '';
    final locale = ApiPrefs.effectiveLocale().toLanguageTag();

    PackageInfo package = await PackageInfo.fromPlatform();

    // Populate the email body with information about the user
    String emailBody = '' +
        '${l10n.featureRequestHeader}\r\n' +
        '${l10n.helpUserId} $parentId\r\n' +
        '${l10n.helpEmail} $email\r\n' +
        '${l10n.helpDomain} $domain\r\n' +
        '${l10n.versionNumber}: ${package.appName} v${package.version} (${package.buildNumber})\r\n' +
        '${l10n.helpLocale} $locale\r\n' +
        '----------------------------------------------\r\n';

    final subject = l10n.featureRequestSubject;
    final canvasEmail = 'mobilesupport@instructure.com';

    _sendIntent(canvasEmail, subject, emailBody);
//    _sendAndroidIntent(canvasEmail, subject, emailBody);
  }

  // Can't use yet, this doesn't set the 'email' field properly. Also can't specify all components via the data uri, as
  //  the encoding isn't properly handled by receiving apps (either spaces are turned into '+' or new lines aren't included).
  //  Can update once AndroidIntent supports string arrays rather than just string array lists (confirmed this is what's
  //  breaking, can include a link to the flutter plugin PR to fix this once I get one made)
  void _sendAndroidIntent(String canvasEmail, String subject, String emailBody) {
    final intent = AndroidIntent(
      action: 'android.intent.action.SENDTO',
      data: Uri(scheme: 'mailto').toString(),
      arguments: {
        'android.intent.extra.EMAIL': [canvasEmail],
        'android.intent.extra.SUBJECT': subject,
        'android.intent.extra.TEXT': emailBody,
      },
    );

    AndroidIntentVeneer().launch(intent);
  }

  // TODO: Switch to AndroidIntent once it supports emails properly (either can't specify 'to' email, or body doesn't support multiline)
  void _sendIntent(String canvasEmail, String subject, String emailBody) {
    android.Intent()
      ..setAction(android.Action.ACTION_SENDTO)
      ..setData(Uri(scheme: 'mailto'))
      ..putExtra(android.Extra.EXTRA_EMAIL, [canvasEmail])
      ..putExtra(android.Extra.EXTRA_SUBJECT, subject)
      ..putExtra(android.Extra.EXTRA_TEXT, emailBody)
      ..startActivity(createChooser: true);
  }

  void _showShareLove() => launch('https://play.google.com/store/apps/details?id=com.instructure.parentapp');

  void _showLegal() => locator<QuickNav>().push(context, LegalScreen());
}
