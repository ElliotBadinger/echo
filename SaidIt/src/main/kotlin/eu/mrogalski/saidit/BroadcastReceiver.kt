package eu.mrogalski.saidit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Modern Kotlin BroadcastReceiver for handling system broadcasts and starting the SaidIt service.
 * 
 * This receiver is responsible for automatically starting the SaidItService when appropriate
 * system events occur, but only if the user has completed the tutorial. The class follows
 * modern Android development patterns with proper null safety and error handling.
 * 
 * Key features:
 * - Null-safe parameter handling with Kotlin's type system
 * - Safe SharedPreferences access with proper context handling
 * - Defensive programming against potential context issues
 * - Modern Kotlin syntax while maintaining Java compatibility
 * 
 * @since 2.0.0
 */
class BroadcastReceiver : android.content.BroadcastReceiver() {
    
    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     * 
     * This method safely checks if the tutorial has been completed before starting
     * the SaidItService. It uses modern Kotlin null safety patterns and defensive
     * programming to handle potential context or intent issues gracefully.
     * 
     * @param context The Context in which the receiver is running. May be null in rare cases.
     * @param intent The Intent being received. May be null in rare cases.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        // Defensive null check - context should never be null in normal operation,
        // but we handle it gracefully to prevent crashes
        context ?: return
        
        try {
            // Get SharedPreferences using the application context for consistency
            // This prevents potential memory leaks and ensures we get the correct preferences
            val sharedPreferences = context.applicationContext
                .getSharedPreferences(SaidIt.PACKAGE_NAME, Context.MODE_PRIVATE)
            
            // Check if tutorial has been completed before starting service
            // Default to false if preference doesn't exist (safe default)
            val tutorialCompleted = sharedPreferences.getBoolean("skip_tutorial", false)
            
            if (tutorialCompleted) {
                // Start the SaidItService using explicit intent for better security
                val serviceIntent = Intent(context, SaidItService::class.java)
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            // Defensive error handling - log the error but don't crash
            // In production, this could be logged to crash reporting service
            android.util.Log.w("BroadcastReceiver", "Error in onReceive: ${e.message}", e)
        }
    }
}