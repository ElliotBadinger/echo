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
- **Build Status:** 🟡 Partially working (70% success rate - compilation passes, some tests fail)
- **Architecture:** Currently undergoing refactoring from monolithic to modular design
- **Active Development:** Focus on fixing build issues and improving test stability

## 🤖 AI Agent Development Workflow

**IMPORTANT:** This project uses AI agents for development assistance. If you're working with Claude or other AI assistants, follow this workflow for best results.

### 📋 **FIRST TIME OR NEW CONVERSATION: Read This Guide**
👉 **[Complete Agent Workflow Guide](AGENT_WORKFLOW_GUIDE.md)** - Essential reading for AI-assisted development

### 🎯 **Quick Reference for AI Agents**

#### Essential Files to Read First (In Order):
1. **`AGENT_DOCUMENTATION.md`** - Current project state, issues, and change tracking
2. **`AGENT_SESSION_CHECKLIST.md`** - Step-by-step session workflow
3. **`documentation/echo-critical-fixes.md`** - Critical build and infrastructure issues
4. **`documentation/echo-refactoring-plan.md`** - Long-term architectural roadmap

#### Recommended First Prompt for New AI Sessions:
```
I'm starting work on the Echo Android project. Please:

1. Read the AGENT_DOCUMENTATION.md file completely to understand current state
2. Follow the AGENT_SESSION_CHECKLIST.md for proper workflow
3. Check current build status with: ./gradlew clean
4. Identify the next small, incremental goal from the priority list
5. Never attempt large architectural changes - focus on small, verifiable improvements

Current focus areas: Fix test failures, resolve build issues, improve Compose integration.
Follow the incremental change methodology documented in AGENT_DOCUMENTATION.md.
```

### 🔄 **Git Workflow for Changes**

#### Making Changes:
```bash
# Always work on a feature branch
git checkout -b fix/your-specific-fix
git add .
git commit -m "Fix: specific description of what was changed"
git push origin fix/your-specific-fix
```

#### For AI Agents to Push Changes:
After making successful changes, use these commands:
```bash
# Add all changes
git add .

# Commit with descriptive message
git commit -m "Agent Session [DATE]: Fixed [specific issue] - improved build success rate"

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

### Build & Test Issues
- Some unit tests fail with runtime exceptions
- Threading violations in SaidItService
- File locking issues in CI/CD
- Gradle warnings about deprecated APIs

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
- **Small incremental changes only**
- **Document every change** in AGENT_DOCUMENTATION.md
- **Test immediately** after each change
- **Never assume context** from previous conversations
- **Follow the checklist** in AGENT_SESSION_CHECKLIST.md

## 📚 Additional Resources

- **[Agent Workflow Guide](AGENT_WORKFLOW_GUIDE.md)** - Complete guide for AI-assisted development
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
1. **ALWAYS start by reading `AGENT_WORKFLOW_GUIDE.md`**
2. Follow the established workflow in `AGENT_SESSION_CHECKLIST.md`
3. Document all changes in `AGENT_DOCUMENTATION.md`
4. Focus on incremental improvements, not large refactors

## 📄 License

[Add your license information here]

---

**🔥 IMPORTANT FOR AI AGENTS:** Before making ANY changes, read `AGENT_WORKFLOW_GUIDE.md` for complete instructions on how to work with this project effectively.