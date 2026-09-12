package com.example.hydrogram.data.repository

import android.util.Log
import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.domain.repository.InboxRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InboxRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : InboxRepository {

    override fun getInboxChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val chatListeners = mutableMapOf<String, ListenerRegistration>()
        val unreadListeners = mutableMapOf<String, ListenerRegistration>()

        val chatsCache = mutableMapOf<String, Chat>()
        val unreadCountsCache = mutableMapOf<String, Int>()

        val chatsListListener = firestore.collection("chats")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chatIds = snapshot.documents.map { it.id }

                    if (chatIds.isEmpty()) {
                        chatsCache.clear()
                        unreadCountsCache.clear()
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val removedIds = chatListeners.keys.filter { it !in chatIds }
                    removedIds.forEach { id ->
                        chatListeners[id]?.remove()
                        chatListeners.remove(id)

                        unreadListeners[id]?.remove()
                        unreadListeners.remove(id)

                        chatsCache.remove(id)
                        unreadCountsCache.remove(id)
                    }

                    chatIds.forEach { chatId ->
                        if (!chatListeners.containsKey(chatId)) {

                            val listener = firestore.collection("chats")
                                .document(chatId)
                                .collection("messages")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .addSnapshotListener { msgSnapshot, msgError ->
                                    if (msgError != null) return@addSnapshotListener

                                    val lastMsgDoc = msgSnapshot?.documents?.firstOrNull()

                                    firestore.collection("chats")
                                        .document(chatId)
                                        .get()
                                        .addOnSuccessListener { chatDoc ->
                                            if (!chatDoc.exists()) return@addOnSuccessListener

                                            val members = chatDoc.get("members") as? List<String>
                                                ?: emptyList()
                                            val messageType =
                                                lastMsgDoc?.getString("type") ?: "text"
                                            val messageText = when (messageType) {
                                                "text" -> lastMsgDoc?.getString("text") ?: ""
                                                "image" -> "Фотография"
                                                "sticker" -> "Стикер"
                                                else -> "Сообщение"
                                            }

                                            val lastMessageStatus =
                                                if (lastMsgDoc?.getString("senderId") == userId) {
                                                    lastMsgDoc.getString("status") ?: "sent"
                                                } else {
                                                    ""
                                                }

                                            val currentUnread = unreadCountsCache[chatId]
                                                ?: chatsCache[chatId]?.unreadCount
                                                ?: 0

                                            val chat = Chat(
                                                senderId = lastMsgDoc?.getString("senderId") ?: "",
                                                chatId = chatId,
                                                lastMessage = messageText,
                                                lastMessageType = messageType,
                                                lastMessageSenderId = lastMsgDoc?.getString("senderId")
                                                    ?: "",
                                                lastMessageTimestamp = lastMsgDoc?.getLong("timestamp")
                                                    ?: 0L,
                                                unreadCount = currentUnread,
                                                members = members,
                                                lastMessageStatus = lastMessageStatus,
                                            )

                                            chatsCache[chatId] = chat
                                            emitSortedChats(chatsCache)
                                        }
                                }
                            chatListeners[chatId] = listener

                            val unreadListener = firestore.collection("chats")
                                .document(chatId)
                                .collection("messages")
                                .whereEqualTo("status", "sent")
                                .addSnapshotListener { messagesSnapshot, messagesError ->
                                    if (messagesError != null) return@addSnapshotListener

                                    if (messagesSnapshot != null) {
                                        val unreadCount = messagesSnapshot.documents.count { doc ->
                                            doc.getString("senderId") != userId
                                        }

                                        unreadCountsCache[chatId] = unreadCount

                                        val existingChat = chatsCache[chatId]
                                        if (existingChat != null) {
                                            chatsCache[chatId] =
                                                existingChat.copy(unreadCount = unreadCount)
                                            emitSortedChats(chatsCache)
                                        }
                                    }
                                }
                            unreadListeners[chatId] = unreadListener
                        }
                    }
                }
            }

        awaitClose {
            chatsListListener.remove()
            chatListeners.values.forEach { it.remove() }
            chatListeners.clear()
            unreadListeners.values.forEach { it.remove() }
            unreadListeners.clear()
            unreadCountsCache.clear()
        }
    }

    private fun ProducerScope<List<Chat>>.emitSortedChats(chatsCache: Map<String, Chat>) {
        val sortedChats = chatsCache.values
            .filter { it.lastMessageTimestamp != 0L }
            .sortedByDescending { it.lastMessageTimestamp }

        trySend(sortedChats)
    }

    override suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            val messagesSnapshot = firestore
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .get()
                .await()

            val batch = firestore.batch()
            messagesSnapshot.documents.forEach { document ->
                batch.delete(document.reference)
            }

            if (messagesSnapshot.documents.isNotEmpty()) {
                batch.commit().await()
            }

            firestore.collection("chats")
                .document(chatId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
