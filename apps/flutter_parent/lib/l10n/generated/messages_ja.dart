// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a ja locale. All the
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
  String get localeName => 'ja';

  static m0(version) => "バージョン ${version}";

  static m1(threshold) => "${threshold} 点を超える課題成績";

  static m2(threshold) => "${threshold} 点未満の課題成績";

  static m3(moduleName) => "この課題はモジュール \"${moduleName}\" によってロックされています。";

  static m4(studentName, assignmentName) => "Re：${studentName}、課題 - ${assignmentName}";

  static m5(points) => "${points} 点";

  static m6(points) => "${points} 点";

  static m7(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName}から他 1 人', other: '${authorName}から他 ${howMany} 人')}";

  static m8(authorName, recipientName) => "${authorName}から${recipientName}";

  static m9(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName}から${recipientName}と他 1 人', other: '${authorName} から${recipientName}と他 ${howMany} 人')}";

  static m10(count) => "${count} 以上";

  static m11(score, pointsPossible) => "${pointsPossible} 点中 ${score} 点";

  static m12(studentShortName) => "${studentShortName}用";

  static m13(threshold) => "${threshold} 点を超えるコース成績";

  static m14(threshold) => "${threshold} 点未満のコース成績";

  static m15(date, time) => "${date}、${time}";

  static m16(canvasGuides, canvasSupport) => "アクセスしようとしている学校または学区の名前を検索してみてください (「Smith Private School」または「Smith County Schools」など)。「Smith.instructure.com」などの Canvas ドメインを直接入力することもできます。\n\n教育機関の Canvas アカウントの検索に関する詳細については、${canvasGuides}にアクセスする、${canvasSupport}に問い合わせる、または学校に問い合わせてサポートを受けることができます。";

  static m17(date, time) => "${date}、${time} 期限";

  static m18(studentName, eventTitle) => "Re：${studentName}、イベント - ${eventTitle}";

  static m19(startAt, endAt) => "${startAt} ～ ${endAt}";

  static m20(grade) => "最終成績：${grade}";

  static m21(studentName) => "Re：${studentName}、フロントページ";

  static m22(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m23(studentName) => "Re：${studentName}、成績";

  static m24(pointsLost) => "提出遅れペナルティ (-${pointsLost})";

  static m25(studentName, linkUrl) => "Re：${studentName}さん、${linkUrl}";

  static m26(percentage) => "${percentage} を超える必要があります";

  static m27(percentage) => "${percentage} 未満にする必要があります";

  static m28(month) => "来月：${month}";

  static m29(date) => "${date} から始まる翌週";

  static m30(query) => "\"${query}\"に一致する学校が見つかりません";

  static m31(points, howMany) => "${Intl.plural(howMany, one: '1 ポイントから', other: '${points} ポイントから')}";

  static m32(count) => "+ ${count}";

  static m33(points) => "${points} の可能なポイント";

  static m34(month) => "前月：${month}";

  static m35(date) => "${date} から始まる前週";

  static m36(month) => "${month} 月";

  static m37(date, time) => "この課題は ${date} の ${time} に提出され、採点中です";

  static m38(studentName) => "Re：${studentName}、シラバス";

  static m39(count) => "未読 ${count} 件";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("説明は必須です。"),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("件名は必須です。"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("受講生を追加"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("添付ファイルを追加する"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("新しい受講生を追加する"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("次を使って受講生を追加します…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("アラート設定"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("次の場合にアラートを送信…"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("すべての採点期間"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("E メールアドレスは必須です。"),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("このリンクを表示しようとしてエラーが発生しました"),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("予期しないエラーが発生しました"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("Android OS バージョン"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("外観"),
    "Application version" : MessageLookupByLibrary.simpleMessage("アプリケーションのバージョン"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("受講生と講師のどちらですか？"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("ログアウトしてもよろしいですか？"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("このページを閉じてもよろしいですか？送信されていないメッセージは失われます。"),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("課題の詳細"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("未満の課題成績"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("を超える課題成績"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("課題がありません"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("予定表"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("キャンセル"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas 受講生"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas 講師"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("GitHub 上のキャンバス"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("メッセージを送るコースを選択する"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("ギャラリーから選択する"),
    "Complete" : MessageLookupByLibrary.simpleMessage("完了"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("サポートに問い合わせる"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("コースのお知らせ"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("コースのお知らせ"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("未満のコース成績"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("を超えるコース成績"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("ダークモード"),
    "Date" : MessageLookupByLibrary.simpleMessage("日付"),
    "Delete" : MessageLookupByLibrary.simpleMessage("削除"),
    "Description" : MessageLookupByLibrary.simpleMessage("説明"),
    "Device" : MessageLookupByLibrary.simpleMessage("デバイス"),
    "Device model" : MessageLookupByLibrary.simpleMessage("デバイスモデル"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("ドメイン："),
    "Done" : MessageLookupByLibrary.simpleMessage("終了"),
    "Download" : MessageLookupByLibrary.simpleMessage("ダウンロード"),
    "Due" : MessageLookupByLibrary.simpleMessage("締切日"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("緊急事態です！"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E メールアドレス"),
    "Email:" : MessageLookupByLibrary.simpleMessage("Eメール："),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("提供されている受講生ペアリングコードを入力してください。ペアリングコードが機能しない場合は、有効期限が切れているかもしれません"),
    "Event" : MessageLookupByLibrary.simpleMessage("イベント"),
    "Excused" : MessageLookupByLibrary.simpleMessage("免除"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("失敗しました。タップしてオプションを表示してください。"),
    "Filter" : MessageLookupByLibrary.simpleMessage("フィルタ"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("フィルタ条件"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("フロントページ"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("完全なエラーメッセージ"),
    "Grade" : MessageLookupByLibrary.simpleMessage("成績"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("成績パーセンテージ"),
    "Graded" : MessageLookupByLibrary.simpleMessage("採点済み"),
    "Grades" : MessageLookupByLibrary.simpleMessage("成績"),
    "Help" : MessageLookupByLibrary.simpleMessage("ヘルプ"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("高コントラストモード"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("これはあなたにどのような影響を与えていますか？"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("返事がくるまで何もできません。"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("急ぎではありませんが、サポートが必要です。"),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("ログインできません"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Canvas Parent アプリ [Android] へのアイデア"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("受信トレイ"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("受信トレイゼロ"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("未完了"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("学校のお知らせ"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("学校のお知らせ"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("指示"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("休息をとってリラックスし、充電するためにぴったりな日のようです。"),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("このスペースでは、まだ課題が作成されていないようです。"),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("ちょっとした疑問、コメント、アイデア、提案です…"),
    "Late" : MessageLookupByLibrary.simpleMessage("提出遅れ"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("外部ツールを起動する"),
    "Legal" : MessageLookupByLibrary.simpleMessage("法務"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("ライトモード"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("リンクエラー"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("ロケール："),
    "Location" : MessageLookupByLibrary.simpleMessage("場所"),
    "Locked" : MessageLookupByLibrary.simpleMessage("ロックされています"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("ログアウト"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("受講生を管理する"),
    "Message" : MessageLookupByLibrary.simpleMessage("メッセージ"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("メッセージの件名"),
    "Missing" : MessageLookupByLibrary.simpleMessage("提出なし"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("100 未満にする必要があります"),
    "Network error" : MessageLookupByLibrary.simpleMessage("ネットワークエラー"),
    "Never" : MessageLookupByLibrary.simpleMessage("今後実行しない"),
    "New message" : MessageLookupByLibrary.simpleMessage("新規メッセージ"),
    "No" : MessageLookupByLibrary.simpleMessage("いいえ"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("アラートはありません"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("課題はありません"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("コースはありません"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("締切日なし"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("今日イベントはありません！"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("成績はありません"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("場所が指定されていません"),
    "No Students" : MessageLookupByLibrary.simpleMessage("受講生はいません"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("件名なし"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("サマリーはありません"),
    "No description" : MessageLookupByLibrary.simpleMessage("説明なし"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("受信者が選択されていません"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("未採点"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("未提出"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("親ではありませんか？"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("課題と予定表イベントに関するリマインダの通知"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("OS バージョン"),
    "Observer" : MessageLookupByLibrary.simpleMessage("オブザーバー"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("他のアプリのいずれかが、より適切かもしれません。ひとつをタップして Play Store にアクセスしてください。"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("ブラウザで開く"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("別のアプリで開く"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("ペアリングコード"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("準備中…"),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("以前のログイン"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("個人情報保護方針"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("プライバシーポリシー、利用規約、オープンソース"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR コード"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("受信者"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("リマインドする"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("リマインダ"),
    "Reply" : MessageLookupByLibrary.simpleMessage("返信"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("全員に返信"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("問題を報告する"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("ログインサポートをリクエストする"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("ログインサポートのリクエストボタン"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("アプリを再起動"),
    "Retry" : MessageLookupByLibrary.simpleMessage("再試行"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("ログインに戻る"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("受講生"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("受信者を選択する"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("この課題についてメッセージを送信する"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("このコースに関するメッセージを送信する"),
    "Send message" : MessageLookupByLibrary.simpleMessage("メッセージを送信する"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("このイベントの通知を受ける日付と時間を設定します。"),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("この特定の課題について通知を受ける日付と時間を設定します。"),
    "Set reminder switch" : MessageLookupByLibrary.simpleMessage("リマインダスイッチを設定する"),
    "Settings" : MessageLookupByLibrary.simpleMessage("設定"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("アプリへの愛を共有しましょう"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("何かが機能していないのですが、必要なことはそれを使わずに実行できます。"),
    "Student" : MessageLookupByLibrary.simpleMessage("受講者"),
    "Subject" : MessageLookupByLibrary.simpleMessage("件名"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("提出済み"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("正常に提出されました！"),
    "Summary" : MessageLookupByLibrary.simpleMessage("概要"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("ユーザーを切り替える"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("シラバス"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("講師"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("タップして予定表に表示したいコースをお気に入りにします。"),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("タップして新しい受講生とペアにします"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("タップしてこの受講生を選択します"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("タップして受講生セレクタを表示します"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("講師"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("このアプリの好きな部分を教えてください"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("利用規約"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("次の情報は、Canvas があなたのアイデアをより良く理解するために役立ちます："),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("入力されたサーバーは、このアプリ用に許可されていません。"),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("このアプリのユーザーエージェントは許可されていません。"),
    "Theme" : MessageLookupByLibrary.simpleMessage("テーマ"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("このファイルを開くことができるアプリケーションがインストールされていません"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("利用できるページ情報はありません。"),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("利用規約のロード中に問題が発生しました"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("このコースの受信者のロード中にエラーが発生しました。"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("このコースのサマリー詳細のロード中にエラーが発生しました。"),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("このお知らせのロード中にエラーが発生しました"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("この会話のロード中にエラーが発生しました"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("このファイルのロード中にエラーが発生しました"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("受信トレイメッセージのロード中にエラーが発生しました。"),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("受講生のアラートのロード中にエラーが発生しました"),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("受講生の予定表のロード中にエラーが発生しました"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("受講生のロード中にエラーが発生しました。"),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("受講生のコースのロード中にエラーが発生しました"),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("通知する事柄はまだ何もありません。"),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("このアプリは使用を許可されていません。"),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("このコースには、まだ課題または予定表イベントがありません。"),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("このファイルはサポートされておらず、アプリで表示できません"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("合計成績"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("エラーです！"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("コースを取得できません。接続を確認して、もう一度お試しください。"),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("このイメージをロードできません"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("このメディアファイルを再生できません"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("メッセージを送信できません。接続をチェックして、もう一度やり直してください。"),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("工事中"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("不明のユーザー"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("変更が保存されていません"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("サポートされないファイル"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("ファイルをアップロードする"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("カメラを使用する"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ユーザー ID："),
    "Version Number" : MessageLookupByLibrary.simpleMessage("バージョン番号"),
    "View error details" : MessageLookupByLibrary.simpleMessage("エラーの詳細を表示"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("現在、皆さんにお楽しみいただく機能を構築中です。"),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("このリンクを表示することができません。このリンクは、現在ログインしていない学校のものである可能性があります。"),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("このアカウントに関連付けられている受講生を見つけることができませんでした"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("このアプリで使用するサーバーを検証できませんでした。"),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("何が起こったかはわかりませんが、問題が発生したようです。問題が解決されない場合は、Canvas までお問い合わせください。"),
    "Yes" : MessageLookupByLibrary.simpleMessage("はい"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("どの受講生もオブザーブしていません。"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("…にこの課題に関する通知を受けます。"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("次の予定でこのイベントに関する通知を受けます…"),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("コードが間違っているか、期限が切れています。"),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("受講生のコースはまだ公開されていない可能性があります"),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("コンテンツは以上です！"),
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("アラート"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("予定表"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas ガイド"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas ロゴ"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas サポート"),
    "collapse" : MessageLookupByLibrary.simpleMessage("折りたたむ"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("折りたたみ"),
    "contentDescriptionScoreOutOfPointsPossible" : m11,
    "courseForWhom" : m12,
    "courseGradeAboveThreshold" : m13,
    "courseGradeBelowThreshold" : m14,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("コース"),
    "dateAtTime" : m15,
    "dismiss" : MessageLookupByLibrary.simpleMessage("取り下げる"),
    "domainSearchHelpBody" : m16,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("私の学校または学区はどのように見つければよいですか？"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("学校名または学区を入力してください…"),
    "dueDateAtTime" : m17,
    "eventSubjectMessage" : m18,
    "eventTime" : m19,
    "expand" : MessageLookupByLibrary.simpleMessage("さらに表示する"),
    "expanded" : MessageLookupByLibrary.simpleMessage("拡大"),
    "finalGrade" : m20,
    "findSchool" : MessageLookupByLibrary.simpleMessage("学校を探す"),
    "frontPageSubjectMessage" : m21,
    "gradeFormatScoreOutOfPointsPossible" : m22,
    "gradesSubjectMessage" : m23,
    "latePenalty" : m24,
    "me" : MessageLookupByLibrary.simpleMessage("私"),
    "messageLinkPostscript" : m25,
    "minus" : MessageLookupByLibrary.simpleMessage("マイナス"),
    "mustBeAboveN" : m26,
    "mustBeBelowN" : m27,
    "next" : MessageLookupByLibrary.simpleMessage("次へ"),
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
    "send" : MessageLookupByLibrary.simpleMessage("送信"),
    "submissionStatusSuccessSubtitle" : m37,
    "syllabusSubjectMessage" : m38,
    "unread" : MessageLookupByLibrary.simpleMessage("未読"),
    "unreadCount" : m39
  };
}
