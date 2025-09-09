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

### 📋 **MANDATORY READING FOR ALL AI AGENTS**

**🚨 CRITICAL: Every AI agent MUST read these files in this exact order before making ANY changes:**

#### Phase 1: Core Workflow Understanding (MANDATORY - 10 minutes)
1. **[Critical Principles](docs/agent-workflow/critical-principles.md)** - ⚠️ NON-NEGOTIABLE quality standards (READ FIRST)
2. **[Complete Agent Workflow Guide](docs/agent-workflow/detailed-guide.md)** - Essential reading for AI-assisted development
3. **[Session Checklist](docs/agent-workflow/session-checklist.md)** - Step-by-step session workflow
4. **[Quick Start Guide](docs/agent-workflow/quick-start.md)** - 15-minute agent onboarding

#### Phase 2: Project State Assessment (MANDATORY - 5 minutes)
5. **[Current Status](docs/project-state/current-status.md)** - Current project state and critical issues
6. **[Current Priorities](docs/project-state/priorities.md)** - Current development priorities
7. **[Change Log](docs/project-state/change-log.md)** - Historical changes and research findings

#### Phase 3: Technical Framework Reference (AS NEEDED)
8. **[Research Framework](docs/frameworks/research-framework.md)** - Research-driven development methodology
9. **[Kotlin Migration Framework](docs/frameworks/kotlin-migration.md)** - Java-to-Kotlin conversion methodology
10. **[ML Strategy Framework](docs/frameworks/ml-strategy.md)** - ML research and implementation strategy
11. **[Performance Framework](docs/frameworks/performance-framework.md)** - Performance optimization research
12. **[UI/UX Framework](docs/frameworks/ui-ux-framework.md)** - Professional UI development framework
13. **[Framework Integration](docs/frameworks/framework-integration.md)** - How frameworks work together

#### Phase 4: MCP Server Integration (AS NEEDED)
14. **[MCP Optimization](docs/mcp-integration/mcp-optimization.md)** - MCP server usage optimization
15. **[Context7 Guide](docs/mcp-integration/context7-guide.md)** - Android documentation access
16. **[Brave Search Guide](docs/mcp-integration/brave-search-guide.md)** - Technical research methodology
17. **[GitHub MCP Guide](docs/mcp-integration/github-mcp-guide.md)** - CI/CD and repository management
18. **[Playwright Guide](docs/mcp-integration/playwright-guide.md)** - Web research and extraction

#### Phase 5: Templates and Automation (WHEN DOCUMENTING)
19. **[Templates Directory](docs/templates/)** - Standardized documentation templates
20. **[Automation Directory](docs/automation/)** - Documentation automation scripts

#### **COPY THIS PROMPT for New AI Sessions:**
```
I'm working on the Echo Android project. Follow this EXACT workflow:

🚨 PHASE 1: MANDATORY READING (15 minutes - DO NOT SKIP)
1. Read docs/agent-workflow/critical-principles.md COMPLETELY - Non-negotiable quality standards
2. Read docs/agent-workflow/detailed-guide.md COMPLETELY - Contains all essential instructions
3. Read docs/agent-workflow/session-checklist.md - Your step-by-step workflow
4. Read docs/project-state/current-status.md - Current project state and priorities

🔍 PHASE 2: PROJECT ASSESSMENT (5 minutes)
5. Check current build status: cd echo && ./gradlew clean
6. Review docs/project-state/priorities.md for current focus areas
7. Check docs/project-state/change-log.md for recent changes

⚡ PHASE 3: ERROR-FIRST PRIORITIZATION (ABSOLUTE PRIORITY)
- TIER 1: Fix any failing tests, build errors, crashes (MUST FIX FIRST)
- TIER 2: Research-informed incremental improvements with comprehensive testing
- TIER 3: Architecture enhancements only if Tiers 1-2 complete

🔬 PHASE 4: RESEARCH-DRIVEN DEVELOPMENT
When encountering technical decisions or errors:
- Use Brave Search MCP for SOTA algorithms and scientific research
- Use Context7 MCP for Android API documentation and implementation guidance
- Consult relevant framework docs:
  - docs/frameworks/research-framework.md for overall methodology
  - docs/frameworks/kotlin-migration.md for Java-to-Kotlin conversion
  - docs/frameworks/ml-strategy.md for ML/AI decisions
  - docs/frameworks/performance-framework.md for optimization
  - docs/frameworks/ui-ux-framework.md for UI work
- Document all research findings in change logs

🛠️ PHASE 5: IMPLEMENTATION RULES
- Use GitHub MCP server functions ONLY for read-only operations (list_workflow_runs, get_job_logs)
- NEVER use GitHub MCP for commits (causes synchronization conflicts)
- Use manual git commands for ALL commits: git add . && git commit -m "..." && git push
- Make small, incremental changes with immediate testing
- Every change must include comprehensive testing (unit, integration, error handling)
- Follow critical-principles.md - NEVER simplify tests to avoid complexity

📊 CURRENT PROJECT STATUS
- Build success rate: 100% (all TIER 1 errors resolved)
- Test compilation: 100% successful
- Current focus: TIER 2 incremental improvements (Kotlin migration, architecture, UI)
- MCP optimization: Target 15-20 Context7 uses/week for Android documentation

🎯 SUCCESS CRITERIA
- Fix issues properly (not with shortcuts)
- Maintain comprehensive test coverage
- Document all changes using templates in docs/templates/
- Use MCP servers effectively for research and monitoring
- Leave clear instructions for next session

REMEMBER: Quality is non-negotiable. Read critical-principles.md to understand what NOT to do.
```


#### **COPY THIS for Completed Feature/Fix Sessions:**
```
I just completed [describe what you implemented/fixed] on the Echo Android project. Please:

🔍 PHASE 1: MANDATORY VERIFICATION (10 minutes)
1. Read docs/agent-workflow/critical-principles.md - Ensure no quality violations occurred
2. Read docs/project-state/current-status.md - Understand current state and recent changes
3. Verify completed work follows all documentation requirements
4. Check that comprehensive tests exist and pass for all changes

📊 PHASE 2: WORK VALIDATION (5 minutes)
5. Use GitHub MCP server ONLY for read-only operations:
   - Check CI status with list_workflow_runs
   - Analyze any test failures with get_job_logs and download_workflow_run_artifact
   - NEVER use push_files or create_or_update_file (causes git sync conflicts)
6. Verify all claims in change log are accurate and verifiable
7. Ensure no technical debt was introduced (no test simplification, no shortcuts)

🔬 PHASE 3: RESEARCH INTEGRATION (AS NEEDED)
8. If technical decisions were made, verify they're backed by research:
   - Brave Search MCP for SOTA techniques and performance optimization
   - Context7 MCP for Android API best practices and implementation patterns
   - Framework documentation for methodology compliance

🎯 PHASE 4: NEXT STEPS PLANNING
9. Identify next highest priority goal from Error-First Priority System:
   - TIER 1: Any failing tests, build errors, crashes (ABSOLUTE PRIORITY)
   - TIER 2: Research-informed incremental improvements
   - TIER 3: Architecture enhancements
10. Update docs/project-state/priorities.md with clear next steps
11. Document any research directions or MCP optimization opportunities

✅ QUALITY ASSURANCE CHECKLIST
- [ ] All changes have comprehensive tests (not simplified validation)
- [ ] No build errors or test failures introduced
- [ ] Research findings documented where applicable
- [ ] Change log entries complete and accurate
- [ ] Next session has clear, actionable priorities

REMEMBER: Never compromise on quality. Follow critical-principles.md religiously.
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

