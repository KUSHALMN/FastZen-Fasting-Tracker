package com.example.ui

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

object AdConfig {
    // Official Google Mobile Ads sample test banner ad unit ID
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
}

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.TEST_BANNER_AD_UNIT_ID,
    showInfoOnClick: Boolean = true
) {
    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }
    var adLoadError by remember { mutableStateOf<String?>(null) }
    var showAdDialog by remember { mutableStateOf(false) }

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
            // Ad Identifier Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showAdDialog = true }
                ) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
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
                        text = if (isAdLoaded) "Google AdMob • Test Mode" else "Sponsored Fasting Partner",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = { showAdDialog = true },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Ad Info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Banner Display Area (Standard 320x50 Banner Slot)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                contentAlignment = Alignment.Center
            ) {
                // Real Google Mobile Ads (AdMob) Banner View
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admob_adview"),
                    factory = { ctx ->
                        try {
                            AdView(ctx).apply {
                                setAdSize(AdSize.BANNER)
                                setAdUnitId(adUnitId)
                                adListener = object : AdListener() {
                                    override fun onAdLoaded() {
                                        super.onAdLoaded()
                                        isAdLoaded = true
                                        adLoadError = null
                                    }

                                    override fun onAdFailedToLoad(error: LoadAdError) {
                                        super.onAdFailedToLoad(error)
                                        isAdLoaded = false
                                        adLoadError = error.message
                                    }
                                }
                                loadAd(AdRequest.Builder().build())
                            }
                        } catch (e: Exception) {
                            isAdLoaded = false
                            adLoadError = e.message ?: "Ads unavailable in container environment"
                            android.view.View(ctx)
                        }
                    },
                    update = { _ -> },
                    onRelease = { adView ->
                        try {
                            (adView as? AdView)?.destroy()
                        } catch (_: Exception) {}
                    }
                )

                // Fallback / Standby Banner when AdMob is initializing or in offline/local emulator mode
                if (!isAdLoaded) {
                    SponsoredFallbackBanner(
                        errorMessage = adLoadError,
                        onClick = { showAdDialog = true }
                    )
                }
            }
        }
    }

    // Ad Information & Configuration Dialog
    if (showAdDialog) {
        AlertDialog(
            onDismissRequest = { showAdDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "AD",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Ad Banner Details")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This app includes an active Google AdMob Banner integration.",
                        fontSize = 14.sp
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Text(
                        text = "Status: ${if (isAdLoaded) "Live AdMob Test Ad Loaded" else "Standby (${adLoadError ?: "Initializing Google Mobile Ads..."})"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAdLoaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Ad Unit ID:\n${adUnitId}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Format: Standard Banner (320x50 dp)\nProvider: Google Mobile Ads SDK (play-services-ads:23.6.0)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAdDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun SponsoredFallbackBanner(
    errorMessage: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.18f)
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Mindful Zen Fasting",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Mindful Fasting Tip",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Stay hydrated with pure water & minerals during your window",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "AD INFO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
