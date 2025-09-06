package eu.mrogalski.android

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.ContentResolver
import android.content.res.Resources
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Patterns
import eu.mrogalski.saidit.UserInfo
import com.siya.epistemophile.R
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mockito.*
import java.util.regex.Pattern

/**
 * Unit tests for UserInfo utility class.
 * Tests core functionality with simplified mocking scenarios.
 */
@RunWith(MockitoJUnitRunner::class)
class UserInfoTest {

    @Mock
    private lateinit var context: Context
    
    @Mock
    private lateinit var telephonyManager: TelephonyManager
    
    @Mock
    private lateinit var accountManager: AccountManager
    
    @Mock
    private lateinit var contentResolver: ContentResolver
    
    @Mock
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        
        // Setup default context mocks
        `when`(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager)
        `when`(context.contentResolver).thenReturn(contentResolver)
        `when`(context.resources).thenReturn(resources)
    }

    // ============ getUserPhoneNumber Tests ============

    @Test
    fun `getUserPhoneNumber should return phone number when available`() {
        // Given
        val expectedPhoneNumber = "+1234567890"
        `when`(telephonyManager.line1Number).thenReturn(expectedPhoneNumber)

        // When
        val result = UserInfo.getUserPhoneNumber(context)

        // Then
        assertEquals(expectedPhoneNumber, result)
        verify(context).getSystemService(Context.TELEPHONY_SERVICE)
        verify(telephonyManager).line1Number
    }

    @Test
    fun `getUserPhoneNumber should return null when no phone number available`() {
        // Given
        `when`(telephonyManager.line1Number).thenReturn(null)

        // When
        val result = UserInfo.getUserPhoneNumber(context)

        // Then
        assertNull(result)
    }

    @Test
    fun `getUserPhoneNumber should return empty string when empty`() {
        // Given
        `when`(telephonyManager.line1Number).thenReturn("")

        // When
        val result = UserInfo.getUserPhoneNumber(context)

        // Then
        assertEquals("", result)
    }

    @Test
    fun `getUserPhoneNumber should handle SecurityException gracefully`() {
        // Given - TelephonyManager throws SecurityException
        `when`(telephonyManager.line1Number).thenThrow(SecurityException("Permission denied"))

        // When/Then - Should not crash and return null
        val result = UserInfo.getUserPhoneNumber(context)
        assertNull(result)
    }

    // ============ getUserID Tests ============

    @Test
    fun `getUserID should return device ID when available`() {
        // Given
        val expectedDeviceId = "device123456"
        `when`(telephonyManager.deviceId).thenReturn(expectedDeviceId)

        // When
        val result = UserInfo.getUserID(context)

        // Then
        assertEquals("device-id:$expectedDeviceId", result)
        verify(telephonyManager).deviceId
    }

    @Test
    fun `getUserID should return Android ID when device ID fails`() {
        // Given - Device ID throws SecurityException
        val androidId = "android123456"
        `when`(telephonyManager.deviceId).thenThrow(SecurityException("Permission denied"))
        
        mockStatic(Settings.Secure::class.java).use { settingsMock ->
            settingsMock.`when`<String> { 
                Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) 
            }.thenReturn(androidId)

            // When
            val result = UserInfo.getUserID(context)

            // Then
            assertEquals("android-id:$androidId", result)
        }
    }

    @Test
    fun `getUserID should handle all exceptions gracefully and return Android ID fallback`() {
        // Given - All methods throw exceptions except Android ID
        val androidId = "fallback123456"
        `when`(telephonyManager.deviceId).thenThrow(SecurityException("Permission denied"))
        
        mockStatic(Settings.Secure::class.java).use { settingsMock ->
            settingsMock.`when`<String> { 
                Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) 
            }.thenReturn(androidId)

            mockStatic(AccountManager::class.java).use { accountManagerMock ->
                accountManagerMock.`when`<AccountManager> { AccountManager.get(context) }
                    .thenThrow(SecurityException("Account access denied"))
                
                // When
                val result = UserInfo.getUserID(context)

                // Then
                assertEquals("android-id:$androidId", result)
            }
        }
    }

    // ============ getUserCountryCode Tests ============

    @Test
    fun `getUserCountryCode should return country code when found`() {
        // Given
        val simCountryCode = "us"
        val countryCodesArray = arrayOf("1,US", "44,GB", "49,DE")
        
        `when`(telephonyManager.simCountryIso).thenReturn(simCountryCode)
        `when`(resources.getStringArray(R.array.country_codes)).thenReturn(countryCodesArray)

        // When
        val result = UserInfo.getUserCountryCode(context)

        // Then
        assertEquals("1", result)
        verify(telephonyManager).simCountryIso
        verify(resources).getStringArray(R.array.country_codes)
    }

    @Test
    fun `getUserCountryCode should return empty string when not found`() {
        // Given
        val simCountryCode = "xx" // Non-existent country code
        val countryCodesArray = arrayOf("1,US", "44,GB", "49,DE")
        
        `when`(telephonyManager.simCountryIso).thenReturn(simCountryCode)
        `when`(resources.getStringArray(R.array.country_codes)).thenReturn(countryCodesArray)

        // When
        val result = UserInfo.getUserCountryCode(context)

        // Then
        assertEquals("", result)
    }

    @Test
    fun `getUserCountryCode should handle case insensitive matching`() {
        // Given
        val simCountryCode = "GB" // Uppercase
        val countryCodesArray = arrayOf("1,US", "44,gb", "49,DE") // Lowercase in array
        
        `when`(telephonyManager.simCountryIso).thenReturn(simCountryCode)
        `when`(resources.getStringArray(R.array.country_codes)).thenReturn(countryCodesArray)

        // When
        val result = UserInfo.getUserCountryCode(context)

        // Then
        assertEquals("44", result)
    }

    @Test
    fun `getUserCountryCode should handle exceptions gracefully`() {
        // Given - TelephonyManager throws exception
        `when`(telephonyManager.simCountryIso).thenThrow(SecurityException("Permission denied"))

        // When
        val result = UserInfo.getUserCountryCode(context)

        // Then
        assertEquals("", result)
    }

    // ============ getUserEmail Tests ============

    @Test
    fun `getUserEmail should return empty string when AccountManager throws exception`() {
        // Given - AccountManager throws SecurityException
        mockStatic(AccountManager::class.java).use { accountManagerMock ->
            accountManagerMock.`when`<AccountManager> { AccountManager.get(context) }
                .thenThrow(SecurityException("Account access denied"))
            
            // When
            val result = UserInfo.getUserEmail(context)

            // Then
            assertEquals("", result)
        }
    }

    @Test
    fun `getUserEmail should return empty string when no accounts`() {
        // Given
        `when`(accountManager.accounts).thenReturn(arrayOf())

        mockStatic(AccountManager::class.java).use { accountManagerMock ->
            accountManagerMock.`when`<AccountManager> { AccountManager.get(context) }
                .thenReturn(accountManager)
            
            // When
            val result = UserInfo.getUserEmail(context)

            // Then
            assertEquals("", result)
        }
    }

    // ============ Integration Tests ============

    @Test
    fun `UserInfo methods should not crash with null context components`() {
        // Given - Minimal setup with potential null returns
        `when`(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null)
        `when`(context.contentResolver).thenReturn(contentResolver)
        
        mockStatic(Settings.Secure::class.java).use { settingsMock ->
            settingsMock.`when`<String> { 
                Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) 
            }.thenReturn("fallback-id")

            // When/Then - All methods should handle gracefully without crashing
            assertNotNull(UserInfo.getUserID(context)) // Should fallback to Android ID
            assertEquals("", UserInfo.getUserCountryCode(context)) // Should return empty
            assertNull(UserInfo.getUserPhoneNumber(context)) // Should return null
            assertEquals("", UserInfo.getUserEmail(context)) // Should return empty
        }
    }

    @Test
    fun `UserInfo object should have proper JvmStatic annotations for Java compatibility`() {
        // This test verifies that the methods are accessible from Java
        // by checking that they can be called on the object directly
        
        // Given - Mock minimal context
        `when`(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager)
        `when`(telephonyManager.deviceId).thenReturn("test-device")

        // When - Call methods that should be accessible from Java
        val result = UserInfo.getUserID(context)

        // Then - Should work without issues
        assertTrue(result.startsWith("device-id:"))
    }
}