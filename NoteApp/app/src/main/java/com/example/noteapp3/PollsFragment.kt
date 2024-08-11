package com.example.noteapp3

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.noteapp3.models.AppDatabase
import com.example.noteapp3.models.RetroFitClient
import com.example.noteapp3.polls.ExpandableListAdapter
import com.example.noteapp3.polls.Poll
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PollsFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var expandableListView: ExpandableListView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: ExpandableListAdapter

    private lateinit var addPollButton: FloatingActionButton
    private var polls: MutableList<Poll> = mutableListOf()
    private lateinit var profileId:String

    private lateinit var pollsFrame: RelativeLayout

    private lateinit var loading: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_polls, container, false)

        pollsFrame = root.findViewById(R.id.polls_frame)
        pollsFrame.setBackgroundResource(R.drawable.polls_empty_bg)
        profileId = arguments?.getString("profile_id") ?: "-1"

        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout)
        loading = root.findViewById(R.id.loading)

        // Initialize ExpandableListView
        expandableListView = root.findViewById(R.id.expandableListView)



        db = AppDatabase.getDatabase(requireContext())

        DataFetcher.fetchDataAndStore(requireContext(),db) {}

        fetchPolls()

        addPollButton = root.findViewById(R.id.floating_btn2)

        addPollButton.setOnClickListener{


            val intent = Intent(requireContext(),AddPollActivity::class.java)
            intent.putExtra("profile_id",profileId)
            startActivity(intent)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout.setOnRefreshListener {
            db = AppDatabase.getDatabase(requireContext())
            DataFetcher.fetchDataAndStore(requireContext(),db) {
                fetchPolls()
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        DataFetcher.fetchDataAndStore(requireContext(),db) {
            fetchPolls()
        }
    }
    private fun fetchPolls() {
        loading.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetroFitClient.apiService.getAllPolls().execute()
                }

                if (response.isSuccessful) {
                    polls = response.body()!!.toMutableList()
                    polls.let {
                        // Save polls to Room database
                        withContext(Dispatchers.IO) {
                            db.pollDao().insertAll(it)
                        }
                        // Update UI with fetched polls
                        adapter = ExpandableListAdapter(requireContext(), it,profileId)
                        expandableListView.setAdapter(adapter)
                    }
                } else {
                    Log.e(ContentValues.TAG, "Retrofit request failed: ${response.code()}")
                    // Fetch polls from Room database on failure
                    val roomPolls = withContext(Dispatchers.IO) {
                        db.pollDao().getAllPolls()
                    }
                    // Update UI with polls from Room
                    adapter = ExpandableListAdapter(requireContext(), roomPolls,profileId)
                    expandableListView.setAdapter(adapter)
                }
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "Error fetching polls", e)
                // Fetch polls from Room database on exception
                val roomPolls = withContext(Dispatchers.IO) {
                    db.pollDao().getAllPolls()
                }
                // Update UI with polls from Room
                adapter = ExpandableListAdapter(requireContext(), roomPolls,profileId)
                expandableListView.setAdapter(adapter)
            }
            finally {
                loading.visibility = View.GONE
                pollsFrame.setBackgroundColor(resources.getColor(R.color.polls_bg))
            }
        }


    }

}
