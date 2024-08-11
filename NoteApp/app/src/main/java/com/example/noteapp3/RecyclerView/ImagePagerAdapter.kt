package com.example.noteapp3.RecyclerView

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.example.noteapp3.PhotoPreviewActivity
import com.example.noteapp3.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

class ImagePagerAdapter(private val mediaItems: List<String>, private val context: Context) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view: View
        val item = mediaItems[position]

        if (item.startsWith("vid:content://")) {
            // It's a video URI
            val videoView = inflater.inflate(R.layout.item_post_video, container, false) as PlayerView
            val exoPlayer = ExoPlayer.Builder(context).build()
            videoView.player = exoPlayer

            // Remove "vid:" prefix if it exists
            val uri = Uri.parse(item.removePrefix("vid:"))
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            view = videoView
        } else {
            // It's a Base64 image string
            val imageView = inflater.inflate(R.layout.item_post_image, container, false) as ImageView
            val decodedString = Base64.decode(item, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            imageView.setImageBitmap(bitmap)
            view = imageView
        }

        container.addView(view)
        return view
    }

    override fun getCount(): Int {
        return mediaItems.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        if (view is PlayerView) {
            view.player?.release()
        }
        container.removeView(view)
    }
}
