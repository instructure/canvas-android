# Pull Request Skill

Create pull requests for Canvas Android following project conventions.

## Instructions

When creating a pull request, use the template located at `/PULL_REQUEST_TEMPLATE` in the repository root.

### PR Template Requirements

The template includes the following sections:

1. **Test plan description** - Describe how to test the changes
2. **Issue references** - Use `refs:` followed by issue numbers
3. **Impact scope** - Use `affects:` to specify which apps are affected:
   - Student
   - Teacher
   - Parent
   - Can be multiple if changes affect multiple apps
4. **Release note** - User-facing description of changes
5. **Checklist**:
   - Dark/light mode testing
   - Landscape/tablet testing
   - Accessibility testing
   - Product approval

### Important Notes

- **DO NOT** include E2E tests or screenshots sections unless specifically needed
- Use `gh pr create` to create PRs from the command line
- The template will be automatically loaded when using the GitHub CLI

### Creating a PR

```bash
# Create PR using GitHub CLI (automatically uses template)
gh pr create

# Or with title and body
gh pr create --title "Your PR Title" --body "$(cat /path/to/description.md)"
```

### Example affects Field

```markdown
affects: Student, Teacher
```

or

```markdown
affects: Student
```

## Context

When the user asks to "create a PR" or "open a pull request", guide them to use the `gh pr create` command. Remind them to:
1. Include the `affects:` field to specify which apps are impacted
2. Reference the related issue(s) with `refs:`
3. Complete the checklist items before marking the PR as ready for review
4. Not include E2E tests or screenshots sections unless specifically requested