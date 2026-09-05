package com.example.hydrogram.data.repository

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

                    val removedIds = chatListeners.keys.filter { it !in chatIds }
                    removedIds.forEach { id ->
                        chatListeners[id]?.remove()
                        chatListeners.remove(id)
                        chatsCache.remove(id)
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
                                            val members = chatDoc.get("members") as? List<String> ?: emptyList()

                                            val messageType = lastMsgDoc?.getString("type") ?: "text"
                                            val messageText = when (messageType) {
                                                "text" -> lastMsgDoc?.getString("text") ?: ""
                                                "image" -> "Фотография"
                                                "sticker" -> "Стикер"
                                                else -> "Сообщение"
                                            }

                                            val chat = Chat(
                                                senderId = lastMsgDoc?.getString("senderId") ?: "",
                                                chatId = chatId,
                                                lastMessage = messageText,
                                                lastMessageType = messageType,
                                                lastMessageSenderId = lastMsgDoc?.getString("senderId") ?: "",
                                                lastMessageTimestamp = lastMsgDoc?.getLong("timestamp") ?: 0L,
                                                unreadCount = chatDoc.getLong("unreadCount")?.toInt() ?: 0,
                                                members = members
                                            )

                                            chatsCache[chatId] = chat

                                            val sortedChats = chatsCache.values
                                                .sortedByDescending { it.lastMessageTimestamp }
                                            trySend(sortedChats)
                                        }
                                }

                            chatListeners[chatId] = listener
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