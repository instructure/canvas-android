/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter_parent/utils/base_model.dart';
import 'package:test/test.dart';

void main() {
  test('Initializes BaseModel state to Idle', () {
    final model = BaseModel();
    expect(model.state, ViewState.Idle);
  });

  test('Calling setState to Busy notifies the liseners', () {
    final model = BaseModel();
    model.addListener(() => expect(model.state, ViewState.Busy));
    model.setState(viewState: ViewState.Busy);
  });

  test('Calling work notifies listeners that the state is busy', () async {
    final model = BaseModel();
    final busyListener = () => expect(model.state, ViewState.Busy);
    model.addListener(busyListener);
    await model.work(() async {
      // Remove the listener, otherwise it will fail for future listener updates
      model.removeListener(busyListener);
    });
  });

  test('Calling work notifies listeners that the state is Idle after the block is run', () async {
    final model = BaseModel();
    await model.work(() async {
      expect(model.state, ViewState.Busy);
      model.addListener(() => expect(model.state, ViewState.Idle));
    });
    expect(model.state, ViewState.Idle);
  });

  test('Calling work notifies listeners that the state is Error if the block throws an exception', () async {
    final model = BaseModel();
    await model.work(() {
      model.addListener(() => expect(model.state, ViewState.Error));
      return Future.error('This failed');
    });
    expect(model.state, ViewState.Error);
  });

  test('Calling work ends with a state of Idle if the block is null', () async {
    final model = BaseModel();
    await model.work(null);
    expect(model.state, ViewState.Idle);
  });
}
