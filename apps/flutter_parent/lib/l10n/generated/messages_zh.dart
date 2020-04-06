// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a zh locale. All the
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
  String get localeName => 'zh';

  static m0(version) => "v. ${version}";

  static m1(threshold) => "作业评分高于${threshold}";

  static m2(threshold) => "作业评分低于${threshold}";

  static m3(moduleName) => "此作业已被模块“${moduleName}”锁定。";

  static m4(studentName, assignmentName) => "事由：${studentName}，作业 - ${assignmentName}";

  static m5(points) => "${points} 分";

  static m6(points) => "${points} 分";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName}至另外1个', other: '${authorName}至另外${howMany}个')}";

  static m8(authorName, recipientName) => "${authorName} 至 ${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName}至${recipientName}和另外1个', other: '${authorName}至${recipientName}和另外${howMany}个')}";

  static m10(count) => "${count}+";

  static m11(score, pointsPossible) => "得分 ${score}，总分 ${pointsPossible}";

  static m12(studentShortName) => "适用于${studentShortName}";

  static m13(threshold) => "课程评分高于${threshold}";

  static m14(threshold) => "课程评分低于${threshold}";

  static m15(date, time) => "${date}，时间 ${time}";

  static m16(canvasGuides, canvasSupport) => "尝试搜索您要访问的学校或校区的名称，例如“Smith Private School”或“Smith County Schools”。也可以直接输入Canvas域，例如“smith.instructure.com”。\n\n有关查找机构Canvas账户的更多信息，可访问${canvasGuides}、联系${canvasSupport}或您的学校寻求帮助。";

  static m17(date, time) => "截止于 ${date}，${time}";

  static m18(studentName, eventTitle) => "事由：${studentName}，活动 - ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} - ${endAt}";

  static m20(grade) => "最终评分：${grade}";

  static m21(studentName) => "事由：${studentName}，首页";

  static m22(score, pointsPossible) => "${score}/${pointsPossible}";

  static m23(studentName) => "事由：${studentName}，评分";

  static m24(pointsLost) => "迟交罚分 (-${pointsLost})";

  static m25(studentName, linkUrl) => "事由：${studentName}，${linkUrl}";

  static m26(percentage) => "必须高于${percentage}";

  static m27(percentage) => "必须低于${percentage}";

  static m28(month) => "下个月：${month}";

  static m29(date) => "下周，从${date}开始";

  static m30(query) => "找不到与“${query}”匹配的学校";

  static m31(points, howMany) => "${Intl.plural(howMany, one: '共1分', other: '共${points}分')}";

  static m32(count) => "+${count}";

  static m33(points) => "${points}满分";

  static m34(month) => "上个月：${month}";

  static m35(date) => "上周，从${date}开始";

  static m36(month) => "${month}月";

  static m37(date, time) => "此作业提交时间为${date}，${time}，正在等待评分";

  static m38(studentName) => "事由：${studentName}，教学大纲";

  static m39(count) => "${count}未读";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("必须填写描述。"),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("必须填写主题。"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("添加学生"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("添加附件"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("添加新学生"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("使用...添加学生"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("警告设置"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("当...时警告我"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("所有评分周期"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("必须填写电子邮件地址。"),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("尝试显示此链接时发生错误"),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("一个意料之外的错误发生了。"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android操作系统版本"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("外观"),
    "Application version" : MessageLookupByLibrary.simpleMessage("应用程序版本"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("您是学生还是教师？"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("是否确定要登出？"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("是否确定要关闭此页面？未发送的消息将会丢失。"),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("作业详情"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("作业评分高于"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("作业评分低于"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("缺少作业"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("日历"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("取消"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas 学生"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas 教师"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("GitHub上的Canvas"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("选择一个课程来发送讯息"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("从图库中选择"),
    "Complete" : MessageLookupByLibrary.simpleMessage("完成"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("联系支持"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("课程公告"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("课程公告"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("课程评分高于"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("课程评分低于"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("暗模式"),
    "Date" : MessageLookupByLibrary.simpleMessage("日期"),
    "Delete" : MessageLookupByLibrary.simpleMessage("删除"),
    "Description" : MessageLookupByLibrary.simpleMessage("说明"),
    "Device" : MessageLookupByLibrary.simpleMessage("设备"),
    "Device model" : MessageLookupByLibrary.simpleMessage("设备型号"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("域："),
    "Done" : MessageLookupByLibrary.simpleMessage("完成"),
    "Download" : MessageLookupByLibrary.simpleMessage("下载"),
    "Due" : MessageLookupByLibrary.simpleMessage("截止"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("非常紧急！"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("电子邮件地址"),
    "Email:" : MessageLookupByLibrary.simpleMessage("电子邮箱："),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("输入提供给您的学生配对代码。如果配对代码无法使用，可能已过期"),
    "Event" : MessageLookupByLibrary.simpleMessage("活动"),
    "Excused" : MessageLookupByLibrary.simpleMessage("已免除"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("失败。点击以获取选项。"),
    "Filter" : MessageLookupByLibrary.simpleMessage("筛选器"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("筛选条件"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("首页"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("完整的错误消息"),
    "Grade" : MessageLookupByLibrary.simpleMessage("评分"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("评分百分数"),
    "Graded" : MessageLookupByLibrary.simpleMessage("已评分"),
    "Grades" : MessageLookupByLibrary.simpleMessage("评分"),
    "Help" : MessageLookupByLibrary.simpleMessage("帮助"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("高对比度模式"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("这对您有什么影响？"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("我无法把事情做好，直到我听到您回来。"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("我需要一些帮助，但不是迫切的。"),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("我在登录时遇到问题"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("构想 Canvas 的[安卓版本]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("收件箱"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("收件箱为零"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("未完成"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("机构公告"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("机构通告"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("说明"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("今天是休息放松的一天。"),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("似乎尚未在此区域创建作业。"),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("只是一个偶然问题、意见、想法、建议..."),
    "Late" : MessageLookupByLibrary.simpleMessage("迟交"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("启动外部工具"),
    "Legal" : MessageLookupByLibrary.simpleMessage("法律"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("亮模式"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("链接错误"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("区域设置："),
    "Location" : MessageLookupByLibrary.simpleMessage("位置"),
    "Locked" : MessageLookupByLibrary.simpleMessage("已锁定"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("注销"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("管理学生"),
    "Message" : MessageLookupByLibrary.simpleMessage("消息"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("消息主题"),
    "Missing" : MessageLookupByLibrary.simpleMessage("未交"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("必须低于100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("网络错误"),
    "Never" : MessageLookupByLibrary.simpleMessage("从不"),
    "New message" : MessageLookupByLibrary.simpleMessage("新建消息"),
    "No" : MessageLookupByLibrary.simpleMessage("否"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("无警告"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("无作业"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("没有课程"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("无截止日期"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("今天没有事件！"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("没有评分"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("没有具体的位置"),
    "No Students" : MessageLookupByLibrary.simpleMessage("没有学生"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("无主题"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("无摘要"),
    "No description" : MessageLookupByLibrary.simpleMessage("无描述"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("未选择收件人"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("未评分"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("未提交"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("不是家长？"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("有关作业和日历活动的提醒通知"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("操作系统版本"),
    "Observer" : MessageLookupByLibrary.simpleMessage("观察员"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("我们的其他应用可能更合适。单击可访问 Play Store。"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("在浏览器里打开"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("使用其他应用程序打开"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("配对代码"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("正在准备..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("历史登录记录"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("隐私政策"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("隐私政策、使用条款、开放源"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("二维码"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("收件人"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("提醒我"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("提醒"),
    "Reply" : MessageLookupByLibrary.simpleMessage("回复"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("回复所有人"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("报告一个问题"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("请求登录帮助"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("请求登录帮助按钮"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("重启应用程序"),
    "Retry" : MessageLookupByLibrary.simpleMessage("重试"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("返回登录"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("学生"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("选择收件人"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("发送有关此作业的消息"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("发送有关此课程的消息"),
    "Send message" : MessageLookupByLibrary.simpleMessage("发送消息"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("设定通知此活动的日期和时间。"),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("设定通知此特定作业的日期和时间。"),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("设定提醒开关"),
    "Settings" : MessageLookupByLibrary.simpleMessage("设置"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("分享您所喜爱的应用程序"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("系统出错了，但我可以解决它，并完成我所需要做的事。"),
    "Student" : MessageLookupByLibrary.simpleMessage("学生"),
    "Subject" : MessageLookupByLibrary.simpleMessage("主题"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("已提交"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("已成功提交。"),
    "Summary" : MessageLookupByLibrary.simpleMessage("摘要"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("转换用户"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("教学大纲"),
    "TA" : MessageLookupByLibrary.simpleMessage("助教"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("教师"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("点击以收藏您希望在日历中看到的课程。"),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("点击以与新生配对"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("点击以选择此学生"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("点击以显示学生选择器"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("教师"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("告诉我们您最喜欢应用程序的部分"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("使用条款"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("下面的信息将帮助我们更好地了解您的想法："),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("您输入的服务器没有授权此应用程序。"),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("此应用程序的用户代理未获得授权。"),
    "Theme" : MessageLookupByLibrary.simpleMessage("主题、话题"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("未安装可以打开此文件的应用程序"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("没有可用的页面信息。"),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("加载使用条款时发生错误"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("加载此课程的收件人时发生错误。"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("加载此课程的摘要详情时发生错误。"),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("加载此公告时发生错误"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("加载此会话时发生错误"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("加载此文件时发生错误"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("加载收件箱消息时发生错误。"),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("加载学生的警告时发生错误。"),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("加载学生的日历时发生错误"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("加载学生时发生错误。"),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("加载学生的课程时发生错误。"),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("暂时没有任何通知。"),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("此程序没有被授权使用。"),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("此课程暂时没有任何作业或日历活动。"),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("此文件不受支持，无法通过应用程序查看"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("总分"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("嗳哟！"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("无法获取课程。请检查您的连接，然后再试一次。"),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("无法加载此图片"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("无法播放此媒体文件"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("无法发送消息。请检查您的网络连接并重试。"),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("正在建设中"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("未知用户"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("无法保存更改"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("不受支持的文件"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("上传文件"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("使用相机"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("用户 ID："),
    "Version Number" : MessageLookupByLibrary.simpleMessage("版本号"),
    "View error details" : MessageLookupByLibrary.simpleMessage("查看错误详细信息"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("我们目前在构建此功能，以便为您提供愉悦的浏览体验。"),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("无法显示此链接，可能属于您目前未登录的机构。"),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("我们找不到任何与该账户关联的学生"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("我们无法验证此应用程序使用的服务器。"),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("不知道发生了什么事情，但似乎不太妙。如果问题持续，请联系我们。"),
    "Yes" : MessageLookupByLibrary.simpleMessage("是"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("您未观察任何学生。"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("您将在...收到有关此作业的通知"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("您将在...收到有关此活动的通知"),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("您的代码错误或已过期。"),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("学生的课程可能尚未发布。"),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("您全部跟上了！"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("警告"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("日历"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas指南"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas徽标"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas支持"),
    "collapse" : MessageLookupByLibrary.simpleMessage("折叠"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("折叠"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("课程"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("撤销"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("如何查找我的学校或校区？"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("输入学校名称或校区..."),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("展开"),
    "expanded" : MessageLookupByLibrary.simpleMessage("展开"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("查找学校"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("本人"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("负分"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("下一步"),
    "nextMonth" : m28,
    "nextWeek" : m29,
    "noDomainResults" : m30,
    "ok" : MessageLookupByLibrary.simpleMessage("确定"),
    "outOfPoints" : m31,
    "plusRecipientCount" : m32,
    "pointsPossible" : m33,
    "previousMonth" : m34,
    "previousWeek" : m35,
    "selectedMonthLabel" : m36,
    "send" : MessageLookupByLibrary.simpleMessage("发送"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("未读"),
    "unreadCount" : m39
  };
}
