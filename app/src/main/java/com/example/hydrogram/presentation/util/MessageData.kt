package com.example.hydrogram.presentation.util

import com.example.hydrogram.domain.model.Message

data class MessageCallbacks(
    val onReply: (Message) -> Unit,
    val onReplyMessageClick: (String) -> Unit,
    val onDoubleClick: (Boolean) -> Unit,
    val onLongClick: (Boolean) -> Unit,
    val onReactionClick: () -> Unit,
)

data class MessageData(
    val replyName: String,
    val mineId: String,
    val mineAvatar: String,
    val penpalAvatar: String,
)