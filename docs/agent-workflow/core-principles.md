# Agent Core Principles - Echo Project

## 🎯 The Only Rules That Matter

### 1. Error-First Priority
- Fix build errors before anything else
- Fix test failures before new features
- Fix crashes before improvements

### 2. Research Before Coding
- Use Brave Search MCP for technical problems
- Use Context7 MCP for Android documentation
- Document what actually helped (skip what didn't)

### 3. Small Changes Only
- One file at a time
- Test immediately after each change
- Commit working changes quickly

### 4. Essential Documentation
- Use `docs/templates/simple-change-log.md` for significant changes
- Use `docs/templates/mcp-research-notes.md` when research helps
- Update `docs/project-state/current-status.md` if project state changes

## 🚫 What NOT to Do
- Don't make multiple unrelated changes at once
- Don't skip testing after changes
- Don't spend more time on documentation than coding

## 📁 Key Files
- `docs/project-state/current-status.md` - Read this first every session
- `docs/project-state/change-log.md` - Add entries using simple template
- Build with: `./gradlew clean build`
- Test with: `./gradlew test`

---
*That's it. Focus on code, not process.*