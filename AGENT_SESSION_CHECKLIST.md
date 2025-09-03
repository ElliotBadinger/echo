# Agent Session Checklist - Echo Project

## 🚀 START OF SESSION (Do this FIRST, always)

### 1. Orientation Phase (5-10 minutes)
- [ ] Read `AGENT_DOCUMENTATION.md` completely
- [ ] Check current project state: `./gradlew --version` and basic build status
- [ ] Review last 3 entries in the Change Log section
- [ ] Identify what the previous session was working on
- [ ] Check if there are any pending/broken changes that need fixing first

### 2. Current State Assessment (5 minutes)
- [ ] Run: `./gradlew clean` (note: may timeout, that's expected)
- [ ] Check if tests run: `./gradlew test` (also may fail, document what fails)
- [ ] Look for any obvious error messages or build failures
- [ ] Update "Current Status" section with any new findings

## 🎯 PLANNING PHASE (Before making ANY changes)

### 3. Goal Selection (10 minutes)
- [ ] Review the "Current Priority Goals" list in documentation
- [ ] Select the SMALLEST possible goal that can be completed in this session
- [ ] Write down exactly what success looks like for this goal
- [ ] Plan how you will verify the change works
- [ ] Consider what could go wrong and how to rollback

### 4. Pre-Change Documentation
- [ ] Create new Change Log entry with template
- [ ] Document exactly what you plan to change
- [ ] List all files you expect to modify
- [ ] Note current state of those files

## 🔧 EXECUTION PHASE (Making changes)

### 5. Make Changes (Incremental approach)
- [ ] Make ONE small change at a time
- [ ] Test immediately after each change
- [ ] If something breaks, rollback immediately
- [ ] Document each successful incremental change
- [ ] Never make multiple unrelated changes at once

### 6. Verification Requirements
- [ ] Build succeeds (or fails in expected/better way)
- [ ] Tests pass (or fail in fewer/different ways)
- [ ] No new errors introduced
- [ ] Feature works as intended (if applicable)
- [ ] Performance hasn't degraded

## 📝 END OF SESSION (Do this BEFORE ending)

### 7. Documentation Updates
- [ ] Complete the Change Log entry with results
- [ ] Update issue statuses if anything was resolved
- [ ] Update "Current Status" section if project state changed
- [ ] Move completed goals from priority list
- [ ] Add any newly discovered issues to the issue list

### 8. Handoff Preparation
- [ ] Fill in "Next Agent Should Focus On" section
- [ ] Highlight any partially completed work
- [ ] Note any new insights about persistent issues
- [ ] Update "Today's Focus" and "Changes Made This Session"

### 9. Git Operations (Use MCP Server!)
- [ ] Use `push_files()` MCP function to commit and push all changes
- [ ] Never use manual git commands when MCP server is available
- [ ] Verify changes are successfully pushed to repository
- [ ] Update README.md if any major functionality changed

## ⚠️ CRITICAL WARNINGS

### Things to NEVER do:
- ❌ Don't start making changes without reading documentation first
- ❌ Don't attempt large architectural changes in one session
- ❌ Don't assume context from previous conversations
- ❌ Don't make changes without testing them immediately
- ❌ Don't end session without documenting what you did
- ❌ Don't use manual git commands when MCP server is available

### Emergency Rollback Process:
If you break something badly:
1. **STOP** making changes immediately
2. Identify exactly what you changed
3. Revert changes one by one in reverse order
4. Test after each revert
5. Document what went wrong and why
6. Update documentation with lessons learned

## 📊 Session Success Indicators

### Minimum Success (any session should achieve at least one):
- [ ] Fixed one small build issue
- [ ] Resolved one test failure
- [ ] Made one incremental improvement to code
- [ ] Gained better understanding of one problem area
- [ ] Updated documentation with new insights

### Good Success:
- [ ] Completed one full priority goal
- [ ] Made measurable progress toward larger goal
- [ ] Improved build or test stability
- [ ] Added useful new documentation

### Excellent Success:
- [ ] Completed multiple small goals
- [ ] Resolved a persistent issue
- [ ] Made significant progress on high-priority item
- [ ] Left clear path forward for next session

## 🎯 Quick Reference - Current Priorities

Based on documentation, current top priorities are:
1. **Build System:** ✅ RESOLVED (100% success rate achieved)
2. **RecordingViewModelTest:** ✅ RESOLVED (now passes successfully)
3. **Other Test Failures:** 🟡 NEEDS INVESTIGATION (focus here next)
4. **SaidItService Threading:** 🔴 ACTIVE ISSUE (threading violations)
5. **AudioMemory Thread Safety:** 🟡 POTENTIAL ISSUE (needs investigation)

## 🤖 MCP Server Integration Reminders

### Always Use MCP Functions Instead of Manual Git:
- ✅ `push_files()` - Push multiple files in one commit (PREFERRED)
- ✅ `create_or_update_file()` - Create/update individual files
- ✅ `get_file_contents()` - Read files from repository
- ❌ Don't use: `git add`, `git commit`, `git push` commands

### Example MCP Usage:
```javascript
// At end of session - push all changes at once
push_files({
  owner: "ElliotBadinger",
  repo: "echo",
  branch: "refactor/phase1-modularization-kts-hilt",
  message: "Agent Session [DATE]: Fixed [specific issue]",
  files: [
    {path: "build.gradle.kts", content: "..."},
    {path: "AGENT_DOCUMENTATION.md", content: "...updated docs..."}
  ]
})
```

---

**Remember: Small incremental progress is better than grand failures. Use MCP server functions for all Git operations. Every change should be documented and tested immediately.**