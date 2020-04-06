// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a en_CA locale. All the
// messages from the main program should be duplicated here with the same
// function name.

// Ignore issues from commonly used lints in this file.
// ignore_for_file:unnecessary_brace_in_string_interps, unnecessary_new
// ignore_for_file:prefer_single_quotes,comment_references, directives_ordering
// ignore_for_file:annotate_overrides,prefer_generic_function_type_aliases
// ignore_for_file:unused_import, file_names

import 'package:intl/intl.dart';
import 'package:intl/message_lookup_by_library.dart';

final messages = new MessageLookup();

typedef String MessageIfAbsent(String messageStr, List<dynamic> args);

class MessageLookup extends MessageLookupByLibrary {
  String get localeName => 'en_CA';

  static m0(version) => "v. ${version}";

  static m1(threshold) => "Assignment Grade Above ${threshold}";

  static m2(threshold) => "Assignment Grade Below ${threshold}";

  static m3(moduleName) => "This assignment is locked by the module \"${moduleName}\".";

  static m4(studentName, assignmentName) => "Regarding: ${studentName}, Assignment - ${assignmentName}";

  static m5(points) => "${points} pts";

  static m6(points) => "${points} points";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} to 1 other', other: '${authorName} to ${howMany} others')}";

  static m8(authorName, recipientName) => "${authorName} to ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} to ${recipientName} & 1 other', other: '${authorName} to ${recipientName} & ${howMany} others')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "${score} out of ${pointsPossible} points";

  static m12(studentShortName) => "for ${studentShortName}";

  static m13(threshold) => "Course Grade Above ${threshold}";

  static m14(threshold) => "Course Grade Below ${threshold}";

  static m15(date, time) => "${date} at ${time}";

  static m16(canvasGuides, canvasSupport) => "Try searching for the name of the school or district you’re attempting to access, like “Smith Private School” or “Smith County Schools.” You can also enter a Canvas domain directly, like “smith.instructure.com.”\n\nFor more information on finding your institution’s Canvas account, you can visit the ${canvasGuides}, reach out to ${canvasSupport}, or contact your school for assistance.";

  static m17(date, time) => "Due ${date} at ${time}";

  static m18(studentName, eventTitle) => "Regarding: ${studentName}, Event - ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} - ${endAt}";

  static m20(grade) => "Final Grade: ${grade}";

  static m21(studentName) => "Regarding: ${studentName}, Front Page";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "Regarding: ${studentName}, Grades";

  static m24(pointsLost) => "Late penalty (-${pointsLost})";

  static m25(studentName, linkUrl) => "Regarding: ${studentName}, ${linkUrl}";

  static m26(percentage) => "Must be above ${percentage}";

  static m27(percentage) => "Must be below ${percentage}";

  static m28(month) => "Next month: ${month}";

  static m29(date) => "Next week starting ${date}";

  static m30(query) => "Unable to find schools matching \"${query}\"";

  static m31(points, howMany) => "${Intl.plural(howMany, one: 'Out of 1 point', other: 'Out of ${points} points')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points} points possible";

  static m34(month) => "Previous month: ${month}";

  static m35(date) => "Previous week starting ${date}";

  static m36(month) => "Month of ${month}";

  static m37(date, time) => "This assignment was submitted on ${date} at ${time} and is waiting to be graded";

  static m38(studentName) => "Regarding: ${studentName}, Syllabus";

  static m39(count) => "${count} unread";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("A description is required."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("A subject is required."),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Add Student"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Add attachment"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Add new student"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Add student with…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Alert Settings"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Alert me when…"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("All Grading Periods"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("An email address is required."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("An error occurred when trying to display this link"),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("An unexpected error occurred"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android OS version"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Appearance"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Application version"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Are you a student or teacher?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Are you sure you want to log out?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Are you sure you wish to close this page? Your unsent message will be lost."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Assignment Details"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Assignment grade above"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Assignment grade below"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Assignment missing"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendars"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Cancel"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas Student"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas on GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Choose a course to message"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Choose from Gallery"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Complete"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Contact Support"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Course Announcement"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Course Announcements"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Course grade above"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Course grade below"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Dark Mode"),
    "Date" : MessageLookupByLibrary.simpleMessage("Date"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Delete"),
    "Description" : MessageLookupByLibrary.simpleMessage("Description"),
    "Device" : MessageLookupByLibrary.simpleMessage("Device"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Device model"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domain:"),
    "Done" : MessageLookupByLibrary.simpleMessage("Done"),
    "Download" : MessageLookupByLibrary.simpleMessage("Download"),
    "Due" : MessageLookupByLibrary.simpleMessage("Due"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EXTREME CRITICAL EMERGENCY!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Email Address"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Email:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired"),
    "Event" : MessageLookupByLibrary.simpleMessage("Event"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Excused"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Failed. Tap for options."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filter"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filter by"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Front Page"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Full error message"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Grade"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Grade percentage"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Graded"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Grades"),
    "Help" : MessageLookupByLibrary.simpleMessage("Help"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("High Contrast Mode"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("How is this affecting you?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("I can\'t get things done until I hear back from you."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("I need some help but it\'s not urgent."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("I\'m having trouble logging in"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idea for Canvas Parent App [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Inbox"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Inbox Zero"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incomplete"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Institution Announcement"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Institution Announcements"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instructions"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("It looks like a great day to rest, relax, and recharge."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("It looks like assignments haven\'t been created in this space yet."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Just a casual question, comment, idea, suggestion…"),
    "Late" : MessageLookupByLibrary.simpleMessage("Late"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Launch External Tool"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Legal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Light Mode"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Link Error"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Locale:"),
    "Location" : MessageLookupByLibrary.simpleMessage("Location"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Locked"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Log Out"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("Manage Students"),
    "Message" : MessageLookupByLibrary.simpleMessage("Message"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("Message subject"),
    "Missing" : MessageLookupByLibrary.simpleMessage("Missing"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("Must be below 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("Network error"),
    "Never" : MessageLookupByLibrary.simpleMessage("Never"),
    "New message" : MessageLookupByLibrary.simpleMessage("New message"),
    "No" : MessageLookupByLibrary.simpleMessage("No"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("No Alerts"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("No Assignments"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("No Courses"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("No Due Date"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("No Events Today!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("No Grade"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("No Location Specified"),
    "No Students" : MessageLookupByLibrary.simpleMessage("No Students"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("No Subject"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("No Summary"),
    "No description" : MessageLookupByLibrary.simpleMessage("No description"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("No recipients selected"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Not Graded"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Not Submitted"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Not a parent?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Notifications for reminders about assignments and calendar events"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("OS Version"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observer"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("One of our other apps might be a better fit. Tap one to visit the Play Store."),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Open In Browser"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Open with another app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Pairing Code"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Preparing…"),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Previous Logins"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Privacy Policy"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Privacy policy, terms of use, open source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR Code"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Recipients"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Remind Me"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Reminders"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Reply"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Reply All"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Report A Problem"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Request Login Help"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Request Login Help Button"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Restart app"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Retry"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Return to Login"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("STUDENT"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Select recipients"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Send a message about this assignment"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Send a message about this course"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Send message"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Set a date and time to be notified of this event."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Set a date and time to be notified of this specific assignment."),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("Set reminder switch"),
    "Settings" : MessageLookupByLibrary.simpleMessage("Settings"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Share Your Love for the App"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Something\'s broken but I can work around it to get what I need done."),
    "Student" : MessageLookupByLibrary.simpleMessage("Student"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Subject"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Submitted"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Successfully submitted!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Summary"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Switch Users"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Syllabus"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("TEACHER"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("Tap to favorite the courses you want to see on the Calendar."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tap to pair with a new student"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tap to select this student"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tap to show student selector"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Teacher"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Tell us about your favorite parts of the app"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Terms of Use"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("The following information will help us better understand your idea:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("The server you entered is not authorized for this app."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("The user agent for this app is not authorized."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Theme"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("There are no installed applications that can open this file"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("There is no page information available."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("There was a problem loading the Terms of Use"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("There was an error loading recipients for this course"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("There was an error loading the summary details for this course."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("There was an error loading this announcement"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("There was an error loading this conversation"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("There was an error loading this file"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("There was an error loading your inbox messages."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("There was an error loading your student\'s alerts."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("There was an error loading your student\'s calendar"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("There was an error loading your students."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("There was an error loading your your student’s courses."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("There’s nothing to be notified of yet."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("This app is not authorized for use."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("This course does not have any assignments or calendar events yet."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("This file is unsupported and can’t be viewed through the app"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Total Grade"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Uh oh!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Unable to fetch courses. Please check your connection and try again."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Unable to load this image"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Unable to play this media file"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Unable to send message. Check your connection and try again."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Under Construction"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Unknown User"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Unsaved changes"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Unsupported File"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Upload File"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Use Camera"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("User ID:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Version Number"),
    "View error details" : MessageLookupByLibrary.simpleMessage("View error details"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("We are currently building this feature for your viewing pleasure."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("We are unable to display this link, it may belong to an institution you currently aren\'t logged in to."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("We couldn\'t find any students associated with this account"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("We were unable to verify the server for use with this app."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening."),
    "Yes" : MessageLookupByLibrary.simpleMessage("Yes"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("You are not observing any students."),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("You will be notified about this assignment on…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("You will be notified about this event on…"),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Your code is incorrect or expired."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Your student’s courses might not be published yet."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("You’re all caught up!"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Alerts"),
    "appVersion" : m0,
    "assignmentGradeAboveThreshold" : m1,
    "assignmentGradeBelowThreshold" : m2,
    "assignmentLockedModule" : m3,
    "assignmentSubjectMessage" : m4,
    "assignmentTotalPoints" : m5,
    "assignmentTotalPointsAccessible" : m6,
    "authorToNOthers" : m7,
    "authorToRecipient" : m8,
    "authorToRecipientAndNOthers" : m9,
    "badgeNumberPlus" : m10,
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Calendar"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas Guides"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas logo"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas Support"),
    "collapse" : MessageLookupByLibrary.simpleMessage("collapse"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("collapsed"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Courses"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("dismiss"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("How do I find my school or district?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Enter school name or district…"),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("expand"),
    "expanded" : MessageLookupByLibrary.simpleMessage("expanded"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Find School"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("me"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("Next"),
    "nextMonth" : m28,
    "nextWeek" : m29,
    "noDomainResults" : m30,
    "ok" : MessageLookupByLibrary.simpleMessage("OK"),
    "outOfPoints" : m31,
    "plusRecipientCount" : m32,
    "pointsPossible" : m33,
    "previousMonth" : m34,
    "previousWeek" : m35,
    "selectedMonthLabel" : m36,
    "send" : MessageLookupByLibrary.simpleMessage("send"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("unread"),
    "unreadCount" : m39
  };
}
