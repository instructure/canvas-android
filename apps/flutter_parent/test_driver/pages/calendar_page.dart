import 'package:flutter_driver/flutter_driver.dart';
import 'package:flutter_parent/models/announcement.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/dataseeding/quiz.dart';

import '../flutter_driver_extensions.dart';

class CalendarPage {
  static Future<void> verifyAnnouncementDisplayed(FlutterDriver driver, Announcement announcement) async {
    var dayOfMonth = announcement.postedAt.day;
    await _presentHelper(driver, dayOfMonth, announcement.title);
  }

  static Future<void> verifyAnnouncementNotDisplayed(FlutterDriver driver, Announcement announcement) async {
    var dayOfMonth = announcement.postedAt.day;
    await _absentHelper(driver, dayOfMonth, announcement.title);
  }

  static Future<void> verifyAssignmentDisplayed(FlutterDriver driver, Assignment assignment) async {
    var dayOfMonth = assignment.dueAt.day;
    await _presentHelper(driver, dayOfMonth, assignment.name);
  }

  static Future<void> verifyAssignmentNotDisplayed(FlutterDriver driver, Assignment assignment) async {
    var dayOfMonth = assignment.dueAt.day;
    await _absentHelper(driver, dayOfMonth, assignment.name);
  }

  static Future<void> verifyQuizDisplayed(FlutterDriver driver, Quiz quiz) async {
    var dayOfMonth = quiz.dueAt.day;
    await _presentHelper(driver, dayOfMonth, quiz.title);
  }

  static Future<void> verifyQuizNotDisplayed(FlutterDriver driver, Quiz quiz) async {
    var dayOfMonth = quiz.dueAt.day;
    await _absentHelper(driver, dayOfMonth, quiz.title);
  }

  static Future<void> _presentHelper(FlutterDriver driver, int dayOfMonth, String searchString) async {
    print('scrolling to day-of-month $dayOfMonth');
    var present = await _scrollToDayOfMonth(driver, dayOfMonth);
    if (!present) print("FAILED to scroll to day-of-month $dayOfMonth");
    print('tapping day-of-month $dayOfMonth');
    await driver.tap(find.byValueKey('day_of_month_$dayOfMonth'));
    await driver.scrollIntoView(find.text(searchString));
    await driver.waitWithRefreshes(find.text(searchString));
  }

  static Future<void> _absentHelper(FlutterDriver driver, int dayOfMonth, String searchString) async {
    await _scrollToDayOfMonth(driver, dayOfMonth);
    await driver.tap(find.byValueKey('day_of_month_$dayOfMonth'));
    await driver.waitForAbsent(find.text(searchString), timeout: Duration(milliseconds: 500));
  }

  // Make an attempt to scroll to the right week if the day of month that we are looking
  // for is not visible.
  static Future<bool> _scrollToDayOfMonth(FlutterDriver driver, int dayOfMonth) async {
    //var day0 = await driver.getText(find.descendant(of: null, matching: null))
    var present = await _dayPresent(driver, dayOfMonth);
    if (present) return true;

    // Not present.  Scroll one way and try again.
    await driver.scroll(find.byType('CalendarWeek'), -400, 0, Duration(milliseconds: 200));
    present = await _dayPresent(driver, dayOfMonth);
    if (present) return true;

    // Still not present.  Try scrolling back the opposite direction (twice, to skip initial page).
    await driver.scroll(find.byType('CalendarWeek'), 400, 0, Duration(milliseconds: 200));
    await driver.scroll(find.byType('CalendarWeek'), 400, 0, Duration(milliseconds: 200));
    present = await _dayPresent(driver, dayOfMonth);

    // If this doesn't fix us, we'll just be hosed.
    return present;
  }

  static Future<bool> _dayPresent(FlutterDriver driver, int dayOfMonth) async {
    try {
      await driver.waitFor(find.byValueKey('day_of_month_$dayOfMonth'), timeout: Duration(milliseconds: 1000));
      return true;
    } catch (e) {
      return false;
    }
  }

  static Future<void> toggleFilter(FlutterDriver driver, Course course) async {
    await driver.tap(find.text("Calendars"));
    await driver.tap(find.text(course.name));
    await driver.tap(find.pageBack());
  }
}
