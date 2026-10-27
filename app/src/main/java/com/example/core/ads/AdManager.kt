package com.example.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private const val TAG = "GoldLedgerAdManager"

    // Official Google Mobile Ads Sample Test Ad Unit IDs
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var lastInterstitialShowTime = 0L
    private const val INTERSTITIAL_MIN_INTERVAL_MS = 60_000L // 60 seconds frequency cap

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob Initialized: $status")
            }
            loadInterstitial(context)
            loadRewarded(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MobileAds", e)
        }
    }

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            TEST_INTERSTITIAL_ID,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial failed to load: ${error.message}")
                }
            }
        )
    }

    fun showInterstitialIfAllowed(activity: Activity, onClosed: () -> Unit = {}) {
        val now = System.currentTimeMillis()
        if (now - lastInterstitialShowTime < INTERSTITIAL_MIN_INTERVAL_MS) {
            // Frequency capped - proceed without showing ad
            onClosed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    lastInterstitialShowTime = System.currentTimeMillis()
                    loadInterstitial(activity)
                    onClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onClosed()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitial(activity)
            onClosed()
        }
    }

    fun loadRewarded(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            TEST_REWARDED_ID,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissed: () -> Unit,
        onNotAvailable: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewarded(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    loadRewarded(activity)
                    onNotAvailable()
                }
            }
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            }
        } else {
            loadRewarded(activity)
            onNotAvailable()
        }
    }
}
