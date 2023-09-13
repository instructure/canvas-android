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
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_svg/svg.dart';

class UnderConstructionScreen extends StatelessWidget {
  final bool showAppBar;

  const UnderConstructionScreen({this.showAppBar = false, super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: showAppBar && (ModalRoute.of(context)?.canPop ?? false)
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
        children: <Widget>[],
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
              SvgPicture.asset('assets/svg/panda-under-construction.svg'),
              SizedBox(height: 64),
              Text(
                L10n(context).underConstruction,
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
              SizedBox(height: 8),
              Text(
                L10n(context).currentlyBuildingThisFeature,
                textAlign: TextAlign.center,
                style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.normal),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
