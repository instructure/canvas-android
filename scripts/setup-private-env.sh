#!/usr/bin/env bash
# Local-dev template for the internal canvas-android build.
#
# WHAT THIS IS:
#   GitHub Secrets is the source of truth. CI workflows wire those secrets into
#   the env vars below automatically. For local builds with real values:
#
#     1. Copy this file:   cp scripts/setup-private-env.sh scripts/setup-private-env.local.sh
#     2. Get a filled-in copy of setup-private-env.local.sh from a teammate via
#        1Password / Slack DM, OR paste the values yourself from the canvas-android
#        repo's Actions secrets (admin-only).
#     3. Source it before building:   source scripts/setup-private-env.local.sh
#     4. Build:   ./gradlew assembleStudentDebug
#
#   setup-private-env.local.sh is gitignored. Never commit filled-in values.
#
# WITHOUT THIS:
#   `./gradlew` fails. There is no open_source_data fallback — every value below
#   must be set. See docs/private-data-migration-plan.md for the full picture.
#
# The values below are dummy placeholders so you can see the variable layout.
# Fill in real values in your gitignored setup-private-env.local.sh copy.

# --- PSPDFKit (Maven creds shared across modules; license differs per app) ---
export PSPDF_MAVEN_USER=""
export PSPDF_MAVEN_PASS=""
export PSPDFKIT_LICENSE_KEY_STUDENT=""
export PSPDFKIT_LICENSE_KEY_TEACHER=""

# --- Crashlytics ---
export CRASHLYTICS_API_KEY="ffffffffffffffffffffffffffffffffffffffff"

# --- Login (login-api module) ---
export LOGIN_ANONYMOUS_SCHOOL_DOMAIN="2"

# --- Parent app ---
export PARENT_LOGIN_CLIENT_ID="1"
export PARENT_LOGIN_CLIENT_SECRET="1"
export PENDO_ACCESS_TOKEN_PARENT="1"
export ROBO_TEST_API_KEY="1"
export ROBO_TEST_USER_ID="1"
export ROBO_TEST_USERNAME="1"

# --- Student app ---
export PENDO_ACCESS_TOKEN_STUDENT="1"
export PRONOUN_TEST_STUDENT="1"
export PRONOUN_TEST_STUDENT_PASSWORD="2"
export PUSH_NOTIF_TEST_STUDENT="3"
export PUSH_NOTIF_TEST_STUDENT_PASSWORD="4"

# --- Teacher app ---
export PENDO_ACCESS_TOKEN_TEACHER="1"
export ROBO_TEST_USER_TOKEN="1"
export ROBO_TEST_DOMAIN="1"
export PRONOUN_TEST_TEACHER="1"
export PRONOUN_TEST_TEACHER_PASSWORD="2"
export PUSH_NOTIF_TEST_TEACHER="3"
export PUSH_NOTIF_TEST_TEACHER_PASSWORD="4"

# --- Base64-encoded file payloads ---
# Every entry is REQUIRED. Build fails if any of these is empty.
# Generate from a local file:   base64 -i path/to/file
export GOOGLE_SERVICES_PARENT_B64=""
export GOOGLE_SERVICES_STUDENT_B64=""
export GOOGLE_SERVICES_TEACHER_B64=""
export ANALYTICS_XML_STUDENT_B64=""
export PRIVATE_STRINGS_XML_STUDENT_B64=""
export ROBO_USER_JSON_TEACHER_B64=""
export PRIVATE_CONST_KT_B64=""
