# Complete AI Agent Workflow Guide for Echo Android Project

**Version:** 1.2  
**Target Audience:** Claude, ChatGPT, and other AI development assistants  
**Project:** Echo Android Application  
**Last Updated:** 2025-09-06 08:40 SAST

**IMPORTANT NOTE ON RESEARCHING FIXES:** When encountering errors or issues (especially TIER 1 build/test failures), **always research fixes using available MCP tools and frameworks before attempting changes**. This is a core principle you must know and apply rigorously: Use Brave Search MCP for state-of-the-art (SOTA) solutions and scientific papers, Context7 MCP for Android API documentation, and integrate findings from RESEARCH_FRAMEWORK.md or specific frameworks like KOTLIN_MIGRATION_FRAMEWORK.md. Document all research in AGENT_DOCUMENTATION.md, including sources and how they apply to the fix. Examples: For ClassNotFoundException, search "Android Kotlin Kapt stub conflicts 2025" via Brave Search MCP; for test classpath issues, query Context7 MCP for "Android Gradle test classpath configuration best practices".

---

## 🎯 ESSENTIAL READING - START HERE

### If this is your FIRST conversation or a NEW Claude session:

1. **Read this entire guide** (15-20 minutes) - DO NOT skip sections
2. **Read `AGENT_DOCUMENTATION.md`** completely - Current project state and change tracking
3. **Read `AGENT_SESSION_CHECKLIST.md`** - Your step-by-step workflow
4. **Follow the "New Session Startup" process below** - Every single time

### If you're continuing from a previous session in the SAME conversation:
- Skip to "Making Changes Workflow" section
- Still check current project state with `./gradlew clean`

---

## 🚀 NEW SESSION STARTUP PROCESS

### Phase 1: Orientation (10 minutes)

#### Step 1: Read Critical Documentation
```bash
# In this exact order:
1. Read AGENT_DOCUMENTATION.md (focus on "Current Status" and "Change Log")
2. Read AGENT_SESSION_CHECKLIST.md (your workflow template)
3. Skim documentation/echo-critical-fixes.md (understand major issues)
4. Check the "Next Agent Should Focus On" section in AGENT_DOCUMENTATION.md
```

#### Step 2: Assess Current Project State
```bash
# Run these commands to understand current state:
cd echo
./gradlew --version                    # Confirm environment
./gradlew clean                        # Should complete in ~20-30s
timeout 120s ./gradlew build          # May fail, document how/where
```

**Note: Use GitHub MCP server functions to read/write files when possible instead of manual file operations.**

#### Step 3: Update Documentation
- Open `AGENT_DOCUMENTATION.md`
- Update "Current Session Workspace" section:
  - Fill in "Today's Focus"
  - Note any changes in build behavior
  - Record your session start time

### Phase 2: Goal Selection (5 minutes)

#### Choose the SMALLEST Possible Goal
- Look at "Current Priority Goals" in `AGENT_DOCUMENTATION.md`
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
5.  **DOCUMENT EVERYTHING** - Use the change log template.
6.  **USE MCP SERVER** - Always use GitHub MCP functions for Git operations.

### Change Process:

#### Step 1: Pre-Change Documentation
```markdown
## Change [YYYY-MM-DD HH:MM] - [CHANGE_ID]

### Goal
- What specific problem this addresses
- Expected outcome

### Research Conducted (if applicable)
- MCP Tool Used: [e.g., Brave Search MCP]
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
  - Use Brave Search MCP: `brave_web_search({query: "specific error + Android Kotlin 2025", summary: true})` for SOTA fixes.
  - Use Context7 MCP: For Android-specific guidance, e.g., "Kapt configuration for Kotlin test classpath".
  - Cross-reference with frameworks: e.g., KOTLIN_MIGRATION_FRAMEWORK.md for migration-related issues.
  - Document: Summarize findings, sources, and rationale in the pre-change log.
- **Know This Well**: Research is not optional—it's the foundation for informed, error-free changes. Avoid guessing; base fixes on documented best practices.

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

#### Step 6: Commit and Push Changes with Verification
- **Preferred Method**: Use GitHub MCP `push_files` function to commit and push all changes in one operation.
- **Verify Push Success**:
  - After `push_files`, use `git ls-remote origin` to confirm the commit appears on GitHub.
  - Alternatively, check the repository manually (e.g., https://github.com/ElliotBadinger/echo/commits/<branch>) to ensure the commit is visible.
  - If `push_files` fails, fall back to manual git commands (below) and document the failure reason.
- **Handle Merge Conflicts**:
  - If push fails due to conflicts, run `git pull --rebase origin <branch>`, resolve conflicts, then retry `git push origin HEAD`.

```javascript
// Push multiple files with one command
push_files({
  owner: "ElliotBadinger",
  repo: "echo", 
  branch: "refactor/phase1-modularization-kts-hilt",
  message: "Agent Session [YYYY-MM-DD HH:MM UTC]: Fixed [specific issue]",
  files: [
    {path: "build.gradle.kts", content: "..."},
    {path: "AGENT_DOCUMENTATION.md", content: "..."}
  ]
}).then(() => {
  // Verify push success
  executeBash("git ls-remote origin").then(output => {
    if (output.includes("<commit-hash>")) {
      console.log("Push confirmed on GitHub");
    } else {
      console.error("Push failed to appear on GitHub, falling back to manual git");
      // Fallback manual git commands
      executeBash(`
        git add .
        git commit -m "Agent Session [YYYY-MM-DD HH:MM UTC]: Fixed [specific issue]"
        git pull --rebase origin refactor/phase1-modularization-kts-hilt
        git push origin HEAD
      `);
    }
  });
});
```

#### Step 7: Verify GitHub Actions Workflow Alignment
- **Check Existing Workflow**: Read `.github/workflows/android-test.yml` to ensure it runs `./gradlew test` on push/pull_request for the `refactor/phase1-modularization-kts-hilt` branch.
- **Create Workflow if Missing**:
  - If no workflow exists, create `.github/workflows/android-test.yml` with a basic configuration (see example below).
  - Ensure it uses JDK 17, Gradle wrapper, and runs unit tests for the `SaidIt` module.
- **Verify Workflow Requirements**:
  - Runs `./gradlew test` to cover unit tests (including `AudioMemoryTest.kt`).
  - Supports Kotlin migration testing (e.g., verifies `Clock.kt` integration).
  - Publishes test artifacts for debugging (via `actions/upload-artifact`).
  - Triggers on push/pull_request to `refactor/phase1-modularization-kts-hilt`.
- **Monitor Workflow**:
  - After pushing, use `list_workflow_runs({owner: "ElliotBadinger", repo: "echo"})` to check run status.
  - If failed, use `get_job_logs({run_id: <id>, failed_only: true})` and `download_workflow_run_artifact` for test reports.
  - Document CI results in `AGENT_DOCUMENTATION.md`.

**Example Workflow File** (create if missing):
```yaml
name: Android CI
on:
  push:
    branches: [ "refactor/phase1-modularization-kts-hilt" ]
  pull_request:
    branches: [ "refactor/phase1-modularization-kts-hilt" ]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Grant execute permission for gradlew
        run: chmod +x ./gradlew
      - name: Run unit tests
        run: ./gradlew test
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: "**/build/test-results/**/*"
```

#### Fallback Manual Git Commands (Use only if MCP fails):
```bash
# Add all changes
git add .

# Commit with descriptive message
git commit -m "Agent Session [YYYY-MM-DD HH:MM UTC]: [Description of what was fixed/changed]

- Fixed: [specific issue]
- Improved: [specific metric]
- Files: [list main files changed]"

# Verify remote availability
git ls-remote origin

# Push to current branch, handling conflicts
git pull --rebase origin refactor/phase1-modularization-kts-hilt
git push origin HEAD
```

#### Step 8: Document Results
Complete the change log entry:
```markdown
### Result
- ✅ SUCCESS: What worked and why
- ❌ FAILED: What didn't work and why
- 🟡 PARTIAL: What partially worked

### Next Steps
- What should be done next
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
- **`AGENT_DOCUMENTATION.md`** - Your primary reference, current state tracker
- **`AGENT_SESSION_CHECKLIST.md`** - Step-by-step workflow checklist
- **`documentation/echo-critical-fixes.md`** - Known critical issues and fixes
- **`documentation/echo-refactoring-plan.md`** - Long-term architectural plans

### Common Code Locations:
- **`build.gradle.kts`** - Root project configuration
- **`gradle/libs.versions.toml`** - Dependency version catalog
- **`SaidIt/`** - Main application module
- **`features/recorder/`** - Audio recording functionality
- **`domain/`** - Business logic and use cases

### Configuration Files:
- **`gradle.properties`** - Gradle build settings
- **`SaidIt/build.gradle.kts`** - Main app module build config
- **`features/recorder/build.gradle.kts`** - Recorder module build config

---

## 🎯 CURRENT PROJECT STATE REFERENCE

### Build Status (as of last update):
- **Clean Build:** ✅ Works (20-30 seconds)
- **Full Build:** ✅ 100% success rate (compiles, tests run properly)
- **Test Compilation:** ✅ Fixed (was blocking before)
- **Test Execution:** ✅ RecordingViewModelTest passes (other tests may need investigation)

### Major Known Issues:
1. **Other Test Failures** - May need investigation (RecordingViewModelTest is now fixed)
2. **SaidItService** - Threading violations
3. **File Locking** - CI/CD and Windows issues
4. **Architecture** - Needs modularization

### Recently Fixed Issues:
- ✅ Test compilation errors (missing dependencies)
- ✅ RecordingViewModelTest runtime failure (IllegalStateException)
- ✅ ComposeView compilation errors
- ✅ Duplicate SaidItFragment class conflicts
- ✅ Build success rate: 70% → 100%

---
## 🏗️ ARCHITECTURAL CONTEXT FOR AI AGENTS

### The Big Picture Vision (READ ONLY - Don't Attempt)
This project is evolving toward an advanced AI-powered audio application with modern Android architecture:

**Core Product Vision:**
- 24/7 background audio recording with intelligent processing
- Real-time ML features: speech-to-text (Whisper), voice activity detection
- Advanced audio enhancement: RNNoise integration, FFmpeg processing  
- Smart segmentation and classification of audio content
- Clean, intuitive Android UI following expert design principles
- Production-ready testing for all features and migrations

**Technical Architecture Target** (see `documentation/echo-refactoring-plan.md`):
- Modern Clean Architecture with MVVM + Hilt DI + Coroutines
- Modular design: domain/data/presentation layers
- Kotlin-first with coroutines for all async operations
- Advanced audio pipeline with ML integration
- Expert Android UI/UX design with Material You principles
- Timeline: 8-week phased refactoring plan
- Current Phase: Phase 1 - Foundation & stabilization

### CRITICAL: Incremental Development Principles
1. **NEVER break existing functionality** - Always maintain current features
2. **ALWAYS fix errors first** - Build/test/integration failures take absolute priority
3. **ALWAYS add comprehensive tests** - Every migration/feature needs production-ready testing
4. **Make smallest possible changes** - Incremental is always better than big changes

### Decision Framework for Small Changes
When choosing between multiple small fixes, prefer changes that align with the vision:

1. **Fix errors FIRST** - Build, unit, integration, functional test failures
2. **Use Research Frameworks for Technical Decisions**:
   - **RESEARCH_FRAMEWORK.md** - Overall research-driven development methodology
   - **ML_STRATEGY_FRAMEWORK.md** - ML research and implementation strategy  
   - **PERFORMANCE_RESEARCH_FRAMEWORK.md** - Performance optimization research
   - **UI_UX_ENHANCEMENT_FRAMEWORK.md** - Professional UI development framework
   - **KOTLIN_MIGRATION_FRAMEWORK.md** - Java-to-Kotlin conversion methodology
3. **Prioritize foundation work** - Kotlin migration before UI enhancement
4. **Make incremental improvements** - Small, well-tested changes that compound
2. **Kotlin over Java** - Convert utilities when fixing them (but don't break existing)
3. **Coroutines over Threads** - Use suspend functions for async fixes  
4. **Result types over exceptions** - Wrap operations in Result<T>
5. **Comprehensive testing** - Add tests for any code you touch
6. **UI/UX improvements** - Modern Material You patterns for frontend changes
7. **Separation of concerns** - Keep UI, business logic, and data separate

### Change Quality Assessment
When documenting changes, note alignment and testing:
- ✅ **FIXES-ERRORS**: Resolves build/test/integration failures
- ✅ **WELL-TESTED**: Includes comprehensive tests for production readiness
- ✅ **ARCHITECTURAL**: Moves toward Clean Architecture (incrementally)
- ✅ **MODERN**: Uses modern Android patterns (coroutines, Material You, etc.)
- ✅ **UI-EXPERT**: Follows expert Android design principles
- ⚠️ **LEGACY**: Fixes issue but maintains old patterns (acceptable if well-tested)

### Example: Priority-Ordered Fixes
```
PRIORITY 1 - ERROR FIXES (Always first):
✅ "Fix failing RecordingViewModelTest with proper test setup"
✅ "Resolve build compilation errors in audio module"
✅ "Fix integration test timeouts with proper resource cleanup"

PRIORITY 2 - TESTED IMPROVEMENTS:  
✅ "Convert AudioManager to Kotlin with comprehensive unit tests"
✅ "Add Result<T> to file operations with integration tests"
✅ "Improve UI button accessibility with UI tests"

PRIORITY 3 - ARCHITECTURAL (Only if no errors):
✅ "Extract repository pattern with full test coverage" 
✅ "Add Hilt DI to single module with integration tests"
```

### Frontend Innovation Guidelines
When touching UI code, follow expert Android design:
- **Material You** design system and dynamic theming
- **Accessibility first** - proper content descriptions, navigation
- **Intuitive navigation** - clear user flows and interaction patterns  
- **Performance** - smooth animations, responsive interactions
- **Clean visual hierarchy** - proper spacing, typography, colors
- **User testing** - UI tests for all interactive elements

## 🔬 RESEARCH & CI INTEGRATION TOOLS

### Available MCP Server Integration
When implementing features or optimizing performance, use available MCP servers for enhanced capabilities:

#### GitHub MCP Server (Already Available)
**Use for**: CI/CD workflow monitoring and artifact analysis
- Monitor GitHub Actions workflow runs and build status
- Download and analyze test artifacts and logs  
- Manage workflow runs (cancel, rerun, get usage statistics)
- Access job logs and failure details for debugging

**Confirmed Available Functions:**
```javascript
// Check workflow status and runs
list_workflow_runs({
  owner: "ElliotBadinger",
  repo: "echo",
  workflow_id: "android-ci.yml"
})

// Get specific workflow run details
get_workflow_run({
  owner: "ElliotBadinger", 
  repo: "echo",
  run_id: 12345
})

// Download test artifacts for analysis
download_workflow_run_artifact({
  owner: "ElliotBadinger",
  repo: "echo", 
  artifact_id: 67890
})

// Get job logs for debugging failures
get_job_logs({
  owner: "ElliotBadinger",
  repo: "echo",
  run_id: 12345,
  failed_only: true,  // Get logs for failed jobs only
  return_content: true  // Return actual log content
})
```

#### Brave Search MCP Server (If Installed: @modelcontextprotocol/server-brave-search)
**Use for**: Scientific research and state-of-the-art algorithm discovery
- Research latest academic papers and scholarly articles
- Find cutting-edge audio processing algorithms and techniques
- Discover mobile ML optimization patterns and performance studies
- Search for Android development best practices and case studies
- Privacy-focused search with no tracking (superior to Google for research)

**Installation Requirements:**
- Brave Search API key (free tier: 2,000 queries/month)
- Add to MCP configuration: `"@modelcontextprotocol/server-brave-search"`

**Research Capabilities:**
- **Web Search**: General queries with freshness and pagination controls
- **Local Search**: Find businesses and services (useful for UX research)
- **Academic Focus**: Better for finding research papers and technical documentation
- **Privacy**: No tracking or data collection during research

**Example Research Queries:**
```javascript
// Research SOTA audio processing for mobile
brave_search({
  query: "state-of-the-art real-time audio processing android mobile 2024 scholarly"
})

// Find performance optimization techniques
brave_search({
  query: "android jetpack compose performance optimization case studies 2024"
})

// Research ML model optimization for mobile devices
brave_search({
  query: "on-device machine learning optimization mobile android research papers"
})

// Find UI/UX research and design patterns
brave_search({
  query: "mobile audio app interface usability research user experience studies"
})
```

#### Context7 MCP Server (If Installed: github.com/upstash/context7)
**Use for**: Fetching current Android development documentation
- Get up-to-date Android API documentation and examples
- Access current Kotlin, Jetpack Compose, and architecture guides
- Fetch specific library documentation (Hilt, Room, Coroutines)

**Usage Pattern:**
```javascript
// First resolve the library you need
resolve_library_id({
  libraryName: "Android Jetpack Compose"  
})

// Then fetch specific documentation  
get_library_docs({
  context7CompatibleLibraryID: "/android/compose",
  topic: "performance optimization",
  tokens: 15000
})
```

**Or simply add "use context7" to your prompts when asking about Android development patterns.**

#### Web Search Tool (Built-in Fallback)
**Use when Brave Search MCP is not available**
- Basic web search functionality
- Less specialized for academic/research content
- No privacy guarantees like Brave Search

### Research Tool Selection Matrix

**Use Brave Search MCP When:**
- ✅ Researching SOTA algorithms and scientific papers
- ✅ Finding performance optimization research
- ✅ Looking for academic studies on mobile ML/audio
- ✅ Privacy-focused research without tracking
- ✅ Need freshness controls and pagination

**Use Context7 MCP When:**
- ✅ Need current API documentation and examples
- ✅ Want library-specific implementation guidance
- ✅ Looking for official Android development patterns
- ✅ Need code examples and best practices

**Use Built-in Web Search When:**
- ⚠️ Neither Brave nor Context7 MCP is available
- ⚠️ Basic information gathering only

### Research-Enhanced Development Process (UPDATED)

#### MCP Server Integration Status:
**✅ CONFIGURED - Brave Search MCP:**
- Academic paper discovery and technical documentation search
- AI-powered summaries for quick research evaluation  
- Free tier: 2,000 queries/month with comprehensive search capabilities
- Usage: `brave_web_search({query: "...", summary: true})`

**✅ CONFIGURED - Playwright MCP:**
- Complete content extraction from research papers and documentation
- PDF processing capabilities for academic papers
- Full page content extraction with structured accessibility snapshots
- Usage: `browser_navigate()` → `browser_snapshot()` → `browser_pdf_save()`

**✅ CONFIGURED - GitHub MCP:**
- CI/CD workflow automation and performance validation
- Comprehensive testing and artifact analysis
- Usage: `list_workflow_runs()` → `get_job_logs()` → `download_workflow_run_artifact()`

#### Complete Research Workflow:

**Phase 1: Discovery (Brave Search MCP)**
```javascript
// Academic research discovery
brave_web_search({
  query: "WebRTC VAD mobile optimization android 2024 research papers",
  result_filter: ["web"],
  summary: true,  // AI summary for quick evaluation
  freshness: "py"  // Recent research priority
})

// Implementation documentation search  
brave_web_search({
  query: "android AudioRecord real-time performance optimization",
  count: 10,
  summary: true
})
```

**Phase 2: Deep Extraction (Playwright MCP)**
```javascript
// Navigate to research papers found in Phase 1
browser_navigate({url: "https://arxiv.org/abs/2024.audio-research"})
browser_snapshot()  // Structured content extraction

// Extract complete PDF research papers
browser_navigate({url: "https://research-paper.pdf"})
browser_pdf_save({filename: "audio-optimization-research.pdf"})

// Get complete implementation documentation
browser_navigate({url: "https://developer.android.com/audio-guide"})
browser_snapshot()  // Full Android documentation
```

**Phase 3: Application & Validation (GitHub MCP)**
```javascript
// Apply research findings to Echo codebase
// Monitor CI performance for research-predicted improvements
list_workflow_runs({workflow_id: "android-ci.yml"})

// Analyze performance data against research benchmarks
get_job_logs({run_id: 12345, failed_only: false})
download_workflow_run_artifact({artifact_id: 67890})
```

#### Research Quality Assurance:
**Source Credibility Assessment:**
- ✅ **Academic papers**: arXiv, IEEE, ACM (highest credibility)
- ✅ **Official documentation**: developer.android.com, official library docs
- ✅ **Industry research**: Google Research, Microsoft Research
- ⚠️ **Blog posts**: Cross-reference with authoritative sources
- ❌ **Stack Overflow answers**: Use for debugging only, not architecture

**Research-to-Implementation Validation:**
- [ ] Research findings include quantified benchmarks
- [ ] Implementation patterns verified across multiple sources  
- [ ] Performance predictions testable with GitHub MCP CI
- [ ] Mobile constraints and Android compatibility confirmed
```

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

### Signs You're Doing Too Much:
- You're modifying more than 3 files
- Your change affects multiple modules
- You're adding new architectural patterns
- The change takes more than 30 minutes to implement
- You can't easily explain exactly what you changed

---

**"Protocol for Intractable Tier 1 Errors"**

> If a Tier 1 error (build failure, test failure) cannot be resolved after a reasonable number of attempts (e.g., 3-5 distinct strategies), the agent must not bypass the error. Instead, it must switch to a **Deep Diagnostic Mode**.
>
> **Deep Diagnostic Mode Checklist:**
>
> 1.  **Halt Feature Progress:** Stop all work on the current feature (e.g., "Kotlin Conversion of Clock.java"). The goal now is to solve the Tier 1 blocker itself.
> 2.  **Isolate the Problem:** Create the smallest possible reproducible example. For the `Clock` issue, this would mean creating a new, single, extremely simple test file (`SimpleTest.kt`) and trying to run just that.
> 3.  **Investigate the Build System:** The problem is likely not in the source code, but in the build configuration. The agent must investigate:
>     *   `SaidIt/build.gradle.kts`: Are the `sourceSets` configured correctly to include Kotlin files in the test directories?
>     *   `gradle/libs.versions.toml`: Are there conflicting test dependency versions?
>     *   `gradle.properties`: Are there any flags affecting compilation or caching?
> 4.  **Force a True Clean State:** The agent tried a clean build, but it saw `FROM-CACHE`. The protocol should be more aggressive:
>     *   Run `./gradlew clean`.
>     *   Attempt to clear the Gradle cache if possible (e.g., by deleting `.gradle/caches`).
>     *   Re-run the build with `--no-build-cache`.
> 5.  **Document the Blocker:** If the issue persists, the final step is to:
>     *   Revert all code changes related to the feature.
>     *   Create a **new Change Log entry** specifically documenting the Tier 1 blocker. Title it: `BLOCKER - [Description of issue]`.
>     *   In the "Next Agent Should Focus On" section, make resolving this blocker the **only priority**.

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

### Good Progress Indicators:
- Build success rate improves
- Fewer compilation errors
- Tests that were failing now pass
- Clear path forward for next change

---

## 📝 DOCUMENTATION REQUIREMENTS

### ALWAYS Update These Files:

#### AGENT_DOCUMENTATION.md:
- Add entry to "Current Active Changes" section
- Update "Current Status" if project state changed
- Update "Current Session Workspace" section
- Move completed goals from priority list

#### Change Log Entry Template:
```markdown
## Change [YYYY-MM-DD HH:MM UTC] - [ID]  // Use get_current_time MCP function

### Goal
[What you were trying to accomplish]

### Files Modified
- file1.kt - [what changed]
- file2.gradle - [what changed]

### Testing Done
- [Commands run and results]

### Result
- ✅/❌/🟡 [Detailed outcome]

### Next Steps
- [What should happen next]

### Rollback Info
- [How to undo this change]
```

---

## 🔄 GIT WORKFLOW & PUBLISHING CHANGES

### ⚠️ CRITICAL GITHUB MCP SYNCHRONIZATION WARNING

**🚨 MAJOR ISSUE: GitHub MCP tools create synchronization conflicts with local git!**

#### The Problem:
- GitHub MCP functions (`push_files`, `create_or_update_file`) create commits **directly on GitHub**
- These commits bypass the user's local git repository
- When user tries to push local changes, git rejects with "fetch first" errors
- This creates complex merge conflicts and workflow disruption

#### MANDATORY WORKFLOW RULES:

**❌ NEVER USE GitHub MCP functions for routine commits:**
- ❌ `push_files()` - Creates commits directly on GitHub, bypassing local git
- ❌ `create_or_update_file()` - Same synchronization issue
- ❌ Any MCP function that creates commits

**✅ ONLY USE GitHub MCP for read-only operations:**
- ✅ `list_workflow_runs()` - Monitor CI status
- ✅ `get_job_logs()` - Debug CI failures
- ✅ `download_workflow_run_artifact()` - Analyze test results
- ✅ `get_file_contents()` - Read repository files
- ✅ `get_pull_request()` - Check PR status

**✅ ALWAYS USE manual git commands for commits:**
```bash
# The ONLY safe way to commit changes
git add .
git commit -m "Agent Session [DATE]: Description"
git push origin HEAD
```

#### Synchronization Conflict Symptoms:
- User gets "error: failed to push some refs" when trying to push
- Git says "Updates were rejected because the remote contains work"
- User must run complex merge procedures to sync local and remote
- Files appear as "untracked" locally but exist in remote commits

#### Emergency Resolution (If Conflict Occurs):
```bash
# 1. Stash local changes
git stash push -m "Local changes before agent sync"

# 2. Remove conflicting untracked files (ONLY if confirmed they exist in remote)
git ls-tree -r origin/branch-name | grep filename  # Verify file exists remotely
rm -rf path/to/conflicting/files  # ONLY if confirmed above

# 3. Pull remote changes
git pull

# 4. Restore and merge local changes
git stash pop
# Resolve any conflicts manually
git add .
git commit -m "Merge local changes with agent commits"
git push origin HEAD
```

### GitHub MCP Server - RESTRICTED USAGE

**🚨 CRITICAL: GitHub MCP server available but RESTRICTED to read-only operations only!**

#### Safe GitHub MCP Functions (Read-Only):
- `list_workflow_runs` - Monitor CI status
- `get_workflow_run` - Check specific run details
- `get_job_logs` - Debug CI failures
- `download_workflow_run_artifact` - Analyze test results
- `get_file_contents` - Read files from repository
- `get_pull_request` - Check PR status
- `list_issues` - View project issues

#### FORBIDDEN GitHub MCP Functions (Create Sync Issues):
- ❌ `push_files` - **NEVER USE** - Creates commits directly on GitHub
- ❌ `create_or_update_file` - **NEVER USE** - Bypasses local git
- ❌ `create_pull_request` - Use only if explicitly requested by user
- ❌ `merge_pull_request` - Use only if explicitly requested by user
- ❌ `create_branch` - Use manual git commands instead

#### Example Safe Usage:
```javascript
// ✅ SAFE: Monitor CI status
list_workflow_runs({
  owner: "ElliotBadinger",
  repo: "echo",
  workflow_id: "android-ci.yml"
})

// ✅ SAFE: Debug CI failures
get_job_logs({
  owner: "ElliotBadinger",
  repo: "echo",
  run_id: 12345,
  failed_only: true
})

// ❌ FORBIDDEN: Creates sync conflicts
// push_files({...})  // NEVER USE THIS
```

### Traditional Git Commands (Use only if MCP unavailable):

#### Check Current State:
```bash
git status                  # See what's changed
git diff                   # See specific changes
git log --oneline -5       # See recent commits
```

#### Stage and Commit Changes:
```bash
# Add all changes
git add .

# Commit with descriptive message
git commit -m "Agent Session [DATE]: [Description of what was fixed/changed]

- Fixed: [specific issue]
- Improved: [specific metric]
- Files: [list main files changed]"
```

#### Push Changes:
```bash
# Push to current branch
git push origin HEAD

# Or push to specific branch
git push origin main
# or
git push origin development
```

### Commit Message Templates:

#### For Successful Fixes:
```
Agent Session 2025-01-15: Fixed RecordingViewModelTest compilation

- Fixed: Added missing mockito-kotlin and coroutines-test dependencies
- Improved: Build success rate from 40% to 100%  
- Files: gradle/libs.versions.toml, features/recorder/build.gradle.kts
- Next: Focus on improving test coverage and runtime stability
```

#### For Partial Progress:
```
Agent Session 2025-01-15: Partial fix for threading issues

- Progress: Identified SaidItService threading violations
- Added: Proper synchronization to 3 methods
- Status: 2 more methods need fixing
- Files: SaidItService.java
- Next: Complete remaining method synchronization
```

#### For Investigation/Documentation:
```
Agent Session 2025-01-15: Documented current build issues

- Analyzed: All current test failures and root causes
- Documented: 5 specific issues in AGENT_DOCUMENTATION.md
- Prioritized: Next 3 changes needed for stability
- Files: AGENT_DOCUMENTATION.md, test reports analysis
```

---

## 🎯 SESSION HANDOFF PROCESS

### Before Ending Your Session:

#### Update Documentation:
1. Complete your change log entry in `AGENT_DOCUMENTATION.md`
2. Update "Next Agent Should Focus On" section with:
   - Specific next task
   - Files that need attention
   - Expected challenges
   - Success criteria

#### Clean Up:
1. Ensure all changes are committed and pushed using MCP functions
2. Verify no uncommitted changes remain (`git status`)
3. Test that your changes don't break basic functionality

#### Handoff Template:
```markdown
### Next Agent Should Focus On
Fix [specific issue] in [specific file]:
- Problem: [exact error or issue]
- Location: [file and line numbers if known]  
- Expected Solution: [what type of fix is needed]
- Success Criteria: [how to know it's fixed]
- Complexity: [estimate: simple/medium/complex]

Avoid: [things that didn't work or should be avoided]
```

---

## 🔍 TROUBLESHOOTING GUIDE

### When Builds Fail:

1. **Read the error carefully** - Don't skim
2. **Identify the specific cause** - compilation vs runtime vs test
3. **Check recent changes** - what did you change that might cause this?
4. **Isolate the problem** - run smaller parts of the build
5. **Fix the immediate cause** - not the root architectural issue

### When Tests Fail:

1. **Run just the failing test** - `./gradlew :module:test --tests SpecificTestName`
2. **Check the full stack trace** - not just the summary
3. **Verify test dependencies** - are all required libraries present?
4. **Check mocking setup** - are mocks configured correctly?
5. **Test in isolation** - comment out other tests if needed

### When Git Push Fails:

```bash
# If push is rejected, first pull latest changes
git pull origin main

# If there are conflicts, resolve them manually
git status                 # See conflicted files
# Edit files to resolve conflicts
git add .
git commit -m "Resolve merge conflicts"
git push origin HEAD
```

---

## 📊 SUCCESS METRICS

### Session Success Levels:

#### Minimum Success (Every session should achieve):
- [ ] Read all required documentation
- [ ] Understood current project state
- [ ] Made at least one small improvement
- [ ] Documented changes properly
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

### Red Flags (Stop and Reassess):
- 🚩 You've been working for 2+ hours on the same issue
- 🚩 You've modified 5+ files for "one" change
- 🚩 The build is in worse state than when you started
- 🚩 You can't clearly explain what you changed and why
- 🚩 You're tempted to "just try one more big change"

---

## 🎪 EXAMPLE SESSION WALKTHROUGH

### Scenario: Fix test compilation issue

#### Step 1: Session Start
```
1. Read AGENT_DOCUMENTATION.md - see that tests don't compile
2. Read priority: "Fix RecordingViewModelTest compilation"
3. Run ./gradlew clean - SUCCESS
4. Run ./gradlew build - FAILS on test compilation
5. Goal: Get tests to compile successfully
```

#### Step 2: Investigation
```
Error message: "Unresolved reference: runTest"
File: RecordingViewModelTest.kt
Issue: Missing kotlinx.coroutines.test dependency
```

#### Step 3: Plan
```
Change: Add coroutines-test dependency
Files: gradle/libs.versions.toml, features/recorder/build.gradle.kts
Test: ./gradlew :features:recorder:compileDebugUnitTestKotlin
Success: Compilation passes without errors
```

#### Step 4: Execute
```
1. Add coroutines-test = "1.8.1" to libs.versions.toml
2. Add testImplementation(libs.coroutines.test) to build.gradle.kts
3. Test: ./gradlew :features:recorder:compileDebugUnitTestKotlin - SUCCESS!
```

#### Step 5: Document & Commit
```
Update AGENT_DOCUMENTATION.md with change log entry
Use push_files() MCP function to commit and push all changes
Update "Next Agent Should Focus On": Fix runtime test failures
```

---

## 🔚 FINAL REMINDERS

### Every Single Session:
1. **Read the documentation first** - No exceptions
2. **Make small changes** - Resist the urge to do more
3. **Test immediately** - After every change
4. **Document everything** - Your future self will thank you
5. **Use MCP server** - Don't use manual git commands
6. **Push changes** - Don't leave work uncommitted

### When in Doubt:
- Choose the smaller change
- Fix one specific error message
- Ask for clarification rather than guessing
- Focus on immediate problems, not architectural ones
- Follow the checklist in `AGENT_SESSION_CHECKLIST.md`

### Remember:
**The goal is not to solve everything in one session. The goal is to make consistent, verifiable progress that builds up over time.**

---

**🎯 Quick Start Summary:**
1. Read `AGENT_DOCUMENTATION.md` completely
2. Check current build status: `./gradlew clean && ./gradlew build`
3. Pick the smallest next goal from the priority list
4. Make one small change and test it immediately
5. Document the change and use MCP server to push
6. Update handoff instructions for next session

**Good luck and happy coding! 🚀**
