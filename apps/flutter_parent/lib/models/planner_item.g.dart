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
  Iterable<Object> serialize(Serializers serializers, PlannerItem object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'context_type',
      serializers.serialize(object.contextType,
          specifiedType: const FullType(String)),
      'context_name',
      serializers.serialize(object.contextName,
          specifiedType: const FullType(String)),
      'plannable_type',
      serializers.serialize(object.plannableType,
          specifiedType: const FullType(String)),
      'plannable',
      serializers.serialize(object.plannable,
          specifiedType: const FullType(Plannable)),
      'plannable_date',
      serializers.serialize(object.plannableDate,
          specifiedType: const FullType(DateTime)),
      'html_url',
      serializers.serialize(object.htmlUrl,
          specifiedType: const FullType(String)),
    ];
    result.add('course_id');
    if (object.courseId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.courseId,
          specifiedType: const FullType(String)));
    }
    result.add('submissions');
    if (object.submissionStatusRaw == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.submissionStatusRaw,
          specifiedType: const FullType(JsonObject)));
    }
    return result;
  }

  @override
  PlannerItem deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PlannerItemBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'course_id':
          result.courseId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'context_type':
          result.contextType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'context_name':
          result.contextName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'plannable_type':
          result.plannableType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'plannable':
          result.plannable.replace(serializers.deserialize(value,
              specifiedType: const FullType(Plannable)) as Plannable);
          break;
        case 'plannable_date':
          result.plannableDate = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'submissions':
          result.submissionStatusRaw = serializers.deserialize(value,
              specifiedType: const FullType(JsonObject)) as JsonObject;
          break;
        case 'html_url':
          result.htmlUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$PlannerItem extends PlannerItem {
  @override
  final String courseId;
  @override
  final String contextType;
  @override
  final String contextName;
  @override
  final String plannableType;
  @override
  final Plannable plannable;
  @override
  final DateTime plannableDate;
  @override
  final JsonObject submissionStatusRaw;
  @override
  final String htmlUrl;

  factory _$PlannerItem([void Function(PlannerItemBuilder) updates]) =>
      (new PlannerItemBuilder()..update(updates)).build();

  _$PlannerItem._(
      {this.courseId,
      this.contextType,
      this.contextName,
      this.plannableType,
      this.plannable,
      this.plannableDate,
      this.submissionStatusRaw,
      this.htmlUrl})
      : super._() {
    if (contextType == null) {
      throw new BuiltValueNullFieldError('PlannerItem', 'contextType');
    }
    if (contextName == null) {
      throw new BuiltValueNullFieldError('PlannerItem', 'contextName');
    }
    if (plannableType == null) {
      throw new BuiltValueNullFieldError('PlannerItem', 'plannableType');
    }
    if (plannable == null) {
      throw new BuiltValueNullFieldError('PlannerItem', 'plannable');
    }
    if (plannableDate == null) {
      throw new BuiltValueNullFieldError('PlannerItem', 'plannableDate');
    }
    if (htmlUrl == null) {
      throw new BuiltValueNullFieldError('PlannerItem', 'htmlUrl');
    }
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
        htmlUrl == other.htmlUrl;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc(
                        $jc(
                            $jc($jc(0, courseId.hashCode),
                                contextType.hashCode),
                            contextName.hashCode),
                        plannableType.hashCode),
                    plannable.hashCode),
                plannableDate.hashCode),
            submissionStatusRaw.hashCode),
        htmlUrl.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('PlannerItem')
          ..add('courseId', courseId)
          ..add('contextType', contextType)
          ..add('contextName', contextName)
          ..add('plannableType', plannableType)
          ..add('plannable', plannable)
          ..add('plannableDate', plannableDate)
          ..add('submissionStatusRaw', submissionStatusRaw)
          ..add('htmlUrl', htmlUrl))
        .toString();
  }
}

class PlannerItemBuilder implements Builder<PlannerItem, PlannerItemBuilder> {
  _$PlannerItem _$v;

  String _courseId;
  String get courseId => _$this._courseId;
  set courseId(String courseId) => _$this._courseId = courseId;

  String _contextType;
  String get contextType => _$this._contextType;
  set contextType(String contextType) => _$this._contextType = contextType;

  String _contextName;
  String get contextName => _$this._contextName;
  set contextName(String contextName) => _$this._contextName = contextName;

  String _plannableType;
  String get plannableType => _$this._plannableType;
  set plannableType(String plannableType) =>
      _$this._plannableType = plannableType;

  PlannableBuilder _plannable;
  PlannableBuilder get plannable =>
      _$this._plannable ??= new PlannableBuilder();
  set plannable(PlannableBuilder plannable) => _$this._plannable = plannable;

  DateTime _plannableDate;
  DateTime get plannableDate => _$this._plannableDate;
  set plannableDate(DateTime plannableDate) =>
      _$this._plannableDate = plannableDate;

  JsonObject _submissionStatusRaw;
  JsonObject get submissionStatusRaw => _$this._submissionStatusRaw;
  set submissionStatusRaw(JsonObject submissionStatusRaw) =>
      _$this._submissionStatusRaw = submissionStatusRaw;

  String _htmlUrl;
  String get htmlUrl => _$this._htmlUrl;
  set htmlUrl(String htmlUrl) => _$this._htmlUrl = htmlUrl;

  PlannerItemBuilder();

  PlannerItemBuilder get _$this {
    if (_$v != null) {
      _courseId = _$v.courseId;
      _contextType = _$v.contextType;
      _contextName = _$v.contextName;
      _plannableType = _$v.plannableType;
      _plannable = _$v.plannable?.toBuilder();
      _plannableDate = _$v.plannableDate;
      _submissionStatusRaw = _$v.submissionStatusRaw;
      _htmlUrl = _$v.htmlUrl;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(PlannerItem other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$PlannerItem;
  }

  @override
  void update(void Function(PlannerItemBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$PlannerItem build() {
    _$PlannerItem _$result;
    try {
      _$result = _$v ??
          new _$PlannerItem._(
              courseId: courseId,
              contextType: contextType,
              contextName: contextName,
              plannableType: plannableType,
              plannable: plannable.build(),
              plannableDate: plannableDate,
              submissionStatusRaw: submissionStatusRaw,
              htmlUrl: htmlUrl);
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'plannable';
        plannable.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'PlannerItem', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
