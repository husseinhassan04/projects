package com.example.noteapp3

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.noteapp3.models.AppDatabase
import com.example.noteapp3.models.Post
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.polls.Poll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPollActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var polls: MutableList<Poll> = mutableListOf()
    private lateinit var authorId:String

    private lateinit var acceptButton: ImageButton
    private lateinit var cancelButton: ImageButton

    private lateinit var text: EditText
    private lateinit var opt1: EditText
    private lateinit var opt2 : EditText
    private lateinit var opt3: EditText
    private lateinit var opt4: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_poll)
        authorId = intent.getStringExtra("profile_id")?:"-1"
        lifecycleScope.launch {
            db = AppDatabase.getDatabase(this@AddPollActivity)
            polls = getPollsFromDatabase().toMutableList()
        }

        acceptButton = findViewById(R.id.accept_button)
        cancelButton = findViewById(R.id.cancel_button)

        text = findViewById(R.id.text)
        opt1 = findViewById(R.id.option1)
        opt2 = findViewById(R.id.option2)
        opt3 = findViewById(R.id.option3)
        opt4 = findViewById(R.id.option4)

        //add the poll
        acceptButton.setOnClickListener {
            if(text.text.isEmpty() || opt1.text.isEmpty()|| opt2.text.isEmpty()
                || opt3.text.isEmpty() || opt4.text.isEmpty()
                ){
                Toast.makeText(this, "No field can be empty", Toast.LENGTH_SHORT).show()
            }
            else{
                val poll = Poll()
                poll.text = text.text.toString()
                poll.authorId = authorId
                poll.option1 = opt1.text.toString()
                poll.option2 = opt2.text.toString()
                poll.option3 = opt3.text.toString()
                poll.option4 = opt4.text.toString()
                addPoll(poll)
            }
        }

        cancelButton.setOnClickListener {
            finish()
            Toast.makeText(this, "Poll canceled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addPoll(poll: Poll) {
        val apiService = RetroFitClient.apiService
        val call: Call<Void> = apiService.addPoll(poll)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    //Handle successful posting
                    lifecycleScope.launch {
                        addPollInApp(poll)
                    }
                    val layout = layoutInflater.inflate(R.layout.success_toast,null)
                    Toast(this@AddPollActivity).apply {
                        duration = Toast.LENGTH_SHORT
                        setGravity(Gravity.BOTTOM,0,0)
                        view = layout
                        show()

                    }

                    finish()
                } else {
                    // Handle unsuccessful response
                    val layout = layoutInflater.inflate(R.layout.fail_toast,null)
                    Toast(this@AddPollActivity).apply {
                        duration = Toast.LENGTH_SHORT
                        setGravity(Gravity.BOTTOM,0,0)
                        view = layout
                        show()
                    }

                    finish()

                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Handle failure
                val layout = layoutInflater.inflate(R.layout.fail_toast,null)
                Toast(this@AddPollActivity).apply {
                    duration = Toast.LENGTH_SHORT
                    setGravity(Gravity.BOTTOM,0,0)
                    view = layout
                    show()
                }

                finish()
            }
        })

    }

    @SuppressLint("SuspiciousIndentation")
    private suspend fun addPollInApp(poll: Poll) {
        withContext(Dispatchers.IO) {
            val pollDao = db.pollDao()

            pollDao.addPoll(poll)
            polls.add(poll)

        }
    }

    private suspend fun getPollsFromDatabase(): List<Poll> {
        return withContext(Dispatchers.IO) {
            db.pollDao().getAllPolls()
        }
    }
}