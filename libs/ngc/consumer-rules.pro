# NGC module consumer ProGuard rules

# PdfAnnotationHostFragment (MBL-19937) is instantiated reflectively by class name via
# androidx.fragment.compose.AndroidFragment's default FragmentFactory — keep it and its no-arg
# constructor so R8 doesn't strip it from a minified consuming app.
-keep class com.instructure.ngc.features.submission.annotation.PdfAnnotationHostFragment {
    <init>();
}
