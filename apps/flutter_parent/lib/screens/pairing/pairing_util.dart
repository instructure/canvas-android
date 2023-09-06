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
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/pairing/pairing_code_dialog.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_svg/flutter_svg.dart';

class PairingUtil {
  pairNewStudent(BuildContext context, Function() onSuccess) {
    showModalBottomSheet(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      context: context,
      isScrollControlled: true,
      builder: (context) {
        return ListView(
          padding: EdgeInsets.symmetric(vertical: 20, horizontal: 16),
          shrinkWrap: true,
          children: <Widget>[
            Text(L10n(context).addStudentWith, style: Theme.of(context).textTheme.bodyMedium),
            SizedBox(height: 12),
            _pairingCode(context, onSuccess),
            if (_hasCameras()) _qrCode(context, onSuccess),
          ],
        );
      },
    );
  }

  Widget _qrCode(BuildContext context, Function() onSuccess) {
    return ListTile(
      title: Text(L10n(context).qrCode, style: Theme.of(context).textTheme.titleMedium),
      subtitle: Padding(
        padding: const EdgeInsets.only(top: 4),
        child: Text(L10n(context).qrCodeDescription, style: Theme.of(context).textTheme.bodyMedium),
      ),
      leading: SvgPicture.asset('assets/svg/qr-code.svg', color: ParentColors.ash, width: 25, height: 25),
      contentPadding: EdgeInsets.symmetric(vertical: 10),
      onTap: () async {
        Navigator.of(context).pop();
        locator<Analytics>().logEvent(AnalyticsEventConstants.ADD_STUDENT_MANAGE_STUDENTS);
        var studentPaired = await locator<QuickNav>().pushRoute(context, PandaRouter.qrPairing());
        if (studentPaired == true) onSuccess();
      },
    );
  }

  Widget _pairingCode(BuildContext context, Function() onSuccess) {
    return ListTile(
      title: Text(L10n(context).pairingCode, style: Theme.of(context).textTheme.titleMedium),
      subtitle: Padding(
        padding: const EdgeInsets.only(top: 4),
        child: Text(L10n(context).pairingCodeDescription, style: Theme.of(context).textTheme.bodyMedium),
      ),
      leading: Icon(CanvasIcons.keyboard_shortcuts, color: ParentColors.ash),
      contentPadding: EdgeInsets.symmetric(vertical: 10),
      onTap: () async {
        Navigator.of(context).pop();
        locator<Analytics>().logEvent(AnalyticsEventConstants.ADD_STUDENT_MANAGE_STUDENTS);
        bool? studentPaired = await locator<QuickNav>().showDialog<bool>(
          context: context,
          barrierDismissible: true,
          builder: (BuildContext context) => PairingCodeDialog(null),
        );
        if (studentPaired == true) onSuccess();
      },
    );
  }
}

class StudentAddedNotifier extends ChangeNotifier {
  void notify() => notifyListeners();
}

bool _hasCameras() {
  return ApiPrefs.getCameraCount() != null && ApiPrefs.getCameraCount() != 0;
}
