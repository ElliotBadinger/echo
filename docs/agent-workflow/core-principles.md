# Core Agent Principles - Echo Project

**MANDATORY READING** - These 5 principles are non-negotiable for all AI agents.

## 🚨 The Error-First Priority System

### Remote-First Rule (New)
- Before local TIER 1, check remote (GitHub) CI/CD. If remote CI is failing, treat that as TIER 1 and fix it first.
- Rationale: Remote CI represents the source of truth and ensures clean-environment reproducibility.

**TIER 1 - Critical Errors (FIX FIRST, ALWAYS)**
- Build failures, test compilation errors, runtime crashes
- **Rule:** Never proceed to TIER 2 if ANY TIER 1 issues exist

**TIER 2 - Incremental Improvements**  
- Feature additions, code quality improvements, architecture enhancements
- **Rule:** Small changes only, test immediately after each change

**TIER 3 - Major Features**
- Large architectural changes, new major functionality
- **Rule:** Only attempt if TIER 1 & 2 are completely clear

## 🔬 Research Before Coding

**When encountering any error or technical decision:**
1. Use **Brave Search MCP** for state-of-the-art solutions and research papers
2. Use **Context7 MCP** for Android API documentation and implementation guidance  
3. Document research findings in change logs with sources and rationale
4. **Never guess** - base all fixes on documented best practices

## 🔧 Small Changes & Test Immediately

**The Golden Rules:**
- Make ONE change at a time (one file, one specific issue)
- Test immediately after each change: `./gradlew clean build` or `./gradlew test`
- If something breaks, rollback immediately before proceeding
- Commit frequently with descriptive messages

### 🚨 Anti-Complexity-Avoidance Rule
**DO NOT cherry-pick the easiest files to avoid real challenges:**
- ❌ **Lazy Pattern**: Converting only 10-line Application classes or simple data holders
- ❌ **Shortcuts**: Choosing files with minimal logic to convert quickly
- ✅ **Proper Approach**: Tackle meaningful complexity - Activities, Adapters, Services with real business logic
- ✅ **Impact Focus**: Each migration should solve actual architectural problems or improve maintainability

**Size Guidelines for Kotlin Migration:**
- **Avoid**: Files < 30 lines (usually trivial conversions)
- **Prefer**: Files 50-200 lines (meaningful business logic)
- **Challenge**: Files > 200 lines (significant architectural improvements)

## 📋 Session Audit Requirement

**Before starting ANY new work:**
1. Read the last entry in `docs/project-state/change-log.md`
2. Verify the previous session's claims by running tests
3. Check that "completed" conversions have proper comprehensive tests
4. If previous work is incomplete/broken, fix it FIRST before new goals

### 🚨 CRITICAL: Migration Quality Audit
**All previous Kotlin migrations MUST be validated for comprehensive testing:**
- EchoApp.kt - Check if tests cover Application lifecycle, not just annotations
- AppModule.kt - Verify Hilt integration tests, singleton behavior, actual usage verification  
- SaidItFragment.kt - Validate Fragment lifecycle, service integration, UI state testing
- All utility classes - Ensure tests cover actual use cases, not just method signatures

**RED FLAGS - Signs of Inadequate Testing:**
- Tests only check annotations exist (@Module, @Singleton, etc.)
- No integration testing with actual Android framework
- No verification that injected dependencies are actually used
- Tests don't cover error scenarios or edge cases
- Missing actual behavioral verification

## ⚠️ Critical Git Warning

**NEVER use GitHub MCP functions for commits** - they cause synchronization conflicts.

**ALWAYS use manual git commands:**
```bash
git add .
git commit -m "Agent Session [DATE]: Description"
git push origin HEAD
```

**Safe GitHub MCP usage:** Read-only operations only (`list_workflow_runs`, `get_job_logs`)

## 🎯 Quality Gates

**Before claiming any work "complete":**
- [ ] All tests compile and pass
- [ ] No build errors introduced  
- [ ] Comprehensive tests exist (not simplified validation)
- [ ] Changes documented in change log
- [ ] CI pipeline passes

## 📚 Essential Files

**Read these first, always:**
- `docs/project-state/current-status.md` - Current project state
- `docs/project-state/change-log.md` - Recent changes and claims to verify
- `docs/templates/simple-change-log.md` - Use for documenting significant changes

**Reference when needed:**
- Framework docs in `docs/frameworks/` for technical guidance
- MCP integration guides in `docs/mcp-integration/` for research tools

---

**Remember:** Quality is non-negotiable. Comprehensive testing is not optional. Technical shortcuts accumulate debt that hurts the project.

*These principles ensure consistent, high-quality development across all AI agent sessions.*