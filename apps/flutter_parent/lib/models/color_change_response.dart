import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'color_change_response.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build --delete-conflicting-outputs
abstract class ColorChangeResponse implements Built<ColorChangeResponse, ColorChangeResponseBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<ColorChangeResponse> get serializer => _$colorChangeResponseSerializer;

  @BuiltValueField(wireName: 'hexcode')
  String? get hexCode;

  ColorChangeResponse._();
  factory ColorChangeResponse([void Function(ColorChangeResponseBuilder) updates]) = _$ColorChangeResponse;

  static void _initializeBuilder(ColorChangeResponseBuilder b) => b..hexCode = null;
}