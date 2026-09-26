package com.example.ui

import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AdMob configuration manager.
 * Strictly guarantees that Google test IDs are used during DEBUG development,
 * and production IDs are used for RELEASE builds.
 */
object AdConfig {
    const val GOOGLE_TEST_BANNER_ID = "ca-app-pub-3940256099942544/9214589741"
    const val PROD_BANNER_ID = "ca-app-pub-1254750164635651/3823022406"
    private const val PROD_PLACEHOLDER_PREFIX = "ca-app-pub-0000000000000000"

    private val isMobileAdsInitialized = AtomicBoolean(false)
    private val isConsentRequestInProgress = AtomicBoolean(false)

    /**
     * Executes the Google User Messaging Platform (UMP) consent flow and initializes
     * the Google Mobile Ads SDK once consent requirements are fulfilled or confirmed.
     * Safe to invoke on Activity start; ensures idempotent, single initialization.
     */
    fun initializeWithConsent(activity: android.app.Activity, onComplete: () -> Unit = {}) {
        val appContext = activity.applicationContext
        try {
            val consentInformation = com.google.android.ump.UserMessagingPlatform.getConsentInformation(activity)

            // If ads can already be requested (e.g. non-EEA, or consent previously gathered), initialize immediately
            if (consentInformation.canRequestAds()) {
                initializeMobileAds(appContext)
            }

            // Prevent duplicate simultaneous consent requests
            if (!isConsentRequestInProgress.compareAndSet(false, true)) {
                onComplete()
                return
            }

            val params = com.google.android.ump.ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()

            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    com.google.android.ump.UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity
                    ) { _ ->
                        isConsentRequestInProgress.set(false)
                        if (consentInformation.canRequestAds()) {
                            initializeMobileAds(appContext)
                        }
                        onComplete()
                    }
                },
                { _ ->
                    isConsentRequestInProgress.set(false)
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAds(appContext)
                    }
                    onComplete()
                }
            )
        } catch (_: Exception) {
            // Fallback initialization if UMP is unavailable in the environment
            initializeMobileAds(appContext)
            onComplete()
        }
    }

    /**
     * Initializes the Google Mobile Ads SDK exactly once in the application process.
     */
    fun initializeMobileAds(context: Context) {
        if (!isMobileAdsInitialized.compareAndSet(false, true)) {
            return
        }
        Thread {
            try {
                val requestConfig = RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build()
                MobileAds.setRequestConfiguration(requestConfig)
                MobileAds.initialize(context.applicationContext) {}
            } catch (_: Exception) {}
        }.start()
    }

    /**
     * Checks if privacy options (revoking/changing consent) are required under GDPR/EEA rules.
     */
    fun isPrivacyOptionsRequired(context: Context): Boolean {
        return try {
            com.google.android.ump.UserMessagingPlatform.getConsentInformation(context).privacyOptionsRequirementStatus ==
                com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Presents the Google UMP Privacy Options Form to let users change their consent choice at any time.
     */
    fun showPrivacyOptionsForm(activity: android.app.Activity, onDismiss: () -> Unit = {}) {
        try {
            com.google.android.ump.UserMessagingPlatform.showPrivacyOptionsForm(activity) { _ ->
                onDismiss()
            }
        } catch (_: Exception) {
            onDismiss()
        }
    }

    /**
     * Retrieves the active banner ad unit ID based on build variant.
     * In DEBUG builds, strictly returns Google's official Android banner test ad unit.
     * In RELEASE builds, strictly returns the production unit ID.
     */
    fun getActiveBannerAdUnitId(): String {
        return if (BuildConfig.DEBUG) {
            GOOGLE_TEST_BANNER_ID
        } else {
            val configured = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
            if (configured.isNotBlank() && configured != GOOGLE_TEST_BANNER_ID && !configured.startsWith(PROD_PLACEHOLDER_PREFIX)) {
                configured
            } else {
                PROD_BANNER_ID
            }
        }
    }

    /**
     * Checks if AdMob is active and configured for the current environment.
     */
    fun isAdMobEnabled(): Boolean {
        return getActiveBannerAdUnitId().isNotBlank()
    }
}

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.getActiveBannerAdUnitId()
) {
    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }

    // Verify User Messaging Platform (UMP) / consent requirements if applicable before requesting ads
    val canRequestAds = remember {
        try {
            com.google.android.ump.UserMessagingPlatform.getConsentInformation(context).canRequestAds()
        } catch (_: Exception) {
            true
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
            .testTag("ad_banner_container"),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val screenWidthDp = maxWidth.value.toInt().coerceAtLeast(320)

        // Calculate responsive adaptive banner size across different Android screen sizes
        val adSize = remember(screenWidthDp) {
            try {
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp)
            } catch (_: Exception) {
                AdSize.BANNER
            }
        }
        val adHeightDp = remember(adSize) {
            try {
                val px = adSize.getHeightInPixels(context)
                if (px > 0) with(density) { px.toDp() } else 50.dp
            } catch (_: Exception) {
                50.dp
            }.coerceAtLeast(50.dp)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hairline divider for clear visual separation from application content
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )

            // Standard Ad Header Label (Google Play compliant disclosure)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(3.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "AD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sponsored Partner",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }

            // Banner Display Area (Responsive Adaptive Banner Slot)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(adHeightDp),
                contentAlignment = Alignment.Center
            ) {
                if (canRequestAds && adUnitId.isNotBlank()) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(adHeightDp)
                            .testTag("admob_adview"),
                        factory = { ctx ->
                            try {
                                AdView(ctx).apply {
                                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                    setAdSize(adSize)
                                    setAdUnitId(adUnitId)
                                    adListener = object : AdListener() {
                                        override fun onAdLoaded() {
                                            super.onAdLoaded()
                                            isAdLoaded = true
                                        }

                                        override fun onAdFailedToLoad(error: LoadAdError) {
                                            super.onAdFailedToLoad(error)
                                            isAdLoaded = false
                                        }
                                    }
                                    // Clean generic request: zero user health or wellness info attached
                                    loadAd(AdRequest.Builder().build())
                                }
                            } catch (_: Exception) {
                                isAdLoaded = false
                                View(ctx)
                            }
                        },
                        update = { _ -> },
                        onRelease = { adView ->
                            try {
                                (adView as? AdView)?.destroy()
                            } catch (_: Exception) {}
                        }
                    )
                }

                // Native mindful wellness fallback displayed when waiting for ads, offline, or on load failure
                if (!isAdLoaded || !canRequestAds || adUnitId.isBlank()) {
                    SponsoredFallbackBanner()
                }
            }
        }
    }
}

@Composable
private fun SponsoredFallbackBanner() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.16f)
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = "FastZen Mindful Wellness",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mindful Fasting Tip",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Stay hydrated with clean water & balanced minerals during your fast",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
