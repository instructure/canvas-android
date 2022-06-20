// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alert.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

const AlertType _$alertTypeAssignmentMissing =
    const AlertType._('assignmentMissing');
const AlertType _$alertTypeAssignmentGradeHigh =
    const AlertType._('assignmentGradeHigh');
const AlertType _$alertTypeAssignmentGradeLow =
    const AlertType._('assignmentGradeLow');
const AlertType _$alertTypeCourseGradeHigh =
    const AlertType._('courseGradeHigh');
const AlertType _$alertTypeCourseGradeLow = const AlertType._('courseGradeLow');
const AlertType _$alertTypeCourseAnnouncement =
    const AlertType._('courseAnnouncement');
const AlertType _$alertTypeInstitutionAnnouncement =
    const AlertType._('institutionAnnouncement');
const AlertType _$alertTypeUnknown = const AlertType._('unknown');

AlertType _$alertTypeValueOf(String name) {
  switch (name) {
    case 'assignmentMissing':
      return _$alertTypeAssignmentMissing;
    case 'assignmentGradeHigh':
      return _$alertTypeAssignmentGradeHigh;
    case 'assignmentGradeLow':
      return _$alertTypeAssignmentGradeLow;
    case 'courseGradeHigh':
      return _$alertTypeCourseGradeHigh;
    case 'courseGradeLow':
      return _$alertTypeCourseGradeLow;
    case 'courseAnnouncement':
      return _$alertTypeCourseAnnouncement;
    case 'institutionAnnouncement':
      return _$alertTypeInstitutionAnnouncement;
    case 'unknown':
      return _$alertTypeUnknown;
    default:
      return _$alertTypeUnknown;
  }
}

final BuiltSet<AlertType> _$alertTypeValues =
    new BuiltSet<AlertType>(const <AlertType>[
  _$alertTypeAssignmentMissing,
  _$alertTypeAssignmentGradeHigh,
  _$alertTypeAssignmentGradeLow,
  _$alertTypeCourseGradeHigh,
  _$alertTypeCourseGradeLow,
  _$alertTypeCourseAnnouncement,
  _$alertTypeInstitutionAnnouncement,
  _$alertTypeUnknown,
]);

const AlertWorkflowState _$alertWorkflowStateRead =
    const AlertWorkflowState._('read');
const AlertWorkflowState _$alertWorkflowStateUnread =
    const AlertWorkflowState._('unread');
const AlertWorkflowState _$alertWorkflowStateDeletted =
    const AlertWorkflowState._('deleted');
const AlertWorkflowState _$alertWorkflowStateDismissed =
    const AlertWorkflowState._('dismissed');
const AlertWorkflowState _$alertWorkflowStateUnknown =
    const AlertWorkflowState._('unknown');

AlertWorkflowState _$alertWorkflowStateValueOf(String name) {
  switch (name) {
    case 'read':
      return _$alertWorkflowStateRead;
    case 'unread':
      return _$alertWorkflowStateUnread;
    case 'deleted':
      return _$alertWorkflowStateDeletted;
    case 'dismissed':
      return _$alertWorkflowStateDismissed;
    case 'unknown':
      return _$alertWorkflowStateUnknown;
    default:
      return _$alertWorkflowStateUnknown;
  }
}

final BuiltSet<AlertWorkflowState> _$alertWorkflowStateValues =
    new BuiltSet<AlertWorkflowState>(const <AlertWorkflowState>[
  _$alertWorkflowStateRead,
  _$alertWorkflowStateUnread,
  _$alertWorkflowStateDeletted,
  _$alertWorkflowStateDismissed,
  _$alertWorkflowStateUnknown,
]);

Serializer<Alert> _$alertSerializer = new _$AlertSerializer();
Serializer<AlertType> _$alertTypeSerializer = new _$AlertTypeSerializer();
Serializer<AlertWorkflowState> _$alertWorkflowStateSerializer =
    new _$AlertWorkflowStateSerializer();

class _$AlertSerializer implements StructuredSerializer<Alert> {
  @override
  final Iterable<Type> types = const [Alert, _$Alert];
  @override
  final String wireName = 'Alert';

  @override
  Iterable<Object> serialize(Serializers serializers, Alert object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'observer_alert_threshold_id',
      serializers.serialize(object.observerAlertThresholdId,
          specifiedType: const FullType(String)),
      'context_type',
      serializers.serialize(object.contextType,
          specifiedType: const FullType(String)),
      'context_id',
      serializers.serialize(object.contextId,
          specifiedType: const FullType(String)),
      'alert_type',
      serializers.serialize(object.alertType,
          specifiedType: const FullType(AlertType)),
      'workflow_state',
      serializers.serialize(object.workflowState,
          specifiedType: const FullType(AlertWorkflowState)),
      'action_date',
      serializers.serialize(object.actionDate,
          specifiedType: const FullType(DateTime)),
      'title',
      serializers.serialize(object.title,
          specifiedType: const FullType(String)),
      'user_id',
      serializers.serialize(object.userId,
          specifiedType: const FullType(String)),
      'observer_id',
      serializers.serialize(object.observerId,
          specifiedType: const FullType(String)),
      'html_url',
      serializers.serialize(object.htmlUrl,
          specifiedType: const FullType(String)),
      'locked_for_user',
      serializers.serialize(object.lockedForUser,
          specifiedType: const FullType(bool)),
    ];

    return result;
  }

  @override
  Alert deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AlertBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'observer_alert_threshold_id':
          result.observerAlertThresholdId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'context_type':
          result.contextType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'context_id':
          result.contextId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'alert_type':
          result.alertType = serializers.deserialize(value,
              specifiedType: const FullType(AlertType)) as AlertType;
          break;
        case 'workflow_state':
          result.workflowState = serializers.deserialize(value,
                  specifiedType: const FullType(AlertWorkflowState))
              as AlertWorkflowState;
          break;
        case 'action_date':
          result.actionDate = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'title':
          result.title = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'user_id':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'observer_id':
          result.observerId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'html_url':
          result.htmlUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'locked_for_user':
          result.lockedForUser = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$AlertTypeSerializer implements PrimitiveSerializer<AlertType> {
  static const Map<String, Object> _toWire = const <String, Object>{
    'assignmentMissing': 'assignment_missing',
    'assignmentGradeHigh': 'assignment_grade_high',
    'assignmentGradeLow': 'assignment_grade_low',
    'courseGradeHigh': 'course_grade_high',
    'courseGradeLow': 'course_grade_low',
    'courseAnnouncement': 'course_announcement',
    'institutionAnnouncement': 'institution_announcement',
  };
  static const Map<Object, String> _fromWire = const <Object, String>{
    'assignment_missing': 'assignmentMissing',
    'assignment_grade_high': 'assignmentGradeHigh',
    'assignment_grade_low': 'assignmentGradeLow',
    'course_grade_high': 'courseGradeHigh',
    'course_grade_low': 'courseGradeLow',
    'course_announcement': 'courseAnnouncement',
    'institution_announcement': 'institutionAnnouncement',
  };

  @override
  final Iterable<Type> types = const <Type>[AlertType];
  @override
  final String wireName = 'alert_type';

  @override
  Object serialize(Serializers serializers, AlertType object,
          {FullType specifiedType = FullType.unspecified}) =>
      _toWire[object.name] ?? object.name;

  @override
  AlertType deserialize(Serializers serializers, Object serialized,
          {FullType specifiedType = FullType.unspecified}) =>
      AlertType.valueOf(_fromWire[serialized] ?? serialized as String);
}

class _$AlertWorkflowStateSerializer
    implements PrimitiveSerializer<AlertWorkflowState> {
  @override
  final Iterable<Type> types = const <Type>[AlertWorkflowState];
  @override
  final String wireName = 'workflow_state';

  @override
  Object serialize(Serializers serializers, AlertWorkflowState object,
          {FullType specifiedType = FullType.unspecified}) =>
      object.name;

  @override
  AlertWorkflowState deserialize(Serializers serializers, Object serialized,
          {FullType specifiedType = FullType.unspecified}) =>
      AlertWorkflowState.valueOf(serialized as String);
}

class _$Alert extends Alert {
  @override
  final String id;
  @override
  final String observerAlertThresholdId;
  @override
  final String contextType;
  @override
  final String contextId;
  @override
  final AlertType alertType;
  @override
  final AlertWorkflowState workflowState;
  @override
  final DateTime actionDate;
  @override
  final String title;
  @override
  final String userId;
  @override
  final String observerId;
  @override
  final String htmlUrl;
  @override
  final bool lockedForUser;

  factory _$Alert([void Function(AlertBuilder) updates]) =>
      (new AlertBuilder()..update(updates)).build();

  _$Alert._(
      {this.id,
      this.observerAlertThresholdId,
      this.contextType,
      this.contextId,
      this.alertType,
      this.workflowState,
      this.actionDate,
      this.title,
      this.userId,
      this.observerId,
      this.htmlUrl,
      this.lockedForUser})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Alert', 'id');
    }
    if (observerAlertThresholdId == null) {
      throw new BuiltValueNullFieldError('Alert', 'observerAlertThresholdId');
    }
    if (contextType == null) {
      throw new BuiltValueNullFieldError('Alert', 'contextType');
    }
    if (contextId == null) {
      throw new BuiltValueNullFieldError('Alert', 'contextId');
    }
    if (alertType == null) {
      throw new BuiltValueNullFieldError('Alert', 'alertType');
    }
    if (workflowState == null) {
      throw new BuiltValueNullFieldError('Alert', 'workflowState');
    }
    if (actionDate == null) {
      throw new BuiltValueNullFieldError('Alert', 'actionDate');
    }
    if (title == null) {
      throw new BuiltValueNullFieldError('Alert', 'title');
    }
    if (userId == null) {
      throw new BuiltValueNullFieldError('Alert', 'userId');
    }
    if (observerId == null) {
      throw new BuiltValueNullFieldError('Alert', 'observerId');
    }
    if (htmlUrl == null) {
      throw new BuiltValueNullFieldError('Alert', 'htmlUrl');
    }
    if (lockedForUser == null) {
      throw new BuiltValueNullFieldError('Alert', 'lockedForUser');
    }
  }

  @override
  Alert rebuild(void Function(AlertBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  AlertBuilder toBuilder() => new AlertBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Alert &&
        id == other.id &&
        observerAlertThresholdId == other.observerAlertThresholdId &&
        contextType == other.contextType &&
        contextId == other.contextId &&
        alertType == other.alertType &&
        workflowState == other.workflowState &&
        actionDate == other.actionDate &&
        title == other.title &&
        userId == other.userId &&
        observerId == other.observerId &&
        htmlUrl == other.htmlUrl &&
        lockedForUser == other.lockedForUser;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc(
                        $jc(
                            $jc(
                                $jc(
                                    $jc(
                                        $jc(
                                            $jc(
                                                $jc(0, id.hashCode),
                                                observerAlertThresholdId
                                                    .hashCode),
                                            contextType.hashCode),
                                        contextId.hashCode),
                                    alertType.hashCode),
                                workflowState.hashCode),
                            actionDate.hashCode),
                        title.hashCode),
                    userId.hashCode),
                observerId.hashCode),
            htmlUrl.hashCode),
        lockedForUser.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Alert')
          ..add('id', id)
          ..add('observerAlertThresholdId', observerAlertThresholdId)
          ..add('contextType', contextType)
          ..add('contextId', contextId)
          ..add('alertType', alertType)
          ..add('workflowState', workflowState)
          ..add('actionDate', actionDate)
          ..add('title', title)
          ..add('userId', userId)
          ..add('observerId', observerId)
          ..add('htmlUrl', htmlUrl)
          ..add('lockedForUser', lockedForUser))
        .toString();
  }
}

class AlertBuilder implements Builder<Alert, AlertBuilder> {
  _$Alert _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _observerAlertThresholdId;
  String get observerAlertThresholdId => _$this._observerAlertThresholdId;
  set observerAlertThresholdId(String observerAlertThresholdId) =>
      _$this._observerAlertThresholdId = observerAlertThresholdId;

  String _contextType;
  String get contextType => _$this._contextType;
  set contextType(String contextType) => _$this._contextType = contextType;

  String _contextId;
  String get contextId => _$this._contextId;
  set contextId(String contextId) => _$this._contextId = contextId;

  AlertType _alertType;
  AlertType get alertType => _$this._alertType;
  set alertType(AlertType alertType) => _$this._alertType = alertType;

  AlertWorkflowState _workflowState;
  AlertWorkflowState get workflowState => _$this._workflowState;
  set workflowState(AlertWorkflowState workflowState) =>
      _$this._workflowState = workflowState;

  DateTime _actionDate;
  DateTime get actionDate => _$this._actionDate;
  set actionDate(DateTime actionDate) => _$this._actionDate = actionDate;

  String _title;
  String get title => _$this._title;
  set title(String title) => _$this._title = title;

  String _userId;
  String get userId => _$this._userId;
  set userId(String userId) => _$this._userId = userId;

  String _observerId;
  String get observerId => _$this._observerId;
  set observerId(String observerId) => _$this._observerId = observerId;

  String _htmlUrl;
  String get htmlUrl => _$this._htmlUrl;
  set htmlUrl(String htmlUrl) => _$this._htmlUrl = htmlUrl;

  bool _lockedForUser;
  bool get lockedForUser => _$this._lockedForUser;
  set lockedForUser(bool lockedForUser) =>
      _$this._lockedForUser = lockedForUser;

  AlertBuilder() {
    Alert._initializeBuilder(this);
  }

  AlertBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _observerAlertThresholdId = _$v.observerAlertThresholdId;
      _contextType = _$v.contextType;
      _contextId = _$v.contextId;
      _alertType = _$v.alertType;
      _workflowState = _$v.workflowState;
      _actionDate = _$v.actionDate;
      _title = _$v.title;
      _userId = _$v.userId;
      _observerId = _$v.observerId;
      _htmlUrl = _$v.htmlUrl;
      _lockedForUser = _$v.lockedForUser;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Alert other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Alert;
  }

  @override
  void update(void Function(AlertBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Alert build() {
    final _$result = _$v ??
        new _$Alert._(
            id: id,
            observerAlertThresholdId: observerAlertThresholdId,
            contextType: contextType,
            contextId: contextId,
            alertType: alertType,
            workflowState: workflowState,
            actionDate: actionDate,
            title: title,
            userId: userId,
            observerId: observerId,
            htmlUrl: htmlUrl,
            lockedForUser: lockedForUser);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
