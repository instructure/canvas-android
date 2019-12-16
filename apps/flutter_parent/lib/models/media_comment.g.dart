// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_comment.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

const MediaType _$mediaTypeAudio = const MediaType._('audio');
const MediaType _$mediaTypeVideo = const MediaType._('video');
const MediaType _$mediaTypeUnknown = const MediaType._('unknown');

MediaType _$mediaTypeValueOf(String name) {
  switch (name) {
    case 'audio':
      return _$mediaTypeAudio;
    case 'video':
      return _$mediaTypeVideo;
    case 'unknown':
      return _$mediaTypeUnknown;
    default:
      return _$mediaTypeUnknown;
  }
}

final BuiltSet<MediaType> _$mediaTypeValues =
    new BuiltSet<MediaType>(const <MediaType>[
  _$mediaTypeAudio,
  _$mediaTypeVideo,
  _$mediaTypeUnknown,
]);

Serializer<MediaComment> _$mediaCommentSerializer =
    new _$MediaCommentSerializer();
Serializer<MediaType> _$mediaTypeSerializer = new _$MediaTypeSerializer();

class _$MediaCommentSerializer implements StructuredSerializer<MediaComment> {
  @override
  final Iterable<Type> types = const [MediaComment, _$MediaComment];
  @override
  final String wireName = 'MediaComment';

  @override
  Iterable<Object> serialize(Serializers serializers, MediaComment object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'media_type',
      serializers.serialize(object.mediaType,
          specifiedType: const FullType(MediaType)),
    ];
    result.add('media_id');
    if (object.mediaId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.mediaId,
          specifiedType: const FullType(String)));
    }
    result.add('display_name');
    if (object.displayName == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.displayName,
          specifiedType: const FullType(String)));
    }
    result.add('url');
    if (object.url == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.url,
          specifiedType: const FullType(String)));
    }
    result.add('content-type');
    if (object.contentType == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.contentType,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  MediaComment deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new MediaCommentBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'media_id':
          result.mediaId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'display_name':
          result.displayName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'media_type':
          result.mediaType = serializers.deserialize(value,
              specifiedType: const FullType(MediaType)) as MediaType;
          break;
        case 'content-type':
          result.contentType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$MediaTypeSerializer implements PrimitiveSerializer<MediaType> {
  @override
  final Iterable<Type> types = const <Type>[MediaType];
  @override
  final String wireName = 'media_type';

  @override
  Object serialize(Serializers serializers, MediaType object,
          {FullType specifiedType = FullType.unspecified}) =>
      object.name;

  @override
  MediaType deserialize(Serializers serializers, Object serialized,
          {FullType specifiedType = FullType.unspecified}) =>
      MediaType.valueOf(serialized as String);
}

class _$MediaComment extends MediaComment {
  @override
  final String id;
  @override
  final String mediaId;
  @override
  final String displayName;
  @override
  final String url;
  @override
  final MediaType mediaType;
  @override
  final String contentType;

  factory _$MediaComment([void Function(MediaCommentBuilder) updates]) =>
      (new MediaCommentBuilder()..update(updates)).build();

  _$MediaComment._(
      {this.id,
      this.mediaId,
      this.displayName,
      this.url,
      this.mediaType,
      this.contentType})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('MediaComment', 'id');
    }
    if (mediaType == null) {
      throw new BuiltValueNullFieldError('MediaComment', 'mediaType');
    }
  }

  @override
  MediaComment rebuild(void Function(MediaCommentBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  MediaCommentBuilder toBuilder() => new MediaCommentBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is MediaComment &&
        id == other.id &&
        mediaId == other.mediaId &&
        displayName == other.displayName &&
        url == other.url &&
        mediaType == other.mediaType &&
        contentType == other.contentType;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc(
                $jc($jc($jc(0, id.hashCode), mediaId.hashCode),
                    displayName.hashCode),
                url.hashCode),
            mediaType.hashCode),
        contentType.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('MediaComment')
          ..add('id', id)
          ..add('mediaId', mediaId)
          ..add('displayName', displayName)
          ..add('url', url)
          ..add('mediaType', mediaType)
          ..add('contentType', contentType))
        .toString();
  }
}

class MediaCommentBuilder
    implements Builder<MediaComment, MediaCommentBuilder> {
  _$MediaComment _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _mediaId;
  String get mediaId => _$this._mediaId;
  set mediaId(String mediaId) => _$this._mediaId = mediaId;

  String _displayName;
  String get displayName => _$this._displayName;
  set displayName(String displayName) => _$this._displayName = displayName;

  String _url;
  String get url => _$this._url;
  set url(String url) => _$this._url = url;

  MediaType _mediaType;
  MediaType get mediaType => _$this._mediaType;
  set mediaType(MediaType mediaType) => _$this._mediaType = mediaType;

  String _contentType;
  String get contentType => _$this._contentType;
  set contentType(String contentType) => _$this._contentType = contentType;

  MediaCommentBuilder();

  MediaCommentBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _mediaId = _$v.mediaId;
      _displayName = _$v.displayName;
      _url = _$v.url;
      _mediaType = _$v.mediaType;
      _contentType = _$v.contentType;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(MediaComment other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$MediaComment;
  }

  @override
  void update(void Function(MediaCommentBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$MediaComment build() {
    final _$result = _$v ??
        new _$MediaComment._(
            id: id,
            mediaId: mediaId,
            displayName: displayName,
            url: url,
            mediaType: mediaType,
            contentType: contentType);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
