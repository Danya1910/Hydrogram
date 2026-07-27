package com.example.hydrogram.data.repository

import androidx.compose.runtime.MutableState
import com.example.hydrogram.domain.model.Chat
import com.example.hydrogram.domain.repository.InboxRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class InboxRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : InboxRepository {

    override fun getInboxChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val listener = firestore
            .collection("chats")
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val filteredDocs = snapshot.documents.filter { doc ->
                        doc.id.contains(userId)
                    }

                    if (filteredDocs.isEmpty()) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    val chatList = mutableListOf<Chat>()
                    val listenersCount = 0

                    filteredDocs.forEach { doc ->
                        val baseChat = doc.toObject(Chat::class.java) ?: return@forEach

                        firestore.collection("chats")
                            .document(doc.id)
                            .collection("messages")
                            .whereEqualTo("isRead", false)
                            .addSnapshotListener { msgSnapshot, _ ->

                                val unread = msgSnapshot?.documents?.count { msgDoc ->
                                    val senderId = msgDoc.getString("senderId") ?: ""
                                    senderId != userId
                                } ?: 0

                                val updatedChat = baseChat.copy(unreadCount = unread)

                                chatList.removeAll { it.chatId == updatedChat.chatId }
                                chatList.add(updatedChat)

                                val sortedChats =
                                    chatList.sortedByDescending { it.lastMessageTimestamp }

                                trySend(sortedChats)

                            }

                    }
                }
            }
        awaitClose {
            listener.remove()
        }
    }

}