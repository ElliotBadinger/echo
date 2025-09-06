# Agent Session Checklist - Echo Project

**Version:** 1.2  
**Last Updated:** 2025-09-06 08:40 SAST

## 🚀 START OF SESSION (Do this FIRST, always)

### Phase 1: Previous Session Audit & Validation (MANDATORY)
*Before assessing the overall project state, you must first verify the claims of the last session. This prevents building on a faulty foundation.*

- [ ] **Identify the Last Change:** Open `AGENT_DOCUMENTATION.md` and locate the most recent entry in the "Change Log". This is your audit target.
- [ ] **List the Claims:** Read the "Result" and "Files Modified" sections of the last change log. Note the specific claims made (e.g., "TIER 2 CLOCK CONVERSION COMPLETE", "integration verified", "comprehensive testing").
- [ ] **Verify Modified Files:** Get the list of files changed in the last commit and compare it to the documentation.
    ```bash
    # See the actual files changed in the last commit
    git show --name-only HEAD
    ```
- [ ] **Audit Test Coverage for Changes:** For every non-test file that was added or modified, verify that a corresponding test file exists and is meaningful.
    *   **Existence Check:** For a changed `src/main/.../Foo.kt`, does `src/test/.../FooTest.kt` exist?
    *   **Content Sanity Check:** Read the contents of the test file. Does it contain `@Test` annotations? Does it import testing libraries (`junit`, `mockito`)? A file with no actual tests is a failure.
    *   **Run Specific Tests:** Run the tests related *only* to the last change. This isolates the validation.
        ```bash
        # Example for the Clock change
        ./gradlew :SaidIt:test --tests "*Clock*Test"
        ```

- [ ] **Verify Full Regression Test:** Run the entire test suite to ensure the last change didn't break anything unexpectedly.
    ```bash
    ./gradlew test
    ```

- [ ] **Make the Audit Verdict:**
    *   [ ] **✅ PASS:** The previous session's documented work is verified and meets the project's testing standards. You may proceed to the next phase.
    *   [ ] **❌ FAIL:** The previous session's work is incomplete, untested, or violates the project's "Error-First" principles (e.g., tests were deleted to achieve a "pass").

- [ ] **Act on the Verdict:**
    *   **If the verdict is FAIL:**
        1.  **Your session's first goal is now to fix the discrepancy.** Create a new Change Log entry titled: `TIER1_FIX - Correcting Incomplete Work from Session [Previous Session Timestamp]`.
        2.  Document your findings from the audit.
        3.  Do not proceed with any other goals until the tests are reinstated and passing. This is now a TIER 1 priority.

### Phase 2: Orientation & State Assessment (5-10 minutes)
*(Only proceed to this phase if the Audit Verdict was PASS)*

- [ ] Read the rest of `AGENT_DOCUMENTATION.md` completely.
- [ ] Check current project state: `./gradlew --version` and basic build status.
- [ ] Run: `./gradlew clean`
- [ ] Review last 3 entries in the Change Log section to understand recent history.

## 🎯 PLANNING PHASE (Before making ANY changes)

### 3. Goal Selection (10 minutes)
- [ ] Review the "Current Priority Goals" list in documentation
- [ ] Select the SMALLEST possible goal that can be completed in this session
- [ ] Write down exactly what success looks like for this goal
- [ ] Plan how you will verify the change works
- [ ] Consider what could go wrong and how to rollback

### 4. Pre-Change Documentation
- [ ] **USE TIME MCP**: Call `get_current_time({timezone: "UTC"})` for accurate timestamps
- [ ] Create new Change Log entry with template using correct UTC time
- [ ] Document exactly what you plan to change
- [ ] List all files you expect to modify
- [ ] Note current state of those files

### 4.1 Error-First Decision Framework
Before making ANY changes, prioritize in this exact order:

#### Step 1: Check for Critical Issues (MUST FIX FIRST)
- [ ] **Build errors** - Any compilation failures
- [ ] **Test failures** - Unit, integration, or UI tests failing
- [ ] **Runtime crashes** - App crashes or ANRs
- [ ] **CI/CD failures** - GitHub Actions workflow failures

#### Step 2: Research Fixes for Issues (Mandatory When Applicable)
- [ ] **When to Research**: Always for TIER 1 errors (e.g., ClassNotFoundException, compilation failures) or any fix requiring technical decisions. This is a principle you must know and apply well: Research ensures informed, reliable solutions.
- [ ] **Process**:
    * Use Brave Search MCP for SOTA fixes: e.g., `brave_web_search({query: "Android Kapt stub conflicts in Kotlin tests 2025", summary: true})`.
    * Use Context7 MCP for Android docs: e.g., query "Gradle Kapt configuration for test classpath".
    * Consult frameworks: e.g., KOTLIN_MIGRATION_FRAMEWORK.md for migration issues, PERFORMANCE_RESEARCH_FRAMEWORK.md for optimization.
    * Document: Include queries, findings, sources, and application in the change log.
- [ ] **Examples**:
    * For ClassNotFoundException: Research "Kapt correctErrorTypes true Android Kotlin" and apply config like `kapt { correctErrorTypes = true }` in build.gradle.kts.
    * For test failures: Search "Android unit test classpath missing main classes 2025".
- [ ] **Know This Well**: Skipping research leads to suboptimal fixes—always prioritize it for accuracy and to avoid repeated issues.

#### Step 3: Verify GitHub Actions Workflow
- [ ] Check if `.github/workflows/android-test.yml` exists and aligns with project requirements:
    * Runs `./gradlew test` for unit tests (including `AudioMemoryTest.kt`).
    * Uses JDK 17 and Gradle wrapper.
    * Publishes test artifacts via `actions/upload-artifact`.
    * Triggers on push/pull_request to `refactor/phase1-modularization-kts-hilt`.
- [ ] If missing or misconfigured, create/update the workflow (see example in `AGENT_WORKFLOW_GUIDE.md`).
- [ ] After pushing changes, monitor CI:
    * Use `list_workflow_runs({owner: "ElliotBadinger", repo: "echo"})` to check status.
    * Use `get_job_logs({run_id: <id>, failed_only: true})` for failures.
    * Use `download_workflow_run_artifact` for test reports.
- [ ] If CI passes but local tests fail, document as a local environment issue.

#### Step 4: Use Research Frameworks for Technical Decisions
For any significant technical work, consult the appropriate framework:
- [ ] **RESEARCH_FRAMEWORK.md** - Overall research-driven development methodology
- [ ] **ML_STRATEGY_FRAMEWORK.md** - ML research and implementation strategy  
- [ ] **PERFORMANCE_RESEARCH_FRAMEWORK.md** - Performance optimization research
- [ ] **UI_UX_ENHANCEMENT_FRAMEWORK.md** - Professional UI development framework
- [ ] **KOTLIN_MIGRATION_FRAMEWORK.md** - Java-to-Kotlin conversion methodology

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

#### Step 7: Testing Requirements Check
Every change must include:
- [ ] **Unit Tests**: Test the component in isolation
- [ ] **Integration Tests**: Test how it works with other components  
- [ ] **Error Handling Tests**: Test failure scenarios
- [ ] **Regression Tests**: Verify existing functionality still works
- [ ] **UI Tests**: If changing UI, test user interactions
- [ ] **Performance Tests**: If affecting audio/ML, measure performance

### 4.2 Decision Examples

#### SCENARIO: Multiple issues exist
```markdown
Current Issues:
- RecorderService crashes on startup (CRITICAL)  
- UI button could be more Material You styled (ENHANCEMENT)
- AudioBuffer could use coroutines (ARCHITECTURAL)

CORRECT ORDER:
1. ✅ Fix RecorderService crash + add crash prevention tests
2. ✅ Convert AudioBuffer to coroutines + add concurrency tests  
3. ✅ Style UI button with Material You + add interaction tests

WRONG ORDER:
❌ Start with UI styling while crashes exist
❌ Do architectural work before fixing stability
```

#### SCENARIO: Choosing between architectural approaches
```markdown
Problem: Audio threading issues

❌ LEGACY: "Add synchronized blocks to existing methods"
✅ BETTER: "Convert to suspend functions with proper dispatchers + concurrency tests"

Problem: File operation errors  

❌ LEGACY: "Add try-catch blocks around operations"
✅ BETTER: "Wrap in Result<T> with comprehensive error handling tests"

Problem: UI component needs improvement

❌ LEGACY: "Quick styling fix"
✅ BETTER: "Redesign with Material You principles + accessibility tests"
```

#### SCENARIO: Testing requirements
```markdown
Change: Convert AudioManager to Kotlin

Required Tests:
✅ Unit: Test AudioManager methods in isolation
✅ Integration: Test AudioManager with RecordingService  
✅ Error: Test AudioManager with invalid input/permissions
✅ Performance: Measure audio processing latency
✅ Regression: Verify existing recording functionality works

Missing Tests:
❌ Only unit tests - insufficient for production readiness
❌ No error handling tests - could cause crashes
❌ No performance validation - could affect real-time audio
```
### 4.3 Research-Enhanced Decision Making

#### Pre-Implementation Research Process:
**For any significant change (threading, performance, UI, ML), conduct research FIRST:**

**STEP 1 - Discovery (Brave Search MCP):**
- [ ] Search for SOTA techniques: `brave_web_search({query: "...", summary: true})`
- [ ] Find performance benchmarks: Include "benchmarks", "optimization", current year
- [ ] Locate implementation guides: Search official documentation sites
- [ ] Document source quality: Academic papers > official docs > blog posts

**STEP 2 - Deep Extraction (Playwright MCP):**
- [ ] Extract complete research papers: `browser_navigate()` → `browser_pdf_save()`
- [ ] Get full documentation: `browser_snapshot()` for structured content
- [ ] Collect code examples: Extract implementation patterns
- [ ] Gather performance data: Benchmarks, optimization techniques, constraints

**STEP 3 - Research Validation:**
- [ ] Cross-reference multiple sources: Ensure consistency across findings
- [ ] Assess mobile applicability: Android constraints, device limitations
- [ ] Set quantified targets: Performance goals based on research
- [ ] Plan validation strategy: How to measure success with GitHub MCP

**STEP 4 - Implementation with CI Validation (GitHub MCP):**
- [ ] Apply research findings incrementally: Small, testable changes
- [ ] Monitor CI performance: `list_workflow_runs()` for benchmark tracking
- [ ] Compare to research predictions: Measured vs. expected improvements
- [ ] Document accuracy: How well research predicted actual results

#### Research Integration Examples:

**SCENARIO: Audio processing optimization**
```
✅ Brave Search: "WebRTC audio processing mobile latency reduction 2024"
✅ Playwright: Extract complete WebRTC documentation + research papers
✅ Implementation: Apply buffer management techniques from research
✅ GitHub MCP: Measure latency improvements through CI benchmarks
✅ Validation: Compare measured latency against research predictions
```

**SCENARIO: UI performance improvement**
```
✅ Brave Search: "jetpack compose performance optimization 2024 case studies"
✅ Playwright: Extract complete Compose performance guides + benchmark data
✅ Implementation: Apply research-backed optimization techniques
✅ GitHub MCP: Monitor UI performance through automated testing
✅ Validation: Measure frame rates against research-established targets
```

### 4.4 UI/UX Excellence Standards
When making frontend changes, ensure:
- [ ] **Material You Design**: Uses modern design tokens and theming
- [ ] **Accessibility**: Proper content descriptions, focus management
- [ ] **Expert Layout**: Clean visual hierarchy, proper spacing
- [ ] **Intuitive Navigation**: Clear user flow and interaction patterns
- [ ] **Performance**: Smooth animations, responsive touch feedback
- [ ] **UI Testing**: Comprehensive interaction and accessibility tests

### 4.4 Production Readiness Checklist
Before completing any change:
- [ ] **All tests pass**: Unit, integration, error, performance tests
- [ ] **No regressions**: Existing functionality verified working  
- [ ] **Error handling**: Comprehensive error scenarios covered
- [ ] **Documentation**: Change properly documented with examples
- [ ] **Performance**: No degradation in critical paths
- [ ] **Accessibility**: UI changes include accessibility testing

## 🔧 EXECUTION PHASE (Making changes)

### Session Execution
- [ ] **Initialize Session**: Record the start time of the session (e.g., using `date +%s` or a timestamp tool).
- [ ] **Task Execution**: Perform the assigned task (e.g., convert a file, fix a TIER 1 error).
- [ ] **Task Completion**: Record the end time upon successful completion (e.g., all tests pass, PR is ready). Calculate the duration (end time - start time) and log it in the session notes.
- [ ] **Audit and Commit**: ...

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
push_files({
  owner: "ElliotBadinger",
  repo: "echo", 
  branch: "current-branch",
  message: "Agent: [specific change description]",
  files: [{path: "file.kt", content: "..."}]
})

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

// Or use natural language with "use context7"
// "How do I optimize AudioRecord for real-time processing? use context7"
```

**STEP 4 - Research Documentation Requirements:**
- [ ] **Document Brave Search findings**: SOTA algorithms, performance benchmarks
- [ ] **Document Context7 findings**: Implementation patterns, API details
- [ ] **Create research-to-implementation mapping**: How findings inform code changes
- [ ] **Set performance targets**: Based on research benchmarks
- [ ] **Plan validation strategy**: CI tests to measure research-predicted improvements

#### Research-Driven Development Examples:

**SCENARIO: Audio latency optimization needed**
```
RESEARCH PHASE:
✅ Brave Search: "WebRTC audio processing mobile latency reduction 2024"
   → Find: Buffer management techniques, processing algorithms
✅ Context7: get Android AudioRecord optimization documentation
   → Find: Android-specific API usage patterns

IMPLEMENTATION PHASE:
✅ Apply research findings to AudioBuffer.kt
✅ Use research-informed buffer sizes and threading patterns
✅ Set performance targets based on research benchmarks

VALIDATION PHASE:  
✅ CI performance tests measure latency improvements
✅ Compare results against research-predicted performance
✅ Document successful patterns for future use
```

**SCENARIO: ML model integration needed**
```
RESEARCH PHASE:
✅ Brave Search: "Whisper model mobile optimization android memory usage 2024"
   → Find: Model size trade-offs, quantization techniques
✅ Context7: get TensorFlow Lite Android implementation guide
   → Find: Integration patterns, optimization flags

IMPLEMENTATION PHASE:
✅ Choose model variant based on research findings
✅ Apply TF Lite optimization techniques from documentation
✅ Implement research-informed memory management

VALIDATION PHASE:
✅ Measure inference time and memory usage
✅ Validate against research benchmarks
✅ Adjust implementation based on performance data
```

#### Research Quality Assurance:
**For Brave Search Results:**
- [ ] **Verify source credibility**: Academic papers, reputable research institutions
- [ ] **Check publication dates**: Prefer 2024+ for rapidly evolving fields
- [ ] **Look for benchmarks**: Quantified performance improvements
- [ ] **Assess mobile relevance**: Ensure findings apply to Android constraints

**For Context7 Results:**
- [ ] **Check documentation currency**: Ensure latest API versions
- [ ] **Verify code examples**: Test snippets work with current Android versions
- [ ] **Cross-reference patterns**: Multiple sources confirm best practices
- [ ] **Validate architecture fit**: Patterns align with Clean Architecture goals

#### Error Analysis Enhanced Workflow:

**For CI Failures:**
1. [ ] Use `get_workflow_run` to understand failure context
2. [ ] Use `get_job_logs` with `failed_only: true` for targeted debugging
3. [ ] Download test artifacts using `download_workflow_run_artifact`
4. [ ] Analyze logs for specific error patterns
5. [ ] Research solutions using Brave Search MCP if errors are complex
6. [ ] Fix issues and push using GitHub MCP functions
7. [ ] Monitor rerun until CI passes

**For Performance Issues:**
1. [ ] Research current optimization techniques using Brave Search MCP
2. [ ] Get Android-specific guidance using Context7 MCP (if available)
3. [ ] Implement research-informed solutions incrementally
4. [ ] Validate improvements using CI performance tests
5. [ ] Document performance gains achieved

#### Research Integration Success Metrics:
- [ ] **Research findings applied**: Clear connection between research and implementation
- [ ] **Performance targets met**: Achieve research-predicted improvements
- [ ] **Documentation quality**: Future developers can understand research basis
- [ ] **Knowledge transfer**: Research insights documented for reuse

#### Example Research-Informed Decision Process:
```
SCENARIO: Audio processing latency issues

STEP 1 - Research:
✅ brave_search("android audio processing latency optimization 2024")
✅ Context7: get Android AudioTrack best practices (if available)

STEP 2 - Analyze Findings:
✅ Document SOTA techniques (WebRTC algorithms, buffer optimization)
✅ Note Android-specific implementation patterns
✅ Plan incremental implementation strategy

STEP 3 - Implement with CI:
✅ Implement smallest change based on research
✅ Push and monitor CI for performance impact
✅ Use CI artifacts to validate latency improvements

STEP 4 - Validate:
✅ Measure performance gains through CI benchmarks
✅ Document successful patterns for future use
```
---

## 📝 END OF SESSION (Do this BEFORE ending)

### 7. Documentation Updates
- [ ] **USE TIME MCP**: Call `get_current_time({timezone: "UTC"})` for completion timestamp
- [ ] Complete the Change Log entry with results and accurate UTC time
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
- [ ] **CRITICAL**: Always verify changes are successfully pushed to repository
- [ ] **CRITICAL**: If MCP push fails, use manual git commands: `git add .`, `git commit -m "message"`, `git push origin HEAD`
- [ ] **CRITICAL**: Resolve any merge conflicts with `git pull --rebase` before pushing
- [ ] Update README.md if any major functionality changed
- [ ] **VERIFY**: Check GitHub to confirm changes are visible

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
5. **COMMIT AND PUSH** reverted state immediately
6. Document what went wrong and why
7. Update documentation with lessons learned

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

// If MCP fails, use manual git:
git add .
git commit -m "Agent Session [DATE]: Fixed [specific issue]"
git pull --rebase origin branch-name  // Resolve conflicts if needed
git push origin HEAD
```

---

**Remember: Small incremental progress is better than grand failures. Use MCP server functions for all Git operations. Every change should be documented and tested immediately.**
