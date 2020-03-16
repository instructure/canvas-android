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

class QuizDetailsScreen extends StatefulWidget {
  final String courseId;
  final String quizId;

  const QuizDetailsScreen({
    Key key,
    @required this.courseId,
    @required this.quizId,
  })  : assert(courseId != null),
        assert(quizId != null),
        super(key: key);

  @override
  _QuizDetailsScreenState createState() => _QuizDetailsScreenState();
}

class _QuizDetailsScreenState extends State<QuizDetailsScreen> {
  @override
  Widget build(BuildContext context) {
    return UnderConstructionScreen();
  }
}
