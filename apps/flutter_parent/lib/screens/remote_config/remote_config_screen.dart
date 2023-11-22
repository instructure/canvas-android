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
import 'package:flutter_parent/screens/remote_config/remote_config_interactor.dart';
import 'package:flutter_parent/utils/remote_config_utils.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class RemoteConfigScreen extends StatefulWidget {
  @override
  _RemoteConfigScreenState createState() => _RemoteConfigScreenState();
}

class _RemoteConfigScreenState extends State<RemoteConfigScreen> {
  RemoteConfigInteractor _interactor = locator<RemoteConfigInteractor>();

  late Map<RemoteConfigParams, String> _remoteConfig;

  @override
  void initState() {
    _remoteConfig = _interactor.getRemoteConfigParams();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
          return Scaffold(
            appBar: AppBar(title: Text('Remote Config Params')),
            body: Padding(
              padding: const EdgeInsets.all(8.0),
              child: ListView(
                key: Key('remote_config_params_list'),
                children: _createListItems(_remoteConfig),
              ),
            ),
          );
  }

  List<Widget> _createListItems(Map<RemoteConfigParams, String> remoteConfigParams) {
    return remoteConfigParams.entries.map((e) => _createListItem(e)).toList();
  }

  Widget _createListItem(MapEntry<RemoteConfigParams, String> entry) {
    return Row(children: [
      Align(
        child: Text(RemoteConfigUtils.getRemoteConfigName(entry.key)),
        alignment: Alignment.centerLeft,
      ),
      Flexible(
          child: Padding(
        padding: EdgeInsets.only(left: 8.0),
        child: TextField(
          controller: TextEditingController()..text = entry.value,
          onChanged: (value) => {
            _interactor.updateRemoteConfig(entry.key, value)
          },
        ),
      ))
    ]);
  }
}
