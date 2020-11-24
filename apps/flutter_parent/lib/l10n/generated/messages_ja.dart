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

  static m0(userName) => "あなたは${userName}として機能しています";

  static m1(version) => "バージョン ${version}";

  static m2(threshold) => "${threshold} 点を超える課題成績";

  static m3(threshold) => "${threshold} 点未満の課題成績";

  static m4(moduleName) => "この課題はモジュール \"${moduleName}\" によってロックされています。";

  static m5(studentName, assignmentName) => "Re：${studentName}、課題 - ${assignmentName}";

  static m6(points) => "${points} 点";

  static m7(points) => "${points} 点";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName}から他 1 人', other: '${authorName}から他 ${howMany} 人')}";

  static m9(authorName, recipientName) => "${authorName}から${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName}から${recipientName}と他 1 人', other: '${authorName} から${recipientName}と他 ${howMany} 人')}";

  static m11(count) => "${count}+";

  static m12(studentName) => "${studentName}の色を変更する";

  static m13(score, pointsPossible) => "${pointsPossible} 点中 ${score} 点";

  static m14(studentShortName) => "${studentShortName}用";

  static m15(threshold) => "${threshold} 点を超えるコース成績";

  static m16(threshold) => "${threshold} 点未満のコース成績";

  static m17(date, time) => "${date}、${time}";

  static m18(alertTitle) => "${alertTitle}を却下する";

  static m19(canvasGuides, canvasSupport) => "アクセスしようとしている学校または学区の名前を検索してみてください (「Smith Private School」または「Smith County Schools」など)。「Smith.instructure.com」などの Canvas ドメインを直接入力することもできます。\n\n教育機関の Canvas アカウントの検索に関する詳細については、${canvasGuides}にアクセスする、${canvasSupport}に問い合わせる、または学校に問い合わせてサポートを受けることができます。";

  static m20(date, time) => "${date}、${time} 期限";

  static m21(userName) => "${userName}として機能することを停止して、ログアウトされます。";

  static m22(userName) => "${userName}として機能することを停止して、元のアカウントに戻ります。";

  static m23(studentName, eventTitle) => "Re：${studentName}、イベント - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} ～ ${endAt}";

  static m25(grade) => "最終成績：${grade}";

  static m26(studentName) => "Re：${studentName}、フロントページ";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "Re：${studentName}、成績";

  static m29(pointsLost) => "提出遅れペナルティ (-${pointsLost})";

  static m30(studentName, linkUrl) => "Re：${studentName}さん、${linkUrl}";

  static m31(percentage) => "${percentage} を超える必要があります";

  static m32(percentage) => "${percentage} 未満にする必要があります";

  static m33(month) => "来月：${month}";

  static m34(date) => "${date} から始まる翌週";

  static m35(query) => "\"${query}\"に一致する学校が見つかりません";

  static m36(points, howMany) => "${Intl.plural(howMany, one: '1 ポイントから', other: '${points} ポイントから')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} の可能なポイント";

  static m39(month) => "前月：${month}";

  static m40(date) => "${date} から始まる前週";

  static m41(termsOfService, privacyPolicy) => "[アカウントを作成]をタップすると、${termsOfService}に同意し、${privacyPolicy}を受け入れたことになります";

  static m42(version) => "Android への提案 - Canvas Parent${version}";

  static m43(month) => "${month} 月";

  static m44(position) => "${Intl.plural(position, one: '${position} 星', other: '${position}星')}";

  static m45(date, time) => "この課題は ${date} の ${time} に提出され、採点中です";

  static m46(studentName) => "Re：${studentName}、シラバス";

  static m47(count) => "未読 ${count} 件";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"機能する\"とは、基本的にパスワードなしでこのユーザーとしてログインすることです。このユーザーであるかのようにアクションを実行することができ、他のユーザーからは、このユーザーがそれらを実行したように見えますが、監査ログには、あなたがこのユーザーの代わりにアクションを実行したことが記録されます。"),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("説明は必須です。"),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("この受講生を追加中にネットワークエラーが発生しました。接続をチェックして、もう一度やり直してください。"),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("件名は必須です。"),
    "Act As User" : MessageLookupByLibrary.simpleMessage("ユーザーとして機能する"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("受講生を追加"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("添付ファイルを追加する"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("新しい受講生を追加する"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("次を使って受講生を追加します…"),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("アラート設定"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("次の場合にアラートを送信…"),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("すべての採点期間"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("アカウントをすでにお持ちですか？ "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("E メールアドレスは必須です。"),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("このリンクを表示しようとしてエラーが発生しました"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("選択の保存中にエラーが発生しました。再度試してください。"),
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
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("バーニー、フューシャ"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("予定表"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("カメラ許可"),
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
    "Create Account" : MessageLookupByLibrary.simpleMessage("アカウントを作成する"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("ダークモード"),
    "Date" : MessageLookupByLibrary.simpleMessage("日付"),
    "Delete" : MessageLookupByLibrary.simpleMessage("削除"),
    "Description" : MessageLookupByLibrary.simpleMessage("説明"),
    "Device" : MessageLookupByLibrary.simpleMessage("デバイス"),
    "Device model" : MessageLookupByLibrary.simpleMessage("デバイスモデル"),
    "Domain" : MessageLookupByLibrary.simpleMessage("ドメイン"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("ドメイン："),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("これ以上表示しないでください"),
    "Done" : MessageLookupByLibrary.simpleMessage("終了"),
    "Download" : MessageLookupByLibrary.simpleMessage("ダウンロード"),
    "Due" : MessageLookupByLibrary.simpleMessage("期限"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("緊急事態です！"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("エレクトリック、ブルー"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("E メールアドレス"),
    "Email:" : MessageLookupByLibrary.simpleMessage("E メール："),
    "Email…" : MessageLookupByLibrary.simpleMessage("メールアドレス..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("提供されている受講生ペアリングコードを入力してください。ペアリングコードが機能しない場合は、有効期限が切れているかもしれません"),
    "Event" : MessageLookupByLibrary.simpleMessage("イベント"),
    "Excused" : MessageLookupByLibrary.simpleMessage("免除"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("期限切れのQRコード"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("失敗しました。タップしてオプションを表示してください。"),
    "Filter" : MessageLookupByLibrary.simpleMessage("フィルタ"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("フィルタ条件"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("ファイアー、オレンジ"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("フロントページ"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("フルネーム"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("フルネーム..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("完全なエラーメッセージ"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("今日に進む"),
    "Grade" : MessageLookupByLibrary.simpleMessage("成績"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("成績パーセンテージ"),
    "Graded" : MessageLookupByLibrary.simpleMessage("採点済み"),
    "Grades" : MessageLookupByLibrary.simpleMessage("成績"),
    "Help" : MessageLookupByLibrary.simpleMessage("ヘルプ"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("パスワードを非表示にする"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("高コントラストモード"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("私たちはどうですか？"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("これはあなたにどのような影響を与えていますか？"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("返事がくるまで何もできません。"),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("Canvasアカウントを持っていません"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("Canvasアカウントを持っています"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("急ぎではありませんが、サポートが必要です。"),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("ログインできません"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("Canvas Parent アプリ [Android] へのアイデア"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("より良いエクスペリエンスを提供できるように、リマインダの機能を更新しました。新しいリマインダを追加するには、課題またはカレンダーの予定を表示し、[リマインダ]セクションのスイッチをタップします。\n\nこのアプリの古いバージョンで作成されたリマインダは、新しい変更と互換性がなく、再度作成する必要があるので注意してください。"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("受信トレイ"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("受信トレイゼロ"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("未完了"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("不正なドメイン"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("学校のお知らせ"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("学校のお知らせ"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("指示"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("このページでのやり取りは、学校によって制限されています。"),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("無効なQRコード"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("休息をとってリラックスし、充電するためにぴったりな日のようです。"),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("このスペースでは、まだ課題が作成されていないようです。"),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("ちょっとした疑問、コメント、アイデア、提案です…"),
    "Late" : MessageLookupByLibrary.simpleMessage("提出遅れ"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("外部ツールを起動する"),
    "Legal" : MessageLookupByLibrary.simpleMessage("法令"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("ライトモード"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("リンクエラー"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("ロケール："),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("QR コードを探す"),
    "Location" : MessageLookupByLibrary.simpleMessage("場所"),
    "Locked" : MessageLookupByLibrary.simpleMessage("ロックされています"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("ログアウト"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("ログインフロー：Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("ログインフロー：標準"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("ログインフロー：サイト管理者"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("ログインフロー：モバイル検証を省略する"),
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
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("Open Canvas受講生"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("ブラウザで開く"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("別のアプリで開く"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("ペアリングコード"),
    "Password" : MessageLookupByLibrary.simpleMessage("パスワード"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("パスワードは必須です"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("パスワードは最低8文字なければなりません"),
    "Password…" : MessageLookupByLibrary.simpleMessage("パスワード..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("計画者メモ"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("有効なメールアドレスを記入してください"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("メールアドレスを入力してください"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("姓名を入力してください"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("Canvas によって生成された QR コードをスキャンしてください"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("プラム、パープル"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("準備しています…"),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("以前のログイン"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("個人情報保護方針"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("個人情報保護方針リンク"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("プライバシーポリシー、利用規約、オープンソース"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("QR コード"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("QRスキャンにはカメラへのアクセスが必要です"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("ラズベリー、レッド"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("受信者"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("更新"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("リマインドする"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("リマインダ"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("リマインダが変更されました！"),
    "Reply" : MessageLookupByLibrary.simpleMessage("返信"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("全員に返信"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("問題を報告する"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("ログインサポートをリクエストする"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("ログインサポートのリクエストボタン"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("アプリを再起動"),
    "Retry" : MessageLookupByLibrary.simpleMessage("再試行"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("ログインに戻る"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("受講生"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("ブラウザ内の QR コード生成の場所を示すスクリーンショット"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("Canvas StudentアプリでのQRコード生成のペアリングの場所を示すスクリーンショット"),
    "Select" : MessageLookupByLibrary.simpleMessage("選択"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("受講生の色を選択する"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("受信者を選択する"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("フィードバックを送信する"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("この課題についてメッセージを送信する"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("このコースに関するメッセージを送信する"),
    "Send message" : MessageLookupByLibrary.simpleMessage("メッセージを送信する"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("このイベントの通知を受ける日付と時間を設定します。"),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("この特定の課題について通知を受ける日付と時間を設定します。"),
    "Settings" : MessageLookupByLibrary.simpleMessage("設定"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("シャムロック、グリーン"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("アプリへの愛を共有しましょう"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("パスワードを表示する"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("サインイン"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("何かが機能していないのですが、必要なことはそれを使わずに実行できます。"),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("ユーザーとしての機能を停止する"),
    "Student" : MessageLookupByLibrary.simpleMessage("受講者"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("受講生のペアリング"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("受講生はモバイルデバイスのCanvas Studentアプリを使用してQRコードを作成できます"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("受講生はCanvas Webサイトからペアリングコードを取得できます"),
    "Subject" : MessageLookupByLibrary.simpleMessage("件名"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("提出済み"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("正常に提出されました！"),
    "Summary" : MessageLookupByLibrary.simpleMessage("概要"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("ユーザーを切り替える"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("シラバス"),
    "TA" : MessageLookupByLibrary.simpleMessage("TA"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("講師"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("タップして予定表に表示したいコースをお気に入りにします。10まで選んでください。"),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("タップして新しい受講生とペアにします"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("タップしてこの受講生を選択します"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("タップして受講生セレクタを表示します"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("講師"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("このアプリの好きな部分を教えてください"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("サービス利用規約"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("サービス利用規約リンク"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("利用規約"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("スキャンしたQRコードの有効期限が切れている可能性があります。受講生のデバイスでコードを更新して、もう一度やり直してください。"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("次の情報は、Canvas があなたのアイデアをより良く理解するために役立ちます："),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("入力されたサーバーは、このアプリ用に許可されていません。"),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("追加しようとしている受講生は、別の学校に所属しています。このコードをスキャンするには、その学校にログインするかアカウントを作成してください。"),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("このアプリのユーザーエージェントは許可されていません。"),
    "Theme" : MessageLookupByLibrary.simpleMessage("テーマ"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("このファイルを開くことができるアプリケーションがインストールされていません"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("利用できるページ情報はありません。"),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("利用規約のロード中に問題が発生しました"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("この受講生をアカウントから削除する際に問題が発生しました。接続を確認して、もう一度お試しください。"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("このコースの受信者のロード中にエラーが発生しました。"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("このコースのサマリー詳細のロード中にエラーが発生しました。"),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("このお知らせのロード中にエラーが発生しました"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("この会話のロード中にエラーが発生しました"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("このファイルのロード中にエラーが発生しました"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("受信トレイメッセージのロード中にエラーが発生しました。"),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("受講生のアラートのロード中にエラーが発生しました"),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("受講生の予定表のロード中にエラーが発生しました"),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("受講生のロード中にエラーが発生しました。"),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("受講生のコースを読み込み中にエラーが発生しました。"),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("ログイン中にエラーが発生しました。別の QR コードを生成して再試行してください。"),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("このユーザーとして機能しようとしてエラーが発生しました。ドメインとユーザー ID をチェックしてから、再試行してください。"),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("通知する事柄はまだ何もありません。"),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("このアプリは使用を許可されていません。"),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("このコースには、まだ課題または予定表イベントがありません。"),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("このファイルはサポートされておらず、アプリで表示できません"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("これにより、この受講生の登録がすべて解除され、アカウントから削除されます。"),
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
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("ウェブコンテンツでダークテーマを使用する"),
    "User ID" : MessageLookupByLibrary.simpleMessage("ユーザー ID"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("ユーザー ID："),
    "Version Number" : MessageLookupByLibrary.simpleMessage("バージョン番号"),
    "View Description" : MessageLookupByLibrary.simpleMessage("説明を表示"),
    "View error details" : MessageLookupByLibrary.simpleMessage("エラーの詳細を表示"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("プライバシーポリシーを表示する"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("現在、皆さんにお楽しみいただく機能を構築中です。"),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("このリンクを表示することができません。このリンクは、現在ログインしていない学校のものである可能性があります。"),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("このアカウントに関連付けられている受講生を見つけることができませんでした"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("このアプリで使用するサーバーを検証できませんでした。"),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("何が起こったかはわかりませんが、問題が発生したようです。問題が解決されない場合は、Canvas までお問い合わせください。"),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("改善する余地はどこにあるでしょう？"),
    "Yes" : MessageLookupByLibrary.simpleMessage("はい"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("どの受講生もオブザーブしていません。"),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("表示するカレンダーは10個だけ選択できます"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("ユーザー ID を入力する必要があります"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("有効なドメインを入力する必要があります"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("少なくともひとつのカレンダーを選択しなければなりません。"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("…にこの課題に関する通知を受けます。"),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("次の予定でこのイベントに関する通知を受けます…"),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("QRコードは、アカウントプロファイルのウェブ上にあります。リストの[モバイルログインのQR]をクリックしてください。"),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("続行するには、受講生のCanvas Studentアプリを開く必要があります。メインメニュー > [設定] > [オブザーバーとのペアリング]に移動し、そこに表示されるQRコードをスキャンしてください。"),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("コードが間違っているか、期限が切れています。"),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("受講生のコースはまだ公開されていない可能性があります"),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("コンテンツは以上です！"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("アラート"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("予定表"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas ガイド"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("Canvas ロゴ"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas サポート"),
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("折りたたむ"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("折りたたみ"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("コース"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("私の学校または学区はどのように見つければよいですか？"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("学校名または学区を入力してください…"),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("さらに表示する"),
    "expanded" : MessageLookupByLibrary.simpleMessage("拡大"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("学校を探す"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("私"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("マイナス"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("次へ"),
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
    "send" : MessageLookupByLibrary.simpleMessage("送信"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("未読"),
    "unreadCount" : m47
  };
}
