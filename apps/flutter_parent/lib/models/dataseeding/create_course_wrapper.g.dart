// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_course_wrapper.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CreateCourseWrapper> _$createCourseWrapperSerializer =
    new _$CreateCourseWrapperSerializer();

class _$CreateCourseWrapperSerializer
    implements StructuredSerializer<CreateCourseWrapper> {
  @override
  final Iterable<Type> types = const [
    CreateCourseWrapper,
    _$CreateCourseWrapper
  ];
  @override
  final String wireName = 'CreateCourseWrapper';

  @override
  Iterable<Object?> serialize(
      Serializers serializers, CreateCourseWrapper object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'course',
      serializers.serialize(object.course,
          specifiedType: const FullType(CreateCourseInfo)),
      'offer',
      serializers.serialize(object.offer, specifiedType: const FullType(bool)),
    ];

    return result;
  }

  @override
  CreateCourseWrapper deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateCourseWrapperBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'course':
          result.course.replace(serializers.deserialize(value,
                  specifiedType: const FullType(CreateCourseInfo))!
              as CreateCourseInfo);
          break;
        case 'offer':
          result.offer = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$CreateCourseWrapper extends CreateCourseWrapper {
  @override
  final CreateCourseInfo course;
  @override
  final bool offer;

  factory _$CreateCourseWrapper(
          [void Function(CreateCourseWrapperBuilder)? updates]) =>
      (new CreateCourseWrapperBuilder()..update(updates))._build();

  _$CreateCourseWrapper._({required this.course, required this.offer})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        course, r'CreateCourseWrapper', 'course');
    BuiltValueNullFieldError.checkNotNull(
        offer, r'CreateCourseWrapper', 'offer');
  }

  @override
  CreateCourseWrapper rebuild(
          void Function(CreateCourseWrapperBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CreateCourseWrapperBuilder toBuilder() =>
      new CreateCourseWrapperBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CreateCourseWrapper &&
        course == other.course &&
        offer == other.offer;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, course.hashCode);
    _$hash = $jc(_$hash, offer.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CreateCourseWrapper')
          ..add('course', course)
          ..add('offer', offer))
        .toString();
  }
}

class CreateCourseWrapperBuilder
    implements Builder<CreateCourseWrapper, CreateCourseWrapperBuilder> {
  _$CreateCourseWrapper? _$v;

  CreateCourseInfoBuilder? _course;
  CreateCourseInfoBuilder get course =>
      _$this._course ??= new CreateCourseInfoBuilder();
  set course(CreateCourseInfoBuilder? course) => _$this._course = course;

  bool? _offer;
  bool? get offer => _$this._offer;
  set offer(bool? offer) => _$this._offer = offer;

  CreateCourseWrapperBuilder() {
    CreateCourseWrapper._initializeBuilder(this);
  }

  CreateCourseWrapperBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _course = $v.course.toBuilder();
      _offer = $v.offer;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateCourseWrapper other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CreateCourseWrapper;
  }

  @override
  void update(void Function(CreateCourseWrapperBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CreateCourseWrapper build() => _build();

  _$CreateCourseWrapper _build() {
    _$CreateCourseWrapper _$result;
    try {
      _$result = _$v ??
          new _$CreateCourseWrapper._(
              course: course.build(),
              offer: BuiltValueNullFieldError.checkNotNull(
                  offer, r'CreateCourseWrapper', 'offer'));
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'course';
        course.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'CreateCourseWrapper', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
