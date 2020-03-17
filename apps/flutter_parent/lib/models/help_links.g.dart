// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'help_links.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<HelpLinks> _$helpLinksSerializer = new _$HelpLinksSerializer();

class _$HelpLinksSerializer implements StructuredSerializer<HelpLinks> {
  @override
  final Iterable<Type> types = const [HelpLinks, _$HelpLinks];
  @override
  final String wireName = 'HelpLinks';

  @override
  Iterable<Object> serialize(Serializers serializers, HelpLinks object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'custom_help_links',
      serializers.serialize(object.customHelpLinks,
          specifiedType:
              const FullType(BuiltList, const [const FullType(HelpLink)])),
      'default_help_links',
      serializers.serialize(object.defaultHelpLinks,
          specifiedType:
              const FullType(BuiltList, const [const FullType(HelpLink)])),
    ];

    return result;
  }

  @override
  HelpLinks deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new HelpLinksBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'custom_help_links':
          result.customHelpLinks.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(HelpLink)]))
              as BuiltList<Object>);
          break;
        case 'default_help_links':
          result.defaultHelpLinks.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(HelpLink)]))
              as BuiltList<Object>);
          break;
      }
    }

    return result.build();
  }
}

class _$HelpLinks extends HelpLinks {
  @override
  final BuiltList<HelpLink> customHelpLinks;
  @override
  final BuiltList<HelpLink> defaultHelpLinks;

  factory _$HelpLinks([void Function(HelpLinksBuilder) updates]) =>
      (new HelpLinksBuilder()..update(updates)).build();

  _$HelpLinks._({this.customHelpLinks, this.defaultHelpLinks}) : super._() {
    if (customHelpLinks == null) {
      throw new BuiltValueNullFieldError('HelpLinks', 'customHelpLinks');
    }
    if (defaultHelpLinks == null) {
      throw new BuiltValueNullFieldError('HelpLinks', 'defaultHelpLinks');
    }
  }

  @override
  HelpLinks rebuild(void Function(HelpLinksBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  HelpLinksBuilder toBuilder() => new HelpLinksBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is HelpLinks &&
        customHelpLinks == other.customHelpLinks &&
        defaultHelpLinks == other.defaultHelpLinks;
  }

  @override
  int get hashCode {
    return $jf(
        $jc($jc(0, customHelpLinks.hashCode), defaultHelpLinks.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('HelpLinks')
          ..add('customHelpLinks', customHelpLinks)
          ..add('defaultHelpLinks', defaultHelpLinks))
        .toString();
  }
}

class HelpLinksBuilder implements Builder<HelpLinks, HelpLinksBuilder> {
  _$HelpLinks _$v;

  ListBuilder<HelpLink> _customHelpLinks;
  ListBuilder<HelpLink> get customHelpLinks =>
      _$this._customHelpLinks ??= new ListBuilder<HelpLink>();
  set customHelpLinks(ListBuilder<HelpLink> customHelpLinks) =>
      _$this._customHelpLinks = customHelpLinks;

  ListBuilder<HelpLink> _defaultHelpLinks;
  ListBuilder<HelpLink> get defaultHelpLinks =>
      _$this._defaultHelpLinks ??= new ListBuilder<HelpLink>();
  set defaultHelpLinks(ListBuilder<HelpLink> defaultHelpLinks) =>
      _$this._defaultHelpLinks = defaultHelpLinks;

  HelpLinksBuilder();

  HelpLinksBuilder get _$this {
    if (_$v != null) {
      _customHelpLinks = _$v.customHelpLinks?.toBuilder();
      _defaultHelpLinks = _$v.defaultHelpLinks?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(HelpLinks other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$HelpLinks;
  }

  @override
  void update(void Function(HelpLinksBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$HelpLinks build() {
    _$HelpLinks _$result;
    try {
      _$result = _$v ??
          new _$HelpLinks._(
              customHelpLinks: customHelpLinks.build(),
              defaultHelpLinks: defaultHelpLinks.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'customHelpLinks';
        customHelpLinks.build();
        _$failedField = 'defaultHelpLinks';
        defaultHelpLinks.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'HelpLinks', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
