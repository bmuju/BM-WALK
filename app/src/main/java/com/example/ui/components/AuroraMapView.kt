package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.GeoPoint
import com.example.ui.theme.AuroraBackground

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuroraMapView(
    currentLocation: GeoPoint?,
    routePoints: List<GeoPoint>,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    onMapReady: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#081B2B"))
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(isInteractive)
                builtInZoomControls = false
                displayZoomControls = false
            }
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onMapReady?.invoke()
                }
            }
            loadUrl("file:///android_asset/map.html")
        }
    }

    // Push new location updates to map
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            val js = "if (window.updateLivePosition) { window.updateLivePosition(${loc.latitude}, ${loc.longitude}, ${loc.accuracy}, false); }"
            webView.evaluateJavascript(js, null)
        }
    }

    // Push route updates
    LaunchedEffect(routePoints.size) {
        if (routePoints.isNotEmpty()) {
            val lastPoint = routePoints.last()
            val js = "if (window.addRoutePoint) { window.addRoutePoint(${lastPoint.latitude}, ${lastPoint.longitude}); }"
            webView.evaluateJavascript(js, null)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuroraBackground)
    ) {
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize()
        )
    }
}
