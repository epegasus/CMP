package dev.pegasus.cmp.managers

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentInformation.ConsentStatus
import com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import dev.pegasus.cmp.interfaces.OnConsentResponse

/**
 * @Author: SOHAIB AHMED
 * @Date: 04,July,2023
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://stackoverflow.com/users/20440272/sohaib-ahmed
 */

class ConsentManager(private val activity: Activity) {

    private var consentInformation: ConsentInformation? = null
    private var onConsentResponse: OnConsentResponse? = null

    val canRequestAds: Boolean get() = consentInformation?.canRequestAds() ?: false

    fun initDebugConsent(deviceId: String, onConsentResponse: OnConsentResponse) {
        this.onConsentResponse = onConsentResponse
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
                Log.d("ConsentManager", "initDebugConsent: Available")
                loadForm()
            }, { error ->
                // Handle the error.
                Log.e("ConsentManager", "initDebugConsent: $error")
                onConsentResponse.onResponse(error.message)
            })
        }
    }

    fun initConsent(onConsentResponse: OnConsentResponse) {
        this.onConsentResponse = onConsentResponse
        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity).also {
            it.requestConsentInfoUpdate(activity, params, {
                if (it.isConsentFormAvailable && it.consentStatus == ConsentStatus.REQUIRED) {
                    Log.d("ConsentManager", "initConsent: Available & Required")
                    loadForm()
                } else {
                    Log.d("ConsentManager", "initConsent: Neither Available nor Required")
                    onConsentResponse.onResponse()
                }
            }, { error ->
                Log.e("ConsentManager", "initConsent: $error")
                onConsentResponse.onResponse(error.message)
            })
        }
    }

    private fun loadForm() {
        // Loads a consent form. Must be called on the main thread.
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
            formError?.let {
                onConsentResponse?.onResponse(it.message)
            } ?: run {
                onConsentResponse?.onResponse()
                checkForPrivacyOptions()
            }
        }
    }

    private fun checkForPrivacyOptions() {
        val isRequired = consentInformation?.privacyOptionsRequirementStatus == PrivacyOptionsRequirementStatus.REQUIRED
        onConsentResponse?.onPolicyRequired(isRequired)
    }

    fun launchPrivacyForm(callback: (formError: FormError?) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            formError?.let {
                Log.e("ConsentManager", "launchPrivacyForm, Error: ${formError.message}")
            } ?: kotlin.run {
                Log.d("ConsentManager", "launchPrivacyForm, Result: Shown")
            }
        }
    }
}