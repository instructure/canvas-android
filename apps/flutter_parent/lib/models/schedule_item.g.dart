// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'schedule_item.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<ScheduleItem> _$scheduleItemSerializer =
    new _$ScheduleItemSerializer();

class _$ScheduleItemSerializer implements StructuredSerializer<ScheduleItem> {
  @override
  final Iterable<Type> types = const [ScheduleItem, _$ScheduleItem];
  @override
  final String wireName = 'ScheduleItem';

  @override
  Iterable<Object?> serialize(Serializers serializers, ScheduleItem object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'all_day',
      serializers.serialize(object.isAllDay,
          specifiedType: const FullType(bool)),
      'type',
      serializers.serialize(object.type, specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.title;

    result
      ..add('title')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.description;

    result
      ..add('description')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.startAt;

    result
      ..add('start_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.endAt;

    result
      ..add('end_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.allDayDate;

    result
      ..add('all_day_date')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.locationAddress;

    result
      ..add('location_address')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.locationName;

    result
      ..add('location_name')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.htmlUrl;

    result
      ..add('html_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.contextCode;

    result
      ..add('context_code')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.effectiveContextCode;

    result
      ..add('effective_context_code')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.isHidden;

    result
      ..add('hidden')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));
    value = object.assignment;

    result
      ..add('assignment')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(Assignment)));
    value = object.assignmentOverrides;

    result
      ..add('assignment_overrides')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(
              BuiltList, const [const FullType(AssignmentOverride)])));

    return result;
  }

  @override
  ScheduleItem deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new ScheduleItemBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'title':
          result.title = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'description':
          result.description = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'start_at':
          result.startAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'end_at':
          result.endAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'all_day':
          result.isAllDay = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'all_day_date':
          result.allDayDate = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'location_address':
          result.locationAddress = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'type':
          result.type = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'location_name':
          result.locationName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'html_url':
          result.htmlUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'context_code':
          result.contextCode = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'effective_context_code':
          result.effectiveContextCode = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'hidden':
          result.isHidden = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool?;
          break;
        case 'assignment':
          result.assignment.replace(serializers.deserialize(value,
              specifiedType: const FullType(Assignment))! as Assignment);
          break;
        case 'assignment_overrides':
          result.assignmentOverrides.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(AssignmentOverride)]))!
              as BuiltList<Object?>);
          break;
      }
    }

    return result.build();
  }
}

class _$ScheduleItem extends ScheduleItem {
  @override
  final String id;
  @override
  final String? title;
  @override
  final String? description;
  @override
  final DateTime? startAt;
  @override
  final DateTime? endAt;
  @override
  final bool isAllDay;
  @override
  final DateTime? allDayDate;
  @override
  final String? locationAddress;
  @override
  final String type;
  @override
  final String? locationName;
  @override
  final String? htmlUrl;
  @override
  final String? contextCode;
  @override
  final String? effectiveContextCode;
  @override
  final bool? isHidden;
  @override
  final Assignment? assignment;
  @override
  final BuiltList<AssignmentOverride>? assignmentOverrides;

  factory _$ScheduleItem([void Function(ScheduleItemBuilder)? updates]) =>
      (new ScheduleItemBuilder()..update(updates))._build();

  _$ScheduleItem._(
      {required this.id,
      this.title,
      this.description,
      this.startAt,
      this.endAt,
      required this.isAllDay,
      this.allDayDate,
      this.locationAddress,
      required this.type,
      this.locationName,
      this.htmlUrl,
      this.contextCode,
      this.effectiveContextCode,
      this.isHidden,
      this.assignment,
      this.assignmentOverrides})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'ScheduleItem', 'id');
    BuiltValueNullFieldError.checkNotNull(
        isAllDay, r'ScheduleItem', 'isAllDay');
    BuiltValueNullFieldError.checkNotNull(type, r'ScheduleItem', 'type');
  }

  @override
  ScheduleItem rebuild(void Function(ScheduleItemBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  ScheduleItemBuilder toBuilder() => new ScheduleItemBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is ScheduleItem &&
        id == other.id &&
        title == other.title &&
        description == other.description &&
        startAt == other.startAt &&
        endAt == other.endAt &&
        isAllDay == other.isAllDay &&
        allDayDate == other.allDayDate &&
        locationAddress == other.locationAddress &&
        type == other.type &&
        locationName == other.locationName &&
        htmlUrl == other.htmlUrl &&
        contextCode == other.contextCode &&
        effectiveContextCode == other.effectiveContextCode &&
        isHidden == other.isHidden &&
        assignment == other.assignment &&
        assignmentOverrides == other.assignmentOverrides;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, title.hashCode);
    _$hash = $jc(_$hash, description.hashCode);
    _$hash = $jc(_$hash, startAt.hashCode);
    _$hash = $jc(_$hash, endAt.hashCode);
    _$hash = $jc(_$hash, isAllDay.hashCode);
    _$hash = $jc(_$hash, allDayDate.hashCode);
    _$hash = $jc(_$hash, locationAddress.hashCode);
    _$hash = $jc(_$hash, type.hashCode);
    _$hash = $jc(_$hash, locationName.hashCode);
    _$hash = $jc(_$hash, htmlUrl.hashCode);
    _$hash = $jc(_$hash, contextCode.hashCode);
    _$hash = $jc(_$hash, effectiveContextCode.hashCode);
    _$hash = $jc(_$hash, isHidden.hashCode);
    _$hash = $jc(_$hash, assignment.hashCode);
    _$hash = $jc(_$hash, assignmentOverrides.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'ScheduleItem')
          ..add('id', id)
          ..add('title', title)
          ..add('description', description)
          ..add('startAt', startAt)
          ..add('endAt', endAt)
          ..add('isAllDay', isAllDay)
          ..add('allDayDate', allDayDate)
          ..add('locationAddress', locationAddress)
          ..add('type', type)
          ..add('locationName', locationName)
          ..add('htmlUrl', htmlUrl)
          ..add('contextCode', contextCode)
          ..add('effectiveContextCode', effectiveContextCode)
          ..add('isHidden', isHidden)
          ..add('assignment', assignment)
          ..add('assignmentOverrides', assignmentOverrides))
        .toString();
  }
}

class ScheduleItemBuilder
    implements Builder<ScheduleItem, ScheduleItemBuilder> {
  _$ScheduleItem? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _title;
  String? get title => _$this._title;
  set title(String? title) => _$this._title = title;

  String? _description;
  String? get description => _$this._description;
  set description(String? description) => _$this._description = description;

  DateTime? _startAt;
  DateTime? get startAt => _$this._startAt;
  set startAt(DateTime? startAt) => _$this._startAt = startAt;

  DateTime? _endAt;
  DateTime? get endAt => _$this._endAt;
  set endAt(DateTime? endAt) => _$this._endAt = endAt;

  bool? _isAllDay;
  bool? get isAllDay => _$this._isAllDay;
  set isAllDay(bool? isAllDay) => _$this._isAllDay = isAllDay;

  DateTime? _allDayDate;
  DateTime? get allDayDate => _$this._allDayDate;
  set allDayDate(DateTime? allDayDate) => _$this._allDayDate = allDayDate;

  String? _locationAddress;
  String? get locationAddress => _$this._locationAddress;
  set locationAddress(String? locationAddress) =>
      _$this._locationAddress = locationAddress;

  String? _type;
  String? get type => _$this._type;
  set type(String? type) => _$this._type = type;

  String? _locationName;
  String? get locationName => _$this._locationName;
  set locationName(String? locationName) => _$this._locationName = locationName;

  String? _htmlUrl;
  String? get htmlUrl => _$this._htmlUrl;
  set htmlUrl(String? htmlUrl) => _$this._htmlUrl = htmlUrl;

  String? _contextCode;
  String? get contextCode => _$this._contextCode;
  set contextCode(String? contextCode) => _$this._contextCode = contextCode;

  String? _effectiveContextCode;
  String? get effectiveContextCode => _$this._effectiveContextCode;
  set effectiveContextCode(String? effectiveContextCode) =>
      _$this._effectiveContextCode = effectiveContextCode;

  bool? _isHidden;
  bool? get isHidden => _$this._isHidden;
  set isHidden(bool? isHidden) => _$this._isHidden = isHidden;

  AssignmentBuilder? _assignment;
  AssignmentBuilder get assignment =>
      _$this._assignment ??= new AssignmentBuilder();
  set assignment(AssignmentBuilder? assignment) =>
      _$this._assignment = assignment;

  ListBuilder<AssignmentOverride>? _assignmentOverrides;
  ListBuilder<AssignmentOverride> get assignmentOverrides =>
      _$this._assignmentOverrides ??= new ListBuilder<AssignmentOverride>();
  set assignmentOverrides(
          ListBuilder<AssignmentOverride>? assignmentOverrides) =>
      _$this._assignmentOverrides = assignmentOverrides;

  ScheduleItemBuilder() {
    ScheduleItem._initializeBuilder(this);
  }

  ScheduleItemBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _title = $v.title;
      _description = $v.description;
      _startAt = $v.startAt;
      _endAt = $v.endAt;
      _isAllDay = $v.isAllDay;
      _allDayDate = $v.allDayDate;
      _locationAddress = $v.locationAddress;
      _type = $v.type;
      _locationName = $v.locationName;
      _htmlUrl = $v.htmlUrl;
      _contextCode = $v.contextCode;
      _effectiveContextCode = $v.effectiveContextCode;
      _isHidden = $v.isHidden;
      _assignment = $v.assignment?.toBuilder();
      _assignmentOverrides = $v.assignmentOverrides?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(ScheduleItem other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$ScheduleItem;
  }

  @override
  void update(void Function(ScheduleItemBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  ScheduleItem build() => _build();

  _$ScheduleItem _build() {
    _$ScheduleItem _$result;
    try {
      _$result = _$v ??
          new _$ScheduleItem._(
              id: BuiltValueNullFieldError.checkNotNull(
                  id, r'ScheduleItem', 'id'),
              title: title,
              description: description,
              startAt: startAt,
              endAt: endAt,
              isAllDay: BuiltValueNullFieldError.checkNotNull(
                  isAllDay, r'ScheduleItem', 'isAllDay'),
              allDayDate: allDayDate,
              locationAddress: locationAddress,
              type: BuiltValueNullFieldError.checkNotNull(
                  type, r'ScheduleItem', 'type'),
              locationName: locationName,
              htmlUrl: htmlUrl,
              contextCode: contextCode,
              effectiveContextCode: effectiveContextCode,
              isHidden: isHidden,
              assignment: _assignment?.build(),
              assignmentOverrides: _assignmentOverrides?.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'assignment';
        _assignment?.build();
        _$failedField = 'assignmentOverrides';
        _assignmentOverrides?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'ScheduleItem', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
