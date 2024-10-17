package com.example.kitabi2.ui.cart


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.kitabi2.R

class CartBottomSheet(private val bookTitle: String, private val listener: (Int) -> Unit) : BottomSheetDialogFragment() {

    private var quantity = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_add_to_cart, container, false)

        val titleTextView: TextView = view.findViewById(R.id.item_title)
        val quantityTextView: TextView = view.findViewById(R.id.quantity_text)
        val increaseButton: ImageButton = view.findViewById(R.id.btn_increase)
        val decreaseButton: ImageButton = view.findViewById(R.id.btn_decrease)
        val addToCartButton: View = view.findViewById(R.id.add_to_cart_button)

        // Set book title
        titleTextView.text = bookTitle

        // Set click listeners for the increase and decrease buttons
        increaseButton.setOnClickListener {
            if (quantity < 99) { // Limit to 99 for example
                quantity++
                quantityTextView.text = quantity.toString()
            }
        }

        decreaseButton.setOnClickListener {
            if (quantity > 1) {
                quantity--
                quantityTextView.text = quantity.toString()
            }
        }

        // Handle "Add to Cart" button click
        addToCartButton.setOnClickListener {
            listener.invoke(quantity)
            dismiss()
        }

        return view
    }
}
