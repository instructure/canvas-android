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

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/common_widgets/dialog_with_navigator_key.dart';
import 'package:flutter_parent/utils/common_widgets/respawn.dart';
import 'package:flutter_parent/utils/common_widgets/user_name.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/style_slicer.dart';

import '../features_utils.dart';

class MasqueradeUI extends StatefulWidget {
  final Widget child;
  final GlobalKey<NavigatorState> navKey;

  const MasqueradeUI({required this.child, required this.navKey, super.key});

  @override
  MasqueradeUIState createState() => MasqueradeUIState();

  static MasqueradeUIState? of(BuildContext context) {
    return context.findAncestorStateOfType<MasqueradeUIState>();
  }

  static void showMasqueradeCancelDialog(GlobalKey<NavigatorState> navKey, BuildContext context) {
    bool logout = ApiPrefs.getCurrentLogin()?.isMasqueradingFromQRCode == true;
    User user = ApiPrefs.getUser()!;
    showDialog(
      context: navKey.currentContext ?? context,
      builder: (context) {
        AppLocalizations l10n = L10n(context);
        var nameText = UserName.fromUser(user).text;
        String messageText = logout ? l10n.endMasqueradeLogoutMessage(nameText) : l10n.endMasqueradeMessage(nameText);
        return AlertDialog(
          title: Text(L10n(context).stopActAsUser),
          content: Text.rich(StyleSlicer.apply(messageText, [PronounSlice(user.pronouns)])),
          actions: [
            TextButton(
              child: new Text(L10n(context).cancel),
              onPressed: () => navKey.currentState?.pop(false),
            ),
            TextButton(
              child: new Text(L10n(context).ok),
              onPressed: () async {
                if (logout) {
                  await ParentTheme.of(context)?.setSelectedStudent(null);
                  await ApiPrefs.performLogout();
                  await FeaturesUtils.performLogout();
                  MasqueradeUI.of(context)?.refresh();
                  locator<QuickNav>().pushRouteAndClearStack(context, PandaRouter.login());
                } else {
                  ApiPrefs.updateCurrentLogin((b) => b
                    ..masqueradeUser = null
                    ..masqueradeDomain = null);
                  Respawn.of(context)?.restart();
                }
              },
            ),
          ],
        );
      },
    );
  }
}

class MasqueradeUIState extends State<MasqueradeUI> {
  bool _enabled = false;
  late User _user;

  GlobalKey _childKey = GlobalKey();

  bool get enabled => _enabled;

  @override
  void initState() {
    refresh(shouldSetState: false);
    super.initState();
  }

  void refresh({bool shouldSetState = true}) {
    bool wasEnabled = _enabled;
    if (ApiPrefs.isLoggedIn() && ApiPrefs.isMasquerading()) {
      _enabled = true;
      _user = ApiPrefs.getUser()!;
    } else {
      _enabled = false;
    }
    if (wasEnabled != _enabled && shouldSetState) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        setState(() {});
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    Widget child = KeyedSubtree(key: _childKey, child: widget.child);
    if (!_enabled) return child;
    String message = L10n(context).actingAsUser(UserName.fromUser(_user).text);
    return SafeArea(
      child: Material(
        child: Container(
          key: Key('masquerade-ui-container'),
          foregroundDecoration: BoxDecoration(
            border: Border.all(color: ParentColors.masquerade, width: 3.0),
          ),
          child: Column(
            children: [
              Container(
                color: ParentColors.masquerade,
                child: Row(
                  mainAxisSize: MainAxisSize.max,
                  children: <Widget>[
                    SizedBox(width: 16),
                    Expanded(
                      child: Text.rich(
                        StyleSlicer.apply(message, [PronounSlice(_user.pronouns)]),
                        style: TextStyle(color: Colors.white),
                      ),
                    ),
                    IconButton(
                      icon: Icon(
                        Icons.close,
                        color: Colors.white,
                        semanticLabel: L10n(context).stopActAsUser,
                      ),
                      onPressed: () => MasqueradeUI.showMasqueradeCancelDialog(widget.navKey, context),
                    ),
                  ],
                ),
              ),
              Expanded(child: child),
            ],
          ),
        ),
      ),
    );
  }
}
