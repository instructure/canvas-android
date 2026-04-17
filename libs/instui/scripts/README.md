# InstUI Token Generator

This directory contains scripts to generate Jetpack Compose design token files from the [instructure-ui](https://github.com/instructure/instructure-ui) design system.

## Overview

The generator downloads design tokens from the instructure-ui repository and uses [Style Dictionary](https://styledictionary.com/) to transform them into Kotlin files for Jetpack Compose. It produces three layers of tokens:

1. **Primitives** - Raw design values (colors, sizes, weights, fonts, opacities)
2. **Semantics** - Theme-aware tokens that reference primitives (light/dark colors, layout, typography)
3. **Components** - Per-component tokens that reference the semantic layer (auto-discovered, 65 files)

## Generated Files

**⚠️ DO NOT EDIT these files manually!** They are regenerated from the source tokens.

### Primitives (`../src/main/java/com/instructure/instui/token/primitives/`)

- **InstUIColors.kt** - Color primitives (grey, blue, red, etc.)
- **InstUISizes.kt** - Size/spacing primitives in dp
- **InstUIFontWeights.kt** - Font weight values (100-900)
- **InstUIFontFamilies.kt** - Font family definitions
- **InstUIOpacities.kt** - Opacity values

### Semantics (`../src/main/java/com/instructure/instui/token/semantic/`)

- **InstUISemanticColors.kt** - Theme-aware colors with Light/Dark + @Composable accessors
- **InstUIElevation.kt** - Shadow values (Level1-4)
- **InstUILayoutSizes.kt** - Border radius, spacing, interactive sizes, breakpoints
- **InstUILayoutTypography.kt** - Font families, weights, sizes, line heights
- **InstUILayoutConfig.kt** - Opacity config, visibility flags

### Components (`../src/main/java/com/instructure/instui/token/component/`)

One file per component, auto-discovered from the instructure-ui repo. Examples:
- **InstUIText.kt** - Text styles (composed TextStyle objects), font sizes, colors
- **InstUIHeading.kt** - Heading levels, composed title TextStyles
- **InstUIBaseButton.kt** - Button variant colors and sizing
- **InstUIPill.kt** - Status pill styling
- **InstUIList.kt** - List item spacing and typography
- ... (65 files total)

Component tokens reference the semantic layer:
- Color tokens use `@Composable get()` for automatic theme switching
- Font sizes are in `sp` (not dp) for accessibility correctness
- Typography composites generate `TextStyle` objects with `sp`/`em` units
- BoxShadow and Border composites are documented as comments (not directly usable in Compose)

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
