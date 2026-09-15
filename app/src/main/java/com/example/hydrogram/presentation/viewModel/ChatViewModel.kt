package com.example.hydrogram.presentation.viewModel

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hydrogram.domain.model.ReplyData
import com.example.hydrogram.domain.usecase.ChangeMessageStatusUseCase
import com.example.hydrogram.domain.usecase.ChangeMessageUseCase
import com.example.hydrogram.domain.usecase.DeleteMessageUseCase
import com.example.hydrogram.domain.usecase.GetChatHistoryUseCase
import com.example.hydrogram.domain.usecase.GetCurrentUserIdUseCase
import com.example.hydrogram.domain.usecase.SendMessageUseCase
import com.example.hydrogram.domain.usecase.ToggleReactionUseCase
import com.example.hydrogram.presentation.states.ChatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.enums.enumEntries


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val changeMessageStatusUseCase: ChangeMessageStatusUseCase,
    private val toggleReactionUseCase: ToggleReactionUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val changeMessageUseCase: ChangeMessageUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isSending = mutableStateOf(false)
    val isSending = _isSending

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()


    private val _currentId = MutableStateFlow("")
    val currentId = _currentId.asStateFlow()

    private val updatingMessageIds = mutableSetOf<String>()

    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var recordingTime = 0L

    private var isRecordingAmplitudes = false
    val voiceMessageAmplitudes = mutableListOf<Int>()


    fun sendText(
        senderId: String,
        chatId: String,
        text: String,
        replyData: ReplyData? = null,
        targetUserId: String,
        senderName: String,
        senderAvatar: String,
    ) {
        if (text.isBlank()) {
            _errorMessage.value = "Пустое сообщение"
            return
        }
        if (_isSending.value) {
            return
        }

        Log.d("ChatVM", "sent text message called")
        viewModelScope.launch {
            _isSending.value = true
            val result = sendMessageUseCase(
                senderId = senderId,
                chatId = chatId,
                content = text,
                messageType = "text",
                replyData = replyData,
                targetUserId = targetUserId,
                senderName = senderName,
                senderAvatar = senderAvatar,
            )
            _isSending.value = false
            Log.d("ChatVM", "sent text message result: $result")
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка отправки" }
        }
    }

    fun sendSticker(
        senderId: String,
        chatId: String,
        stickerPath: String,
        replyData: ReplyData? = null,
        targetUserId: String,
        senderName: String,
        senderAvatar: String,
    ) {
        if (stickerPath.isBlank()) {
            _errorMessage.value = "Пустой Стикер"
            return
        }
        if (_isSending.value) {
            return
        }
        Log.d("ChatVM", "relay data : $replyData")
        Log.d("ChatVM", "sent sticker message called")
        viewModelScope.launch {
            _isSending.value = true
            val result = sendMessageUseCase(
                senderId = senderId,
                chatId = chatId,
                content = stickerPath,
                messageType = "sticker",
                replyData = replyData,
                targetUserId = targetUserId,
                senderName = senderName,
                senderAvatar = senderAvatar,
            )
            _isSending.value = false
            Log.d("ChatVM", "sent sticker message result: $result")
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка отправки" }
        }
    }

    fun sendImage(
        senderId: String,
        chatId: String,
        imageUri: Uri,
        replyData: ReplyData? = null,
        targetUserId: String,
        senderName: String,
        senderAvatar: String,
    ) {
        if (_isSending.value) {
            return
        }
        Log.d("ChatVM", "sent image message called")
        viewModelScope.launch {
            _isSending.value = true
            val result = sendMessageUseCase(
                senderId = senderId,
                chatId = chatId,
                messageType = "image",
                imageUri = imageUri,
                replyData = replyData,
                targetUserId = targetUserId,
                senderName = senderName,
                senderAvatar = senderAvatar,
            )
            _isSending.value = false
            Log.d("ChatVM", "sent image message result: $result")
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка отправки" }
        }
    }

    fun startRecording() {
        try {
            voiceMessageAmplitudes.clear()
            recordingTime = System.currentTimeMillis()
            Log.d("Recording", "recording starts")

            val file = File(context.cacheDir, "voice_msg_${recordingTime}.m4a")
            currentRecordingFile = file

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)

                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            isRecordingAmplitudes = true
            viewModelScope.launch(Dispatchers.Default) {
                delay(50)
                while (isRecordingAmplitudes) {
                    val maxAmplitude = try {
                        mediaRecorder?.maxAmplitude ?: 0
                    } catch (e: Exception) {
                        0
                    }

                    val normalized = (maxAmplitude / 6553.5).toInt().coerceIn(1, 50)
                    voiceMessageAmplitudes.add(normalized)

                    delay(100)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            currentRecordingFile = null
            isRecordingAmplitudes = false
        }
    }

    fun stopAndSendRecording(
        senderId: String,
        chatId: String,
        replyData: ReplyData? = null,
        targetUserId: String,
        senderName: String,
        senderAvatar: String,
    ) {
        Log.d("Recording", "stopAndSendRecording вызвана!")
        isRecordingAmplitudes = false
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
        }
        val file = currentRecordingFile ?: return
        val endOfRecordingTime = System.currentTimeMillis()
        val durationSeconds = ((endOfRecordingTime - recordingTime) / 1000).toInt()

        if(durationSeconds >= 1) {
            viewModelScope.launch {

                val finalAmplitudes = getTelegramStyleAmplitudes(voiceMessageAmplitudes, durationSeconds)

                Log.d("Recording", "recording sending")

                _isSending.value = true
                val result = sendMessageUseCase(
                    senderId = senderId,
                    chatId = chatId,
                    messageType = "voice",
                    audio = file,
                    voiceDuration = durationSeconds,
                    replyData = replyData,
                    targetUserId = targetUserId,
                    senderName = senderName,
                    senderAvatar = senderAvatar,
                    recordingAmplitudes = finalAmplitudes,
                )
                Log.d("Recording", "recording result: $result")

                voiceMessageAmplitudes.clear()

                _isSending.value = false
                Log.d("ChatVM", "sent image message result: $result")
                result
                    .onSuccess { _isSuccess.value = true }
                    .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка отправки" }
            }
        } else {
            file.delete()
        }
        currentRecordingFile = null
    }

    fun cancelRecording() {
        isRecordingAmplitudes = false
        try {
            Log.d("Recording", "stop recording")

            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
        }

        voiceMessageAmplitudes.clear()

        currentRecordingFile?.delete()
        currentRecordingFile = null

    }

    override fun onCleared() {
        super.onCleared()
        isRecordingAmplitudes = false
        voiceMessageAmplitudes.clear()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    fun deleteMessage(
        chatId: String,
        messageId: String,
    ) {
        if (_isSending.value) {
            return
        }
        viewModelScope.launch {
            viewModelScope.launch {
                _isSending.value = true

                val result = deleteMessageUseCase(
                    chatId = chatId,
                    messageId = messageId,
                )

                _isSending.value = false

                result
                    .onSuccess { _isSuccess.value = true }
                    .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка удаления" }
            }
        }
    }

    fun changeMessage(
        chatId: String,
        messageId: String,
        currentMessageType: String,
        typeOfChange: String,
        change: String,
    ) {
        if (_isSending.value) {
            return
        }
        viewModelScope.launch {
            _isSending.value = true

            val result = changeMessageUseCase(
                chatId = chatId,
                messageId = messageId,
                currentMessageType = currentMessageType,
                typeOfChange = typeOfChange,
                change = change,
            )

            _isSending.value = false

            result
                .onSuccess { _isSuccess.value = true }
                .onFailure { _errorMessage.value = it.localizedMessage ?: "Ошибка удаления" }
        }
    }

    fun toggleReaction(
        reaction: String?,
        chatId: String,
        messageId: String,
    ) {

        if (chatId.isEmpty() || messageId.isEmpty()) {
            return
        }

        if (_isSending.value) {
            return
        }

        viewModelScope.launch {
            _isSending.value = true

            Log.d("ChatVM", "chatId: $chatId, messageId: $messageId")

            val result = toggleReactionUseCase(
                reaction = reaction,
                chatId = chatId,
                messageId = messageId,
            )

            _isSending.value = false

            Log.d("ChatVM", "toggle reaction result: $result")

            result
                .onSuccess { _isSuccess.value = true }
                .onFailure {
                    _errorMessage.value = it.localizedMessage ?: "Ошибка изменения реакции"
                }

        }
    }

    fun changeMessageStatus(
        chatId: String,
        messageId: String,
        status: String,
    ) {
        if (updatingMessageIds.contains(messageId)) {
            return
        }
        if (chatId.isBlank() || messageId.isBlank()) {
            _errorMessage.value = "Такого сообщения нет"
            return
        }
        updatingMessageIds.add(messageId)
        viewModelScope.launch {
            _isSending.value = true
            val result = changeMessageStatusUseCase(
                chatId = chatId,
                messageId = messageId,
                status = status,
            )
            _isSending.value = false
            result
                .onSuccess { _isSuccess.value = true }
                .onFailure {
                    _errorMessage.value = it.localizedMessage ?: ("Ошибка изменения" +
                            " статуса сообщения")
                    updatingMessageIds.remove(messageId)
                }
        }
    }

    fun resetSendStatus() {
        _isSuccess.value = false
    }

    fun getCurrentUserId() {
        viewModelScope.launch {
            val result = getCurrentUserIdUseCase()
            if (!result.isNullOrEmpty()) {
                _currentId.value = result
            } else return@launch
        }
    }

    fun observeChatHistory(
        chatId: String,
    ) {
        if (chatId.isBlank()) {
            _uiState.value = ChatUiState.Error("Чат не найден")
            return
        }
        viewModelScope.launch {
            getChatHistoryUseCase(
                chatId = chatId
            ).catch { exception ->
                _uiState.value = ChatUiState.Error(
                    exception.localizedMessage ?: "Не удалось загрузить сообщения"
                )
            }
                .collect { messages ->
                    _uiState.value = ChatUiState.Success(messages)
                }
        }
    }

    private fun getTelegramStyleAmplitudes(rawAmplitudes: List<Int>, durationSeconds: Int): List<Int> {
        if (rawAmplitudes.isEmpty()) return emptyList()

        // 1. Убираем "мусорные" нули (единицы после нормализации) с самого начала и конца записи,
        // чтобы не было пустых плоских заборов, как на скриншоте
        val trimmed = rawAmplitudes.dropWhile { it <= 1 }.dropLastWhile { it <= 1 }
        val dataToProcess = if (trimmed.size >= 5) trimmed else rawAmplitudes

        // 2. Вычисляем целевое количество палочек по логарифмической шкале Telegram
        val targetSpikesCount = when {
            durationSeconds <= 1 -> 8
            durationSeconds <= 2 -> 12
            durationSeconds <= 3 -> 15 // Для 3 секунд делаем строго 15 палочек!
            durationSeconds <= 5 -> 20
            durationSeconds <= 10 -> 26
            else -> 35 // Жесткий потолок, чтобы карточка не раздувалась
        }

        val compressed = mutableListOf<Int>()
        val step = dataToProcess.size.toFloat() / targetSpikesCount

        for (i in 0 until targetSpikesCount) {
            val startIdx = (i * step).toInt().coerceIn(0, dataToProcess.lastIndex)
            val endIdx = ((i + 1) * step).toInt().coerceIn(0, dataToProcess.lastIndex)

            // Вместо слепого копирования берем МАКСИМАЛЬНОЕ значение на этом отрезке времени.
            // Это сделает график выразительным, выделяя именно пики речи.
            val subList = dataToProcess.subList(startIdx, (endIdx + 1).coerceAtMost(dataToProcess.size))
            val maxVal = subList.maxOrNull() ?: 1

            compressed.add(maxVal)
        }

        // 3. Сглаживание (Moving Average): чтобы палочки плавно росли и убывали, а не прыгали хаотично
        val smoothed = mutableListOf<Int>()
        for (i in compressed.indices) {
            val prev = if (i > 0) compressed[i - 1] else compressed[i]
            val curr = compressed[i]
            val next = if (i < compressed.lastIndex) compressed[i + 1] else compressed[i]

            // Среднее значение между соседями сглаживает "острые" заборы
            smoothed.add((prev + curr + next) / 3)
        }

        return smoothed
    }

}