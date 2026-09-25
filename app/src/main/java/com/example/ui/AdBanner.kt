package com.example.ui

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig
import com.example.util.DeviceUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * AdMob configuration manager.
 * Test IDs are strictly confined to DEBUG builds and will NEVER be used in RELEASE builds.
 */
object AdConfig {
    // Official Google Mobile Ads test IDs — strictly restricted to DEBUG builds
    const val DEBUG_TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val DEBUG_TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    // Placeholder indicator for unconfigured release builds
    const val PROD_PLACEHOLDER_PREFIX = "ca-app-pub-0000000000000000"

    /**
     * Retrieves the active banner ad unit ID based on build variant.
     * In DEBUG builds, returns the official Google test unit ID.
     * In RELEASE builds, returns the production unit ID if configured, or empty string if not yet provided.
     */
    fun getActiveBannerAdUnitId(): String {
        return if (BuildConfig.DEBUG) {
            DEBUG_TEST_BANNER_AD_UNIT_ID
        } else {
            val configured = BuildConfig.ADMOB_BANNER_AD_UNIT_ID
            if (configured.isNotBlank() && !configured.startsWith(PROD_PLACEHOLDER_PREFIX)) {
                configured
            } else {
                ""
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
    var isAdLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            .testTag("ad_banner_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            // Banner Display Area (Standard 320x50 Banner Slot)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!DeviceUtils.isEmulator && adUnitId.isNotBlank()) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admob_adview"),
                        factory = { ctx ->
                            try {
                                AdView(ctx).apply {
                                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                    setAdSize(AdSize.BANNER)
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

                // Native mindful wellness fallback displayed when waiting for ads or in offline mode
                if (DeviceUtils.isEmulator || !isAdLoaded || adUnitId.isBlank()) {
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
