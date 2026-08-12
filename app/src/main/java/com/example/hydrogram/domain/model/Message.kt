package com.example.hydrogram.domain.model

sealed interface Message {
    val messageId: String
    val senderId: String
    val type: String
    val timestamp: Long
    val status: String

    data class Text(
        override val messageId: String = "",
        override val senderId: String = "",
        override val type: String = "text",
        override val status: String = "sent",
        override val timestamp: Long = 0L,
        val text: String? = "",
    ) : Message

    data class Sticker(
        override val messageId: String = "",
        override val senderId: String = "",
        override val type: String = "sticker",
        override val status: String = "sent",
        override val timestamp: Long = 0L,
        val stickerPath: String? = "",
    ) : Message

}