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

import 'package:flutter/widgets.dart';

/// Widget that can be respawned with a clean state. To perform respawn, call Respawn.of(context).kill()
class Respawn extends StatefulWidget {
  final Widget? child;

  const Respawn({this.child, super.key});

  @override
  _RespawnState createState() => _RespawnState();

  static _RespawnState? of(BuildContext context) {
    return context.findAncestorStateOfType<_RespawnState>();
  }
}

class _RespawnState extends State<Respawn> {
  Key _key = GlobalKey();

  restart() => setState(() => _key = UniqueKey());

  @override
  Widget build(BuildContext context) {
    return Container(
      key: _key,
      child: widget.child,
    );
  }
}
