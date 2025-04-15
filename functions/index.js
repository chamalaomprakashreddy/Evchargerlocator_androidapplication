const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendChatNotification = functions.database
  .ref("/chats/{chatId}/{messageId}")
  .onCreate(async (snapshot, context) => {
    const message = snapshot.val();
    const { receiverId, senderId, message: text } = message;

    if (!receiverId || !senderId || !text) {
      console.warn("⚠️ Incomplete message data:", message);
      return null;
    }

    try {
      const tokenSnapshot = await admin.database().ref(`/users/${receiverId}/deviceToken`).once("value");
      const token = tokenSnapshot.val();

      if (!token) {
        console.warn(`⚠️ No FCM token found for user ${receiverId}`);
        return null;
      }

      const senderSnapshot = await admin.database().ref(`/users/${senderId}/fullName`).once("value");
      const senderName = senderSnapshot.val() || "Chat User"; // ✅ Needed

      const payload = {
        token: token,
        data: {
          title: `Message from ${senderName}`,
          body: text,
          senderId: senderId,
          senderName: senderName, // ✅ INCLUDE THIS
          receiverId: receiverId
        }
      };

      const response = await admin.messaging().send(payload);
      console.log("✅ Notification sent successfully:", response);
    } catch (error) {
      console.error("❌ Failed to send notification:", error);
    }

    return null;
  });
