# InstUI Token Generator

This directory contains scripts to generate Jetpack Compose design token files from the [instructure-ui](https://github.com/instructure/instructure-ui) design system.

## Overview

The generator downloads the primitive tokens JSON from the instructure-ui repository and uses [Style Dictionary](https://styledictionary.com/) to transform them into Kotlin files for Jetpack Compose.

## Generated Files

The following files are generated in `../src/main/java/com/instructure/instui/primitives/`:

- **InstUIColors.kt** - Color primitives (grey, blue, red, etc.)
- **InstUISizes.kt** - Size/spacing primitives in dp
- **InstUIFontWeights.kt** - Font weight values (100-900)
- **InstUIFontFamilies.kt** - Font family definitions
- **InstUIOpacities.kt** - Opacity values

**⚠️ DO NOT EDIT these files manually!** They are regenerated from the source tokens.

## Usage

### Via Gradle (Recommended)

```bash
./gradlew :libs:instui:generateInstUITokens
```

### Via npm directly

```bash
cd libs/instui/scripts
npm install
npm run build
```

## Updating InstUI Version

To update to a newer version of instructure-ui:

1. Edit `build-instui.js` and change `INSTUI_VERSION`
2. Run the generator
3. Verify the generated files
4. Commit the changes

## Font Files

The generator creates references to font resources that must exist in `../src/main/res/font/`.

Currently required fonts:
- **Lato** - lato_regular.ttf, lato_semibold.ttf, lato_bold.ttf
- **Inclusive Sans** - inclusive_sans_regular.ttf, inclusive_sans_semibold.ttf, inclusive_sans_bold.ttf
- **Atkinson Hyperlegible** - atkinson_hyperlegible_next_regular.ttf, etc.
- **Menlo** - Uses system monospace font, no file needed

If a font file is missing, the app will crash at runtime when that font is used.

## Dependencies

- Node.js 18+
- style-dictionary ^4.0.0
