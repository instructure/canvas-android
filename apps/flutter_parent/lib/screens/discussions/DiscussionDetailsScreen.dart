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
import 'package:flutter_parent/screens/under_construction_screen.dart';

class DiscussionDetailsScreen extends StatefulWidget {
  final String courseId;
  final String topicId;

  const DiscussionDetailsScreen({
    Key key,
    @required this.courseId,
    @required this.topicId,
  })  : assert(courseId != null),
        assert(topicId != null),
        super(key: key);

  @override
  _DiscussionDetailsScreenState createState() => _DiscussionDetailsScreenState();
}

class _DiscussionDetailsScreenState extends State<DiscussionDetailsScreen> {
  @override
  Widget build(BuildContext context) {
    return UnderConstructionScreen();
  }
}
