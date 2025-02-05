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

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/parent_app.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/features_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_svg/svg.dart';

class NotAParentScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: <Widget>[
          Expanded(
            child: EmptyPandaWidget(
              svgPath: 'assets/svg/panda-book.svg',
              title: L10n(context).notAParentTitle,
              subtitle: L10n(context).notAParentSubtitle,
              buttonText: L10n(context).returnToLogin,
              onButtonTap: () async {
                await ApiPrefs.performLogout(app: ParentApp.of(context));
                await FeaturesUtils.performLogout();
                locator<QuickNav>().pushRouteAndClearStack(context, PandaRouter.login());
              },
            ),
          ),
          _appOptions(context),
        ],
      ),
    );
  }

  Padding _appOptions(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 32),
      child: ExpansionTile(
        trailing: SizedBox(width: 1), // Replace dropdown chevron with empty, non-zero width element
        title: Padding(
          padding: const EdgeInsetsDirectional.only(start: 16), // Offset to keep text centered
          child: Text(
            L10n(context).studentOrTeacherTitle,
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodySmall,
          ),
        ),
        children: [
          Text(
            L10n(context).studentOrTeacherSubtitle,
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodySmall,
          ),
          SizedBox(height: 24),
          _appButton(context, L10n(context).studentApp, L10n(context).canvasStudentApp, ParentColors.studentApp, 'assets/svg/canvas-logo-student.svg', () {
            locator<UrlLauncher>().launch('market://details?id=com.instructure.candroid');
          }),
          _appButton(context, L10n(context).teacherApp, L10n(context).canvasTeacherApp, ParentColors.teacherApp, 'assets/svg/canvas-logo-teacher.svg', () {
            locator<UrlLauncher>().launch('market://details?id=com.instructure.teacher');
          }),
          SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _appButton(BuildContext context, String name, String label, Color color, String logo, GestureTapCallback onTap) {
    return InkWell(
      onTap: onTap,
      child: Semantics(
        label: label,
        excludeSemantics: true,
        child: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              SvgPicture.asset(
                logo,
                height: 48,
                color: color,
              ),
              SizedBox(width: 8),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  SvgPicture.asset(
                    'assets/svg/canvas-wordmark.svg',
                    alignment: Alignment.centerLeft,
                    height: 24,
                  ),
                  Text(
                    name,
                    style: TextStyle(color: color, fontSize: 10, fontWeight: FontWeight.w900),
                  ),
                ],
              )
            ],
          ),
        ),
      ),
    );
  }
}
