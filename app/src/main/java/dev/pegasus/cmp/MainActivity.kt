package dev.pegasus.cmp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class MainActivity : AppCompatActivity() {

    private lateinit var consentInformation: ConsentInformation
    private lateinit var consentForm: ConsentForm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initDebugConsent()
        //initConsent()
    }

    private fun initDebugConsent() {
        Log.d("TAG", "asd: ")
        val debugSettings = ConsentDebugSettings.Builder(this)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("C60A9BCFA47C43BBBD0578BFEE354822")
            .build()

        val params = ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(debugSettings)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.reset()
        consentInformation.requestConsentInfoUpdate(this, params, {
            // The consent information state was updated.
            // You are now ready to check if a form is available.
            loadForm()
            Log.d("TAG", "initDebugConsent: success")
        }, {
            // Handle the error.
            Log.d("TAG", "initDebugConsent: $it")
            showToast(it)
        })
    }

    private fun initConsent() {
        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(this, params, {
            // The consent information state was updated.
            // You are now ready to check if a form is available.
            if (consentInformation.isConsentFormAvailable) {
                loadForm()
            }
        }, {
            // Handle the error.
            Log.d("TAG", "initDebugConsent: $it")
            showToast(it)
        })
    }

    private fun loadForm() {
        // Loads a consent form. Must be called on the main thread.
        UserMessagingPlatform.loadConsentForm(this, {
            this.consentForm = it
            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                consentForm.show(this) {
                    if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                        // App can start requesting ads.
                        showToast("Obtained, now we can fetch ads")
                        return@show
                    }
                    showToast("Trying again")
                    // Handle dismissal by reloading form.
                    loadForm()
                }
            }
        }, {
            // Handle the error.
            showToast(it)
        })
    }

    private fun showToast(message: Any) {
        Toast.makeText(this, message.toString(), Toast.LENGTH_SHORT).show()
    }
}