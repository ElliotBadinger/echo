# Agent Quick Start Guide

**Version**: 1.0  
**Updated**: 2025-01-09 09:57 UTC  
**Target**: New AI agents starting work on the Echo project

## 🚀 First Steps (< 2 minutes)

### 1. Immediate Health Check
Before doing anything else, run the agent health check:

```bash
# Quick validation (environment + fast compile + core tests)
bash scripts/agent/healthcheck.sh

# Or just environment checks if time is critical
bash scripts/agent/healthcheck.sh --tier 0
```

**Expected result**: All tiers should pass. If Tier 0 fails, fix environment issues before proceeding.

### 2. Read Project Status
```bash
# Check current project health and known issues
cat docs/project-state/current-status.md
cat docs/project-state/health-dashboard.md
```

### 3. Verify CI Status
Check if CI is currently stable:
```bash
# View recent CI status (if gh CLI available)
gh workflow list
gh run list --limit 5
```

## ✅ Safe Commands for New Agents

### Build Commands (Tested & Safe)
```bash
# Quick compile check (safest, fastest feedback)
./gradlew :SaidIt:compileDebugKotlin :domain:assemble --no-daemon -q

# Core unit tests (fast, no Android dependencies)
./gradlew :domain:test :data:test :core:test --no-daemon --continue

# Full clean build (takes longer but comprehensive)
./gradlew clean build --continue
```

### Testing Commands
```bash
# Specific module tests
./gradlew :domain:test --no-daemon
./gradlew :features:recorder:test --no-daemon

# Test compilation only (no execution)
./gradlew testClasses --no-daemon

# Coverage (if tests are passing)
./gradlew jacocoAll --no-daemon
```

### Safe Development Commands
```bash
# Find source files
find . -name "*.kt" -o -name "*.java" | grep -v build | head -20

# Check dependencies
./gradlew dependencies --configuration implementation

# Verify Gradle setup
./gradlew --version
```

## ⚠️ Current Known Issues

### Temporarily Excluded from CI
- `:SaidIt:test` - Test compilation issues being resolved
- Some mockkStatic references need cleanup

### Safe Test Modules
- `:domain:test` ✅ 
- `:data:test` ✅
- `:core:test` ✅
- `:features:recorder:test` ✅ (with --with-android flag)

## 🔄 Iteration Pattern

1. **Start**: Run `bash scripts/agent/healthcheck.sh --tier 0-1`
2. **Develop**: Make small, focused changes
3. **Validate**: Run `bash scripts/agent/healthcheck.sh --tier 2` 
4. **Commit**: Use manual git commands (avoid automated tools)
5. **CI Check**: Monitor GitHub Actions after push

## 📚 Key Documentation

- `WARP.md` - Project overview, architecture, essential commands
- `docs/agent-workflow/core-principles.md` - Non-negotiable development rules
- `docs/project-state/current-status.md` - Live project state
- `docs/agent-workflow/session-checklist.md` - Development workflow

## 🎯 Priority Levels (CRITICAL)

**TIER 1**: Build failures, compilation errors, test failures, runtime crashes
**TIER 2**: Code quality, architecture improvements, small features  
**TIER 3**: Major features, architectural changes

Always fix TIER 1 issues before proceeding to lower tiers.

## 🚫 Avoid These Patterns

- Don't run `./gradlew build` immediately without checking health first
- Don't assume all tests pass - use the health check to identify safe modules
- Don't use automated git tools - use manual git commands only
- Don't make large architectural changes without confirming current stability

## 💡 Tips for Success

- **Start small**: Use health check tiers to build confidence progressively
- **Fast feedback**: Tier 0-1 gives you feedback in under 30 seconds
- **Test incrementally**: Add `--with-android` only after core tests pass
- **Document changes**: Use the provided templates for significant changes
- **Ask for clarification**: Better to ask than assume and break CI

## 🆘 If Things Go Wrong

1. **Build fails**: Check `docs/project-state/health-dashboard.md` for known issues
2. **Tests fail**: Use `--continue` flag to see all failures, focus on safe modules first  
3. **CI fails**: Check recent successful runs for pattern comparison
4. **Environment issues**: Ensure Java 17, proper Android SDK setup per WARP.md

## 📞 Getting Help

- Check health dashboard for current status
- Review recent git history for context
- Use health check script to isolate issues to specific tiers
- Focus on TIER 1 priorities first

---

**Remember**: The health check script is your friend. When in doubt, run it first! 🛡️
