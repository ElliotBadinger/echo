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

**IMPORTANT:** This project uses AI agents for development assistance and has comprehensive MCP server integration. The documentation system has been refactored into a unified structure for better navigation and maintenance.

### 📋 **FIRST TIME OR NEW CONVERSATION: Read This Guide**
👉 **[Complete Agent Workflow Guide](docs/agent-workflow/detailed-guide.md)** - Essential reading for AI-assisted development

### 🎯 **Quick Reference for AI Agents**

#### Essential Documentation Structure (In Order):
1. **`docs/project-state/current-status.md`** - Current project state and critical issues
2. **`docs/agent-workflow/session-checklist.md`** - Step-by-step session workflow
3. **`docs/project-state/priorities.md`** - Current development priorities
4. **`docs/frameworks/research-framework.md`** - Research-driven development methodology
5. **`docs/frameworks/ml-strategy.md`** - ML research and implementation strategy
6. **`docs/frameworks/performance-framework.md`** - Performance optimization research
7. **`docs/frameworks/ui-ux-framework.md`** - Professional UI development framework
8. **`docs/frameworks/kotlin-migration.md`** - Java-to-Kotlin conversion methodology
9. **`docs/project-state/change-log.md`** - Historical changes and research findings
10. **`docs/mcp-integration/mcp-optimization.md`** - MCP server usage optimization

#### **COPY THIS PROMPT for New AI Sessions:**
```
I'm continuing work on the Echo Android project. Please:

1. Read the docs/agent-workflow/detailed-guide.md file completely - this contains all essential instructions including research tools and CI integration
2. Follow the docs/agent-workflow/session-checklist.md for proper workflow with error-first prioritization  
3. Read docs/project-state/current-status.md to understand current project state and research-enhanced development process
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
7. Use GitHub MCP server functions ONLY for read-only operations (list_workflow_runs, get_job_logs) - NEVER for commits due to synchronization conflicts
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
- Do not proceed until CI tests pass or the failure is analyzed/documented in docs/project-state/current-status.md.
- If CI passes but local fails, document it as a potential local environment issue.

When encountering errors or issues (especially TIER 1 build/test failures), always research fixes using available MCP tools and frameworks before attempting changes. Use Brave Search MCP for state-of-the-art (SOTA) solutions and scientific papers, Context7 MCP for Android API documentation, and integrate findings from RESEARCH_FRAMEWORK.md or specific frameworks like KOTLIN_MIGRATION_FRAMEWORK.md. Document all research in docs/project-state/current-status.md, including sources and how they apply to the fix. Examples: For ClassNotFoundException, search "Android Kotlin Kapt stub conflicts 2025" via Brave Search MCP; for test classpath issues, query Context7 MCP for "Android Gradle test classpath configuration best practices".
```


#### **COPY THIS for Completed Feature/Fix Sessions:**
```
I just completed [describe what you implemented/fixed] on the Echo Android project. Please:

1. Read docs/project-state/current-status.md to understand current state and recent changes with research integration
2. Verify the completed work is properly documented in the change log with research findings
3. Check if there are any follow-up issues or improvements needed using available MCP tools
4. Use GitHub MCP server ONLY for read-only operations:
   - Check CI status with list_workflow_runs  
   - Analyze any test failures with get_job_logs and download_workflow_run_artifact
   - NEVER use push_files or create_or_update_file (causes git sync conflicts)
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

#### **⚠️ CRITICAL FOR AI AGENTS: GitHub MCP Synchronization Issue**

**🚨 WARNING: GitHub MCP functions create git synchronization conflicts!**

**❌ NEVER USE these GitHub MCP functions for commits:**
- `push_files()` - Creates commits directly on GitHub, bypassing local git
- `create_or_update_file()` - Same synchronization issue
- These cause "fetch first" errors when user tries to push local changes

**✅ SAFE GitHub MCP functions (read-only only):**
- `list_workflow_runs()` - Monitor CI status
- `get_job_logs()` - Debug CI failures
- `download_workflow_run_artifact()` - Analyze test results

**✅ REQUIRED: Use Manual Git for ALL Commits:**
```bash
# The ONLY safe way to commit changes
git add .
git commit -m "Agent Session [DATE]: Fixed [specific issue] - improved [metric]"
git push origin HEAD
```

**📝 See docs/agent-workflow/detailed-guide.md section 4.5 for complete synchronization issue details and resolution steps.**

## 📁 Project Structure

```
echo/
├── SaidIt/                          # Main app module
├── domain/                          # Domain layer (business logic)
├── features/recorder/               # Recording feature module
├── docs/                           # 📚 Unified Documentation System
│   ├── README.md                   # Documentation navigation
│   ├── agent-workflow/             # 🤖 Agent development guides
│   ├── project-state/              # 📊 Current status and priorities
│   ├── frameworks/                 # 🏗️ Development frameworks
│   ├── mcp-integration/            # 🔌 MCP server optimization
│   ├── templates/                  # 📝 Standardized templates
│   └── automation/                 # ⚙️ Documentation automation
├── build.gradle.kts                 # Project build configuration
└── README.md                        # Main project README
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
- **NEVER use GitHub MCP functions for commits** - causes synchronization conflicts with local git
- **Use manual git commands only** for all commits and pushes
- **Small incremental changes only** - never attempt large architectural changes  
- **Document every change** in docs/project-state/current-status.md with proper change log entries
- **Test immediately** after each change using ./gradlew commands
- **Never assume context** from previous conversations - always read current documentation
- **Follow the complete workflow** in docs/agent-workflow/detailed-guide.md and docs/agent-workflow/session-checklist.md

## 📚 Additional Resources

### 📖 Documentation System
- **[Documentation Overview](docs/README.md)** - Unified documentation system navigation
- **[🚨 Critical Principles](docs/agent-workflow/critical-principles.md)** - MANDATORY quality standards (READ FIRST)
- **[Agent Workflow Guide](docs/agent-workflow/detailed-guide.md)** - Complete guide for AI-assisted development
- **[Quick Start Guide](docs/agent-workflow/quick-start.md)** - 1-page agent onboarding
- **[Session Checklist](docs/agent-workflow/session-checklist.md)** - Step-by-step workflow

### 🏗️ Development Frameworks
- **[Research Framework](docs/frameworks/research-framework.md)** - Research-driven development methodology
- **[ML Strategy Framework](docs/frameworks/ml-strategy.md)** - ML research and implementation
- **[Performance Framework](docs/frameworks/performance-framework.md)** - Performance optimization research
- **[UI/UX Framework](docs/frameworks/ui-ux-framework.md)** - Professional UI development
- **[Kotlin Migration Framework](docs/frameworks/kotlin-migration.md)** - Java-to-Kotlin conversion
- **[Framework Integration](docs/frameworks/framework-integration.md)** - How frameworks work together

### 📊 Project State
- **[Current Status](docs/project-state/current-status.md)** - Live project state and critical issues
- **[Change Log](docs/project-state/change-log.md)** - Historical changes and research findings
- **[Priorities](docs/project-state/priorities.md)** - Current development priorities
- **[Research Findings](docs/project-state/research-findings.md)** - Technical insights and discoveries

### 🔌 MCP Integration
- **[MCP Optimization](docs/mcp-integration/mcp-optimization.md)** - Overall MCP server usage strategy
- **[Context7 Guide](docs/mcp-integration/context7-guide.md)** - Android documentation access
- **[Brave Search Guide](docs/mcp-integration/brave-search-guide.md)** - Technical research methodology
- **[GitHub MCP Guide](docs/mcp-integration/github-mcp-guide.md)** - CI/CD and repository management
- **[Playwright Guide](docs/mcp-integration/playwright-guide.md)** - Web research and extraction

### 📝 Templates
- **[Change Log Template](docs/templates/change-log-template.md)** - Standardized change documentation
- **[MCP Usage Template](docs/templates/mcp-usage-template.md)** - MCP server usage tracking
- **[Session Report Template](docs/templates/session-report-template.md)** - Session documentation
- **[Research Template](docs/templates/research-template.md)** - Research documentation
- **[Testing Template](docs/templates/testing-template.md)** - Testing documentation

### ⚙️ Automation
- **[Documentation Config](docs/automation/docs-config.yaml)** - Documentation automation configuration

## 🤝 Contributing

### For Human Contributors:
1. Read the documentation in `documentation/` folder
2. Focus on small, testable improvements
3. Follow Android development best practices
4. Submit PRs with comprehensive descriptions

### For AI Assistant Contributors:
1. **🚨 MANDATORY: Read `docs/agent-workflow/critical-principles.md` FIRST** - Non-negotiable quality standards
2. **ALWAYS start by reading `docs/agent-workflow/detailed-guide.md`** (contains complete instructions)
3. **Use manual git commands only** for commits (GitHub MCP causes sync conflicts)
4. **Follow the established workflow** in `docs/agent-workflow/session-checklist.md`
5. **Document all changes** in `docs/project-state/current-status.md` with proper change log entries
6. **Focus on incremental improvements**, not large refactors
7. **NEVER delete or simplify test files** - fix testing issues properly
8. **Use the copy-paste prompts above** for consistent session starts

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
1. **READ** `docs/agent-workflow/detailed-guide.md` before making ANY changes
2. **USE** GitHub MCP server functions for Git operations
3. **FOLLOW** the incremental change methodology - small steps only
4. **DOCUMENT** every change in `docs/project-state/current-status.md`

## 📚 Documentation

This project uses a unified documentation system for efficient knowledge management:

- **[Documentation Hub](docs/README.md)** - Central navigation and overview
- **[Agent Quick Start](docs/agent-workflow/quick-start.md)** - 15-minute agent onboarding
- **[Current Status](docs/project-state/current-status.md)** - Live project state
- **[Development Frameworks](docs/frameworks/)** - Technical frameworks and methodologies
- **[MCP Integration](docs/mcp-integration/)** - MCP server optimization guides

### Quick Navigation
- 🚀 **New to the project?** Start with [Agent Quick Start](docs/agent-workflow/quick-start.md)
- 📊 **Current project state?** Check [Current Status](docs/project-state/current-status.md)
- 🔧 **Development workflow?** See [Session Checklist](docs/agent-workflow/session-checklist.md)
- 🤖 **MCP optimization?** Review [MCP Integration](docs/mcp-integration/mcp-optimization.md)

