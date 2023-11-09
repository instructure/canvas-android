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
import 'package:flutter_parent/models/terms_of_service.dart';
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/web_view_utils.dart';
import 'package:webview_flutter/webview_flutter.dart';
import '../../utils/veneers/android_intent_veneer.dart';

class TermsOfUseScreen extends StatefulWidget {
  final String? accountId;
  final String? domain;

  const TermsOfUseScreen({this.accountId, this.domain, super.key});

  @override
  _TermsOfUseScreenState createState() => _TermsOfUseScreenState();
}

class _TermsOfUseScreenState extends State<TermsOfUseScreen> {
  late Future<TermsOfService?> _tosFuture;

  @override
  void initState() {
    _tosFuture = getTosFuture();
    super.initState();
  }

  Future<TermsOfService?> getTosFuture() {
    return (widget.accountId != null && widget.domain != null)
        ? locator<AccountsApi>().getTermsOfServiceForAccount(widget.accountId!, widget.domain!)
        : locator<AccountsApi>().getTermsOfService();
  }

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        appBar: AppBar(
          title: Text(L10n(context).termsOfUse),
          bottom: ParentTheme.of(context)?.appBarDivider(shadowInLightMode: false),
        ),
        body: FutureBuilder(
          future: _tosFuture,
          builder: (BuildContext context, AsyncSnapshot<TermsOfService?> snapshot) {
            // Loading
            if (snapshot.connectionState != ConnectionState.done) return LoadingIndicator();

            // Error
            if (snapshot.hasError) {
              return ErrorPandaWidget(
                L10n(context).errorLoadingTermsOfUse,
                () => setState(() {
                  _tosFuture = getTosFuture();
                }),
              );
            }

            // Content
            return WebView(
              onWebViewCreated: (controller) {
                controller.loadHtml(snapshot.data!.content!,
                      horizontalPadding: 16,
                      darkMode:
                          ParentTheme.of(context)?.isWebViewDarkMode ?? false);
                },
              navigationDelegate: _handleNavigation
            );
          },
        ),
      ),
    );
  }

  NavigationDecision _handleNavigation(NavigationRequest request) {
    if (request.url.contains("mailto:")) {
      locator<AndroidIntentVeneer>().launchEmail(request.url);
      return NavigationDecision.prevent;
    }
    return NavigationDecision.navigate;
  }
}
