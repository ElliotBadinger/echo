# Critical Agent Development Principles

**Version:** 1.0
**Last Updated:** 2025-09-08 21:40 UTC
**Status:** MANDATORY READING - These principles are non-negotiable

## 🚨 CRITICAL ANTI-PATTERNS - NEVER DO THESE

### 1. Test File Deletion or Simplification
**❌ NEVER DELETE OR SIMPLIFY TEST FILES WHEN ENCOUNTERING ERRORS**

- **What NOT to do:** Remove test files or replace comprehensive tests with simple validation tests because of compilation/mocking issues
- **Why this is wrong:** This accumulates technical debt and reduces test coverage
- **What TO do:** Fix the underlying testing issues properly (mocking problems, dependency issues, etc.)
- **Example violation:** Replacing `AacMp4WriterTest.kt` with MockedStatic issues with simple parameter validation tests
- **Correct approach:** Fix MockedStatic issues by using constructor injection mocking or other proper mocking techniques

### 2. Claiming Work is "Done" When Tests Don't Exist or Pass
**❌ NEVER MARK CONVERSIONS/FEATURES AS COMPLETE WITHOUT PROPER TESTS**

- **What NOT to do:** Convert Java files to Kotlin and claim completion without comprehensive unit tests
- **Why this is wrong:** Creates false confidence in code quality and hides potential issues
- **What TO do:** Every converted file must have comprehensive unit tests that actually test the functionality
- **Verification required:** Check that test files exist AND that they contain meaningful tests with proper assertions

### 3. Avoiding Complex Testing Due to Difficulty
**❌ NEVER CHOOSE SIMPLE TESTS OVER COMPREHENSIVE ONES DUE TO IMPLEMENTATION DIFFICULTY**

- **What NOT to do:** Use simple validation tests instead of proper MediaCodec/Android component testing
- **Why this is wrong:** Reduces confidence in code correctness and misses integration issues
- **What TO do:** Invest time in proper mocking setup, dependency injection, or test framework configuration
- **Principle:** Incremental comprehensiveness is always preferred over simple tests

## ✅ MANDATORY PRACTICES

### 1. Comprehensive Test Coverage
- **Every converted file MUST have comprehensive unit tests**
- **Tests MUST actually test the functionality, not just parameter validation**
- **Integration tests MUST verify interaction with Android components**
- **Error handling MUST be tested with proper exception scenarios**

### 2. Proper Issue Classification
- **TIER 1 errors:** Build failures, test compilation failures, runtime crashes
- **TIER 2 improvements:** Feature additions, code quality improvements
- **Never proceed to TIER 2 if ANY TIER 1 issues exist**
- **Previous session claims MUST be audited and verified before proceeding**

### 3. Technical Debt Prevention
- **Never take shortcuts that reduce code quality**
- **Never remove functionality to avoid fixing issues**
- **Always fix root causes, not symptoms**
- **Document WHY complex approaches are necessary**

## 🔍 AUDIT REQUIREMENTS

### Previous Session Verification
Before starting ANY new work, agents MUST:

1. **Verify Previous Claims:**
   - Read the last change log entry
   - Check that claimed files actually exist and work
   - Run tests to verify claimed functionality
   - Validate that "completed" conversions have proper tests

2. **Test Coverage Audit:**
   - For every converted `.kt` file, verify corresponding test file exists
   - Check that test files contain `@Test` annotations and meaningful assertions
   - Verify tests actually compile and run
   - Ensure tests cover error cases, not just happy path

3. **Build Health Verification:**
   - Run `./gradlew clean build` to verify current state
   - Check that ALL tests compile (not just pass, but compile)
   - Verify no regressions from previous sessions

## 📋 DOCUMENTATION REQUIREMENTS

### Change Log Entries MUST Include:
- **Files Modified:** Complete list with before/after state
- **Tests Added:** Specific test files and what they cover
- **Verification Steps:** How the change was validated
- **Rollback Plan:** Exact steps to undo if issues arise
- **Technical Debt Assessment:** Any shortcuts taken and why they're temporary

### Status Updates MUST Include:
- **Honest Assessment:** Don't claim completion without proper verification
- **Test Coverage Status:** Percentage of converted files with comprehensive tests
- **Known Issues:** Any problems discovered during audit
- **Next Steps:** Specific, actionable items for next session

## 🎯 QUALITY GATES

### Before Claiming "Conversion Complete":
- [ ] Original Java file removed
- [ ] Kotlin file compiles successfully
- [ ] Comprehensive unit test file exists and passes
- [ ] Integration with existing code verified
- [ ] Error handling tested
- [ ] Performance impact assessed
- [ ] Documentation updated

### Before Claiming "TIER 1 Resolved":
- [ ] All build errors eliminated
- [ ] All test compilation errors fixed
- [ ] All runtime crashes resolved
- [ ] CI pipeline passes
- [ ] No regressions introduced
- [ ] Root causes addressed (not just symptoms)

## 🚫 CONSEQUENCES OF VIOLATIONS

### Technical Debt Accumulation:
- Simplified tests reduce confidence in code quality
- Missing tests hide integration issues
- Shortcuts create maintenance burden
- False completion claims mislead future sessions

### Project Health Impact:
- Reduced reliability of build system
- Increased risk of production issues
- Difficulty in future refactoring
- Loss of development velocity

## 📖 REQUIRED READING FOR ALL AGENTS

Every AI agent working on this project MUST:
1. Read this document completely before making ANY changes
2. Understand that these principles are non-negotiable
3. Apply these principles consistently throughout the session
4. Update documentation honestly and completely
5. Never compromise on quality for speed

## 🔄 CONTINUOUS IMPROVEMENT

### When Encountering Difficult Testing Scenarios:
1. **Research proper solutions** using available MCP tools
2. **Invest time in proper setup** rather than taking shortcuts
3. **Document the solution** for future reference
4. **Ask for clarification** if requirements are unclear
5. **Never assume simple tests are acceptable**

### When Previous Sessions Have Issues:
1. **Fix the issues properly** before proceeding
2. **Document what was wrong** and how it was fixed
3. **Update status honestly** to reflect actual state
4. **Don't hide or minimize problems**

---

**Remember: Quality is not negotiable. Comprehensive testing is not optional. Technical debt is not acceptable.**

*This document establishes the minimum quality standards for all AI agent development work on the Echo project.*