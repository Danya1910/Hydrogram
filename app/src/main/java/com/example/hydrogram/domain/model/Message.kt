package com.example.hydrogram.domain.model

sealed interface Message {
    val messageId: String
    val senderId: String
    val type: String
    val timestamp: Long
    val status: String
    val reactions: Map<String, String>?
    val replyData: ReplyData?
    val isEdited: Boolean

    data class Text(
        override val messageId: String = "",
        override val senderId: String = "",
        override val type: String = "text",
        override val status: String = "sent",
        override val timestamp: Long = 0L,
        override val reactions: Map<String, String>? = null,
        override val replyData: ReplyData? = null,
        override val isEdited: Boolean = false,
        val text: String? = "",
    ) : Message

    data class Sticker(
        override val messageId: String = "",
        override val senderId: String = "",
        override val type: String = "sticker",
        override val status: String = "sent",
        override val timestamp: Long = 0L,
        override val reactions: Map<String, String>? = null,
        override val replyData: ReplyData? = null,
        override val isEdited: Boolean = false,
        val stickerPath: String? = "",
    ) : Message

    data class Image(
        override val messageId: String = "",
        override val senderId: String = "",
        override val type: String = "image",
        override val status: String = "sent",
        override val timestamp: Long = 0L,
        override val reactions: Map<String, String>? = null,
        override val replyData: ReplyData? = null,
        override val isEdited: Boolean = false,
        val image: String? = "",
    ) : Message


}

data class ReplyData(
    val messageId: String = "",
    val senderId: String = "",
    val type: String = "",
    val content: String = "",
)
