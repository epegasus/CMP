package dev.pegasus.cmp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dev.pegasus.cmp.ads.BannerAdsConfig
import dev.pegasus.cmp.databinding.ActivityMainBinding
import dev.pegasus.cmp.interfaces.OnConsentResponse
import dev.pegasus.cmp.managers.ConsentManager

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val consentManager by lazy { ConsentManager(this) }
    private val bannerAdsConfig by lazy { BannerAdsConfig(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        showConsent()

        binding.mbPrivacy.setOnClickListener {
            consentManager.launchPrivacyForm {
                binding.mtvStatus.text = getString(R.string.status, it.toString())
            }
        }
    }

    private fun showConsent() {
        binding.mtvStatus.text = getString(R.string.status, "Gathering Consent Information...")
        when (BuildConfig.DEBUG) {
            true -> consentManager.initDebugConsent(onConsentResponse = onConsentResponse)
            false -> consentManager.initReleaseConsent(onConsentResponse = onConsentResponse)
        }
    }

    private val onConsentResponse = object : OnConsentResponse {
        override fun onResponse(errorMessage: String?) {
            binding.mtvStatus.text = getString(R.string.status, "On Response: Error: $errorMessage")
            errorMessage?.let {
                Log.e("TAG", "onResponse: Error: $it")
            }
            loadAds()
        }

        override fun onPolicyRequired(isRequired: Boolean) {
            Log.d("TAG", "onPolicyRequired: Is-Required: $isRequired")
            binding.mbPrivacy.isEnabled = isRequired
        }
    }

    @Suppress("DEPRECATION")
    private fun loadAds() {
        // Load ad if permitted
        val canLoadAd = consentManager.canRequestAds
        Log.d("TAG", "loadAds: $canLoadAd")
        if (!canLoadAd) {
            binding.mtvStatus.text = getString(R.string.status, "Cannot load Ads")
            binding.frameLayout.removeAllViews()
            return
        }
        binding.progressBar.visibility = View.VISIBLE
        bannerAdsConfig.loadBannerAds(binding.frameLayout)
    }
}