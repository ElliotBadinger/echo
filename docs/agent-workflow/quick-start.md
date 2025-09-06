# Agent Onboarding Quick Start Guide

**Target Time:** 15 minutes
**Goal:** Get new agents productive on Echo Android project immediately

---

## 🎯 5-MINUTE ORIENTATION

### Step 1: Understand the Project (2 minutes)
Echo is a modern Android audio recording application with:
- 24/7 background audio recording
- Real-time ML features (Whisper, VAD)
- Material You UI design
- Clean Architecture with MVVM

### Step 2: Know Your Role (1 minute)
As an AI agent, you will:
- Fix build and test issues
- Implement new features incrementally
- Research solutions using MCP tools
- Document all changes thoroughly

### Step 3: Learn the Workflow (2 minutes)
1. **Read documentation first** - Never start without context
2. **Make small changes** - One issue, one file at a time
3. **Test immediately** - Validate after each change
4. **Document everything** - Use standardized templates
5. **Use MCP servers** - GitHub MCP for all Git operations

---

## 📚 10-MINUTE ONBOARDING

### Essential Reading (5 minutes)
```markdown
# Read in this order:
1. docs/README.md (2 min) - Documentation navigation
2. docs/project-state/current-status.md (3 min) - Project state
```

### Key File Locations
- **Documentation:** `docs/` directory
- **Code:** `SaidIt/`, `features/`, `domain/`, `data/`
- **Build:** `build.gradle.kts`, `gradle/libs.versions.toml`

### Current Project Status
- **Build:** ✅ Clean build works (20-30s)
- **Tests:** ✅ RecordingViewModelTest passes
- **Priority:** Fix remaining test failures

---

## 🛠️ 5-MINUTE FIRST SESSION

### Session Setup (1 minute)
```bash
cd echo
./gradlew --version  # Confirm environment
./gradlew clean      # Check build status
```

### Choose Your First Task (2 minutes)
Look at `docs/project-state/priorities.md` and pick:
- Fix one failing test
- Resolve one build warning
- Add one missing test

### Make Your First Change (2 minutes)
1. **Document intent** in change log template
2. **Make small change** to one file
3. **Test immediately** with `./gradlew test`
4. **Push changes** using GitHub MCP

---

## 🎯 SUCCESS CRITERIA

### Minimum Success (achieve in first session):
- [ ] Fixed one small issue
- [ ] Ran tests successfully
- [ ] Pushed changes using MCP
- [ ] Updated documentation

### You're Ready When:
- [ ] You understand the project structure
- [ ] You know where to find documentation
- [ ] You can run basic Gradle commands
- [ ] You understand MCP server usage

---

## 🚨 EMERGENCY CONTACTS

### If You Get Stuck:
1. **Read the detailed guide:** `docs/agent-workflow/detailed-guide.md`
2. **Check current priorities:** `docs/project-state/priorities.md`
3. **Review frameworks:** `docs/frameworks/` directory

### Common First Issues:
- **Build fails:** Check `docs/project-state/current-status.md`
- **Tests fail:** Look at CI logs in GitHub Actions
- **MCP issues:** Check `docs/mcp-integration/` guides

---

## 🎉 WELCOME TO THE TEAM!

You're now ready to contribute to Echo Android project. Remember:
- **Small changes, big impact**
- **Research before implementing**
- **Document everything**
- **Use MCP servers effectively**

*For detailed workflow, see `docs/agent-workflow/detailed-guide.md`*
*For session procedures, see `docs/agent-workflow/session-checklist.md`*