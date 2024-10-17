package com.example.kitabi2.ui.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kitabi2.KitabiApplication
import com.example.kitabi2.adapters.CategoriesAdapter
import com.example.kitabi2.database.Category
import com.example.kitabi2.databinding.FragmentCatalogBinding

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoriesAdapter: CategoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the fragment layout
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RecyclerView
        val recyclerView = binding.verticalRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        // Get the instance of KitabiApplication to access the loaded books and categories
        val kitabiApp = requireActivity().application as KitabiApplication

        // Prepare categories with loaded books, filtering out empty categories
        val categories = kitabiApp.categoriesList.map { category ->
            Category(category.id, category.Category, kitabiApp.booksList.filter { it.category == category.id })
        }.filter { it.books.isNotEmpty() }

        categoriesAdapter = CategoriesAdapter(categories,requireContext())
        recyclerView.adapter = categoriesAdapter

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
