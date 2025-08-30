# Cross-Platform CI/CD Setup Guide

This project supports both **GitLab CI** and **GitHub Actions** for comprehensive CI/CD coverage.

## GitLab CI Configuration

### Required Environment Variables

Set these in **GitLab CI/CD Variables** (`Project Settings > CI/CD > Variables`):

```bash
# GitHub Personal Access Token (Classic)
GITHUB_TOKEN=<your_github_pat>
```

**Variable Settings:**
- **Type:** Variable
- **Environment Scope:** All (default)
- **Protect Variable:** ✅ Enabled
- **Mask Variable:** ✅ Enabled

### GitHub Token Requirements

Your GitHub Personal Access Token (PAT) must have:
- Repository access scope
- Push permissions for `ElliotBadinger/echo` repository

To generate a PAT:
1. Go to https://github.com/settings/tokens
2. Generate new token (classic)
3. Select `repo` scope
4. Copy the token value

### Pipeline Features

- **Automatic Sync**: When commits are pushed to `refactor/phase1-modularization-kts-hilt`
- **Token Validation**: Fails early if `GITHUB_TOKEN` is missing
- **Retry Logic**: Automatic retry on failures
- **Cross-Platform Labels**: All output includes "Cross-platform CI" prefix

## GitHub Actions Configuration

### Required Repository Secrets

Set these in **GitHub Repository Settings** (`Settings > Secrets and variables > Actions`):

```bash
# GitLab Personal Access Token
GITLAB_ACCESS_TOKEN=<your_gitlab_pat>
```

### GitLab Token Requirements

Your GitLab Personal Access Token must have:
- `write_repository` or `maintainer` role permissions
- Access to `gitlab.com/ElliotBadinger/echo` project

### Supported Branches

GitHub Actions will run on:
- All push events to any branch
- Pull requests to `main`, `master`, `develop`, and `refactor/**` branches

### Pipeline Jobs

- **Validate**: Gradle setup verification
- **Android Build**: Multi-module build with caching
- **Test Matrix**: Parallel testing across modules
- **Lint**: Code quality checks
- **Sync to GitLab**: Bidirectional sync capability

## Cross-Platform Benefits

✅ **Redundancy**: Both platforms can run CI/CD
✅ **Backup CI**: If one platform fails, the other continues
✅ **Faster Feedback**: Parallel pipeline execution
✅ **Comprehensive Testing**: Matrix testing across all modules
✅ **Bidirectional Sync**: Push from GitLab to GitHub or vice versa

## Testing the Setup

### GitLab CI Test
```bash
# Push to the configured branch to trigger sync
git checkout refactor/phase1-modularization-kts-hilt
git push gitlab refactor/phase1-modularization-kts-hilt
```

### GitHub Actions Test
```bash
# Push any branch to trigger GitHub Actions
git push origin main
```

## Troubleshooting

### Common Issues

**GitLab CI Errors:**
- `GITHUB_TOKEN` not set: Check GitLab CI/CD Variables
- Permission denied: Verify GitHub PAT has `repo` scope
- Branch not syncing: Ensure pushing to `refactor/phase1-modularization-kts-hilt`

**GitHub Actions Errors:**
- `GITLAB_ACCESS_TOKEN` not set: Check GitHub Repository Secrets
- Sync fails: Verify GitLab PAT permissions
- Pipeline doesn't run: Check branch triggers in workflow file

### Log Analysis

Both pipelines include detailed logging:
- `Cross-platform CI:` prefixes for easy identification
- Token validation messages
- Debug information for troubleshooting
- Error messages for failed operations

## Maintenance

- Keep both tokens updated before expiration
- Monitor both platforms for deprecation warnings
- Update pipeline configurations when GitHub/GitLab APIs change
- Review cached dependencies periodically
