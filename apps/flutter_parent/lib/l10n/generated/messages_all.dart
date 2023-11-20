// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that looks up messages for specific locales by
// delegating to the appropriate library.

// Ignore issues from commonly used lints in this file.
// ignore_for_file:implementation_imports, file_names, unnecessary_new
// ignore_for_file:unnecessary_brace_in_string_interps, directives_ordering
// ignore_for_file:argument_type_not_assignable, invalid_assignment
// ignore_for_file:prefer_single_quotes, prefer_generic_function_type_aliases
// ignore_for_file:comment_references

import 'dart:async';

import 'package:intl/intl.dart';
import 'package:intl/message_lookup_by_library.dart';
import 'package:intl/src/intl_helpers.dart';

import 'messages_ar.dart' as messages_ar;
import 'messages_ca.dart' as messages_ca;
import 'messages_cy.dart' as messages_cy;
import 'messages_da.dart' as messages_da;
import 'messages_da_instk12.dart' as messages_da_instk12;
import 'messages_de.dart' as messages_de;
import 'messages_en.dart' as messages_en;
import 'messages_en_AU.dart' as messages_en_au;
import 'messages_en_AU_unimelb.dart' as messages_en_au_unimelb;
import 'messages_en_CA.dart' as messages_en_ca;
import 'messages_en_CY.dart' as messages_en_cy;
import 'messages_en_GB.dart' as messages_en_gb;
import 'messages_en_GB_instukhe.dart' as messages_en_gb_instukhe;
import 'messages_es.dart' as messages_es;
import 'messages_fi.dart' as messages_fi;
import 'messages_fr.dart' as messages_fr;
import 'messages_fr_CA.dart' as messages_fr_ca;
import 'messages_ht.dart' as messages_ht;
import 'messages_is.dart' as messages_is;
import 'messages_it.dart' as messages_it;
import 'messages_ja.dart' as messages_ja;
import 'messages_messages.dart' as messages_messages;
import 'messages_mi.dart' as messages_mi;
import 'messages_nb.dart' as messages_nb;
import 'messages_nb_instk12.dart' as messages_nb_instk12;
import 'messages_nl.dart' as messages_nl;
import 'messages_pl.dart' as messages_pl;
import 'messages_pt_BR.dart' as messages_pt_br;
import 'messages_pt_PT.dart' as messages_pt_pt;
import 'messages_ru.dart' as messages_ru;
import 'messages_sl.dart' as messages_sl;
import 'messages_sv.dart' as messages_sv;
import 'messages_sv_instk12.dart' as messages_sv_instk12;
import 'messages_zh.dart' as messages_zh;
import 'messages_zh_HK.dart' as messages_zh_hk;

typedef Future<dynamic>? LibraryLoader();
Map<String, LibraryLoader> _deferredLibraries = {
  'ar': () => new Future.value(null),
  'ca': () => new Future.value(null),
  'cy': () => new Future.value(null),
  'da': () => new Future.value(null),
  'da_instk12': () => new Future.value(null),
  'de': () => new Future.value(null),
  'en': () => new Future.value(null),
  'en_AU': () => new Future.value(null),
  'en_AU_unimelb': () => new Future.value(null),
  'en_CA': () => new Future.value(null),
  'en_CY': () => new Future.value(null),
  'en_GB': () => new Future.value(null),
  'en_GB_instukhe': () => new Future.value(null),
  'es': () => new Future.value(null),
  'fi': () => new Future.value(null),
  'fr': () => new Future.value(null),
  'fr_CA': () => new Future.value(null),
  'ht': () => new Future.value(null),
  'is': () => new Future.value(null),
  'it': () => new Future.value(null),
  'ja': () => new Future.value(null),
  'messages': () => new Future.value(null),
  'mi': () => new Future.value(null),
  'nb': () => new Future.value(null),
  'nb_instk12': () => new Future.value(null),
  'nl': () => new Future.value(null),
  'pl': () => new Future.value(null),
  'pt_BR': () => new Future.value(null),
  'pt_PT': () => new Future.value(null),
  'ru': () => new Future.value(null),
  'sl': () => new Future.value(null),
  'sv': () => new Future.value(null),
  'sv_instk12': () => new Future.value(null),
  'zh': () => new Future.value(null),
  'zh_HK': () => new Future.value(null),
};

MessageLookupByLibrary? _findExact(String localeName) {
  switch (localeName) {
    case 'ar':
      return messages_ar.messages;
    case 'ca':
      return messages_ca.messages;
    case 'cy':
      return messages_cy.messages;
    case 'da':
      return messages_da.messages;
    case 'da_instk12':
      return messages_da_instk12.messages;
    case 'de':
      return messages_de.messages;
    case 'en':
      return messages_en.messages;
    case 'en_AU':
      return messages_en_au.messages;
    case 'en_AU_unimelb':
      return messages_en_au_unimelb.messages;
    case 'en_CA':
      return messages_en_ca.messages;
    case 'en_CY':
      return messages_en_cy.messages;
    case 'en_GB':
      return messages_en_gb.messages;
    case 'en_GB_instukhe':
      return messages_en_gb_instukhe.messages;
    case 'es':
      return messages_es.messages;
    case 'fi':
      return messages_fi.messages;
    case 'fr':
      return messages_fr.messages;
    case 'fr_CA':
      return messages_fr_ca.messages;
    case 'ht':
      return messages_ht.messages;
    case 'is':
      return messages_is.messages;
    case 'it':
      return messages_it.messages;
    case 'ja':
      return messages_ja.messages;
    case 'messages':
      return messages_messages.messages;
    case 'mi':
      return messages_mi.messages;
    case 'nb':
      return messages_nb.messages;
    case 'nb_instk12':
      return messages_nb_instk12.messages;
    case 'nl':
      return messages_nl.messages;
    case 'pl':
      return messages_pl.messages;
    case 'pt_BR':
      return messages_pt_br.messages;
    case 'pt_PT':
      return messages_pt_pt.messages;
    case 'ru':
      return messages_ru.messages;
    case 'sl':
      return messages_sl.messages;
    case 'sv':
      return messages_sv.messages;
    case 'sv_instk12':
      return messages_sv_instk12.messages;
    case 'zh':
      return messages_zh.messages;
    case 'zh_HK':
      return messages_zh_hk.messages;
    default:
      return null;
  }
}

/// User programs should call this before using [localeName] for messages.
Future<bool> initializeMessages(String localeName) async {
  var availableLocale = Intl.verifiedLocale(
    localeName,
    (locale) => _deferredLibraries[locale] != null,
    onFailure: (_) => null);
  if (availableLocale == null) {
    return new Future.value(false);
  }
  var lib = _deferredLibraries[availableLocale];
  await (lib == null ? new Future.value(false) : lib());
  initializeInternalMessageLookup(() => new CompositeMessageLookup());
  messageLookup.addLocale(availableLocale, _findGeneratedMessagesFor);
  return new Future.value(true);
}

bool _messagesExistFor(String locale) {
  try {
    return _findExact(locale) != null;
  } catch (e) {
    return false;
  }
}

MessageLookupByLibrary? _findGeneratedMessagesFor(String locale) {
  var actualLocale = Intl.verifiedLocale(locale, _messagesExistFor,
      onFailure: (_) => null);
  if (actualLocale == null) return null;
  return _findExact(actualLocale);
}
