/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandautils.utils;

public class Const {
    public static final String PANDA_UTILS = "panda_utils";
    public static final String PANDA_UTILS_FILE_UPLOAD_UTILS_LOG = "file_upload_utils";
    public static final String OPEN_MEDIA_ASYNC_TASK_LOADER_LOG = "OpenMediaAsynTaskLoader";

    public static final int SETTINGS_CHANGED_RESULT_CODE = 18304;
    public static final int PROFILE_BACKGROUND_SELECTED_RESULT_CODE = 55935;
    public static final int PANDA_AVATAR_RESULT_CODE = 15964;

    //////////////////////////////////////////////////////////
    // Intent Strings
    //////////////////////////////////////////////////////////

    public static final String ACCOUNT_NOTIFICATION = "accountNotification";
    public static final String ACTION = "action";
    public static final String ACTION_BAR_TITLE = "actionBarTitle";
    public static final String ADD_SYLLABUS = "addSyllabus";
    public static final String ANNOUNCEMENT = "announcement";
    public static final String API_URL = "apiURL";
    public static final String STUDIO_SUBMISSION = "studioSubmission";
    public static final String ATTACHMENT = "attachment";
    public static final String ATTACHMENTS = "attachments";
    public static final String ASSIGNMENT = "assignment";
    public static final String ASSIGNMENT_ID = "assignmentId";
    public static final String ASSIGNMENT_NAME = "assignmentName";
    public static final String ASSIGNMENT_SET = "assignment_set";
    public static final String AUTHENTICATE = "authenticate";
    public static final String BUNDLE = "bundle";
    public static final String BUNDLE_INTENT = "bundledIntent";
    public static final String CALENDAR_DIALOG_CONTEXT_LIST = "calendarDialogContextList";
    public static final String CALENDAR_DIALOG_FILTER_PREFS = "calendarDialogFilterPrefs";
    public static final String CALENDAR_DIALOG_CONTEXT_IDS= "calendarDialogContextIds";
    public static final String CALENDAR_DIALOG_CONTEXT_COURSE_IDS = "calendarDialogContextCourseIds";
    public static final String CALENDAR_EVENT_START_DATE = "calendarEventStartDate";
    public static final String CANVAS_CONTEXT = "canvasContext";
    public static final String CANVAS_CONTEXT_ID = "canvasContextId";
    public static final String CANVAS_LOGIN = "canvas_login";
    public static final String CANVAS_USER_GUIDES = "https://community.canvaslms.com/community/answers/guides/mobile-guide/content?filterID=contentstatus%5Bpublished%5D~category%5Btable-of-contents%5D";
    public static final String CHANGED = "changed";
    public static final String CHILD_POSITION = "childPosition";
    public static final String CLASS_NAME = "className";
    public static final String COMPOSE_FRAGMENT = "composeFragment";
    public static final String CONFERENCE = "conference";
    public static final String COURSE = "course";
    public static final String COURSE_HOMEPAGE = "course_homepage";
    public static final String DEFAULT_TAB = "defaultTab";
    public static final String DEPTH = "depth";
    public static final String DISCUSSION_ENTRY = "discussion_entry";
    public static final String DISCUSSION_HEADER = "discussion_header";
    public static final String DISCUSSION_TOPIC = "discussion_topic";
    public static final String DISCUSSION_ID = "discussion_id";
    public static final String EDIT_NAME = "editName";
    public static final String EMAIL = "email";
    public static final String EVENT_LIST = "eventList";
    public static final String EXPAND = "expand";
    public static final String COLLAPSE = "collapse";
    public static final String CALENDAR_STATE = "calendarSaveState";
    public static final String FEATURE_NAME = "featureName";
    public static final String FILE_ALLOWED = "isFileUploadAllowed";
    public static final String FILE_URL = "fileUrl";
    public static final String FILE_DOWNLOADED = "fileDownloaded";
    public static final String FINISH = "finish";
    public static final String FOLDER = "folder";
    public static final String FOLDER_ID = "folderId";
    public static final String FOLDER_NAME = "folderName";
    public static final String FOR_RESULT = "forResult";
    public static final String FROM_DASHBOARD = "fromDashboard";
    public static final String FROM_PEOPLE = "fromPeople";
    public static final String FRAGMENT_TYPE = "fragmentType";
    public static final String GROUP_POSITION = "groupPosition";
    public static final String HAS_STUDENTS = "hasStudents";
    public static final String HOST = "host";
    public static final String HTML = "html";
    public static final String ID = "id";
    public static final String IMAGE_ID = "imageId";
    public static final String INTERNAL_URL = "internalURL";
    public static final String IN_EDIT_MODE = "inEditMode";
    public static final String IS_STUDIO_ENABLED = "isStudioEnabled";
    public static final String IS_DASHBOARD = "isDashboard";
    public static final String IS_MEDIA_TYPE = "isMediaType";
    public static final String IS_OVERRIDDEN = "isOverridden";
    public static final String IS_SHOW_FIRST_ITEM = "isShowFirstItem";
    public static final String IS_FIRST_SHOW = "isFirstShow";
    public static final String IS_UNREAD ="isUnread";
    public static final String IS_STARRED = "isStarred";
    public static final String IS_GROUP = "isGroup";
    public static final String IS_UNSUPPORTED_FEATURE ="isUnsupportedFeature";
    public static final String IS_OBSERVER ="isObserver";
    public static final String ALLOW_UNSUPPORTED_ROUTING ="allowUnsupportedRouting";
    public static final String LAYOUT_ID = "layout_id";
    public static final String MASTERY_PATH = "mastery_path";
    public static final String MEDIA_UPLOAD_ALLOWED = "isMediaUploadAllowed";
    public static final String MEDIA_RECORDING = "media_recording";
    public static final String MESSAGE = "message";
    public static final String MIME = "mime";
    public static final String MESSAGE_TO_USER = "messageToUser";
    public static final String MESSAGE_TYPE ="messageType";
    public static final String MODULE_ID = "moduleId";
    public static final String MODULE_ITEM = "moduleItems";
    public static final String MODULE_ITEM_ID = "moduleItemId";
    public static final String MODULE_OBJECT = "moduleObject";
    public static final String MODULE_OBJECTS = "moduleObjects";
    public static final String NAME = "name";
    public static final String PAGE = "page";
    public static final String PAGE_NAME = "pageName";
    public static final String PARENT_FOLDER_ID = "parentFolderID";
    public static final String PASSED_URI = "passedURI";
    public static final String PENDING_REVIEW = "pending_review";
    public static final String QUIZ = "quiz";
    public static final String RECIPIENT = "recipient";
    public static final String RECIPIENTS = "recipients";
    public static final String REMOVAL_TYPE = "removalType";
    public static final String SCHEDULE_ITEM= "scheduleItem";
    public static final String SCHEDULE_ITEM_ID = "scheduleItemId";
    public static final String SCOPE = "scope";
    public static final String SCORE = "score";
    public static final String SELECTED = "selected";
    public static final String SELECTED_ITEM = "selectedItem";
    public static final String SHOW_BUTTON = "showButton" ;
    public static final String SHOW_FRONT_PAGE = "isShowFrontPage";
    public static final String SHOW_MESSAGE = "showMessage";
    public static final String SPINNER = "spinner";
    public static final String STREAM_ITEM = "streamItem";
    public static final String STREAM_ITEM_BODY = "streamItemBody";
    public static final String STREAM_ITEM_COURSE_CODE = "streamItemCourseCode";
    public static final String STREAM_ITEM_TITLE = "streamItemTitle";
    public static final String STREAM_ITEM_TYPE = "streamItemType";
    public static final String SUBJECT = "subject";
    public static final String SUBMISSION_GRADE = "SubmissionGrade";
    public static final String SUBMIT_URI = "submitURI";
    public static final String SUBMIT_URL = "submitURI";
    public static final String SUBTITLE_TEXT = "subtitleText";
    public static final String SYLLABUS = "syllabus";
    public static final String TAB = "tab";
    public static final String TAB_ID = "tabId";
    public static final String TEXT = "text";
    public static final String TEXT_ALLOWED = "isOnlineTextAllowed";
    public static final String TITLE_TEXT = "titleText";
    public static final String TOPIC_HEADER = "topicHeader";
    public static final String TOPIC_ID = "topicId";
    public static final String TRANSITION_TYPE = "transitionType";
    public static final String UNREAD = "unread";
    public static final String UPLOAD_TYPE = "uploadType";
    public static final String URI = "uri";
    public static final String TEXT_EXTRA = "textExtra";
    public static final String URL = "url";
    public static final String URL_ALLOWED = "isUrlEntryAllowed";
    public static final String USER = "user";
    public static final String USER_ID = "userId";
    public static final String USER_IDS = "userIds";
    public static final String __CURRENT = "__current";
    public static final String __PREVIOUS = "__previous";
    public static final String WIDGET = "fromWidget";
    public static final String COURSE_URL = "/courses/";
    public static final String GRADE_URL = "/grades";
    public static final String SESSIONLESS_LAUNCH = "sessionlessLaunch";
    public static final String ASSIGNMENT_LTI = "assignmentLti";
    //Broadcast
    public static final String REFRESH = "refresh";
    public static final String SUBMISSION_COMMENT_SUBMITTED = "submission-comment-submitted";
    public static final String SUBMISSION = "submission";
    public static final String SUBMISSION_ID = "submission_id";
    public static final String SUBMISSION_TARGET = "submission_target";
    public static final String DISCUSSION_REPLY_SUBMITTED = "discussion_reply_submitted";

    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String COURSE_VIEW = "course_view";
    public static final String WITH_SLIDING_PANE = "with_sliding_pane";
    public static final String PLACEMENT = "placement";
    public static final String PROFILE_URL = "canvas.instructure.com";
    public static final String NO_PICTURE_URL = "images/dotted_pic.png";

    public static final String LOGIN = "login";
    public static final String CALENDAR = "calendar";
    public static final String CONVERSATION = "conversations";
    public static final String COURSES = "courses";
    public static final String GROUPS = "groups";
    public static final String FILES = "files";
    public static final String APP_ROUTER = "comingFromAppRouter";
    public static final String EXTRAS = "bundledExtras";
    public static final String CONTEXT_ID = "contextId";
    public static final String CONTEXT_TYPE = "contextType";
    public static final String CONTENT_TYPE = "contentType";
    public static final String PATH = "path";
    public static final String SIZE = "size";
    public static final String DELETE = "delete";
    public static final String LOADER_ID = "loaderID";
    public static final String LOADER_BUNDLE = "loaderBundle";
    public static final String OPEN_MEDIA_LOADER_BUNDLE = "openMediaLoaderBundle";
    public static final String FIRST_PARAM = "firstParam";
    public static final String SECOND_PARAM = "secondParam";
    public static final String THIRD_PARAM = "thirdParam";
    public static final String INTENT_TYPE = "intentType";
    public static final String TYPE = "type";
    public static final String NONE = "none";

    public static final String FIRST_PARAM_PARSED = "firstParamParsed";
    public static final String SECOND_PARAM_PARSED = "secondParamParsed";
    public static final String THIRD_PARAM_PARSED = "thirdParamParsed";
    public static final String PARAMS = "params";
    public static final String ACTION_ROUTING = "com.instructure.student.ROUTING_ACTIVITY";
    public static final String DOMAIN = "domain";
    public static final String DOMAIN_FOR_DISPLAY = "domainForDisplay";
    public static final String SCHEME = "scheme";
    public static final String PARSE = "parse";
    public static final String SPEEDGRADER_PACKAGE = "com.instructure.speedgrader";
    public static final String SPEEDGRADER_ACTION = "com.instructure.speedgrader.SPEED_GRADER";
    public static final String SPEEDGRADER_PLAYSTORE = "market://details?id=com.instructure.speedgrader";


    public static final String ITEM_ID = "genericItemId";
    public static final String PASSED_ITEM_TRIGGERED = "passedItemTriggered";
    public static final String MASQUERADE_VISIBLE = "masquerade_visible";
    public static final String MODULE_POSITION = "module_position";
    public static final String COURSE_COLOR = "courses";
    public static final String COURSE_COLORS = "all_canvas_context_colors_1";
    public static final String COURSE_FAVORITES = "courseFavorites";
    public static final String FAVORITE_STATUS = "favoriteStatus";

    public static final String NAVIGATION_SHORTCUTS_EXPANDED = "navigationShortcuts";

    public static final String USER_BACKGROUND_IMAGE_URL = "userBackgroundImageUrl";

    public static final String UPLOAD_STARTED = "uploadStarted";
    public static final String UPLOAD_SUCCESS = "uploadSuccess";
    public static final String URL_PARAMS = "url_params";
    public static final String URL_QUERY_PARAMS = "url_query_params";

    public static final String ACTION_MEDIA_UPLOAD_SUCCESS = "com.instructure.speedgrader.SPEED_GRADER_MEDIA_COMMENT_UPDATE";
    public static final String CLOAKMAN_TO_COMMENT_VIEW = "cloakmanToCommentView";

    //Data loss
    public static final String DATA_LOSS_COMPOSE_NEW_MESSAGE = "dataLossComposeNewMessage";
    public static final String DATA_LOSS_ADD_SUBMISSION = "dataLossAddSubmission";
    public static final String DATA_LOSS_ADD_SUBMISSION_URL = "dataLossAddSubmissionUrl";
    public static final String DATA_LOSS_DISCUSSION_TITLE = "dataLossDiscussionTitle";
    public static final String DATA_LOSS_DISCUSSION_MESSAGE = "dataLossDiscussionMessage";
    public static final String DATA_LOSS_ANNOUNCEMENT_TITLE = "dataLossAnnouncementTitle";
    public static final String DATA_LOSS_ANNOUNCEMENT_MESSAGE = "dataLossAnnouncementMessage";

    public static final String HAS_SHOWN_CANVAS_POLL = "hasShownCanvasPoll";
    public static final String DATE_TO_SHOW_CANVAS_POLL = "canvasPollShowDate";
    public static final String CAN_BE_POLLED = "canBePolled";

    public static final String STUDENT_ID = "student_id";
    public static final String STUDENT = "student";

    public static final String CONVERSATION_ID = "conversation_id";

    public static final String POSITION = "position";
    public static final String ARRAY = "array";

    public static final String QUIZ_SUBMISSION_ID = "quiz_submission_id";
    public static final String QUIZ_SHOULD_LET_ANSWER = "should_let_answer";
    public static final String QUIZ_SUBMISSION = "quiz_submission";
    public static final String QUIZ_QUESTION_IDS = "quiz_question_ids";
    public static final String QUIZ_QUESTIONS = "quiz_questions";
    public static final String QUIZ_MATCH_ID = "match_id";
    public static final String QUIZ_ANSWER_ID = "answer_id";
    public static final String QUIZ_ID = "quiz_id;";

    // Shared File
    public static final String IS_STUDENT = "isStudent";
    public static final String IS_TEACHER = "isTeacher";
    public static final String FILENAME = "fileName";
    public static final String COURSE_ID = "courseId";

    public static final String MAP = "map";
    public static final String COURSE_THING_CHANGED = "courseTHINGChangedBroadcast";

    public static final String BOOKMARK = "bookmark";

    public static final String WAIT_FOR_TRANSITION = "WAIT_FOR_TRANSITION";

    public static final float ACTIONBAR_ELEVATION = 10;

    public static final String RECEIVED_FROM_OUTSIDE = "receivedFromOutside";
    public static final String FROM_MODULE = "fromModule";

    public static final String PARENT = "parent";

    public static final String ITEM = "item";
    public static final String ITEMS = "items";
    public static final String OPEN_OUTSIDE = "isOpenOutside";

    public static final String RENAME = "rename";
    public static final String IS_EXTERNAL_TOOL = "isExternalTool";

    public static final String FILE_PROVIDER_AUTHORITY = ".provider";
    public static final String POINTS = "points";

    public static final String OPTIONS = "options";
    public static final String MEDIA_FILE_PATH = "media_file_path";
    public static final String SUBMISSION_COMMENT_LIST = "submission_comment_list";
    public static final String PAGE_ID = "PAGE_ID";
    public static final String ACTION_MEDIA_UPLOAD_FAIL = "com.instructure.action.NOTORIOUS_UPLOAD_ERROR";
    public static final String ERROR = "UPLOAD_ERROR";
    public static final String IS_SUBMISSION = "isSubmission";
    public static final String IS_FAILURE = "isFailure";

    // Language intent strings used for checking the incoming pending intent for a push notification
    public static final String LOCAL_NOTIFICATION = "localNotification";

    public static final String HIDE_TOOLBAR = "hideToolbar";
    public static final String STUDIO_LTI_TOOL = "studioLTITool";
    public static final String STUDENT_USER_AGENT = "candroid";
    public static final String TEACHER_USER_AGENT = "androidTeacher";

    // Intent String for QR Code Masquerading
    public static final String QR_CODE_MASQUERADE_ID = "qrCodeMasqueradeId";
    public static final String MARKET_URI_PREFIX = "market://details?id=";
    public static final String CANVAS_STUDENT_ID = "com.instructure.candroid";
    public static final String TOKEN = "token";

    // This is tied directly to an intent filter in the Student app AndroidManifest - if either one changes, make sure they stay in sync
    public static final String INTENT_ACTION_STUDENT_VIEW = "com.instructure.student.STUDENT_VIEW";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
}
