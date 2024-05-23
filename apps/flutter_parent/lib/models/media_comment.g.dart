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
  Iterable<Object?> serialize(Serializers serializers, MediaComment object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'media_type',
      serializers.serialize(object.mediaType,
          specifiedType: const FullType(MediaType)),
    ];
    Object? value;
    value = object.mediaId;

    result
      ..add('media_id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.displayName;

    result
      ..add('display_name')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.url;

    result
      ..add('url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.contentType;

    result
      ..add('content-type')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  MediaComment deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new MediaCommentBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'media_id':
          result.mediaId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'display_name':
          result.displayName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'media_type':
          result.mediaType = serializers.deserialize(value,
              specifiedType: const FullType(MediaType))! as MediaType;
          break;
        case 'content-type':
          result.contentType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
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
  final String? mediaId;
  @override
  final String? displayName;
  @override
  final String? url;
  @override
  final MediaType mediaType;
  @override
  final String? contentType;

  factory _$MediaComment([void Function(MediaCommentBuilder)? updates]) =>
      (new MediaCommentBuilder()..update(updates))._build();

  _$MediaComment._(
      {this.mediaId,
      this.displayName,
      this.url,
      required this.mediaType,
      this.contentType})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        mediaType, r'MediaComment', 'mediaType');
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
        mediaId == other.mediaId &&
        displayName == other.displayName &&
        url == other.url &&
        mediaType == other.mediaType &&
        contentType == other.contentType;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, mediaId.hashCode);
    _$hash = $jc(_$hash, displayName.hashCode);
    _$hash = $jc(_$hash, url.hashCode);
    _$hash = $jc(_$hash, mediaType.hashCode);
    _$hash = $jc(_$hash, contentType.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'MediaComment')
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
  _$MediaComment? _$v;

  String? _mediaId;
  String? get mediaId => _$this._mediaId;
  set mediaId(String? mediaId) => _$this._mediaId = mediaId;

  String? _displayName;
  String? get displayName => _$this._displayName;
  set displayName(String? displayName) => _$this._displayName = displayName;

  String? _url;
  String? get url => _$this._url;
  set url(String? url) => _$this._url = url;

  MediaType? _mediaType;
  MediaType? get mediaType => _$this._mediaType;
  set mediaType(MediaType? mediaType) => _$this._mediaType = mediaType;

  String? _contentType;
  String? get contentType => _$this._contentType;
  set contentType(String? contentType) => _$this._contentType = contentType;

  MediaCommentBuilder();

  MediaCommentBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _mediaId = $v.mediaId;
      _displayName = $v.displayName;
      _url = $v.url;
      _mediaType = $v.mediaType;
      _contentType = $v.contentType;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(MediaComment other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$MediaComment;
  }

  @override
  void update(void Function(MediaCommentBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  MediaComment build() => _build();

  _$MediaComment _build() {
    final _$result = _$v ??
        new _$MediaComment._(
            mediaId: mediaId,
            displayName: displayName,
            url: url,
            mediaType: BuiltValueNullFieldError.checkNotNull(
                mediaType, r'MediaComment', 'mediaType'),
            contentType: contentType);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
