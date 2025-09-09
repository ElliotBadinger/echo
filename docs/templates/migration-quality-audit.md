# Migration Quality Audit Template

**MANDATORY**: Every agent must complete this audit before ANY new Kotlin migration work.

## 🔍 Previous Migration Validation

### Files to Audit (Current as of 2025-09-09):

#### ✅ **EchoApp.kt** - NEEDS COMPREHENSIVE TESTING
**Current Test Issues:**
- ❌ Tests only check annotations, not actual Application lifecycle
- ❌ No Hilt initialization verification  
- ❌ No Android framework integration testing

**Required Fixes:**
```kotlin
@Test
fun `EchoApp should initialize Hilt correctly`() {
    // Test actual Hilt initialization, not just annotation presence
}

@Test 
fun `EchoApp should handle Application lifecycle properly`() {
    // Test onCreate, onLowMemory, onTerminate if overridden
}
```

#### ✅ **AppModule.kt** - CRITICAL ARCHITECTURAL ISSUE
**Current Test Issues:**
- ❌ Tests only check annotations exist
- ❌ No singleton behavior verification
- ❌ **CRITICAL**: AudioConfig is NOT used by SaidItService - architectural disconnect!

**Required Investigation:**
1. Determine if AudioConfig should be injected into SaidItService
2. If yes, fix the integration
3. If no, document why AudioConfig exists

**Required Fixes:**
```kotlin
@HiltAndroidTest
class AppModuleIntegrationTest {
    @Test
    fun `AudioConfig should be singleton across injections`() {
        // Test actual singleton behavior
    }
    
    @Test
    fun `AudioConfig should be used by SaidItService`() {
        // Test integration with actual consumers
    }
}
```

#### ✅ **SaidItFragment.kt** - NEEDS LIFECYCLE TESTING  
**Current Test Issues:**
- Most tests fail due to Robolectric configuration
- Fragment lifecycle not properly tested
- Service integration needs validation

#### ✅ **Utility Classes** (StringFormat, Clock, TimeFormat, etc.)
**Current Test Issues:**  
- Need verification that tests cover actual use cases
- Edge case handling validation required

## 🚨 CRITICAL FINDINGS TO ADDRESS

### 1. AudioConfig Architectural Issue
**Problem**: `AppModule.provideAudioConfig()` returns hardcoded config, but `SaidItService` reads sample rate from SharedPreferences.

**Investigation Required:**
- Should SaidItService use injected AudioConfig?
- Is this a bug or intentional design?
- Document the decision with rationale

### 2. Test Quality Pattern
**Problem**: Tests focus on checking annotations exist rather than verifying actual functionality.

**Fix Required**: Rewrite tests to verify integration and behavior.

## 📋 Audit Checklist

**Before starting ANY new migration work:**

- [ ] **EchoApp Integration Tests**: Verify Application lifecycle and Hilt initialization
- [ ] **AppModule Architecture Fix**: Resolve AudioConfig usage disconnect
- [ ] **AppModule Integration Tests**: Verify singleton behavior and actual usage
- [ ] **SaidItFragment Tests**: Fix Robolectric issues and add lifecycle testing
- [ ] **Utility Class Validation**: Ensure tests cover real use cases
- [ ] **Architectural Documentation**: Document any design decisions or discovered issues

## ⚠️ Quality Gates

**Do NOT proceed with new migrations until:**
1. All previous migrations have comprehensive integration tests
2. The AudioConfig architectural issue is resolved
3. All tests pass with proper behavioral verification
4. Any discovered architectural issues are documented and addressed

## 🎯 Next Migration Target

**Only after completing the audit above**, choose migration targets based on:
- **Impact**: Files with significant business logic (50+ lines)
- **Complexity**: Activities, Adapters, Services with meaningful functionality
- **Architecture**: Opportunities to improve design during conversion

**Avoid**: Trivial conversions, simple data holders, minimal logic files

---

**Remember**: The goal is architectural improvement through migration, not just syntax conversion!
