package com.example.core.ads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdWidget(
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current

    if (isPreview) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("AdMob Banner Test Slot", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF171717))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdManager.TEST_BANNER_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
