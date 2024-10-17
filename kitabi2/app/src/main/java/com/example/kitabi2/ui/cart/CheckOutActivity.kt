package com.example.kitabi2.ui.cart

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kitabi2.R
import com.example.kitabi2.ThankYouActivity
import com.example.kitabi2.roomdb.AppDatabase
import com.stripe.android.Stripe
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.view.CardInputWidget
import com.stripe.android.model.ConfirmPaymentIntentParams
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class CheckOutActivity : AppCompatActivity() {

    private lateinit var totalPriceLabel: TextView
    private lateinit var cardInputWidget: CardInputWidget
    private lateinit var checkoutButton: Button

    private lateinit var stripe: Stripe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        // Initialize Stripe with your publishable key
        stripe = Stripe(applicationContext, "pk_test_51QA4KzRsEwZBJdQR2kpKA3p2CUvOVC3wi2mHXTf6QgNmB7Ccs4pSg9V2OgJJIaJ1Z2sbniDO76ViE0laaqJr1Jrn00g4cBmQMc")

        // Initialize the views
        totalPriceLabel = findViewById(R.id.total_price_label)
        cardInputWidget = findViewById(R.id.stripe_card_input_widget)
        checkoutButton = findViewById(R.id.checkout_button)

        // Get total price from intent and display it
        val totalPrice = intent.getStringExtra("totalPrice") ?: "0.0"
        totalPriceLabel.text = "Total Price: $$totalPrice"

        // Handle the checkout button click
        checkoutButton.setOnClickListener {
            handlePayment()
        }
    }

    // Function to handle payment
    private fun handlePayment() {
        val cardDetails = cardInputWidget.paymentMethodCreateParams
        if (cardDetails != null) {
            val totalPrice = intent.getStringExtra("totalPrice")?.toDoubleOrNull() ?: 0.0
            createPaymentIntent(totalPrice, cardDetails)
        } else {
            Toast.makeText(this, "Please enter valid card details.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createPaymentIntent(amount: Double, cardDetails: PaymentMethodCreateParams) {
        val serverUrl = "http://192.168.0.107:5000/create-payment-intent"  // Replace with your server URL

        // Create a JSON object with the amount and currency
        val requestBody = JSONObject().apply {
            put("amount", (amount * 100).toInt()) // Amount in cents
            put("currency", "usd") // Change this to your desired currency
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(serverUrl)
            .post(RequestBody.create("application/json".toMediaType(), requestBody.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val clientSecret = JSONObject(responseBody).getString("clientSecret")
                    confirmPayment(clientSecret, cardDetails)

                    // Show success toast
                    runOnUiThread {
                        Toast.makeText(this@CheckOutActivity, "Payment successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@CheckOutActivity, ThankYouActivity::class.java))
                        val db = AppDatabase.getDatabase(this@CheckOutActivity)
                        lifecycleScope.launch {
                            db.bookDao().emptyCart()
                        }
                        finish()
                    }
                } else {
                    // Show error toast with the HTTP status code and message
                    runOnUiThread {
                        Toast.makeText(
                            this@CheckOutActivity,
                            "Payment failed: ${response.code} - ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CheckOutActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    println("Error: ${e.message}")
                }
            }
        })
    }


    private fun confirmPayment(clientSecret: String, cardDetails: PaymentMethodCreateParams) {
        // Create ConfirmPaymentIntentParams
        val params = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(cardDetails, clientSecret)

        // Confirm the payment
        stripe.confirmPayment(this, params)
    }
}
