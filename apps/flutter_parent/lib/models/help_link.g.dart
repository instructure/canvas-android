// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'help_link.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

const AvailableTo _$user = const AvailableTo._('user');
const AvailableTo _$student = const AvailableTo._('student');
const AvailableTo _$teacher = const AvailableTo._('teacher');
const AvailableTo _$admin = const AvailableTo._('admin');
const AvailableTo _$observer = const AvailableTo._('observer');
const AvailableTo _$unenrolled = const AvailableTo._('unenrolled');

AvailableTo _$atValueOf(String name) {
  switch (name) {
    case 'user':
      return _$user;
    case 'student':
      return _$student;
    case 'teacher':
      return _$teacher;
    case 'admin':
      return _$admin;
    case 'observer':
      return _$observer;
    case 'unenrolled':
      return _$unenrolled;
    default:
      throw new ArgumentError(name);
  }
}

final BuiltSet<AvailableTo> _$atValues =
    new BuiltSet<AvailableTo>(const <AvailableTo>[
  _$user,
  _$student,
  _$teacher,
  _$admin,
  _$observer,
  _$unenrolled,
]);

Serializer<HelpLink> _$helpLinkSerializer = new _$HelpLinkSerializer();
Serializer<AvailableTo> _$availableToSerializer = new _$AvailableToSerializer();

class _$HelpLinkSerializer implements StructuredSerializer<HelpLink> {
  @override
  final Iterable<Type> types = const [HelpLink, _$HelpLink];
  @override
  final String wireName = 'HelpLink';

  @override
  Iterable<Object?> serialize(Serializers serializers, HelpLink object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'type',
      serializers.serialize(object.type, specifiedType: const FullType(String)),
      'available_to',
      serializers.serialize(object.availableTo,
          specifiedType:
              const FullType(BuiltList, const [const FullType(AvailableTo)])),
    ];
    Object? value;
    value = object.id;

    result
      ..add('id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.url;

    result
      ..add('url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.text;

    result
      ..add('text')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.subtext;

    result
      ..add('subtext')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  HelpLink deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new HelpLinkBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'type':
          result.type = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'available_to':
          result.availableTo.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(AvailableTo)]))!
              as BuiltList<Object?>);
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'text':
          result.text = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'subtext':
          result.subtext = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
      }
    }

    return result.build();
  }
}

class _$AvailableToSerializer implements PrimitiveSerializer<AvailableTo> {
  @override
  final Iterable<Type> types = const <Type>[AvailableTo];
  @override
  final String wireName = 'AvailableTo';

  @override
  Object serialize(Serializers serializers, AvailableTo object,
          {FullType specifiedType = FullType.unspecified}) =>
      object.name;

  @override
  AvailableTo deserialize(Serializers serializers, Object serialized,
          {FullType specifiedType = FullType.unspecified}) =>
      AvailableTo.valueOf(serialized as String);
}

class _$HelpLink extends HelpLink {
  @override
  final String? id;
  @override
  final String type;
  @override
  final BuiltList<AvailableTo> availableTo;
  @override
  final String? url;
  @override
  final String? text;
  @override
  final String? subtext;

  factory _$HelpLink([void Function(HelpLinkBuilder)? updates]) =>
      (new HelpLinkBuilder()..update(updates))._build();

  _$HelpLink._(
      {this.id,
      required this.type,
      required this.availableTo,
      this.url,
      this.text,
      this.subtext})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(type, r'HelpLink', 'type');
    BuiltValueNullFieldError.checkNotNull(
        availableTo, r'HelpLink', 'availableTo');
  }

  @override
  HelpLink rebuild(void Function(HelpLinkBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  HelpLinkBuilder toBuilder() => new HelpLinkBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is HelpLink &&
        id == other.id &&
        type == other.type &&
        availableTo == other.availableTo &&
        url == other.url &&
        text == other.text &&
        subtext == other.subtext;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, type.hashCode);
    _$hash = $jc(_$hash, availableTo.hashCode);
    _$hash = $jc(_$hash, url.hashCode);
    _$hash = $jc(_$hash, text.hashCode);
    _$hash = $jc(_$hash, subtext.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'HelpLink')
          ..add('id', id)
          ..add('type', type)
          ..add('availableTo', availableTo)
          ..add('url', url)
          ..add('text', text)
          ..add('subtext', subtext))
        .toString();
  }
}

class HelpLinkBuilder implements Builder<HelpLink, HelpLinkBuilder> {
  _$HelpLink? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _type;
  String? get type => _$this._type;
  set type(String? type) => _$this._type = type;

  ListBuilder<AvailableTo>? _availableTo;
  ListBuilder<AvailableTo> get availableTo =>
      _$this._availableTo ??= new ListBuilder<AvailableTo>();
  set availableTo(ListBuilder<AvailableTo>? availableTo) =>
      _$this._availableTo = availableTo;

  String? _url;
  String? get url => _$this._url;
  set url(String? url) => _$this._url = url;

  String? _text;
  String? get text => _$this._text;
  set text(String? text) => _$this._text = text;

  String? _subtext;
  String? get subtext => _$this._subtext;
  set subtext(String? subtext) => _$this._subtext = subtext;

  HelpLinkBuilder();

  HelpLinkBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _type = $v.type;
      _availableTo = $v.availableTo.toBuilder();
      _url = $v.url;
      _text = $v.text;
      _subtext = $v.subtext;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(HelpLink other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$HelpLink;
  }

  @override
  void update(void Function(HelpLinkBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  HelpLink build() => _build();

  _$HelpLink _build() {
    _$HelpLink _$result;
    try {
      _$result = _$v ??
          new _$HelpLink._(
              id: id,
              type: BuiltValueNullFieldError.checkNotNull(
                  type, r'HelpLink', 'type'),
              availableTo: availableTo.build(),
              url: url,
              text: text,
              subtext: subtext);
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'availableTo';
        availableTo.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'HelpLink', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
