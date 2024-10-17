package com.example.kitabi2.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kitabi2.KitabiApplication
import com.example.kitabi2.R
import com.example.kitabi2.adapters.SearchResultAdapter
import com.example.kitabi2.database.Book
import com.example.kitabi2.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: SearchResultAdapter
    private lateinit var filterBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //search result
        var result:String=""

        //search edit text
        val editText = root.findViewById<EditText>(R.id.search)
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                result= editText.text.toString().trim()


                val app = requireActivity().application as KitabiApplication

                var books:List<Book> = emptyList()
                books = app.booksList.filter { it.title.contains(result,ignoreCase = true)
                        || it.author.contains(result, ignoreCase = true) }

                // Set up RecyclerView
                recyclerView = root.findViewById(R.id.recycler_view) // Assuming you have a RecyclerView with this ID
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                bookAdapter = SearchResultAdapter(requireContext(), books)
                recyclerView.adapter = bookAdapter

                // Hide keyboard after search
                hideKeyboard()

                true // Return true to indicate the action has been handled
            } else {
                false
            }
        }

        //filtering
        filterBtn = root.findViewById(R.id.filter)

        filterBtn.setOnClickListener{
            showPopupFilter(it,result)
        }


        return root
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun showPopupFilter(view: View,result: String) {
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.search_filter_popup, null)

        // Create the popup window
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true // Focusable
        )

        // Handle apply button
        popupView.findViewById<Button>(R.id.apply_button).setOnClickListener {
            val selectedCategory = when (popupView.findViewById<RadioGroup>(R.id.radioGroup).checkedRadioButtonId) {
                R.id.default_filter -> "Default"
                R.id.search_by_title -> "Search By Title"
                R.id.search_by_author -> "Search By Author"
                else -> ""
            }
            // Apply the filter
            applyFilter(selectedCategory, result )
            popupWindow.dismiss() // Close the popup
        }

        // Show the popup window at a specific position relative to the button
        popupWindow.showAsDropDown(view, 0, 0)
    }

    private fun applyFilter(category: String,result:String) {
        if(category.equals("Default")){

            val app = requireActivity().application as KitabiApplication

            var books:List<Book> = emptyList()
            books = app.booksList.filter { it.title.contains(result,ignoreCase = true)
                    || it.author.contains(result, ignoreCase = true) }

            // Set up RecyclerView
            recyclerView = binding.root.findViewById(R.id.recycler_view) // Assuming you have a RecyclerView with this ID
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            bookAdapter = SearchResultAdapter(requireContext(), books)
            recyclerView.adapter = bookAdapter

        }
        else if(category.equals("Search By Title")){

            val app = requireActivity().application as KitabiApplication

            var books:List<Book> = emptyList()
            books = app.booksList.filter { it.title.contains(result,ignoreCase = true) }

            // Set up RecyclerView
            recyclerView = binding.root.findViewById(R.id.recycler_view) // Assuming you have a RecyclerView with this ID
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            bookAdapter = SearchResultAdapter(requireContext(), books)
            recyclerView.adapter = bookAdapter

        }
        else if(category.equals("Search By Author")){

            val app = requireActivity().application as KitabiApplication

            var books:List<Book> = emptyList()
            books = app.booksList.filter { it.author.contains(result, ignoreCase = true) }

            // Set up RecyclerView
            recyclerView = binding.root.findViewById(R.id.recycler_view) // Assuming you have a RecyclerView with this ID
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            bookAdapter = SearchResultAdapter(requireContext(), books)
            recyclerView.adapter = bookAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}