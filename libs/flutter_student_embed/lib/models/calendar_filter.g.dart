// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'calendar_filter.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

class _$CalendarFilter extends CalendarFilter {
  @override
  final int id;
  @override
  final String userDomain;
  @override
  final String userId;
  @override
  final BuiltSet<String> filters;

  factory _$CalendarFilter([void Function(CalendarFilterBuilder) updates]) =>
      (new CalendarFilterBuilder()..update(updates)).build();

  _$CalendarFilter._({this.id, this.userDomain, this.userId, this.filters})
      : super._() {
    if (userDomain == null) {
      throw new BuiltValueNullFieldError('CalendarFilter', 'userDomain');
    }
    if (userId == null) {
      throw new BuiltValueNullFieldError('CalendarFilter', 'userId');
    }
    if (filters == null) {
      throw new BuiltValueNullFieldError('CalendarFilter', 'filters');
    }
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
        filters == other.filters;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc($jc($jc(0, id.hashCode), userDomain.hashCode), userId.hashCode),
        filters.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CalendarFilter')
          ..add('id', id)
          ..add('userDomain', userDomain)
          ..add('userId', userId)
          ..add('filters', filters))
        .toString();
  }
}

class CalendarFilterBuilder
    implements Builder<CalendarFilter, CalendarFilterBuilder> {
  _$CalendarFilter _$v;

  int _id;
  int get id => _$this._id;
  set id(int id) => _$this._id = id;

  String _userDomain;
  String get userDomain => _$this._userDomain;
  set userDomain(String userDomain) => _$this._userDomain = userDomain;

  String _userId;
  String get userId => _$this._userId;
  set userId(String userId) => _$this._userId = userId;

  SetBuilder<String> _filters;
  SetBuilder<String> get filters =>
      _$this._filters ??= new SetBuilder<String>();
  set filters(SetBuilder<String> filters) => _$this._filters = filters;

  CalendarFilterBuilder() {
    CalendarFilter._initializeBuilder(this);
  }

  CalendarFilterBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _userDomain = _$v.userDomain;
      _userId = _$v.userId;
      _filters = _$v.filters?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CalendarFilter other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CalendarFilter;
  }

  @override
  void update(void Function(CalendarFilterBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CalendarFilter build() {
    _$CalendarFilter _$result;
    try {
      _$result = _$v ??
          new _$CalendarFilter._(
              id: id,
              userDomain: userDomain,
              userId: userId,
              filters: filters.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'filters';
        filters.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'CalendarFilter', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
