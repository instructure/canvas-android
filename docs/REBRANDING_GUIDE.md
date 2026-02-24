# Canvas Android — Rebranding & White-Label Customization Guide
> Complete guide to replacing all Instructure/Canvas branding with your own · 2026-02-24

---

## Table of Contents
1. [Overview](#1-overview)
2. [Pre-Rebranding Checklist](#2-pre-rebranding-checklist)
3. [Step 1 — Application IDs & Package Names](#3-step-1--application-ids--package-names)
4. [Step 2 — App Names & String Resources](#4-step-2--app-names--string-resources)
5. [Step 3 — App Icons & Splash Screen](#5-step-3--app-icons--splash-screen)
6. [Step 4 — Color System (InstUI Tokens)](#6-step-4--color-system-instui-tokens)
7. [Step 5 — Typography](#7-step-5--typography)
8. [Step 6 — Material / App Theme](#8-step-6--material--app-theme)
9. [Step 7 — Compose Theme](#9-step-7--compose-theme)
10. [Step 8 — Login Screen Branding](#10-step-8--login-screen-branding)
11. [Step 9 — About / Legal Screens](#11-step-9--about--legal-screens)
12. [Step 10 — Firebase Project](#12-step-10--firebase-project)
13. [Step 11 — Pendo Analytics Token](#13-step-11--pendo-analytics-token)
14. [Step 12 — PDF / Nutrient License](#14-step-12--pdf--nutrient-license)
15. [Step 13 — Play Store Listing](#15-step-13--play-store-listing)
16. [Step 14 — Build Signing](#16-step-14--build-signing)
17. [Step 15 — Keystore & CI Secrets](#17-step-15--keystore--ci-secrets)
18. [Step 16 — Remove Instructure Copyright Headers](#18-step-16--remove-instructure-copyright-headers)
19. [Step 17 — Horizon Module (optional)](#19-step-17--horizon-module-optional)
20. [Quick-Reference Change Matrix](#20-quick-reference-change-matrix)

---

## 1. Overview

The canvas-android repo is designed to connect to **any** Canvas LMS instance (the server URL is user-supplied at login). Rebranding involves:
- Replacing visual identity (name, icon, colors)
- Replacing third-party API keys (Firebase, Pendo, Nutrient/PSPDFKit)
- Changing application IDs so the Play Store treats it as a distinct app
- Optionally restricting to a single institution (removing the school-picker)

**Three apps share a common library stack.** Most branding changes live in:
- `libs/pandares/` — shared resources (colors, themes, drawables)
- `apps/*/src/main/res/` — per-app resources
- `apps/*/build.gradle` — application IDs, versions
- `apps/buildSrc/GlobalDependencies.kt` — SDK/tool versions

---

## 2. Pre-Rebranding Checklist

```
[ ] Brand name decided
[ ] Logo files ready (SVG source, then exported as PNG/XML vector)
[ ] Brand color palette finalized (primary, secondary, background, text)
[ ] Firebase project created for your brand
[ ] Google Play Developer account ready
[ ] Keystore generated for release signing
[ ] Nutrient/PSPDFKit license obtained (or replaced with alternative PDF lib)
[ ] Pendo account created (or analytics removed)
```

---

## 3. Step 1 — Application IDs & Package Names

### Files to edit

**`apps/student/build.gradle`**
```groovy
android {
    namespace 'com.YOURBRAND.student'        // ← change
    defaultConfig {
        applicationId "com.YOURBRAND.student" // ← change (Play Store unique ID)
        versionCode = 1
        versionName = '1.0.0'
    }
}
```

**`apps/teacher/build.gradle`**
```groovy
namespace 'com.YOURBRAND.teacher'
applicationId "com.YOURBRAND.teacher"
```

**`apps/parent/build.gradle`**
```groovy
namespace 'com.YOURBRAND.parent'
applicationId "com.YOURBRAND.parent"
```

### Package rename (source files)
Use Android Studio's **Refactor → Rename** on the root package in each app's `src/main/java/`:
```
com.instructure.student  →  com.YOURBRAND.student
com.instructure.teacher  →  com.YOURBRAND.teacher
com.instructure.parentapp → com.YOURBRAND.parent
```

> ⚠️ The shared library packages (`com.instructure.canvasapi2`, `com.instructure.pandautils`, etc.) can stay as-is unless you also want to rename libraries. Changing application-module packages is sufficient for Play Store publishing.

---

## 4. Step 2 — App Names & String Resources

### Per-app `strings.xml`
**`apps/student/src/main/res/values/strings.xml`**
```xml
<string name="app_name">YourBrand Student</string>
<string name="app_name_short">YourBrand</string>
```

**`apps/teacher/src/main/res/values/strings.xml`**
```xml
<string name="app_name">YourBrand Instructor</string>
```

**`apps/parent/src/main/res/values/strings.xml`**
```xml
<string name="app_name">YourBrand Parent</string>
```

### Shared strings in `pandautils`
Search for and replace any hardcoded "Canvas" references:
```bash
grep -r "Canvas" libs/pandautils/src/main/res/values/ --include="*.xml"
grep -r "Instructure" libs/pandautils/src/main/res/values/ --include="*.xml"
```
Update mentions of "Canvas LMS", "Canvas Student", "Canvas by Instructure", etc.

### AndroidManifest labels
Each app's `AndroidManifest.xml` uses `@string/app_name` — updating the string resource is sufficient.

---

## 5. Step 3 — App Icons & Splash Screen

### Launcher icons
Each app has icons in multiple density folders:
```
apps/student/src/main/res/
├── mipmap-mdpi/ic_launcher.png          (48×48)
├── mipmap-hdpi/ic_launcher.png          (72×72)
├── mipmap-xhdpi/ic_launcher.png         (96×96)
├── mipmap-xxhdpi/ic_launcher.png        (144×144)
├── mipmap-xxxhdpi/ic_launcher.png       (192×192)
└── mipmap-anydpi-v26/
    ├── ic_launcher.xml                   (adaptive icon — foreground layer)
    └── ic_launcher_round.xml
```

**Recommended workflow:**
1. Create your icon in Android Studio: **File → New → Image Asset**
2. Choose "Launcher Icons (Adaptive and Legacy)"
3. Provide your logo as the foreground layer
4. Set background color to your brand primary

### Shared drawable logo
The login screen uses a logo drawable from `pandares`:
```
libs/pandares/src/main/res/drawable/ic_canvas_logo_white.xml   ← login screen logo
libs/pandares/src/main/res/drawable/ic_panda_*.xml             ← panda mascot images
```
Replace `ic_canvas_logo_white.xml` with your brand logo as an Android Vector Drawable.

To remove the Panda mascot:
- Replace or delete `ic_panda_*.xml` drawables
- Find usages: `grep -r "ic_panda" apps/ libs/ --include="*.xml" --include="*.kt"`
- Substitute with your own illustration or remove the references

### Splash screen
Configured in `res/values/themes.xml` as a `windowSplashScreenBackground` + `windowSplashScreenAnimatedIcon`. Update those theme attributes with your brand colors/icon.

---

## 6. Step 4 — Color System (InstUI Tokens)

### Primary file
**`libs/pandares/src/main/res/values/colors.xml`** — light mode
**`libs/pandares/src/main/res/values-night/colors.xml`** — dark mode

### App accent colors (most important)
```xml
<!-- Light mode — apps/*/src/main/res/values/colors.xml or pandares -->
<color name="login_studentAppTheme">#E66000</color>   <!-- ← your brand primary -->
<color name="login_teacherAppTheme">#A855A1</color>   <!-- ← teacher accent -->
<color name="login_parentAppTheme">#1770AB</color>    <!-- ← parent accent -->

<!-- Dark mode overrides -->
<color name="login_studentAppTheme">#FF6B72</color>
<color name="login_teacherAppTheme">#CE7BE7</color>
<color name="login_parentAppTheme">#8094FF</color>
```

### Full semantic token replacement
Replace the semantic tokens to match your design system:

| Token | Purpose | Default (light) |
|-------|---------|----------------|
| `backgroundLightest` | Page background | `#FFFFFF` |
| `backgroundLight` | Card / surface background | `#F5F5F5` |
| `backgroundMedium` | Dividers, disabled states | `#C7CDD1` |
| `backgroundDark` | Strong dividers | `#8B969E` |
| `textDarkest` | Primary text | `#2D3B45` |
| `textDark` | Secondary text | `#394B58` |
| `textPlaceholder` | Placeholder text | `#8B969E` |
| `textInfo` | Links, info | `#0770A3` |
| `textSuccess` | Success states | `#127A1B` |
| `textWarning` | Warning states | `#C23C0D` |
| `textDanger` | Error states | `#E72929` |
| `borderMedium` | Input borders | `#C7CDD1` |

### Course colors
12 preset course colors (`courseColor1`–`courseColor12`) are used to distinguish courses. Replace these with your brand's extended palette.

---

## 7. Step 5 — Typography

### Current font
The project uses the system default font (Roboto) unless overridden.

### To add a custom font
1. Add font files to `libs/pandares/src/main/res/font/`
2. Create a font XML: `res/font/yourbrand_sans.xml`
3. Update `libs/pandares/src/main/res/values/themes.xml`:
```xml
<style name="Base.Theme.Canvas" parent="Theme.Material3.DayNight.NoActionBar">
    <item name="fontFamily">@font/yourbrand_sans</item>
    <item name="android:fontFamily">@font/yourbrand_sans</item>
</style>
```
4. Update Compose theme in `CanvasTheme.kt`:
```kotlin
val YourBrandTypography = Typography(
    bodyLarge = TextStyle(fontFamily = FontFamily(Font(R.font.yourbrand_sans_regular))),
    // ... other text styles
)
```

---

## 8. Step 6 — Material / App Theme

### Main theme file
**`libs/pandares/src/main/res/values/themes.xml`**

Key theme attributes to update:
```xml
<style name="Base.Theme.Canvas" parent="Theme.Material3.DayNight.NoActionBar">
    <item name="colorPrimary">@color/YOUR_PRIMARY</item>
    <item name="colorOnPrimary">@color/YOUR_ON_PRIMARY</item>
    <item name="colorSecondary">@color/YOUR_SECONDARY</item>
    <item name="colorSurface">@color/backgroundLightest</item>
    <item name="colorOnSurface">@color/textDarkest</item>
    <item name="colorError">@color/textDanger</item>
    <!-- splash screen -->
    <item name="android:windowSplashScreenBackground">@color/YOUR_PRIMARY</item>
    <item name="android:windowSplashScreenAnimatedIcon">@drawable/ic_your_logo</item>
</style>
```

---

## 9. Step 7 — Compose Theme

**`libs/pandautils/src/main/java/com/instructure/pandautils/compose/CanvasTheme.kt`**

```kotlin
@Composable
fun CanvasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) YourBrandDarkColorScheme else YourBrandLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = YourBrandTypography,
        shapes = YourBrandShapes,
        content = content
    )
}

// Define your color schemes:
private val YourBrandLightColorScheme = lightColorScheme(
    primary = Color(0xFF_YOUR_PRIMARY_HEX),
    onPrimary = Color.White,
    secondary = Color(0xFF_YOUR_SECONDARY),
    // ...
)
```

---

## 10. Step 8 — Login Screen Branding

The login screens are in `libs/login-api-2/`. Key areas:

### School domain picker
`FindSchoolActivity` / `FindSchoolFragment` — if you want to lock to a single institution:
```kotlin
// In LoginLandingPageActivity or equivalent
// Skip the domain picker and hardcode your domain:
val domain = "yourinstitution.instructure.com"
ApiPrefs.domain = domain
// Proceed directly to OAuth
```

### Login logo
Replace `ic_canvas_logo_white` drawable (referenced in login XML layouts) with your logo.

### Login background color
Set in `login-api-2/src/main/res/values/colors.xml` or override per-app:
```xml
<color name="loginBackground">#YOUR_LOGIN_BG_COLOR</color>
```

### Login helper text
Update "Find your school" strings to match your product language.

---

## 11. Step 9 — About / Legal Screens

### About screen
`apps/student/src/main/java/.../features/about/` — update:
- App name, version display
- Copyright string → `© 2024 YourCompany, Inc.`
- Privacy policy URL
- Terms of service URL

### Legal / Open Source notices
`apps/student/src/main/java/.../features/legal/` — retain open-source licenses as required by GPL v3 and Apache 2.0, but update company attribution sections.

### Shared about strings in `pandautils/res/values/strings.xml`
```xml
<string name="about_company">YourCompany, Inc.</string>
<string name="privacyUrl">https://yourcompany.com/privacy</string>
<string name="termsUrl">https://yourcompany.com/terms</string>
```

---

## 12. Step 10 — Firebase Project

### Create a new Firebase project
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create project: "YourBrand-Android"
3. Add Android apps for each application ID:
   - `com.YOURBRAND.student`
   - `com.YOURBRAND.teacher`
   - `com.YOURBRAND.parent`
4. Download each `google-services.json`
5. Place in the corresponding app directory:
   - `apps/student/google-services.json`
   - `apps/teacher/google-services.json`
   - `apps/parent/google-services.json`

### Enable services
In Firebase Console for each app:
- ✅ Crashlytics
- ✅ Cloud Messaging (FCM)
- ✅ Remote Config (optional)

> The existing `apply plugin: 'com.google.firebase.crashlytics'` and `apply plugin: 'com.google.gms.google-services'` in each `build.gradle` will automatically pick up the new `google-services.json`.

---

## 13. Step 11 — Pendo Analytics Token

Pendo is configured via build config field in each app's `build.gradle`:
```groovy
buildConfigField "String", "PENDO_TOKEN", "\"$pendoAccessToken\""
```

`$pendoAccessToken` is read from `private-data/` at build time via `MergePrivateData`.

**To update:**
1. Create your Pendo account at [pendo.io](https://pendo.io)
2. Get your App Key from Pendo console
3. Update `private-data/student/gradle.properties`:
```properties
pendoAccessToken=YOUR_PENDO_APP_KEY
```

**To remove Pendo entirely:**
1. Remove `implementation Libs.PENDO` from each `build.gradle`
2. Remove `buildConfigField "String", "PENDO_TOKEN"` lines
3. Search and remove all `Pendo.startSession(...)` calls:
   ```bash
   grep -r "Pendo\|pendo" apps/ --include="*.kt" -l
   ```

---

## 14. Step 12 — PDF / Nutrient License

Nutrient (PSPDFKit) requires a per-app license key:
```groovy
buildConfigField "String", "PSPDFKIT_LICENSE_KEY", "\"$pspdfkitLicenseKey\""
```

**To update:** Set `pspdfkitLicenseKey` in `private-data/student/gradle.properties`.

**To replace with a free alternative (if you don't need PDF annotations):**
- Remove `implementation Libs.NUTRIENT` from `build.gradle`
- Replace `CandroidPSPDFActivity` with a WebView-based PDF viewer or `AndroidPdfViewer` library
- Remove `PSPDFKIT_LICENSE_KEY` build config fields

---

## 15. Step 13 — Play Store Listing

After changing application IDs, you'll publish new apps. Prepare:

| Asset | Specification |
|-------|--------------|
| App icon | 512×512 PNG |
| Feature graphic | 1024×500 PNG |
| Screenshots (phone) | Minimum 2, max 8 per locale |
| Short description | ≤ 80 characters |
| Full description | ≤ 4000 characters |
| Privacy Policy URL | Required |
| Content rating | Complete questionnaire |

---

## 16. Step 14 — Build Signing

### Generate a new keystore
```bash
keytool -genkey -v \
  -keystore yourbrand.keystore \
  -alias yourbrand-key \
  -keyalg RSA -keysize 2048 \
  -validity 10000
```

### Update `build.gradle` signing config
**`apps/student/build.gradle`** (and teacher, parent):
```groovy
signingConfigs {
    release {
        storeFile     file('../yourbrand.keystore')
        storePassword System.getenv('KEYSTORE_PASSWORD')
        keyAlias      System.getenv('KEY_ALIAS')
        keyPassword   System.getenv('KEY_PASSWORD')
    }
}
```

> ⚠️ Never commit the `.keystore` file or passwords to git. Add `*.keystore` to `.gitignore`.

---

## 17. Step 15 — Keystore & CI Secrets

Set environment variables in your CI system (Bitrise/GitHub Actions/etc.):
```
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=yourbrand-key
KEY_PASSWORD=your_key_password
```

For the private data pattern, update `private-data/` directory:
```
private-data/
├── student/
│   └── gradle.properties        # pendoAccessToken, pspdfkitLicenseKey, OAuth credentials
├── teacher/
│   └── gradle.properties
└── parent/
    └── gradle.properties
```

---

## 18. Step 16 — Remove Instructure Copyright Headers

Each source file has a GPL/Apache copyright header mentioning Instructure. Update these in bulk:

```bash
# Preview files to change
grep -r "Copyright.*Instructure" apps/ libs/ --include="*.kt" -l | wc -l

# Replace (adjust sed for your OS)
find apps/ libs/ -name "*.kt" -exec sed -i \
  's/Copyright (C) .* - present Instructure, Inc./Copyright (C) 2024 - present YourCompany, Inc./g' {} \;
```

> **Legal note:** GPL v3 requires preserving the license notice. You must keep the GPL v3 license text but can change the copyright holder to yourself (you are a derivative work).

---

## 19. Step 17 — Horizon Module (optional)

The student app depends on a `:horizon` module:
```groovy
implementation project(path: ':horizon')
```
This is a private Instructure module not included in the open-source release. You have two options:

**Option A — Create a stub module:**
```kotlin
// Create apps/horizon/src/main/java/com/instructure/horizon/HorizonModule.kt
// as an empty Hilt module that provides no-op implementations
```

**Option B — Remove the dependency:**
Find all usages of Horizon APIs and remove or replace them. Search:
```bash
grep -r "horizon\|Horizon" apps/student/src/ --include="*.kt" -l
```

---

## 20. Quick-Reference Change Matrix

| What to change | File(s) | Notes |
|---------------|---------|-------|
| App name | `apps/*/res/values/strings.xml` | `app_name` string |
| Application ID | `apps/*/build.gradle` | `applicationId` |
| Package name | Refactor in Android Studio | `namespace` + source dirs |
| Primary color | `libs/pandares/res/values/colors.xml` | `login_*AppTheme` colors |
| All UI colors | `libs/pandares/res/values/colors.xml` + `-night/` | InstUI token system |
| Compose theme | `libs/pandautils/.../compose/CanvasTheme.kt` | `MaterialTheme` setup |
| App icon | `apps/*/res/mipmap-*/ic_launcher*` | All densities + adaptive |
| Login logo | `libs/pandares/res/drawable/ic_canvas_logo_white.xml` | Replace vector |
| Splash screen | `libs/pandares/res/values/themes.xml` | `windowSplashScreen*` attrs |
| Firebase config | `apps/*/google-services.json` | One per app per Firebase project |
| Pendo token | `private-data/*/gradle.properties` | `pendoAccessToken` |
| PDF license | `private-data/*/gradle.properties` | `pspdfkitLicenseKey` |
| Signing keystore | `apps/*.keystore` + `build.gradle` | New keystore per brand |
| Copyright headers | All `.kt` files | Bulk `sed` replace |
| Privacy/Terms URLs | `strings.xml` in pandautils/apps | `privacyUrl`, `termsUrl` |
| About screen content | `apps/*/features/about/` | Company name, version |
| Open-source notices | `apps/*/features/legal/` | Keep OSS licenses |
| Translations | `translations/` + per-app `values-*/strings.xml` | Update Crowdin project |

---

*This guide covers all visual and identity touch-points. For server-side Canvas theming (brand variables API), configure your Canvas LMS admin panel.*
