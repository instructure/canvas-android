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

import 'package:flutter/material.dart';

/// Represents the state of the view
enum ViewState { Idle, Busy, Error }

/// Represents the model of the widget
class BaseModel extends ChangeNotifier {
  ViewState _state = ViewState.Idle;

  ViewState get state => _state;

  void setState({required ViewState viewState}) {
    _state = viewState;
    notifyListeners();
  }

  // A helper method to set the state to busy when starting a load, and setting the state back to idle when done
  Future<void> work(Future Function()? loadBlock) async {
    try {
      setState(viewState: ViewState.Busy);
      if (loadBlock != null) await loadBlock();
      setState(viewState: ViewState.Idle);
    } catch (e) {
      print('error while doing work: $e');
      setState(viewState: ViewState.Error);
    }
  }
}
