---
name: release
description: Use when the user wants to create a new release. Handles changelog updates, version bumping, and git tagging.
---

# Release Skill

## Workflow

### 1. Ask version type
Ask the user if the release is `major`, `minor` or `patch`.

### 2. Infer new version
- Read the `CHANGELOG.md` file at the project root.
- Find the latest release matching the pattern `## Release v<MAJOR>.<MINOR>.<PATCH>`.
- Increment based on the user's choice:
  - `major`: +MAJOR, MINOR=0, PATCH=0
  - `minor`: +MINOR, PATCH=0
  - `patch`: +PATCH
- Show the inferred version to the user and ask for confirmation.

### 3. Validate Unreleased
- Read the `## Unreleased` section.
- If `Added`, `Changed` and `Fixed` are all empty → **stop** and warn the user that the changelog is likely missing content.

### 4. Update changelog
- Rename `## Unreleased` → `## Release v<NEW_VERSION>`.
- Insert a new empty `## Unreleased` block above the renamed release.

### 5. Git push and tag
```bash
git push
git tag -a v<VERSION> -m "Release v<VERSION>"
git push origin v<VERSION>
```
