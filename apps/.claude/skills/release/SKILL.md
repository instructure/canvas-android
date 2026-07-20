---
name: release
description: Start a Canvas Android release or hotfix — create the release/hotfix branch, bump the version (versionCode / versionName / updatePriority) in the app's build.gradle, and open the release PR. Use when the user mentions starting/cutting a release, a hotfix, a release candidate, or bumping an app version for release.
allowed-tools: Bash, Read, Edit, AskUserQuestion
---

# Start a Canvas Android Release or Hotfix

This skill automates the **manual start** of the Android release process: picking the mode,
creating the branch, bumping the version in `apps/{app}/build.gradle`, and opening the PR
against the protected `release/{app}` branch. Everything after the PR (Flank tests, RC build,
tag, deploy, Play Store rollout) is automated by GitHub Actions workflows or done later in the
Play Console — see [Android Release Process](https://instructure.atlassian.net/wiki/spaces/MOBILE/pages/1733329301/Android+Release+Process).

There are two modes:
- **Release** — a new version cut from `master`.
- **Hotfix** — a patch cut from the protected `release/{app}` branch, changing **only** the
  hotfix (patch) component of the version.

## Golden rules (read first)

1. **Ask before every decision that isn't already specified.** Use `AskUserQuestion` for mode,
   app, version, and update priority. Never guess these.
2. **Validate every input before acting.** If validation fails, stop and report exactly what's
   wrong — do not "fix it up" silently.
3. **Confirm the full plan** (mode, app, base branch, new branch name, version changes,
   updatePriority) with the user **before** creating the branch, editing files, or pushing.
4. **Push and PR are outward-facing** — only do them after the plan is confirmed.

## App reference

| App | Module / build.gradle | `build.gradle` version syntax | `updatePriority`? |
| --- | --- | --- | --- |
| Student | `apps/student/build.gradle` | `versionCode = N`, `versionName = 'x.y.z'` (equals, single quotes) | Yes — `def updatePriority = N` near the top |
| Teacher | `apps/teacher/build.gradle` | `versionCode = N`, `versionName = 'x.y.z'` (equals, single quotes) | Yes — `def updatePriority = N` near the top |
| Parent | `apps/parent/build.gradle` | `versionCode N`, `versionName "x.y.z"` (no equals, double quotes) | **No such field** — do not add one |

Always read the actual current lines before editing and **preserve each app's exact syntax**
(the `=`/no-`=` and quote style differ between parent and the others).

## Step 0 — Mode and app

- If the user didn't state the **mode**, ask (Release vs Hotfix).
- If the user didn't state the **app**, ask (Student / Teacher / Parent). Validate it is one of
  those three; reject anything else.

## Step 1 — Translations (Release mode only; skip for Hotfix)

The official process starts by importing translations to `master` before branching. Ask the
user: *"Are the latest translations already imported and merged to master?"*
- If **no**: offer to trigger the workflow with `gh workflow run import-translations.yml`, then
  **stop** and tell the user to review/merge the resulting PR to `master` before re-running this
  skill (the release branch must be cut from a `master` that already contains the translations).
- If **yes**: continue.

## Step 2 — Preconditions and base branch

Run from the repository root (`canvas-android/`).

1. Verify a clean working tree: `git status --porcelain`. If it's not empty, stop and ask the
   user to commit/stash first.
2. `git fetch origin`.
3. Determine the **base branch**:
   - **Release** → `master`.
   - **Hotfix** → the protected `release/{app}` branch. If the user named a specific release
     branch, use that; otherwise default to `release/{app}` and confirm. Validate the branch
     exists on origin (`git ls-remote --exit-code --heads origin <branch>`); if not, stop.
4. Check out and update the base: `git checkout <base>` then `git pull origin <base>`.

## Step 3 — Read the current version

- Read `versionCode`, `versionName`, and (Student/Teacher only) `updatePriority` from
  `apps/{app}/build.gradle`. Use the syntax from the App reference table.
- Show the current values to the user.

## Step 4 — Compute the new version (validate)

- **Release:**
  - `versionName`: ask the user. Propose a minor bump as the default (e.g. `8.9.0` → `8.10.0`).
    Validate it matches `^\d+\.\d+\.\d+$`.
  - `versionCode`: propose `current + 1`. Must be **strictly greater** than current.
- **Hotfix:**
  - `versionName`: bump **only the patch** component (e.g. `8.9.0` → `8.9.1`). Major and minor
    **must stay the same**. If the user supplies a version, validate that only the patch
    increased relative to the base; reject a major/minor change.
  - `versionCode`: `current + 1`.
- In both modes, reject the change if `newVersionCode <= currentVersionCode` or the versionName
  is malformed.

## Step 5 — Update priority (Student/Teacher only)

- For **Parent**, skip entirely (there is no field — do not add one).
- For **Student/Teacher**, ask for the value `0`–`5`, showing the tiers:
  - `0–1` — no update notification
  - `2–3` — flexible update notification
  - `4–5` — immediate update notification
  - Default: keep the current value if the user doesn't want to change it. Validate it is an
    integer in `0..5`.

## Step 6 — Confirm the plan

Present a single summary and get explicit confirmation (via `AskUserQuestion`) before any writes:

- Mode, app, base branch.
- New working branch: `release/{app}-{versionName}-{versionCode}`.
- Version changes: `versionCode` old → new, `versionName` old → new, `updatePriority` old → new
  (or "n/a" for Parent).

Validate the new branch does not already exist (locally or on origin); if it does, stop.

## Step 7 — Create the branch and edit build.gradle

1. From the base branch: `git checkout -b release/{app}-{versionName}-{versionCode}`.
2. Edit `apps/{app}/build.gradle`, changing only the version fields, preserving that app's exact
   syntax and indentation:
   - `versionCode` → new value
   - `versionName` → new value
   - (Student/Teacher, if changed) `updatePriority` → new value
3. Show the resulting diff (`git diff apps/{app}/build.gradle`) and confirm it changed only the
   intended lines.

## Step 8 — Commit, push, open the PR

1. Commit with a clear message:
   - Release: `[Release][{App}] {versionName} ({versionCode})`
   - Hotfix: `[Hotfix][{App}] {versionName} ({versionCode})`
2. Push the branch: `git push -u origin release/{app}-{versionName}-{versionCode}`.
3. Open the PR **against the protected `release/{app}` branch**:
   ```bash
   gh pr create --base release/{app} --head release/{app}-{versionName}-{versionCode} \
     --title "[Release][{App}] {versionName} ({versionCode})"
   ```
   (Use `[Hotfix]` in the title for hotfix mode.) In the body, note that opening the PR triggers
   the `Release {App}` pipeline.

## Step 9 — Tell the user what happens next (do not do these — just report)

- Opening the PR runs `Release {App}` → `reusable-release-pipeline.yml`: build test APKs,
  `testDevDebugUnitTest`, Flank tests (portrait/landscape/tablet/e2e), then **Build Release
  Candidate** if all pass (signed `prodRelease` APK+AAB, distributed via Firebase App
  Distribution, install link + QR posted to Slack `#github_android_release`).
- Before merging: **create the JIRA fix version** `Android {App} {versionName}` (it must exist so
  `Create Tag` can attach it), then **merge the PR — do NOT squash merge**.
- On merge, `create-tag.yml` tags `{app}-{versionName}-{versionCode}`, creates a GitHub draft
  release, updates JIRA `fixVersions`, and triggers `Deploy {App}` → uploads the signed AAB to the
  Play Store **internal track** via fastlane.
- Then: smoke test, Play Console staged rollout, release notes (`release-notes.yml`), monitor/
  hotfix for a week, back-merge the release branch into `master` (no squash), press **Release** on
  the JIRA fix version, and publish the GitHub draft release.

**Need an RC without the test gates?** Run the `Manual Release Candidate` workflow
(`gh workflow run manual-rc.yml`) and pick the branch to build.
