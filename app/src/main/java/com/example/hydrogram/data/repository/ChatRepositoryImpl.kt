package com.example.hydrogram.data.repository

import android.util.Log
import com.example.hydrogram.data.dto.MessageDto
import com.example.hydrogram.data.wrapper.toDomain
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObjects
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
        message: Message,
    ): Result<Unit> {
        return try {
            val chatRef = firestore.collection("chats").document(chatId)
            val messageRef = chatRef.collection("messages").document()
            val currentTime = System.currentTimeMillis()

            Log.d("ChatRepositoryImpl", "message: $message")

            val targetUserId = chatId.split("_").firstOrNull() { it != senderId } ?: ""

            val (lastMessagePreview, lastMessageType) = when (message) {
                is Message.Text -> {
                    message.text to "text"
                }

                is Message.Sticker -> {
                    "Стикер" to "sticker"
                }

                is Message.Image -> {
                    "Фото" to "image"
                }
            }

            val messageDto = when (message) {
                is Message.Sticker -> {
                    MessageDto(
                        messageId = messageRef.id,
                        senderId = message.senderId,
                        timestamp = message.timestamp,
                        status = message.status,
                        type = "sticker",
                        reactions = null,
                        replyData = message.replyData,
                        stickerPath = message.stickerPath,
                    )
                }

                is Message.Text -> {
                    MessageDto(
                        messageId = messageRef.id,
                        senderId = message.senderId,
                        timestamp = message.timestamp,
                        status = message.status,
                        type = "text",
                        reactions = null,
                        replyData = message.replyData,
                        text = message.text,
                    )
                }

                is Message.Image -> {
                    MessageDto(
                        messageId = messageRef.id,
                        senderId = message.senderId,
                        timestamp = message.timestamp,
                        status = message.status,
                        type = "image",
                        reactions = null,
                        replyData = message.replyData,
                        image = message.image,
                    )
                }
            }

            val chatUpdate = mapOf(
                "chatId" to chatId,
                "members" to listOf(senderId, targetUserId),
                "lastMessage" to lastMessagePreview,
                "lastMessageType" to lastMessageType,
                "lastMessageSenderId" to senderId,
                "lastMessageTimestamp" to currentTime
            )
            val batch = firestore.batch()

            batch.set(messageRef, messageDto)
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
                    val dtos = snapshot.toObjects(MessageDto::class.java)

                    val domainMessages = dtos.map {
                        it.toDomain()
                    }

                    trySend(domainMessages)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun changeMessageStatus(
        chatId: String,
        messageId: String,
        status: String
    ): Result<Unit> {
        return try {
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .update("status", status)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleReaction(
        reaction: String?,
        chatId: String,
        messageId: String,
    ): Result<Unit> {
        return try {

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .update("reactions.$currentUserId", reaction)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> {
        return try {
            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changeMessage(
        chatId: String,
        messageId: String,
        currentMessageType: String,
        typeOfChange: String,
        change: String,
        ): Result<Unit> {
        return try {
            val docRef = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)

            val batch = firestore.batch()

            val updates = mutableMapOf<String, Any>()

            updates["type"] = typeOfChange

            when(typeOfChange) {
                "text" -> updates["text"] = change
                "sticker" -> updates["stickerPath"] = change
                "image" -> updates["image"] = change
            }

            updates["isEdited"] = true

            if (currentMessageType != typeOfChange) {
                when (currentMessageType) {
                    "text" -> updates["text"] = FieldValue.delete()
                    "sticker" -> updates["stickerPath"] = FieldValue.delete()
                    "image" -> updates["image"] = FieldValue.delete()
                }
            }

            batch.update(docRef, updates)

            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}