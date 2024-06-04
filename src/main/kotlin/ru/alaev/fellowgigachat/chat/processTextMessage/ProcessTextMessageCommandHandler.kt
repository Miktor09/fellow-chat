package ru.alaev.fellowgigachat.chat.processTextMessage

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import ru.alaev.fellowgigachat.chat.dto.ResponseType.MESSAGE
import ru.alaev.fellowgigachat.chat.dto.message.ChatMessageRequest
import ru.alaev.fellowgigachat.chat.dto.message.ChatMessageResponse
import ru.alaev.fellowgigachat.chat.dto.toCommonResponse
import ru.alaev.fellowgigachat.chat.processTextMessage.saveHistory.SaveHistoryCommand
import ru.alaev.fellowgigachat.chat.processTextMessage.saveHistory.SaveMessageCommandHandler
import ru.alaev.fellowgigachat.domain.Username
import java.util.concurrent.ConcurrentHashMap

@Service
class ProcessTextMessageCommandHandler(
    private val saveMessageCommandHandler: SaveMessageCommandHandler,
    private val objectMapper: ObjectMapper,
) {
    fun handle(command: ProcessTextMessageCommand) {
        val message = command.chatMessage.toDomain(command.from)

        command.sessions[message.to]?.sendMessage(
            TextMessage(
                ChatMessageResponse.from(message).toCommonResponse(MESSAGE).toJson(objectMapper)
            )
        )
        command.sessions[message.from]?.sendMessage(
            TextMessage(
                ChatMessageResponse.from(message).toCommonResponse(MESSAGE).toJson(objectMapper)
            )
        )

        log.info("Message received: ${message.content} for ${message.to.value} from ${message.from.value}")

        saveMessageCommandHandler.handle(SaveHistoryCommand(message))
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}

data class ProcessTextMessageCommand(
    val chatMessage: ChatMessageRequest,
    val from: Username,
    val sessions: ConcurrentHashMap<Username, WebSocketSession>,
)