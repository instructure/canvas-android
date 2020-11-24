// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a zh_HK locale. All the
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
  String get localeName => 'zh_HK';

  static m0(userName) => "您正在作為 ${userName}";

  static m1(version) => "v. ${version}";

  static m2(threshold) => "作業評分高於 ${threshold}";

  static m3(threshold) => "作業評分低於 ${threshold}";

  static m4(moduleName) => "此作業由單元 \"${moduleName}\" 鎖定。";

  static m5(studentName, assignmentName) => "關於：${studentName}，作業 - ${assignmentName}";

  static m6(points) => "${points} 分";

  static m7(points) => "${points} 分";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} 到 1 個其他', other: '${authorName} 到 ${howMany} 其他')}";

  static m9(authorName, recipientName) => "${authorName} 至 ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} 到 ${recipientName} 和 1 個其他', other: '${authorName} 到 ${recipientName} 和 ${howMany} 其他')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "變更 ${studentName} 的顏色";

  static m13(score, pointsPossible) => "得分為 ${score}，滿分為 ${pointsPossible}";

  static m14(studentShortName) => "針對 ${studentShortName}";

  static m15(threshold) => "課程評分高於 ${threshold}";

  static m16(threshold) => "課程評分低於 ${threshold}";

  static m17(date, time) => "日期 ${date}，時間 ${time}";

  static m18(alertTitle) => "忽略 ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "嘗試搜尋您試著存取的學校或地區的名稱，例如“Smith Private School”或“Smith County Schools”。您也可以直接輸入 Canvas 網域，例如“smith.instructure.com”。\n\n如需更多有關尋找您的機構的 Canvas 帳戶的資訊，您可以瀏覽 ${canvasGuides}、連線到 ${canvasSupport} 或聯絡您的學校尋求協助。";

  static m20(date, time) => "截止於 ${date} 的 ${time}";

  static m21(userName) => "您將會停止作為 ${userName} 並登出。";

  static m22(userName) => "您將會停止作為 ${userName} 並返回到您的原有帳戶。";

  static m23(studentName, eventTitle) => "關於：${studentName}，活動 - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "最終評分：${grade}";

  static m26(studentName) => "關於：${studentName}，封面頁";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "關於：${studentName}，成績";

  static m29(pointsLost) => "逾期懲罰 (-${pointsLost})";

  static m30(studentName, linkUrl) => "關於：${studentName}，${linkUrl}";

  static m31(percentage) => "必須高於 ${percentage}";

  static m32(percentage) => "必須低於 ${percentage}";

  static m33(month) => "下個月：${month}";

  static m34(date) => "下一週開始 ${date}";

  static m35(query) => "找不到符合 \"${query}\" 的學校";

  static m36(points, howMany) => "${Intl.plural(howMany, one: '滿分為 1 分', other: '滿分為 ${points} 分')}";

  static m37(count) => "+${count}";

  static m38(points) => "可能的分數 ${points}";

  static m39(month) => "上個月：${month}";

  static m40(date) => "上一週開始 ${date}";

  static m41(termsOfService, privacyPolicy) => "點擊「創建帳戶」，代表您同意${termsOfService}，並了解${privacyPolicy}";

  static m42(version) => "給 Android 的建議 - Canvas Parent ${version}";

  static m43(month) => "${month} 的月份";

  static m44(position) => "${Intl.plural(position, one: '${position} 顆星', other: '${position}顆星')}";

  static m45(date, time) => "此作業在 ${date} 的 ${time} 上已提交，並且正在等待評分";

  static m46(studentName) => "關於：${studentName}，課程大綱";

  static m47(count) => "${count} 則未讀";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("「作為」本質上是以此使用者身份登錄，沒有密碼。您將能夠進行任何操作，就好像您就是此使用者一樣，並且，其他使用者也會認為是此使用者執行了操作。然而，活動紀錄會記錄事實，即您是代表此使用者執行操作的人。"),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("描述必填。"),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("添加此學生時發生網路錯誤。請檢查您的連線，然後再試一次。"),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("主題必填。"),
    "Act As User" : MessageLookupByLibrary.simpleMessage("作為使用者"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("添加學生"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("添加附件"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("添加新學生"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("添加學生…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("提醒設定"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("在下列情形時提醒我..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("所有評分期"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("已經擁有帳戶？ "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("電郵地址必填。"),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("嘗試顯示此連結時出現錯誤"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("儲存您的選擇時發生錯誤。請重試。"),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("出現意外錯誤"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android 作業系統版本"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("外觀"),
    "Application version" : MessageLookupByLibrary.simpleMessage("應用程式版本"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("您是學生或教師？"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("是否確定登出？"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("是否確定要關閉此頁面？將遺失未傳送的訊息。"),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("作業詳細資料"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("作業評分高於"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("作業評分低於"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("缺少作業"),
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("巴尼，紫紅色"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("行事曆"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("攝影機權限"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("取消"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas Student"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("GitHub 上的 Canvas"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("選擇課程以傳送訊息"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("從圖片庫中選擇"),
    "Complete" : MessageLookupByLibrary.simpleMessage("完成"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("聯絡支援"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("課程通告"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("課程通告"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("課程評分高於"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("課程評分低於"),
    "Create Account" : MessageLookupByLibrary.simpleMessage("建立帳戶"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("暗色模式"),
    "Date" : MessageLookupByLibrary.simpleMessage("日期"),
    "Delete" : MessageLookupByLibrary.simpleMessage("刪除"),
    "Description" : MessageLookupByLibrary.simpleMessage("描述"),
    "Device" : MessageLookupByLibrary.simpleMessage("裝置"),
    "Device model" : MessageLookupByLibrary.simpleMessage("裝置機型"),
    "Domain" : MessageLookupByLibrary.simpleMessage("網域"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("網域："),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("不要再次顯示"),
    "Done" : MessageLookupByLibrary.simpleMessage("已完成"),
    "Download" : MessageLookupByLibrary.simpleMessage("下載"),
    "Due" : MessageLookupByLibrary.simpleMessage("截止"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("極其重要的緊急情況！！"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("電，藍色"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("電郵地址"),
    "Email:" : MessageLookupByLibrary.simpleMessage("電郵地址："),
    "Email…" : MessageLookupByLibrary.simpleMessage("電郵..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("輸入已提供給您的學生配對代碼。如果配對代碼無效，可能是已到期"),
    "Event" : MessageLookupByLibrary.simpleMessage("活動"),
    "Excused" : MessageLookupByLibrary.simpleMessage("已免除"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("已過期 QR 碼"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("失敗。點選選項。"),
    "Filter" : MessageLookupByLibrary.simpleMessage("篩選器"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("篩選條件"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("火，橙色"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("首頁"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("全名"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("全名..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("完整錯誤訊息"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("前往今天"),
    "Grade" : MessageLookupByLibrary.simpleMessage("評分"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("評分百分比"),
    "Graded" : MessageLookupByLibrary.simpleMessage("已評分"),
    "Grades" : MessageLookupByLibrary.simpleMessage("成績"),
    "Help" : MessageLookupByLibrary.simpleMessage("支援"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("隱藏密碼"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("高對比模式"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("我們表現如何？"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("問題對您造成什麼影響？"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("在收到您的回覆之前，我無法處理。"),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("我沒有 Canvas 帳戶"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("我有 Canvas 帳戶"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("我需要支援，但並不緊迫。"),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("我登入時發生問題"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("關於 Canvas Parent 程式 [Android] 的想法"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("為向您提供更佳體驗，我們更新了提醒功能的運作方式。您可以在檢視作業或行事曆活動時，點擊「提醒我」部分下方的切換按鈕，以新增提醒。\n\n請注意，任何透過較舊版本應用程式創建的提醒將不能與新功能相容，您需要再次創建提醒。"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("收件匣"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("收件匣無訊息"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("未完成"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("錯誤網域"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("機構通告"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("機構的告"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("教學"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("本頁面的互動受您所在機構的限制。"),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("無效的 QR 碼"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("看來是適合休息、放鬆和充電的一天。"),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("此空間中尚未建立任何作業。"),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("只是隨意提問、評論、想法、建議……"),
    "Late" : MessageLookupByLibrary.simpleMessage("逾期"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("啟動外部工具"),
    "Legal" : MessageLookupByLibrary.simpleMessage("法律事務"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("淡色模式"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("連結錯誤"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("位置："),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("尋找 QR 碼"),
    "Location" : MessageLookupByLibrary.simpleMessage("位置"),
    "Locked" : MessageLookupByLibrary.simpleMessage("已鎖定"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("登出"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("登入流態：Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("登入流態：正常"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("登入流態：網站管理員"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("登入流態：跳過流動認證"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("管理學生"),
    "Message" : MessageLookupByLibrary.simpleMessage("訊息"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("訊息主題"),
    "Missing" : MessageLookupByLibrary.simpleMessage("缺失"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("必須低於 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("網路錯誤"),
    "Never" : MessageLookupByLibrary.simpleMessage("永不"),
    "New message" : MessageLookupByLibrary.simpleMessage("新訊息"),
    "No" : MessageLookupByLibrary.simpleMessage("否"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("無提醒"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("無作業"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("無課程"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("無截止日期"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("今天並無活動！"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("無評分"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("無指定的地點"),
    "No Students" : MessageLookupByLibrary.simpleMessage("無學生"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("無主題"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("無摘要"),
    "No description" : MessageLookupByLibrary.simpleMessage("沒有說明"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("未選擇收件人"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("未評分"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("未提交"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("不是父母？"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("與作業和行事曆活動有關的提醒通知"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("OS 版本"),
    "Observer" : MessageLookupByLibrary.simpleMessage("觀察者"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("我們其他應用程式可能更適合您。按一查看 Play Store。"),
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("開啟 Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("在瀏覽器中打開"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("使用其他應用程式開啟"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("配對代碼"),
    "Password" : MessageLookupByLibrary.simpleMessage("密碼"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("需要輸入密碼"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("密碼必須最少包含 8 個字符"),
    "Password…" : MessageLookupByLibrary.simpleMessage("密碼..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("規劃注釋"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("請輸入有效電郵地址"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("請輸入電郵地址"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("請輸入全名"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("請掃描由 Canvas 產生的 QR 碼"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("梅子，紫色"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("準備中…"),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("先前登入"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("隱私政策"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("隱私政策連結"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("隱私政策，使用條款，開放源碼"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR 碼"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("QR 碼掃描需要攝影機存取權限"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("樹莓，紅色"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("收件人"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("重新整理"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("提醒我"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("提醒"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("提醒功能已改變！"),
    "Reply" : MessageLookupByLibrary.simpleMessage("回覆"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("全部回覆"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("報告問題"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("請求登入支援"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("請求登入支援按鈕"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("重新啟動應用程式"),
    "Retry" : MessageLookupByLibrary.simpleMessage("重試"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("返回登入頁面"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("學生"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("瀏覽器內的螢幕截圖顯示產生 QR 碼的位置"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("螢幕截圖顯示 Canvas Student 應用程式內產生配對 QR 碼的位置"),
    "Select" : MessageLookupByLibrary.simpleMessage("選擇"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("選擇學生顏色"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("選擇收件人"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("發送回饋"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("傳送與此作業有關的訊息"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("傳送與此學生有關的訊息"),
    "Send message" : MessageLookupByLibrary.simpleMessage("傳送訊息"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("設定通知此活動的日期和時間。"),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("設定通知此指定作業的日期和時間。"),
    "Settings" : MessageLookupByLibrary.simpleMessage("設定"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("三葉草，綠色"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("分享您對於本應用程式的喜愛"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("顯示密碼"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("登入"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("有些地方出現了問題，但我能繞過問題實現我的目的。"),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("停止作為使用者"),
    "Student" : MessageLookupByLibrary.simpleMessage("學生"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("學生配對"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("學生可以使用流動裝置上的 Canvas Student 應用程式創建 QR 碼"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("學生可以透過 Canvas 網站獲取配對碼"),
    "Subject" : MessageLookupByLibrary.simpleMessage("主題"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("已提交"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("成功提交！"),
    "Summary" : MessageLookupByLibrary.simpleMessage("摘要"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("切換使用者"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("課程大綱"),
    "TA" : MessageLookupByLibrary.simpleMessage("助教"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("教師"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("點選以選擇您要在行事曆上看到的最愛課程。選擇最多 10 個。"),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("點選以和新學生配對"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("點選以選擇此學生"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("點選以顯示學生選擇器"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("教師"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("告訴我們本應用程式最讓您滿意之處"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("服務條款"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("服務條款連結"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("使用條款"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("您所掃描的 QR 碼可能已經過期。請在學生的裝置上重新載入 QR 碼然後重試。"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("下列資訊能幫我們好好理解您的想法："),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("您輸入的伺服器未獲得使用該應用程式的授權。"),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("您嘗試添加的學生屬於另一所學校。請登入至該學校或在該學校創建帳戶以掃描此 QR 碼。"),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("該應用程式的使用者代理未獲得授權。"),
    "Theme" : MessageLookupByLibrary.simpleMessage("主題"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("未安裝可開啟此檔案的應用程式"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("沒有可用的頁面資訊。"),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("載入使用條款時出現問題"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("從帳戶中移除此學生時發生問題。請檢查您的連接然後重試。"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("載入此課程的收件人時發生錯誤"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("載入此課程的摘要詳細資料時發生錯誤。"),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("載入此通告時發生錯誤"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("載入此交談時發生錯誤"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("載入此檔案時發生錯誤"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("載入您的收件匣訊息時發生錯誤。"),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("載入您的學生的提醒時發生錯誤。"),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("載入您的學生的行事曆時發生錯誤"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("載入您的學生時發生錯誤。"),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("載入您的學生的課程時發生錯誤。"),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("登入時出現錯誤。請產生另一個 QR 碼然後重試。"),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("嘗試作為此使用者時出現錯誤。請檢查網域及使用者 ID 然後重試。"),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("尚未有任何通知。尚未有任何通知。"),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("該 app 程式未獲得使用權限。"),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("此課程還沒有任何作業列表或行事曆。"),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("不支援此檔案，而且無法透過應用程式檢視"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("此操作將取消配對並從您的帳戶中移除所有此學生的註冊資料。"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("總評級"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("噢！"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("無法取得課程。請檢查您的連接然後重試。"),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("無法載入此影像"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("無法播放此媒體檔案"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("無法傳送訊息。請檢查您的連線，然後再試一次。"),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("建構中"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("未知使用者"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("未儲存的變更"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("不支援的檔案"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("上傳檔案"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("使用攝影機"),
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("在網頁內容使用暗色外觀主題"),
    "User ID" : MessageLookupByLibrary.simpleMessage("使用者 ID"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("使用者 ID："),
    "Version Number" : MessageLookupByLibrary.simpleMessage("版本編號"),
    "View Description" : MessageLookupByLibrary.simpleMessage("查看描述"),
    "View error details" : MessageLookupByLibrary.simpleMessage("檢視錯誤詳細資料"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("檢視隱私政策"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("我們目前正在建立讓您開心檢視的功能。"),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("我們無法顯示此連結，連結可能屬於您現在尚未登入的機構。"),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("我們找不到與此帳戶有關的任何學生"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("無法確認此應用程式使用的伺服器。"),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("不確定發生什麼事，但不是好事。如果持續發生，請聯絡我們。"),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("我們有何可改進之處？"),
    "Yes" : MessageLookupByLibrary.simpleMessage("是"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("您未正在觀察任何學生。"),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("您只能選擇顯示 10 個行事曆"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("您必須輸入使用者 ID"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("您必須輸入有效的網域"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("必須選擇最少顯示一個行事曆"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("將在以下時間通知您關於此作業…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("將在以下時間通知您關於此沽動…"),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("您可以在您的網上帳戶個人檔案找到這個 QR 碼。點擊列表內的「流動登入 QR 碼」。"),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("您需要開啟學生的 Canvas Student 應用程式以繼續操作。前往主選單 > 設定 > 與觀察者配對，然後掃描該處顯示的 QR 碼。"),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("您的代碼錯誤或到期。"),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("您的學生的課程可能尚未發佈。"),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("您已讀完所有新訊息！"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("提醒"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("行事曆"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas 指南"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas 標誌"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas 支援"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("收起"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("收起"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("課程"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("如何找到我的學校？"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("輸入學校名稱或地區…"),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("展開"),
    "expanded" : MessageLookupByLibrary.simpleMessage("展開"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("尋找學校"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("我"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("減"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("下一個"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("確定"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("傳送"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("未讀"),
    "unreadCount" : m47
  };
}
