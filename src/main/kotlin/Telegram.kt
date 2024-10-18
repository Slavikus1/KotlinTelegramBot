import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.lang.Exception
import java.util.*

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String,
    @SerialName("message")
    val message: Message? = null
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>,
)

@Serializable
data class InlineKeyboard(
    @SerialName("text")
    val text: String,
    @SerialName("callback_data")
    val callbackData: String,
)

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

fun main() {
    var lastUpdateId = 0L
    val telegramBotService = TelegramBotService()

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    val json = Json {
        ignoreUnknownKeys = true
    }

    val commandsToReact = listOf("menu", "/start")
    val helloRequest = "Hello"

    while (true) {
        val statistic: Statistics = trainer.getStatistics()

        Thread.sleep(2000)
        val responseString: String = telegramBotService.getUpdates(lastUpdateId)
        val response: Response = json.decodeFromString(responseString)
        println(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdateId = updateId + 1

        val message = firstUpdate.message?.text
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id
        val data = firstUpdate.callbackQuery?.data

        if (chatId == null) continue
        if (message?.lowercase(Locale.getDefault())?.capitalize() == helloRequest) {
            telegramBotService.sendMessage(json, chatId, helloRequest)
        }

        if (message?.lowercase(Locale.getDefault()) in commandsToReact) {
            telegramBotService.sendMenu(json, chatId, helloRequest)
        }

        if (data?.lowercase(Locale.getDefault()) == STATISTICS_CLICKED) {
            telegramBotService.sendMessage(
                json,
                chatId,
                "Выучено ${statistic.learned} из ${statistic.total} | ${statistic.percent}%"
            )
        }

        if (data?.lowercase(Locale.getDefault()) == LEARN_WORDS_CLICKED) {
            telegramBotService.checkNextQuestionAndSend(json, trainer, telegramBotService, chatId)
        }

        if (data?.lowercase(Locale.getDefault())?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
            val userAnswerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(userAnswerIndex)) {
                telegramBotService.sendMessage(json, chatId, "Правильно!")
            } else {
                telegramBotService.sendMessage(
                    json,
                    chatId,
                    "Неправильно! ${trainer.question?.correctAnswer?.questionWord} это ${trainer.question?.correctAnswer?.translate}"
                )
            }
            telegramBotService.checkNextQuestionAndSend(json, trainer, telegramBotService, chatId)
        }
    }
}

