// Copyright (C) 2023 - present Instructure, Inc.
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
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/aup/acceptable_use_policy_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/masquerade_ui.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/html_description_screen.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/features_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';

import '../../parent_app.dart';

class AcceptableUsePolicyScreen extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _AcceptableUsePolicyState();
}

class _AcceptableUsePolicyState extends State<AcceptableUsePolicyScreen> {
  AcceptableUsePolicyInteractor _interactor =
      locator<AcceptableUsePolicyInteractor>();
  bool _isAccepted = false;

  @override
  Widget build(BuildContext context) {
    return DefaultParentTheme(
        builder: (context) => Scaffold(
              appBar: AppBar(
                title: Text(L10n(context).acceptableUsePolicyTitle),
                leading: IconButton(
                  icon: Icon(CanvasIcons.x),
                  onPressed: () => _close(),
                ),
                actions: [
                  TextButton(
                      onPressed: _isAccepted ? () => _confirm() : null,
                      child: Text(L10n(context).acceptableUsePolicyConfirm))
                ],
              ),
              body: Column(
                children: [
                  Divider(),
                  Container(
                      padding:
                          EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                      child: Text(
                        L10n(context).acceptableUsePolicyDescription,
                        style: TextStyle(fontSize: 16),
                      )),
                  Divider(),
                  TextButton(
                      onPressed: _readPolicy,
                      style: TextButton.styleFrom(padding: EdgeInsets.symmetric(horizontal: 16, vertical: 12)),
                      child: Row(
                        children: [
                          Text(L10n(context).acceptableUsePolicyTitle,
                            style: Theme.of(context).textTheme.titleMedium?.copyWith(fontSize: 16)),
                          Spacer(),
                          Icon(
                            CanvasIcons.arrow_open_right,
                            color: Theme.of(context).iconTheme.color,
                            size: 12,
                          )
                        ],
                      )),
                  Divider(),
                  Container(
                    padding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    child: Row(
                      children: [
                        Text(
                          L10n(context).acceptableUsePolicyAgree,
                          style: TextStyle(fontSize: 16),
                        ),
                        Spacer(),
                        Switch(
                            value: _isAccepted,
                            onChanged: (isEnabled) =>
                                _onSwitchChanged(isEnabled))
                      ],
                    ),
                  ),
                  Divider()
                ],
              ),
            ));
  }

  _close() async {
    try {
      locator<Analytics>().logEvent(AnalyticsEventConstants.LOGOUT);
      await ParentTheme.of(context)?.setSelectedStudent(null);
      await ApiPrefs.performLogout(app: ParentApp.of(context));
      MasqueradeUI.of(context)?.refresh();
      await FeaturesUtils.performLogout();
      locator<QuickNav>().pushRouteAndClearStack(context, PandaRouter.login());
    } catch (e) {
      // Just in case we experience any error we still need to go back to the login screen.
      locator<QuickNav>().pushRouteAndClearStack(context, PandaRouter.login());
    }
  }

  _confirm() async {
    await _interactor.acceptTermsOfUse();
    locator<QuickNav>().pushRouteAndClearStack(
        context, PandaRouter.rootSplash());
  }

  _readPolicy() {
    _interactor.getTermsOfService().then((termsOfService) => locator<QuickNav>()
        .push(
            context,
            HtmlDescriptionScreen(termsOfService?.content,
                L10n(context).acceptableUsePolicyTitle)));
  }

  _onSwitchChanged(bool isEnabled) {
    setState(() {
      _isAccepted = isEnabled;
    });
  }
}
