# Echo - Never Miss a Moment

Echo is a modern Android application that continuously records audio in the background, allowing you to go back in time and save moments that have already happened. Whether it's a brilliant idea, a funny quote, or an important note, Echo ensures you never miss it.

## 🚀 Quick Start for Users

### What Echo Does
- **Continuous Background Recording:** Records audio silently in the background with a rolling buffer
- **Time-Travel Audio:** Save clips from moments that already happened
- **Auto-Save:** Never lose important audio when memory buffer fills
- **Modern Interface:** Clean Material You design
- **Customizable:** Adjust audio quality and memory usage

### Installation & Usage
1. Install the app on your Android device (minimum API 30)
2. Grant audio recording permissions when prompted
3. The app starts recording automatically in the background
4. Use the interface to save clips from recent audio
5. Access saved recordings through the app's library

## 🛠️ Development Setup

### Prerequisites
- **Android Studio** (Latest stable version recommended)
- **JDK 17** (Required for this project)
- **Android SDK** with API level 30+
- **Git** for version control

### Building the Project
```bash
# Clone the repository
git clone https://github.com/your-username/echo.git
cd echo

# Create local.properties file (replace with your SDK path)
echo "sdk.dir=/path/to/your/android/sdk" > local.properties

# Build the project
./gradlew clean build

# Install on connected device
./gradlew installDebug
```

### Project Status
- **Build Status:** 🟢 Fully working (100% success rate - full build completes successfully)
- **Architecture:** Currently undergoing refactoring from monolithic to modular design
- **Active Development:** Focus on improving test coverage and runtime stability
- **Recent Success:** RecordingViewModelTest fixed, compilation errors resolved

## 🤖 AI Agent Development Workflow

**IMPORTANT:** This project uses AI agents for development assistance and has GitHub MCP server integration. If you're working with Claude or other AI assistants, follow this workflow for best results.

### 📋 **FIRST TIME OR NEW CONVERSATION: Read This Guide**
👉 **[Complete Agent Workflow Guide](AGENT_WORKFLOW_GUIDE.md)** - Essential reading for AI-assisted development

### 🎯 **Quick Reference for AI Agents**

#### Essential Files to Read First (In Order):
1. **`AGENT_DOCUMENTATION.md`** - Current project state, issues, and change tracking
2. **`AGENT_SESSION_CHECKLIST.md`** - Step-by-step session workflow
3. **`RESEARCH_FRAMEWORK.md`** - Research-driven development methodology
4. **`ML_STRATEGY_FRAMEWORK.md`** - ML research and implementation strategy
5. **`PERFORMANCE_RESEARCH_FRAMEWORK.md`** - Performance optimization research
6. **`UI_UX_ENHANCEMENT_FRAMEWORK.md`** - Professional UI development framework
7. **`KOTLIN_MIGRATION_FRAMEWORK.md`** - Java-to-Kotlin conversion methodology
8. **`documentation/echo-critical-fixes.md`** - Critical build and infrastructure issues
9. **`documentation/echo-refactoring-plan.md`** - Long-term architectural roadmap

#### **COPY THIS PROMPT for New AI Sessions:**
```
I'm continuing work on the Echo Android project. Please:

1. Read the AGENT_WORKFLOW_GUIDE.md file completely - this contains all essential instructions including research tools and CI integration
2. Follow the AGENT_SESSION_CHECKLIST.md for proper workflow with error-first prioritization  
3. Read AGENT_DOCUMENTATION.md to understand current project state and research-enhanced development process
4. Check current build status with: cd echo && ./gradlew clean
5. Focus on ERROR FIXES FIRST - any failing tests, build errors, or crashes take absolute priority
6. Use available MCP servers for enhanced development:
   - GitHub MCP (available): CI/CD workflow monitoring, artifact analysis
   - Brave Search MCP (if installed): SOTA algorithm research, scientific papers  
   - Context7 MCP (if installed): Current Android API documentation
7. Use research frameworks for technical decisions:
   - RESEARCH_FRAMEWORK.md for overall methodology
   - ML_STRATEGY_FRAMEWORK.md for ML/AI decisions
   - PERFORMANCE_RESEARCH_FRAMEWORK.md for optimization
   - UI_UX_ENHANCEMENT_FRAMEWORK.md for UI work
   - KOTLIN_MIGRATION_FRAMEWORK.md for Java conversion
7. Use GitHub MCP server functions (push_files, list_workflow_runs, get_job_logs) for all Git operations and CI monitoring
8. Never attempt large architectural changes - focus on small, well-tested, incremental improvements
9. Every change must include comprehensive testing (unit, integration, error handling, performance)
10. For UI changes, follow expert Android design principles with Material You and accessibility

Current status: Build success rate is 100%, RecordingViewModelTest fixed. 

PRIORITY SYSTEM:
- TIER 1: Fix any failing tests, build errors, crashes (ABSOLUTE PRIORITY)
- TIER 2: Research-informed incremental improvements with comprehensive testing
- TIER 3: Architecture enhancements only if Tiers 1-2 complete

RESEARCH WORKFLOW:
- Use Brave Search MCP for SOTA algorithms and scientific research
- Use Context7 MCP for Android API documentation and implementation guidance  
- Use GitHub Actions CI for faster testing and comprehensive validation
- Document all research findings and their application to implementation

Follow the incremental change methodology and comprehensive testing requirements documented in the guides.

The current TIER 1 priority is resolving the ClassNotFoundException in AudioMemoryTest.kt (Clock class missing from test classpath). Local testing hasn't resolved it, so shift to GitHub Actions CI for validation in a clean environment. 
- If no workflow exists, create a basic .github/workflows/android-test.yml file to run './gradlew test' on push/pull_request.
- Use push_files to commit and push the workflow file (or any code fixes).
- Trigger a workflow run by pushing a small change.
- Monitor with list_workflow_runs and get_job_logs (focus on failed_only: true).
- Download artifacts if needed with download_workflow_run_artifact for detailed test reports.
- Do not proceed until CI tests pass or the failure is analyzed/documented in AGENT_DOCUMENTATION.md.
- If CI passes but local fails, document it as a potential local environment issue.

When encountering errors or issues (especially TIER 1 build/test failures), always research fixes using available MCP tools and frameworks before attempting changes. Use Brave Search MCP for state-of-the-art (SOTA) solutions and scientific papers, Context7 MCP for Android API documentation, and integrate findings from RESEARCH_FRAMEWORK.md or specific frameworks like KOTLIN_MIGRATION_FRAMEWORK.md. Document all research in AGENT_DOCUMENTATION.md, including sources and how they apply to the fix. Examples: For ClassNotFoundException, search "Android Kotlin Kapt stub conflicts 2025" via Brave Search MCP; for test classpath issues, query Context7 MCP for "Android Gradle test classpath configuration best practices".
```


#### **COPY THIS for Completed Feature/Fix Sessions:**
```
I just completed [describe what you implemented/fixed] on the Echo Android project. Please:

1. Read AGENT_DOCUMENTATION.md to understand current state and recent changes with research integration
2. Verify the completed work is properly documented in the change log with research findings
3. Check if there are any follow-up issues or improvements needed using available MCP tools
4. Use GitHub MCP server to:
   - Push any documentation updates or final fixes using push_files
   - Check CI status with list_workflow_runs  
   - Analyze any test failures with get_job_logs and download_workflow_run_artifact
5. If needed, conduct research using:
   - Brave Search MCP for SOTA techniques and performance optimization
   - Context7 MCP for Android API best practices and implementation patterns
6. Identify and start the next highest priority goal from the Error-First Priority System
7. Update the "Next Agent Should Focus On" section with clear next steps and research directions

Current focus should be: [check documentation for current priorities - always ERROR FIXES FIRST]

REMEMBER: 
- Always prioritize failing tests, build errors, and crashes before any other work
- Every change needs comprehensive testing (unit, integration, error, performance, UI)
- Use research tools to make informed decisions about optimizations and implementations
- Leverage GitHub Actions CI for faster, more comprehensive testing
- Make incremental improvements that don't break existing functionality
```

### 🔄 **Git Workflow for Changes**

#### **🤖 IMPORTANT FOR AI AGENTS: Use GitHub MCP Server Functions**

**✅ PREFERRED METHOD - Use MCP Functions:**
```javascript
// Push multiple files at once (RECOMMENDED)
push_files({
  owner: "ElliotBadinger",
  repo: "echo",
  branch: "refactor/phase1-modularization-kts-hilt", // or current branch
  message: "Agent Session [DATE]: Fixed [specific issue] - description",
  files: [
    {path: "file1.kt", content: "..."},
    {path: "AGENT_DOCUMENTATION.md", content: "...updated docs..."}
  ]
})

// Or for single files
create_or_update_file({
  owner: "ElliotBadinger", 
  repo: "echo",
  path: "path/to/file.kt",
  content: "file content",
  message: "Agent: Fixed specific issue",
  branch: "refactor/phase1-modularization-kts-hilt"
})
```

#### **🔧 FALLBACK: Manual Git (only if MCP unavailable):**
```bash
# Add all changes
git add .
# Commit with descriptive message
git commit -m "Agent Session [DATE]: Fixed [specific issue] - improved [metric]"
# Push to current branch  
git push origin HEAD
```

## 📁 Project Structure

```
echo/
├── SaidIt/                          # Main app module
├── domain/                          # Domain layer (business logic)
├── features/recorder/               # Recording feature module
├── documentation/                   # Critical fixes and refactoring plans
├── AGENT_DOCUMENTATION.md           # 🤖 AGENT STATE TRACKING
├── AGENT_SESSION_CHECKLIST.md       # 🤖 AGENT WORKFLOW GUIDE
├── AGENT_WORKFLOW_GUIDE.md          # 🤖 COMPLETE AGENT INSTRUCTIONS
└── build.gradle.kts                 # Project build configuration
```

## 🚨 Current Known Issues

### Build & Test Issues ✅ MAJOR IMPROVEMENTS MADE
- ✅ **Build system now 100% functional** (was 70% success rate)
- ✅ **RecordingViewModelTest fixed** (was failing with IllegalStateException)
- 🟡 **Some other unit tests may still need investigation**
- 🟡 **Threading violations in SaidItService** (next priority)
- 🟡 **File locking issues in CI/CD**
- 🟡 **Gradle warnings about deprecated APIs**

### Architecture Issues  
- Monolithic service design needs refactoring
- Tight coupling between components
- Missing proper dependency injection
- Incomplete Jetpack Compose integration

**📖 See `documentation/echo-critical-fixes.md` for detailed fixes**

## 🎯 Development Principles

### For Human Developers:
- Follow Android development best practices
- Use MVVM architecture pattern
- Implement proper testing at all levels
- Follow Material Design guidelines

### For AI Agents:
- **Use GitHub MCP server functions** for all Git operations (push_files, create_or_update_file, etc.)
- **Small incremental changes only** - never attempt large architectural changes  
- **Document every change** in AGENT_DOCUMENTATION.md with proper change log entries
- **Test immediately** after each change using ./gradlew commands
- **Never assume context** from previous conversations - always read current documentation
- **Follow the complete workflow** in AGENT_WORKFLOW_GUIDE.md and AGENT_SESSION_CHECKLIST.md

## 📚 Additional Resources

- **[Agent Workflow Guide](AGENT_WORKFLOW_GUIDE.md)** - Complete guide for AI-assisted development
- **[Research Framework](RESEARCH_FRAMEWORK.md)** - Research-driven development methodology
- **[ML Strategy Framework](ML_STRATEGY_FRAMEWORK.md)** - ML research and implementation
- **[Performance Research Framework](PERFORMANCE_RESEARCH_FRAMEWORK.md)** - Performance optimization
- **[UI/UX Enhancement Framework](UI_UX_ENHANCEMENT_FRAMEWORK.md)** - Professional UI development
- **[Kotlin Migration Framework](KOTLIN_MIGRATION_FRAMEWORK.md)** - Java-to-Kotlin conversion
- **[Critical Fixes Documentation](documentation/echo-critical-fixes.md)** - Immediate build fixes needed
- **[Refactoring Plan](documentation/echo-refactoring-plan.md)** - Long-term architecture roadmap
- **[Agent Session Logs](AGENT_DOCUMENTATION.md)** - Track all changes and current state

## 🤝 Contributing

### For Human Contributors:
1. Read the documentation in `documentation/` folder
2. Focus on small, testable improvements
3. Follow Android development best practices
4. Submit PRs with comprehensive descriptions

### For AI Assistant Contributors:
1. **ALWAYS start by reading `AGENT_WORKFLOW_GUIDE.md`** (contains complete instructions)
2. **Use GitHub MCP server functions** instead of manual git commands
3. **Follow the established workflow** in `AGENT_SESSION_CHECKLIST.md`
4. **Document all changes** in `AGENT_DOCUMENTATION.md` with proper change log entries
5. **Focus on incremental improvements**, not large refactors
6. **Use the copy-paste prompts above** for consistent session starts

## 📄 License

[Add your license information here]

---

---

## 🤖 **QUICK START COMMANDS FOR AI AGENTS**

**✅ Use these exact commands in your development environment:**

```bash
# First time setup - verify environment
cd echo && ./gradlew --version && ./gradlew clean

# Check current build status  
./gradlew build --continue

# Run specific tests
./gradlew :features:recorder:test
./gradlew test

# Find files
find . -name "*.kt" -o -name "*.java" | head -20
```

**🔥 CRITICAL FOR AI AGENTS:** 
1. **READ** `AGENT_WORKFLOW_GUIDE.md` before making ANY changes
2. **USE** GitHub MCP server functions for Git operations
3. **FOLLOW** the incremental change methodology - small steps only
4. **DOCUMENT** every change in `AGENT_DOCUMENTATION.md`
