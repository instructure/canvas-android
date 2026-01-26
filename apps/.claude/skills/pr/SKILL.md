---
name: pr
description: Create pull requests for Canvas Android following project conventions. Use when user mentions creating PR, pull request, opening PR, or submitting changes for review. Includes PR template requirements and affects field guidelines.
allowed-tools: Bash, Read
---

# Create Pull Requests for Canvas Android

Create pull requests for Canvas Android following project conventions and using the standard template.

## PR Template Location

The PR template is located at `/PULL_REQUEST_TEMPLATE` in the repository root.

## Creating a PR

Use the GitHub CLI to create pull requests:

```bash
# Create PR (automatically uses template)
gh pr create

# Or with title and body
gh pr create --title "Your PR Title" --body "$(cat /path/to/description.md)"
```

The template will be automatically loaded when using `gh pr create`.

### PR Title Format

**CRITICAL**: The PR title MUST include the affected app(s) in square brackets AFTER the ticket ID.

Format: `[TICKET-ID][AppName] Description`

Examples:
- `[MBL-19497][Student] Fix bookmark URL placeholders from notifications`
- `[MBL-12345][Teacher] Add assignment grading improvements`
- `[MBL-67890][Student][Teacher] Fix discussion loading for both apps`
- `[MBL-11111][All] Update login flow across all apps`

**Rules:**
- The ticket ID comes FIRST, then the app name(s)
- Always include the app name(s) based on the `affects:` field in the PR body
- If `affects: Student`, use `[Student]`
- If `affects: Student, Teacher`, use `[Student][Teacher]`
- If `affects: Student, Teacher, Parent`, use `[All]`
- The app tags come AFTER the ticket ID and BEFORE the description

## Template Requirements

The PR template includes the following sections that must be completed:

### 1. Test Plan Description

Describe how to test the changes. Include:
- Steps to reproduce/verify the fix or feature
- Expected behavior
- Any prerequisites or setup needed

### 2. Issue References

Use `refs:` followed by issue numbers:

```markdown
refs: MBL-12345
```

or for multiple issues:

```markdown
refs: MBL-12345, MBL-67890
```

### 3. Impact Scope (affects field)

**IMPORTANT**: Use `affects:` to specify which apps are impacted by the changes.

Valid values:
- `Student`
- `Teacher`
- `Parent`

Examples:

```markdown
affects: Student
```

```markdown
affects: Student, Teacher
```

```markdown
affects: Student, Teacher, Parent
```

### 4. Release Note

Provide a user-facing description of changes. This should be:
- Written for end users, not developers
- Clear and concise
- Focused on user impact

### 5. Checklist

Complete the following items before marking PR as ready:

- [ ] Follow-up e2e test ticket created or not needed
- [ ] Run E2E test suite
- [ ] Tested in dark mode
- [ ] Tested in light mode
- [ ] Test in landscape mode and/or tablet
- [ ] A11y checked
- [ ] Approve from product

## Important Notes

- **DO NOT** include this checkbox item "- [ ] Run E2E test suite" or screenshots sections unless specifically needed
- Always include the `affects:` field to specify which apps are impacted
- Reference the related issue(s) with `refs:`
- Complete the checklist before marking the PR as ready for review

## Examples

### Example PR for Student App Only

**Title:** `[MBL-19453][Student] Add dashboard widget customization`

```markdown
Test plan:
1. Navigate to Dashboard
2. Verify widgets are displayed correctly
3. Test widget reordering

refs: MBL-19453
affects: Student
release note: Students can now customize their dashboard with widgets
```

### Example PR for Multiple Apps

**Title:** `[MBL-12345][Student][Teacher] Improve discussion loading performance`

```markdown
Test plan:
1. Open any course
2. Verify discussion loading
3. Test comment threading

refs: MBL-12345
affects: Student, Teacher
release note: Improved discussion loading performance
```

### Example PR for All Apps

**Title:** `[MBL-67890][All] Update login flow`

```markdown
Test plan:
1. Launch any app
2. Log in with credentials
3. Verify successful authentication

refs: MBL-67890
affects: Student, Teacher, Parent
release note: Updated login experience for improved security
```