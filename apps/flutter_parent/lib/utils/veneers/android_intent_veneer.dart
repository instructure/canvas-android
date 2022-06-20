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

import 'package:android_intent_plus/android_intent.dart';

class AndroidIntentVeneer {
  launch(AndroidIntent intent) => intent.launch();

  launchPhone(String phoneNumber) {
    final intent = AndroidIntent(
        action: 'android.intent.action.DIAL',
        data: Uri.parse(phoneNumber).toString());

    launch(intent);
  }

  launchEmail(String url) {
    final intent = AndroidIntent(
        action: 'android.intent.action.SENDTO',
        data: Uri.parse(url).toString());

    launch(intent);
  }

  launchEmailWithBody(String subject, String emailBody,
      {String recipientEmail = 'mobilesupport@instructure.com'}) {
    final intent = AndroidIntent(
      action: 'android.intent.action.SENDTO',
      data: Uri(
              scheme: 'mailto',
              query: encodeQueryParameters(
                  {'subject': subject, 'body': emailBody}),
              path: recipientEmail)
          .toString(),
    );

    launch(intent);
  }

  String encodeQueryParameters(Map<String, String> params) {
    return params.entries
        .map((e) => '${Uri.encodeComponent(e.key)}=${Uri.encodeComponent(e.value)}')
        .join('&');
  }
}
