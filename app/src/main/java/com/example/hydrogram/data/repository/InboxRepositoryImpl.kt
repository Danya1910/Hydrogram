package com.example.hydrogram.data.repository

import android.util.Log
import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.domain.repository.InboxRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class InboxRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : InboxRepository {

    override fun getInboxChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val chatListeners = mutableMapOf<String, ListenerRegistration>()
        val chatsCache = mutableMapOf<String, Chat>()

        val chatsListListener = firestore.collection("chats")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chatIds = snapshot.documents.map { it.id }

                    // Удаляем чаты, которых больше нет
                    val removedIds = chatListeners.keys.filter { it !in chatIds }
                    removedIds.forEach { id ->
                        chatListeners[id]?.remove()
                        chatListeners.remove(id)
                        chatsCache.remove(id)
                    }

                    chatIds.forEach { chatId ->
                        // Проверяем, есть ли сообщения в чате
                        firestore.collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .limit(1)  // Проверяем наличие хотя бы одного сообщения
                            .get()
                            .addOnSuccessListener { messagesSnapshot ->
                                if (messagesSnapshot.isEmpty) {
                                    // В чате нет сообщений - пропускаем его
                                    Log.d("InboxRepository", "Чат $chatId пустой, пропускаем")
                                    chatsCache.remove(chatId)

                                    // Отправляем актуальный список
                                    val sortedChats = chatsCache.values
                                        .sortedByDescending { it.lastMessageTimestamp }
                                    trySend(sortedChats)
                                    return@addOnSuccessListener
                                }

                                // Если сообщения есть - подписываемся на изменения
                                if (!chatListeners.containsKey(chatId)) {
                                    val listener = firestore.collection("chats")
                                        .document(chatId)
                                        .collection("messages")
                                        .orderBy("timestamp", Query.Direction.DESCENDING)
                                        .limit(1)
                                        .addSnapshotListener { msgSnapshot, msgError ->
                                            if (msgError != null) return@addSnapshotListener

                                            val lastMsgDoc = msgSnapshot?.documents?.firstOrNull()

                                            // Если сообщение удалено и чат стал пустым
                                            if (lastMsgDoc == null) {
                                                Log.d("InboxRepository", "Чат $chatId стал пустым, удаляем")
                                                chatsCache.remove(chatId)
                                                val sortedChats = chatsCache.values
                                                    .sortedByDescending { it.lastMessageTimestamp }
                                                trySend(sortedChats)
                                                return@addSnapshotListener
                                            }

                                            firestore.collection("chats")
                                                .document(chatId)
                                                .get()
                                                .addOnSuccessListener { chatDoc ->
                                                    val members = chatDoc.get("members") as? List<String> ?: emptyList()

                                                    val messageType = lastMsgDoc.getString("type") ?: "text"
                                                    val messageText = when (messageType) {
                                                        "text" -> lastMsgDoc.getString("text") ?: ""
                                                        "image" -> "Фотография"
                                                        "sticker" -> "Стикер"
                                                        else -> "Сообщение"
                                                    }

                                                    val lastMessageStatus = if (lastMsgDoc.getString("senderId") == userId) {
                                                        lastMsgDoc.getString("status") ?: "sent"
                                                    } else {
                                                        ""
                                                    }

                                                    // Считаем непрочитанные сообщения
                                                    firestore.collection("chats")
                                                        .document(chatId)
                                                        .collection("messages")
                                                        .whereEqualTo("status", "sent")
                                                        .whereNotEqualTo("senderId", userId)
                                                        .get()
                                                        .addOnSuccessListener { messagesSnapshot ->
                                                            val unreadCount = messagesSnapshot.size()

                                                            val chat = Chat(
                                                                senderId = lastMsgDoc.getString("senderId") ?: "",
                                                                chatId = chatId,
                                                                lastMessage = messageText,
                                                                lastMessageType = messageType,
                                                                lastMessageSenderId = lastMsgDoc.getString("senderId") ?: "",
                                                                lastMessageTimestamp = lastMsgDoc.getLong("timestamp") ?: 0L,
                                                                unreadCount = unreadCount,
                                                                members = members,
                                                                lastMessageStatus = lastMessageStatus,
                                                            )
                                                            Log.d("InboxRepository", "Чат $chatId, unreadCount: $unreadCount")

                                                            chatsCache[chatId] = chat

                                                            val sortedChats = chatsCache.values
                                                                .sortedByDescending { it.lastMessageTimestamp }
                                                            trySend(sortedChats)
                                                        }
                                                        .addOnFailureListener {
                                                            // Если ошибка при подсчете, отправляем с unreadCount = 0
                                                            val chat = Chat(
                                                                senderId = lastMsgDoc.getString("senderId") ?: "",
                                                                chatId = chatId,
                                                                lastMessage = messageText,
                                                                lastMessageType = messageType,
                                                                lastMessageSenderId = lastMsgDoc.getString("senderId") ?: "",
                                                                lastMessageTimestamp = lastMsgDoc.getLong("timestamp") ?: 0L,
                                                                unreadCount = 0,
                                                                members = members,
                                                                lastMessageStatus = lastMessageStatus,
                                                            )

                                                            chatsCache[chatId] = chat
                                                            val sortedChats = chatsCache.values
                                                                .sortedByDescending { it.lastMessageTimestamp }
                                                            trySend(sortedChats)
                                                        }
                                                }
                                                .addOnFailureListener {
                                                    // Если не удалось получить данные чата - пропускаем
                                                    Log.e("InboxRepository", "Ошибка получения чата $chatId", it)
                                                    chatsCache.remove(chatId)
                                                    val sortedChats = chatsCache.values
                                                        .sortedByDescending { it.lastMessageTimestamp }
                                                    trySend(sortedChats)
                                                }
                                        }

                                    chatListeners[chatId] = listener
                                }
                            }
                            .addOnFailureListener { error ->
                                // Если ошибка при проверке сообщений - пропускаем чат
                                Log.e("InboxRepository", "Ошибка проверки сообщений в чате $chatId", error)
                                chatsCache.remove(chatId)
                                val sortedChats = chatsCache.values
                                    .sortedByDescending { it.lastMessageTimestamp }
                                trySend(sortedChats)
                            }
                    }
                }
            }

        awaitClose {
            chatsListListener.remove()
            chatListeners.values.forEach { it.remove() }
            chatListeners.clear()
        }
    }
}