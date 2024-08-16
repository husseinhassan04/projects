package com.example.noteapp3

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.noteapp3.models.AppDatabase
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.models.StreamUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LiveStreaming : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private var urls:MutableList<String> = mutableListOf()
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_streaming)

        viewPager = findViewById(R.id.view_pager)

        db = AppDatabase.getDatabase(this)

        // get list of URLs data to load in the WebViews
        loadData()




        createScrollHint(viewPager)

    }
    override fun onResume() {
        super.onResume()
        db = AppDatabase.getDatabase(this)
        DataFetcher.fetchDataAndStore(this,db) {

        }
    }

    private fun createScrollHint(viewPager: ViewPager2) {
        val hintDistance = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 30f, resources.displayMetrics
        ).toInt()

        val animator = ValueAnimator.ofInt(0, hintDistance, 0)
        animator.duration = 1000
        animator.repeatCount = 2
        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            viewPager.translationY = -value.toFloat()
        }

        animator.start()
    }

    private fun loadData() {
        val apiService = RetroFitClient.apiService
        val call = apiService.getAllLiveUrls()

        lifecycleScope.launch {
            call.enqueue(object : Callback<List<StreamUrl>> {
                override fun onResponse(
                    call: Call<List<StreamUrl>>,
                    response: Response<List<StreamUrl>>
                ) {
                    if (response.isSuccessful) {
                        val urlsWithIds = response.body() ?: emptyList()

                        for (url in urlsWithIds) {
                            urls.add(url.url)
                        }
                        val adapter = WebViewPagerAdapter(this@LiveStreaming, urls)
                        viewPager.adapter = adapter

                        viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
                    } else {
                        // Handle error by loading from Room database
                        loadUrlsFromRoom()
                    }
                }

                override fun onFailure(call: Call<List<StreamUrl>>, t: Throwable) {
                    Toast.makeText(this@LiveStreaming, "Connection Error", Toast.LENGTH_SHORT)
                        .show()
                    loadUrlsFromRoom()
                }
            })
        }
    }

    private fun loadUrlsFromRoom() {
        lifecycleScope.launch(Dispatchers.IO) {
            val roomUrls = db.liveStreamUrlsDao().getAllUrls()
            withContext(Dispatchers.Main) {
                for (url in roomUrls) {
                    urls.add(url.url)
                }
                val adapter = WebViewPagerAdapter(this@LiveStreaming, urls)
                viewPager.adapter = adapter

                viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
            }
        }
    }




}
