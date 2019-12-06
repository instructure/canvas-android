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

import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/utils/base_model.dart';

class ManageStudentsModel extends BaseModel {
  int observerId;
  List<User> students;

  ManageStudentsModel(this.students);

  // Used when we are deep linking and don't have the students already
  ManageStudentsModel.withObserverId(this.observerId);

//  Future<void> loadData({bool refreshStudents = false}) {
//    return work();
//  }
}