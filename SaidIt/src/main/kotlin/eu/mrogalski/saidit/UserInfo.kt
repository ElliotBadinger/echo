package eu.mrogalski.saidit

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Patterns
import com.siya.epistemophile.R

/**
 * Modern Kotlin utility object for retrieving user information from Android system services.
 * 
 * This object provides secure access to user identification data including phone numbers,
 * device IDs, country codes, and email addresses. All methods handle security exceptions
 * gracefully and provide fallback mechanisms where appropriate.
 * 
 * @since 2.0.0
 */
object UserInfo {

    /**
     * Retrieves the user's phone number from the device's telephony service.
     * 
     * Requires READ_PHONE_STATE permission. May return null if permission is denied
     * or if the phone number is not available on the device.
     * 
     * @param context The application context
     * @return The phone number string, or null if not available
     * @throws SecurityException if READ_PHONE_STATE permission is not granted
     */
    @JvmStatic
    fun getUserPhoneNumber(context: Context): String? {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            telephonyManager?.line1Number
        } catch (e: SecurityException) {
            // Permission denied - return null gracefully
            null
        }
    }

    /**
     * Retrieves a unique user identifier using a hierarchical fallback strategy.
     * 
     * Priority order:
     * 1. Device ID (if available and permissions granted)
     * 2. Gmail account (first Gmail address found in Google accounts)
     * 3. Android ID (secure settings identifier)
     * 
     * @param context The application context
     * @return A prefixed unique identifier string (device-id:, email:, or android-id:)
     */
    @JvmStatic
    fun getUserID(context: Context): String {
        // First priority: Device ID (requires permission)
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            telephonyManager?.deviceId?.let { deviceId ->
                return "device-id:$deviceId"
            }
        } catch (e: SecurityException) {
            // Permission denied, continue to next fallback
        }

        // Second priority: Gmail account
        try {
            val accountManager = AccountManager.get(context)
            val googleAccounts = accountManager.getAccountsByType("com.google")
            
            googleAccounts.firstOrNull { account ->
                account.name.contains("@gmail.com")
            }?.let { gmailAccount ->
                return "email:${gmailAccount.name}"
            }
        } catch (e: SecurityException) {
            // Permission denied, continue to final fallback
        } catch (e: Exception) {
            // Other exceptions (account service unavailable, etc.)
        }

        // Final fallback: Android ID (always available)
        val androidId = Settings.Secure.getString(
            context.contentResolver, 
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        
        return "android-id:$androidId"
    }

    /**
     * Retrieves the user's country code based on SIM card information.
     * 
     * Maps the SIM country ISO code to a numeric country code using the 
     * country_codes string array resource.
     * 
     * @param context The application context
     * @return The numeric country code (e.g., "1" for US), or empty string if not found
     */
    @JvmStatic
    fun getUserCountryCode(context: Context): String {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val countryIso = telephonyManager?.simCountryIso?.uppercase() ?: return ""
            
            val countryCodesArray = context.resources.getStringArray(R.array.country_codes)
            
            countryCodesArray
                .asSequence()
                .map { it.split(",") }
                .filter { it.size >= 2 }
                .firstOrNull { parts ->
                    parts[1].trim().equals(countryIso.trim(), ignoreCase = true)
                }
                ?.get(0) ?: ""
                
        } catch (e: Exception) {
            // Handle any exceptions gracefully (missing resources, security, etc.)
            ""
        }
    }

    /**
     * Retrieves the first valid email address from the user's accounts.
     * 
     * Searches through all accounts on the device and returns the first one
     * that matches a valid email address pattern.
     * 
     * @param context The application context
     * @return The first valid email address found, or empty string if none found
     */
    @JvmStatic
    fun getUserEmail(context: Context): String {
        return try {
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.accounts
            val emailPattern = Patterns.EMAIL_ADDRESS
            
            accounts
                .asSequence()
                .map { it.name }
                .firstOrNull { accountName ->
                    emailPattern.matcher(accountName).matches()
                } ?: ""
                
        } catch (e: SecurityException) {
            // Permission denied
            ""
        } catch (e: Exception) {
            // Other exceptions (account service unavailable, etc.)
            ""
        }
    }
}