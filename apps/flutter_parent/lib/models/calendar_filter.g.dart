// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'calendar_filter.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$CalendarFilter extends CalendarFilter {
  @override
  final int? id;
  @override
  final String userDomain;
  @override
  final String userId;
  @override
  final String observeeId;
  @override
  final BuiltSet<String> filters;

  factory _$CalendarFilter([void Function(CalendarFilterBuilder)? updates]) =>
      (new CalendarFilterBuilder()..update(updates))._build();

  _$CalendarFilter._(
      {this.id,
      required this.userDomain,
      required this.userId,
      required this.observeeId,
      required this.filters})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        userDomain, r'CalendarFilter', 'userDomain');
    BuiltValueNullFieldError.checkNotNull(userId, r'CalendarFilter', 'userId');
    BuiltValueNullFieldError.checkNotNull(
        observeeId, r'CalendarFilter', 'observeeId');
    BuiltValueNullFieldError.checkNotNull(
        filters, r'CalendarFilter', 'filters');
  }

  @override
  CalendarFilter rebuild(void Function(CalendarFilterBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CalendarFilterBuilder toBuilder() =>
      new CalendarFilterBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CalendarFilter &&
        id == other.id &&
        userDomain == other.userDomain &&
        userId == other.userId &&
        observeeId == other.observeeId &&
        filters == other.filters;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, userDomain.hashCode);
    _$hash = $jc(_$hash, userId.hashCode);
    _$hash = $jc(_$hash, observeeId.hashCode);
    _$hash = $jc(_$hash, filters.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CalendarFilter')
          ..add('id', id)
          ..add('userDomain', userDomain)
          ..add('userId', userId)
          ..add('observeeId', observeeId)
          ..add('filters', filters))
        .toString();
  }
}

class CalendarFilterBuilder
    implements Builder<CalendarFilter, CalendarFilterBuilder> {
  _$CalendarFilter? _$v;

  int? _id;
  int? get id => _$this._id;
  set id(int? id) => _$this._id = id;

  String? _userDomain;
  String? get userDomain => _$this._userDomain;
  set userDomain(String? userDomain) => _$this._userDomain = userDomain;

  String? _userId;
  String? get userId => _$this._userId;
  set userId(String? userId) => _$this._userId = userId;

  String? _observeeId;
  String? get observeeId => _$this._observeeId;
  set observeeId(String? observeeId) => _$this._observeeId = observeeId;

  SetBuilder<String>? _filters;
  SetBuilder<String> get filters =>
      _$this._filters ??= new SetBuilder<String>();
  set filters(SetBuilder<String>? filters) => _$this._filters = filters;

  CalendarFilterBuilder() {
    CalendarFilter._initializeBuilder(this);
  }

  CalendarFilterBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _userDomain = $v.userDomain;
      _userId = $v.userId;
      _observeeId = $v.observeeId;
      _filters = $v.filters.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CalendarFilter other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CalendarFilter;
  }

  @override
  void update(void Function(CalendarFilterBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CalendarFilter build() => _build();

  _$CalendarFilter _build() {
    _$CalendarFilter _$result;
    try {
      _$result = _$v ??
          new _$CalendarFilter._(
              id: id,
              userDomain: BuiltValueNullFieldError.checkNotNull(
                  userDomain, r'CalendarFilter', 'userDomain'),
              userId: BuiltValueNullFieldError.checkNotNull(
                  userId, r'CalendarFilter', 'userId'),
              observeeId: BuiltValueNullFieldError.checkNotNull(
                  observeeId, r'CalendarFilter', 'observeeId'),
              filters: filters.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'filters';
        filters.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'CalendarFilter', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
