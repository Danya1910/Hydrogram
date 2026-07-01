package com.example.hydrogram.data.repository

import com.example.hydrogram.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ChatRepository {


}