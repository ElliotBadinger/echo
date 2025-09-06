package eu.mrogalski.saidit

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class UserInfoTest {

    @Test
    fun testUserInfoObjectExists() {
        // Verify that UserInfo object is accessible
        assertNotNull(UserInfo)
    }

    @Test
    fun testAllMethodsExist() {
        // Verify all expected methods exist
        val methods = UserInfo::class.java.declaredMethods
        val methodNames = methods.map { it.name }
        
        assertTrue("getUserPhoneNumber should exist", methodNames.contains("getUserPhoneNumber"))
        assertTrue("getUserID should exist", methodNames.contains("getUserID"))
        assertTrue("getUserCountryCode should exist", methodNames.contains("getUserCountryCode"))
        assertTrue("getUserEmail should exist", methodNames.contains("getUserEmail"))
        
        // Should have exactly 4 user methods
        val userMethods = methodNames.filter { it.startsWith("getUser") }
        assertEquals("Should have 4 user methods", 4, userMethods.size)
    }

    @Test
    fun testMethodsAreStatic() {
        // Verify methods are static for Java compatibility
        try {
            val getUserIDMethod = UserInfo::class.java.getMethod("getUserID", android.content.Context::class.java)
            assertTrue("getUserID should be static", java.lang.reflect.Modifier.isStatic(getUserIDMethod.modifiers))
            
            val getUserPhoneNumberMethod = UserInfo::class.java.getMethod("getUserPhoneNumber", android.content.Context::class.java)
            assertTrue("getUserPhoneNumber should be static", java.lang.reflect.Modifier.isStatic(getUserPhoneNumberMethod.modifiers))
            
            val getUserCountryCodeMethod = UserInfo::class.java.getMethod("getUserCountryCode", android.content.Context::class.java)
            assertTrue("getUserCountryCode should be static", java.lang.reflect.Modifier.isStatic(getUserCountryCodeMethod.modifiers))
            
            val getUserEmailMethod = UserInfo::class.java.getMethod("getUserEmail", android.content.Context::class.java)
            assertTrue("getUserEmail should be static", java.lang.reflect.Modifier.isStatic(getUserEmailMethod.modifiers))
        } catch (e: NoSuchMethodException) {
            fail("Required methods should exist: " + e.message)
        }
    }

    @Test
    fun testMethodReturnTypes() {
        // Verify correct return types
        try {
            val getUserIDMethod = UserInfo::class.java.getMethod("getUserID", android.content.Context::class.java)
            assertEquals("getUserID should return String", String::class.java, getUserIDMethod.returnType)
            
            val getUserPhoneNumberMethod = UserInfo::class.java.getMethod("getUserPhoneNumber", android.content.Context::class.java)
            assertEquals("getUserPhoneNumber should return String", String::class.java, getUserPhoneNumberMethod.returnType)
            
            val getUserCountryCodeMethod = UserInfo::class.java.getMethod("getUserCountryCode", android.content.Context::class.java)
            assertEquals("getUserCountryCode should return String", String::class.java, getUserCountryCodeMethod.returnType)
            
            val getUserEmailMethod = UserInfo::class.java.getMethod("getUserEmail", android.content.Context::class.java)
            assertEquals("getUserEmail should return String", String::class.java, getUserEmailMethod.returnType)
        } catch (e: NoSuchMethodException) {
            fail("Required methods should exist: " + e.message)
        }
    }

    @Test
    fun testKotlinObjectBehavior() {
        // Verify Kotlin object singleton behavior
        val instance1 = UserInfo
        val instance2 = UserInfo
        assertSame("UserInfo should be singleton", instance1, instance2)
    }

    @Test
    fun testCorrectPackage() {
        // Verify class is in correct package
        assertEquals("Should be in eu.mrogalski.saidit package",
                    "eu.mrogalski.saidit",
                    UserInfo::class.java.`package`?.name ?: "unknown")
    }
}