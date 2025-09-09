# Agent Session Checklist

**Simple workflow for consistent, quality development**

## 🔍 Session Start (5 minutes)

### 1. Audit Previous Work
- [ ] Read last entry in `docs/project-state/change-log.md`
- [ ] Verify previous claims by running tests: `./gradlew test`
- [ ] If previous work is broken/incomplete, fix it FIRST

### 2. Check Current State  
- [ ] Read `docs/project-state/current-status.md`
- [ ] Run `./gradlew clean` to verify build health
- [ ] Check `docs/project-state/priorities.md` for current goals

## 🎯 Work Phase

### 3. Choose Goal (Error-First Priority)
- [ ] **TIER 1 errors exist?** Fix build/test failures FIRST
- [ ] **No TIER 1 errors?** Pick smallest TIER 2 improvement
- [ ] **Research needed?** Use Brave Search/Context7 MCP before coding

### 4. Make Change
- [ ] Change ONE thing at a time
- [ ] Test immediately: `./gradlew build` or `./gradlew test`
- [ ] If it breaks, rollback before continuing

### 5. Document (if significant)
- [ ] Use `docs/templates/simple-change-log.md` for important changes
- [ ] Use `docs/templates/mcp-research-notes.md` if MCP research helped

## 📝 Session End

### 6. Commit & Push
```bash
git add .
git commit -m "Agent Session [DATE]: [what you fixed/improved]"
git push origin HEAD
```

### 7. Update Status
- [ ] Update `docs/project-state/current-status.md` if project state changed
- [ ] Update `docs/project-state/priorities.md` with next steps

## ⚠️ Critical Rules

- **Never use GitHub MCP for commits** (causes sync conflicts)
- **Fix TIER 1 errors before anything else** (build/test failures)
- **Make small changes only** (one file, one issue)
- **Test immediately after each change**
- **Research complex issues before guessing**

---

*This checklist focuses on the essential quality gates while eliminating bureaucratic overhead.*