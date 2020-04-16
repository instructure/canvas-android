// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'course_permissions.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CoursePermissions> _$coursePermissionsSerializer =
    new _$CoursePermissionsSerializer();

class _$CoursePermissionsSerializer
    implements StructuredSerializer<CoursePermissions> {
  @override
  final Iterable<Type> types = const [CoursePermissions, _$CoursePermissions];
  @override
  final String wireName = 'CoursePermissions';

  @override
  Iterable<Object> serialize(Serializers serializers, CoursePermissions object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[];
    if (object.sendMessages != null) {
      result
        ..add('send_messages')
        ..add(serializers.serialize(object.sendMessages,
            specifiedType: const FullType(bool)));
    }
    return result;
  }

  @override
  CoursePermissions deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CoursePermissionsBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      switch (key) {
        case 'send_messages':
          result.sendMessages = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$CoursePermissions extends CoursePermissions {
  @override
  final bool sendMessages;

  factory _$CoursePermissions(
          [void Function(CoursePermissionsBuilder) updates]) =>
      (new CoursePermissionsBuilder()..update(updates)).build();

  _$CoursePermissions._({this.sendMessages}) : super._();

  @override
  CoursePermissions rebuild(void Function(CoursePermissionsBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CoursePermissionsBuilder toBuilder() =>
      new CoursePermissionsBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CoursePermissions && sendMessages == other.sendMessages;
  }

  @override
  int get hashCode {
    return $jf($jc(0, sendMessages.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CoursePermissions')
          ..add('sendMessages', sendMessages))
        .toString();
  }
}

class CoursePermissionsBuilder
    implements Builder<CoursePermissions, CoursePermissionsBuilder> {
  _$CoursePermissions _$v;

  bool _sendMessages;
  bool get sendMessages => _$this._sendMessages;
  set sendMessages(bool sendMessages) => _$this._sendMessages = sendMessages;

  CoursePermissionsBuilder();

  CoursePermissionsBuilder get _$this {
    if (_$v != null) {
      _sendMessages = _$v.sendMessages;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CoursePermissions other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CoursePermissions;
  }

  @override
  void update(void Function(CoursePermissionsBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CoursePermissions build() {
    final _$result =
        _$v ?? new _$CoursePermissions._(sendMessages: sendMessages);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
