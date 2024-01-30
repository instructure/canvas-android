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
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/url_launcher.dart';

class LegalScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final l10n = L10n(context);
    return DefaultParentTheme(
      builder: (context) => Scaffold(
        appBar: AppBar(
          title: Text(l10n.helpLegalLabel),
          bottom: ParentTheme.of(context)?.appBarDivider(shadowInLightMode: false),
        ),
        body: ListView(
          children: <Widget>[
            _LegalRow(
              label: l10n.privacyPolicy,
              onTap: () => locator<UrlLauncher>().launch('https://www.instructure.com/policies/product-privacy-policy'),
              icon: CanvasIcons.admin,
            ),
            _LegalRow(
              label: L10n(context).termsOfUse,
              icon: CanvasIcons.document,
              onTap: () => locator<QuickNav>().pushRoute(context, PandaRouter.termsOfUse()),
            ),
            _LegalRow(
              label: l10n.canvasOnGithub,
              icon: CanvasIcons.github,
              onTap: () => locator<UrlLauncher>().launch('https://github.com/instructure/canvas-android'),
            ),
          ],
        ),
      ),
    );
  }
}

class _LegalRow extends StatelessWidget {
  final String label;
  final VoidCallback onTap;
  final IconData icon;

  const _LegalRow({ required this.label, required this.onTap, required this.icon, super.key});

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;

    return ListTile(
      title: Row(
        children: <Widget>[
          Icon(icon, color: Theme.of(context).colorScheme.secondary, size: 20),
          SizedBox(width: 20),
          Expanded(child: Text(label, style: textTheme.titleMedium)),
        ],
      ),
      onTap: onTap,
    );
  }
}
