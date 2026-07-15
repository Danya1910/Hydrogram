package com.example.hydrogram.data.repository

import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ChatRepository {

    override suspend fun sendMessage(
        senderId: String,
        chatId: String,
        text: String,
        type: String
    ): Result<Unit> {
        return try {
            val chatRef = firestore.collection("chats").document(chatId)
            val messageRef = chatRef.collection("messages").document()
            val currentTime = System.currentTimeMillis()

            val newMessage = Message(
                messageId = messageRef.id,
                senderId = senderId,
                text = text,
                type = type,
                timestamp = currentTime,
                status = "sent",
            )

            val targetUserId = chatId.split("_").firstOrNull() { it != senderId } ?: ""


            val chatUpdate = mapOf(
                "chatId" to chatId,
                "members" to listOf(senderId, targetUserId),
                "lastMessage" to text,
                "lastMessageType" to type,
                "lastMessageSenderId" to senderId,
                "lastMessageTimestamp" to currentTime
            )
            val batch = firestore.batch()

            batch.set(messageRef, newMessage)
            batch.set(chatRef, chatUpdate, SetOptions.merge())

            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getChatHistory(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)
                    }

                    trySend(messages)
                }
            }
        awaitClose { listener.remove() }
    }

}