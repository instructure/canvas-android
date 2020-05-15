// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a ar locale. All the
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
  String get localeName => 'ar';

  static m0(userName) => "أنت تتصرف باعتبارك ${userName}";

  static m1(version) => "إصدار ${version}";

  static m2(threshold) => "تقييم المهمة أعلاه ${threshold}";

  static m3(threshold) => "تقييم المهمة أدناه ${threshold}";

  static m4(moduleName) => "هذه المهمة مؤمّنة بسبب الوحدة المنطقية \"${moduleName}\".";

  static m5(studentName, assignmentName) => "بخصوص: ${studentName}، المهمة - ${assignmentName}";

  static m6(points) => "${points} نقاط";

  static m7(points) => "${points} من النقاط";

  static m8(authorName, howMany) => "${Intl.plural(howMany, one: '${authorName} إلى 1 آخرين', other: '${authorName} إلى ${howMany} آخرين')}";

  static m9(authorName, recipientName) => "${authorName} إلى ${recipientName}";

  static m10(authorName, recipientName, howMany) => "${Intl.plural(howMany, one: '${authorName} إلى ${recipientName} و 1 أخرى', other: '${authorName} آخر إلى ${recipientName} و ${howMany} أخرى')}";

  static m11(count) => "${count}+";

  static m12(score, pointsPossible) => "${score} من إجمالي ${pointsPossible} نقاط";

  static m13(studentShortName) => "بالنسبة لـ ${studentShortName}";

  static m14(threshold) => "تقييم المساق أعلاه ${threshold}";

  static m15(threshold) => "تقييم المساق أدناه ${threshold}";

  static m16(date, time) => "${date} في ${time}";

  static m17(canvasGuides, canvasSupport) => "جرب البحث عن اسم المدرسة أو الدائرة التعليمية التي تحاول الوصول إليها مثل \"Smith Private School\" أو \"Smith County Schools\". يمكنك أيضاً الدخول في مجال Canvas مباشرة، مثل \"smith.instructure.com.\"\n\nللمزيد من المعلومات حول العثور على حساب Canvas للمؤسسة التي تبحث عنها، يمكنك زيارة ${canvasGuides} أو التواصل مع ${canvasSupport} أو الاتصال بمدرستك لطلب المساعدة.";

  static m18(date, time) => "تاريخ الاستحقاق ${date} في ${time}";

  static m19(userName) => "ستتوقف عن التصرف باعتبارك ${userName} وسيتم إخراجك من الحساب.";

  static m20(userName) => "ستتوقف عن التصرف باعتبارك ${userName} وستعود إلى حسابك الأصلي.";

  static m21(studentName, eventTitle) => "بخصوص: ${studentName}، الحدث - ${eventTitle}";

  static m22(startAt, endAt) => "${startAt} - ${endAt}";

  static m23(grade) => "الدرجة النهائية: ${grade}";

  static m24(studentName) => "بخصوص: ${studentName}، الصفحة الأمامية";

  static m25(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m26(studentName) => "بخصوص: ${studentName}، الدرجات";

  static m27(pointsLost) => "عقوبة التأخير (-${pointsLost})";

  static m28(studentName, linkUrl) => "بخصوص: ${studentName}، ${linkUrl}";

  static m29(percentage) => "يجب أن تزيد عن ${percentage}";

  static m30(percentage) => "يجب أن تقل عن ${percentage}";

  static m31(month) => "الشهر القادم: ${month}";

  static m32(date) => "الأسبوع القادم بدءاً من ${date}";

  static m33(query) => "يتعذر العثور على مدارس مطابقة \"${query}\"";

  static m34(points, howMany) => "${Intl.plural(howMany, one: 'من إجمالي 1 نقطة', other: 'من إجمالي ${points} نقاط')}";

  static m35(count) => "+${count}";

  static m36(points) => "${points} نقاط ممكنة";

  static m37(month) => "الشهر السابق: ${month}";

  static m38(date) => "الأسبوع الماضي بدءاً من ${date}";

  static m39(month) => "شهر ${month}";

  static m40(date, time) => "تم إرسال هذه المهمة في ${date} ${time} وبانتظار التقييم";

  static m41(studentName) => "بخصوص: ${studentName}، المخطط الدراسي";

  static m42(count) => "${count} غير مقروء";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"تصرف كـ\" هو تسجيل الدخول كما لو كنت هذا المستخدم دون كلمة مرور. ستتمكن من اتخاذ أي إجراء كما لو كنت هذا المستخدم، ومن وجهة نظر المستخدمين الآخرين، سيبدو الأمر كما لو كان هذا المستخدم هو من قام بهذه الإجراءات. ولكن سجلات التدقيق ستسجل أنك كنت الشخص الذي قام بالإجراءات نيابةً عن هذا المستخدم."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("يجب توفير وصف."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("يجب توفر موضوع."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("تصرف كمستخدم"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("إضافة طالب"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("إضافة مرفق"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("إضافة طالب جديد"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("إضافة طالب بـ..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("إعدادات التنبيه"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("تنبيهي عندما..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("جميع فترات تقييم الدرجات"),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("يجب توفير عنوان بريد إلكتروني."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء محاولة عرض هذا الرابط"),
    "An unexpected error occurred" : MessageLookupByLibrary.simpleMessage("حدث خطأ غير متوقع"),
    "Android OS version" : MessageLookupByLibrary.simpleMessage("إصدار نظام تشغيل Android"),
    "Appearance" : MessageLookupByLibrary.simpleMessage("المظهر"),
    "Application version" : MessageLookupByLibrary.simpleMessage("إصدار التطبيق"),
    "Are you a student or teacher?" : MessageLookupByLibrary.simpleMessage("هل أنت طالب أو معلم؟"),
    "Are you sure you want to log out?" : MessageLookupByLibrary.simpleMessage("هل تريد بالتأكيد تسجيل الخروج؟"),
    "Are you sure you wish to close this page? Your unsent message will be lost." : MessageLookupByLibrary.simpleMessage("هل ترغب بالتأكيد في إغلاق هذه الصفحة؟ سيتم فقد رسالتك غير المرسلة."),
    "Assignment Details" : MessageLookupByLibrary.simpleMessage("تفاصيل المهمة"),
    "Assignment grade above" : MessageLookupByLibrary.simpleMessage("تقييم المهمة أعلاه"),
    "Assignment grade below" : MessageLookupByLibrary.simpleMessage("تقييم المهمة أدناه"),
    "Assignment missing" : MessageLookupByLibrary.simpleMessage("مهمة مفقودة"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("التقويمات"),
    "Cancel" : MessageLookupByLibrary.simpleMessage("إلغاء"),
    "Canvas Student" : MessageLookupByLibrary.simpleMessage("Canvas Student"),
    "Canvas Teacher" : MessageLookupByLibrary.simpleMessage("Canvas Teacher"),
    "Canvas on GitHub" : MessageLookupByLibrary.simpleMessage("Canvas على GitHub"),
    "Choose a course to message" : MessageLookupByLibrary.simpleMessage("اختر مساقاً لإرسال رسالة"),
    "Choose from Gallery" : MessageLookupByLibrary.simpleMessage("اختر من المعرض"),
    "Complete" : MessageLookupByLibrary.simpleMessage("مكتمل"),
    "Contact Support" : MessageLookupByLibrary.simpleMessage("اتصل بالدعم"),
    "Course Announcement" : MessageLookupByLibrary.simpleMessage("إعلان المساق"),
    "Course Announcements" : MessageLookupByLibrary.simpleMessage("إعلانات المساق"),
    "Course grade above" : MessageLookupByLibrary.simpleMessage("تقييم المساق أعلاه"),
    "Course grade below" : MessageLookupByLibrary.simpleMessage("تقييم المساق أدناه"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("وضع داكن"),
    "Date" : MessageLookupByLibrary.simpleMessage("التاريخ"),
    "Delete" : MessageLookupByLibrary.simpleMessage("حذف"),
    "Description" : MessageLookupByLibrary.simpleMessage("الوصف"),
    "Device" : MessageLookupByLibrary.simpleMessage("الجهاز"),
    "Device model" : MessageLookupByLibrary.simpleMessage("طراز الجهاز"),
    "Domain" : MessageLookupByLibrary.simpleMessage("المجال"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("المجال:"),
    "Done" : MessageLookupByLibrary.simpleMessage("تم"),
    "Download" : MessageLookupByLibrary.simpleMessage("تنزيل"),
    "Due" : MessageLookupByLibrary.simpleMessage("الاستحقاق"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("حالة طارئة حرجة للغاية!!"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("عنوان البريد الإلكتروني"),
    "Email:" : MessageLookupByLibrary.simpleMessage("البريد الإلكتروني:"),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("أدخل رمز إقران الطالب المقدم لك. إذا لم يعمل رمز الإقران، فقد يكون انتهت صلاحيته"),
    "Event" : MessageLookupByLibrary.simpleMessage("الحدث"),
    "Excused" : MessageLookupByLibrary.simpleMessage("معفى"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("فشل. اضغط للوصول إلى الخيارات."),
    "Filter" : MessageLookupByLibrary.simpleMessage("عامل تصفية"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("التصفية حسب"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("الصفحة الأمامية"),
    "Full error message" : MessageLookupByLibrary.simpleMessage("رسالة الخطأ الكاملة"),
    "Grade" : MessageLookupByLibrary.simpleMessage("الدرجة"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("النسبة المئوية للدرجة"),
    "Graded" : MessageLookupByLibrary.simpleMessage("تم تقييم الدرجة"),
    "Grades" : MessageLookupByLibrary.simpleMessage("الدرجات"),
    "Help" : MessageLookupByLibrary.simpleMessage("المساعدة"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("وضع عالي التباين"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("كيف يؤثر عليك ذلك؟"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("لا يمكنني القيام بأي شيء ما لم أحصل على رد منكم."),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("أحتاج لبعض المساعدة لكن الأمر غير عاجل."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("لدي مشكلة في تسجيل الدخول"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("أفكار لتطبيق Canvas Parent [Android]"),
    "Inbox" : MessageLookupByLibrary.simpleMessage("علبة الوارد"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("علبة الوارد صفر"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("غير مكتمل"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("إعلان المؤسسة"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("إعلانات المؤسسة"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("الإرشادات"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("التفاعلات الموجودة في هذه الصفحة مقيدة بمؤسستك."),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("يبدو أنه يوم رائع للراحة والاسترخاء وتجديد النشاط."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("يبدو أنه لم يتم إنشاء مهام في هذه المساحة حتى الآن."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("مجرد سؤال عابر أو تعليق أو فكرة أو اقتراح..."),
    "Late" : MessageLookupByLibrary.simpleMessage("متأخر"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("تشغيل الأداة الخارجية"),
    "Legal" : MessageLookupByLibrary.simpleMessage("القانوني"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("وضع فاتح"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("خطأ في الرابط"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("الإعدادات المحلية"),
    "Location" : MessageLookupByLibrary.simpleMessage("الموقع"),
    "Locked" : MessageLookupByLibrary.simpleMessage("مؤمّن"),
    "Log Out" : MessageLookupByLibrary.simpleMessage("تسجيل الخروج"),
    "Login flow: Canvas" : MessageLookupByLibrary.simpleMessage("تدفق تسجيل الدخول: Canvas"),
    "Login flow: Normal" : MessageLookupByLibrary.simpleMessage("تدفق تسجيل الدخول: عادي"),
    "Login flow: Site Admin" : MessageLookupByLibrary.simpleMessage("تدفق تسجيل الدخول: مسؤول الموقع"),
    "Login flow: Skip mobile verify" : MessageLookupByLibrary.simpleMessage("تدفق تسجيل الدخول: تخطي التحقق من الجوّال"),
    "Manage Students" : MessageLookupByLibrary.simpleMessage("إدارة الطلاب"),
    "Message" : MessageLookupByLibrary.simpleMessage("الرسالة"),
    "Message subject" : MessageLookupByLibrary.simpleMessage("موضوع الرسالة"),
    "Missing" : MessageLookupByLibrary.simpleMessage("مفقود"),
    "Must be below 100" : MessageLookupByLibrary.simpleMessage("يجب أن تقل عن 100"),
    "Network error" : MessageLookupByLibrary.simpleMessage("خطأ في الشبكة"),
    "Never" : MessageLookupByLibrary.simpleMessage("أبدًا"),
    "New message" : MessageLookupByLibrary.simpleMessage("رسالة جديدة"),
    "No" : MessageLookupByLibrary.simpleMessage("لا"),
    "No Alerts" : MessageLookupByLibrary.simpleMessage("لا توجد تنبيهات"),
    "No Assignments" : MessageLookupByLibrary.simpleMessage("لا توجد أي مهام"),
    "No Courses" : MessageLookupByLibrary.simpleMessage("لا يوجد أي مساق"),
    "No Due Date" : MessageLookupByLibrary.simpleMessage("لا يوجد تاريخ استحقاق"),
    "No Events Today!" : MessageLookupByLibrary.simpleMessage("لا توجد أحداث اليوم!"),
    "No Grade" : MessageLookupByLibrary.simpleMessage("لا توجد أي درجة"),
    "No Location Specified" : MessageLookupByLibrary.simpleMessage("لم يتم تحديد أي موقع"),
    "No Students" : MessageLookupByLibrary.simpleMessage("لا يوجد طلاب"),
    "No Subject" : MessageLookupByLibrary.simpleMessage("لا يوجد موضوع"),
    "No Summary" : MessageLookupByLibrary.simpleMessage("لا يوجد ملخص"),
    "No description" : MessageLookupByLibrary.simpleMessage("بلا وصف"),
    "No recipients selected" : MessageLookupByLibrary.simpleMessage("لم يتم تحديد متلقين"),
    "Not Graded" : MessageLookupByLibrary.simpleMessage("لم يتم التقييم"),
    "Not Submitted" : MessageLookupByLibrary.simpleMessage("لم يتم الإرسال"),
    "Not a parent?" : MessageLookupByLibrary.simpleMessage("لست ولي أمر؟"),
    "Notifications for reminders about assignments and calendar events" : MessageLookupByLibrary.simpleMessage("إعلامات لتذكيرات حول المهام وأحداث التقويم"),
    "OS Version" : MessageLookupByLibrary.simpleMessage("إصدار نظام التشغيل"),
    "Observer" : MessageLookupByLibrary.simpleMessage("المراقب"),
    "One of our other apps might be a better fit. Tap one to visit the Play Store." : MessageLookupByLibrary.simpleMessage("ربما تكون إحدى تطبيقاتنا الأخرى مناسبة بشكل أفضل. اضغط على واحد لزيارة Play Store."),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("الفتح في المستعرض"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("فتح بواسطة تطبيق آخر"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("رمز الإقران"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("جارٍ الإعداد..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("عمليات تسجيل الدخول السابقة"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("سياسة الخصوصية"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("سياسة الخصوصية، شروط الاستخدام، المصدر المفتوح"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("رمز QR"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("المتلقون"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("ذكرني"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("التذكيرات"),
    "Reply" : MessageLookupByLibrary.simpleMessage("رد"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("رد على الكل"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("الإبلاغ عن مشكلة"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("طلب مساعدة لتسجيل الدخول"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("زر طلب مساعدة لتسجيل الدخول"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("إعادة تشغيل التطبيق"),
    "Retry" : MessageLookupByLibrary.simpleMessage("إعادة المحاولة"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("العودة إلى تسجيل الدخول"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("الطالب"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("تحديد متلقين"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("إرسال رسالة حول هذه المهمة"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("إرسال رسالة حول هذا المساق"),
    "Send message" : MessageLookupByLibrary.simpleMessage("إرسال رسالة"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("تعيين تاريخ ووقت للإعلام بهذا الحدث."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("تعيين تاريخ ووقت للإعلام بهذه المهمة المحددة."),
    "Settings" : MessageLookupByLibrary.simpleMessage("الإعدادات"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("شارك إعجابك بالتطبيق"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("شيء ما معطل لكن بإمكاني إتمام المهام التي يجب أن أقوم بإنجازها."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("إيقاف التصرف كمستخدم"),
    "Student" : MessageLookupByLibrary.simpleMessage("الطالب"),
    "Subject" : MessageLookupByLibrary.simpleMessage("الموضوع"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("تم الإرسال"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("تم الإرسال بنجاح!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("ملخص"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("تبديل المستخدمين"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("المناهج"),
    "TA" : MessageLookupByLibrary.simpleMessage("مساعد المعلم"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("المعلم"),
    "Tap to favorite the courses you want to see on the Calendar." : MessageLookupByLibrary.simpleMessage("اضغط إضافة المساقات إلى المفضلة التي ترغب في رؤيتها على التقويم."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("اضغط لإقران مع طالب جديد"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("اضغط لتحديد هذا الطالب"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("اضغط لإظهار أداة تحديد الطلاب"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("المعلم"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("أخبرنا بالأشياء التي تفضلها في التطبيق"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("شروط الاستخدام"),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("ستساعدنا المعلومات التالية على فهم فكرتك بشكل أفضل:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("الخادم الذي أدخلته غير مخول لهذا التطبيق."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("وكيل المستخدم لهذا التطبيق غير مخوَّل."),
    "Theme" : MessageLookupByLibrary.simpleMessage("النسق"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("لا توجد تطبيقات مثبتة يمكن من خلالها فتح هذا الملف"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("لا تتوفر أي صفحة معلومات"),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("حدثت مشكلة أثناء تحميل شروط الاستخدام"),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل متلقين لهذا المساق"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل تفاصيل الملخص لهذا المساق."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل هذا الإعلان"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل هذه المحادثة"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل هذا الملف"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل رسائلك في علبة الوارد."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل تنبيهات الطالب."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل تقويم الطالب."),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل طلابك."),
    "There was an error loading your your student’s courses." : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل مساقات الطالب."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("حدث خطأ ما في التصرف باعتبارك هذا المستخدم. يرجى التحقق من المجال ومعرف المستخدم وإعادة المحاولة."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("لا يوجد شيء للإعلام عنه حتى الآن."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("هذا التطبيق غير مخول للاستخدام."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("لا يحتوي هذا المساق على أي مهام أو أحداث بالتقويم حتى الآن."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("هذا الملف غير مدعوم ولا يمكن عرضه من خلال التطبيق"),
    "Total Grade" : MessageLookupByLibrary.simpleMessage("التقدير الإجمالي"),
    "Uh oh!" : MessageLookupByLibrary.simpleMessage("عذرًا!"),
    "Unable to fetch courses. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("يتعذر إحضار المساقات. يرجى التحقق من الاتصال وإعادة المحاولة."),
    "Unable to load this image" : MessageLookupByLibrary.simpleMessage("يتعذر تحميل هذه الصورة"),
    "Unable to play this media file" : MessageLookupByLibrary.simpleMessage("يتعذر تشغيل ملف الوسائط هذا"),
    "Unable to send message. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("يتعذر إرسال رسالة. تحقق من الاتصال وأعد المحاولة."),
    "Under Construction" : MessageLookupByLibrary.simpleMessage("تحت الإنشاء"),
    "Unknown User" : MessageLookupByLibrary.simpleMessage("مستخدم غير معروف"),
    "Unsaved changes" : MessageLookupByLibrary.simpleMessage("تغييرات غير محفوظة"),
    "Unsupported File" : MessageLookupByLibrary.simpleMessage("الملف غير مدعوم"),
    "Upload File" : MessageLookupByLibrary.simpleMessage("تحميل ملف"),
    "Use Camera" : MessageLookupByLibrary.simpleMessage("استخدام الكاميرا"),
    "User ID" : MessageLookupByLibrary.simpleMessage("معرف المستخدم"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("معرف المستخدم:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("رقم الإصدار"),
    "View error details" : MessageLookupByLibrary.simpleMessage("عرض تفاصيل الخطأ"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("نقوم في الوقت الحالي ببناء هذه الميزة لتستمتع بالعرض."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("لم يمكننا عرض هذا الرابط، قد يكون تابعاً لمؤسسة لم تسجل الدخول إليها في الوقت الحالي."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("تعذر علينا العثور على أي طلاب مقترنين بهذا الحساب"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("لم نتمكن من التحقق من الخادم لاستخدامه مع هذا التطبيق."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("لا نعلم على وجه الدقة بما حدث، ولكنه لم يكن أمراً جيداً. اتصل بنا إذا استمر هذا في الحدوث."),
    "Yes" : MessageLookupByLibrary.simpleMessage("نعم"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("أنت لا تراقب أي طالب."),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("يجب أن تدخل معرف مستخدم"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("يجب عليك إدخال مجال صالح"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("سيتم إعلامك بهذه المهمة في..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("سيتم إعلامك بهذا الحدث في..."),
    "Your code is incorrect or expired." : MessageLookupByLibrary.simpleMessage("رمزك غير صحيح أو منته الصلاحية."),
    "Your student’s courses might not be published yet." : MessageLookupByLibrary.simpleMessage("ربما لم يتم نشر مساقات الطالب بعد."),
    "You’re all caught up!" : MessageLookupByLibrary.simpleMessage("لقد اطلعت عليها جميعًا!"),
    "actingAsUser" : m0,
    "alertsLabel" : MessageLookupByLibrary.simpleMessage("التنبيهات"),
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
    "calendarLabel" : MessageLookupByLibrary.simpleMessage("التقويم"),
    "canvasGuides" : MessageLookupByLibrary.simpleMessage("Canvas Guides"),
    "canvasLogoLabel" : MessageLookupByLibrary.simpleMessage("شعار Canvas"),
    "canvasSupport" : MessageLookupByLibrary.simpleMessage("Canvas Support"),
    "collapse" : MessageLookupByLibrary.simpleMessage("طي"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("تم الطي"),
    "contentDescriptionScoreOutOfPointsPossible" : m12,
    "courseForWhom" : m13,
    "courseGradeAboveThreshold" : m14,
    "courseGradeBelowThreshold" : m15,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("المساقات"),
    "dateAtTime" : m16,
    "domainSearchHelpBody" : m17,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("كيف أعثر على مدرستي أو دائرتي التعليمية؟"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("أدخل اسم المدرسة أو الدائرة التعليمية..."),
    "dueDateAtTime" : m18,
    "endMasqueradeLogoutMessage" : m19,
    "endMasqueradeMessage" : m20,
    "eventSubjectMessage" : m21,
    "eventTime" : m22,
    "expand" : MessageLookupByLibrary.simpleMessage("توسيع"),
    "expanded" : MessageLookupByLibrary.simpleMessage("تم التوسيع"),
    "finalGrade" : m23,
    "findSchool" : MessageLookupByLibrary.simpleMessage("البحث عن مدرسة"),
    "frontPageSubjectMessage" : m24,
    "gradeFormatScoreOutOfPointsPossible" : m25,
    "gradesSubjectMessage" : m26,
    "latePenalty" : m27,
    "me" : MessageLookupByLibrary.simpleMessage("أنا"),
    "messageLinkPostscript" : m28,
    "minus" : MessageLookupByLibrary.simpleMessage("سالب"),
    "mustBeAboveN" : m29,
    "mustBeBelowN" : m30,
    "next" : MessageLookupByLibrary.simpleMessage("التالي"),
    "nextMonth" : m31,
    "nextWeek" : m32,
    "noDomainResults" : m33,
    "ok" : MessageLookupByLibrary.simpleMessage("موافق"),
    "outOfPoints" : m34,
    "plusRecipientCount" : m35,
    "pointsPossible" : m36,
    "previousMonth" : m37,
    "previousWeek" : m38,
    "selectedMonthLabel" : m39,
    "send" : MessageLookupByLibrary.simpleMessage("إرسال"),
    "submissionStatusSuccessSubtitle" : m40,
    "syllabusSubjectMessage" : m41,
    "unread" : MessageLookupByLibrary.simpleMessage("غير مقروء"),
    "unreadCount" : m42
  };
}
