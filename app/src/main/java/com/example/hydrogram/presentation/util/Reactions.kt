package com.example.hydrogram.presentation.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hydrogram.domain.model.Message
import com.example.hydrogram.presentation.widgets.messages.ReactionWidget
import com.example.hydrogram.presentation.widgets.messages.text.MessageReactions
import kotlin.collections.component1
import kotlin.collections.component2


@Composable
fun ReactionsBlock(
    message: Message,
    mineId: String,
    mineAvatar: String,
    penpalAvatar: String,
    onReactionClick: () -> Unit,
    mineColors: Pair<Color, Color>,
    extraPadding: Dp = 0.dp,
) {
    var mineReactionEmoji: String? = null
    var penpalReactionEmoji: String? = null
    message.reactions?.forEach { (key, value) ->
        if (value == null) return@forEach
        if (key == mineId) mineReactionEmoji = value
        else penpalReactionEmoji = value
    }

    val currentReactions = if (mineReactionEmoji != null || penpalReactionEmoji != null) {
        MessageReactions(
            mineReaction = mineReactionEmoji,
            penpalReaction = penpalReactionEmoji,
        )
    } else null

    val haveReaction = currentReactions != null

    var lastReactions by remember { mutableStateOf(currentReactions) }
    var lastMineEmoji by remember { mutableStateOf(mineReactionEmoji) }
    var lastPenpalEmoji by remember { mutableStateOf(penpalReactionEmoji) }

    LaunchedEffect(currentReactions, mineReactionEmoji, penpalReactionEmoji) {
        if (currentReactions != null) {
            lastReactions = currentReactions
            lastMineEmoji = mineReactionEmoji
            lastPenpalEmoji = penpalReactionEmoji
        }
    }

    val displayReactions = currentReactions ?: lastReactions
    val displayMine = if (currentReactions != null) mineReactionEmoji else lastMineEmoji
    val displayPenpal = if (currentReactions != null) penpalReactionEmoji else lastPenpalEmoji

    AnimatedVisibility(
        visible = haveReaction,
        modifier = Modifier.padding(end = extraPadding),
        enter = fadeIn(animationSpec = tween(200)) +
                expandVertically(
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Top
                ),
        exit = fadeOut(animationSpec = tween(180)) +
                shrinkVertically(
                    animationSpec = tween(180, easing = FastOutLinearInEasing),
                    shrinkTowards = Alignment.Top
                ),
    ) {
        val reactions = displayReactions ?: return@AnimatedVisibility
        val mineEmoji = displayMine
        val penpalEmoji = displayPenpal

        val hasBothDifferentReactions = reactions.mineReaction != null &&
                reactions.penpalReaction != null &&
                reactions.mineReaction != reactions.penpalReaction

        Box(modifier = Modifier.padding(end = extraPadding)) {
            if (hasBothDifferentReactions) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ReactionWidget(
                        reactions = MessageReactions(reactions.mineReaction, null),
                        color = mineColors.first,
                        onReactionClick = onReactionClick,
                        mineAvatar = if (mineEmoji != null) mineAvatar else null,
                    )
                    Spacer(Modifier.width(8.dp))
                    ReactionWidget(
                        reactions = MessageReactions(null, reactions.penpalReaction),
                        color = mineColors.second,
                        onReactionClick = onReactionClick,
                        mineAvatar = if (penpalEmoji != null) penpalAvatar else null,
                    )
                }
            } else {
                val isPenpalOnly = reactions.mineReaction == null && reactions.penpalReaction != null
                ReactionWidget(
                    reactions = reactions,
                    color = if (isPenpalOnly) mineColors.second else mineColors.first,
                    onReactionClick = onReactionClick,
                    mineAvatar = if (mineEmoji != null) mineAvatar else null,
                    penpalAvatar = if (penpalEmoji != null) penpalAvatar else null,
                )
            }
        }
    }
}