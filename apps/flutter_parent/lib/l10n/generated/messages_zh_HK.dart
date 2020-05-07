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

  static m12(score, pointsPossible) => "得分為 ${score}，滿分為 ${pointsPossible}";

  static m13(studentShortName) => "針對 ${studentShortName}";

  static m14(threshold) => "課程評分高於 ${threshold}";

  static m15(threshold) => "課程評分低於 ${threshold}";

  static m16(date, time) => "日期 ${date}，時間 ${time}";

  static m17(canvasGuides, canvasSupport) => "嘗試搜尋您試著存取的學校或地區的名稱，例如“Smith Private School”或“Smith County Schools”。您也可以直接輸入 Canvas 網域，例如“smith.instructure.com”。\n\n如需更多有關尋找您的機構的 Canvas 帳戶的資訊，您可以瀏覽 ${canvasGuides}、連線到 ${canvasSupport} 或聯絡您的學校尋求協助。";

  static m18(date, time) => "截止於 ${date} 的 ${time}";

  static m19(userName) => "您將會停止作為 ${userName} 並登出。";

  static m20(userName) => "您將會停止作為 ${userName} 並返回到您的原有帳戶。";

  static m21(studentName, eventTitle) => "關於：${studentName}，活動 - ${eventTitle}";

  static m22(startAt, endAt) => "${startAt} - ${endAt}";

  static m23(grade) => "最終評分：${grade}";

  static m24(studentName) => "關於：${studentName}，封面頁";

  static m25(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m26(studentName) => "關於：${studentName}，成績";

  static m27(pointsLost) => "逾期懲罰 (-${pointsLost})";

  static m28(studentName, linkUrl) => "關於：${studentName}，${linkUrl}";

  static m29(percentage) => "必須高於 ${percentage}";

  static m30(percentage) => "必須低於 ${percentage}";

  static m31(month) => "下個月：${month}";

  static m32(date) => "下一週開始 ${date}";

  static m33(query) => "找不到符合 \"${query}\" 的學校";

  static m34(points, howMany) => "${Intl.plural(howMany, one: '滿分為 1 分', other: '滿分為 ${points} 分')}";

  static m35(count) => "+${count}";

  static m36(points) => "可能的分數 ${points}";

  static m37(month) => "上個月：${month}";

  static m38(date) => "上一週開始 ${date}";

  static m39(month) => "${month} 的月份";

  static m40(date, time) => "此作業在 ${date} 的 ${time} 上已提交，並且正在等待評分";

  static m41(studentName) => "關於：${studentName}，課程大綱";

  static m42(count) => "${count} 則未讀";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("「作為」本質上是以此使用者身份登錄，沒有密碼。您將能夠進行任何操作，就好像您就是此使用者一樣，並且，其他使用者也會認為是此使用者執行了操作。然而，活動紀錄會記錄事實，即您是代表此使用者執行操作的人。"),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("描述必填。"),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("主題必填。"),
    "Act As User" : MessageLookupByLibrary.simpleMessage("作為使用者"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("添加學生"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("添加附件"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("添加新學生"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("添加學生…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("提醒設定"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("在下列情形時提醒我..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("所有評分期"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("電郵地址必填。"),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("嘗試顯示此連結時出現錯誤"),
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
    "Calendars" : MessageLookupByLibrary.simpleMessage("行事曆"),
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
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("暗色模式"),
    "Date" : MessageLookupByLibrary.simpleMessage("日期"),
    "Delete" : MessageLookupByLibrary.simpleMessage("刪除"),
    "Description" : MessageLookupByLibrary.simpleMessage("描述"),
    "Device" : MessageLookupByLibrary.simpleMessage("裝置"),
    "Device model" : MessageLookupByLibrary.simpleMessage("裝置機型"),
    "Domain" : MessageLookupByLibrary.simpleMessage("網域"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("網域："),
    "Done" : MessageLookupByLibrary.simpleMessage("已完成"),
    "Download" : MessageLookupByLibrary.simpleMessage("下載"),
    "Due" : MessageLookupByLibrary.simpleMessage("截止"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("極其重要的緊急情況！！"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("電郵地址"),
    "Email:" : MessageLookupByLibrary.simpleMessage("電郵地址："),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("輸入已提供給您的學生配對代碼。如果配對代碼無效，可能是已到期"),
    "Event" : MessageLookupByLibrary.simpleMessage("活動"),
    "Excused" : MessageLookupByLibrary.simpleMessage("已免除"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("失敗。點選選項。"),
    "Filter" : MessageLookupByLibrary.simpleMessage("篩選器"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("篩選條件"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("首頁"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("完整錯誤訊息"),
    "Grade" : MessageLookupByLibrary.simpleMessage("評分"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("評分百分比"),
    "Graded" : MessageLookupByLibrary.simpleMessage("已評分"),
    "Grades" : MessageLookupByLibrary.simpleMessage("成績"),
    "Help" : MessageLookupByLibrary.simpleMessage("支援"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("高對比模式"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("問題對您造成什麼影響？"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("在收到您的回覆之前，我無法處理。"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("我需要支援，但並不緊迫。"),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("我登入時發生問題"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("關於 Canvas Parent 程式 [Android] 的想法"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("收件匣"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("收件匣無訊息"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("未完成"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("機構通告"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("機構的告"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("教學"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("本頁面的互動受您所在機構的限制。"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("看來是適合休息、放鬆和充電的一天。"),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("此空間中尚未建立任何作業。"),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("只是隨意提問、評論、想法、建議……"),
    "Late" : MessageLookupByLibrary.simpleMessage("逾期"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("啟動外部工具"),
    "Legal" : MessageLookupByLibrary.simpleMessage("法律事務"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("淡色模式"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("連結錯誤"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("位置："),
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
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("在瀏覽器中打開"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("使用其他應用程式開啟"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("配對代碼"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("準備中…"),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("先前登入"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("隱私政策"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("隱私政策，使用條款，開放源碼"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR 碼"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("收件人"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("提醒我"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("提醒"),
    "Reply" : MessageLookupByLibrary.simpleMessage("回覆"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("全部回覆"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("報告問題"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("請求登入支援"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("請求登入支援按鈕"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("重新啟動應用程式"),
    "Retry" : MessageLookupByLibrary.simpleMessage("重試"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("返回登入頁面"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("學生"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("選擇收件人"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("傳送與此作業有關的訊息"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("傳送與此學生有關的訊息"),
    "Send message" : MessageLookupByLibrary.simpleMessage("傳送訊息"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("設定通知此活動的日期和時間。"),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("設定通知此指定作業的日期和時間。"),
    "Settings" : MessageLookupByLibrary.simpleMessage("設定"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("分享您對於本應用程式的喜愛"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("有些地方出現了問題，但我能繞過問題實現我的目的。"),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("停止作為使用者"),
    "Student" : MessageLookupByLibrary.simpleMessage("學生"),
    "Subject" : MessageLookupByLibrary.simpleMessage("主題"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("已提交"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("成功提交！"),
    "Summary" : MessageLookupByLibrary.simpleMessage("摘要"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("切換使用者"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("課程大綱"),
    "TA" : MessageLookupByLibrary.simpleMessage("助教"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("教師"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("點選以選擇您要在行事曆上看到的最愛課程。"),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("點選以和新學生配對"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("點選以選擇此學生"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("點選以顯示學生選擇器"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("教師"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("告訴我們本應用程式最讓您滿意之處"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("使用條款"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("下列資訊能幫我們好好理解您的想法："),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("您輸入的伺服器未獲得使用該應用程式的授權。"),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("該應用程式的使用者代理未獲得授權。"),
    "Theme" : MessageLookupByLibrary.simpleMessage("主題"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("未安裝可開啟此檔案的應用程式"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("沒有可用的頁面資訊。"),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("載入使用條款時出現問題"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("載入此課程的收件人時發生錯誤"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("載入此課程的摘要詳細資料時發生錯誤。"),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("載入此通告時發生錯誤"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("載入此交談時發生錯誤"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("載入此檔案時發生錯誤"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("載入您的收件匣訊息時發生錯誤。"),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("載入您的學生的提醒時發生錯誤。"),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("載入您的學生的行事曆時發生錯誤"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("載入您的學生時發生錯誤。"),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("載入您的學生的課程時發生錯誤。"),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("嘗試作為此使用者時出現錯誤。請檢查網域及使用者 ID 然後重試。"),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("尚未有任何通知。尚未有任何通知。"),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("該 app 程式未獲得使用權限。"),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("此課程還沒有任何作業列表或行事曆。"),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("不支援此檔案，而且無法透過應用程式檢視"),
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
    "User ID" : MessageLookupByLibrary.simpleMessage("使用者 ID"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("使用者 ID："),
    "Version Number" : MessageLookupByLibrary.simpleMessage("版本編號"),
    "View error details" : MessageLookupByLibrary.simpleMessage("檢視錯誤詳細資料"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("我們目前正在建立讓您開心檢視的功能。"),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("我們無法顯示此連結，連結可能屬於您現在尚未登入的機構。"),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("我們找不到與此帳戶有關的任何學生"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("無法確認此應用程式使用的伺服器。"),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("不確定發生什麼事，但不是好事。如果持續發生，請聯絡我們。"),
    "Yes" : MessageLookupByLibrary.simpleMessage("是"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("您未正在觀察任何學生。"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("您必須輸入使用者 ID"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("您必須輸入有效的網域"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("將在以下時間通知您關於此作業…"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("將在以下時間通知您關於此沽動…"),
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
    "collapse" : MessageLookupByLibrary.simpleMessage("收起"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("收起"),
    "contentDescriptionScoreOutOfPointsPossible" : m12,
    "courseForWhom" : m13,
    "courseGradeAboveThreshold" : m14,
    "courseGradeBelowThreshold" : m15,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("課程"),
    "dateAtTime" : m16,
    "domainSearchHelpBody" : m17,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("如何找到我的學校？"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("輸入學校名稱或地區…"),
    "dueDateAtTime" : m18,
    "endMasqueradeLogoutMessage" : m19,
    "endMasqueradeMessage" : m20,
    "eventSubjectMessage" : m21,
    "eventTime" : m22,
    "expand" : MessageLookupByLibrary.simpleMessage("展開"),
    "expanded" : MessageLookupByLibrary.simpleMessage("展開"),
    "finalGrade" : m23,
    "findSchool" : MessageLookupByLibrary.simpleMessage("尋找學校"),
    "frontPageSubjectMessage" : m24,
    "gradeFormatScoreOutOfPointsPossible" : m25,
    "gradesSubjectMessage" : m26,
    "latePenalty" : m27,
    "me" : MessageLookupByLibrary.simpleMessage("我"),
    "messageLinkPostscript" : m28,
    "minus" : MessageLookupByLibrary.simpleMessage("減"),
    "mustBeAboveN" : m29,
    "mustBeBelowN" : m30,
    "next" : MessageLookupByLibrary.simpleMessage("下一個"),
    "nextMonth" : m31,
    "nextWeek" : m32,
    "noDomainResults" : m33,
    "ok" : MessageLookupByLibrary.simpleMessage("確定"),
    "outOfPoints" : m34,
    "plusRecipientCount" : m35,
    "pointsPossible" : m36,
    "previousMonth" : m37,
    "previousWeek" : m38,
    "selectedMonthLabel" : m39,
    "send" : MessageLookupByLibrary.simpleMessage("傳送"),
    "submissionStatusSuccessSubtitle" : m40,
    "syllabusSubjectMessage" : m41,
    "unread" : MessageLookupByLibrary.simpleMessage("未讀"),
    "unreadCount" : m42
  };
}
