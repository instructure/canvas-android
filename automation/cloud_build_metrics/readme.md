## cloud build metrics

Goal:
- Send build health metrics to BigQuery.
- Export metrics from BigQuery into Sheets

# Git Large File Storage

See the [Git LFS website](https://git-lfs.github.com/) for details.

- `brew install git-lfs`
- `git lfs install`

## To Do

- [x] API client for Bitrise
- [x] API client Buddybuild
- [x] Generate build activity reports locally
- [x] BigQuery integration
  - [x] Document and normalize status_text (buddybuid & bitrise)
  - [x] Create tables per app
  - [x] Insert build results via CI job
  - [x] Duplicate filtering (row id) & last update timestamp
- [x] Google sheets integration
  - [x] Duplicate removal
  - [x] Export buddybuild build health report
  - [x] Export bitrise build health report
  - [x] Run report generation in CI job

## Environment variables

| ENV name      | Description
| ------------- | ----
| BITRISE_TOKEN | API token for Bitrise
| BUDDYBUILD_TOKEN | API token for Buddybuild
| BITRISE_ORG | Org name on Bitrise

## Schema

| Name            | Type
| --------------- | ----
| build_id        | STRING
| build_date      | DATE
| build_yearweek  | STRING
| build_queued    | INTEGER
| build_duration  | INTEGER
| build_status    | STRING

## Creating a new sheet

How to create a new spreadsheet & authorize:


- Create spreadsheet as medwards@instructure.com
  - Set as "Anyone at Instructure who has the link can comment"
  - Grant edit permissions to mobileqainstruct@gmail.com
- Log into the gmail account mobileqainstruct@gmail.com
  - Run project to trigger authorization request
  - Authorize with mobileqainstruct@gmail.com
