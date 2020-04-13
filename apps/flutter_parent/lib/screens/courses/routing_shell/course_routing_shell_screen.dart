/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:flutter/material.dart';

enum CourseShellType {
  frontPage,
  syllabus,
}

class CourseRoutingShellScreen extends StatefulWidget {
  final String courseId;
  final CourseShellType type;

  CourseRoutingShellScreen(this.courseId, this.type, {Key key}) : super(key: key);

  @override
  State<StatefulWidget> createState() => _CourseRoutingShellScreenState();
}

class _CourseRoutingShellScreenState extends State<CourseRoutingShellScreen> {
  @override
  Widget build(BuildContext context) {
    return null;
  }
}
