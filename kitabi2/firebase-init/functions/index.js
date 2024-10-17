const functions = require('firebase-functions');
const axios = require('axios');

exports.createPaymentIntent = functions.https.onRequest(async (req, res) => {
    try {
        const response = await axios.post('http://localhost:3000/v1/payment_intents', {
            amount: req.body.amount,
            currency: 'usd',
        });

        res.status(200).json(response.data);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});
