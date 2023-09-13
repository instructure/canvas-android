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
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class QRLoginUtil {
  launchQRTutorial(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (context) {
        return ListView(
          padding: EdgeInsets.symmetric(vertical: 20, horizontal: 16),
          shrinkWrap: true,
          children: <Widget>[
            Text(L10n(context).qrLoginSelect, style: Theme.of(context).textTheme.bodySmall),
            SizedBox(height: 12),
            _login(context),
            _createAccount(context),
          ],
        );
      },
    );
  }

  Widget _login(BuildContext context) {
    return ListTile(
      title: Text(L10n(context).qrLoginHaveAccount),
      contentPadding: EdgeInsets.symmetric(vertical: 10),
      onTap: () async {
        Navigator.of(context).pop();
        locator<Analytics>().logEvent(AnalyticsEventConstants.QR_LOGIN_CLICKED);
        locator<QuickNav>().pushRoute(context, PandaRouter.qrTutorial());
      },
    );
  }

  Widget _createAccount(BuildContext context) {
    return ListTile(
      title: Text(L10n(context).qrLoginNewAccount),
      contentPadding: EdgeInsets.symmetric(vertical: 10),
      onTap: () async {
        Navigator.of(context).pop();
        locator<Analytics>().logEvent(AnalyticsEventConstants.QR_ACCOUNT_CREATION_CLICKED);
        locator<QuickNav>().pushRoute(context, PandaRouter.qrPairing(isCreatingAccount: true));
      },
    );
  }
}
