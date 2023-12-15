package dev.pegasus.cmp

import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import dev.pegasus.cmp.databinding.ActivityMainBinding
import dev.pegasus.cmp.interfaces.OnConsentResponse
import dev.pegasus.cmp.managers.ConsentManager

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val consentManager by lazy { ConsentManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("TAG", "onCreate: called")
        when (BuildConfig.DEBUG) {
            true -> consentManager.initDebugConsent("4348F5520B1A7098C5EB2B68F959C10B", onConsentResponse)
            false -> consentManager.initConsent(onConsentResponse)
        }
    }

    private val onConsentResponse = object : OnConsentResponse {
        override fun onResponse(errorMessage: String?) {
            errorMessage?.let {
                Log.e("TAG", "onResponse: Error: $it")
            }
            loadAds()
        }

        override fun onPolicyRequired(isRequired: Boolean) {
            Log.d("TAG", "onPolicyRequired: Is-Required: $isRequired")
            // Show Button in setting screen
        }
    }

    private fun loadAds() {
        // Load ad if permitted
        val canLoadAd = consentManager.canRequestAds
        Log.d("TAG", "loadAds: $canLoadAd")

        if (!canLoadAd) return

        val adRequest = AdRequest.Builder().build()
        val adView = AdView(this)
        adView.adUnitId = "ca-app-pub-3940256099942544/2014213617"
        adView.setAdSize(getAdSize(binding.frameLayout))
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d("TAG", "onAdFailedToLoad: $p0")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d("TAG", "onAdLoaded: called")
            }
        }
        adView.loadAd(adRequest)
    }

    @Suppress("DEPRECATION")
    private fun getAdSize(viewGroup: ViewGroup): AdSize {
        var adWidthPixels: Float = viewGroup.width.toFloat()
        val density = resources.displayMetrics.density

        if (adWidthPixels == 0f) {
            adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowManager = getSystemService<WindowManager>()
                val bounds = windowManager?.currentWindowMetrics?.bounds
                bounds?.width()?.toFloat() ?: 380f
            } else {
                val display: Display? = getSystemService<DisplayManager>()?.getDisplay(Display.DEFAULT_DISPLAY)
                val outMetrics = DisplayMetrics()
                display?.getMetrics(outMetrics)
                outMetrics.widthPixels.toFloat()
            }
        }
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }
}