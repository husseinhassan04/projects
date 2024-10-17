const express = require('express');
const stripe = require('stripe')('sk_test_51QA4KzRsEwZBJdQR5qWNGBvddFlVmjZgO8e2dpMa2KGa03sjvtLdeaDcefTiKloFuSBJtaMGtWnKZVIFvqrOgbdI002m9CaAAh'); // Replace with your Stripe secret key
const bodyParser = require('body-parser');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 5000;

app.use(cors());
app.use(bodyParser.json());

// Define a route for the root URL
app.get('/', (req, res) => {
    res.send('Welcome to the Payment API'); // This will display when you access the root URL
});

// Endpoint to create a PaymentIntent
app.post('/create-payment-intent', async (req, res) => {
    const { amount, currency } = req.body;

    try {
        const paymentIntent = await stripe.paymentIntents.create({
            amount: amount,
            currency: currency,
        });
        res.status(200).send({ clientSecret: paymentIntent.client_secret });
    } catch (error) {
        console.error('Error creating payment intent:', error);
        res.status(500).send({ error: error.message });
    }
});

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});
