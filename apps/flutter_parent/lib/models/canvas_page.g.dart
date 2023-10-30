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
  Iterable<Object?> serialize(Serializers serializers, CanvasPage object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
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
    Object? value;
    value = object.url;

    result
      ..add('url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.title;

    result
      ..add('title')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.createdAt;

    result
      ..add('created_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.updatedAt;

    result
      ..add('updated_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.status;

    result
      ..add('status')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.body;

    result
      ..add('body')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.editingRoles;

    result
      ..add('editing_roles')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.lockExplanation;

    result
      ..add('lock_explanation')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  CanvasPage deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CanvasPageBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'page_id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'title':
          result.title = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'created_at':
          result.createdAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'updated_at':
          result.updatedAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'hide_from_students':
          result.hideFromStudents = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'status':
          result.status = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'body':
          result.body = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'front_page':
          result.frontPage = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'lock_info':
          result.lockInfo.replace(serializers.deserialize(value,
              specifiedType: const FullType(LockInfo))! as LockInfo);
          break;
        case 'published':
          result.published = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'editing_roles':
          result.editingRoles = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'lock_explanation':
          result.lockExplanation = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
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
  final String? url;
  @override
  final String? title;
  @override
  final DateTime? createdAt;
  @override
  final DateTime? updatedAt;
  @override
  final bool hideFromStudents;
  @override
  final String? status;
  @override
  final String? body;
  @override
  final bool frontPage;
  @override
  final LockInfo lockInfo;
  @override
  final bool published;
  @override
  final String? editingRoles;
  @override
  final String? lockExplanation;

  factory _$CanvasPage([void Function(CanvasPageBuilder)? updates]) =>
      (new CanvasPageBuilder()..update(updates))._build();

  _$CanvasPage._(
      {required this.id,
      this.url,
      this.title,
      this.createdAt,
      this.updatedAt,
      required this.hideFromStudents,
      this.status,
      this.body,
      required this.frontPage,
      required this.lockInfo,
      required this.published,
      this.editingRoles,
      this.lockExplanation})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'CanvasPage', 'id');
    BuiltValueNullFieldError.checkNotNull(
        hideFromStudents, r'CanvasPage', 'hideFromStudents');
    BuiltValueNullFieldError.checkNotNull(
        frontPage, r'CanvasPage', 'frontPage');
    BuiltValueNullFieldError.checkNotNull(lockInfo, r'CanvasPage', 'lockInfo');
    BuiltValueNullFieldError.checkNotNull(
        published, r'CanvasPage', 'published');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, url.hashCode);
    _$hash = $jc(_$hash, title.hashCode);
    _$hash = $jc(_$hash, createdAt.hashCode);
    _$hash = $jc(_$hash, updatedAt.hashCode);
    _$hash = $jc(_$hash, hideFromStudents.hashCode);
    _$hash = $jc(_$hash, status.hashCode);
    _$hash = $jc(_$hash, body.hashCode);
    _$hash = $jc(_$hash, frontPage.hashCode);
    _$hash = $jc(_$hash, lockInfo.hashCode);
    _$hash = $jc(_$hash, published.hashCode);
    _$hash = $jc(_$hash, editingRoles.hashCode);
    _$hash = $jc(_$hash, lockExplanation.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CanvasPage')
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
  _$CanvasPage? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _url;
  String? get url => _$this._url;
  set url(String? url) => _$this._url = url;

  String? _title;
  String? get title => _$this._title;
  set title(String? title) => _$this._title = title;

  DateTime? _createdAt;
  DateTime? get createdAt => _$this._createdAt;
  set createdAt(DateTime? createdAt) => _$this._createdAt = createdAt;

  DateTime? _updatedAt;
  DateTime? get updatedAt => _$this._updatedAt;
  set updatedAt(DateTime? updatedAt) => _$this._updatedAt = updatedAt;

  bool? _hideFromStudents;
  bool? get hideFromStudents => _$this._hideFromStudents;
  set hideFromStudents(bool? hideFromStudents) =>
      _$this._hideFromStudents = hideFromStudents;

  String? _status;
  String? get status => _$this._status;
  set status(String? status) => _$this._status = status;

  String? _body;
  String? get body => _$this._body;
  set body(String? body) => _$this._body = body;

  bool? _frontPage;
  bool? get frontPage => _$this._frontPage;
  set frontPage(bool? frontPage) => _$this._frontPage = frontPage;

  LockInfoBuilder? _lockInfo;
  LockInfoBuilder get lockInfo => _$this._lockInfo ??= new LockInfoBuilder();
  set lockInfo(LockInfoBuilder? lockInfo) => _$this._lockInfo = lockInfo;

  bool? _published;
  bool? get published => _$this._published;
  set published(bool? published) => _$this._published = published;

  String? _editingRoles;
  String? get editingRoles => _$this._editingRoles;
  set editingRoles(String? editingRoles) => _$this._editingRoles = editingRoles;

  String? _lockExplanation;
  String? get lockExplanation => _$this._lockExplanation;
  set lockExplanation(String? lockExplanation) =>
      _$this._lockExplanation = lockExplanation;

  CanvasPageBuilder() {
    CanvasPage._initializeBuilder(this);
  }

  CanvasPageBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _url = $v.url;
      _title = $v.title;
      _createdAt = $v.createdAt;
      _updatedAt = $v.updatedAt;
      _hideFromStudents = $v.hideFromStudents;
      _status = $v.status;
      _body = $v.body;
      _frontPage = $v.frontPage;
      _lockInfo = $v.lockInfo.toBuilder();
      _published = $v.published;
      _editingRoles = $v.editingRoles;
      _lockExplanation = $v.lockExplanation;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CanvasPage other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CanvasPage;
  }

  @override
  void update(void Function(CanvasPageBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CanvasPage build() => _build();

  _$CanvasPage _build() {
    _$CanvasPage _$result;
    try {
      _$result = _$v ??
          new _$CanvasPage._(
              id: BuiltValueNullFieldError.checkNotNull(
                  id, r'CanvasPage', 'id'),
              url: url,
              title: title,
              createdAt: createdAt,
              updatedAt: updatedAt,
              hideFromStudents: BuiltValueNullFieldError.checkNotNull(
                  hideFromStudents, r'CanvasPage', 'hideFromStudents'),
              status: status,
              body: body,
              frontPage: BuiltValueNullFieldError.checkNotNull(
                  frontPage, r'CanvasPage', 'frontPage'),
              lockInfo: lockInfo.build(),
              published: BuiltValueNullFieldError.checkNotNull(
                  published, r'CanvasPage', 'published'),
              editingRoles: editingRoles,
              lockExplanation: lockExplanation);
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'lockInfo';
        lockInfo.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'CanvasPage', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
