# flutter_parent

A new Flutter project.

## Getting Started

This project is a starting point for a Flutter application.

A few resources to get you started if this is your first Flutter project:

- [Lab: Write your first Flutter app](https://flutter.dev/docs/get-started/codelab)
- [Cookbook: Useful Flutter samples](https://flutter.dev/docs/cookbook)

For help getting started with Flutter, view our
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.

## Localization

Localization is not built in to flutter like it is with native Android.
Instead, we need to use the `intl` and `flutter_localizations` libraries.
This will allow us to have different translations being used depending
on the users locale.

It's pretty easy to use, once set up. To add a new string, create a new
getter in `lib/i10n/app_localizations.dart` that looks something like this:
```
  // The 'name' field must match the function name, and is optional unless arguments are part of the string resource

  String get myCoolNewString {
    return Intl.message('Cool string here', name: 'myCoolNewString', desc: 'A description to help translators');
  }
```

Then, to use the newly created string, access it like so using a BuildContext:
```
final alertTabLabel = AppLocalizations.of(context).myCoolNewString;
```

That is the basics of using the `intl` library for localized strings!

### Automating Imports
Generated files are created through a dev dependency of `intl_translation`.

The base strings are held in `lib/i10n/res/intl_messages.arb` and each
translation is held in `lib/i10n/res/intl_*.arb` where * is the language
identifier. e.g., `intl_en_unimelb_AU.arb` has the locale identifier
`en_unimelb_AU`.

Linking the translations back in will generate a
`lib/i10n/generated/messages_*.dart` for each `intl_*.arb` that is
provided. It will also generate a `messages_all.dart` to be able to
actually perform the lookups. The `AppLocalization#load` function hooks
into this by calling `initializeMessages` from the `messages_all.dart` file.

Command Line Usage:

Update the base intl_messages file for translators by running this
command from the project root:
```
flutter pub run intl_translation:extract_to_arb --output-dir=lib/l10n/res lib/l10n/app_localizations.dart
```

Once intl_messages has been created, it can be sent to translators where
they will create all the other `intl_*.arb` files. To link translations
back into the project once translators finish the translations, run this
command:
```
flutter pub run intl_translation:generate_from_arb --output-dir=lib/l10n/generated --no-use-deferred-loading lib/l10n/app_localizations.dart lib/l10n/res/intl_*.arb
```
