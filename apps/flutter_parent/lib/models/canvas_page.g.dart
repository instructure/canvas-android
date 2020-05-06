// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'canvas_page.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CanvasPage> _$canvasPageSerializer = new _$CanvasPageSerializer();

class _$CanvasPageSerializer implements StructuredSerializer<CanvasPage> {
  @override
  final Iterable<Type> types = const [CanvasPage, _$CanvasPage];
  @override
  final String wireName = 'CanvasPage';

  @override
  Iterable<Object> serialize(Serializers serializers, CanvasPage object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'page_id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'hide_from_students',
      serializers.serialize(object.hideFromStudents,
          specifiedType: const FullType(bool)),
      'front_page',
      serializers.serialize(object.frontPage,
          specifiedType: const FullType(bool)),
      'lock_info',
      serializers.serialize(object.lockInfo,
          specifiedType: const FullType(LockInfo)),
      'published',
      serializers.serialize(object.published,
          specifiedType: const FullType(bool)),
    ];
    result.add('url');
    if (object.url == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.url,
          specifiedType: const FullType(String)));
    }
    result.add('title');
    if (object.title == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.title,
          specifiedType: const FullType(String)));
    }
    result.add('created_at');
    if (object.createdAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.createdAt,
          specifiedType: const FullType(DateTime)));
    }
    result.add('updated_at');
    if (object.updatedAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.updatedAt,
          specifiedType: const FullType(DateTime)));
    }
    result.add('status');
    if (object.status == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.status,
          specifiedType: const FullType(String)));
    }
    result.add('body');
    if (object.body == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.body,
          specifiedType: const FullType(String)));
    }
    result.add('editing_roles');
    if (object.editingRoles == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.editingRoles,
          specifiedType: const FullType(String)));
    }
    result.add('lock_explanation');
    if (object.lockExplanation == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.lockExplanation,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  CanvasPage deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CanvasPageBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'page_id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'title':
          result.title = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'created_at':
          result.createdAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'updated_at':
          result.updatedAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'hide_from_students':
          result.hideFromStudents = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'status':
          result.status = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'body':
          result.body = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'front_page':
          result.frontPage = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'lock_info':
          result.lockInfo.replace(serializers.deserialize(value,
              specifiedType: const FullType(LockInfo)) as LockInfo);
          break;
        case 'published':
          result.published = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'editing_roles':
          result.editingRoles = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'lock_explanation':
          result.lockExplanation = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$CanvasPage extends CanvasPage {
  @override
  final String id;
  @override
  final String url;
  @override
  final String title;
  @override
  final DateTime createdAt;
  @override
  final DateTime updatedAt;
  @override
  final bool hideFromStudents;
  @override
  final String status;
  @override
  final String body;
  @override
  final bool frontPage;
  @override
  final LockInfo lockInfo;
  @override
  final bool published;
  @override
  final String editingRoles;
  @override
  final String lockExplanation;

  factory _$CanvasPage([void Function(CanvasPageBuilder) updates]) =>
      (new CanvasPageBuilder()..update(updates)).build();

  _$CanvasPage._(
      {this.id,
      this.url,
      this.title,
      this.createdAt,
      this.updatedAt,
      this.hideFromStudents,
      this.status,
      this.body,
      this.frontPage,
      this.lockInfo,
      this.published,
      this.editingRoles,
      this.lockExplanation})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('CanvasPage', 'id');
    }
    if (hideFromStudents == null) {
      throw new BuiltValueNullFieldError('CanvasPage', 'hideFromStudents');
    }
    if (frontPage == null) {
      throw new BuiltValueNullFieldError('CanvasPage', 'frontPage');
    }
    if (lockInfo == null) {
      throw new BuiltValueNullFieldError('CanvasPage', 'lockInfo');
    }
    if (published == null) {
      throw new BuiltValueNullFieldError('CanvasPage', 'published');
    }
  }

  @override
  CanvasPage rebuild(void Function(CanvasPageBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CanvasPageBuilder toBuilder() => new CanvasPageBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CanvasPage &&
        id == other.id &&
        url == other.url &&
        title == other.title &&
        createdAt == other.createdAt &&
        updatedAt == other.updatedAt &&
        hideFromStudents == other.hideFromStudents &&
        status == other.status &&
        body == other.body &&
        frontPage == other.frontPage &&
        lockInfo == other.lockInfo &&
        published == other.published &&
        editingRoles == other.editingRoles &&
        lockExplanation == other.lockExplanation;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc(
                        $jc(
                            $jc(
                                $jc(
                                    $jc(
                                        $jc(
                                            $jc(
                                                $jc($jc(0, id.hashCode),
                                                    url.hashCode),
                                                title.hashCode),
                                            createdAt.hashCode),
                                        updatedAt.hashCode),
                                    hideFromStudents.hashCode),
                                status.hashCode),
                            body.hashCode),
                        frontPage.hashCode),
                    lockInfo.hashCode),
                published.hashCode),
            editingRoles.hashCode),
        lockExplanation.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CanvasPage')
          ..add('id', id)
          ..add('url', url)
          ..add('title', title)
          ..add('createdAt', createdAt)
          ..add('updatedAt', updatedAt)
          ..add('hideFromStudents', hideFromStudents)
          ..add('status', status)
          ..add('body', body)
          ..add('frontPage', frontPage)
          ..add('lockInfo', lockInfo)
          ..add('published', published)
          ..add('editingRoles', editingRoles)
          ..add('lockExplanation', lockExplanation))
        .toString();
  }
}

class CanvasPageBuilder implements Builder<CanvasPage, CanvasPageBuilder> {
  _$CanvasPage _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _url;
  String get url => _$this._url;
  set url(String url) => _$this._url = url;

  String _title;
  String get title => _$this._title;
  set title(String title) => _$this._title = title;

  DateTime _createdAt;
  DateTime get createdAt => _$this._createdAt;
  set createdAt(DateTime createdAt) => _$this._createdAt = createdAt;

  DateTime _updatedAt;
  DateTime get updatedAt => _$this._updatedAt;
  set updatedAt(DateTime updatedAt) => _$this._updatedAt = updatedAt;

  bool _hideFromStudents;
  bool get hideFromStudents => _$this._hideFromStudents;
  set hideFromStudents(bool hideFromStudents) =>
      _$this._hideFromStudents = hideFromStudents;

  String _status;
  String get status => _$this._status;
  set status(String status) => _$this._status = status;

  String _body;
  String get body => _$this._body;
  set body(String body) => _$this._body = body;

  bool _frontPage;
  bool get frontPage => _$this._frontPage;
  set frontPage(bool frontPage) => _$this._frontPage = frontPage;

  LockInfoBuilder _lockInfo;
  LockInfoBuilder get lockInfo => _$this._lockInfo ??= new LockInfoBuilder();
  set lockInfo(LockInfoBuilder lockInfo) => _$this._lockInfo = lockInfo;

  bool _published;
  bool get published => _$this._published;
  set published(bool published) => _$this._published = published;

  String _editingRoles;
  String get editingRoles => _$this._editingRoles;
  set editingRoles(String editingRoles) => _$this._editingRoles = editingRoles;

  String _lockExplanation;
  String get lockExplanation => _$this._lockExplanation;
  set lockExplanation(String lockExplanation) =>
      _$this._lockExplanation = lockExplanation;

  CanvasPageBuilder() {
    CanvasPage._initializeBuilder(this);
  }

  CanvasPageBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _url = _$v.url;
      _title = _$v.title;
      _createdAt = _$v.createdAt;
      _updatedAt = _$v.updatedAt;
      _hideFromStudents = _$v.hideFromStudents;
      _status = _$v.status;
      _body = _$v.body;
      _frontPage = _$v.frontPage;
      _lockInfo = _$v.lockInfo?.toBuilder();
      _published = _$v.published;
      _editingRoles = _$v.editingRoles;
      _lockExplanation = _$v.lockExplanation;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CanvasPage other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CanvasPage;
  }

  @override
  void update(void Function(CanvasPageBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CanvasPage build() {
    _$CanvasPage _$result;
    try {
      _$result = _$v ??
          new _$CanvasPage._(
              id: id,
              url: url,
              title: title,
              createdAt: createdAt,
              updatedAt: updatedAt,
              hideFromStudents: hideFromStudents,
              status: status,
              body: body,
              frontPage: frontPage,
              lockInfo: lockInfo.build(),
              published: published,
              editingRoles: editingRoles,
              lockExplanation: lockExplanation);
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'lockInfo';
        lockInfo.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'CanvasPage', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
