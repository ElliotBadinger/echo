# Agent Session Checklist - Echo Project

**Version:** 2.0 - Unified Documentation System  
**Last Updated:** 2025-09-06 18:40 UTC

## 🚀 START OF SESSION (Do this FIRST, always)

### Phase 1: Previous Session Audit & Validation (MANDATORY)
*Before assessing the overall project state, you must first verify the claims of the last session. This prevents building on a faulty foundation.*

- [ ] **Read Critical Standards:** Review **[Critical Principles](critical-principles.md)** to understand quality requirements
- [ ] **Identify the Last Change:** Open **[Change Log](../project-state/change-log.md)** and locate the most recent entry
- [ ] **List the Claims:** Read the "Result" and "Files Modified" sections of the last change log. Note the specific claims made
- [ ] **Verify Modified Files:** Get the list of files changed in the last commit and compare it to the documentation
    ```bash
    # See the actual files changed in the last commit
    git show --name-only HEAD
    ```
- [ ] **Audit Test Coverage for Changes:** For every non-test file that was added or modified, verify that a corresponding test file exists and is meaningful
    *   **Existence Check:** For a changed `src/main/.../Foo.kt`, does `src/test/.../FooTest.kt` exist?
    *   **Content Sanity Check:** Read the contents of the test file. Does it contain `@Test` annotations? Does it import testing libraries (`junit`, `mockito`)?
    *   **Quality Check:** Ensure tests follow **[Critical Principles](critical-principles.md)** - no simplified tests to avoid complexity
    *   **Run Specific Tests:** Run the tests related *only* to the last change. This isolates the validation
        ```bash
        # Example for the Clock change
        ./gradlew :SaidIt:test --tests "*Clock*Test"
        ```
- [ ] **Verify Full Regression Test:** Run the entire test suite to ensure the last change didn't break anything unexpectedly
    ```bash
    ./gradlew test
    ```
- [ ] **Make the Audit Verdict:**
    *   [ ] **✅ PASS:** The previous session's documented work is verified and meets the project's testing standards per **[Critical Principles](critical-principles.md)**. You may proceed to the next phase
    *   [ ] **❌ FAIL:** The previous session's work is incomplete, untested, or violates the project's "Error-First" principles
- [ ] **Act on the Verdict:**
    *   **If the verdict is FAIL:**
        1.  **Your session's first goal is now to fix the discrepancy.** Create a new Change Log entry using **[Change Log Template](../templates/change-log-template.md)**
        2.  Document your findings from the audit
        3.  Do not proceed with any other goals until the tests are reinstated and passing per **[Critical Principles](critical-principles.md)**

### Phase 2: Orientation & State Assessment (5-10 minutes)
*(Only proceed to this phase if the Audit Verdict was PASS)*

- [ ] Read **[Current Status](../project-state/current-status.md)** completely
- [ ] Check current project state: `./gradlew --version` and basic build status
- [ ] Run: `./gradlew clean`
- [ ] Review last 3 entries in **[Change Log](../project-state/change-log.md)** to understand recent history
- [ ] Check **[MCP Optimization](../mcp-integration/mcp-optimization.md)** for current MCP usage targets
- [ ] Review **[Current Priorities](../project-state/priorities.md)** for focus areas

## 🎯 PLANNING PHASE (Before making ANY changes)

### 3. Goal Selection (10 minutes)
- [ ] Review the "Current Priority Goals" list in **[Current Priorities](../project-state/priorities.md)**
- [ ] Select the SMALLEST possible goal that can be completed in this session
- [ ] Write down exactly what success looks like for this goal
- [ ] Plan how you will verify the change works
- [ ] Consider what could go wrong and how to rollback
- [ ] Ensure goal aligns with **[Critical Principles](critical-principles.md)** - no shortcuts or test simplification

### 4. Pre-Change Documentation
- [ ] **USE TIME MCP**: Call `get_current_time({timezone: "UTC"})` for accurate timestamps
- [ ] Create new Change Log entry using **[Change Log Template](../templates/change-log-template.md)**
- [ ] Document exactly what you plan to change
- [ ] List all files you expect to modify
- [ ] Note current state of those files
- [ ] Plan MCP server usage for research and implementation (see **[MCP Optimization](../mcp-integration/mcp-optimization.md)**)

### 4.1 Error-First Decision Framework
Before making ANY changes, prioritize in this exact order:

#### Step 1: Check for Critical Issues (MUST FIX FIRST)
- [ ] **Build errors** - Any compilation failures
- [ ] **Test failures** - Unit, integration, or UI tests failing
- [ ] **Runtime crashes** - App crashes or ANRs
- [ ] **CI/CD failures** - GitHub Actions workflow failures

#### Step 2: Research Fixes for Issues (Mandatory When Applicable)
- [ ] **When to Research**: Always for TIER 1 errors or any fix requiring technical decisions
- [ ] **Process**:
    * Use Brave Search MCP for SOTA fixes: `brave_web_search({query: "Android Kapt stub conflicts in Kotlin tests 2025", summary: true})`
    * Use Context7 MCP for Android docs: Query "Gradle Kapt configuration for test classpath"
    * Consult frameworks: docs/frameworks/kotlin-migration.md for migration issues
    * Document: Include queries, findings, sources, and application in the change log using MCP usage template
- [ ] **Examples**:
    * For ClassNotFoundException: Research "Kapt correctErrorTypes true Android Kotlin"
    * For test failures: Search "Android unit test classpath missing main classes 2025"

#### Step 3: Verify GitHub Actions Workflow
- [ ] Check if `.github/workflows/android-test.yml` exists and aligns with project requirements
- [ ] If missing or misconfigured, create/update the workflow
- [ ] After pushing changes, monitor CI using GitHub MCP functions
- [ ] If CI passes but local tests fail, document as a local environment issue

#### Step 4: Use Research Frameworks for Technical Decisions
For any significant technical work, consult the appropriate framework:
- [ ] **[Research Framework](../frameworks/research-framework.md)** - Overall research-driven development methodology
- [ ] **[ML Strategy Framework](../frameworks/ml-strategy.md)** - ML research and implementation strategy  
- [ ] **[Performance Framework](../frameworks/performance-framework.md)** - Performance optimization research
- [ ] **[UI/UX Framework](../frameworks/ui-ux-framework.md)** - Professional UI development framework
- [ ] **[Kotlin Migration Framework](../frameworks/kotlin-migration.md)** - Java-to-Kotlin conversion methodology
- [ ] **[Framework Integration](../frameworks/framework-integration.md)** - How all frameworks work together

#### Step 5: Apply Incremental Improvement Strategy
- [ ] **Build Errors**: Are there any compilation failures?
- [ ] **Test Failures**: Are any unit/integration tests failing?
- [ ] **Runtime Crashes**: Are there any exceptions or crashes?
- [ ] **Resource Issues**: File locks, memory leaks, resource cleanup problems?

**If ANY of these exist, fix them FIRST before any other work.**

#### Step 6: Plan Incremental Improvement (Only if no critical issues)
- [ ] **Maintains Functionality**: Will existing features continue working?
- [ ] **Comprehensive Testing**: Can I add complete tests for this change?
- [ ] **Incremental**: Is this the smallest possible improvement?
- [ ] **Architecture Alignment**: Does this move toward modern patterns?
- [ ] **UI Excellence**: If touching UI, does this follow expert design principles?

#### Step 7: MCP Server Usage Planning
- [ ] **Research Phase**: Plan which MCP servers to use for research
- [ ] **Implementation Phase**: Plan MCP usage for documentation and validation
- [ ] **Documentation Phase**: Plan MCP usage tracking and effectiveness measurement
- [ ] **Validation Phase**: Plan CI monitoring and performance validation

### 4.2 Research-Enhanced Decision Making

#### Pre-Implementation Research Process:
**For any significant change (threading, performance, UI, ML), conduct research FIRST:**

**STEP 1 - Discovery (Brave Search MCP):**
- [ ] Search for SOTA techniques: `brave_web_search({query: "...", summary: true})` (see **[Brave Search Guide](../mcp-integration/brave-search-guide.md)**)
- [ ] Find performance benchmarks: Include "benchmarks", "optimization", current year
- [ ] Locate implementation guides: Search official documentation sites
- [ ] Document source quality: Academic papers > official docs > blog posts

**STEP 2 - Deep Extraction (Playwright MCP):**
- [ ] Extract complete research papers: `browser_navigate()` → `browser_pdf_save()` (see **[Playwright Guide](../mcp-integration/playwright-guide.md)**)
- [ ] Get full documentation: `browser_snapshot()` for structured content
- [ ] Collect code examples: Extract implementation patterns
- [ ] Gather performance data: Benchmarks, optimization techniques, constraints

**STEP 3 - Research Validation:**
- [ ] Cross-reference multiple sources: Ensure consistency across findings
- [ ] Assess mobile applicability: Android constraints, device limitations
- [ ] Set quantified targets: Performance goals based on research
- [ ] Plan validation strategy: How to measure success with GitHub MCP

**STEP 4 - Implementation with CI Validation (GitHub MCP):**
- [ ] Apply research findings incrementally: Small, testable changes per **[Critical Principles](critical-principles.md)**
- [ ] Monitor CI performance: `list_workflow_runs()` for benchmark tracking (see **[GitHub MCP Guide](../mcp-integration/github-mcp-guide.md)**)
- [ ] Compare to research predictions: Measured vs. expected improvements
- [ ] Document accuracy: How well research predicted actual results using **[Research Template](../templates/research-template.md)**

### 4.3 MCP Server Optimization Strategy

#### Context7 Usage Optimization
**Target**: 15-20 tool uses per week (see **[Context7 Guide](../mcp-integration/context7-guide.md)**)
**Focus Areas**:
- Android API documentation access
- Library implementation guidance
- Best practices research
- Code example extraction

**Usage Pattern**:
```javascript
// Library resolution before implementation
resolve_library_id({libraryName: "Android Jetpack Compose"})

// Documentation fetch during development
get_library_docs({
  context7CompatibleLibraryID: "/android/compose",
  topic: "performance optimization",
  tokens: 15000
})
```

#### Brave Search Usage Optimization
**Target**: 10-15 tool uses per week (see **[Brave Search Guide](../mcp-integration/brave-search-guide.md)**)
**Research Areas**:
- Android UI best practices
- Mobile performance optimization
- ML model optimization
- Accessibility guidelines

**Usage Pattern**:
```javascript
// SOTA algorithm research
brave_web_search({
  query: "state-of-the-art real-time audio processing android mobile 2024",
  summary: true,
  freshness: "py"
})
```

#### GitHub MCP Usage Optimization
**Maintain Current High Usage** (see **[GitHub MCP Guide](../mcp-integration/github-mcp-guide.md)**)
**Focus Areas**:
- CI monitoring and validation
- Test artifact analysis
- Workflow optimization
- Performance benchmarking

### 4.4 UI/UX Excellence Standards
When making frontend changes, ensure:
- [ ] **Material You Design**: Uses modern design tokens and theming
- [ ] **Accessibility**: Proper content descriptions, focus management
- [ ] **Expert Layout**: Clean visual hierarchy, proper spacing
- [ ] **Intuitive Navigation**: Clear user flow and interaction patterns
- [ ] **Performance**: Smooth animations, responsive touch feedback
- [ ] **UI Testing**: Comprehensive interaction and accessibility tests

### 4.5 Production Readiness Checklist
Before completing any change:
- [ ] **All tests pass**: Unit, integration, error, performance tests
- [ ] **No regressions**: Existing functionality verified working  
- [ ] **Error handling**: Comprehensive error scenarios covered
- [ ] **Documentation**: Change properly documented with MCP usage template
- [ ] **Performance**: No degradation in critical paths
- [ ] **Accessibility**: UI changes include accessibility testing

## 🔧 EXECUTION PHASE (Making changes)

### Session Execution
- [ ] **Initialize Session**: Record the start time of the session
- [ ] **Task Execution**: Perform the assigned task
- [ ] **Task Completion**: Record the end time upon successful completion
- [ ] **Calculate Duration**: Log duration in session notes
- [ ] **Audit and Commit**: Verify all changes and documentation

### 5. Make Changes (Incremental approach)
- [ ] Make ONE small change at a time
- [ ] Test immediately after each change
- [ ] If something breaks, rollback immediately
- [ ] Document each successful incremental change using MCP usage template
- [ ] Never make multiple unrelated changes at once

### 6. Verification Requirements
- [ ] Build succeeds (or fails in expected/better way)
- [ ] Tests pass (or fail in fewer/different ways)
- [ ] No new errors introduced
- [ ] Feature works as intended (if applicable)
- [ ] Performance hasn't degraded
- [ ] MCP server usage is documented and effective

### 6.1 Enhanced Testing & Research Integration

#### Testing Strategy Decision Matrix:
**Use Local Testing When:**
- [ ] Small, isolated changes (single function, simple bug fix)
- [ ] Changes don't affect audio/performance critical paths
- [ ] Quick iteration needed for development workflow
- [ ] Limited to single module/component

**Use GitHub Actions CI When:**
- [ ] Changes affect multiple modules or core architecture
- [ ] Audio processing or performance-critical modifications  
- [ ] Need matrix testing across Android API levels
- [ ] Complex integration testing required
- [ ] Local testing is slow (>5 minutes) or resource-intensive

#### GitHub Actions Integration Process:
```javascript
// 1. Push changes and trigger CI
// Use manual git commands for commits to avoid sync issues

// 2. Monitor workflow progress
list_workflow_runs({
  owner: "ElliotBadinger",
  repo: "echo",
  workflow_id: "android-ci.yml",
  per_page: 5
})

// 3. If failures occur, get detailed logs
get_job_logs({
  owner: "ElliotBadinger", 
  repo: "echo",
  run_id: 12345,
  failed_only: true,
  return_content: true
})

// 4. Download artifacts for analysis
download_workflow_run_artifact({
  owner: "ElliotBadinger",
  repo: "echo",
  artifact_id: 67890
})
```

#### Research Integration Guidelines:

**Research Tool Selection Decision Tree:**

**STEP 1 - Identify Research Need:**
- [ ] **Scientific/SOTA research needed?** → Use Brave Search MCP
- [ ] **Implementation guidance needed?** → Use Context7 MCP  
- [ ] **Both research types needed?** → Use both in sequence
- [ ] **Simple fact-checking only?** → Use built-in web_search

**STEP 2 - Brave Search MCP Usage (Scientific Research):**
```javascript
// For SOTA algorithm research
brave_search({
  query: "WebRTC VAD mobile optimization android 2024 research papers"
})

// For performance optimization research  
brave_search({
  query: "jetpack compose performance optimization case studies benchmarks"
})

// For UI/UX research
brave_search({
  query: "mobile audio app interface usability studies accessibility research"
})

// For ML model research
brave_search({
  query: "on-device speech recognition mobile optimization tensorflow lite"
})
```

**STEP 3 - Context7 MCP Usage (Implementation Guidance):**
```javascript
// For Android API documentation
get_library_docs({
  context7CompatibleLibraryID: "/android/media",
  topic: "real-time audio processing"
})

// For architecture patterns
get_library_docs({
  context7CompatibleLibraryID: "/android/architecture",
  topic: "clean architecture MVVM"
})

// For UI framework guidance
get_library_docs({
  context7CompatibleLibraryID: "/jetpack/compose", 
  topic: "performance optimization"
})
```

**STEP 4 - Research Documentation Requirements:**
- [ ] **Document Brave Search findings**: SOTA algorithms, performance benchmarks
- [ ] **Document Context7 findings**: Implementation patterns, API details
- [ ] **Create research-to-implementation mapping**: How findings inform code changes
- [ ] **Set performance targets**: Based on research benchmarks
- [ ] **Plan validation strategy**: CI tests to measure research-predicted improvements

#### MCP Usage Effectiveness Tracking:
For each MCP server usage, document:
- **Query/Request**: What was searched or requested
- **Result Quality**: 1-10 rating of usefulness
- **Time Saved**: Estimated minutes saved vs. manual research
- **Implementation Impact**: How research informed the solution
- **Success Rate**: Whether the research led to a working solution

---

## 📝 END OF SESSION (Do this BEFORE ending)

### 7. Documentation Updates
- [ ] **USE TIME MCP**: Call `get_current_time({timezone: "UTC"})` for completion timestamp
- [ ] Complete the Change Log entry using **[MCP Usage Template](../templates/mcp-usage-template.md)**
- [ ] Update issue statuses if anything was resolved
- [ ] Update "Current Status" section in **[Current Status](../project-state/current-status.md)** if project state changed
- [ ] Move completed goals from priority list in **[Current Priorities](../project-state/priorities.md)**
- [ ] Add any newly discovered issues to the issue list
- [ ] Document MCP server usage effectiveness and optimization opportunities in **[MCP Optimization](../mcp-integration/mcp-optimization.md)**
- [ ] Ensure all changes comply with **[Critical Principles](critical-principles.md)**

### 8. Handoff Preparation
- [ ] Fill in "Next Agent Should Focus On" section in **[Current Priorities](../project-state/priorities.md)**
- [ ] Highlight any partially completed work
- [ ] Note any new insights about persistent issues
- [ ] Update MCP usage statistics and recommendations in **[MCP Optimization](../mcp-integration/mcp-optimization.md)**
- [ ] Document any research findings that should be preserved in **[Research Findings](../project-state/research-findings.md)**
- [ ] Ensure handoff follows **[Critical Principles](critical-principles.md)** for quality standards

### 9. Git Operations (Manual Commands Required)
**⚠️ CRITICAL: Use manual git commands to avoid synchronization conflicts**
```bash
# Add all changes
git add .

# Commit with descriptive message including MCP usage summary
git commit -m "Agent Session [YYYY-MM-DD HH:MM UTC]: [Description of what was fixed/changed]

- Fixed: [specific issue]
- Improved: [specific metric]
- Files: [list main files changed]
- MCP Usage: [summary of MCP tools used and effectiveness]
- Research: [key findings if applicable]"

# Push to current branch
git push origin HEAD
```

**⚠️ NEVER use GitHub MCP push_files() for routine commits** - it creates synchronization conflicts with local git.

### 10. MCP Usage Reporting
- [ ] Document all MCP server usage in change log using **[MCP Usage Template](../templates/mcp-usage-template.md)**
- [ ] Rate effectiveness of each MCP tool used (1-10 scale)
- [ ] Note time saved through MCP research
- [ ] Identify MCP optimization opportunities for future sessions
- [ ] Update **[MCP Optimization](../mcp-integration/mcp-optimization.md)** with usage statistics
- [ ] Cross-reference with specific MCP guides for improvement opportunities

## ⚠️ CRITICAL WARNINGS

### Things to NEVER do:
- ❌ Don't start making changes without reading documentation first
- ❌ Don't attempt large architectural changes in one session
- ❌ Don't assume context from previous conversations
- ❌ Don't make changes without testing them immediately
- ❌ Don't end session without documenting what you did
- ❌ Don't use GitHub MCP push_files() for routine commits (causes sync issues)
- ❌ Don't skip MCP research for complex technical decisions

### Emergency Rollback Process:
If you break something badly:
1. **STOP** making changes immediately
2. Identify exactly what you changed
3. Revert changes one by one in reverse order
4. Test after each revert
5. **COMMIT AND PUSH** reverted state immediately
6. Document what went wrong and why
7. Update documentation with lessons learned

## 📊 Session Success Indicators

### Minimum Success (any session should achieve at least one):
- [ ] Fixed one small build issue
- [ ] Resolved one test failure
- [ ] Made one incremental improvement to code
- [ ] Gained better understanding of one problem area
- [ ] Updated documentation with new insights and MCP usage
- [ ] Optimized MCP server usage effectiveness

### Good Success:
- [ ] Completed one full priority goal
- [ ] Made measurable progress toward larger goal
- [ ] Improved build or test stability
- [ ] Added useful new documentation
- [ ] Enhanced MCP integration effectiveness

### Excellent Success:
- [ ] Completed multiple small goals
- [ ] Resolved a persistent issue
- [ ] Made significant progress on high-priority item
- [ ] Left clear path forward for next session
- [ ] Significantly improved MCP server usage balance and effectiveness

## 🎯 Quick Reference - Current Priorities

Based on documentation, current top priorities are:
1. **Build System:** ✅ RESOLVED (100% success rate achieved)
2. **Kotlin Migration:** 🔄 IN PROGRESS (TIER 2 - incremental improvements)
3. **MCP Optimization:** 🔄 IN PROGRESS (target: 15-20 Context7 uses/week)
4. **Documentation System:** 🔄 IN PROGRESS (unified system implementation)
5. **Testing Excellence:** 🔄 IN PROGRESS (comprehensive test coverage)

## 🤖 MCP Server Integration Guidelines

### Target Usage Balance (Weekly):
- **Context7**: 15-20 uses (Android documentation access)
- **Brave Search**: 10-15 uses (research and SOTA solutions)
- **GitHub MCP**: Maintain high usage (CI monitoring and analysis)
- **Playwright**: 8-12 uses (documentation extraction and validation)

### Always Document MCP Usage:
- Query/Request details
- Result quality rating (1-10)
- Time saved vs. manual research
- Implementation impact
- Success rate and lessons learned

---

**Remember: Small incremental progress is better than grand failures. Use MCP server functions for research and repository monitoring, but manual git commands for commits. Every change should be documented with MCP usage effectiveness tracking.**

---

*This checklist is part of the Unified Documentation System v2.0. For MCP integration details, see `docs/mcp-integration/mcp-optimization.md`. For current project status, see `docs/project-state/current-status.md`.*