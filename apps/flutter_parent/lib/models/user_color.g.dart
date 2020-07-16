// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_color.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$UserColor extends UserColor {
  @override
  final int id;
  @override
  final String userDomain;
  @override
  final String userId;
  @override
  final String canvasContext;
  @override
  final Color color;

  factory _$UserColor([void Function(UserColorBuilder) updates]) =>
      (new UserColorBuilder()..update(updates)).build();

  _$UserColor._(
      {this.id, this.userDomain, this.userId, this.canvasContext, this.color})
      : super._() {
    if (userDomain == null) {
      throw new BuiltValueNullFieldError('UserColor', 'userDomain');
    }
    if (userId == null) {
      throw new BuiltValueNullFieldError('UserColor', 'userId');
    }
    if (canvasContext == null) {
      throw new BuiltValueNullFieldError('UserColor', 'canvasContext');
    }
    if (color == null) {
      throw new BuiltValueNullFieldError('UserColor', 'color');
    }
  }

  @override
  UserColor rebuild(void Function(UserColorBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  UserColorBuilder toBuilder() => new UserColorBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is UserColor &&
        id == other.id &&
        userDomain == other.userDomain &&
        userId == other.userId &&
        canvasContext == other.canvasContext &&
        color == other.color;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc($jc($jc($jc(0, id.hashCode), userDomain.hashCode), userId.hashCode),
            canvasContext.hashCode),
        color.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('UserColor')
          ..add('id', id)
          ..add('userDomain', userDomain)
          ..add('userId', userId)
          ..add('canvasContext', canvasContext)
          ..add('color', color))
        .toString();
  }
}

class UserColorBuilder implements Builder<UserColor, UserColorBuilder> {
  _$UserColor _$v;

  int _id;
  int get id => _$this._id;
  set id(int id) => _$this._id = id;

  String _userDomain;
  String get userDomain => _$this._userDomain;
  set userDomain(String userDomain) => _$this._userDomain = userDomain;

  String _userId;
  String get userId => _$this._userId;
  set userId(String userId) => _$this._userId = userId;

  String _canvasContext;
  String get canvasContext => _$this._canvasContext;
  set canvasContext(String canvasContext) =>
      _$this._canvasContext = canvasContext;

  Color _color;
  Color get color => _$this._color;
  set color(Color color) => _$this._color = color;

  UserColorBuilder() {
    UserColor._initializeBuilder(this);
  }

  UserColorBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _userDomain = _$v.userDomain;
      _userId = _$v.userId;
      _canvasContext = _$v.canvasContext;
      _color = _$v.color;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(UserColor other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$UserColor;
  }

  @override
  void update(void Function(UserColorBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$UserColor build() {
    final _$result = _$v ??
        new _$UserColor._(
            id: id,
            userDomain: userDomain,
            userId: userId,
            canvasContext: canvasContext,
            color: color);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
