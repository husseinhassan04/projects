import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.text.format.DateUtils
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.noteapp3.MessagesPage
import com.example.noteapp3.models.Post
import com.example.noteapp3.R
import com.example.noteapp3.ViewPagerAdapter
import com.example.noteapp3.ViewPagerItem
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.models.profile
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.relex.circleindicator.CircleIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Locale
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PostsAdapter(
    private val context: android.content.Context,
    private var posts: MutableList<Post>,
    private val profileId: String,
    private val listener: OnItemClickListener,
    private val itemClickListener: OnPostItemClickListener
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    private var profiles: Map<String, profile> = emptyMap()

    interface OnPostItemClickListener {
        fun onItemClick(postId: String)
    }

    interface OnItemClickListener {
        fun onButtonEditClick(position: Int)
        fun onButtonDeleteClick(position: Int)
        fun onButtonSaveClick(position: Int, post: Post)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val picture: CircleImageView = itemView.findViewById(R.id.authorPicture)
        val username: TextView = itemView.findViewById(R.id.authorUsername)
        val messageButton: ImageButton = itemView.findViewById(R.id.message)
        val titleTextView: TextView = itemView.findViewById(R.id.title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.description)
        val dateTextView: TextView = itemView.findViewById(R.id.date)
        val viewsTextView: TextView = itemView.findViewById(R.id.views)
        val editButton: ImageButton = itemView.findViewById(R.id.btn_edit)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btn_delete)
        val saveButton: ImageButton = itemView.findViewById(R.id.btn_save_edit)
        val viewPager: ViewPager = itemView.findViewById(R.id.image)
        val indicator: CircleIndicator = itemView.findViewById(R.id.indicator)

        init {
            editButton.setOnClickListener {
                val position = adapterPosition
                descriptionTextView.isFocusableInTouchMode = true
                descriptionTextView.isFocusable = true
                saveButton.visibility = View.VISIBLE
                descriptionTextView.visibility = View.VISIBLE
                editButton.visibility = View.INVISIBLE
                if (position != RecyclerView.NO_POSITION) {
                    listener.onButtonEditClick(position)
                }
                descriptionTextView.requestFocus()
            }

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onButtonDeleteClick(position)
                }
            }

            saveButton.setOnClickListener {
                val updatedDescription = descriptionTextView.text.toString()
                val position = adapterPosition
                saveButton.visibility = View.INVISIBLE
                editButton.visibility = View.VISIBLE
                descriptionTextView.isFocusableInTouchMode = false
                descriptionTextView.isFocusable = false
                if (position != RecyclerView.NO_POSITION) {
                    val post = posts[position]
                    post.desc = updatedDescription
                    listener.onButtonSaveClick(position, post)
                }
                if(descriptionTextView.text.isBlank()){
                    descriptionTextView.visibility = View.GONE
                }
                else{
                    descriptionTextView.visibility = View.VISIBLE
                }
            }
            messageButton.setOnClickListener {
                val context = itemView.context
                AlertDialog.Builder(context)
                    .setMessage("Do you want to chat with ${username.text}?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        // Handle the chat action here
                        Toast.makeText(context, "Starting chat with ${username.text}", Toast.LENGTH_SHORT).show()


                        val intent = Intent(context,MessagesPage::class.java)
                        intent.putExtra("senderId",profileId)
                        intent.putExtra("username",username.text)
                        intent.putExtra("receiverId", posts[adapterPosition].authorId.toString())
                        context.startActivity(intent)

                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            messageButton.setOnLongClickListener {

                val context = itemView.context
                Toast.makeText(context,"Mesaage with ${username.text}",Toast.LENGTH_SHORT).show()
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_feed_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.titleTextView.text = post.title
        holder.descriptionTextView.text = post.desc
        holder.viewsTextView.text = "${post.views} "
        holder.itemView.setOnClickListener { itemClickListener.onItemClick(post.id) }

        // Hide empty desc
        holder.descriptionTextView.visibility = if (holder.descriptionTextView.text.isBlank()) View.GONE else View.VISIBLE

        // Handle author information
        val author = profiles[post.authorId]
        holder.username.text = author?.user ?: "Unknown"
        val pictureBase64 = author?.picture
        val bitmap = decodeBase64(pictureBase64 ?: "")
        holder.picture.setImageBitmap(bitmap ?: BitmapFactory.decodeResource(holder.itemView.context.resources, R.drawable.default_profile_picture))

        // Check if the profileId matches the authorId of the post
        holder.editButton.visibility = if (post.authorId == profileId) View.VISIBLE else View.INVISIBLE
        holder.deleteButton.visibility = holder.editButton.visibility

        // Handle post images and videos
        val imagesAndVideos = post.imgBase64
        if (imagesAndVideos.isNotEmpty()) {
            GlobalScope.launch(Dispatchers.Main) {
                val mediaItems = getMediaItems(imagesAndVideos).toMutableList()
                val adapter = ViewPagerAdapter(mediaItems, holder.itemView.context)
                holder.viewPager.adapter = adapter
            }
            holder.indicator.setViewPager(holder.viewPager)
            holder.viewPager.visibility = View.VISIBLE
            holder.indicator.visibility = View.VISIBLE
        } else {
            holder.viewPager.adapter = null
            holder.viewPager.visibility = View.GONE
            holder.indicator.visibility = View.GONE
        }

        // Handle date and time display
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val postDateTimeString = "${post.date} ${post.time}"
            val postDateTime: Date? = sdf.parse(postDateTimeString)
            val relativeTime = postDateTime?.let {
                DateUtils.getRelativeTimeSpanString(it.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
            } ?: "Unknown"
            holder.dateTextView.text = relativeTime
        } catch (e: Exception) {
            holder.dateTextView.text = "Unknown"
            Log.e(TAG, "Error parsing date and time", e)
        }
    }



    override fun getItemCount(): Int {
        return posts.size
    }

    fun updatePosts(newPosts: MutableList<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    private fun fetchProfiles() {
        val apiService = RetroFitClient.apiService
        val call = apiService.getAllProfiles()

        call.enqueue(object : Callback<List<profile>> {
            override fun onResponse(call: Call<List<profile>>, response: Response<List<profile>>) {
                if (response.isSuccessful) {
                    val profilesList = response.body()
                    profilesList?.let {
                        profiles = it.associateBy { profile -> profile.id }
                        notifyDataSetChanged() // Notify the adapter to refresh the list
                    } ?: run {
                        Log.e(TAG, "Empty response body")
                    }
                } else {
                    Log.e(TAG, "Response not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<profile>>, t: Throwable) {
                Log.e(TAG, "Error fetching profiles", t)
            }
        })
    }

    init {
        fetchProfiles() // Fetch profiles when the adapter is initialized
    }

    private fun decodeBase64(base64Str: String): Bitmap? {
        return try {
            if (base64Str.isNotEmpty()) {
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } else {
                null
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error decoding Base64 string", e)
            null
        }
    }

    private suspend fun getMediaItems(imgBase64: List<String>?): List<ViewPagerItem> {
        if (imgBase64 == null) return emptyList()

        return withContext(Dispatchers.IO) {
            imgBase64.map { item ->
                try {
                    if (item.startsWith("vid:")) {
                        val uri = item.removePrefix("vid:")
                        ViewPagerItem.VideoItem(Uri.parse(uri))
                    } else {
                        val decodedBytes = Base64.decode(item, Base64.DEFAULT)
                        val bitmap =
                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        val imageItem = if (bitmap != null) {
                            // Use Glide to load and resize the image
                            Glide.with(context)
                                .asBitmap()
                                .load(bitmap)
                                .into(
                                    300, 100
                                )
                                .get()
                        } else {
                            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                        }
                        ViewPagerItem.ImageItem(imageItem)
                    }
                } catch (e: Exception) {
                    Log.e("MediaItemDebug", "Error processing media item: $item", e)
                    if (item.startsWith("vid:")) {
                        ViewPagerItem.VideoItem(Uri.EMPTY) // Placeholder or empty URI
                    } else {
                        ViewPagerItem.ImageItem(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888))
                    }
                }
            }
        }
    }

}
