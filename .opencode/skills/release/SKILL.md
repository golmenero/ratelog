---
name: release
description: Use when the user wants to create a new release. Handles changelog updates, version bumping, and git tagging.
---

# Release Skill

## Workflow

### 0. Integration checking
Execute 'mvn test' and ensure all tests pass successfully. If there are test failures, **stop** and warn the user.

### 1. Ask version type
Ask the user if the release is `major`, `minor`, `patch` or `beta`.

### 2. Infer new version
- Read the `CHANGELOG.md` file at the project root.
- Find the latest release matching the pattern `## Release v<MAJOR>.<MINOR>.<PATCH>` or `## Release v<MAJOR>.<MINOR>.<PATCH>-beta.<N>`.
- Increment based on the user's choice:
  - `major`: +MAJOR, MINOR=0, PATCH=0
  - `minor`: +MINOR, PATCH=0
  - `patch`: +PATCH
  - For stable releases (`major`/`minor`/`patch`), if the latest release is a beta, strip the `-beta.<N>` suffix before incrementing.
  - `beta`:
    - If the latest release is `v<MAJOR>.<MINOR>.<PATCH>-beta.<N>` → `v<MAJOR>.<MINOR>.<PATCH>-beta.<N+1>`
    - If the latest release is stable → ask if the beta targets the next `major` or `minor`, and use `v<NEW_BASE>-beta.1`
- Show the inferred version to the user and ask for confirmation.

### 3. Validate Unreleased
- Read the `## Unreleased` section.
- If `Added`, `Changed` and `Fixed` are all empty → **stop** and warn the user that the changelog is likely missing content.

### 4. Update changelog
- Rename `## Unreleased` → `## Release v<NEW_VERSION>`.
- Insert a new empty `## Unreleased` block above the renamed release.

### 5. Commit changes
- Commit changelog changed with the commit message "release v<NEW_VERSION>-changelog"

### 6. Git push and tag
```bash
git push
git tag -a v<VERSION> -m "Release v<VERSION>"
git push origin v<VERSION>
```
