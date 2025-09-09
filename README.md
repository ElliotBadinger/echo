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

## 🤖 AI Agent Development

**Essential Reading (5 minutes):**
1. **[Core Principles](docs/agent-workflow/core-principles.md)** - The 5 non-negotiable rules
2. **[Current Status](docs/project-state/current-status.md)** - Project state and priorities
3. **[Session Checklist](docs/agent-workflow/session-checklist.md)** - Simple workflow

**Key Rules:**
- Fix build/test errors FIRST (TIER 1) before any improvements
- Make small changes, test immediately
- Use manual git commands: `git add . && git commit -m "..." && git push`
- Research with MCP tools before coding complex fixes
- Document significant changes using templates in `docs/templates/`

**Quick Start:**
```bash
cd echo && ./gradlew clean  # Check build health
# Read docs/agent-workflow/core-principles.md
# Pick smallest goal from docs/project-state/priorities.md
# Make one small change, test, commit
```

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
- **Read core-principles.md first** - The 5 essential rules
- **Use manual git commands only** - Never use GitHub MCP for commits
- **Small incremental changes only** - One file, one issue at a time
- **Test immediately** after each change
- **Follow session-checklist.md** for consistent workflow

## 📚 Additional Resources

### 📖 Documentation System
- **[Core Principles](docs/agent-workflow/core-principles.md)** - Essential workflow rules
- **[Session Checklist](docs/agent-workflow/session-checklist.md)** - Simple workflow
- **[Current Status](docs/project-state/current-status.md)** - Project state
- **[Documentation Overview](docs/README.md)** - Full navigation

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
1. **Read `docs/agent-workflow/core-principles.md` FIRST** - The 5 essential rules
2. **Use manual git commands only** for commits (GitHub MCP causes sync conflicts)
3. **Follow the workflow** in `docs/agent-workflow/session-checklist.md`
4. **Focus on small improvements**, not large refactors
5. **Document significant changes** using templates in `docs/templates/`

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
1. **READ** `docs/agent-workflow/core-principles.md` before making ANY changes
2. **NEVER USE** GitHub MCP for commits - use manual git commands only
3. **FOLLOW** the incremental change methodology - small steps only
4. **DOCUMENT** significant changes using templates in `docs/templates/`

## 📚 Documentation

This project uses a unified documentation system for efficient knowledge management:

- **[Documentation Hub](docs/README.md)** - Central navigation and overview
- **[Agent Quick Start](docs/agent-workflow/quick-start.md)** - 15-minute agent onboarding
- **[Current Status](docs/project-state/current-status.md)** - Live project state
- **[Development Frameworks](docs/frameworks/)** - Technical frameworks and methodologies
- **[MCP Integration](docs/mcp-integration/)** - MCP server optimization guides

### Quick Navigation
- 🚀 **New to the project?** Start with [Core Principles](docs/agent-workflow/core-principles.md)
- 📊 **Current project state?** Check [Current Status](docs/project-state/current-status.md)
- 🔧 **Development workflow?** See [Session Checklist](docs/agent-workflow/session-checklist.md)
- 🤖 **MCP optimization?** Review [MCP Integration](docs/mcp-integration/mcp-optimization.md)

