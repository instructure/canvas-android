// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'planner_item.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<PlannerItem> _$plannerItemSerializer = new _$PlannerItemSerializer();

class _$PlannerItemSerializer implements StructuredSerializer<PlannerItem> {
  @override
  final Iterable<Type> types = const [PlannerItem, _$PlannerItem];
  @override
  final String wireName = 'PlannerItem';

  @override
  Iterable<Object?> serialize(Serializers serializers, PlannerItem object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'plannable_type',
      serializers.serialize(object.plannableType,
          specifiedType: const FullType(String)),
      'plannable',
      serializers.serialize(object.plannable,
          specifiedType: const FullType(Plannable)),
    ];
    Object? value;
    value = object.courseId;

    result
      ..add('course_id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.contextType;

    result
      ..add('context_type')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.contextName;

    result
      ..add('context_name')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.plannableDate;

    result
      ..add('plannable_date')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.submissionStatusRaw;

    result
      ..add('submissions')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(JsonObject)));
    value = object.htmlUrl;

    result
      ..add('html_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.submissionStatus;

    result
      ..add('submissionStatus')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(PlannerSubmission)));

    return result;
  }

  @override
  PlannerItem deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PlannerItemBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'course_id':
          result.courseId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'context_type':
          result.contextType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'context_name':
          result.contextName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'plannable_type':
          result.plannableType = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'plannable':
          result.plannable.replace(serializers.deserialize(value,
              specifiedType: const FullType(Plannable))! as Plannable);
          break;
        case 'plannable_date':
          result.plannableDate = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'submissions':
          result.submissionStatusRaw = serializers.deserialize(value,
              specifiedType: const FullType(JsonObject)) as JsonObject?;
          break;
        case 'html_url':
          result.htmlUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'submissionStatus':
          result.submissionStatus.replace(serializers.deserialize(value,
                  specifiedType: const FullType(PlannerSubmission))!
              as PlannerSubmission);
          break;
      }
    }

    return result.build();
  }
}

class _$PlannerItem extends PlannerItem {
  @override
  final String? courseId;
  @override
  final String? contextType;
  @override
  final String? contextName;
  @override
  final String plannableType;
  @override
  final Plannable plannable;
  @override
  final DateTime? plannableDate;
  @override
  final JsonObject? submissionStatusRaw;
  @override
  final String? htmlUrl;
  @override
  final PlannerSubmission? submissionStatus;

  factory _$PlannerItem([void Function(PlannerItemBuilder)? updates]) =>
      (new PlannerItemBuilder()..update(updates))._build();

  _$PlannerItem._(
      {this.courseId,
      this.contextType,
      this.contextName,
      required this.plannableType,
      required this.plannable,
      this.plannableDate,
      this.submissionStatusRaw,
      this.htmlUrl,
      this.submissionStatus})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        plannableType, r'PlannerItem', 'plannableType');
    BuiltValueNullFieldError.checkNotNull(
        plannable, r'PlannerItem', 'plannable');
  }

  @override
  PlannerItem rebuild(void Function(PlannerItemBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PlannerItemBuilder toBuilder() => new PlannerItemBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is PlannerItem &&
        courseId == other.courseId &&
        contextType == other.contextType &&
        contextName == other.contextName &&
        plannableType == other.plannableType &&
        plannable == other.plannable &&
        plannableDate == other.plannableDate &&
        submissionStatusRaw == other.submissionStatusRaw &&
        htmlUrl == other.htmlUrl &&
        submissionStatus == other.submissionStatus;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, courseId.hashCode);
    _$hash = $jc(_$hash, contextType.hashCode);
    _$hash = $jc(_$hash, contextName.hashCode);
    _$hash = $jc(_$hash, plannableType.hashCode);
    _$hash = $jc(_$hash, plannable.hashCode);
    _$hash = $jc(_$hash, plannableDate.hashCode);
    _$hash = $jc(_$hash, submissionStatusRaw.hashCode);
    _$hash = $jc(_$hash, htmlUrl.hashCode);
    _$hash = $jc(_$hash, submissionStatus.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'PlannerItem')
          ..add('courseId', courseId)
          ..add('contextType', contextType)
          ..add('contextName', contextName)
          ..add('plannableType', plannableType)
          ..add('plannable', plannable)
          ..add('plannableDate', plannableDate)
          ..add('submissionStatusRaw', submissionStatusRaw)
          ..add('htmlUrl', htmlUrl)
          ..add('submissionStatus', submissionStatus))
        .toString();
  }
}

class PlannerItemBuilder implements Builder<PlannerItem, PlannerItemBuilder> {
  _$PlannerItem? _$v;

  String? _courseId;
  String? get courseId => _$this._courseId;
  set courseId(String? courseId) => _$this._courseId = courseId;

  String? _contextType;
  String? get contextType => _$this._contextType;
  set contextType(String? contextType) => _$this._contextType = contextType;

  String? _contextName;
  String? get contextName => _$this._contextName;
  set contextName(String? contextName) => _$this._contextName = contextName;

  String? _plannableType;
  String? get plannableType => _$this._plannableType;
  set plannableType(String? plannableType) =>
      _$this._plannableType = plannableType;

  PlannableBuilder? _plannable;
  PlannableBuilder get plannable =>
      _$this._plannable ??= new PlannableBuilder();
  set plannable(PlannableBuilder? plannable) => _$this._plannable = plannable;

  DateTime? _plannableDate;
  DateTime? get plannableDate => _$this._plannableDate;
  set plannableDate(DateTime? plannableDate) =>
      _$this._plannableDate = plannableDate;

  JsonObject? _submissionStatusRaw;
  JsonObject? get submissionStatusRaw => _$this._submissionStatusRaw;
  set submissionStatusRaw(JsonObject? submissionStatusRaw) =>
      _$this._submissionStatusRaw = submissionStatusRaw;

  String? _htmlUrl;
  String? get htmlUrl => _$this._htmlUrl;
  set htmlUrl(String? htmlUrl) => _$this._htmlUrl = htmlUrl;

  PlannerSubmissionBuilder? _submissionStatus;
  PlannerSubmissionBuilder get submissionStatus =>
      _$this._submissionStatus ??= new PlannerSubmissionBuilder();
  set submissionStatus(PlannerSubmissionBuilder? submissionStatus) =>
      _$this._submissionStatus = submissionStatus;

  PlannerItemBuilder();

  PlannerItemBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _courseId = $v.courseId;
      _contextType = $v.contextType;
      _contextName = $v.contextName;
      _plannableType = $v.plannableType;
      _plannable = $v.plannable.toBuilder();
      _plannableDate = $v.plannableDate;
      _submissionStatusRaw = $v.submissionStatusRaw;
      _htmlUrl = $v.htmlUrl;
      _submissionStatus = $v.submissionStatus?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PlannerItem other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$PlannerItem;
  }

  @override
  void update(void Function(PlannerItemBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  PlannerItem build() => _build();

  _$PlannerItem _build() {
    _$PlannerItem _$result;
    try {
      _$result = _$v ??
          new _$PlannerItem._(
              courseId: courseId,
              contextType: contextType,
              contextName: contextName,
              plannableType: BuiltValueNullFieldError.checkNotNull(
                  plannableType, r'PlannerItem', 'plannableType'),
              plannable: plannable.build(),
              plannableDate: plannableDate,
              submissionStatusRaw: submissionStatusRaw,
              htmlUrl: htmlUrl,
              submissionStatus: _submissionStatus?.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'plannable';
        plannable.build();

        _$failedField = 'submissionStatus';
        _submissionStatus?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'PlannerItem', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
