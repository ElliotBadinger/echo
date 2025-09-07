# Change Log Template

**Version:** 2.0 - Unified Documentation System
**Purpose:** Standardized format for documenting all changes with MCP integration
**Integration:** Used across all project documentation

---

## Change [YYYY-MM-DD HH:MM] - [CHANGE_ID]

### Goal
[Brief description of what was accomplished]
- What specific problem this addresses
- Expected outcome and success criteria
- Scope and impact of the change

### Research Conducted
**MCP Tool Used**: [Brave Search, Context7, Playwright, GitHub MCP, etc.]
**Query**: [Specific search query or library ID used]
**Key Findings**:
- [Finding 1]: [Source, credibility assessment, key insights]
- [Finding 2]: [Source, credibility assessment, key insights]
- [Finding 3]: [Source, credibility assessment, key insights]

**Sources Applied**:
- [Source 1]: [URL or reference, publication date, credibility rating]
- [Source 2]: [URL or reference, publication date, credibility rating]

**Application to Implementation**:
- How research findings informed the technical approach
- Specific patterns or techniques adopted from research
- Rationale for chosen implementation strategy

### Files Modified
- **ADDED**: `path/to/new/file.kt` - [Brief description of new functionality]
- **MODIFIED**: `path/to/existing/file.kt` - [Specific changes made]
- **REMOVED**: `path/to/deleted/file.kt` - [Reason for removal]
- **RENAMED**: `old/path/file.kt` → `new/path/file.kt` - [Reason for rename]

### Testing Done
**Unit Tests**:
- [Test 1]: [Description, pass/fail, coverage achieved]
- [Test 2]: [Description, pass/fail, coverage achieved]

**Integration Tests**:
- [Test 1]: [Description, pass/fail, scenarios covered]
- [Test 2]: [Description, pass/fail, scenarios covered]

**CI Validation**:
- **Build Status**: ✅ PASSED / ❌ FAILED
- **Test Results**: [X] passed, [Y] failed, [Z] skipped
- **Performance Impact**: [Description of any performance changes]
- **Artifact Analysis**: [Key findings from CI artifacts]

### Result
✅ **SUCCESS**: [What worked and why]
- [Specific achievement 1]
- [Specific achievement 2]
- [Measurable improvement]

❌ **FAILED**: [What didn't work and why]
- [Specific issue encountered]
- [Root cause analysis]
- [Impact assessment]

🟡 **PARTIAL**: [What partially worked]
- [What succeeded]
- [What needs follow-up]
- [Interim solution]

🔍 **MCP Insights**: [Learnings about MCP tool effectiveness]
- [Tool performance assessment]
- [Usage pattern effectiveness]
- [Optimization opportunities identified]

### Technical Implementation Details
**Architecture Changes**:
- [High-level architecture modifications]
- [Design pattern adoption]
- [Framework integration changes]

**Code Quality Improvements**:
- [Specific improvements made]
- [Best practices applied]
- [Technical debt reduction]

**Performance Optimizations**:
- [Performance improvements achieved]
- [Benchmark results]
- [Resource usage impact]

### Research Effectiveness Assessment
**Research Quality**: [1-10 rating]
- Criteria: Source credibility, recency, relevance, completeness

**Implementation Alignment**: [1-10 rating]
- How well research informed the actual implementation

**Time Savings**: [Estimated hours saved vs. manual approach]
- Research phase time savings
- Implementation phase time savings
- Debugging time savings

**Knowledge Gain**: [Key learnings and best practices acquired]
- New patterns learned
- Best practices adopted
- Technical insights gained

### Next Steps
**Immediate Actions**:
- [Action 1]: [Specific next step with timeline]
- [Action 2]: [Specific next step with timeline]

**Follow-up Tasks**:
- [Task 1]: [Related work identified]
- [Task 2]: [Improvement opportunity]

**Monitoring Requirements**:
- [Metric to track]: [Success criteria]
- [Performance indicator]: [Target value]

### Rollback Information
**Rollback Plan**:
- [Step-by-step rollback procedure]
- [Files to restore]
- [Commands to execute]

**Rollback Impact**:
- [Functionality that will be lost]
- [Data that may be affected]
- [Time required for rollback]

**Rollback Validation**:
- [Tests to verify rollback success]
- [Functionality to validate]

### Documentation Updates
**Files Updated**:
- `docs/project-state/change-log.md` - Added this change entry
- `docs/project-state/current-status.md` - Updated project status
- `docs/project-state/priorities.md` - Updated priorities if needed

**Cross-References**:
- Related changes: [CHANGE_ID_1], [CHANGE_ID_2]
- Framework updates: [Specific frameworks affected]
- MCP usage documented: [Tools used and effectiveness]

---

## Template Usage Guidelines

### When to Use This Template
- ✅ All code changes and implementations
- ✅ Framework modifications and updates
- ✅ Research-informed technical decisions
- ✅ CI/CD pipeline changes
- ✅ Documentation system updates

### Required Sections
- **Goal**: Always required - defines success criteria
- **Research Conducted**: Required for technical changes
- **Files Modified**: Always required - tracks scope
- **Testing Done**: Always required - validates implementation
- **Result**: Always required - documents outcomes

### Optional Sections
- **Technical Implementation Details**: Use for complex changes
- **Research Effectiveness Assessment**: Use when MCP tools were used
- **Next Steps**: Use when follow-up work is identified
- **Rollback Information**: Use for production-affecting changes

### Quality Standards
- **Completeness**: All relevant information included
- **Accuracy**: Technical details are correct
- **Clarity**: Easy to understand by future developers
- **Actionability**: Provides clear next steps and rollback procedures

---

*This template ensures consistent, comprehensive documentation of all changes with integrated MCP research tracking and effectiveness assessment.*