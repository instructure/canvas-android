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
    if (object.courseSummary != null) {
      result
        ..add('syllabus_course_summary')
        ..add(serializers.serialize(object.courseSummary,
            specifiedType: const FullType(bool)));
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
      final dynamic value = iterator.current;
      switch (key) {
        case 'syllabus_course_summary':
          result.courseSummary = serializers.deserialize(value,
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

  factory _$CourseSettings([void Function(CourseSettingsBuilder) updates]) =>
      (new CourseSettingsBuilder()..update(updates)).build();

  _$CourseSettings._({this.courseSummary}) : super._();

  @override
  CourseSettings rebuild(void Function(CourseSettingsBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CourseSettingsBuilder toBuilder() =>
      new CourseSettingsBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CourseSettings && courseSummary == other.courseSummary;
  }

  @override
  int get hashCode {
    return $jf($jc(0, courseSummary.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CourseSettings')
          ..add('courseSummary', courseSummary))
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

  CourseSettingsBuilder();

  CourseSettingsBuilder get _$this {
    if (_$v != null) {
      _courseSummary = _$v.courseSummary;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CourseSettings other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CourseSettings;
  }

  @override
  void update(void Function(CourseSettingsBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CourseSettings build() {
    final _$result =
        _$v ?? new _$CourseSettings._(courseSummary: courseSummary);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
