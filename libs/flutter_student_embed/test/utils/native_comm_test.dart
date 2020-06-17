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

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_student_embed/models/login.dart';
import 'package:flutter_student_embed/models/serializers.dart';
import 'package:flutter_student_embed/network/utils/api_prefs.dart';
import 'package:flutter_student_embed/screens/calendar/planner_fetcher.dart';
import 'package:flutter_student_embed/utils/design/student_colors.dart';
import 'package:flutter_student_embed/utils/native_comm.dart';
import 'package:flutter_test/flutter_test.dart';

import '../testutils/canvas_model_utils.dart';
import '../testutils/test_app.dart';

void main() {
  setUp(() {
    setupPlatformChannels();
    NativeComm.init();
    StudentColors.reset();
  });

  test('setStatusBarColor correctly invokes method on channel', () {
    Color color = Color(0xff123abc);
    String expectedColorString = 'ff123abc';
    bool called = false;
    String calledColorString;

    NativeComm.channel.setMockMethodCallHandler((call) async {
      called = call.method == NativeComm.methodSetStatusBarColor;
      calledColorString = call.arguments;
    });

    NativeComm.setStatusBarColor(color);

    expect(called, isTrue);
    expect(calledColorString, expectedColorString);
  });

  test('methodUpdateLoginData updates with null login data', () async {
    var login = Login((b) => b
      ..uuid = 'uuid'
      ..domain = 'domain'
      ..accessToken = 'token'
      ..user = CanvasModelTestUtils.mockUser().toBuilder());
    ApiPrefs.setLogin(login);

    await NativeComm.methodCallHandler(MethodCall(NativeComm.methodUpdateLoginData, null));

    expect(ApiPrefs.getCurrentLogin(), isNull);
  });

  test('methodUpdateLoginData updates with valid login data', () async {
    var login = Login((b) => b
      ..uuid = 'uuid'
      ..domain = 'domain'
      ..accessToken = 'token'
      ..user = CanvasModelTestUtils.mockUser().toBuilder());

    String loginJson = json.encode(serialize(login));

    await NativeComm.methodCallHandler(MethodCall(NativeComm.methodUpdateLoginData, loginJson));

    expect(ApiPrefs.getCurrentLogin(), login);
  });

  test('methodUpdateLoginData catches errors', () async {
    bool hasError = false;
    try {
      await NativeComm.methodCallHandler(MethodCall(NativeComm.methodUpdateLoginData, "This is not json"));
    } catch (e) {
      hasError = true;
    }
    expect(hasError, isFalse);
  });

  test('methodUpdateThemeData sets new theme values and calls onThemeUpdated', () async {
    Map<String, dynamic> data = {
      'primaryColor': Colors.red.value.toRadixString(16),
      'accentColor': Colors.orange.value.toRadixString(16),
      'buttonColor': Colors.yellow.value.toRadixString(16),
      'primaryTextColor': Colors.green.value.toRadixString(16),
      'contextColors': {
        'course_123': Colors.blue.value.toRadixString(16),
        'course_456': Colors.purple.value.toRadixString(16),
      }
    };

    bool onThemeUpdatedCalled = false;
    NativeComm.onThemeUpdated = () => onThemeUpdatedCalled = true;
    await NativeComm.methodCallHandler(MethodCall(NativeComm.methodUpdateThemeData, data));

    expect(onThemeUpdatedCalled, isTrue);
    expect(StudentColors.primaryColor.value, Colors.red.value);
    expect(StudentColors.accentColor.value, Colors.orange.value);
    expect(StudentColors.buttonColor.value, Colors.yellow.value);
    expect(StudentColors.primaryTextColor.value, Colors.green.value);
    expect(StudentColors.contextColors['course_123'].value, Colors.blue.value);
    expect(StudentColors.contextColors['course_456'].value, Colors.purple.value);
  });

  test('methodUpdateThemeData catches errors', () async {
    bool hasError = false;
    try {
      await NativeComm.methodCallHandler(MethodCall(NativeComm.methodUpdateThemeData, "This is not valid data"));
    } catch (e) {
      hasError = true;
    }
    expect(hasError, isFalse);
  });

  test('methodUpdateCalendarDates updates PlannerFetcher', () async {
    bool updateCalled = false;
    PlannerFetcher.updateNotifier.addListener(() => updateCalled = true);
    await NativeComm.methodCallHandler(MethodCall(NativeComm.methodUpdateCalendarDates, []));

    expect(updateCalled, isTrue);
  });

  test('methodReset resets route, ApiPrefs, and StudentColors', () async {
    bool resetRouteCalled = false;
    NativeComm.resetRoute = () => resetRouteCalled = true;

    bool onThemeUpdatedCalled = false;
    NativeComm.onThemeUpdated = () => onThemeUpdatedCalled = true;

    ApiPrefs.setLogin(
      Login((b) => b
        ..uuid = 'uuid'
        ..domain = 'domain'
        ..accessToken = 'token'
        ..user = CanvasModelTestUtils.mockUser().toBuilder()),
    );

    StudentColors.primaryColor = Colors.black;
    StudentColors.accentColor = Colors.black;
    StudentColors.buttonColor = Colors.black;
    StudentColors.primaryTextColor = Colors.black;

    await NativeComm.methodCallHandler(MethodCall(NativeComm.methodReset, null));

    expect(resetRouteCalled, isTrue);
    expect(ApiPrefs.getCurrentLogin(), isNull);
    expect(onThemeUpdatedCalled, isTrue);
    expect(StudentColors.primaryColor, StudentColors.defaultPrimary);
    expect(StudentColors.accentColor, StudentColors.defaultAccent);
    expect(StudentColors.buttonColor, StudentColors.defaultButton);
    expect(StudentColors.primaryTextColor, StudentColors.defaultPrimaryText);
  });

  test('methodRouteToCalendar routes to the calendar', () async {
    String channelId = 'test_channel_id';
    bool routeCalled = false;
    NativeComm.routeToCalendar = (channel) {
      routeCalled = true;
      expect(channel, channelId);
    };
    await NativeComm.methodCallHandler(MethodCall(NativeComm.methodRouteToCalendar, channelId));

    expect(routeCalled, isTrue);
  });

  test('Throws for unknown method', () async {
    bool threw = false;
    try {
      await NativeComm.methodCallHandler(MethodCall('Not a real method name', null));
    } catch (e) {
      threw = true;
    }

    expect(threw, isTrue);
  });
}
