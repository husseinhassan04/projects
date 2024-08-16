package com.example.noteapp3

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.recyclerview.widget.RecyclerView


class WebViewPagerAdapter(private val context: Context, private val urls: List<String>) :
    RecyclerView.Adapter<WebViewPagerAdapter.WebViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_webview, parent, false)
        return WebViewHolder(view)
    }

    override fun onBindViewHolder(holder: WebViewHolder, position: Int) {
        holder.bind(formatLiveStreamUrl(urls[position]))
    }

    fun formatLiveStreamUrl(url: String): String {
        // Extract the live stream ID from the URL
        val videoId = url.substringAfterLast("live/").substringBefore("?")
        return "https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1&rel=0&fs=1"
    }


    override fun getItemCount(): Int {
        return urls.size
    }

    class WebViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val webView: WebView = itemView.findViewById(R.id.webView)

        fun bind(url: String) {
            webView.webViewClient = WebViewClient()
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true

            // Set WebView to full screen
            webView.settings.loadWithOverviewMode = true
            webView.settings.useWideViewPort = true

            webView.loadUrl(url)

            // Adjust WebView layout parameters
            val layoutParams = webView.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            webView.layoutParams = layoutParams
        }
    }
}
