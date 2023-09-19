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
  Iterable<Object?> serialize(Serializers serializers, CreateCourseInfo object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
      'course_code',
      serializers.serialize(object.courseCode,
          specifiedType: const FullType(String)),
      'role',
      serializers.serialize(object.role, specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.enrollmentTermId;

    result
      ..add('enrollment_term_id')
      ..add(serializers.serialize(value, specifiedType: const FullType(int)));
    value = object.syllabusBody;

    result
      ..add('syllabus_body')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  CreateCourseInfo deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateCourseInfoBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'course_code':
          result.courseCode = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'enrollment_term_id':
          result.enrollmentTermId = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int?;
          break;
        case 'role':
          result.role = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'syllabus_body':
          result.syllabusBody = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
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
  final int? enrollmentTermId;
  @override
  final String role;
  @override
  final String? syllabusBody;

  factory _$CreateCourseInfo(
          [void Function(CreateCourseInfoBuilder)? updates]) =>
      (new CreateCourseInfoBuilder()..update(updates))._build();

  _$CreateCourseInfo._(
      {required this.name,
      required this.courseCode,
      this.enrollmentTermId,
      required this.role,
      this.syllabusBody})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(name, r'CreateCourseInfo', 'name');
    BuiltValueNullFieldError.checkNotNull(
        courseCode, r'CreateCourseInfo', 'courseCode');
    BuiltValueNullFieldError.checkNotNull(role, r'CreateCourseInfo', 'role');
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
        role == other.role &&
        syllabusBody == other.syllabusBody;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, courseCode.hashCode);
    _$hash = $jc(_$hash, enrollmentTermId.hashCode);
    _$hash = $jc(_$hash, role.hashCode);
    _$hash = $jc(_$hash, syllabusBody.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateCourseInfo')
          ..add('name', name)
          ..add('courseCode', courseCode)
          ..add('enrollmentTermId', enrollmentTermId)
          ..add('role', role)
          ..add('syllabusBody', syllabusBody))
        .toString();
  }
}

class CreateCourseInfoBuilder
    implements Builder<CreateCourseInfo, CreateCourseInfoBuilder> {
  _$CreateCourseInfo? _$v;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  String? _courseCode;
  String? get courseCode => _$this._courseCode;
  set courseCode(String? courseCode) => _$this._courseCode = courseCode;

  int? _enrollmentTermId;
  int? get enrollmentTermId => _$this._enrollmentTermId;
  set enrollmentTermId(int? enrollmentTermId) =>
      _$this._enrollmentTermId = enrollmentTermId;

  String? _role;
  String? get role => _$this._role;
  set role(String? role) => _$this._role = role;

  String? _syllabusBody;
  String? get syllabusBody => _$this._syllabusBody;
  set syllabusBody(String? syllabusBody) => _$this._syllabusBody = syllabusBody;

  CreateCourseInfoBuilder() {
    CreateCourseInfo._initializeBuilder(this);
  }

  CreateCourseInfoBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _name = $v.name;
      _courseCode = $v.courseCode;
      _enrollmentTermId = $v.enrollmentTermId;
      _role = $v.role;
      _syllabusBody = $v.syllabusBody;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateCourseInfo other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CreateCourseInfo;
  }

  @override
  void update(void Function(CreateCourseInfoBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateCourseInfo build() => _build();

  _$CreateCourseInfo _build() {
    final _$result = _$v ??
        new _$CreateCourseInfo._(
            name: BuiltValueNullFieldError.checkNotNull(
                name, r'CreateCourseInfo', 'name'),
            courseCode: BuiltValueNullFieldError.checkNotNull(
                courseCode, r'CreateCourseInfo', 'courseCode'),
            enrollmentTermId: enrollmentTermId,
            role: BuiltValueNullFieldError.checkNotNull(
                role, r'CreateCourseInfo', 'role'),
            syllabusBody: syllabusBody);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
