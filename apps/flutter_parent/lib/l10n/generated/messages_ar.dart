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

  static m12(studentName) => "تغيير اللون لـ ${studentName}";

  static m13(score, pointsPossible) => "${score} من إجمالي ${pointsPossible} نقاط";

  static m14(studentShortName) => "بالنسبة لـ ${studentShortName}";

  static m15(threshold) => "تقييم المساق أعلاه ${threshold}";

  static m16(threshold) => "تقييم المساق أدناه ${threshold}";

  static m17(date, time) => "${date} في ${time}";

  static m18(alertTitle) => "تجاهل ${alertTitle}";

  static m19(canvasGuides, canvasSupport) => "جرب البحث عن اسم المدرسة أو الدائرة التعليمية التي تحاول الوصول إليها مثل \"Smith Private School\" أو \"Smith County Schools\". يمكنك أيضاً الدخول في مجال Canvas مباشرة، مثل \"smith.instructure.com.\"\n\nللمزيد من المعلومات حول العثور على حساب Canvas للمؤسسة التي تبحث عنها، يمكنك زيارة ${canvasGuides} أو التواصل مع ${canvasSupport} أو الاتصال بمدرستك لطلب المساعدة.";

  static m20(date, time) => "تاريخ الاستحقاق ${date} في ${time}";

  static m21(userName) => "ستتوقف عن التصرف باعتبارك ${userName} وسيتم إخراجك من الحساب.";

  static m22(userName) => "ستتوقف عن التصرف باعتبارك ${userName} وستعود إلى حسابك الأصلي.";

  static m23(studentName, eventTitle) => "بخصوص: ${studentName}، الحدث - ${eventTitle}";

  static m24(startAt, endAt) => "${startAt} - ${endAt}";

  static m25(grade) => "الدرجة النهائية: ${grade}";

  static m26(studentName) => "بخصوص: ${studentName}، الصفحة الأمامية";

  static m27(score, pointsPossible) => "${score} / ${pointsPossible}";

  static m28(studentName) => "بخصوص: ${studentName}، الدرجات";

  static m29(pointsLost) => "عقوبة التأخير (-${pointsLost})";

  static m30(studentName, linkUrl) => "بخصوص: ${studentName}، ${linkUrl}";

  static m31(percentage) => "يجب أن تزيد عن ${percentage}";

  static m32(percentage) => "يجب أن تقل عن ${percentage}";

  static m33(month) => "الشهر القادم: ${month}";

  static m34(date) => "الأسبوع القادم بدءاً من ${date}";

  static m35(query) => "يتعذر العثور على مدارس مطابقة \"${query}\"";

  static m36(points, howMany) => "${Intl.plural(howMany, one: 'من إجمالي 1 نقطة', other: 'من إجمالي ${points} نقاط')}";

  static m37(count) => "+${count}";

  static m38(points) => "${points} نقاط ممكنة";

  static m39(month) => "الشهر السابق: ${month}";

  static m40(date) => "الأسبوع الماضي بدءاً من ${date}";

  static m41(termsOfService, privacyPolicy) => "بالضغط على \"إنشاء حساب\"، أنت توافق على ${termsOfService} وتقر بـ ${privacyPolicy}";

  static m42(version) => "اقتراحات لـ Android - Canvas Parent ${version}";

  static m43(month) => "شهر ${month}";

  static m44(position) => "${Intl.plural(position, one: '${position} نجمة', other: '${position} نجوم')}";

  static m45(date, time) => "تم إرسال هذه المهمة في ${date} ${time} وبانتظار التقييم";

  static m46(studentName) => "بخصوص: ${studentName}، المخطط الدراسي";

  static m47(count) => "${count} غير مقروء";

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "\"Act as\" is essentially logging in as this user without a password. You will be able to take any action as if you were this user, and from other users\' points of views, it will be as if this user performed them. However, audit logs record that you were the one who performed the actions on behalf of this user." : MessageLookupByLibrary.simpleMessage("\"تصرف كـ\" هو تسجيل الدخول كما لو كنت هذا المستخدم دون كلمة مرور. ستتمكن من اتخاذ أي إجراء كما لو كنت هذا المستخدم، ومن وجهة نظر المستخدمين الآخرين، سيبدو الأمر كما لو كان هذا المستخدم هو من قام بهذه الإجراءات. ولكن سجلات التدقيق ستسجل أنك كنت الشخص الذي قام بالإجراءات نيابةً عن هذا المستخدم."),
    "-" : MessageLookupByLibrary.simpleMessage("-"),
    "A description is required." : MessageLookupByLibrary.simpleMessage("يجب توفير وصف."),
    "A network error occurred when adding this student. Check your connection and try again." : MessageLookupByLibrary.simpleMessage("حدث خطأ في الشبكة أثناء إضافة هذا الطالب. تحقق من الاتصال وأعد المحاولة."),
    "A subject is required." : MessageLookupByLibrary.simpleMessage("يجب توفر موضوع."),
    "Act As User" : MessageLookupByLibrary.simpleMessage("تصرف كمستخدم"),
    "Add Student" : MessageLookupByLibrary.simpleMessage("إضافة طالب"),
    "Add attachment" : MessageLookupByLibrary.simpleMessage("إضافة مرفق"),
    "Add new student" : MessageLookupByLibrary.simpleMessage("إضافة طالب جديد"),
    "Add student with…" : MessageLookupByLibrary.simpleMessage("إضافة طالب بـ..."),
    "Alert Settings" : MessageLookupByLibrary.simpleMessage("إعدادات التنبيه"),
    "Alert me when…" : MessageLookupByLibrary.simpleMessage("تنبيهي عندما..."),
    "All Grading Periods" : MessageLookupByLibrary.simpleMessage("جميع فترات تقييم الدرجات"),
    "Already have an account? " : MessageLookupByLibrary.simpleMessage("هل تمتلك حسابًا بالفعل؟ "),
    "An email address is required." : MessageLookupByLibrary.simpleMessage("يجب توفير عنوان بريد إلكتروني."),
    "An error occurred when trying to display this link" : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء محاولة عرض هذا الرابط"),
    "An error occurred while saving your selection. Please try again." : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء حفظ التحديد الخاص بك يرجى إعادة المحاولة."),
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
    "Barney, Fuschia" : MessageLookupByLibrary.simpleMessage("فوشيا، بارني"),
    "Calendars" : MessageLookupByLibrary.simpleMessage("التقويمات"),
    "Camera Permission" : MessageLookupByLibrary.simpleMessage("إذن الكاميرا"),
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
    "Create Account" : MessageLookupByLibrary.simpleMessage("إنشاء حساب"),
    "Dark Mode" : MessageLookupByLibrary.simpleMessage("وضع داكن"),
    "Date" : MessageLookupByLibrary.simpleMessage("التاريخ"),
    "Delete" : MessageLookupByLibrary.simpleMessage("حذف"),
    "Description" : MessageLookupByLibrary.simpleMessage("الوصف"),
    "Device" : MessageLookupByLibrary.simpleMessage("الجهاز"),
    "Device model" : MessageLookupByLibrary.simpleMessage("طراز الجهاز"),
    "Domain" : MessageLookupByLibrary.simpleMessage("المجال"),
    "Domain:" : MessageLookupByLibrary.simpleMessage("المجال:"),
    "Don\'t show again" : MessageLookupByLibrary.simpleMessage("عدم الإظهار مرة أخرى"),
    "Done" : MessageLookupByLibrary.simpleMessage("تم"),
    "Download" : MessageLookupByLibrary.simpleMessage("تنزيل"),
    "Due" : MessageLookupByLibrary.simpleMessage("الاستحقاق"),
    "EXTREME CRITICAL EMERGENCY!!" : MessageLookupByLibrary.simpleMessage("حالة طارئة حرجة للغاية!!"),
    "Electric, blue" : MessageLookupByLibrary.simpleMessage("أزرق، كهربائي"),
    "Email Address" : MessageLookupByLibrary.simpleMessage("عنوان البريد الإلكتروني"),
    "Email:" : MessageLookupByLibrary.simpleMessage("البريد الإلكتروني:"),
    "Email…" : MessageLookupByLibrary.simpleMessage("البريد الإلكتروني..."),
    "Enter the student pairing code provided to you. If the pairing code doesn\'t work, it may have expired" : MessageLookupByLibrary.simpleMessage("أدخل رمز إقران الطالب المقدم لك. إذا لم يعمل رمز الإقران، فقد يكون انتهت صلاحيته"),
    "Event" : MessageLookupByLibrary.simpleMessage("الحدث"),
    "Excused" : MessageLookupByLibrary.simpleMessage("معفى"),
    "Expired QR Code" : MessageLookupByLibrary.simpleMessage("رمز QR منته الصلاحية"),
    "Failed. Tap for options." : MessageLookupByLibrary.simpleMessage("فشل. اضغط للوصول إلى الخيارات."),
    "Filter" : MessageLookupByLibrary.simpleMessage("عامل تصفية"),
    "Filter by" : MessageLookupByLibrary.simpleMessage("التصفية حسب"),
    "Fire, Orange" : MessageLookupByLibrary.simpleMessage("برتقالي، ناري"),
    "Front Page" : MessageLookupByLibrary.simpleMessage("الصفحة الأمامية"),
    "Full Name" : MessageLookupByLibrary.simpleMessage("الاسم الكامل"),
    "Full Name…" : MessageLookupByLibrary.simpleMessage("الاسم الكامل..."),
    "Full error message" : MessageLookupByLibrary.simpleMessage("رسالة الخطأ الكاملة"),
    "Go to today" : MessageLookupByLibrary.simpleMessage("الذهاب إلى اليوم"),
    "Grade" : MessageLookupByLibrary.simpleMessage("الدرجة"),
    "Grade percentage" : MessageLookupByLibrary.simpleMessage("النسبة المئوية للدرجة"),
    "Graded" : MessageLookupByLibrary.simpleMessage("تم تقييم الدرجة"),
    "Grades" : MessageLookupByLibrary.simpleMessage("الدرجات"),
    "Help" : MessageLookupByLibrary.simpleMessage("المساعدة"),
    "Hide Password" : MessageLookupByLibrary.simpleMessage("إخفاء كلمة المرور"),
    "High Contrast Mode" : MessageLookupByLibrary.simpleMessage("وضع عالي التباين"),
    "How are we doing?" : MessageLookupByLibrary.simpleMessage("كيف هو أداؤنا؟"),
    "How is this affecting you?" : MessageLookupByLibrary.simpleMessage("كيف يؤثر عليك ذلك؟"),
    "I can\'t get things done until I hear back from you." : MessageLookupByLibrary.simpleMessage("لا يمكنني القيام بأي شيء ما لم أحصل على رد منكم."),
    "I don\'t have a Canvas account" : MessageLookupByLibrary.simpleMessage("ليس لدي حساب Canvas"),
    "I have a Canvas account" : MessageLookupByLibrary.simpleMessage("لدي حساب Canvas"),
    "I need some help but it\'s not urgent." : MessageLookupByLibrary.simpleMessage("أحتاج لبعض المساعدة لكن الأمر غير عاجل."),
    "I\'m having trouble logging in" : MessageLookupByLibrary.simpleMessage("لدي مشكلة في تسجيل الدخول"),
    "Idea for Canvas Parent App [Android]" : MessageLookupByLibrary.simpleMessage("أفكار لتطبيق Canvas Parent [Android]"),
    "In order to provide you with a better experience, we have updated how reminders work. You can add new reminders by viewing an assignment or calendar event and tapping the switch under the \"Remind Me\" section.\n\nBe aware that any reminders created with older versions of this app will not be compatible with the new changes and you will need to create them again." : MessageLookupByLibrary.simpleMessage("لتقديم تجربة أفضل، قمنا بتحديث طريقة التذكيرات. يمكنك إضافة تذكيرات جديدة عن طريق عرض حدث مهمة أو تقويم والضغط على المفتاح تحت قسم \"ذكرني\".\n\nاعلم أن أي تذكيرات يتم إنشاؤها بإصدارات أقدم من هذا التطبيق ستكون غير متوافقة مع التغييرات الجديدة وستحتاج إلى إنشائها مجدداً."),
    "Inbox" : MessageLookupByLibrary.simpleMessage("علبة الوارد"),
    "Inbox Zero" : MessageLookupByLibrary.simpleMessage("علبة الوارد صفر"),
    "Incomplete" : MessageLookupByLibrary.simpleMessage("غير مكتمل"),
    "Incorrect Domain" : MessageLookupByLibrary.simpleMessage("مجال غير صالح"),
    "Institution Announcement" : MessageLookupByLibrary.simpleMessage("إعلان المؤسسة"),
    "Institution Announcements" : MessageLookupByLibrary.simpleMessage("إعلانات المؤسسة"),
    "Instructions" : MessageLookupByLibrary.simpleMessage("الإرشادات"),
    "Interactions on this page are limited by your institution." : MessageLookupByLibrary.simpleMessage("التفاعلات الموجودة في هذه الصفحة مقيدة بمؤسستك."),
    "Invalid QR Code" : MessageLookupByLibrary.simpleMessage("رمز QR غير صالح"),
    "It looks like a great day to rest, relax, and recharge." : MessageLookupByLibrary.simpleMessage("يبدو أنه يوم رائع للراحة والاسترخاء وتجديد النشاط."),
    "It looks like assignments haven\'t been created in this space yet." : MessageLookupByLibrary.simpleMessage("يبدو أنه لم يتم إنشاء مهام في هذه المساحة حتى الآن."),
    "Just a casual question, comment, idea, suggestion…" : MessageLookupByLibrary.simpleMessage("مجرد سؤال عابر أو تعليق أو فكرة أو اقتراح..."),
    "Late" : MessageLookupByLibrary.simpleMessage("متأخر"),
    "Launch External Tool" : MessageLookupByLibrary.simpleMessage("تشغيل الأداة الخارجية"),
    "Legal" : MessageLookupByLibrary.simpleMessage("القانوني"),
    "Light Mode" : MessageLookupByLibrary.simpleMessage("وضع فاتح"),
    "Link Error" : MessageLookupByLibrary.simpleMessage("خطأ في الرابط"),
    "Locale:" : MessageLookupByLibrary.simpleMessage("الإعدادات المحلية"),
    "Locate QR Code" : MessageLookupByLibrary.simpleMessage("ابحث عن رمز QR"),
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
    "Open Canvas Student" : MessageLookupByLibrary.simpleMessage("افتح Canvas Student"),
    "Open In Browser" : MessageLookupByLibrary.simpleMessage("الفتح في المستعرض"),
    "Open with another app" : MessageLookupByLibrary.simpleMessage("فتح بواسطة تطبيق آخر"),
    "Pairing Code" : MessageLookupByLibrary.simpleMessage("رمز الإقران"),
    "Password" : MessageLookupByLibrary.simpleMessage("كلمة المرور"),
    "Password is required" : MessageLookupByLibrary.simpleMessage("كلمة المرور مطلوبة"),
    "Password must contain at least 8 characters" : MessageLookupByLibrary.simpleMessage("يجب أن تحتوي كلمة المرور على 8 أحرف على الأقل"),
    "Password…" : MessageLookupByLibrary.simpleMessage("كلمة المرور..."),
    "Planner Note" : MessageLookupByLibrary.simpleMessage("ملاحظة المخطِط"),
    "Please enter a valid email address" : MessageLookupByLibrary.simpleMessage("يرجى إدخال عنوان بريد إلكتروني صالح"),
    "Please enter an email address" : MessageLookupByLibrary.simpleMessage("يُرجى إدخال عنوان بريد إلكتروني"),
    "Please enter full name" : MessageLookupByLibrary.simpleMessage("يُرجى إدخال الاسم الكامل"),
    "Please scan a QR code generated by Canvas" : MessageLookupByLibrary.simpleMessage("يرجى مسح رمز QR من إنشاء Canvas"),
    "Plum, Purple" : MessageLookupByLibrary.simpleMessage("أرجواني، برقوقي"),
    "Preparing…" : MessageLookupByLibrary.simpleMessage("جارٍ الإعداد..."),
    "Previous Logins" : MessageLookupByLibrary.simpleMessage("عمليات تسجيل الدخول السابقة"),
    "Privacy Policy" : MessageLookupByLibrary.simpleMessage("سياسة الخصوصية"),
    "Privacy Policy Link" : MessageLookupByLibrary.simpleMessage("ارتباط سياسة الخصوصية"),
    "Privacy policy, terms of use, open source" : MessageLookupByLibrary.simpleMessage("سياسة الخصوصية، شروط الاستخدام، المصدر المفتوح"),
    "QR Code" : MessageLookupByLibrary.simpleMessage("رمز QR"),
    "QR scanning requires camera access" : MessageLookupByLibrary.simpleMessage("مسح رمز QR يتطلب الوصول إلى الكاميرا"),
    "Raspberry, Red" : MessageLookupByLibrary.simpleMessage("أحمر، لون التوت"),
    "Recipients" : MessageLookupByLibrary.simpleMessage("المتلقون"),
    "Refresh" : MessageLookupByLibrary.simpleMessage("تحديث"),
    "Remind Me" : MessageLookupByLibrary.simpleMessage("ذكرني"),
    "Reminders" : MessageLookupByLibrary.simpleMessage("التذكيرات"),
    "Reminders have changed!" : MessageLookupByLibrary.simpleMessage("تم تغيير التذكيرات!"),
    "Reply" : MessageLookupByLibrary.simpleMessage("رد"),
    "Reply All" : MessageLookupByLibrary.simpleMessage("رد على الكل"),
    "Report A Problem" : MessageLookupByLibrary.simpleMessage("الإبلاغ عن مشكلة"),
    "Request Login Help" : MessageLookupByLibrary.simpleMessage("طلب مساعدة لتسجيل الدخول"),
    "Request Login Help Button" : MessageLookupByLibrary.simpleMessage("زر طلب مساعدة لتسجيل الدخول"),
    "Restart app" : MessageLookupByLibrary.simpleMessage("إعادة تشغيل التطبيق"),
    "Retry" : MessageLookupByLibrary.simpleMessage("إعادة المحاولة"),
    "Return to Login" : MessageLookupByLibrary.simpleMessage("العودة إلى تسجيل الدخول"),
    "STUDENT" : MessageLookupByLibrary.simpleMessage("الطالب"),
    "Screenshot showing location of QR code generation in browser" : MessageLookupByLibrary.simpleMessage("التقط لقطة شاشة لإظهار موقع إنشاء رمز QR في المستعرض"),
    "Screenshot showing location of pairing QR code generation in the Canvas Student app" : MessageLookupByLibrary.simpleMessage("لقطة شاشة تظهر موقع إنشاء رمز QR للإقران في تطبيق Canvas Student"),
    "Select" : MessageLookupByLibrary.simpleMessage("تحديد"),
    "Select Student Color" : MessageLookupByLibrary.simpleMessage("حدد اللون المخصص للطالب"),
    "Select recipients" : MessageLookupByLibrary.simpleMessage("تحديد متلقين"),
    "Send Feedback" : MessageLookupByLibrary.simpleMessage("إرسال تعليقات"),
    "Send a message about this assignment" : MessageLookupByLibrary.simpleMessage("إرسال رسالة حول هذه المهمة"),
    "Send a message about this course" : MessageLookupByLibrary.simpleMessage("إرسال رسالة حول هذا المساق"),
    "Send message" : MessageLookupByLibrary.simpleMessage("إرسال رسالة"),
    "Set a date and time to be notified of this event." : MessageLookupByLibrary.simpleMessage("تعيين تاريخ ووقت للإعلام بهذا الحدث."),
    "Set a date and time to be notified of this specific assignment." : MessageLookupByLibrary.simpleMessage("تعيين تاريخ ووقت للإعلام بهذه المهمة المحددة."),
    "Settings" : MessageLookupByLibrary.simpleMessage("الإعدادات"),
    "Shamrock, Green" : MessageLookupByLibrary.simpleMessage("أخضر، النفل"),
    "Share Your Love for the App" : MessageLookupByLibrary.simpleMessage("شارك إعجابك بالتطبيق"),
    "Show Password" : MessageLookupByLibrary.simpleMessage("إظهار كلمة المرور"),
    "Sign In" : MessageLookupByLibrary.simpleMessage("تسجيل الدخول"),
    "Something\'s broken but I can work around it to get what I need done." : MessageLookupByLibrary.simpleMessage("شيء ما معطل لكن بإمكاني إتمام المهام التي يجب أن أقوم بإنجازها."),
    "Stop Acting as User" : MessageLookupByLibrary.simpleMessage("إيقاف التصرف كمستخدم"),
    "Student" : MessageLookupByLibrary.simpleMessage("الطالب"),
    "Student Pairing" : MessageLookupByLibrary.simpleMessage("إقران الطالب"),
    "Students can create a QR code using the Canvas Student app on their mobile device" : MessageLookupByLibrary.simpleMessage("يستطيع الطلاب إنشاء رمز QR باستخدام تطبيق Canvas Student على أجهزتهم المحمولة"),
    "Students can obtain a pairing code through the Canvas website" : MessageLookupByLibrary.simpleMessage("يمكن للطلاب الحصول على رمز إقران من خلال موقع Canvas على الويب."),
    "Subject" : MessageLookupByLibrary.simpleMessage("الموضوع"),
    "Submitted" : MessageLookupByLibrary.simpleMessage("تم الإرسال"),
    "Successfully submitted!" : MessageLookupByLibrary.simpleMessage("تم الإرسال بنجاح!"),
    "Summary" : MessageLookupByLibrary.simpleMessage("ملخص"),
    "Switch Users" : MessageLookupByLibrary.simpleMessage("تبديل المستخدمين"),
    "Syllabus" : MessageLookupByLibrary.simpleMessage("المناهج"),
    "TA" : MessageLookupByLibrary.simpleMessage("مساعد المعلم"),
    "TEACHER" : MessageLookupByLibrary.simpleMessage("المعلم"),
    "Tap to favorite the courses you want to see on the Calendar. Select up to 10." : MessageLookupByLibrary.simpleMessage("اضغط إضافة المساقات إلى المفضلة التي ترغب في رؤيتها على التقويم. تحديد 10 بحد أقصى."),
    "Tap to pair with a new student" : MessageLookupByLibrary.simpleMessage("اضغط لإقران مع طالب جديد"),
    "Tap to select this student" : MessageLookupByLibrary.simpleMessage("اضغط لتحديد هذا الطالب"),
    "Tap to show student selector" : MessageLookupByLibrary.simpleMessage("اضغط لإظهار أداة تحديد الطلاب"),
    "Teacher" : MessageLookupByLibrary.simpleMessage("المعلم"),
    "Tell us about your favorite parts of the app" : MessageLookupByLibrary.simpleMessage("أخبرنا بالأشياء التي تفضلها في التطبيق"),
    "Terms of Service" : MessageLookupByLibrary.simpleMessage("شروط الخدمة"),
    "Terms of Service Link" : MessageLookupByLibrary.simpleMessage("ارتباط شروط الخدمة"),
    "Terms of Use" : MessageLookupByLibrary.simpleMessage("شروط الاستخدام"),
    "The QR code you scanned may have expired. Refresh the code on the student\'s device and try again." : MessageLookupByLibrary.simpleMessage("ربما يكون رمز QR الذي مسحته انتهت صلاحيته. قم بتحديث الرمز على جهاز الطالب وأعد المحاولة."),
    "The following information will help us better understand your idea:" : MessageLookupByLibrary.simpleMessage("ستساعدنا المعلومات التالية على فهم فكرتك بشكل أفضل:"),
    "The server you entered is not authorized for this app." : MessageLookupByLibrary.simpleMessage("الخادم الذي أدخلته غير مخول لهذا التطبيق."),
    "The student you are trying to add belongs to a different school. Log in or create an account with that school to scan this code." : MessageLookupByLibrary.simpleMessage("الطالب الذي تحاول إضافته يتبع مدرسة مختلفة. سجّل الدخول أو أنشئ حساباً في هذه المدرسة لمسح هذا الرمز."),
    "The user agent for this app is not authorized." : MessageLookupByLibrary.simpleMessage("وكيل المستخدم لهذا التطبيق غير مخوَّل."),
    "Theme" : MessageLookupByLibrary.simpleMessage("النسق"),
    "There are no installed applications that can open this file" : MessageLookupByLibrary.simpleMessage("لا توجد تطبيقات مثبتة يمكن من خلالها فتح هذا الملف"),
    "There is no page information available." : MessageLookupByLibrary.simpleMessage("لا تتوفر أي صفحة معلومات"),
    "There was a problem loading the Terms of Use" : MessageLookupByLibrary.simpleMessage("حدثت مشكلة أثناء تحميل شروط الاستخدام"),
    "There was a problem removing this student from your account. Please check your connection and try again." : MessageLookupByLibrary.simpleMessage("حدثت مشكلة في إزالة هذا الطالب من حسابك. يرجى التحقق من الاتصال وإعادة المحاولة."),
    "There was an error loading recipients for this course" : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل متلقين لهذا المساق"),
    "There was an error loading the summary details for this course." : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل تفاصيل الملخص لهذا المساق."),
    "There was an error loading this announcement" : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل هذا الإعلان"),
    "There was an error loading this conversation" : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل هذه المحادثة"),
    "There was an error loading this file" : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل هذا الملف"),
    "There was an error loading your inbox messages." : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل رسائلك في علبة الوارد."),
    "There was an error loading your student\'s alerts." : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل تنبيهات الطالب."),
    "There was an error loading your student\'s calendar" : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل تقويم الطالب."),
    "There was an error loading your students." : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل طلابك."),
    "There was an error loading your student’s courses." : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تحميل مساقات الطالب لديك."),
    "There was an error logging in. Please generate another QR Code and try again." : MessageLookupByLibrary.simpleMessage("حدث خطأ أثناء تسجيل الدخول. يرجى إنشاء رمز QR وإعادة المحاولة."),
    "There was an error trying to act as this user. Please check the Domain and User ID and try again." : MessageLookupByLibrary.simpleMessage("حدث خطأ ما في التصرف باعتبارك هذا المستخدم. يرجى التحقق من المجال ومعرف المستخدم وإعادة المحاولة."),
    "There’s nothing to be notified of yet." : MessageLookupByLibrary.simpleMessage("لا يوجد شيء للإعلام عنه حتى الآن."),
    "This app is not authorized for use." : MessageLookupByLibrary.simpleMessage("هذا التطبيق غير مخول للاستخدام."),
    "This course does not have any assignments or calendar events yet." : MessageLookupByLibrary.simpleMessage("لا يحتوي هذا المساق على أي مهام أو أحداث بالتقويم حتى الآن."),
    "This file is unsupported and can’t be viewed through the app" : MessageLookupByLibrary.simpleMessage("هذا الملف غير مدعوم ولا يمكن عرضه من خلال التطبيق"),
    "This will unpair and remove all enrollments for this student from your account." : MessageLookupByLibrary.simpleMessage("سيؤدي هذا إلى إلغاء الإقران وإزالة جميع التسجيلات لهذا الطالب من حسابك."),
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
    "Use Dark Theme in Web Content" : MessageLookupByLibrary.simpleMessage("استخدام النسق الداكن في محتوى الويب"),
    "User ID" : MessageLookupByLibrary.simpleMessage("معرف المستخدم"),
    "User ID:" : MessageLookupByLibrary.simpleMessage("معرف المستخدم:"),
    "Version Number" : MessageLookupByLibrary.simpleMessage("رقم الإصدار"),
    "View Description" : MessageLookupByLibrary.simpleMessage("وصف طريقة عرض"),
    "View error details" : MessageLookupByLibrary.simpleMessage("عرض تفاصيل الخطأ"),
    "View the Privacy Policy" : MessageLookupByLibrary.simpleMessage("عرض سياسة الخصوصية"),
    "We are currently building this feature for your viewing pleasure." : MessageLookupByLibrary.simpleMessage("نقوم في الوقت الحالي ببناء هذه الميزة لتستمتع بالعرض."),
    "We are unable to display this link, it may belong to an institution you currently aren\'t logged in to." : MessageLookupByLibrary.simpleMessage("لم يمكننا عرض هذا الرابط، قد يكون تابعاً لمؤسسة لم تسجل الدخول إليها في الوقت الحالي."),
    "We couldn\'t find any students associated with this account" : MessageLookupByLibrary.simpleMessage("تعذر علينا العثور على أي طلاب مقترنين بهذا الحساب"),
    "We were unable to verify the server for use with this app." : MessageLookupByLibrary.simpleMessage("لم نتمكن من التحقق من الخادم لاستخدامه مع هذا التطبيق."),
    "We’re not sure what happened, but it wasn’t good. Contact us if this keeps happening." : MessageLookupByLibrary.simpleMessage("لا نعلم على وجه الدقة بما حدث، ولكنه لم يكن أمراً جيداً. اتصل بنا إذا استمر هذا في الحدوث."),
    "What can we do better?" : MessageLookupByLibrary.simpleMessage("ماذا يمكننا فعله لتطوير أداءنا؟"),
    "Yes" : MessageLookupByLibrary.simpleMessage("نعم"),
    "You are not observing any students." : MessageLookupByLibrary.simpleMessage("أنت لا تراقب أي طالب."),
    "You may only choose 10 calendars to display" : MessageLookupByLibrary.simpleMessage("يمكنك اختيار 10 تقويمات فقط لعرضها"),
    "You must enter a user id" : MessageLookupByLibrary.simpleMessage("يجب أن تدخل معرف مستخدم"),
    "You must enter a valid domain" : MessageLookupByLibrary.simpleMessage("يجب عليك إدخال مجال صالح"),
    "You must select at least one calendar to display" : MessageLookupByLibrary.simpleMessage("يجب عليك تحديد تقويم واحد على الأقل للعرض"),
    "You will be notified about this assignment on…" : MessageLookupByLibrary.simpleMessage("سيتم إعلامك بهذه المهمة في..."),
    "You will be notified about this event on…" : MessageLookupByLibrary.simpleMessage("سيتم إعلامك بهذا الحدث في..."),
    "You\'ll find the QR code on the web in your account profile. Click \'QR for Mobile Login\' in the list." : MessageLookupByLibrary.simpleMessage("ستجد رمز QR على الويب في ملف تعريف حسابك. انقر فوق \"QR لتسجيل دخول الجوال\" في القائمة."),
    "You\'ll need to open your student\'s Canvas Student app to continue. Go into Main Menu > Settings > Pair with Observer and scan the QR code you see there." : MessageLookupByLibrary.simpleMessage("ستحتاج إلى فتح تطبيق Canvas Student لدى الطالب للمتابعة. انتقل إلى القائمة الرئيسية > الإعدادات > إقران مع Observer ثم امسح رمز QR الذي تراه هناك."),
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
    "changeStudentColorLabel" : m12,
    "collapse" : MessageLookupByLibrary.simpleMessage("طي"),
    "collapsed" : MessageLookupByLibrary.simpleMessage("تم الطي"),
    "contentDescriptionScoreOutOfPointsPossible" : m13,
    "courseForWhom" : m14,
    "courseGradeAboveThreshold" : m15,
    "courseGradeBelowThreshold" : m16,
    "coursesLabel" : MessageLookupByLibrary.simpleMessage("المساقات"),
    "dateAtTime" : m17,
    "dismissAlertLabel" : m18,
    "domainSearchHelpBody" : m19,
    "domainSearchHelpLabel" : MessageLookupByLibrary.simpleMessage("كيف أعثر على مدرستي أو دائرتي التعليمية؟"),
    "domainSearchInputHint" : MessageLookupByLibrary.simpleMessage("أدخل اسم المدرسة أو الدائرة التعليمية..."),
    "dueDateAtTime" : m20,
    "endMasqueradeLogoutMessage" : m21,
    "endMasqueradeMessage" : m22,
    "eventSubjectMessage" : m23,
    "eventTime" : m24,
    "expand" : MessageLookupByLibrary.simpleMessage("توسيع"),
    "expanded" : MessageLookupByLibrary.simpleMessage("تم التوسيع"),
    "finalGrade" : m25,
    "findSchool" : MessageLookupByLibrary.simpleMessage("البحث عن مدرسة"),
    "frontPageSubjectMessage" : m26,
    "gradeFormatScoreOutOfPointsPossible" : m27,
    "gradesSubjectMessage" : m28,
    "latePenalty" : m29,
    "me" : MessageLookupByLibrary.simpleMessage("أنا"),
    "messageLinkPostscript" : m30,
    "minus" : MessageLookupByLibrary.simpleMessage("سالب"),
    "mustBeAboveN" : m31,
    "mustBeBelowN" : m32,
    "next" : MessageLookupByLibrary.simpleMessage("التالي"),
    "nextMonth" : m33,
    "nextWeek" : m34,
    "noDomainResults" : m35,
    "ok" : MessageLookupByLibrary.simpleMessage("موافق"),
    "outOfPoints" : m36,
    "plusRecipientCount" : m37,
    "pointsPossible" : m38,
    "previousMonth" : m39,
    "previousWeek" : m40,
    "qrCreateAccountTos" : m41,
    "ratingDialogEmailSubject" : m42,
    "selectedMonthLabel" : m43,
    "send" : MessageLookupByLibrary.simpleMessage("إرسال"),
    "starRating" : m44,
    "submissionStatusSuccessSubtitle" : m45,
    "syllabusSubjectMessage" : m46,
    "unread" : MessageLookupByLibrary.simpleMessage("غير مقروء"),
    "unreadCount" : m47
  };
}
