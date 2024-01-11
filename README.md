# CMP

Google Consent Management Platform

### Step 1

    private val consentManager by lazy { ConsentManager(this) }

### Step 2
    when (BuildConfig.DEBUG) {
        true -> consentManager.initDebugConsent(onConsentResponse = onConsentResponse)
        false -> consentManager.initReleaseConsent(onConsentResponse = onConsentResponse)
    }

### Step 3

    private val onConsentResponse = object : OnConsentResponse {
        override fun onResponse(errorMessage: String?) {
            errorMessage?.let {
                Log.e("TAG", "onResponse: Error: $it")
            }
            loadAds()
        }

        override fun onPolicyRequired(isRequired: Boolean) {
            Log.d("TAG", "onPolicyRequired: Is-Required: $isRequired")
        }
    }
    

## Sample Video


https://github.com/epegasus/CMP/assets/100923337/de02c989-a53e-42a0-af52-c73b6f560f5b
