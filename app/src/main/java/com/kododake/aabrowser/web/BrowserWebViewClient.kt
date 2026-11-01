/*
 * Copyright (C) 2025 AABrowser Contributors (https://github.com/kododake/AABrowser)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://gnu.org>.
 */

package com.kododake.aabrowser.web

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.ClientCertRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewFeature
import com.kododake.aabrowser.R
import com.kododake.aabrowser.data.BrowserPreferences

class BrowserWebViewClient(
    private val callbacks: BrowserCallbacks
) : WebViewClient() {

    // A high-performance set of domains to block
    private val blockedDomains = hashSetOf(
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "adsystem.com",
        "tracker.com",
        "amazon-adsystem.com",
        "scorecardresearch.com",
        "criteo.com",
        "outbrain.com",
        "taboola.com",
        "adnxs.com",
        "rubiconproject.com"
    )

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val url = request.url.toString()
        val host = request.url.host
        
        // --- Extra AdBlock catch-all for common ad URLs ---
        // Block by simple URL matching (often used in simple ad blockers)
        if (url.contains("/ads/") || url.contains("adserver") || url.contains("banner")) {
            return WebResourceResponse("text/plain", "UTF-8", null)
        }
        
        if (host != null) {
            // Check if the domain or its parent domain is in the blocklist
            var currentHost: String = host
            while (currentHost.contains(".")) {
                if (blockedDomains.contains(currentHost)) {
                    // Ad detected! Return an empty response to block it.
                    return WebResourceResponse("text/plain", "UTF-8", null)
                }
                
                // Strip the subdomain (e.g. move from "ads.example.com" to "example.com")
                val nextDot = currentHost.indexOf('.')
                if (nextDot != -1 && nextDot + 1 < currentHost.length) {
                    currentHost = currentHost.substring(nextDot + 1)
                } else {
                    break
                }
            }
        }
        
        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val uri = request.url
        if (CleartextNavigationHandler.handleCleartextIfNeeded(view, uri, callbacks, onPageStart = false)) {
            return true
        }
        return handleUri(uri)
    }

    private fun handleUri(uri: Uri?): Boolean {
        if (uri == null) return false
        val scheme = uri.scheme?.lowercase()
        if (scheme == null || scheme in setOf("http", "https", "about", "file", "data", "javascript")) {
            return false
        }
        return true
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        val stringUrl = url ?: return
        val uri = Uri.parse(stringUrl)
        val scheme = uri.scheme?.lowercase()

        if (scheme == "http") {
            val allowedOnce = view.getTag(R.id.webview_allow_once_uri_tag) as? String
            if (allowedOnce == stringUrl) {
                view.setTag(R.id.webview_allow_once_uri_tag, null)
            } else if (CleartextNavigationHandler.handleCleartextIfNeeded(view, uri, callbacks, onPageStart = true)) {
                return
            }
        }
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        view.evaluateJavascript(SpeechRecognitionBridge.POLYFILL_JS, null)
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            if (BrowserPreferences.isDrmL3EnforcerEnabled(view.context)) {
                view.evaluateJavascript(WebScripts.DRM_L3_ENFORCER_JS, null)
            }
        }
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.NAVIGATION_LISTENER)) {
            url?.let(callbacks.onUrlChange)
        }

        // --- YouTube Ad Skipper Injection ---
        if (url != null && url.contains("youtube.com")) {
            val skipAdsScript = """
                javascript:(function() {
                    function skipYouTubeAds() {
                        // 1. Find the skip button and click it if it exists
                        var skipBtn = document.querySelector('.ytp-skip-ad-button, .ytp-ad-skip-button, .ytp-ad-skip-button-modern');
                        if (skipBtn) {
                            skipBtn.click();
                        }
                        
                        // 2. Find the ad video element and fast forward to the end
                        var adVideo = document.querySelector('.ad-showing video') || document.querySelector('.video-ads video');
                        if (adVideo && !isNaN(adVideo.duration)) {
                            adVideo.currentTime = adVideo.duration;
                        }
                        
                        // 3. Hide banner ads below the video
                        var adBanners = document.querySelectorAll('ytd-ad-slot-renderer, .ytd-companion-slot-renderer, #ad-text:not(:empty), .ytp-ad-overlay-container');
                        adBanners.forEach(banner => {
                            banner.style.display = 'none';
                        });
                        
                        // 4. If the page is "stuck" on a pre-roll ad screen before the video loads, force skip
                        var adModule = document.querySelector('.ytp-ad-module');
                        if (adModule) {
                            adModule.style.display = 'none';
                        }
                    }
                    
                    // Run the function every 100ms in the background (very fast)
                    setInterval(skipYouTubeAds, 100);
                    
                    // Also run it immediately when the DOM changes (grabs ads that load instantly)
                    var observer = new MutationObserver(skipYouTubeAds);
                    observer.observe(document.body, { childList: true, subtree: true });
                })();
            """.trimIndent()
            
            view.evaluateJavascript(skipAdsScript, null)
        }
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        // 1. Is this a background request (like an ad tracker)? Ignore it entirely.
        if (!request.isForMainFrame) {
            return
        }

        val code = error.errorCode
        val shouldShowErrorPage = when (code) {
            ERROR_HOST_LOOKUP,
            ERROR_CONNECT,
            ERROR_TIMEOUT,
            ERROR_UNKNOWN,
            ERROR_PROXY_AUTHENTICATION -> true
            else -> false
        }

        if (shouldShowErrorPage) {
            val failed = request.url?.toString().orEmpty()
            
            // --- Ignore errors caused by our AdBlocker ---
            if (failed.contains("ad") || failed.contains("tracker") || failed.contains("googleadservices")) {
                return // Quietly ignore the error
            }

            val message = error.description?.toString().orEmpty()
            val assetUrl = "file:///android_asset/error.html?failedUrl=${Uri.encode(failed)}&code=$code&message=${Uri.encode(message)}"
            try {
                view.loadUrl(assetUrl)
            } catch (_: Exception) {
                // If loading the error page fails, do nothing instead of showing a toast/message
            }
            return
        }
        // Do not call callbacks.onError for non-critical background errors
    }

    override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
        // 1. Is this a background request (like an ad tracker)? Ignore it entirely.
        if (!request.isForMainFrame) {
            return
        }

        val code = errorResponse.statusCode
        if (code in 400..599 && code != 429) {
            val failed = request.url?.toString().orEmpty()
            
            // --- Ignore errors caused by our AdBlocker on YouTube ---
            if (failed.contains("youtube.com") && (failed.contains("ad") || failed.contains("ptracking") || failed.contains("pagead"))) {
                return // Quietly ignore HTTP errors for blocked YouTube ad trackers
            }

            val message = errorResponse.reasonPhrase.orEmpty()
            val assetUrl = "file:///android_asset/error.html?failedUrl=${Uri.encode(failed)}&code=$code&message=${Uri.encode(message)}"
            try {
                view.loadUrl(assetUrl)
            } catch (_: Exception) {
                // If loading the error page fails, do nothing instead of showing a toast/message
            }
            return
        }
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        val activity = view.context as? Activity
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            SslErrorHandlerHelper.handleSslError(activity, handler, error)
        } else {
            handler.cancel()
        }
    }

    override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
        val activity = view.context as? Activity
        if (activity != null) {
            ClientCertHandler.handleClientCertRequest(activity, request)
        } else {
            request.cancel()
        }
    }

    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
        val parent = view.parent as? ViewGroup
        parent?.removeView(view)
        view.destroy()
        callbacks.onRenderProcessGone(detail.didCrash())
        return true
    }
}
