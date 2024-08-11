package com.example.noteapp3

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

sealed class ViewPagerItem {
    data class ImageItem(val bitmap: Bitmap) : ViewPagerItem()
    data class VideoItem(val uri: Uri) : ViewPagerItem()
}

class ViewPagerAdapter(
    private val items: MutableList<ViewPagerItem>,
    private val context: Context
) : PagerAdapter() {

    override fun getCount(): Int = items.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val item = items[position]
        val inflater = LayoutInflater.from(context)

        val view = when (item) {
            is ViewPagerItem.ImageItem -> {
                val imageView = inflater.inflate(R.layout.item_post_image, container, false) as ImageView
                imageView.setImageBitmap(item.bitmap)
                imageView.setOnClickListener {
                    PhotoPreviewActivity.imageBitmap = item.bitmap
                    val intent = Intent(context, PhotoPreviewActivity::class.java)
                    context.startActivity(intent)
                }
                imageView
            }
            is ViewPagerItem.VideoItem -> {
                val videoView = inflater.inflate(R.layout.item_post_video, container, false) as PlayerView
                val exoPlayer = ExoPlayer.Builder(context).build()
                videoView.player = exoPlayer
                exoPlayer.setMediaItem(MediaItem.fromUri(item.uri))
                exoPlayer.prepare()
                //exoPlayer.playWhenReady = true

                videoView.setOnClickListener {
                    val intent = Intent(context, VideoPreviewActivity::class.java).apply {
                        putExtra("video_uri", item.uri)
                    }
                    context.startActivity(intent)
                }
                videoView
            }
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (items[position] is ViewPagerItem.VideoItem) {
            val playerView = `object` as PlayerView
            playerView.player?.release()
        }
        container.removeView(`object` as View)
    }
}
