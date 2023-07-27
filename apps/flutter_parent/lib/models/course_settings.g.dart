// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'course_settings.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CourseSettings> _$courseSettingsSerializer =
    new _$CourseSettingsSerializer();

class _$CourseSettingsSerializer
    implements StructuredSerializer<CourseSettings> {
  @override
  final Iterable<Type> types = const [CourseSettings, _$CourseSettings];
  @override
  final String wireName = 'CourseSettings';

  @override
  Iterable<Object> serialize(Serializers serializers, CourseSettings object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[];
    Object value;
    value = object.courseSummary;
    if (value != null) {
      result
        ..add('syllabus_course_summary')
        ..add(
            serializers.serialize(value, specifiedType: const FullType(bool)));
    }
    value = object.restrictQuantitativeData;
    if (value != null) {
      result
        ..add('restrict_quantitative_data')
        ..add(
            serializers.serialize(value, specifiedType: const FullType(bool)));
    }
    return result;
  }

  @override
  CourseSettings deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CourseSettingsBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final Object value = iterator.current;
      switch (key) {
        case 'syllabus_course_summary':
          result.courseSummary = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'restrict_quantitative_data':
          result.restrictQuantitativeData = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$CourseSettings extends CourseSettings {
  @override
  final bool courseSummary;
  @override
  final bool restrictQuantitativeData;

  factory _$CourseSettings([void Function(CourseSettingsBuilder) updates]) =>
      (new CourseSettingsBuilder()..update(updates)).build();

  _$CourseSettings._({this.courseSummary, this.restrictQuantitativeData})
      : super._();

  @override
  CourseSettings rebuild(void Function(CourseSettingsBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CourseSettingsBuilder toBuilder() =>
      new CourseSettingsBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CourseSettings &&
        courseSummary == other.courseSummary &&
        restrictQuantitativeData == other.restrictQuantitativeData;
  }

  @override
  int get hashCode {
    return $jf(
        $jc($jc(0, courseSummary.hashCode), restrictQuantitativeData.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CourseSettings')
          ..add('courseSummary', courseSummary)
          ..add('restrictQuantitativeData', restrictQuantitativeData))
        .toString();
  }
}

class CourseSettingsBuilder
    implements Builder<CourseSettings, CourseSettingsBuilder> {
  _$CourseSettings _$v;

  bool _courseSummary;
  bool get courseSummary => _$this._courseSummary;
  set courseSummary(bool courseSummary) =>
      _$this._courseSummary = courseSummary;

  bool _restrictQuantitativeData;
  bool get restrictQuantitativeData => _$this._restrictQuantitativeData;
  set restrictQuantitativeData(bool restrictQuantitativeData) =>
      _$this._restrictQuantitativeData = restrictQuantitativeData;

  CourseSettingsBuilder();

  CourseSettingsBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _courseSummary = $v.courseSummary;
      _restrictQuantitativeData = $v.restrictQuantitativeData;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CourseSettings other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CourseSettings;
  }

  @override
  void update(void Function(CourseSettingsBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CourseSettings build() {
    final _$result = _$v ??
        new _$CourseSettings._(
            courseSummary: courseSummary,
            restrictQuantitativeData: restrictQuantitativeData);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,deprecated_member_use_from_same_package,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
