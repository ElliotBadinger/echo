# Complete AI Agent Workflow Guide for Echo Android Project

**Version:** 1.0  
**Target Audience:** Claude, ChatGPT, and other AI development assistants  
**Project:** Echo Android Application  
**Last Updated:** 2025-01-15

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

1. **ONE CHANGE AT A TIME** - Never modify multiple unrelated things
2. **TEST IMMEDIATELY** - After each change, verify it works
3. **ROLLBACK ON FAILURE** - If something breaks, undo immediately
4. **DOCUMENT EVERYTHING** - Use the change log template
5. **USE MCP SERVER** - Always use GitHub MCP functions for Git operations

### Change Process:

#### Step 1: Pre-Change Documentation
```markdown
## Change [YYYY-MM-DD HH:MM] - [CHANGE_ID]

### Goal
- What specific problem this addresses
- Expected outcome

### Files Modified
- [List files you plan to change]

### Testing Plan
- How you'll verify this works
```

#### Step 2: Make ONE Small Change
- Edit one file or fix one specific issue
- Keep the change as minimal as possible
- Do NOT make multiple changes at once

#### Step 3: Test Immediately
```bash
# For build changes:
./gradlew clean build

# For specific module changes:
./gradlew :module-name:compileDebugUnitTestKotlin

# For test fixes:
./gradlew :module-name:test
```

#### Step 4: Document Results
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

### The Big Picture (READ ONLY - Don't Attempt)
This project is gradually moving toward Clean Architecture (see `documentation/echo-refactoring-plan.md`):
- **Target**: Modern Clean Architecture with MVVM + Hilt DI + Coroutines
- **Timeline**: 8-week phased refactoring plan 
- **Current Phase**: Phase 1 - Foundation & stabilization
- **Your Role**: Contribute through tiny incremental improvements only

### Decision Framework for Small Changes
When choosing between multiple small fixes, prefer:
1. **Kotlin over Java** - Convert utilities when fixing them
2. **Coroutines over Threads** - Use suspend functions for async fixes
3. **Result types over exceptions** - Wrap operations in Result<T>
4. **Dependency injection patterns** - Avoid manual instantiation
5. **Separation of concerns** - Keep UI, business logic, and data separate

### Change Quality Assessment
Document architectural alignment:
- ✅ **ARCHITECTURAL**: Moves toward target architecture
- ✅ **MODERN**: Uses modern Android patterns  
- ✅ **TESTABLE**: Improves testability
- ⚠️ **LEGACY**: Fixes issue but maintains old patterns

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
## Change [YYYY-MM-DD HH:MM] - [ID]

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

### GitHub MCP Server - IMPORTANT FOR ALL AGENTS

**🚨 CRITICAL: This project has GitHub MCP server available! Use it instead of manual git commands.**

#### Available GitHub MCP Functions:
- `push_files` - Push multiple files in a single commit (PREFERRED)
- `create_or_update_file` - Create/update single files
- `get_file_contents` - Read files from repository
- `create_pull_request` - Create PRs
- `merge_pull_request` - Merge PRs
- `create_branch` - Create new branches
- And many more GitHub API functions

#### Recommended Workflow:
```
1. Make local changes using edit_file, delete_path, etc.
2. Use push_files() to commit and push all changes at once
3. Never use manual git commands when MCP server is available
```

#### Example Usage:
```javascript
// Push multiple files with one command
push_files({
  owner: "ElliotBadinger",
  repo: "echo", 
  branch: "refactor/phase1-modularization-kts-hilt",
  message: "Agent Session: Fixed build issues",
  files: [
    {path: "build.gradle.kts", content: "..."},
    {path: "README.md", content: "..."}
  ]
})
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
