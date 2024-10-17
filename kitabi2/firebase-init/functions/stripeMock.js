const express = require('express');
const bodyParser = require('body-parser');

const app = express();
app.use(bodyParser.json());

// Mock endpoint for creating a payment intent
app.post('/v1/payment_intents', (req, res) => {
    const { amount, currency } = req.body;
    // Simulate a successful payment
    res.json({
        id: 'pi_test_123',
        amount,
        currency,
        status: 'succeeded',
    });
});

// Start the mock server
const PORT = 3000; // Use any port you want
app.listen(PORT, () => {
    console.log(`Stripe mock server running on port ${PORT}`);
});
