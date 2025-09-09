# Complete AI Agent Workflow Guide for Echo Android Project

**Version:** 2.0 - Unified Documentation System  
**Target Audience:** Claude, ChatGPT, and other AI development assistants  
**Project:** Echo Android Application  
**Last Updated:** 2025-09-06 18:40 UTC

**IMPORTANT NOTE ON RESEARCHING FIXES:** When encountering errors or issues (especially TIER 1 build/test failures), **always research fixes using available MCP tools and frameworks before attempting changes**. This is a core principle you must know and apply rigorously: Use Brave Search MCP for state-of-the-art (SOTA) solutions and scientific papers, Context7 MCP for Android API documentation, and integrate findings from docs/frameworks/research-framework.md or specific frameworks like docs/frameworks/kotlin-migration.md. Document all research in change logs using the MCP usage template, including sources and how they apply to the fix.

---

## 🎯 ESSENTIAL READING - START HERE

### 🚨 MANDATORY READING ORDER (Every AI Agent MUST Follow)

**Phase 1: Core Principles & Workflow (MANDATORY - 15 minutes)**
1. **[🚨 Critical Principles](critical-principles.md)** - NON-NEGOTIABLE quality standards (READ FIRST)
2. **This guide** (detailed-guide.md) - Complete workflow reference (15-20 minutes)
3. **[Session Checklist](session-checklist.md)** - Step-by-step executable workflow
4. **[Quick Start Guide](quick-start.md)** - 15-minute agent onboarding

**Phase 2: Project State Assessment (MANDATORY - 5 minutes)**
5. **[Current Status](../project-state/current-status.md)** - Live project state and critical issues
6. **[Current Priorities](../project-state/priorities.md)** - Current development priorities
7. **[Change Log](../project-state/change-log.md)** - Historical changes and research findings

**Phase 3: Technical Framework Reference (AS NEEDED)**
- **[Research Framework](../frameworks/research-framework.md)** - Research-driven development methodology
- **[Kotlin Migration Framework](../frameworks/kotlin-migration.md)** - Java-to-Kotlin conversion methodology
- **[ML Strategy Framework](../frameworks/ml-strategy.md)** - ML research and implementation strategy
- **[Performance Framework](../frameworks/performance-framework.md)** - Performance optimization research
- **[UI/UX Framework](../frameworks/ui-ux-framework.md)** - Professional UI development framework
- **[Framework Integration](../frameworks/framework-integration.md)** - How all frameworks work together

**Phase 4: MCP Server Integration (AS NEEDED)**
- **[MCP Optimization](../mcp-integration/mcp-optimization.md)** - Overall MCP server usage strategy
- **[Context7 Guide](../mcp-integration/context7-guide.md)** - Android documentation access
- **[Brave Search Guide](../mcp-integration/brave-search-guide.md)** - Technical research methodology
- **[GitHub MCP Guide](../mcp-integration/github-mcp-guide.md)** - CI/CD and repository management
- **[Playwright Guide](../mcp-integration/playwright-guide.md)** - Web research and extraction

**Phase 5: Templates & Documentation (WHEN DOCUMENTING)**
- **[Templates Directory](../templates/)** - Standardized documentation templates
- **[Automation Directory](../automation/)** - Documentation automation scripts

### If you're continuing from a previous session in the SAME conversation:
- Still read [Critical Principles](critical-principles.md) to ensure compliance
- Check [Current Status](../project-state/current-status.md) for any changes
- Skip to "Making Changes Workflow" section
- Still verify current project state with `./gradlew clean`

---

## 🚀 NEW SESSION STARTUP PROCESS

### Phase 1: Orientation (10 minutes)

#### Step 1: Read Critical Documentation
```bash
# In this exact order:
1. Read docs/project-state/current-status.md (focus on "Current Status" and "Change Log")
2. Read docs/agent-workflow/session-checklist.md (your workflow template)
3. Check docs/project-state/priorities.md for current focus areas
4. Review docs/project-state/research-findings.md for latest research
```

#### Step 2: Assess Current Project State
```bash
# Run these commands to understand current state:
cd echo
./gradlew --version                    # Confirm environment
./gradlew clean                        # Should complete in ~20-30s
timeout 120s ./gradlew build          # May fail, document how/where
```

#### Step 3: Update Documentation
- Open `docs/project-state/current-status.md`
- Update "Current Session Workspace" section:
  - Fill in "Today's Focus"
  - Note any changes in build behavior
  - Record your session start time

### Phase 2: Goal Selection (5 minutes)

#### Choose the SMALLEST Possible Goal
- Look at "Current Priority Goals" in `docs/project-state/priorities.md`
- Select the topmost goal that seems achievable in 1-2 hours
- Break it down further if it seems too large
- Write down EXACTLY what success looks like

#### Success Criteria Template:
```
Goal: [Specific thing to fix/improve]
Success Metric: [How you'll know it worked]
Test Plan: [How you'll verify it works]
Rollback Plan: [How to undo if it breaks things]
```

---

## 🔧 MAKING CHANGES WORKFLOW

### The Golden Rules:

1.  **VERIFY PREVIOUS WORK FIRST** - Always audit the last session's changes before starting new work.
2.  **ONE CHANGE AT A TIME** - Never modify multiple unrelated things.
3.  **TEST IMMEDIATELY** - After each change, verify it works.
4.  **ROLLBACK ON FAILURE** - If something breaks, undo immediately.
5.  **DOCUMENT EVERYTHING** - Use the MCP usage template from docs/templates/mcp-usage-template.md.
6.  **USE MCP SERVERS** - Always use MCP tools for research and GitHub functions for repository operations.

### Change Process:

#### Step 1: Pre-Change Documentation
```markdown
## Change [YYYY-MM-DD HH:MM] - [CHANGE_ID]

### Goal
- What specific problem this addresses
- Expected outcome

### Research Conducted (if applicable)
- MCP Tool Used: [e.g., Brave Search MCP, Context7 MCP]
- Query: [e.g., "Android Kotlin Kapt stub conflicts fixes 2025"]
- Key Findings: [Summarize relevant solutions and sources]
- Application to Fix: [How research informs the planned change]

### Files Modified
- [List files you plan to change]

### Testing Plan
- How you'll verify this works
```

#### Step 2: Research Fixes (Mandatory for Errors/Fixes)
- **When Applicable**: Always for TIER 1 issues (e.g., build errors, test failures like ClassNotFoundException) or any non-trivial fix.
- **Process**:
  - Use **[Brave Search MCP](../mcp-integration/brave-search-guide.md)**: `brave_web_search({query: "specific error + Android Kotlin 2025", summary: true})` for SOTA fixes.
  - Use **[Context7 MCP](../mcp-integration/context7-guide.md)**: For Android-specific guidance, e.g., "Kapt configuration for Kotlin test classpath".
  - Cross-reference with frameworks: e.g., **[Kotlin Migration Framework](../frameworks/kotlin-migration.md)** for migration-related issues.
  - Consult **[Research Framework](../frameworks/research-framework.md)** for overall research methodology.
  - Document: Summarize findings, sources, and rationale in the pre-change log using **[MCP Usage Template](../templates/mcp-usage-template.md)**.
- **Know This Well**: Research is not optional—it's the foundation for informed, error-free changes. Avoid guessing; base fixes on documented best practices from **[Critical Principles](critical-principles.md)**.

#### Step 3: Make ONE Small Change
- Edit one file or fix one specific issue
- Keep the change as minimal as possible
- Do NOT make multiple changes at once

#### Step 4: Test Immediately
```bash
# For build changes:
./gradlew clean build

# For specific module changes:
./gradlew :module-name:compileDebugUnitTestKotlin

# For test fixes:
./gradlew :module-name:test
```

#### Step 5: Document MCP Usage
Use the **[MCP Usage Template](../templates/mcp-usage-template.md)** to track:
- Which MCP servers were used (see **[MCP Optimization Guide](../mcp-integration/mcp-optimization.md)**)
- Effectiveness of each tool
- Time saved through research
- Implementation success rate
- Cross-reference with specific MCP guides:
  - **[Context7 Guide](../mcp-integration/context7-guide.md)** for Android documentation
  - **[Brave Search Guide](../mcp-integration/brave-search-guide.md)** for research
  - **[GitHub MCP Guide](../mcp-integration/github-mcp-guide.md)** for CI/CD monitoring

#### Step 6: Commit and Push Changes
- **Preferred Method**: Use manual git commands for commits (see GitHub MCP synchronization warning below)
- **Verify Push Success**: Check that changes are visible on GitHub
- **Handle Merge Conflicts**: Resolve any conflicts before pushing

```bash
# The ONLY safe way to commit changes
git add .
git commit -m "Agent Session [DATE]: Description"
git push origin HEAD
```

#### Step 7: Verify GitHub Actions Workflow
- **Check Existing Workflow**: Read `.github/workflows/android-test.yml` to ensure it runs `./gradlew test` on push/pull_request
- **Monitor Workflow**: After pushing, use GitHub MCP functions to check run status
- **Document CI Results**: Update your change log with CI validation results

#### Step 8: Document Results
Complete the change log entry:
```markdown
### Result
- ✅ SUCCESS: What worked and why
- ❌ FAILED: What didn't work and why
- 🟡 PARTIAL: What partially worked
- 🔍 MCP Insights: Learnings about MCP tool effectiveness

### Next Steps
- What should be done next
- MCP optimization opportunities identified

### Rollback Info
- How to revert if needed
```

### What Counts as "One Change":
- ✅ Add one missing dependency
- ✅ Fix one compilation error
- ✅ Resolve one import issue
- ✅ Fix one failing test
- ❌ "Refactor the entire service layer"
- ❌ "Migrate to Jetpack Compose"
- ❌ "Fix all the threading issues"

---

## 📁 KEY FILE LOCATIONS & WHAT THEY CONTAIN

### MUST-READ Documentation Files:
- **[Current Status](../project-state/current-status.md)** - Your primary reference, current state tracker
- **[Session Checklist](session-checklist.md)** - Step-by-step workflow checklist
- **[Current Priorities](../project-state/priorities.md)** - Current priority goals and next steps
- **[MCP Optimization](../mcp-integration/mcp-optimization.md)** - MCP server usage optimization

### Framework Documentation (Consult When Needed):
- **[Kotlin Migration Framework](../frameworks/kotlin-migration.md)** - Java-to-Kotlin conversion methodology
- **[Research Framework](../frameworks/research-framework.md)** - Research-driven development methodology
- **[ML Strategy Framework](../frameworks/ml-strategy.md)** - ML implementation guide
- **[Performance Framework](../frameworks/performance-framework.md)** - Performance optimization
- **[UI/UX Framework](../frameworks/ui-ux-framework.md)** - UI/UX development
- **[Framework Integration](../frameworks/framework-integration.md)** - How all frameworks work together

### MCP Integration Guides (Use for Research & Automation):
- **[Context7 Guide](../mcp-integration/context7-guide.md)** - Android documentation access
- **[Brave Search Guide](../mcp-integration/brave-search-guide.md)** - Technical research methodology
- **[GitHub MCP Guide](../mcp-integration/github-mcp-guide.md)** - CI/CD and repository management
- **[Playwright Guide](../mcp-integration/playwright-guide.md)** - Web research and extraction

### Documentation Templates (Use When Documenting):
- **[Change Log Template](../templates/change-log-template.md)** - Standardized change documentation
- **[MCP Usage Template](../templates/mcp-usage-template.md)** - MCP server usage tracking
- **[Research Template](../templates/research-template.md)** - Research documentation
- **[Session Report Template](../templates/session-report-template.md)** - Session documentation
- **[Testing Template](../templates/testing-template.md)** - Testing documentation

### Common Code Locations:
- **`build.gradle.kts`** - Root project configuration
- **`gradle/libs.versions.toml`** - Dependency version catalog
- **`SaidIt/`** - Main application module
- **`features/recorder/`** - Audio recording functionality
- **`domain/`** - Business logic and use cases

---

## 🎯 CURRENT PROJECT STATE REFERENCE

### Build Status (as of last update):
- **Clean Build:** ✅ Works (20-30 seconds)
- **Full Build:** ✅ 100% success rate (compiles, tests run properly)
- **Test Compilation:** ✅ Fixed (was blocking before)
- **Test Execution:** ✅ 93% pass rate (14/15 tests pass)

### Major Known Issues:
1. **AudioMemoryTest** - Local environment issue (CI passes)
2. **SaidItService** - Threading violations
3. **File Locking** - CI/CD and Windows issues
4. **Architecture** - Needs modularization

### Current Development Focus:
- **TIER 2 Kotlin Migration**: Converting Java utilities to Kotlin
- **Next Target**: UserInfo.java → UserInfo.kt conversion
- **Methodology**: Research-driven with comprehensive testing

---

## 🔬 RESEARCH & MCP INTEGRATION TOOLS

### Available MCP Server Integration
When implementing features or optimizing performance, use available MCP servers for enhanced capabilities:

#### GitHub MCP Server (Already Available)
**Use for**: CI/CD workflow monitoring and artifact analysis
- Monitor GitHub Actions workflow runs and build status
- Download and analyze test artifacts and logs  
- Manage workflow runs (cancel, rerun, get usage statistics)
- Access job logs and failure details for debugging

#### Brave Search MCP Server (Configured)
**Use for**: Scientific research and state-of-the-art algorithm discovery
- Research latest academic papers and scholarly articles
- Find cutting-edge audio processing algorithms and techniques
- Discover mobile ML optimization patterns and performance studies
- Search for Android development best practices and case studies

#### Context7 MCP Server (Configured)
**Use for**: Fetching current Android development documentation
- Get up-to-date Android API documentation and examples
- Access current Kotlin, Jetpack Compose, and architecture guides
- Fetch specific library documentation (Hilt, Room, Coroutines)

### Research Tool Selection Matrix

**Use Brave Search MCP When:**
- ✅ Researching SOTA algorithms and scientific papers
- ✅ Finding performance optimization research
- ✅ Looking for academic studies on mobile ML/audio
- ✅ Privacy-focused research without tracking

**Use Context7 MCP When:**
- ✅ Need current API documentation and examples
- ✅ Want library-specific implementation guidance
- ✅ Looking for official Android development patterns
- ✅ Need code examples and best practices

---

## 📋 SPECIFIC WORKFLOWS BY TASK TYPE

### For Build Failures:
1. Read the error message carefully
2. Identify the specific file and line causing issues
3. Check if it's a dependency, import, or syntax issue
4. Make the minimal fix needed
5. Test compilation before proceeding

### For Test Failures:
1. Run the specific failing test in isolation
2. Read the stack trace to understand the failure
3. Check if dependencies or mocking setup are correct
4. Fix one test at a time
5. Verify the fix with repeated test runs

### For New Feature Development:
1. **DON'T** - Focus on fixing existing issues first
2. If absolutely necessary, implement in smallest possible increments
3. Add tests for any new functionality
4. Update documentation

---

## 🚫 CRITICAL DON'Ts - THINGS THAT ALWAYS FAIL

### Never Attempt These in One Session:
- ❌ "Refactor the architecture"
- ❌ "Migrate everything to Compose"
- ❌ "Fix all threading issues"
- ❌ "Implement dependency injection"
- ❌ "Rewrite the service layer"

### Anti-Patterns to Avoid:
- ❌ Making multiple unrelated changes at once
- ❌ Assuming context from previous conversations
- ❌ Starting work without reading current documentation
- ❌ Ignoring test failures to "move forward"
- ❌ Making changes without understanding their impact

---

## ✅ BEST PRACTICES - THINGS THAT WORK

### Successful Change Patterns:
- ✅ Add one missing dependency
- ✅ Fix one import error
- ✅ Resolve one specific compilation issue
- ✅ Update one test to work correctly
- ✅ Add one missing configuration property

### Effective Debugging Approach:
- Start with the error message
- Find the smallest reproduction case
- Fix the immediate cause, not the underlying architecture
- Test the fix thoroughly
- Document what you learned

---

## 📝 DOCUMENTATION REQUIREMENTS

### ALWAYS Update These Files:

#### Change Log Entry:
Use the **[Change Log Template](../templates/change-log-template.md)** which includes:
- MCP server usage tracking (see **[MCP Usage Template](../templates/mcp-usage-template.md)**)
- Research findings documentation (see **[Research Template](../templates/research-template.md)**)
- Implementation success metrics
- Time savings analysis

#### Project State Documentation:
- Update **[Current Status](../project-state/current-status.md)** with current build status
- Update **[Current Priorities](../project-state/priorities.md)** with completed/moved goals
- Add research findings to **[Research Findings](../project-state/research-findings.md)**
- Follow **[Critical Principles](critical-principles.md)** for quality standards

---

## 🎯 SESSION HANDOFF PROCESS

### Before Ending Your Session:

#### Update Documentation:
1. Complete your change log entry using the **[MCP Usage Template](../templates/mcp-usage-template.md)**
2. Update "Next Agent Should Focus On" section in **[Current Priorities](../project-state/priorities.md)** with:
   - Specific next task
   - Files that need attention
   - Expected challenges
   - Success criteria
3. Ensure compliance with **[Critical Principles](critical-principles.md)**
4. Update **[Current Status](../project-state/current-status.md)** if project state changed

#### Clean Up:
1. Ensure all changes are committed and pushed
2. Verify no uncommitted changes remain (`git status`)
3. Test that your changes don't break basic functionality

---

## 🔍 TROUBLESHOOTING GUIDE

### When Builds Fail:
1. Read the error message carefully - Don't skim
2. Identify the specific cause - compilation vs runtime vs test
3. Check recent changes - what did you change that might cause this?
4. Isolate the problem - run smaller parts of the build
5. Fix the immediate cause - not the root architectural issue

### When Tests Fail:
1. Run just the failing test - `./gradlew :module:test --tests SpecificTestName`
2. Check the full stack trace - not just the summary
3. Verify test dependencies - are all required libraries present?
4. Check mocking setup - are mocks configured correctly?
5. Test in isolation - comment out other tests if needed

---

## 📊 SUCCESS METRICS

### Session Success Levels:

#### Minimum Success (Every session should achieve):
- [ ] Read all required documentation
- [ ] Understood current project state
- [ ] Made at least one small improvement
- [ ] Documented changes properly using MCP usage template
- [ ] Left clear instructions for next session

#### Good Success:
- [ ] Fixed one complete issue (compilation, test, etc.)
- [ ] Improved a measurable metric (build success rate, test pass rate)
- [ ] Added useful documentation or insights
- [ ] Identified clear next steps

#### Excellent Success:
- [ ] Fixed multiple small issues
- [ ] Significantly improved project stability
- [ ] Resolved a persistent problem
- [ ] Created reusable solutions or documentation

---

## 🔚 FINAL REMINDERS

### Every Single Session:
1. **Read the documentation first** - No exceptions
2. **Make small changes** - Resist the urge to do more
3. **Test immediately** - After every change
4. **Document everything** - Use MCP usage templates
5. **Use MCP servers** - For research and repository operations
6. **Push changes** - Don't leave work uncommitted

### When in Doubt:
- Choose the smaller change
- Fix one specific error message
- Ask for clarification rather than guessing
- Focus on immediate problems, not architectural ones
- Follow the checklist in `docs/agent-workflow/session-checklist.md`

### Remember:
**The goal is not to solve everything in one session. The goal is to make consistent, verifiable progress that builds up over time.**

---

**🎯 Quick Start Summary:**
1. Read `docs/project-state/current-status.md` completely
2. Check current build status: `./gradlew clean && ./gradlew build`
3. Pick the smallest next goal from the priority list
4. Make one small change and test it immediately
5. Document the change using MCP usage template
6. Update handoff instructions for next session

**Good luck and happy coding! 🚀**

---

*This guide is part of the Unified Documentation System v2.0. For MCP integration details, see `docs/mcp-integration/mcp-optimization.md`. For current project status, see `docs/project-state/current-status.md`.*