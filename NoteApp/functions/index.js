const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendNotification = functions.https.onCall((data, context) => {
    const { receiverId, content, senderId } = data;
    console.log(`sendNotification called with receiverId: ${receiverId}, content: ${content}, senderId: ${senderId}`);

    return admin.firestore().collection('users').doc(receiverId).get()
        .then(doc => {
            if (doc.exists) {
                const token = doc.data().token;
                console.log(`Token for receiverId ${receiverId}: ${token}`);

                const payload = {
                    notification: {
                        title: "New Message",
                        body: `New message from ${senderId}`,
                    },
                    data: {
                        messageContent: content,
                    },
                };

                return admin.messaging().sendToDevice(token, payload)
                    .then((response) => {
                        console.log("Successfully sent message:", response);
                        return { success: true };
                    })
                    .catch((error) => {
                        console.log("Error sending message:", error);
                        return { success: false, error: error.message };
                    });
            } else {
                console.log("No such document!");
                return { success: false, error: "No such document!" };
            }
        })
        .catch((error) => {
            console.log("Error getting document:", error);
            return { success: false, error: error.message };
        });
});
