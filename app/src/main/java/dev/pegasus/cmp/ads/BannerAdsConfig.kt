package dev.pegasus.cmp.ads

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.getSystemService
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * @Author: SOHAIB AHMED
 * @Date: 11,January,2024.
 * @Accounts
 *      -> https://github.com/epegasus
 *      -> https://linkedin.com/in/epegasus
 */

class BannerAdsConfig(private val context: Context) {

    fun loadBannerAds(frameLayout: FrameLayout) {
        // request for a new banner ad
        val adRequest = AdRequest.Builder().build()
        val adView = AdView(context)
        adView.adUnitId = "ca-app-pub-3940256099942544/2014213617"
        adView.setAdSize(getAdSize(frameLayout))
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d("TAG", "onAdFailedToLoad: $p0")
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d("TAG", "onAdLoaded: called")
                frameLayout.removeAllViews()
                frameLayout.addView(adView)
            }
        }
        adView.loadAd(adRequest)
    }

    @Suppress("DEPRECATION")
    private fun getAdSize(viewGroup: ViewGroup): AdSize {
        var adWidthPixels: Float = viewGroup.width.toFloat()
        val density = context.resources.displayMetrics.density

        if (adWidthPixels == 0f) {
            adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowManager = context.getSystemService<WindowManager>()
                val bounds = windowManager?.currentWindowMetrics?.bounds
                bounds?.width()?.toFloat() ?: 380f
            } else {
                val display: Display? = context.getSystemService<DisplayManager>()?.getDisplay(Display.DEFAULT_DISPLAY)
                val outMetrics = DisplayMetrics()
                display?.getMetrics(outMetrics)
                outMetrics.widthPixels.toFloat()
            }
        }
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }
}