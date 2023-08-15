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

import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_dialog.dart';
import 'package:flutter_parent/utils/common_widgets/respawn.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_svg/svg.dart';
import 'package:package_info_plus/package_info_plus.dart';

class CrashScreen extends StatelessWidget {
  final FlutterErrorDetails error;

  const CrashScreen(this.error, {super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: ModalRoute.of(context)?.canPop ?? false
          ? AppBar(
              elevation: 0,
              backgroundColor: Colors.transparent,
              iconTheme: Theme.of(context).iconTheme,
              bottom: ParentTheme.of(context)?.appBarDivider(shadowInLightMode: false),
            )
          : null,
      body: _body(context),
      bottomNavigationBar: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: <Widget>[_errorDetailsButton(context), _restartButton(context)],
      ),
    );
  }

  Center _body(BuildContext context) {
    return Center(
      child: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              SvgPicture.asset('assets/svg/panda-not-supported.svg'),
              SizedBox(height: 64),
              Text(
                L10n(context).crashScreenTitle,
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.w600),
              ),
              SizedBox(height: 8),
              Text(
                L10n(context).crashScreenMessage,
                style: TextStyle(fontSize: 16),
                textAlign: TextAlign.center,
              ),
              SizedBox(height: 40),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: <Widget>[
                  TextButton(
                    onPressed: () => ErrorReportDialog.asDialog(context, error: error),
                    child: Text(
                      L10n(context).crashScreenContact,
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(fontSize: 16),
                    ),
                    style: TextButton.styleFrom(
                      shape: RoundedRectangleBorder(
                        borderRadius: new BorderRadius.circular(4),
                        side: BorderSide(color: ParentColors.tiara),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _errorDetailsButton(BuildContext context) {
    return FutureBuilder(
      future: Future.wait([PackageInfo.fromPlatform(), DeviceInfoPlugin().androidInfo]),
      builder: (context, snapshot) {
        if (!snapshot.hasData) return Container();
        PackageInfo packageInfo = snapshot.data![0] as PackageInfo;
        AndroidDeviceInfo deviceInfo = snapshot.data![1] as AndroidDeviceInfo;
        return TextButton(
          onPressed: () => _showDetailsDialog(context, packageInfo, deviceInfo),
          child: Text(
            L10n(context).crashScreenViewDetails,
            style: Theme.of(context).textTheme.titleSmall,
          ),
        );
      },
    );
  }

  TextButton _restartButton(BuildContext context) {
    return TextButton(
      onPressed: () => Respawn.of(context)?.restart(),
      child: Text(
        L10n(context).crashScreenRestart,
        style: Theme.of(context).textTheme.titleSmall,
      ),
    );
  }

  String _getFullErrorMessage() {
    String message = '';
    try {
      message = error.exception.toString();
    } catch (e) {
      // Intentionally left blank
    }
    return '$message\n\n${error.stack.toString()}';
  }

  _showDetailsDialog(BuildContext context, PackageInfo packageInfo, AndroidDeviceInfo deviceInfo) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        contentPadding: EdgeInsets.all(0),
        content: Container(
          width: double.maxFinite,
          child: ListView(
            padding: EdgeInsets.all(8),
            shrinkWrap: true,
            children: <Widget>[
              ListTile(
                title: Text(L10n(context).crashDetailsAppVersion),
                subtitle: Text('${packageInfo.version} (${packageInfo.buildNumber})'),
              ),
              ListTile(
                title: Text(L10n(context).crashDetailsDeviceModel),
                subtitle: Text('${deviceInfo.manufacturer} ${deviceInfo.model}'),
              ),
              ListTile(
                title: Text(L10n(context).crashDetailsAndroidVersion),
                subtitle: Text(deviceInfo.version.release),
              ),
              ExpansionTile(
                title: Text(L10n(context).crashDetailsFullMessage),
                children: <Widget>[
                  Padding(
                    padding: const EdgeInsets.fromLTRB(8, 0, 8, 16),
                    child: Container(
                      key: Key('full-error-message'),
                      padding: EdgeInsets.all(8),
                      decoration: BoxDecoration(
                          color: ParentTheme.of(context)?.nearSurfaceColor,
                          borderRadius: BorderRadius.all(Radius.circular(8))),
                      child: Text(
                        _getFullErrorMessage(),
                        style: TextStyle(fontSize: 12, fontWeight: FontWeight.normal),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
        actions: <Widget>[
          TextButton(
            child: Text(L10n(context).done),
            onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      ),
    );
  }
}
