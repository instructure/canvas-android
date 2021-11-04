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
// import 'package:intent/action.dart' as android;
// import 'package:intent/extra.dart' as android;
// import 'package:intent/intent.dart' as android;

class AndroidIntentVeneer {
  launch(AndroidIntent intent) => intent.launch();

  launchPhone(String phoneNumber) {
    // android.Intent()
    //   ..setAction(android.Action.ACTION_DIAL)
    //   ..setData(Uri.parse(phoneNumber))
    //   ..startActivity(createChooser: false);
  }

  launchEmail(String url) {
    // android.Intent()
    //   ..setAction(android.Action.ACTION_SENDTO)
    //   ..setData(Uri.parse(url))
    //   ..startActivity(createChooser: true);
  }

  // TODO: Switch to AndroidIntent once it supports emails properly (either can't specify 'to' email, or body doesn't support multiline)
  launchEmailWithBody(String subject, String emailBody, {String recipientEmail = 'mobilesupport@instructure.com'}) {
//    _launchEmailWithBody(canvasEmail, subject, emailBody); // Can't do until it supports email better
//     android.Intent()
//       ..setAction(android.Action.ACTION_SENDTO)
//       ..setData(Uri(scheme: 'mailto'))
//       ..putExtra(android.Extra.EXTRA_EMAIL, [recipientEmail])
//       ..putExtra(android.Extra.EXTRA_SUBJECT, subject)
//       ..putExtra(android.Extra.EXTRA_TEXT, emailBody)
//       ..startActivity(createChooser: true);
  }

  // Can't use yet, this doesn't set the 'email' field properly. Also can't specify all components via the data uri, as
  //  the encoding isn't properly handled by receiving apps (either spaces are turned into '+' or new lines aren't included).
  //  Can update once AndroidIntent supports string arrays rather than just string array lists (confirmed this is what's
  //  breaking, can include a link to the flutter plugin PR to fix this once I get one made)
  void _launchEmailWithBody(String recipientEmail, String subject, String emailBody) {
    final intent = AndroidIntent(
      action: 'android.intent.action.SENDTO',
      data: Uri(scheme: 'mailto').toString(),
      arguments: {
        'android.intent.extra.EMAIL': [recipientEmail],
        'android.intent.extra.SUBJECT': subject,
        'android.intent.extra.TEXT': emailBody,
      },
    );

    launch(intent);
  }
}
