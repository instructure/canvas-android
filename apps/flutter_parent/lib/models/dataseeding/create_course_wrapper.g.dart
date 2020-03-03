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
  Iterable<Object> serialize(
      Serializers serializers, CreateCourseWrapper object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
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
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CreateCourseWrapperBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'course':
          result.course.replace(serializers.deserialize(value,
                  specifiedType: const FullType(CreateCourseInfo))
              as CreateCourseInfo);
          break;
        case 'offer':
          result.offer = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
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
          [void Function(CreateCourseWrapperBuilder) updates]) =>
      (new CreateCourseWrapperBuilder()..update(updates)).build();

  _$CreateCourseWrapper._({this.course, this.offer}) : super._() {
    if (course == null) {
      throw new BuiltValueNullFieldError('CreateCourseWrapper', 'course');
    }
    if (offer == null) {
      throw new BuiltValueNullFieldError('CreateCourseWrapper', 'offer');
    }
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
    return $jf($jc($jc(0, course.hashCode), offer.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CreateCourseWrapper')
          ..add('course', course)
          ..add('offer', offer))
        .toString();
  }
}

class CreateCourseWrapperBuilder
    implements Builder<CreateCourseWrapper, CreateCourseWrapperBuilder> {
  _$CreateCourseWrapper _$v;

  CreateCourseInfoBuilder _course;
  CreateCourseInfoBuilder get course =>
      _$this._course ??= new CreateCourseInfoBuilder();
  set course(CreateCourseInfoBuilder course) => _$this._course = course;

  bool _offer;
  bool get offer => _$this._offer;
  set offer(bool offer) => _$this._offer = offer;

  CreateCourseWrapperBuilder() {
    CreateCourseWrapper._initializeBuilder(this);
  }

  CreateCourseWrapperBuilder get _$this {
    if (_$v != null) {
      _course = _$v.course?.toBuilder();
      _offer = _$v.offer;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CreateCourseWrapper other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CreateCourseWrapper;
  }

  @override
  void update(void Function(CreateCourseWrapperBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CreateCourseWrapper build() {
    _$CreateCourseWrapper _$result;
    try {
      _$result = _$v ??
          new _$CreateCourseWrapper._(course: course.build(), offer: offer);
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'course';
        course.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'CreateCourseWrapper', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
