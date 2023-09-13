// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_color.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$UserColor extends UserColor {
  @override
  final int? id;
  @override
  final String userDomain;
  @override
  final String userId;
  @override
  final String canvasContext;
  @override
  final Color color;

  factory _$UserColor([void Function(UserColorBuilder)? updates]) =>
      (new UserColorBuilder()..update(updates))._build();

  _$UserColor._(
      {this.id,
      required this.userDomain,
      required this.userId,
      required this.canvasContext,
      required this.color})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        userDomain, r'UserColor', 'userDomain');
    BuiltValueNullFieldError.checkNotNull(userId, r'UserColor', 'userId');
    BuiltValueNullFieldError.checkNotNull(
        canvasContext, r'UserColor', 'canvasContext');
    BuiltValueNullFieldError.checkNotNull(color, r'UserColor', 'color');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, userDomain.hashCode);
    _$hash = $jc(_$hash, userId.hashCode);
    _$hash = $jc(_$hash, canvasContext.hashCode);
    _$hash = $jc(_$hash, color.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'UserColor')
          ..add('id', id)
          ..add('userDomain', userDomain)
          ..add('userId', userId)
          ..add('canvasContext', canvasContext)
          ..add('color', color))
        .toString();
  }
}

class UserColorBuilder implements Builder<UserColor, UserColorBuilder> {
  _$UserColor? _$v;

  int? _id;
  int? get id => _$this._id;
  set id(int? id) => _$this._id = id;

  String? _userDomain;
  String? get userDomain => _$this._userDomain;
  set userDomain(String? userDomain) => _$this._userDomain = userDomain;

  String? _userId;
  String? get userId => _$this._userId;
  set userId(String? userId) => _$this._userId = userId;

  String? _canvasContext;
  String? get canvasContext => _$this._canvasContext;
  set canvasContext(String? canvasContext) =>
      _$this._canvasContext = canvasContext;

  Color? _color;
  Color? get color => _$this._color;
  set color(Color? color) => _$this._color = color;

  UserColorBuilder() {
    UserColor._initializeBuilder(this);
  }

  UserColorBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _userDomain = $v.userDomain;
      _userId = $v.userId;
      _canvasContext = $v.canvasContext;
      _color = $v.color;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(UserColor other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$UserColor;
  }

  @override
  void update(void Function(UserColorBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  UserColor build() => _build();

  _$UserColor _build() {
    final _$result = _$v ??
        new _$UserColor._(
            id: id,
            userDomain: BuiltValueNullFieldError.checkNotNull(
                userDomain, r'UserColor', 'userDomain'),
            userId: BuiltValueNullFieldError.checkNotNull(
                userId, r'UserColor', 'userId'),
            canvasContext: BuiltValueNullFieldError.checkNotNull(
                canvasContext, r'UserColor', 'canvasContext'),
            color: BuiltValueNullFieldError.checkNotNull(
                color, r'UserColor', 'color'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
