package dev.pegasus.cmp.managers

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * @Author: SOHAIB AHMED
 * @Date: 04,July,2023
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

class ConsentManager(private val activity: Activity) {

    private var consentInformation: ConsentInformation? = null
    private var consentForm: ConsentForm? = null
    private var callback: ((errorMessage: String?) -> Unit)? = null

    fun initDebugConsent(deviceId: String, callback: (errorMessage: String?) -> Unit) {
        this.callback = callback
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId(deviceId)
            .build()

        val params = ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(debugSettings)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity).also {
            it.reset()
            it.requestConsentInfoUpdate(activity, params, {
                // The consent information state was updated.
                // You are now ready to check if a form is available.
                Log.d("TAG", "initDebugConsent: Available")
                loadForm()
            }, { error ->
                // Handle the error.
                Log.e("TAG", "initDebugConsent: $error")
                callback.invoke(error.message)
            })
        }
    }

    fun initConsent(callback: (errorMessage: String?) -> Unit) {
        this.callback = callback
        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity).also {
            it.requestConsentInfoUpdate(activity, params, {
                if (it.isConsentFormAvailable) {
                    Log.d("TAG", "initConsent: Available")
                    loadForm()
                } else {
                    callback.invoke(null)
                }
            }, { error ->
                Log.e("TAG", "initConsent: $error")
                callback.invoke(error.message)
            })
        }
    }

    private fun loadForm() {
        // Loads a consent form. Must be called on the main thread.
        UserMessagingPlatform.loadConsentForm(activity, {
            this.consentForm = it
            if (consentInformation?.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                Log.d("TAG", "loadForm: showing")
                it.show(activity) {
                    if (consentInformation?.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                        // App can start requesting ads.
                        callback?.invoke("Obtained, now we can fetch ads")
                        return@show
                    }
                    callback?.invoke("Failed! Trying again")
                }
            } else {
                callback?.invoke("Consent form not required")
            }
        }, { error ->
            // Handle the error.
            Log.e("TAG", "loadForm: ${error.message}")
            callback?.invoke(error.message)
        })
    }
}