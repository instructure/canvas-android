import 'package:flutter_driver/flutter_driver.dart';
import 'package:flutter_parent/models/announcement.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/dataseeding/quiz.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:test/test.dart';

import '../flutter_driver_extensions.dart';

class CalendarPage {
  static Future<void> waitForRender(FlutterDriver? driver) async {
    var dayOfMonth = DateTime.now().toLocal().day;
    await driver?.waitFor(find.byValueKey('day_of_month_$dayOfMonth'), timeout: Duration(milliseconds: 9998));
  }

  static Future<void> verifyAnnouncementDisplayed(FlutterDriver? driver, Announcement announcement) async {
    await _presentHelper(driver, announcement.postedAt, announcement.title);
  }

  static Future<void> verifyAnnouncementNotDisplayed(FlutterDriver? driver, Announcement announcement) async {
    await _absentHelper(driver, announcement.postedAt, announcement.title);
  }

  static Future<void> verifyAssignmentDisplayed(FlutterDriver? driver, Assignment assignment) async {
    await _presentHelper(driver, assignment.dueAt!, assignment.name!);
  }

  static Future<void> verifyAssignmentNotDisplayed(FlutterDriver? driver, Assignment assignment) async {
    await _absentHelper(driver, assignment.dueAt!, assignment.name!);
  }

  static Future<void> verifyQuizDisplayed(FlutterDriver? driver, Quiz quiz) async {
    await _presentHelper(driver, quiz.dueAt, quiz.title);
  }

  static Future<void> verifyQuizNotDisplayed(FlutterDriver? driver, Quiz quiz) async {
    await _absentHelper(driver, quiz.dueAt, quiz.title);
  }

  static Future<void> verifyEventDisplayed(FlutterDriver? driver, ScheduleItem event) async {
    await _presentHelper(driver, event.isAllDay ? event.allDayDate! : event.startAt!, event.title!);
  }

  static Future<void> verifyEventNotDisplayed(FlutterDriver? driver, ScheduleItem event) async {
    await _absentHelper(driver, event.isAllDay ? event.allDayDate! : event.startAt!, event.title!);
  }

  static Future<void> toggleFilter(FlutterDriver? driver, Course course) async {
    await driver?.tap(find.text("Calendars"));
    await driver?.tap(find.text(course.name));
    await driver?.tap(find.pageBack());
    await driver?.waitForAbsent(find.byType('LoadingIndicator'), timeout: Duration(milliseconds: 4999));
  }

  static Future<void> openAssignment(FlutterDriver? driver, Assignment assignment) async {
    // This should get the assignment into view
    await _presentHelper(driver, assignment.dueAt!, assignment.name!);
    await driver?.tap(find.text(assignment.name!));
  }

  // Helper function to (1) scroll to the correct week if necessary, (2) select the date, and
  // (3) make sure that the search text is present.
  static Future<void> _presentHelper(FlutterDriver? driver, DateTime displayDate, String searchString) async {
    //print('_presentHelper($displayDate,$searchString)');
    var dayOfMonth = displayDate.toLocal().day;
    var present = await _scrollToDayOfMonth(driver, dayOfMonth);
    expect(present, true, reason: "FAILED to scroll to day-of-month $dayOfMonth");
    await driver?.tap(find.byValueKey('day_of_month_$dayOfMonth'));
    await driver?.scrollIntoView(find.text(searchString));
    await driver?.waitWithRefreshes(find.text(searchString));
  }

  // Helper function to (1) scroll to the correct week if necessary, (2) select the date, and
  // (3) make sure that the search text is NOT present.
  static Future<void> _absentHelper(FlutterDriver? driver, DateTime displayDate, String searchString) async {
    //print('_absentHelper($displayDate,$searchString)');
    var dayOfMonth = displayDate.toLocal().day;
    var present = await _scrollToDayOfMonth(driver, dayOfMonth);
    expect(present, true, reason: "FAILED to scroll to day-of-month $dayOfMonth");
    await driver?.tap(find.byValueKey('day_of_month_$dayOfMonth'));
    await driver?.waitForAbsentWithRefreshes(find.text(searchString));
  }

  // Helper function makes an attempt to scroll to the right week if the day of month that we are looking
  // for is not visible.  Only searches one week forward and one week backwards.
  static Future<bool> _scrollToDayOfMonth(FlutterDriver? driver, int dayOfMonth) async {
    var present = await _dayPresent(driver, dayOfMonth);
    if (present) return true;

    // Not present.  Scroll one way and try again.
    //print("scrolling -400");
    await driver?.scroll(find.byType('CalendarWeek'), -400, 0, Duration(milliseconds: 200));
    present = await _dayPresent(driver, dayOfMonth);
    if (present) return true;

    // Still not present.  Try scrolling back the opposite direction (twice, to skip initial page).
    //print("scrolling 800");
    await driver?.scroll(find.byType('CalendarWeek'), 400, 0, Duration(milliseconds: 200));
    await driver?.scroll(find.byType('CalendarWeek'), 400, 0, Duration(milliseconds: 200));
    present = await _dayPresent(driver, dayOfMonth);

    // If this doesn't fix us, we'll just be hosed.
    return present;
  }

  // Helper method returns true if the specified dayOfMonth is displayed
  static Future<bool> _dayPresent(FlutterDriver? driver, int dayOfMonth) async {
    try {
      await driver?.waitFor(find.byValueKey('day_of_month_$dayOfMonth'), timeout: Duration(milliseconds: 1000));
      return true;
    } catch (e) {
      return false;
    }
  }
}
