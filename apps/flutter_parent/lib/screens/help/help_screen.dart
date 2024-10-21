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
import 'package:flutter_parent/models/help_link.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_dialog.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_parent/utils/veneers/android_intent_veneer.dart';
import 'package:package_info_plus/package_info_plus.dart';

import 'help_screen_interactor.dart';

class HelpScreen extends StatefulWidget {
  @override
  _HelpScreenState createState() => _HelpScreenState();
}

class _HelpScreenState extends State<HelpScreen> {
  final _interactor = locator<HelpScreenInteractor>();
  late Future<List<HelpLink>> _helpLinksFuture;
  late AppLocalizations l10n;

  @override
  void initState() {
    _helpLinksFuture = _interactor.getObserverCustomHelpLinks(forceRefresh: true);
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    l10n = L10n(context);
    return FutureBuilder(
        future: _helpLinksFuture,
        builder: (context, snapshot) {
          Widget _body;
          _body = snapshot.connectionState == ConnectionState.waiting ? LoadingIndicator() : _success(snapshot.data);
          return DefaultParentTheme(
            builder: (context) => Scaffold(
                appBar: AppBar(
                  title: Text(l10n.help),
                  bottom: ParentTheme.of(context)?.appBarDivider(shadowInLightMode: false),
                ),
                body: _body),
          );
        });
  }

  Widget _success(List<HelpLink>? links) => ListView(children: _generateLinks(links));

  List<Widget> _generateLinks(List<HelpLink>? links) {
    List<Widget> helpLinks = List.from(links?.map(
      (l) => ListTile(
        title: Text(l.text ?? '', style: Theme.of(context).textTheme.titleMedium),
        subtitle: Text(l.subtext ?? '', style: Theme.of(context).textTheme.bodySmall),
        onTap: () => _linkClick(l),
      ),
    ) ?? []);

    // Add in the legal and share the love tiles
    helpLinks.addAll([
      ListTile(
        title: Text(l10n.helpShareLoveLabel, style: Theme.of(context).textTheme.titleMedium),
        subtitle: Text(l10n.helpShareLoveDescription, style: Theme.of(context).textTheme.bodySmall),
        onTap: _showShareLove,
      )
    ]);

    return helpLinks;
  }

  void _linkClick(HelpLink link) {
    String url = link.url ?? '';
    if (url[0] == '#') {
      // Internal link
      if (url.contains('#create_ticket')) {
        _showReportProblem();
      } else if (url.contains('#share_the_love')) {
        // Custom for Android
        _showShareLove();
      }
    } else if (link.id?.contains('submit_feature_idea') == true) {
      _showRequestFeature();
    } else if (url.startsWith('tel:+')) {
      // Support phone links: https://community.canvaslms.com/docs/DOC-12664-4214610054
      locator<AndroidIntentVeneer>().launchPhone(url);
    } else if (url.startsWith('mailto:')) {
      // Support mailto links: https://community.canvaslms.com/docs/DOC-12664-4214610054
      locator<AndroidIntentVeneer>().launchEmail(url);
    } else if (url.contains('cases.canvaslms.com/liveagentchat')) {
      // Chat with Canvas Support - Doesn't seem work properly with WebViews, so we kick it out
      // to the external browser
      locator<UrlLauncher>().launch(url);
    } else if (link.id?.contains('search_the_canvas_guides') == true) {
      // Send them to the mobile Canvas guides
      _showSearch();
    } else {
      // External url
      locator<UrlLauncher>().launch(url);
    }
  }

  void _showSearch() => locator<UrlLauncher>().launch(
      'https://community.canvaslms.com/community/answers/guides/mobile-guide/content?filterID=contentstatus%5Bpublished%5D~category%5Btable-of-contents%5D');

  void _showReportProblem() => ErrorReportDialog.asDialog(context);

  void _showRequestFeature() async {
    final l10n = L10n(context);

    final parentId = ApiPrefs.getUser()?.id ?? 0;
    final email = ApiPrefs.getUser()?.primaryEmail ?? '';
    final domain = ApiPrefs.getDomain() ?? '';
    final locale = ApiPrefs.effectiveLocale()?.toLanguageTag();

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

    locator<AndroidIntentVeneer>().launchEmailWithBody(subject, emailBody);
  }

  void _showShareLove() => locator<UrlLauncher>().launchAppStore();
}
