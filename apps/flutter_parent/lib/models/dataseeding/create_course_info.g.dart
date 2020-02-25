// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_course_info.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CreateCourseInfo> _$createCourseInfoSerializer =
    new _$CreateCourseInfoSerializer();

class _$CreateCourseInfoSerializer
    implements StructuredSerializer<CreateCourseInfo> {
  @override
  final Iterable<Type> types = const [CreateCourseInfo, _$CreateCourseInfo];
  @override
  final String wireName = 'CreateCourseInfo';

  @override
  Iterable<Object> serialize(Serializers serializers, CreateCourseInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
      'course_code',
      serializers.serialize(object.courseCode,
          specifiedType: const FullType(String)),
      'role',
      serializers.serialize(object.role, specifiedType: const FullType(String)),
    ];
    result.add('enrollment_term_id');
    if (object.enrollmentTermId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.enrollmentTermId,
          specifiedType: const FullType(int)));
    }
    return result;
  }

  @override
  CreateCourseInfo deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateCourseInfoBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'course_code':
          result.courseCode = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'enrollment_term_id':
          result.enrollmentTermId = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'role':
          result.role = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$CreateCourseInfo extends CreateCourseInfo {
  @override
  final String name;
  @override
  final String courseCode;
  @override
  final int enrollmentTermId;
  @override
  final String role;

  factory _$CreateCourseInfo(
          [void Function(CreateCourseInfoBuilder) updates]) =>
      (new CreateCourseInfoBuilder()..update(updates)).build();

  _$CreateCourseInfo._(
      {this.name, this.courseCode, this.enrollmentTermId, this.role})
      : super._() {
    if (name == null) {
      throw new BuiltValueNullFieldError('CreateCourseInfo', 'name');
    }
    if (courseCode == null) {
      throw new BuiltValueNullFieldError('CreateCourseInfo', 'courseCode');
    }
    if (role == null) {
      throw new BuiltValueNullFieldError('CreateCourseInfo', 'role');
    }
  }

  @override
  CreateCourseInfo rebuild(void Function(CreateCourseInfoBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateCourseInfoBuilder toBuilder() =>
      new CreateCourseInfoBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateCourseInfo &&
        name == other.name &&
        courseCode == other.courseCode &&
        enrollmentTermId == other.enrollmentTermId &&
        role == other.role;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc($jc($jc(0, name.hashCode), courseCode.hashCode),
            enrollmentTermId.hashCode),
        role.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CreateCourseInfo')
          ..add('name', name)
          ..add('courseCode', courseCode)
          ..add('enrollmentTermId', enrollmentTermId)
          ..add('role', role))
        .toString();
  }
}

class CreateCourseInfoBuilder
    implements Builder<CreateCourseInfo, CreateCourseInfoBuilder> {
  _$CreateCourseInfo _$v;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  String _courseCode;
  String get courseCode => _$this._courseCode;
  set courseCode(String courseCode) => _$this._courseCode = courseCode;

  int _enrollmentTermId;
  int get enrollmentTermId => _$this._enrollmentTermId;
  set enrollmentTermId(int enrollmentTermId) =>
      _$this._enrollmentTermId = enrollmentTermId;

  String _role;
  String get role => _$this._role;
  set role(String role) => _$this._role = role;

  CreateCourseInfoBuilder() {
    CreateCourseInfo._initializeBuilder(this);
  }

  CreateCourseInfoBuilder get _$this {
    if (_$v != null) {
      _name = _$v.name;
      _courseCode = _$v.courseCode;
      _enrollmentTermId = _$v.enrollmentTermId;
      _role = _$v.role;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateCourseInfo other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CreateCourseInfo;
  }

  @override
  void update(void Function(CreateCourseInfoBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CreateCourseInfo build() {
    final _$result = _$v ??
        new _$CreateCourseInfo._(
            name: name,
            courseCode: courseCode,
            enrollmentTermId: enrollmentTermId,
            role: role);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
