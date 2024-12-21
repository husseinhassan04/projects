const mongoose = require('mongoose');

const uri = "mongodb+srv://sis_zouz_hass:5201314@cluster0.sdjkv.mongodb.net/sis?retryWrites=true&w=majority";

async function connectToDatabase() {
    try {
        await mongoose.connect(uri, {
            useNewUrlParser: true,
            useUnifiedTopology: true,
        });
        console.log("Connected to MongoDB Atlas successfully.");
    } catch (error) {
        console.error("Error connecting to MongoDB Atlas:", error);
    }
}

module.exports = connectToDatabase;
