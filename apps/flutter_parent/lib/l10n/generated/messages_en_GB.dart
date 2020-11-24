// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a en_GB locale. All the
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
  String get localeName => 'en_GB';

  static m0(userName) => "You are acting as ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "Assignment Grade Above ${threshold}";

  static m3(threshold) => "Assignment Grade Below ${threshold}";

  static m4(moduleName) => "This assignment is locked by the module \"${moduleName}\".";

  static m5(studentName, assignmentName) => "Regarding: ${studentName}, Assignment - ${assignmentName}";

  static m6(points) => "${points} pts";

  static m7(points) => "${points} points";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} to 1 other', other: '${authorName} to ${howMany} others')}";

  static m9(authorName, recipientName) => "${authorName} to ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} to ${recipientName} & 1 other', other: '${authorName} to ${recipientName} & ${howMany} others')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "Change colour for ${studentName}";

  static m13(score, pointsPossible) => "${score} out of ${pointsPossible} points";

  static m14(studentShortName) => "for ${studentShortName}";

  static m15(threshold) => "Course Grade Above ${threshold}";

  static m16(threshold) => "Course Grade Below ${threshold}";

  static m17(date, time) => "${date} at ${time}";

  static m18(alertTitle) => "Dismiss ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "Try searching for the name of the school or district you’re attempting to access, like “Smith Private School” or “Smith County Schools.” You can also enter a Canvas domain directly, like “smith.instructure.com.”\n\nFor more information on finding your institution’s Canvas account, you can visit the ${canvasGuides}, reach out to ${canvasSupport}, or contact your school for assistance.";

  static m20(date, time) => "Due ${date} at ${time}";

  static m21(userName) => "You will stop acting as ${userName} and will be logged out.";

  static m22(userName) => "You will stop acting as ${userName} and return to your original account.";

  static m23(studentName, eventTitle) => "Regarding: ${studentName}, Event - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "Final grade: ${grade}";

  static m26(studentName) => "Regarding: ${studentName}, Front Page";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Regarding: ${studentName}, Grades";

  static m29(pointsLost) => "Late penalty (-${pointsLost})";

  static m30(studentName, linkUrl) => "Regarding: ${studentName}, ${linkUrl}";

  static m31(percentage) => "Must be above ${percentage}";

  static m32(percentage) => "Must be below ${percentage}";

  static m33(month) => "Next month: ${month}";

  static m34(date) => "Next week starting ${date}";

  static m35(query) => "Unable to find schools matching \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'Out of 1 point', other: 'Out of ${points} points')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} points possible";

  static m39(month) => "Previous month: ${month}";

  static m40(date) => "Previous week starting ${date}";

  static m41(termsOfService, privacyPolicy) => "By tapping \'Create Account\', you agree to the ${termsOfService} and acknowledge the ${privacyPolicy}";

  static m42(version) => "Suggestions for Android - Canvas Parent ${version}";

  static m43(month) => "Month of ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} star', other: '${position} stars')}";

  static m45(date, time) => "This assignment was submitted on ${date} at ${time} and is waiting to be graded";

  static m46(studentName) => "Regarding: ${studentName}, Syllabus";

  static m47(count) => "${count} unread";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("A description is required."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("A network error occurred when adding this student. Check your connection and try again."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("A subject is required."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("Act As User"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("Add student"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("Add attachment"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("Add new student"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("Add student with…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("Alert Settings"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("Alert me when…"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("All Grading Periods"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("Already have an account? "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("An email address is required."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("An error occurred when trying to display this link"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("An error occurred while saving your selection. Please try again."),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("An unexpected error occurred"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android OS version"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("Appearance"),
    "Application version" : MessageLookupByLibrary.simpleMessage("Application version"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("Are you a student or teacher?"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("Are you sure you want to log out?"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("Are you sure you wish to close this page? Your unsent message will be lost."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("Assignment details"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("Assignment mark above"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("Assignment mark below"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("Assignment missing"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("Barney, Fuchsia"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("Calendars"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("Camera Permission"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("Cancel"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas student"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas on GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("Choose a course to message"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("Choose from Gallery"),
    "Complete" : MessageLookupByLibrary.simpleMessage("Complete"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("Contact support"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("Course announcement"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("Course announcements"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("Course mark above"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("Course mark below"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("Create account"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("Dark mode"),
    "Date" : MessageLookupByLibrary.simpleMessage("Date"),
    "Delete" : MessageLookupByLibrary.simpleMessage("Delete"),
    "Description" : MessageLookupByLibrary.simpleMessage("Description"),
    "Device" : MessageLookupByLibrary.simpleMessage("Device"),
    "Device model" : MessageLookupByLibrary.simpleMessage("Device model"),
    "Domain" : MessageLookupByLibrary.simpleMessage("Domain"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("Domain:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("Don\'t show again"),
    "Done" : MessageLookupByLibrary.simpleMessage("Done"),
    "Download" : MessageLookupByLibrary.simpleMessage("Download"),
    "Due" : MessageLookupByLibrary.simpleMessage("Due"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("EXTREME CRITICAL EMERGENCY!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("Electric, blue"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("Email address"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Email"),
    "Email…" : MessageLookupByLibrary.simpleMessage("Email…"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired"),
    "Event" : MessageLookupByLibrary.simpleMessage("Event"),
    "Excused" : MessageLookupByLibrary.simpleMessage("Excused"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("Expired QR Code"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("Failed. Tap for options."),
    "Filter" : MessageLookupByLibrary.simpleMessage("Filter"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("Filter by"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("Fire, Orange"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("Front page"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("Full name"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("Full Name…"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("Full error message"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("Go to today"),
    "Grade" : MessageLookupByLibrary.simpleMessage("Grade"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("Grade percentage"),
    "Graded" : MessageLookupByLibrary.simpleMessage("Graded"),
    "Grades" : MessageLookupByLibrary.simpleMessage("Grades"),
    "Help" : MessageLookupByLibrary.simpleMessage("Help"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("Hide Password"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("High Contrast Mode"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("How are we doing?"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("How is this affecting you?"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("I can\'t finish what I need to do until I hear back from you."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("I don\'t have a Canvas account"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("I have a Canvas account"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("I need some help but it\'s not urgent."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("I\'m having trouble logging in"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Idea for Canvas Parent App [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("Inbox"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("Inbox zero"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("Incomplete"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("Incorrect Domain"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("Institution announcement"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("Institution announcements"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("Instructions"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("Interactions on this page are limited by your institution."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("Invalid QR Code"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("It looks like a great day to rest, relax, and recharge."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("It looks like assignments haven\'t been created in this space yet."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("Just a casual question, comment, idea, suggestion…"),
    "Late" : MessageLookupByLibrary.simpleMessage("Late"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("Launch external tool"),
    "Legal" : MessageLookupByLibrary.simpleMessage("Legal"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("Light Mode"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("Link Error"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("Locale:"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("Locate QR Code"),
    "Location" : MessageLookupByLibrary.simpleMessage("Location"),
    "Locked" : MessageLookupByLibrary.simpleMessage("Locked"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("Log out"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("Login flow: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("Login flow: Normal"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("Login flow: Site admin"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("Login flow: Skip mobile verify"),
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
    "No Assignments" : MessageLookupByLibrary.simpleMessage("No assignments"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("No courses"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("No due date"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("No events today!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("No mark"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("No location specified"),
    "No Students" : MessageLookupByLibrary.simpleMessage("No Students"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("No Subject"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("No Summary"),
    "No description" : MessageLookupByLibrary.simpleMessage("No description"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("No recipients selected"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("Not graded"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("Not submitted"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("Not a parent?"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("Notifications for reminders about assignments and calendar events"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("OS version"),
    "Observer" : MessageLookupByLibrary.simpleMessage("Observer"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("One of our other apps may be a better fit. Tap one to visit the Play Store."),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Open Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("Open In browser"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("Open with another app"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("Pairing Code"),
    "Password" : MessageLookupByLibrary.simpleMessage("Password"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("Password is required"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("Password must contain at least 8 characters"),
    "Password…" : MessageLookupByLibrary.simpleMessage("Password…"),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("Planner note"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("Please enter a valid email address"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("Please enter an email address"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("Please enter full name"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Please scan a QR code generated by Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("Plum, Purple"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("Preparing…"),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("Previous logins"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("Privacy Policy"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("Privacy Policy Link"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("Privacy policy, terms of use, open source"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR Code"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("QR scanning requires camera access"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("Raspberry, Red"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("Recipients"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("Refresh"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("Remind Me"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("Reminders"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("Reminders have changed!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("Reply"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("Reply all"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("Report a problem"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("Request Login Help"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("Request Login Help Button"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("Restart app"),
    "Retry" : MessageLookupByLibrary.simpleMessage("Retry"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("Return to login"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("STUDENT"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("Screenshot showing location of QR code generation in browser"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Screenshot showing location of pairing QR code generation in the Canvas Student app"),
    "Select" : MessageLookupByLibrary.simpleMessage("Select"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("Select Student Colour"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("Select recipients"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("Send feedback"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("Send a message about this assignment"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("Send a message about this course"),
    "Send message" : MessageLookupByLibrary.simpleMessage("Send message"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("Set a date and time to be notified of this event."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("Set a date and time to be notified of this specific assignment."),
    "Settings" : MessageLookupByLibrary.simpleMessage("Settings"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("Shamrock, Green"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("Share your love for the app"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("Show Password"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("Sign in"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("Something\'s broken, but I can work around it to finish what I need to do."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("Stop acting as user"),
    "Student" : MessageLookupByLibrary.simpleMessage("Student"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("Student Pairing"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("Students can create a QR code using the Canvas Student app on their mobile device"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("Students can obtain a pairing code through the Canvas website"),
    "Subject" : MessageLookupByLibrary.simpleMessage("Subject"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("Submitted"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("Successfully submitted!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("Summary"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("Switch users"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("Syllabus"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("TEACHER"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("Tap to favourite the courses you want to see on the Calendar. Select up to 10."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("Tap to pair with a new student"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("Tap to select this student"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("Tap to show student selector"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("Teacher"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("Tell us about your favourite parts of the app"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("Terms of Service"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("Terms of Service Link"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("Terms of Use"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("The QR code you scanned may have expired. Refresh the code on the student\'s device and try again."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("The following information will help us better understand your idea:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("The server you entered is not authorised for this app."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("The user agent for this app is not authorised."),
    "Theme" : MessageLookupByLibrary.simpleMessage("Theme"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("There are no installed applications that can open this file"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("There is no page information available."),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("There was a problem loading the Terms of Use"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("There was a problem removing this student from your account. Please check your connection and try again."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("There was an error loading recipients for this course"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("There was an error loading the summary details for this course."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("There was an error loading this announcement"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("There was an error loading this conversation"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("There was an error loading this file"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("There was an error loading your inbox messages."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("There was an error loading your student\'s alerts."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("There was an error loading your student\'s calendar"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("There was an error loading your students."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("There was an error loading your student’s courses."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("There was an error logging in. Please generate another QR Code and try again."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("There was an error trying to act as this user. Please check the Domain and User ID and try again."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("There’s nothing to be notified of yet."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("This app is not authorised for use."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("This course does not have any assignments or calendar events yet."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("This file is unsupported and can’t be viewed through the app"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("This will unpair and remove all enrolments for this student from your account."),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("Total mark"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("Uh oh!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("Unable to fetch courses. Please check your connection and try again."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("Unable to load this image"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("Unable to play this media file"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("Unable to send message. Check your connection and try again."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("Under Construction"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("Unknown user"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("Unsaved changes"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("Unsupported File"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("Upload file"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("Use Camera"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("Use Dark Theme in Web Content"),
    "User ID" : MessageLookupByLibrary.simpleMessage("User ID"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("User ID"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("Version number"),
    "View Description" : MessageLookupByLibrary.simpleMessage("View Description"),
    "View error details" : MessageLookupByLibrary.simpleMessage("View error details"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("View the Privacy Policy"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("We are currently building this feature for your viewing pleasure."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("We are unable to display this link, it may belong to an institution you currently aren\'t logged in to."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("We couldn\'t find any students associated with this account"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("We were unable to verify the server for use with this app."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("What can we do better?"),
    "Yes" : MessageLookupByLibrary.simpleMessage("Yes"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("You are not observing any students."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("You may only choose 10 calendars to display"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("You must enter a user id"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("You must enter a valid domain"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("You must select at least one calendar to display"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("You will be notified about this assignment on…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("You will be notified about this event on…"),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("Your code is incorrect or expired."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("Your student’s courses might not be published yet."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("You’re all caught up!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("Alerts"),
    "appVersion" : m1,
    "assignmentGradeAboveThreshold" : m2,
    "assignmentGradeBelowThreshold" : m3,
    "assignmentLockedModule" : m4,
    "assignmentSubjectMessage" : m5,
    "assignmentTotalPoints" : m6,
    "assignmentTotalPointsAccessible" : m7,
    "authorToNOthers" : m8,
    "authorToRecipient" : m9,
    "authorToRecipientAndNOthers" : m10,
    "badgeNumberPlus" : m11,
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("Calendar"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas Guides"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas logo"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas Support"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("collapse"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("collapsed"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("Courses"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("How do I find my school or district?"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("Enter school name or district…"),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("expand"),
    "expanded" : MessageLookupByLibrary.simpleMessage("expanded"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("Find School"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("me"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("minus"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("Next"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("OK"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("send"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("unread"),
    "unreadCount" : m47
  };
}
