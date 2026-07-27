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
        val messageListenersMap = mutableMapOf<String, ListenerRegistration>()
        val currentChatsMap = mutableMapOf<String, Chat>()

        val mainChatsListener = firestore.collection("chats")
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chatDocuments = snapshot.documents.filter { doc ->
                        doc.id.contains(userId)
                    }

                    if (chatDocuments.isEmpty()) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val currentDocIds = chatDocuments.map { it.id }
                    val removedIds = currentChatsMap.keys.filter { it !in currentDocIds }
                    removedIds.forEach { id ->
                        currentChatsMap.remove(id)
                        messageListenersMap[id]?.remove()
                        messageListenersMap.remove(id)
                    }

                    chatDocuments.forEach { doc ->
                        val baseChat = doc.toObject(Chat::class.java) ?: return@forEach
                        val chatId = doc.id

                        val existingChat = currentChatsMap[chatId]
                        currentChatsMap[chatId] = baseChat.copy(
                            unreadCount = existingChat?.unreadCount ?: 0
                        )

                        if (!messageListenersMap.containsKey(chatId)) {
                            val msgListener = firestore.collection("chats")
                                .document(chatId)
                                .collection("messages")
                                .whereEqualTo("status", "sent")
                                .addSnapshotListener { msgSnapshot, msgError ->
                                    if (msgError != null) return@addSnapshotListener

                                    val unread = msgSnapshot?.documents?.count { msgDoc ->
                                        val senderId = msgDoc.getString("senderId") ?: ""
                                        senderId != userId
                                    } ?: 0

                                    val latestBaseChat = currentChatsMap[chatId] ?: baseChat
                                    currentChatsMap[chatId] = latestBaseChat.copy(unreadCount = unread)

                                    val sortedList = currentChatsMap.values.sortedByDescending { it.lastMessageTimestamp }
                                    trySend(sortedList)
                                }

                            messageListenersMap[chatId] = msgListener
                        } else {
                            val sortedList = currentChatsMap.values.sortedByDescending { it.lastMessageTimestamp }
                            trySend(sortedList)
                        }
                    }
                }
            }

        awaitClose {
            mainChatsListener.remove()
            messageListenersMap.values.forEach { it.remove() }
            messageListenersMap.clear()
            currentChatsMap.clear()
        }
    }

}