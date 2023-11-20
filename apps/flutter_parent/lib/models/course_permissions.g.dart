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
  Iterable<Object?> serialize(Serializers serializers, CoursePermissions object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[];
    Object? value;
    value = object.sendMessages;
    if (value != null) {
      result
        ..add('send_messages')
        ..add(
            serializers.serialize(value, specifiedType: const FullType(bool)));
    }
    return result;
  }

  @override
  CoursePermissions deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CoursePermissionsBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'send_messages':
          result.sendMessages = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool?;
          break;
      }
    }

    return result.build();
  }
}

class _$CoursePermissions extends CoursePermissions {
  @override
  final bool? sendMessages;

  factory _$CoursePermissions(
          [void Function(CoursePermissionsBuilder)? updates]) =>
      (new CoursePermissionsBuilder()..update(updates))._build();

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
    var _$hash = 0;
    _$hash = $jc(_$hash, sendMessages.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CoursePermissions')
          ..add('sendMessages', sendMessages))
        .toString();
  }
}

class CoursePermissionsBuilder
    implements Builder<CoursePermissions, CoursePermissionsBuilder> {
  _$CoursePermissions? _$v;

  bool? _sendMessages;
  bool? get sendMessages => _$this._sendMessages;
  set sendMessages(bool? sendMessages) => _$this._sendMessages = sendMessages;

  CoursePermissionsBuilder();

  CoursePermissionsBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _sendMessages = $v.sendMessages;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CoursePermissions other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CoursePermissions;
  }

  @override
  void update(void Function(CoursePermissionsBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CoursePermissions build() => _build();

  _$CoursePermissions _build() {
    final _$result =
        _$v ?? new _$CoursePermissions._(sendMessages: sendMessages);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
